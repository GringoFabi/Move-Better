package com.group1.movebetter.view_model.controller

import android.util.Log
import com.group1.movebetter.model.EvaNumbers
import com.group1.movebetter.model.MailingAddress
import com.group1.movebetter.model.NvvStation
import com.group1.movebetter.model.StaDaStation
import com.group1.movebetter.repository.Repository
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class NvvController(
    private val viewModelScope: CoroutineScope,
    private val repository: Repository,
    private val mapController: MapController
) {

    var nearestStation: NvvStation? = null

    fun getNvvStations()
    {
        viewModelScope.launch {
            repository.getNvvStations()
        }
    }

    fun createStationList(stops: List<NvvStation>): ArrayList<Feature> {
        val stationFeatures = ArrayList<Feature>()

        for (station in stops) {
            val feature = createStationFeature(station.name, station.number, station.lat, station.lng)
            stationFeatures.add(feature)
        }

        nearestStation = getNearestStation(stops)
        return stationFeatures
    }

    private fun createStationFeature(name: String, number: String, lat: Double, lng: Double): Feature {
        //LatLng in coordinates
        val feature = Feature.fromGeometry(Point.fromLngLat(lng, lat))

        feature.addStringProperty("name", name)
        feature.addStringProperty("provider", "trains")
        feature.addNumberProperty("latitude", lat)
        feature.addNumberProperty("longitude", lng)
        return feature
    }

    private fun getNearestStation(stations: List<NvvStation>): NvvStation? {
        val distances = ArrayList<Double>()
        val distanceNetworkMap = HashMap<Double, NvvStation>()
        for (station in stations) {
            val d = mapController.haversineFormular(mapController.getLocation(station.lat, station.lng))
            distances.add(d)
            distanceNetworkMap[d] = station
        }
        val minDistance: Double? = distances.minByOrNull { it }
        return distanceNetworkMap[minDistance]
    }
}