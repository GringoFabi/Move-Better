package com.group1.movebetter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group1.movebetter.model.CityBikes
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

    //ehemals controller:

    val BIKE_ICON_ID = "BIKE"
    val PROPERTY_SELECTED = "selected"
    val SCOOTER_ICON_ID = "SCOOTER"

    private val markerStations: HashMap<LatLng, List<LatLng>> = HashMap()
    private val random: Random = Random()

    private fun getMarkers(): HashMap<String, LatLng> {
        //TODO get List from DataModel of Stations and Networks

        val latLng = ArrayList<LatLng>()

        latLng.add(LatLng(-34.603684, -58.381559)) // Buenos Aires
        latLng.add(LatLng(48.85819, 2.29458)) //Eiffel Tower
        latLng.add(LatLng(45.42153, -75.697193)) //Ottawa
        latLng.add(LatLng(35.709026, 139.731992)) //Tokyo
        latLng.add(LatLng(23.05407, -82.345189)) //Havana
        latLng.add(LatLng(52.520007, 13.404954)) //Berlin
        latLng.add(LatLng(37.983917, 23.72936)) //Athen
        latLng.add(LatLng(14.634915, -90.506882)) //Guatemala
        latLng.add(LatLng(51.507351, -0.127758)) //London

        val cities = HashMap<String, LatLng>()

        cities["Buenos Aires"] = latLng[0]
        cities["Eiffel Tower"] = latLng[1]
        cities["Ottawa"] = latLng[2]
        cities["Tokyo"] = latLng[3]
        cities["Havana"] = latLng[4]
        cities["Berlin"] = latLng[5]
        cities["Athen"] = latLng[6]
        cities["Guatemala"] = latLng[7]
        cities["London"] = latLng[8]

        return cities
    }

    fun addNetworks(symbolManager: SymbolManager?) {
        val cities = getMarkers()

        val markers = ArrayList<SymbolOptions>()

        for ((key, value) in cities) {
            val symbol = createSymbolOptions(key, value)
            markers.add(symbol)

            val stations = ArrayList<LatLng>()

            // TODO would be good have an reference of (network) marker to stations you can create the stations markers based on that
            // TODO HashMap or something

            for (i in 0..5) {
                val latLng = createRandomLatLng()
                stations.add(latLng)
            }

            markerStations[value] = stations
        }

        symbolManager!!.create(markers)
    }

    private fun createSymbolOptions(key: String, value: LatLng): SymbolOptions {
        return SymbolOptions().withLatLng(value).withIconImage(BIKE_ICON_ID).withIconSize(0.5f).withTextField(key)
    }

    fun addStations(symbolManager: SymbolManager?, symbol: Symbol) {
        if (markerStations.containsKey(symbol.latLng)) {
            symbolManager!!.delete(symbol);

            val stations = markerStations[symbol.latLng]

            val markers = ArrayList<SymbolOptions>()

            for (i in stations!!) {
                markers.add(createSymbolOptions(symbol.textField, i))
            }

            symbolManager.create(markers)
        }
    }

    private fun createRandomLatLng(): LatLng {
        return LatLng(random.nextDouble() * -180.0 + 90.0,
            random.nextDouble() * -360.0 + 180.0)
    }
}