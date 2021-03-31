package com.group1.movebetter.view_model.controller

import com.group1.movebetter.model.*
import com.group1.movebetter.repository.Repository
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.function.Predicate
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CityBikeController(private val viewModelScope: CoroutineScope, private val repository: Repository, val mapController: MapController) {

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
    var nearestBike: CityBikesStation? = null

    /**
     * prepare marker list for inserting in layer as source
     */
    fun createBikeNetworkList(cityBikes: List<CityBikesNetworks>): ArrayList<Feature> {
        for (network in cityBikes) {
            val feature = createBikeFeature(network.id, network.location!!, true)

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
        feature.addNumberProperty("latitude", location.latitude)
        feature.addNumberProperty("longitude", location.longitude)

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

        nearestBike = getNearestBike(network.stations)

        return networkFeatures
    }

    fun exchangeNetworkWithStations(network: CityBikesNetwork): ArrayList<Feature> {
        val featureList = ArrayList<Feature>()

        for (station in network.stations) {
            val featureStation = Feature.fromGeometry(Point.fromLngLat(station.longitude, station.latitude))
            featureStation.addStringProperty("name", station.name)
            featureStation.addStringProperty("id", station.id)
            featureStation.addNumberProperty("latitude", station.latitude)
            featureStation.addNumberProperty("longitude", station.longitude)
            featureStation.addNumberProperty("freeBikes", station.free_bikes.amount)
            featureStation.addNumberProperty("emptySlots", station.empty_slots.amount)
            featureStation.addStringProperty("timestamp", station.timestamp)
            featureStation.addStringProperty("provider", "bikes")

            featureList.add(featureStation)
        }

        return featureList
    }

    lateinit var closestNetwork: CityBikesNetworks

    /**
     * method for calculating the nearest city bike network in relation to the user's current location
     */
    fun getNearestNetwork(res: List<CityBikesNetworks>) {
        val distances = ArrayList<Double>()
        val distanceNetworkMap = HashMap<Double, CityBikesNetworks>()

        for (network in res) {
            val d = mapController.haversineFormular(mapController.getLocation(network.location!!.latitude, network.location.longitude))
            distances.add(d)
            distanceNetworkMap[d] = network
        }
        val minDistance: Double? = distances.minByOrNull { it }
        closestNetwork = distanceNetworkMap[minDistance]!!
    }

    /**
     * method for calculating the nearest city bike in relation to the user's current location
     */
    private fun getNearestBike(stations: List<CityBikesStation>): CityBikesStation? {
        val distances = ArrayList<Double>()
        val distanceNetworkMap = HashMap<Double, CityBikesStation>()
        for (station in stations) {
            val d = mapController.haversineFormular(mapController.getLocation(station.latitude, station.longitude))
            distances.add(d)
            distanceNetworkMap[d] = station
        }
        val minDistance: Double? = distances.minByOrNull { it }
        return distanceNetworkMap[minDistance]
    }
}