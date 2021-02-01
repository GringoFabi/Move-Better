package com.group1.movebetter

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group1.movebetter.model.CityBikes
import com.group1.movebetter.model.CityBikesNetwork
import com.group1.movebetter.model.CityBikesNetworks
import com.group1.movebetter.repository.Repository
import kotlinx.coroutines.launch

class MainViewModel(private val repository: Repository) : ViewModel() {

    val getResponseNetworks: MutableLiveData<CityBikes> = MutableLiveData()
    val getResponseNetwork: MutableLiveData<CityBikesNetwork> = MutableLiveData()
    fun getNetworks()
    {
        viewModelScope.launch {
            val responseNetworks = repository.getNetworks()
            getResponseNetworks.value = responseNetworks

            val responseNetwork = repository.getNetwork("velib")
            getResponseNetwork.value = responseNetwork
        }
    }
}