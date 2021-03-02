package com.group1.movebetter.view_model.controller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.group1.movebetter.model.EvaNumbers
import com.group1.movebetter.model.MailingAddress
import com.group1.movebetter.model.StaDaStations
import com.group1.movebetter.repository.Repository
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class StadaStationController (private val viewModelScope: CoroutineScope, private val repository: Repository) {

    private val _getResponseStations: MutableLiveData<StaDaStations> = MutableLiveData()
    val getResponseStations: LiveData<StaDaStations>
        get() = _getResponseStations

    fun getStations()
    {
        viewModelScope.launch {
            val responseStations = repository.getStations()
            _getResponseStations.value = responseStations
        }
    }

    fun createStationList(stations: StaDaStations?): ArrayList<Feature> {
        val stationFeatures = ArrayList<Feature>()

        for (station in stations!!.result) {
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
        feature.addStringProperty("provider", "trams")

        return feature
    }
}