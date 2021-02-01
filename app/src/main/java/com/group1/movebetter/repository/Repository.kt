package com.group1.movebetter.repository

import com.group1.movebetter.model.CityBikes
import com.group1.movebetter.model.CityBikesNetwork
import com.group1.movebetter.model.CityBikesNetworks
import com.group1.movebetter.network.RetrofitInstance

class Repository {
    suspend fun getNetworks(): CityBikes
    {
        return RetrofitInstance.api.getNetworks()
    }

    suspend fun getNetwork(network_Id: String): CityBikesNetwork
    {
        return RetrofitInstance.api.getNetwork(network_Id)
    }
}