package com.group1.movebetter.network

import com.group1.movebetter.model.CityBikes
import com.group1.movebetter.model.CityBikesNetwork
import com.group1.movebetter.model.CityBikesNetworks
import retrofit2.http.GET
import retrofit2.http.Path

interface CityBikesService {

    @GET("networks/")
    suspend fun getNetworks(): CityBikes

    @GET("networks/{network_Id}")

    suspend fun getNetwork(
        @Path("network_Id"
        ) network_Id: String): CityBikesNetwork

}