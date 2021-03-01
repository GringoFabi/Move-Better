package com.group1.movebetter.view_model.controller

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.group1.movebetter.model.*
import com.group1.movebetter.repository.Repository
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import java.util.function.Predicate
import kotlin.math.*
import com.group1.movebetter.model.CityBikes
import com.group1.movebetter.model.CityBikesLocation
import com.group1.movebetter.model.CityBikesNetworks
import com.group1.movebetter.view_model.MapFragment
import kotlinx.coroutines.CoroutineScope


class MapController(private val viewModelScope: CoroutineScope, private val repository: Repository) {

    private val networkCoordinates: ArrayList<Feature> = ArrayList()
    private var currentNetwork: CityBikesNetwork? = null

    fun createFeatureList(networkSource: GeoJsonSource?, cityBikes: CityBikes, cityBikeController: CityBikeController) {
        for (network in cityBikes.networks) {
            val feature = createFeature(network.id, network.location, true)

            if (network.id == closestNetwork.id) {
                feature.addBooleanProperty("show", false)
                cityBikeController.getNetwork(network.id)
            }

            networkCoordinates.add(feature)
        }

        refreshSource(networkSource!!, networkCoordinates)
    }

    private fun createFeature(id: String, location: CityBikesLocation, show: Boolean): Feature {
        val feature = Feature.fromGeometry(Point.fromLngLat(location.longitude, location.latitude))
        feature.addStringProperty("id", id)
        feature.addBooleanProperty("show", show)

        return feature
    }

    @SuppressLint("NewApi")
    fun exchangeNetworkWithStations(networkSource: GeoJsonSource?, stationSource: GeoJsonSource?, network: CityBikesNetwork) {
        // Update currentNetwork
        if (currentNetwork == null) {
            currentNetwork = network
        }

        val condition = Predicate<Feature> { x -> x.getStringProperty("id").equals(network.id) || x.getStringProperty("id").equals(currentNetwork!!.id) }
        networkCoordinates.removeIf(condition)

        // Update feature list of bike networks
        networkCoordinates.add(createFeature(network.id, network.location, false))

        if (currentNetwork!!.id != network.id) {
            networkCoordinates.add(createFeature(currentNetwork!!.id, currentNetwork!!.location, true))
            currentNetwork = network
        }

        refreshSource(networkSource!!, networkCoordinates)

        // Add stations to map
        val featureList = ArrayList<Feature>()

        for (station in network.stations) {
            val featureStation = Feature.fromGeometry(Point.fromLngLat(station.longitude, station.latitude))
            featureStation.addStringProperty("name", station.name)
            featureStation.addStringProperty("id", station.id)
            featureStation.addNumberProperty("freeBikes", station.free_bikes)
            featureStation.addNumberProperty("emptySlots", station.empty_slots)
            featureStation.addStringProperty("timestamp", station.timestamp)

            featureList.add(featureStation)
        }

        refreshSource(stationSource!!, featureList)
    }

    private fun refreshSource(source: GeoJsonSource, featureList: ArrayList<Feature>) {
        source.setGeoJson(FeatureCollection.fromFeatures(featureList))
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