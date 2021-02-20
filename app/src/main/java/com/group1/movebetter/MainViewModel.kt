package com.group1.movebetter

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group1.movebetter.model.CityBikes
import com.group1.movebetter.model.CityBikesNetworkList
import com.group1.movebetter.repository.Repository
import kotlinx.coroutines.launch

class MainViewModel(private val repository: Repository) : ViewModel() {

    val getResponseNetworks: MutableLiveData<CityBikes> = MutableLiveData()
    val getResponseNetwork: MutableLiveData<CityBikesNetworkList> = MutableLiveData()
    val getResponseNetworkFiltered: MutableLiveData<CityBikes> = MutableLiveData()

    fun getNetworks()
    {
        viewModelScope.launch {
            val responseNetworks = repository.getNetworks()
            getResponseNetworks.value = responseNetworks
        }
    }

    fun getNetworksFiltered()
    {
        viewModelScope.launch {
            val responseNetworksFiltered = repository.getNetworksFiltered()
            getResponseNetworkFiltered.postValue(responseNetworksFiltered)
        }
    }

    fun getNetwork(networkId: String)
    {
        viewModelScope.launch {
            val responseNetwork = repository.getNetwork(networkId)
            getResponseNetwork.postValue(responseNetwork)
        }
    }
}