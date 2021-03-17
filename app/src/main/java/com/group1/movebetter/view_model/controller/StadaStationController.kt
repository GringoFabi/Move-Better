package com.group1.movebetter.view_model.controller

import com.group1.movebetter.model.EvaNumbers
import com.group1.movebetter.model.MailingAddress
import com.group1.movebetter.model.StaDaStation
import com.group1.movebetter.repository.Repository
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class StadaStationController(
    private val viewModelScope: CoroutineScope,
    private val repository: Repository,
    private val mapController: MapController
) {

    lateinit var nearestStation: StaDaStation
    private var evaIdLatLngMap: HashMap<Long, LatLng> = HashMap()
    var selectedStation: LatLng? = null

    fun getStations()
    {
        viewModelScope.launch {
            repository.getStations()
        }
    }

    fun setSelectedStation(evaId: Long) {
        selectedStation = evaIdLatLngMap[evaId]
    }

    fun createStationList(stations: List<StaDaStation>): ArrayList<Feature> {
        val stationFeatures = ArrayList<Feature>()

        for (station in stations) {
            val evaNumbers = station.evaNumbers
            for (evaNumber in evaNumbers) {
                if (!evaNumber.isMain || evaNumber.geographicCoordinates == null) {
                    continue
                }
                val feature = createStationFeature(station.name, evaNumber, station.mailingAddress)

                stationFeatures.add(feature)
            }
        }

        return stationFeatures
    }

    private fun createStationFeature(name: String, evaNumbers: EvaNumbers, mailingAddress: MailingAddress?): Feature {
        val coordinates = evaNumbers.geographicCoordinates!!.coordinates

        //LatLng in coordinates
        val feature = Feature.fromGeometry(Point.fromLngLat(coordinates[0], coordinates[1]))

        feature.addNumberProperty("evaId", evaNumbers.number)
        feature.addStringProperty("name", name)

        feature.addStringProperty("address", "${mailingAddress?.street ?: "N/A"} ${mailingAddress?.zipcode ?: "N/A"} ${mailingAddress?.city ?: "N/A"}")
        feature.addStringProperty("provider", "db")
        feature.addNumberProperty("latitude", coordinates[1])
        feature.addNumberProperty("longitude", coordinates[0])

        evaIdLatLngMap[evaNumbers.number] = LatLng(coordinates[1], coordinates[0])

        return feature
    }

    fun getNearestStation(stations: List<StaDaStation>) {
        val distances = ArrayList<Double>()
        val distanceNetworkMap = HashMap<Double, StaDaStation>()

        for (station in stations) {
            val evaNumbers = station.evaNumbers
            for (evaNumber in evaNumbers) {
                if (evaNumber.isMain) {
                    val coordinates = evaNumber.geographicCoordinates?.coordinates
                    val lng = coordinates?.get(0)
                    val lat = coordinates?.get(1)
                    if (lat != null && lng != null) {
                        val location = mapController.getLocation(lat, lng)
                        val d = mapController.haversineFormular(location)
                        distances.add(d)
                        distanceNetworkMap[d] = station
                    }
                }
            }
        }
        val minDistance: Double? = distances.minByOrNull { it }
        nearestStation = distanceNetworkMap[minDistance]!!
    }

}