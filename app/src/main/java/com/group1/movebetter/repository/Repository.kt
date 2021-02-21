package com.group1.movebetter.repository

import com.group1.movebetter.model.CityBikes
import com.group1.movebetter.model.CityBikesNetworkList
import com.group1.movebetter.network.RetrofitInstance

class Repository {
    suspend fun getNetworks(): CityBikes
    {
        return RetrofitInstance.api.getNetworks()
    }

    suspend fun getNetworksFiltered(fields: String): CityBikes
    {
        return RetrofitInstance.api.getNetworksFiltered(fields)
    }

    suspend fun getNetwork(networkId: String): CityBikesNetworkList
    {
        return RetrofitInstance.api.getNetwork(networkId)
    }


}