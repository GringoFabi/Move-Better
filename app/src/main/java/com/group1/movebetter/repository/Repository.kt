package com.group1.movebetter.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.group1.movebetter.model.CityBikes
import com.group1.movebetter.model.CityBikesNetworkList
import com.group1.movebetter.model.Departures
import com.group1.movebetter.model.StaDaStations
import com.group1.movebetter.model.*
import com.group1.movebetter.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Repository {

    private val _getResponseNetworks: MutableLiveData<CityBikes> = MutableLiveData()
    val getResponseNetworks: LiveData<CityBikes>
        get() = _getResponseNetworks

    private val _getResponseNetwork: MutableLiveData<CityBikesNetworkList> = MutableLiveData()
    val getResponseNetwork: LiveData<CityBikesNetworkList>
        get() = _getResponseNetwork

    private val _getResponseArrival: MutableLiveData<Departures> = MutableLiveData()
    val getResponseArrival: LiveData<Departures>
        get() = _getResponseArrival

    private val _getResponseStations: MutableLiveData<StaDaStations> = MutableLiveData()
    val getResponseStations: LiveData<StaDaStations>
        get() = _getResponseStations

    private val _myResponse: MutableLiveData<EmailBody> = MutableLiveData()
    val myResponse: LiveData<EmailBody>
        get() = _myResponse

    private val _myTokens: MutableLiveData<BirdTokens> = MutableLiveData()
    val myTokens: LiveData<BirdTokens>
        get() = _myTokens

    private val _myBirds: MutableLiveData<Birds> = MutableLiveData()
    val myBirds: LiveData<Birds>
        get() = _myBirds

    suspend fun getNetworks()
    {
        withContext(Dispatchers.IO){
            val responseNetworks = RetrofitInstance.apiCityBikes.getNetworks()
            _getResponseNetworks.postValue(responseNetworks)
        }
    }

    suspend fun getNetworksFiltered(fields: String): CityBikes
    {
        return RetrofitInstance.apiCityBikes.getNetworksFiltered(fields)
    }

    suspend fun getNetwork(networkId: String)
    {
        withContext(Dispatchers.IO){
            val responseNetwork = RetrofitInstance.apiCityBikes.getNetwork(networkId)
            _getResponseNetwork.postValue(responseNetwork)
        }
    }

    suspend fun getStations()
    {
        withContext(Dispatchers.IO){
            val responseStations = RetrofitInstance.apiStadaStations.getStations()
            _getResponseStations.postValue(responseStations)
        }
    }

    suspend fun getArrival(evaId: Long, lookahead: Long)
    {
        withContext(Dispatchers.IO){
            val getResponseArrival = RetrofitInstance.apiMarudor.getArrival(evaId, lookahead)
            _getResponseArrival.postValue(getResponseArrival)
        }
    }
    suspend fun getBirdToken(body: EmailBody) {
        return RetrofitInstance.birdAuthApi.getAuthToken(body)
    }

    suspend fun postMagicToken(body: Token) {
        withContext(Dispatchers.IO){
            val response = RetrofitInstance.birdAuthApi.postAuthToken(body)
            _myTokens.postValue(response)
        }
    }

    suspend fun refresh(token: String) {
        withContext(Dispatchers.IO){
            val response = RetrofitInstance.birdAuthApi.refresh(token)
            _myTokens.postValue(response)
        }
    }

    suspend fun getBirds(lat: Double, lng: Double, rad: Int, token: String, location: String) {
        withContext(Dispatchers.IO){
            val response = RetrofitInstance.birdApi.getNearbyBirds(lat, lng, rad, token, location)
            _myBirds.postValue(response)
        }
    }
}