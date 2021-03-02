package com.group1.movebetter.network

import com.group1.movebetter.model.*
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CityBikesService {

    @GET("networks/")
    suspend fun getNetworks(): CityBikes

    @GET("networks/{networkId}")
    suspend fun getNetwork(
        @Path("networkId") networkId: String)
    : CityBikesNetworkList

    @GET("networks")
    suspend fun getNetworksFiltered(
        @Query("fields") fields: String,
    ): CityBikes
}