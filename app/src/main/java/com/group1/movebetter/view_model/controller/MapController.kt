package com.group1.movebetter.view_model.controller

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.group1.movebetter.model.CityBikes
import com.group1.movebetter.model.CityBikesLocation
import com.group1.movebetter.model.CityBikesNetworks
import com.group1.movebetter.repository.Repository
import com.group1.movebetter.view_model.MapFragment
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.*

class MapController(private val viewModelScope: CoroutineScope, private val repository: Repository) {

    val BIKE_ICON_ID = "BIKE"
    val NETWORK_ICON_ID = "NETWORK"
    val PROPERTY_SELECTED = "selected"
    val SCOOTER_ICON_ID = "SCOOTER"

    private val markerNetwork: HashMap<LatLng, CityBikesNetworks> = HashMap()

    fun addNetworks(networks: CityBikes, symbolManager: SymbolManager?) {
        val markers = ArrayList<SymbolOptions>()

        for (network in networks.networks) {
            if (network.id == closestNetwork.id) {
                getStation(closestNetwork, symbolManager!!, viewModelScope, repository)
                continue
            }
            val location = network.location

            val symbol = createSymbolOptions("", LatLng(location.latitude, location.longitude), NETWORK_ICON_ID)
            markers.add(symbol)

            markerNetwork[LatLng(location.latitude, location.longitude)] = network
        }

        symbolManager!!.create(markers)
    }

    private fun createSymbolOptions(key: String, value: LatLng, iconID: String): SymbolOptions {
        return SymbolOptions().withLatLng(value).withIconImage(iconID).withIconSize(0.3f).withTextField(key)
    }

    fun addStations(symbolManager: SymbolManager?, symbol: Symbol) {
        if (markerNetwork.containsKey(symbol.latLng)) {
            symbolManager!!.delete(symbol)

            val network = markerNetwork[symbol.latLng]

            getStation(network, symbolManager, viewModelScope, repository)
        }
    }

    private fun getStation(network: CityBikesNetworks?, symbolManager: SymbolManager,  viewModelScope: CoroutineScope, repository: Repository) {
        viewModelScope.launch {
            val responseNetwork = repository.getNetwork(network!!.id)

            val markers = ArrayList<SymbolOptions>()

            val stations = responseNetwork.network.stations

            for (station in stations) {
                markers.add(createSymbolOptions("", LatLng(station.latitude, station.longitude), BIKE_ICON_ID))
            }

            symbolManager.create(markers)
        }
    }

    // nearest-network logic
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var permissionsManager: PermissionsManager? = null

    lateinit var closestNetwork: CityBikesNetworks
    private var currentLocation: Location = initLocation()

    private fun initLocation() : Location {

        // Setting default location to Berlin

        val location = Location(LocationManager.GPS_PROVIDER)
        location.longitude = 13.404954
        location.latitude = 52.520008
        return location
    }

    fun getNearestNetwork(res: CityBikes) {

        // Haversine formular (see more here: https://en.wikipedia.org/wiki/Haversine_formula)

        val earthRadius = 6371
        val distances = ArrayList<Double>()
        val distanceNetworkMap = HashMap<Double, CityBikesNetworks>()
        for (network in res.networks) {
            val networkLocation: CityBikesLocation = network.location
            val distanceLatitude = degreeToRadial(abs(currentLocation.latitude) - abs(networkLocation.latitude))
            val distanceLongitude = degreeToRadial(abs(currentLocation.longitude) - abs(networkLocation.longitude))

            val a = sin(distanceLatitude/2) * sin(distanceLatitude/2) +
                    cos(degreeToRadial(abs(networkLocation.latitude))) * cos(degreeToRadial(abs(currentLocation.latitude))) *
                    sin(distanceLongitude/2) * sin(distanceLongitude/2)

            val c = 2 * atan2(sqrt(a), sqrt(1-a));
            val d = earthRadius * c; // Distance in km
            distances.add(d)
            distanceNetworkMap[d] = network
        }
        val minDistance: Double? = distances.minByOrNull { it }
        closestNetwork = distanceNetworkMap[minDistance]!!
    }

    private fun degreeToRadial(degree: Double): Double {
        return degree * (Math.PI/180)
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(activity: FragmentActivity, context: Context?, mapFragment: MapFragment) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

        if (!PermissionsManager.areLocationPermissionsGranted(context)) {
            permissionsManager = PermissionsManager(mapFragment)
            permissionsManager!!.requestLocationPermissions(activity)
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                currentLocation = location
            }
        }
    }
}