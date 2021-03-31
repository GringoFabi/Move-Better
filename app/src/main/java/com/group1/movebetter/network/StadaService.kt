package com.group1.movebetter.network

import com.group1.movebetter.model.*
import com.group1.movebetter.util.Constants.Companion.URL_STADA
import com.group1.movebetter.util.Constants.Companion.STADA_API_KEY
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.*

interface StadaService {

    //Returns Deutsche Bahn stations + additional information
    @Headers("Authorization: $STADA_API_KEY")
    @GET(URL_STADA + "stations/")
    fun getStationsAsync(
    ): Deferred<Response<StaDaStations>>

}