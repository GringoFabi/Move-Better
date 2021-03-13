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
}