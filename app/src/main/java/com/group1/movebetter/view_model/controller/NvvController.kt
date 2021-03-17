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
}