package com.group1.movebetter.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.group1.movebetter.model.CityBikes
import com.group1.movebetter.model.CityBikesNetworkList
import com.group1.movebetter.model.Departures
import com.group1.movebetter.model.StaDaStations
import com.group1.movebetter.model.*
import com.group1.movebetter.network.RetrofitInstance
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class Repository {

    private val _getResponseNetworks: MutableLiveData<CityBikes> = MutableLiveData()
    val getResponseNetworks: LiveData<CityBikes>
        get() = _getResponseNetworks

    private val _getResponseNetworksFiltered: MutableLiveData<CityBikes> = MutableLiveData()
    val getResponseNetworksFiltered: LiveData<CityBikes>
        get() = _getResponseNetworksFiltered

    private val _getResponseNetwork: MutableLiveData<CityBikesNetworkList> = MutableLiveData()
    val getResponseNetwork: LiveData<CityBikesNetworkList>
        get() = _getResponseNetwork

    private val _getResponseArrival: MutableLiveData<Departures> = MutableLiveData()
    val getResponseArrival: LiveData<Departures>
        get() = _getResponseArrival

    private val _getResponseStations: MutableLiveData<StaDaStations> = MutableLiveData()
    val getResponseStations: LiveData<StaDaStations>
        get() = _getResponseStations

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

    private val _myBirds: MutableLiveData<Birds> = MutableLiveData()
    val myBirds: LiveData<Birds>
        get() = _myBirds

    suspend fun getNetworks()
    {
        //withContext(Dispatchers.IO){
        //    val responseNetworks = RetrofitInstance.apiCityBikes.getNetworks()
        //    _getResponseNetworks.postValue(responseNetworks)
        //}
        launch(RetrofitInstance.apiCityBikes.getNetworksAsync(), {}, { _getResponseNetworks.postValue(it) }, { Log.d("getNetworks", it.toString()) })
    }

    private suspend fun <T> launch(
            request: Deferred<Response<T>>,
            onLoading: suspend () -> Unit,
            onSuccess: suspend ((body: T?) -> Unit),
            onError: suspend ((error: Int?) -> Unit)
    ) {
        withContext(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                onLoading.invoke()
            }
            try {
                val response = request.await()
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onSuccess.invoke(response.body())
                    }
                } else {
                    val body = response.errorBody()
                    if (body != null) {
                        try {
                            val parsedError = response.code()
                            withContext(Dispatchers.Main) {
                                onError.invoke(parsedError)
                            }
                        } catch (e: Exception) {
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            onError.invoke(null)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
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
        launch(RetrofitInstance.apiCityBikes.getNetworkAsync(networkId), {}, { _getResponseNetwork.postValue(it) }, { Log.d("getNetwork", it.toString()) })
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

    suspend fun getNextStations(lat: Double, lng: Double, radius: Long)
    {
        withContext(Dispatchers.IO){
            val getResponseNextStations = RetrofitInstance.apiMarudor.getNextStations(lat, lng, radius)
            _getResponseNextStations.postValue(getResponseNextStations)
        }
    }

    suspend fun getStationsByTerm(searchTerm: String)
    {
        withContext(Dispatchers.IO){
            val getStationsByTerm = RetrofitInstance.apiMarudor.getStationsByTerm(searchTerm)
            _getStationsByTerm.postValue(getStationsByTerm)
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