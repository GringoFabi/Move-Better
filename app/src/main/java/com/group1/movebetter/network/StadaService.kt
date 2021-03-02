package com.group1.movebetter.network

import com.group1.movebetter.model.*
import com.group1.movebetter.util.Constants.Companion.URL_STADA
import com.group1.movebetter.util.Constants.Companion.STADA_API_KEY
import retrofit2.http.*

interface StadaService {

    @Headers("Authorization: $STADA_API_KEY")
    @GET(URL_STADA + "stations/")
    suspend fun getStations(
    ): StaDaStations

}