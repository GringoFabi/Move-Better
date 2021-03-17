package com.group1.movebetter.network

import com.group1.movebetter.model.*
import com.group1.movebetter.util.Constants.Companion.URL_MARUDOR_HAFAS
import com.group1.movebetter.util.Constants.Companion.URL_MARUDOR_HAFAS2
import com.group1.movebetter.util.Constants.Companion.URL_MARUDOR_IRIS
import com.group1.movebetter.util.Constants.Companion.URL_MARUDOR_STATION
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MarudorService {

    @GET(URL_MARUDOR_IRIS + "abfahrten/{evaId}")
    fun getArrivalAsync(
        @Path("evaId") evaId: Long,
        @Query("lookahead") lookahead: Long,
    ): Deferred<Response<Departures>>

    @GET(URL_MARUDOR_HAFAS2 + "arrivalStationBoard")
    fun getArrivalNvvAsync(
            @Query("station") station: String,
    ): Deferred<Response<NvvDepartures>>

    @GET(URL_MARUDOR_HAFAS + "geoStation")
    fun getNextStationsAsync(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("maxDist") maxDist: Long,
    ): Deferred<Response<NextStations>>

    @GET(URL_MARUDOR_STATION + "search/{searchTerm}")
    fun getStationsByTermAsync(
            @Path("searchTerm") searchTerm: String,
    ): Deferred<Response<NextStations>>

    @GET(URL_MARUDOR_STATION + "search/{searchTerm}")
    fun getNvvStationIdAsync(
            @Path("searchTerm") searchTerm: String,
            @Query("type") type: String,
            @Query("max") max: Long,
    ): Deferred<Response<NextNvvStations>>
}