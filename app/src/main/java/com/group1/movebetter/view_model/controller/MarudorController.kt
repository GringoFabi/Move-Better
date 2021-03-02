package com.group1.movebetter.view_model.controller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.group1.movebetter.model.Departures
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
}