package com.group1.movebetter.network

import com.group1.movebetter.model.*
import com.group1.movebetter.util.Constants.Companion.URL_STADA
import com.group1.movebetter.util.Constants.Companion.STADA_API_KEY
import com.group1.movebetter.util.Constants.Companion.URL_NVV
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.*

interface NvvService {

    @GET(URL_NVV)
    fun getNvvStationsAsync(
    ): Deferred<Response<NvvStations>>

}