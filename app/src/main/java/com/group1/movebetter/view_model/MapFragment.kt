package com.group1.movebetter.view_model

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil.inflate
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.*
import com.group1.movebetter.R
import com.group1.movebetter.databinding.FragmentMapBinding
import com.group1.movebetter.repository.Repository
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource


class MapFragment : Fragment(), OnMapReadyCallback, PermissionsListener, MapboxMap.OnMapClickListener {

    private lateinit var BIKE_STATION_LAYER: String
    private lateinit var BIKE_STATIONS: String
    private lateinit var BIKE_NETWORK_LAYER: String
    private lateinit var BIKE_NETWORKS: String
    private lateinit var SELECTED_MARKER: String
    private lateinit var SELECTED_MARKER_LAYER: String

    private lateinit var BIKE_ICON_ID: String
    private lateinit var NETWORK_ICON_ID: String
    private lateinit var SCOOTER_ICON_ID: String

    private var markerAnimator: ValueAnimator? = null
    private var markerSelected = false

    private lateinit var mapView: MapView;
    private lateinit var mapboxMap: MapboxMap
    private var permissionsManager: PermissionsManager? = null

    private lateinit var mapViewModel: MapViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.let { Mapbox.getInstance(it, getString(R.string.mapbox_access_token)) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val binding : FragmentMapBinding = inflate(inflater, R.layout.fragment_map, container, false)
        mapView = binding.mapView

        // Get a reference to the ViewModel associated with this fragment.
        val repository = Repository();
        val viewModelFactory = MapViewModelFactory(repository)
        mapViewModel = ViewModelProvider(this, viewModelFactory).get(MapViewModel::class.java)
        binding.mapViewModel = mapViewModel;

        binding.lifecycleOwner = this

        initIds()

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        mapViewModel.mapController.getCurrentLocation(this.requireActivity(), context, this)

        mapViewModel.cityBikeController.getNetworks()

        return binding.root
    }

    private fun initIds() {
        BIKE_STATION_LAYER = resources.getString(R.string.BIKE_STATION_LAYER)
        BIKE_STATIONS = resources.getString(R.string.BIKE_STATIONS)
        BIKE_NETWORK_LAYER = resources.getString(R.string.BIKE_NETWORK_LAYER)
        BIKE_NETWORKS = resources.getString(R.string.BIKE_NETWORKS)
        SELECTED_MARKER = resources.getString(R.string.SELECTED_MARKER)
        SELECTED_MARKER_LAYER = resources.getString(R.string.SELECTED_MARKER_LAYER)

        BIKE_ICON_ID = resources.getString(R.string.BIKE_ICON_ID)
        NETWORK_ICON_ID = resources.getString(R.string.NETWORK_ICON_ID)
        SCOOTER_ICON_ID = resources.getString(R.string.SCOOTER_ICON_ID)
    }

    override fun onMapClick(point: LatLng): Boolean {
        val style = mapboxMap.style
        if (style != null) {
            // when card view is shown and user clicks on map, make it invisible
            checkIfCardViewVisible()

            val pixel = mapboxMap.projection.toScreenLocation(point)

            val bikeNetworks = mapboxMap.queryRenderedFeatures(pixel, BIKE_NETWORK_LAYER)
            val bikeStation = mapboxMap.queryRenderedFeatures(pixel, BIKE_STATION_LAYER)
            val selectedFeature = mapboxMap.queryRenderedFeatures(pixel, SELECTED_MARKER_LAYER)

            val selectedMarkerLayer = style.getLayer(SELECTED_MARKER_LAYER) as SymbolLayer

            // when clicked on a bikeNetwork get the stations via REST
            if (bikeNetworks.isNotEmpty()) {
                resetSelectedMarkerLayer(style)
                mapViewModel.cityBikeController.getNetwork(bikeNetworks[0]!!.getStringProperty("id"))
            }

            // when clicked on icon which was already clicked on, show card view
            if (selectedFeature.size > 0 && markerSelected) {
                adaptCardView(selectedFeature[0])
                return false
            }

            // when clicked on map and a marker is selected, deselect it
            if (bikeStation.isEmpty()) {
                if (markerSelected) {
                    deselectMarker(selectedMarkerLayer, style, true)
                }
                return false
            }

            // TODO differentiate between layers of bikes, scooters and stops of NVV
            // Add picture to Selected_Marker_layer dynamically with an property in clicked icon
            // e.g. "provider" = bikeStation or bikeNetwork or scooter or train
            val source = style.getSourceAs<GeoJsonSource>(SELECTED_MARKER)
            source?.setGeoJson(FeatureCollection.fromFeature(bikeStation[0]))

            // check if an icon is already selected
            if (markerSelected) {
                deselectMarker(selectedMarkerLayer, style, false)
            }
            // if clicked on a bike station, make it bigger and show information of that bike station
            if (bikeStation.size > 0) {
                selectMarker(selectedMarkerLayer)
                adaptCardView(bikeStation[0])
            }
        }
        return true
    }

    private fun resetSelectedMarkerLayer(style: Style) {
        val source = style.getSourceAs<GeoJsonSource>(SELECTED_MARKER)
        source?.setGeoJson(FeatureCollection.fromFeatures(arrayOf()))
    }

    private fun selectMarker(iconLayer: SymbolLayer) {
        markerAnimator = ValueAnimator()
        markerAnimator!!.setObjectValues(0.3f, 1f)
        markerAnimator!!.duration = 300
        markerAnimator!!.addUpdateListener { animator ->
            iconLayer.setProperties(
                    iconSize(animator.animatedValue as Float)
            )
        }
        markerAnimator!!.start()
        markerSelected = true
    }

    private fun deselectMarker(iconLayer: SymbolLayer, style: Style, clickedOnMap: Boolean) {
        markerAnimator!!.setObjectValues(1f, 0.3f)
        markerAnimator!!.duration = 300
        markerAnimator!!.addUpdateListener { animator ->
            iconLayer.setProperties(
                    iconSize(animator.animatedValue as Float)
            )
        }
        if (clickedOnMap) {
            markerAnimator!!.doOnEnd {
                // Reset selected-marker-source
                val source = style.getSourceAs<GeoJsonSource>(SELECTED_MARKER)
                source?.setGeoJson(FeatureCollection.fromFeatures(arrayOf()))
            }
        }
        markerAnimator!!.start()
        markerSelected = false
    }

    private fun adaptCardView(feature: Feature) {
        val name = this.activity?.findViewById<TextView>(R.id.textView_title)
        name!!.text = feature.getStringProperty("name")
        val freeBikes = this.activity?.findViewById<TextView>(R.id.textView_freeBikes)
        freeBikes!!.text = "Free Bikes = ${feature.getNumberProperty("freeBikes")}"
        val emptySlots = this.activity?.findViewById<TextView>(R.id.textView_emptySlots)
        emptySlots!!.text = "Empty Slots = ${feature.getNumberProperty("emptySlots")}"
        val timestamp = this.activity?.findViewById<TextView>(R.id.textView_timestamp)
        timestamp!!.text = feature.getStringProperty("timestamp")


        val cardView = this.activity?.findViewById<CardView>(R.id.single_location_cardView)
        cardView!!.visibility = View.VISIBLE
    }

    private fun checkIfCardViewVisible() {
        val cardView = this.activity?.findViewById<CardView>(R.id.single_location_cardView)

        /*if (cardView == null) {
            val frameLayout = this.activity?.findViewById<FrameLayout>(R.id.frameLayout)
            frameLayout!!.addView(this.activity!!.layoutInflater.inflate(R.layout.cardview_symbol_layer, null))
            cardView = this.activity?.findViewById<CardView>(R.id.single_location_cardView)
        }*/

        cardView!!.visibility = View.GONE
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap

        mapboxMap.setStyle(Style.MAPBOX_STREETS) {
            loadImages(it)
            enableLocationComponent(it)
            setupLayers(it)
            mapboxMap.addOnMapClickListener(this@MapFragment)
        }
    }

    private fun setupLayers(style: Style) {
        // Bike Network Layer
        style.addSource(GeoJsonSource(BIKE_NETWORKS))

        style.addLayer(SymbolLayer(BIKE_NETWORK_LAYER, BIKE_NETWORKS)
                .withProperties(iconImage(NETWORK_ICON_ID),
                        iconAllowOverlap(false),
                        iconSize(0.3f))
                .withFilter(eq((get("show")), literal(true))))

        // Bike Stations Layer
        style.addSource(GeoJsonSource(BIKE_STATIONS))

        style.addLayer(SymbolLayer(BIKE_STATION_LAYER, BIKE_STATIONS)
                .withProperties(iconImage(BIKE_ICON_ID),
                        iconAllowOverlap(false),
                        iconSize(0.3f)))

        // Selected Icon Layer
        style.addSource(GeoJsonSource(SELECTED_MARKER))

        style.addLayer(SymbolLayer(SELECTED_MARKER_LAYER, SELECTED_MARKER)
                .withProperties(iconImage(BIKE_ICON_ID),
                iconSize(0.3f)))

        // Add data to layers
        mapViewModel.cityBikeController.getResponseNetworks.observe(viewLifecycleOwner, Observer {
            val networkSource = style.getSourceAs<GeoJsonSource>(BIKE_NETWORKS)
            mapViewModel.mapController.getNearestNetwork(it)
            mapViewModel.mapController.createFeatureList(networkSource, it, mapViewModel.cityBikeController)
        })

        mapViewModel.cityBikeController.getResponseNetwork.observe(viewLifecycleOwner, Observer {
            val networkSource = style.getSourceAs<GeoJsonSource>(BIKE_NETWORKS)
            val stationSource = style.getSourceAs<GeoJsonSource>(BIKE_STATIONS)
            mapViewModel.mapController.exchangeNetworkWithStations(networkSource, stationSource, it.network)
        })
    }

    private fun loadImages(style: Style) {
        style.addImage(BIKE_ICON_ID, BitmapFactory.decodeResource(this.resources, R.raw.bike))
        style.addImage(NETWORK_ICON_ID, BitmapFactory.decodeResource(this.resources, R.raw.network))
        style.addImage(SCOOTER_ICON_ID, BitmapFactory.decodeResource(this.resources, R.raw.scooter))
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
// Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(context)) {

// Create and customize the LocationComponent's options
            val customLocationComponentOptions = LocationComponentOptions.builder(context!!)
                .trackingGesturesManagement(true)
                .accuracyColor(ContextCompat.getColor(context!!, R.color.blue))
                .build()

            val locationComponentActivationOptions = LocationComponentActivationOptions.builder(context!!, loadedMapStyle)
                .locationComponentOptions(customLocationComponentOptions)
                .build()

// Get an instance of the LocationComponent and then adjust its settings
            mapboxMap.locationComponent.apply {

// Activate the LocationComponent with options
                activateLocationComponent(locationComponentActivationOptions)

// Enable to make the LocationComponent visible
                isLocationComponentEnabled = true

// Set the LocationComponent's camera mode
                cameraMode = CameraMode.TRACKING

// Set the LocationComponent's render mode
                renderMode = RenderMode.COMPASS
            }
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager!!.requestLocationPermissions(this.activity)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissionsManager!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        Toast.makeText(context, "R.string.user_location_permission_explanation", Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocationComponent(mapboxMap.style!!)
        } else {
            Toast.makeText(context, "R.string.user_location_permission_not_granted", Toast.LENGTH_LONG).show()
            activity!!.finish()
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapboxMap.removeOnMapClickListener(this)
        markerAnimator?.cancel()
        mapView.onDestroy()
    }
}