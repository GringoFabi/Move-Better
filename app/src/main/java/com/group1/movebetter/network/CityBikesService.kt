package com.group1.movebetter.network

import com.group1.movebetter.model.*
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CityBikesService {

    @GET("networks/")
    fun getNetworksAsync(): Deferred<Response<CityBikes>>

    @GET("networks/{networkId}")
    fun getNetworkAsync(
        @Path("networkId") networkId: String)
    : Deferred<Response<CityBikesNetworkList>>

    @GET("networks")
    fun getNetworksFilteredAsync(
        @Query("fields") fields: String,
    ): Deferred<Response<CityBikes>>
}