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


    @GET("networks?fields=")
    suspend fun getNetworksFiltered(
            @Query("id") id: String,
            @Query("name") name: String,
            @Query("href") href: String,
    ): CityBikes
}