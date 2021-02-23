package com.group1.movebetter.nextbike

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group1.movebetter.model.CityBikes
import com.group1.movebetter.model.CityBikesNetworks
import com.group1.movebetter.model.CityBikesNetworkList
import com.group1.movebetter.repository.Repository
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.group1.movebetter.model.CityBikesLocation
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import java.util.function.Predicate
import kotlin.math.*

class MapViewModel(private val repository: Repository) : ViewModel() {
    //ehemals MainViewModel:

    private val _getResponseNetworks: MutableLiveData<CityBikes> = MutableLiveData()
    val getResponseNetworks: LiveData<CityBikes>
        get() = _getResponseNetworks

    private val _getResponseNetwork: MutableLiveData<CityBikesNetworkList> = MutableLiveData()
    val getResponseNetwork: LiveData<CityBikesNetworkList>
        get() = _getResponseNetwork

    private val _getResponseNetworkFiltered: MutableLiveData<CityBikesNetworkList> = MutableLiveData()

    fun getNetworks()
    {
        viewModelScope.launch {
            val responseNetworks = repository.getNetworks()
            _getResponseNetworks.value = responseNetworks

            val responseNetwork = repository.getNetwork("velib")
            _getResponseNetwork.value = responseNetwork
            /*val responseNetworkFiltered = repository.getNetwork("")
            getResponseNetworkFiltered.value = responseNetworkFiltered*/
        }
    }

    fun getNetwork()
    {
        viewModelScope.launch {
            val responseNetworks = repository.getNetworks()
        }
    }


    //ehemals MapController:

    val BIKE_ICON_ID = "BIKE"
    val NETWORK_ICON_ID = "NETWORK"
    val SCOOTER_ICON_ID = "SCOOTER"

    val networkCoordinates: ArrayList<Feature> = ArrayList()
    val stationCoordinates: ArrayList<Feature> = ArrayList()

    fun createFeatureList(cityBikes: CityBikes) : Feature? {
        var closestNetworkFeature: Feature? = null

        for (network in cityBikes.networks) {
            val location = network.location
            val feature = Feature.fromGeometry(Point.fromLngLat(location.longitude, location.latitude))
            feature.addStringProperty("id", network.id)

            if (network.id == closestNetwork.id) {
                closestNetworkFeature = feature
            }

            networkCoordinates.add(feature)
        }

        return closestNetworkFeature
    }

    @SuppressLint("NewApi")
    fun exchangeNetworkWithStations(networkSource: GeoJsonSource, stationSource: GeoJsonSource, network: Feature) {
        viewModelScope.launch {
            val responseNetwork = repository.getNetwork(network.getStringProperty("id"))

            val condition = Predicate<Feature> { x -> x.getStringProperty("id").equals(network.getStringProperty("id")) }

            networkCoordinates.removeIf(condition)

            val stations = responseNetwork.network.stations

            for (station in stations) {
                val featureStation = Feature.fromGeometry(Point.fromLngLat(station.longitude, station.latitude))
                featureStation.addNumberProperty("freeBikes", station.free_bikes)
                featureStation.addNumberProperty("emptySlots", station.empty_slots)
                featureStation.addStringProperty("timestamp", station.timestamp)
                stationCoordinates.add(featureStation)
            }

            refreshSources(networkSource, stationSource)
        }
    }

    fun refreshSources(networkSource: GeoJsonSource, stationSource: GeoJsonSource) {
        networkSource.setGeoJson(FeatureCollection.fromFeatures(networkCoordinates))
        stationSource.setGeoJson(FeatureCollection.fromFeatures(stationCoordinates))
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