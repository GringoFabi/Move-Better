package com.group1.movebetter.network.bird

import com.group1.movebetter.model.Birds
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * this service handles all requests which are sent to https://api-bird.prod.birdapp.com/
 */
interface BirdService {

    /**
     * call for receiving all bird scooters which are in reach of a given radius to the user
     */
    @GET("bird/nearby")
    suspend fun getNearbyBirds(
            @Query("latitude") latitude: Double,
            @Query("longitude") longitude: Double,
            @Query("radius") radius: Int,
            @Header("Authorization") token: String,
            @Header("Location") location: String
    ): Birds
}