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
import com.group1.movebetter.model.CityBikesNetwork
import com.group1.movebetter.model.CityBikesNetworks
import com.group1.movebetter.repository.Repository
import com.group1.movebetter.view_model.MapFragment
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.function.Predicate
import kotlin.math.*

class CityBikeController (private val viewModelScope: CoroutineScope, private val repository: Repository) {

    fun getNetworks()
    {
        viewModelScope.launch {
            repository.getNetworks()
        }
    }

    fun getNetwork(id: String)
    {
        viewModelScope.launch {
            repository.getNetwork(id)
        }
    }

    private val networkFeatures: ArrayList<Feature> = ArrayList()
    private var currentNetwork: CityBikesNetwork? = null

    fun createBikeNetworkList(cityBikes: List<CityBikesNetworks>): ArrayList<Feature> {
        for (network in cityBikes) {
            val feature = createBikeFeature(network.id, network.location, true)

            if (network.id == closestNetwork.id) {
                feature.addBooleanProperty("show", false)
                getNetwork(network.id)
            }

            networkFeatures.add(feature)
        }

        return networkFeatures
    }

    private fun createBikeFeature(id: String, location: CityBikesLocation, show: Boolean): Feature {
        val feature = Feature.fromGeometry(Point.fromLngLat(location.longitude, location.latitude))
        feature.addStringProperty("id", id)
        feature.addBooleanProperty("show", show)

        return feature
    }

    fun updateCurrentNetwork(network: CityBikesNetwork): ArrayList<Feature> {
        // Update currentNetwork
        if (currentNetwork == null) {
            currentNetwork = network
        }

        // removeIf is android version dependent
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            val condition = Predicate<Feature> { x -> x.getStringProperty("id").equals(network.id) || x.getStringProperty("id").equals(currentNetwork!!.id) }
            networkFeatures.removeIf(condition)
        } else {
            for (networkF in networkFeatures) {
                if (networkF.getStringProperty("id").equals(network.id) || networkF.getStringProperty("id").equals(currentNetwork!!.id)) {
                    networkFeatures.remove(networkF)
                }
            }
        }

        // Update feature list of bike networks
        networkFeatures.add(createBikeFeature(network.id, network.location, false))

        if (currentNetwork!!.id != network.id) {
            networkFeatures.add(createBikeFeature(currentNetwork!!.id, currentNetwork!!.location, true))
            currentNetwork = network
        }

        return networkFeatures
    }

    fun exchangeNetworkWithStations(network: CityBikesNetwork): ArrayList<Feature> {
        // Add stations to map
        val featureList = ArrayList<Feature>()

        for (station in network.stations) {
            val featureStation = Feature.fromGeometry(Point.fromLngLat(station.longitude, station.latitude))
            featureStation.addStringProperty("name", station.name)
            featureStation.addStringProperty("id", station.id)
            featureStation.addNumberProperty("freeBikes", station.free_bikes.amount)
            featureStation.addNumberProperty("emptySlots", station.empty_slots.amount)
            featureStation.addStringProperty("timestamp", station.timestamp)
            featureStation.addStringProperty("provider", "bikes")

            featureList.add(featureStation)
        }

        return featureList
    }

    // nearest-network logic
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var permissionsManager: PermissionsManager? = null

    lateinit var closestNetwork: CityBikesNetworks
    var currentLocation: Location = initLocation()

    private fun initLocation() : Location {

        // Setting default location to Berlin

        val location = Location(LocationManager.GPS_PROVIDER)
        location.longitude = 13.404954
        location.latitude = 52.520008
        return location
    }

    fun getNearestNetwork(res: List<CityBikesNetworks>) {

        // Haversine formular (see more here: https://en.wikipedia.org/wiki/Haversine_formula)

        val earthRadius = 6371
        val distances = ArrayList<Double>()
        val distanceNetworkMap = HashMap<Double, CityBikesNetworks>()
        for (network in res) {
            val networkLocation: CityBikesLocation = network.location
            val distanceLatitude = degreeToRadial(abs(currentLocation.latitude) - abs(networkLocation.latitude))
            val distanceLongitude = degreeToRadial(abs(currentLocation.longitude) - abs(networkLocation.longitude))

            val a = sin(distanceLatitude / 2) * sin(distanceLatitude / 2) +
                    cos(degreeToRadial(abs(networkLocation.latitude))) * cos(degreeToRadial(abs(currentLocation.latitude))) *
                    sin(distanceLongitude / 2) * sin(distanceLongitude / 2)

            val c = 2 * atan2(sqrt(a), sqrt(1 - a));
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