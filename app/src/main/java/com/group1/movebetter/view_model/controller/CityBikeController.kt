package com.group1.movebetter.view_model.controller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.group1.movebetter.model.CityBikes
import com.group1.movebetter.model.CityBikesNetworkList
import com.group1.movebetter.repository.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class CityBikeController (private val viewModelScope: CoroutineScope, private val repository: Repository) {

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

}