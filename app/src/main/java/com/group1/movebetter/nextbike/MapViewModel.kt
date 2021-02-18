package com.group1.movebetter.nextbike

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group1.movebetter.model.CityBikes
import com.group1.movebetter.model.CityBikesNetworks
import com.group1.movebetter.model.CityBikesNetworksList
import com.group1.movebetter.repository.Repository
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MapViewModel(private val repository: Repository) : ViewModel() {
    //ehemals MainViewModel:

    private val _getResponseNetworks: MutableLiveData<CityBikes> = MutableLiveData()
    val getResponseNetworks: LiveData<CityBikes>
        get() = _getResponseNetworks

    private val _getResponseNetwork: MutableLiveData<CityBikesNetworksList> = MutableLiveData()
    val getResponseNetwork: LiveData<CityBikesNetworksList>
        get() = _getResponseNetwork

    private val _getResponseNetworkFiltered: MutableLiveData<CityBikesNetworksList> = MutableLiveData()

    fun getNetworks()
    {
        viewModelScope.launch {
            val responseNetworks = repository.getNetworks()
            _getResponseNetworks.value = responseNetworks

            val responseNetwork = repository.getNetwork("velib")
            _getResponseNetwork.value = responseNetwork
            /*val responseNetworkFiltered = repository.getNetwork("")
            getResponseNetworkFiltered.value = responseNetworkFiltered*/
        }
    }

    fun getNetwork()
    {
        viewModelScope.launch {
            val responseNetworks = repository.getNetworks()
        }
    }


    //ehemals MapController:

    val BIKE_ICON_ID = "BIKE"
    val PROPERTY_SELECTED = "selected"
    val SCOOTER_ICON_ID = "SCOOTER"

    private val markerNetwork: HashMap<LatLng, CityBikesNetworks> = HashMap()

    fun addNetworks(networks: CityBikes, symbolManager: SymbolManager?) {
        val markers = ArrayList<SymbolOptions>()

        for (network in networks.networks) {
            val location = network.location

            val symbol = createSymbolOptions("", LatLng(location.latitude, location.longitude))
            markers.add(symbol)

            markerNetwork[LatLng(location.latitude, location.longitude)] = network
        }

        symbolManager!!.create(markers)
    }

    private fun createSymbolOptions(key: String, value: LatLng): SymbolOptions {
        return SymbolOptions().withLatLng(value).withIconImage(BIKE_ICON_ID).withIconSize(0.2f).withTextField(key)
    }

    fun addStations(symbolManager: SymbolManager?, symbol: Symbol) {
        if (markerNetwork.containsKey(symbol.latLng)) {
            symbolManager!!.delete(symbol)

            val network = markerNetwork[symbol.latLng]

            viewModelScope.launch {
                val responseNetwork = repository.getNetwork(network!!.id)

                val markers = ArrayList<SymbolOptions>()

                val stations = responseNetwork.network.stations

                for (station in stations) {
                    markers.add(createSymbolOptions("", LatLng(station.latitude, station.longitude)))
                }

                symbolManager.create(markers)
            }
        }
    }
}