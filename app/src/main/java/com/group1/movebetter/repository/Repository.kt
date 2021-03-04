package com.group1.movebetter.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.group1.movebetter.database.*
import com.group1.movebetter.model.CityBikes
import com.group1.movebetter.model.*
import com.group1.movebetter.network.RetrofitInstance
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class Repository(private val database: MyDatabase) {

    val getResponseNetworks: LiveData<List<CityBikesNetworks>> = Transformations.map(database.cityBikesNetworksDao.getCityBikesNetworks()){
        it.asCityBikesNetworksList()
    }

    private val _getResponseNetworksFiltered: MutableLiveData<CityBikes> = MutableLiveData()
    val getResponseNetworksFiltered: LiveData<CityBikes>
        get() = _getResponseNetworksFiltered

    val getResponseNetwork: LiveData<List<CityBikesNetwork>> = Transformations.map(database.cityBikesNetworkDao.getCityBikesNetwork()){
        it.asCityBikesNetworkList()
    }

    val getResponseArrival: LiveData<List<Departure>> = Transformations.map(database.databaseDepartureDao.getDeparture()){
        it.asDepartureList()
    }

    val getResponseStations: LiveData<List<StaDaStation>> = Transformations.map(database.staDaStationDao.getStaDaStation()){
        it.asStaDaStationList()
    }

    private val _getResponseNextStations: MutableLiveData<NextStations> = MutableLiveData()
    val getResponseNextStations: LiveData<NextStations>
        get() = _getResponseNextStations

    private val _getStationsByTerm: MutableLiveData<NextStations> = MutableLiveData()
    val getStationsByTerm: LiveData<NextStations>
        get() = _getStationsByTerm


    private val _myResponse: MutableLiveData<EmailBody> = MutableLiveData()
    val myResponse: LiveData<EmailBody>
        get() = _myResponse

    private val _myTokens: MutableLiveData<BirdTokens> = MutableLiveData()
    val myTokens: LiveData<BirdTokens>
        get() = _myTokens

    val myBirds: LiveData<List<Bird>> = Transformations.map(database.databaseBirdDao.getBird()){
        it.asBirdList()
    }

    suspend fun getNetworks()
    {
        launch(RetrofitInstance.apiCityBikes.getNetworksAsync(), {}, {
            if (it != null) {
                database.cityBikesNetworksDao.insertAll(it.networks.asDatabaseCityBikesNetworksList())
            }
        }, { Log.d("getNetworks", it.toString()) })
    }

    private suspend fun <T> launch(
            request: Deferred<Response<T>>,
            onLoading: suspend () -> Unit,
            onSuccess: suspend ((body: T?) -> Unit),
            onError: suspend ((error: Int?) -> Unit)
    ) {
        withContext(Dispatchers.IO) {
            withContext(Dispatchers.IO) {
                onLoading.invoke()
            }
            try {
                val response = request.await()
                if (response.isSuccessful) {
                    withContext(Dispatchers.IO) {
                        onSuccess.invoke(response.body())
                    }
                } else {
                    val body = response.errorBody()
                    if (body != null) {
                        try {
                            val parsedError = response.code()
                            withContext(Dispatchers.IO) {
                                onError.invoke(parsedError)
                            }
                        } catch (e: Exception) {
                        }
                    } else {
                        withContext(Dispatchers.IO) {
                            onError.invoke(null)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.IO) {
                    onError.invoke(null)
                }
            }
        }
    }


    suspend fun getNetworksFiltered(fields: String)
    {
        launch(RetrofitInstance.apiCityBikes.getNetworksFilteredAsync(fields), {}, { _getResponseNetworksFiltered.postValue(it) }, { Log.d("getNetworksFiltered", it.toString()) })
    }

    suspend fun getNetwork(networkId: String)
    {
        launch(RetrofitInstance.apiCityBikes.getNetworkAsync(networkId), {}, {
            if (it != null) {
                database.cityBikesNetworkDao.insertAll(it.network.asDatabaseCityBikesNetworkList())
            }
        }, { Log.d("getNetworks", it.toString()) })
    }

    suspend fun getStations()
    {
        launch(RetrofitInstance.apiStadaStations.getStationsAsync(), {}, {
            if (it != null) {
                database.staDaStationDao.insertAll(it.result.asDatabaseStaDaStationList())
            }
        }, { Log.d("getStations", it.toString()) })
    }

    suspend fun getArrival(evaId: Long, lookahead: Long)
    {
        launch(RetrofitInstance.apiMarudor.getArrivalAsync(evaId, lookahead), {}, {
            if (it != null) {
                database.databaseDepartureDao.clearTable()
                database.databaseDepartureDao.insertAll(it.departures.asDatabaseDepartureList())
            }
        }, { Log.d("getArrival", it.toString()) })
    }

    suspend fun getNextStations(lat: Double, lng: Double, radius: Long)
    {
        launch(RetrofitInstance.apiMarudor.getNextStationsAsync(lat, lng, radius),
            {},
            {
                it
                _getResponseNextStations.postValue(it) },
            { Log.d("getNetworksFiltered", it.toString()) })
    }

    suspend fun getStationsByTerm(searchTerm: String)
    {
        launch(RetrofitInstance.apiMarudor.getStationsByTermAsync(searchTerm),
            {},
            { _getStationsByTerm.postValue(it) },
            { Log.d("getNetworksFiltered", it.toString()) })
    }

    suspend fun getBirdToken(body: EmailBody) {
        return RetrofitInstance.birdAuthApi.getAuthToken(body)
    }

    suspend fun postMagicToken(body: Token) {
        launch(RetrofitInstance.birdAuthApi.postAuthTokenAsync(body),
            {},
            { _myTokens.postValue(it) },
            { Log.d("getNetworksFiltered", it.toString()) })
    }

    suspend fun refresh(token: String) {
        launch(RetrofitInstance.birdAuthApi.refreshAsync(token),
            {},
            { _myTokens.postValue(it) },
            { Log.d("getNetworksFiltered", it.toString()) })
    }

    suspend fun getBirds(lat: Double, lng: Double, rad: Int, token: String, location: String) {
        withContext(Dispatchers.IO){
            val response = RetrofitInstance.birdApi.getNearbyBirds(lat, lng, rad, token, location)
            database.databaseBirdDao.insertAll(response.birds.asDatabaseBirdList())
        }
    }
}