package com.group1.movebetter.network

import com.group1.movebetter.model.*
import com.group1.movebetter.util.Constants.Companion.URL_MARUDOR_IRIS
import com.group1.movebetter.util.Constants.Companion.URL_MARUDOR_STATION
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MarudorService {

    @GET(URL_MARUDOR_IRIS + "abfahrten/{evaId}")
    suspend fun getArrival(
        @Path("evaId") evaId: Long,
        @Query("lookahead") lookahead: Long,
    ): Departures

    @GET(URL_MARUDOR_STATION + "geoSearch")
    suspend fun getNextStations(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius") radius: Long,
    ): NextStations

    @GET(URL_MARUDOR_STATION + "search/{searchTerm}")
    suspend fun getStationsByTerm(
            @Path("searchTerm") searchTerm: String,
    ): NextStations
}