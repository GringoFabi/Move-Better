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

class Repository(val database: MyDatabase, uuid:String) {

    val instance: RetrofitInstance = RetrofitInstance(uuid)

    val getResponseNetworks: LiveData<List<CityBikesNetworks>> = Transformations.map(database.cityBikesNetworksDao.getCityBikesNetworks()){
        it.asCityBikesNetworksList()
    }

    private val _getResponseNetworksFiltered: MutableLiveData<CityBikes> = MutableLiveData()
    val getResponseNetworksFiltered: LiveData<CityBikes>
        get() = _getResponseNetworksFiltered

    val getNvvStations: LiveData<List<NvvStation>> = Transformations.map(database.databaseNvvStationDao.getStations()){
        it.asNvvStationList()
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

    private val _getResponseNextStations: MutableLiveData<NextStations> = MutableLiveData()
    val getResponseNextStations: LiveData<NextStations>
        get() = _getResponseNextStations

    private val _getStationsByTerm: MutableLiveData<NextStations> = MutableLiveData()
    val getStationsByTerm: LiveData<NextStations>
        get() = _getStationsByTerm

    val getNvvStation: LiveData<List<NextNvvStation>> = Transformations.map(database.databaseNextNvvStationDao.getNextStations()){
        it.asNextNvvStationList()
    }

    private val _getResponseNvvArrival: MutableLiveData<NvvDepartures> = MutableLiveData()
    val getResponseNvvArrival: LiveData<NvvDepartures>
        get() = _getResponseNvvArrival

    private val _myResponse: MutableLiveData<EmailBody> = MutableLiveData()
    val myResponse: LiveData<EmailBody>
        get() = _myResponse

    private val _myTokens: MutableLiveData<BirdTokens> = MutableLiveData()

    val myBirds: LiveData<List<Bird>> = Transformations.map(database.databaseBirdDao.getBird()){
        it.asBirdList()
    }

    suspend fun getNetworks()
    {
        launch(instance.apiCityBikes.getNetworksAsync(), {}, {
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
        launch(instance.apiCityBikes.getNetworksFilteredAsync(fields), {}, { _getResponseNetworksFiltered.postValue(it) }, { Log.d("getNetworksFiltered", it.toString()) })
    }

    suspend fun getNetwork(networkId: String)
    {
        launch(instance.apiCityBikes.getNetworkAsync(networkId), {}, {
            if (it != null) {
                database.cityBikesNetworkDao.insertAll(it.network.asDatabaseCityBikesNetworkList())
            }
        }, { Log.d("getNetworks", it.toString()) })
    }

    suspend fun getStations()
    {
        launch(instance.apiStadaStations.getStationsAsync(), {}, {
            if (it != null) {
                database.staDaStationDao.insertAll(it.result.asDatabaseStaDaStationList())
            }
        }, { Log.d("getStations", it.toString()) })
    }

    suspend fun getNvvStations()
    {
        launch(instance.apiNvv.getNvvStationsAsync(), {}, {
            if (it != null) {
                database.databaseNvvStationDao.insertAll(it.stops.asDatabaseNvvStationList())
            }
        }, { Log.d("getNvvStations", it.toString()) })
    }

    suspend fun getArrival(evaId: Long, lookahead: Long)
    {
        launch(instance.apiMarudor.getArrivalAsync(evaId, lookahead), {}, {
            if (it != null) {
                database.databaseDepartureDao.clearTable()
                database.databaseDepartureDao.insertAll(it.departures.asDatabaseDepartureList())
            }
        }, { Log.d("getArrival", it.toString()) })
    }

    suspend fun getArrivalNvvAsync(evaId: String)
    {
        launch(instance.apiMarudor.getArrivalNvvAsync(evaId), {}, {
            _getResponseNvvArrival.postValue(it)
        }, { Log.d("getArrivalNvvAsync", it.toString()) })
    }

    suspend fun getNextStations(lat: Double, lng: Double, radius: Long)
    {
        launch(instance.apiMarudor.getNextStationsAsync(lat, lng, radius),
            {},
            {
                _getResponseNextStations.postValue(it) },
            { Log.d("getNetworksFiltered", it.toString()) })
    }

    suspend fun getStationsByTerm(searchTerm: String)
    {
        launch(instance.apiMarudor.getStationsByTermAsync(searchTerm),
            {},
            { _getStationsByTerm.postValue(it) },
            { Log.d("getNetworksFiltered", it.toString()) })
    }


    suspend fun getNvvStationIdAsync(searchTerm: String, type: String, max: Long)
    {
        launch(instance.apiMarudor.getNvvStationIdAsync(searchTerm, type, max),
            {},
            {
                if (it != null) {
                    database.databaseNextNvvStationDao.insertAll(it.nextStation.filter { nextNvvStation -> nextNvvStation.id != null }
                        .asDatabaseNextNvvStationList())
                }
            },
            { Log.d("getNvvStationIdAsync", it.toString()) })
    }

    suspend fun getBirdToken(body: EmailBody) {
        return instance.birdAuthApi.getAuthToken(body)
    }

    suspend fun postMagicToken(body: Token) {
        withContext(Dispatchers.IO) {
            val response = instance.birdAuthApi.postAuthTokenAsync(body)
            database.databaseBirdTokensDao.insertAll(listOf(response).asDatabaseBirdTokensList())
        }
    }

    suspend fun refresh(token: String) {
        withContext(Dispatchers.IO) {
            val response = instance.birdAuthApi.refreshAsync(token)
            database.databaseBirdTokensDao.insertAll(listOf(response).asDatabaseBirdTokensList())
        }
    }

    suspend fun getBirds(lat: Double, lng: Double, rad: Int, token: String, location: String) {
        withContext(Dispatchers.IO){
            val response = instance.birdApi.getNearbyBirds(lat, lng, rad, token, location)
            database.databaseBirdDao.clearTable()
            database.databaseBirdDao.insertAll(response.birds.asDatabaseBirdList())
        }
    }
}