package com.group1.movebetter.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.gson.Gson
import com.group1.movebetter.database.*
import com.group1.movebetter.model.CityBikes
import com.group1.movebetter.model.CityBikesNetworkList
import com.group1.movebetter.model.Departures
import com.group1.movebetter.model.StaDaStations
import com.group1.movebetter.model.*
import com.group1.movebetter.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Repository(private val database: MyDatabase) {

    val getResponseNetworks: LiveData<List<CityBikesNetworks>> = Transformations.map(database.cityBikesNetworksDao.getCityBikesNetworks()){
        it.asCityBikesNetworksList()
    }

    val getResponseNetwork: LiveData<List<CityBikesNetwork>> = Transformations.map(database.cityBikesNetworkDao.getCityBikesNetwork()){
        it.asCityBikesNetworkList()
    }

    val getResponseArrival: LiveData<List<Departure>> = Transformations.map(database.databaseDepartureDao.getDeparture()){
        it.asDepartureList()
    }

    val getResponseStations: LiveData<List<StaDaStation>> = Transformations.map(database.staDaStationDao.getStaDaStation()){
        it.asStaDaStationList()
    }

    val myTokens: LiveData<List<BirdTokens>> = Transformations.map(database.databaseBirdTokensDao.getBirdTokens()){
        it.asBirdTokensList()
    }

    val myBirds: LiveData<List<Bird>> = Transformations.map(database.databaseBirdDao.getBird()){
        it.asBirdList()
    }

    suspend fun getNetworks()
    {
        withContext(Dispatchers.IO){
            val responseNetworks = RetrofitInstance.apiCityBikes.getNetworks().networks
            database.cityBikesNetworksDao.insertAll(responseNetworks.asDatabaseCityBikesNetworksList())
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
            database.cityBikesNetworkDao.insertAll(responseNetwork.network.asDatabaseCityBikesNetworkList())
        }
    }

    suspend fun getStations()
    {
        withContext(Dispatchers.IO){
            val responseStations = RetrofitInstance.apiStadaStations.getStations()
            database.staDaStationDao.insertAll(responseStations.result.asDatabaseStaDaStationList())
        }
    }

    suspend fun getArrival(evaId: Long, lookahead: Long)
    {
        withContext(Dispatchers.IO){
            val getResponseArrival = RetrofitInstance.apiMarudor.getArrival(evaId, lookahead)
            database.databaseDepartureDao.clearTable()
            database.databaseDepartureDao.insertAll(getResponseArrival.departures.asDatabaseDepartureList())
        }
    }
    suspend fun getBirdToken(body: EmailBody) {
        return RetrofitInstance.birdAuthApi.getAuthToken(body)
    }

    suspend fun postMagicToken(body: Token) {
        withContext(Dispatchers.IO){
            val response = RetrofitInstance.birdAuthApi.postAuthToken(body)
            database.databaseBirdTokensDao.insertAll(listOf(response).asDatabaseBirdTokensList())
        }
    }

    suspend fun refresh(token: String) {
        withContext(Dispatchers.IO){
            val response = RetrofitInstance.birdAuthApi.refresh(token)
            database.databaseBirdTokensDao.insertAll(listOf(response).asDatabaseBirdTokensList())
        }
    }

    suspend fun getBirds(lat: Double, lng: Double, rad: Int, token: String, location: String) {
        withContext(Dispatchers.IO){
            val response = RetrofitInstance.birdApi.getNearbyBirds(lat, lng, rad, token, location)
            database.databaseBirdDao.insertAll(response.birds.asDatabaseBirdList())
        }
    }
}