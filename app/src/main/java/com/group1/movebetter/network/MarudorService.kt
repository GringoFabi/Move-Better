package com.group1.movebetter.network

import com.group1.movebetter.model.*
import com.group1.movebetter.util.Constants.Companion.URL_MARUDOR_IRIS
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MarudorService {

    @GET(URL_MARUDOR_IRIS + "abfahrten/{evaId}")
    suspend fun getArrival(
        @Query("lookahead") lookahead: Long,
        @Path("evaId") evaId: Long
    ): Departures

}