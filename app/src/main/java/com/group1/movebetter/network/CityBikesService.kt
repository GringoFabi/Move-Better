package com.group1.movebetter.network

import com.group1.movebetter.model.CityBikes
import com.group1.movebetter.model.CityBikesNetwork
import com.group1.movebetter.model.CityBikesNetworks
import com.group1.movebetter.model.CityBikesNetworksList
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import java.net.ResponseCache

interface CityBikesService {

    @GET("networks/")
    suspend fun getNetworks(): CityBikes

    @GET("networks/{networkId}")
    suspend fun getNetwork(
        @Path("networkId") networkId: String)
    : CityBikesNetworksList

}