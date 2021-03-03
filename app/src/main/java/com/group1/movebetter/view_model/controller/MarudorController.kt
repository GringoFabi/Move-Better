package com.group1.movebetter.view_model.controller

import com.group1.movebetter.repository.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MarudorController (private val viewModelScope: CoroutineScope, private val repository: Repository) {

    fun getArrival(evaId: Long, lookahead: Long = 0)
    {
        viewModelScope.launch {
            repository.getArrival(evaId,lookahead)
        }
    }

    fun getNextStations(lat: Double, lng: Double, radius: Long = 3000)
    {
        viewModelScope.launch {
            repository.getNextStations(lat, lng, radius)
        }
    }
}