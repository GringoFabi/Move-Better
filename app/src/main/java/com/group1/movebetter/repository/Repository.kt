package com.group1.movebetter.repository

import com.group1.movebetter.model.CityBikes
import com.group1.movebetter.model.CityBikesNetworkList
import com.group1.movebetter.model.Departures
import com.group1.movebetter.model.StaDaStations
import com.group1.movebetter.network.RetrofitInstance

class Repository {
    suspend fun getNetworks(): CityBikes
    {
        return RetrofitInstance.apiCityBikes.getNetworks()
    }

    suspend fun getNetworksFiltered(fields: String): CityBikes
    {
        return RetrofitInstance.apiCityBikes.getNetworksFiltered(fields)
    }

    suspend fun getNetwork(networkId: String): CityBikesNetworkList
    {
        return RetrofitInstance.apiCityBikes.getNetwork(networkId)
    }

    suspend fun getStations(): StaDaStations
    {
        return RetrofitInstance.apiStadaStations.getStations()
    }

    suspend fun getArrival(evaId: Long, lookahead: Long): Departures
    {
        return RetrofitInstance.apiMarudor.getArrival(evaId, lookahead)
    }

}