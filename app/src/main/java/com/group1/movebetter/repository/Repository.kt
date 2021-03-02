package com.group1.movebetter.repository

import com.group1.movebetter.model.CityBikes
import com.group1.movebetter.model.CityBikesNetworkList
import com.group1.movebetter.model.Departures
import com.group1.movebetter.model.StaDaStations
import com.group1.movebetter.model.*
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
    suspend fun getBirdToken(body: EmailBody) {
        return RetrofitInstance.birdAuthApi.getAuthToken(body)
    }

    suspend fun postMagicToken(body: Token): BirdTokens {
        return RetrofitInstance.birdAuthApi.postAuthToken(body)
    }

    suspend fun refresh(token: String): BirdTokens {
        return RetrofitInstance.birdAuthApi.refresh(token)
    }

    suspend fun getBirds(lat: Double, lng: Double, rad: Int, token: String, location: String): Birds {
        return RetrofitInstance.birdApi.getNearbyBirds(lat, lng, rad, token, location)
    }
}