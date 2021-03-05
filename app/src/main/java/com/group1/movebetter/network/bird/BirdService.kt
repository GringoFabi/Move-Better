package com.group1.movebetter.network.bird

import com.group1.movebetter.model.Birds
import com.group1.movebetter.model.NextStations
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface BirdService {

    @GET("bird/nearby")
    suspend fun getNearbyBirds(
            @Query("latitude") latitude: Double,
            @Query("longitude") longitude: Double,
            @Query("radius") radius: Int,
            @Header("Authorization") token: String,
            @Header("Location") location: String
    ): Birds
}