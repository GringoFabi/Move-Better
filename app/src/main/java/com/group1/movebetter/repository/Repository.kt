package com.group1.movebetter.repository

import com.group1.movebetter.model.CityBikes
import com.group1.movebetter.model.CityBikesNetwork
import com.group1.movebetter.model.CityBikesNetworks
import com.group1.movebetter.model.CityBikesNetworksList
import com.group1.movebetter.network.RetrofitInstance

class Repository {
    suspend fun getNetworks(): CityBikes
    {
        return RetrofitInstance.api.getNetworks()
    }

    suspend fun getNetwork(networkId: String): CityBikesNetworksList
    {
        return RetrofitInstance.api.getNetwork(networkId)
    }

    suspend fun getNetworkFiltered(networkId: String): CityBikesNetworksList
    {
        return RetrofitInstance.api.getNetwork(networkId)
    }
}