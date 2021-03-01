package com.group1.movebetter.view_model.controller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.group1.movebetter.model.CityBikes
import com.group1.movebetter.model.CityBikesNetworkList
import com.group1.movebetter.repository.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class CityBikeController (private val viewModelScope: CoroutineScope, private val repository: Repository) {
    //ehemals MainViewModel:

    private val _getResponseNetworks: MutableLiveData<CityBikes> = MutableLiveData()
    val getResponseNetworks: LiveData<CityBikes>
        get() = _getResponseNetworks

    private val _getResponseNetwork: MutableLiveData<CityBikesNetworkList> = MutableLiveData()
    val getResponseNetwork: LiveData<CityBikesNetworkList>
        get() = _getResponseNetwork

    private val _getResponseNetworkFiltered: MutableLiveData<CityBikesNetworkList> = MutableLiveData()

    fun getNetworks()
    {
        viewModelScope.launch {
            val responseNetworks = repository.getNetworks()
            _getResponseNetworks.value = responseNetworks
        }
    }

    fun getNetwork(id: String)
    {
        viewModelScope.launch {
            val responseNetwork = repository.getNetwork(id)
            _getResponseNetwork.value = responseNetwork
        }
    }

}