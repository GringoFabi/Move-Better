package com.group1.movebetter.view_model

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil.inflate
import androidx.lifecycle.ViewModelProvider
import android.annotation.SuppressLint
import androidx.lifecycle.Observer
import com.group1.movebetter.R
import com.group1.movebetter.databinding.FragmentMapBinding
import com.group1.movebetter.repository.Repository
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.style.layers.Property.ICON_ANCHOR_BOTTOM
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*

class MapFragment : Fragment(), OnMapReadyCallback, PermissionsListener {

    private val CALLOUT_LAYER_ID = "CALLOUT_LAYER_ID"
    private val GEOJSON_SOURCE_ID = "GEOJSON_SOURCE_ID"

    private lateinit var mapView: MapView;
    private var mapboxMap: MapboxMap? = null
    private var permissionsManager: PermissionsManager? = null

    private lateinit var symbolManager: SymbolManager

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

        mapViewModel.mapController.getCurrentLocation(this.requireActivity(), context, this)

        mapViewModel.cityBikeController.getNetworks()
        mapViewModel.stadaStationController.getStations()

        mapViewModel.marudorController.getArrival(8000105)
        return binding.root
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap

        mapboxMap.setStyle(Style.MAPBOX_STREETS) {
            configSymbolManager(it)
            loadImages(it)
            //setUpInfoWindowLayer(it)
            enableLocationComponent(it)
        }
    }

    private fun setUpInfoWindowLayer(style: Style) {
        // TODO https://docs.mapbox.com/android/maps/examples/symbol-layer-info-window/
        // TODO this
        style.addLayer(SymbolLayer(CALLOUT_LAYER_ID, GEOJSON_SOURCE_ID)
            .withProperties(
                iconImage(mapViewModel.mapController.BIKE_ICON_ID), /* show image with id title based on the value of the name feature property */
                iconAnchor(ICON_ANCHOR_BOTTOM), /* set anchor of icon to bottom-left */
                iconAllowOverlap(true), /* all info window and marker image to appear at the same time */
                iconOffset(arrayOf(-2f, -28f)) /* offset the info window to be above the marker */
            ))
        //.withFilter(eq(get(mapController!!.PROPERTY_SELECTED), literal(true)))) /* add a filter to show only when selected feature property is true */
    }

    private fun loadImages(style: Style) {
        style.addImage(mapViewModel.mapController.BIKE_ICON_ID, BitmapFactory.decodeResource(this.resources, R.raw.bike))
        style.addImage(mapViewModel.mapController.NETWORK_ICON_ID, BitmapFactory.decodeResource(this.resources, R.raw.network))
    }

    private fun configSymbolManager(style: Style) {
        symbolManager = SymbolManager(mapView, this.mapboxMap!!, style)

        mapViewModel.cityBikeController.getResponseNetworks.observe(viewLifecycleOwner, Observer {
            mapViewModel.mapController.getNearestNetwork(it)
            mapViewModel.mapController.addNetworks(it, symbolManager)
        })

        symbolManager.iconAllowOverlap = false

        symbolManager.addClickListener(OnSymbolClickListener { symbol: Symbol ->
            Toast.makeText(
                context, String.format("Symbol clicked %s", symbol.textField),
                Toast.LENGTH_SHORT
            ).show()

            mapViewModel.mapController.addStations(symbolManager, symbol)

            false
        })
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