package com.group1.movebetter.nextbike

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

    private val BIKE_STATION_LAYER = "bike-station-layer"
    private val BIKE_STATIONS = "bike-stations"
    private val BIKE_NETWORK_LAYER = "bike-network-layer"
    private val BIKE_NETWORK_SOURCE = "bike-network-source"

    private lateinit var mapView: MapView;
    private var mapboxMap: MapboxMap? = null
    private var permissionsManager: PermissionsManager? = null

    private lateinit var mapViewModel: MapViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.let { Mapbox.getInstance(it, getString(R.string.mapbox_access_token)) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding : FragmentMapBinding = inflate(inflater, R.layout.fragment_map, container, false)
        mapView = binding.mapView

        // Get a reference to the ViewModel associated with this fragment.
        val repository = Repository();
        val viewModelFactory = MapViewModelFactory(repository)
        mapViewModel = ViewModelProvider(this, viewModelFactory).get(MapViewModel::class.java)
        binding.mapViewModel = mapViewModel;

        binding.lifecycleOwner = this

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        mapViewModel.getCurrentLocation(this.requireActivity(), context, this)

        mapViewModel.getNetworks()

        return binding.root
    }

    override fun onMapClick(point: LatLng): Boolean {
        val style = mapboxMap!!.style
        if (style != null) {
            val pixel = mapboxMap!!.projection.toScreenLocation(point)

            val bikeNetworks = mapboxMap!!.queryRenderedFeatures(pixel, BIKE_NETWORK_LAYER)
            val bikeStation = mapboxMap!!.queryRenderedFeatures(pixel, BIKE_STATION_LAYER)

            val networkSource = style.getSourceAs<GeoJsonSource>(BIKE_NETWORK_SOURCE)
            val stationSource = style.getSourceAs<GeoJsonSource>(BIKE_STATIONS)

            if (bikeNetworks.isNotEmpty()) {
                mapViewModel.getNetwork(bikeNetworks[0]!!.getStringProperty("id"))
            }

            if (bikeStation.isEmpty()) {
                return false
            }

            val output = "Free Bikes: ${bikeStation[0].getNumberProperty("freeBikes")}\nEmpty Slots: ${bikeStation[0].getNumberProperty("emptySlots")}\n" +
                    "Timestamp: ${bikeStation[0].getStringProperty("timestamp")}"

            //Get bounds of cameraPosition: mapboxMap!!.getLatLngBoundsZoomFromCamera(mapboxMap!!.cameraPosition)

            Toast.makeText(context, output, Toast.LENGTH_LONG).show()
        }
        return true
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap

        mapboxMap.setStyle(Style.MAPBOX_STREETS, Style.OnStyleLoaded {
            loadImages(it)
            enableLocationComponent(it)
            setupLayers(it)
            mapboxMap.addOnMapClickListener(this@MapFragment)
        })
    }

    private fun setupLayers(style: Style) {
        // Bike Network Layer
        style.addSource(GeoJsonSource(BIKE_NETWORK_SOURCE))

        style.addLayer(SymbolLayer(BIKE_NETWORK_LAYER, BIKE_NETWORK_SOURCE)
                .withProperties(iconImage(mapViewModel.NETWORK_ICON_ID),
                        iconAllowOverlap(false),
                        iconSize(0.3f))
                .withFilter(eq((get("show")), literal(true))))

        // Bike Stations Layer
        style.addSource(GeoJsonSource(BIKE_STATIONS))

        style.addLayer(SymbolLayer(BIKE_STATION_LAYER, BIKE_STATIONS)
                .withProperties(iconImage(mapViewModel.BIKE_ICON_ID),
                        iconAllowOverlap(false),
                        iconSize(0.3f)))

        // Add things to Layer
        mapViewModel.getResponseNetworks.observe(viewLifecycleOwner, Observer {
            val networkSource = style.getSourceAs<GeoJsonSource>(BIKE_NETWORK_SOURCE)
            mapViewModel.getNearestNetwork(it)
            mapViewModel.createFeatureList(networkSource, it)
        })

        mapViewModel.getResponseNetwork.observe(viewLifecycleOwner, Observer {
            val networkSource = style.getSourceAs<GeoJsonSource>(BIKE_NETWORK_SOURCE)
            val stationSource = style.getSourceAs<GeoJsonSource>(BIKE_STATIONS)
            mapViewModel.exchangeNetworkWithStations(networkSource, stationSource, it.network)
        })
    }

    private fun loadImages(style: Style) {
        style.addImage(mapViewModel.BIKE_ICON_ID, BitmapFactory.decodeResource(this.resources, R.raw.bike))
        style.addImage(mapViewModel.NETWORK_ICON_ID, BitmapFactory.decodeResource(this.resources, R.raw.network))
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
            mapboxMap!!.locationComponent.apply {

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
            enableLocationComponent(mapboxMap!!.style!!)
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
        mapView.onDestroy()
    }
}