package com.group1.movebetter.view_model.controller

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.group1.movebetter.model.CityBikes
import com.group1.movebetter.model.CityBikesNetworkList
import com.group1.movebetter.model.StaDaStations
import com.group1.movebetter.network.RetrofitInstance
import com.group1.movebetter.repository.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class StadaStationController (private val viewModelScope: CoroutineScope, private val repository: Repository) {


    fun getStations()
    {
        viewModelScope.launch {
            repository.getStations()
        }
    }
}