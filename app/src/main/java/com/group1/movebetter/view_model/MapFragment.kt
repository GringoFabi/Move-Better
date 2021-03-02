package com.group1.movebetter.view_model

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil.inflate
import androidx.fragment.app.Fragment
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
import java.util.*


class MapFragment : Fragment(), OnMapReadyCallback, PermissionsListener, MapboxMap.OnMapClickListener {

    private lateinit var BIKE_STATION_LAYER: String
    private lateinit var BIKE_STATIONS: String
    private lateinit var BIKE_NETWORK_LAYER: String
    private lateinit var BIKE_NETWORKS: String
    private lateinit var SELECTED_MARKER_LAYER: String
    private lateinit var SELECTED_MARKER: String
    private lateinit var BIRD_SCOOTER_LAYER: String
    private lateinit var BIRD_SCOOTER: String
    private lateinit var TRAM_STATION: String
    private lateinit var TRAM_STATION_LAYER: String

    private lateinit var BIKE_ICON_ID: String
    private lateinit var NETWORK_ICON_ID: String
    private lateinit var SCOOTER_ICON_ID: String
    private lateinit var TRAM_STATION_ICON_ID: String

    private var markerAnimator: ValueAnimator? = null
    private var markerSelected = false

    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap
    private var permissionsManager: PermissionsManager? = null

    private lateinit var mapViewModel: MapViewModel

    private lateinit var binding : FragmentMapBinding

    private lateinit var repository : Repository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.let { Mapbox.getInstance(it, getString(R.string.mapbox_access_token)) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = inflate(inflater, R.layout.fragment_map, container, false)
        mapView = binding.mapView

        // Get a reference to the ViewModel associated with this fragment.
        repository = Repository()
        val viewModelFactory = MapViewModelFactory(repository)
        mapViewModel = ViewModelProvider(this, viewModelFactory).get(MapViewModel::class.java)
        binding.mapViewModel = mapViewModel

        binding.lifecycleOwner = this

        initIds()

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        mapViewModel.cityBikeController.getCurrentLocation(this.requireActivity(), context, this)

        mapViewModel.cityBikeController.getNetworks()

/*
mapViewModel.birdController.getAuthToken("<your email>")

mapViewModel.birdController.postAuthToken("<token form the verify mail>")
mapViewModel.birdController.myTokens.observe(this, Observer { response ->
Log.d("Tokens", response.access.access)
Log.d("Tokens", response.refresh.refresh)
})
mapViewModel.birdController.refresh()

mapViewModel.birdController.myTokens.observe(this, Observer { tokens ->
Log.d("Tokens", tokens.access)
Log.d("Tokens", tokens.refresh)
})
*/

        mapViewModel.birdController.getBirds(mapViewModel.cityBikeController.currentLocation)

        mapViewModel.stadaStationController.getStations()

        return binding.root
    }

    private fun initIds() {
        BIKE_STATION_LAYER = resources.getString(R.string.BIKE_STATION_LAYER)
        BIKE_STATIONS = resources.getString(R.string.BIKE_STATIONS)
        BIKE_NETWORK_LAYER = resources.getString(R.string.BIKE_NETWORK_LAYER)
        BIKE_NETWORKS = resources.getString(R.string.BIKE_NETWORKS)
        SELECTED_MARKER = resources.getString(R.string.SELECTED_MARKER)
        SELECTED_MARKER_LAYER = resources.getString(R.string.SELECTED_MARKER_LAYER)
        BIRD_SCOOTER_LAYER = resources.getString(R.string.BIRD_SCOOTER_LAYER)
        BIRD_SCOOTER = resources.getString(R.string.BIRD_SCOOTER)
        TRAM_STATION_LAYER = resources.getString(R.string.TRAM_STATION_LAYER)
        TRAM_STATION = resources.getString(R.string.TRAM_STATION)

        BIKE_ICON_ID = resources.getString(R.string.BIKE_ICON_ID)
        NETWORK_ICON_ID = resources.getString(R.string.NETWORK_ICON_ID)
        SCOOTER_ICON_ID = resources.getString(R.string.SCOOTER_ICON_ID)
        TRAM_STATION_ICON_ID = resources.getString(R.string.TRAM_STATION_ICON_ID)
    }

    override fun onMapClick(point: LatLng): Boolean {
        val style = mapboxMap.style
        if (style != null) {
            val pixel = mapboxMap.projection.toScreenLocation(point)

            val bikeNetworks = mapboxMap.queryRenderedFeatures(pixel, BIKE_NETWORK_LAYER)
            val bikeStation = mapboxMap.queryRenderedFeatures(pixel, BIKE_STATION_LAYER)
            val scooter = mapboxMap.queryRenderedFeatures(pixel, BIRD_SCOOTER_LAYER)
            val tramStation = mapboxMap.queryRenderedFeatures(pixel, TRAM_STATION_LAYER)
            val selectedFeature = mapboxMap.queryRenderedFeatures(pixel, SELECTED_MARKER_LAYER)

            val selectedMarkerLayer = style.getLayer(SELECTED_MARKER_LAYER) as SymbolLayer

            // when clicked on a bikeNetwork get the stations via REST
            if (bikeNetworks.isNotEmpty()) {
                resetSelectedMarkerLayer(style)
                mapViewModel.cityBikeController.getNetwork(bikeNetworks[0]!!.getStringProperty("id"))
            }

            // when clicked on icon which was already clicked on, show card view
            if (selectedFeature.size > 0 && markerSelected) {
                return false
            }

            // when clicked on map and a marker is selected, deselect it
            if (bikeStation.isEmpty() && scooter.isEmpty() && tramStation.isEmpty()) {
                // when card view is shown and user clicks on map, make it invisible
                checkIfCardViewVisible()
                if (markerSelected) {
                    deselectMarker(selectedMarkerLayer, style, true)
                }
                return false
            }

            val source = style.getSourceAs<GeoJsonSource>(SELECTED_MARKER)

            // differentiate what layer was clicked and adapt the icon image
            when {
                bikeStation.isNotEmpty() -> {
                    source?.setGeoJson(FeatureCollection.fromFeature(bikeStation[0]))
                    selectedMarkerLayer.setProperties(iconImage(BIKE_ICON_ID))
                }
                scooter.isNotEmpty() -> {
                    source?.setGeoJson(FeatureCollection.fromFeature(scooter[0]))
                    selectedMarkerLayer.setProperties(iconImage(SCOOTER_ICON_ID))
                }
                tramStation.isNotEmpty() -> {
                    source?.setGeoJson(FeatureCollection.fromFeature(tramStation[0]))
                    selectedMarkerLayer.setProperties(iconImage(TRAM_STATION_ICON_ID))
                }
            }


            // check if an icon is already selected
            if (markerSelected) {
                deselectMarker(selectedMarkerLayer, style, false)
            }
            // if clicked on a bike station/ scooter/ tram station,
            // make it bigger and show information
            when {
                bikeStation.size > 0 -> {
                    selectMarker(selectedMarkerLayer)
                    adaptCardView(bikeStation[0])
                }
                tramStation.size > 0 -> {
                    selectMarker(selectedMarkerLayer)
                    adaptCardView(tramStation[0])
                }
                scooter.size > 0 -> {
                    selectMarker(selectedMarkerLayer)
                    adaptCardView(scooter[0])
                }
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
        markerAnimator!!.setObjectValues(0.3f, 0.6f)
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
        markerAnimator!!.setObjectValues(0.6f, 0.3f)
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
        val provider = feature.getStringProperty("provider")
        val property0 = binding.property0
        val property1 = binding.property1
        val property2 = binding.property2
        val property3 = binding.property3
        when (provider) {
            "bikes" -> {
                property0.text = feature.getStringProperty("name")
                property1.text = "Free Bikes = ${feature.getNumberProperty("freeBikes")}"
                property2.text = "Empty Slots = ${feature.getNumberProperty("emptySlots")}"
                property3.text = "Stand vom ${feature.getStringProperty("timestamp")}"
            }
            "birds" -> {
                property0.text = feature.getStringProperty("vehicleClass")
                property1.text = "Estimated Range = ${feature.getNumberProperty("estimatedRange")}"
                property2.text = "Battery Level = ${feature.getNumberProperty("batteryLevel")}%"
                property3.text = ""
            }
            else -> {
                property0.text = feature.getStringProperty("name")
                property1.text = "Address = ${feature.getNumberProperty("address")}"
                property2.text = ""
                property3.text = ""
            }
        }

        val cardView = binding.singleLocationCardView
        cardView.visibility = View.VISIBLE
    }

    private fun checkIfCardViewVisible() {
        val cardView = binding.singleLocationCardView

        cardView.visibility = View.GONE
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

        style.addLayer(createLayer(BIKE_NETWORK_LAYER, BIKE_NETWORKS, NETWORK_ICON_ID))

        // Bike Stations Layer
        style.addSource(GeoJsonSource(BIKE_STATIONS))

        style.addLayer(createLayer(BIKE_STATION_LAYER, BIKE_STATIONS, BIKE_ICON_ID))

        // Bird Scooter Layer
        style.addSource(GeoJsonSource(BIRD_SCOOTER))

        style.addLayer(createLayer(BIRD_SCOOTER_LAYER, BIRD_SCOOTER, SCOOTER_ICON_ID))

        // Tram Station Layer
        style.addSource(GeoJsonSource(TRAM_STATION))

        style.addLayer(createLayer(TRAM_STATION_LAYER, TRAM_STATION, TRAM_STATION_ICON_ID))

        // Selected Icon Layer
        style.addSource(GeoJsonSource(SELECTED_MARKER))

        style.addLayer(SymbolLayer(SELECTED_MARKER_LAYER, SELECTED_MARKER)
                .withProperties(iconImage(BIKE_ICON_ID),
                iconSize(0.3f)))

        // Add data to layers
        observers(style)
    }

    private fun createLayer(id: String, src: String, icon: String): SymbolLayer {
        val layer = SymbolLayer(id, src).withProperties(
                iconImage(icon),
                iconAllowOverlap(false),
                iconSize(0.3f))

        if (id == BIKE_NETWORK_LAYER) {
            layer.withFilter(eq((get("show")), literal(true)))
        }

        return layer
    }

    private fun observers(style: Style) {
        // Observer for bike networks
        repository.getResponseNetworks.observe(viewLifecycleOwner) {
            val networkSource = style.getSourceAs<GeoJsonSource>(BIKE_NETWORKS)
            mapViewModel.cityBikeController.getNearestNetwork(it)
            val networks = mapViewModel.cityBikeController.createBikeNetworkList(it)
            mapViewModel.mapController.refreshSource(networkSource!!, networks)
        }

        // Observer for bike stations
        repository.getResponseNetwork.observe(viewLifecycleOwner) {
            val networkSource = style.getSourceAs<GeoJsonSource>(BIKE_NETWORKS)
            val stationSource = style.getSourceAs<GeoJsonSource>(BIKE_STATIONS)
            val networks = mapViewModel.cityBikeController.updateCurrentNetwork(it.network)
            val stations = mapViewModel.cityBikeController.exchangeNetworkWithStations(it.network)
            mapViewModel.mapController.refreshSource(networkSource!!, networks)
            mapViewModel.mapController.refreshSource(stationSource!!, stations)
        }

        // Observer for birds (scooters)
        repository.myBirds.observe(viewLifecycleOwner) {
            val birdSource = style.getSourceAs<GeoJsonSource>(BIRD_SCOOTER)
            val birds = mapViewModel.birdController.createBirdList(it)
            mapViewModel.mapController.refreshSource(birdSource!!, birds)
        }

        // Observer for tram stations
        repository.getResponseStations.observe(viewLifecycleOwner) {
            val stationSource = style.getSourceAs<GeoJsonSource>(TRAM_STATION)
            val stations = mapViewModel.stadaStationController.createStationList(it)
            mapViewModel.mapController.refreshSource(stationSource!!, stations)
        }
    }

    private fun loadImages(style: Style) {
        style.addImage(BIKE_ICON_ID, BitmapFactory.decodeResource(resources, R.raw.bike))
        style.addImage(NETWORK_ICON_ID, BitmapFactory.decodeResource(resources, R.raw.network))
        style.addImage(SCOOTER_ICON_ID, BitmapFactory.decodeResource(resources, R.raw.scooter))
        style.addImage(TRAM_STATION_ICON_ID, BitmapFactory.decodeResource(resources, R.raw.tram))
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

    //Open other Apps or their link to play store

    @SuppressLint("QueryPermissionsNeeded")
    private fun onMapsNavigateTo(lat: Double, lng: Double){
        val gmmIntentUri: Uri = Uri.parse("google.navigation:q=$lat,$lng&mode=w")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        if (mapIntent.resolveActivity(context!!.packageManager) != null) {
            startActivity(mapIntent)
        } else {
            openPlayStoreFor("com.google.android.apps.maps")
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun openBird(){
        val intentL = context!!.packageManager.getLaunchIntentForPackage("co.bird.android")

        if (intentL?.resolveActivity(context!!.packageManager) != null) {
            startActivity(intentL)
        } else {
            openPlayStoreFor("co.bird.android")
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun openNvv(){
        val intentL = context!!.packageManager.getLaunchIntentForPackage("de.hafas.android.nvv")

        if (intentL?.resolveActivity(context!!.packageManager) != null) {
            startActivity(intentL)
        } else {
            openPlayStoreFor("de.hafas.android.nvv")
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun openNextBike(){
        val intentL = context!!.packageManager.getLaunchIntentForPackage("de.nextbike")

        if (intentL?.resolveActivity(context!!.packageManager) != null) {
            startActivity(intentL)
        } else {
            openPlayStoreFor("de.nextbike")
        }
    }

    private fun openPlayStoreFor(packageName: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }
}