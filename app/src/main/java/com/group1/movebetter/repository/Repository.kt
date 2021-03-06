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

    //Get LiveData's from Database and Transform them to App-Model

    /**
     * all CityBike Networks
     */
    val getResponseNetworks: LiveData<List<CityBikesNetworks>> = Transformations.map(database.cityBikesNetworksDao.getCityBikesNetworks()){
        it.asCityBikesNetworksList()
    }

    private val _getResponseNetworksFiltered: MutableLiveData<CityBikes> = MutableLiveData()
    val getResponseNetworksFiltered: LiveData<CityBikes>
        get() = _getResponseNetworksFiltered

    /**
     * NVV Stations
     */
    val getNvvStations: LiveData<List<NvvStation>> = Transformations.map(database.databaseNvvStationDao.getStations()){
        it.asNvvStationList()
    }

    /**
     * one CityBike Network (with Stations)
     */
    val getResponseNetwork: LiveData<List<CityBikesNetwork>> = Transformations.map(database.cityBikesNetworkDao.getCityBikesNetwork()){
        it.asCityBikesNetworkList()
    }

    /**
     * Departure Data for DB
     */
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

    val getResponseNvvArrival: LiveData<List<NvvDeparture>> = Transformations.map(database.databaseNvvDepartureDao.getDeparture()){
        it.asNvvDepartureList()
    }

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

    /**
     * Function to intercept errors in case of an unexpected response.
     */
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

    /**
     * method to get CityBike networks with set values. Should've implemented to save data.
     */
    suspend fun getNetworksFiltered(fields: String)
    {
        launch(instance.apiCityBikes.getNetworksFilteredAsync(fields), {}, { _getResponseNetworksFiltered.postValue(it) }, { Log.d("getNetworksFiltered", it.toString()) })
    }

    /**
     * method to get CityBike network with all Stations
     */
    suspend fun getNetwork(networkId: String)
    {
        launch(instance.apiCityBikes.getNetworkAsync(networkId), {}, {
            if (it != null) {
                database.cityBikesNetworkDao.insertAll(it.network.asDatabaseCityBikesNetworkList())
            }
        }, { Log.d("getNetworks", it.toString()) })
    }

    /**
     * method to get db stations
     */
    suspend fun getStations()
    {
        launch(instance.apiStadaStations.getStationsAsync(), {}, {
            if (it != null) {
                database.staDaStationDao.insertAll(it.result.asDatabaseStaDaStationList())
            }
        }, { Log.d("getStations", it.toString()) })
    }

    /**
     * method to get nvv stations
     */
    suspend fun getNvvStations()
    {
        launch(instance.apiNvv.getNvvStationsAsync(), {}, {
            if (it != null) {
                database.databaseNvvStationDao.insertAll(it.stops.asDatabaseNvvStationList())
            }
        }, { Log.d("getNvvStations", it.toString()) })
    }

    /**
     * method to get arrival of a db station
     */

    suspend fun getArrival(evaId: Long, lookahead: Long)
    {
        launch(instance.apiMarudor.getArrivalAsync(evaId, lookahead), {}, {
            if (it != null) {
                database.databaseDepartureDao.clearTable()
                database.databaseDepartureDao.insertAll(it.departures.asDatabaseDepartureList())
            }
        }, { Log.d("getArrival", it.toString()) })
    }

    /**
     * method to get arrival of a Nvv station
     */

    suspend fun getArrivalNvvAsync(evaId: String)
    {
        launch(instance.apiMarudor.getArrivalNvvAsync(evaId), {}, {
            if (it != null){
                database.databaseNvvDepartureDao.clearTable()
                database.databaseNvvDepartureDao.insertAll(it.departures.asDatabaseNvvDepartureList())
            }
        }, { Log.d("getArrivalNvvAsync", it.toString()) })
    }

    /**
     * method to get closest db station
     */
    suspend fun getNextStations(lat: Double, lng: Double, radius: Long)
    {
        launch(instance.apiMarudor.getNextStationsAsync(lat, lng, radius),
            {},
            {
                _getResponseNextStations.postValue(it) },
            { Log.d("getNetworksFiltered", it.toString()) })
    }

    /**
     * method to find a station via name
     */
    suspend fun getStationsByTerm(searchTerm: String)
    {
        launch(instance.apiMarudor.getStationsByTermAsync(searchTerm),
            {},
            { _getStationsByTerm.postValue(it) },
            { Log.d("getNetworksFiltered", it.toString()) })
    }

    /**
     * method for requesting data of a NvvStation
     */
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

    // bird methods

    /**
     * method for requesting the bird authentication mail
     */
    suspend fun getBirdToken(body: EmailBody) {
        return instance.birdAuthApi.getAuthToken(body)
    }

    /**
     * method for requesting the validation of the magic token
     */
    suspend fun postMagicToken(body: Token) {
        withContext(Dispatchers.IO) {
            val response = instance.birdAuthApi.postAuthTokenAsync(body)
            database.databaseBirdTokensDao.insertAll(listOf(response).asDatabaseBirdTokensList())
        }
    }

    /**
     * method for requesting a refresh of the user's access and fresh token
     */
    suspend fun refresh(token: String) {
        withContext(Dispatchers.IO) {
            val response = instance.birdAuthApi.refreshAsync(token)
            database.databaseBirdTokensDao.insertAll(listOf(response).asDatabaseBirdTokensList())
        }
    }

    /**
     * method for requesting bird scooter data
     */
    suspend fun getBirds(lat: Double, lng: Double, rad: Int, token: String, location: String) {
        withContext(Dispatchers.IO){
            val response = instance.birdApi.getNearbyBirds(lat, lng, rad, token, location)
            database.databaseBirdDao.clearTable()
            database.databaseBirdDao.insertAll(response.birds.asDatabaseBirdList())
        }
    }
}