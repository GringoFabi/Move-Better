package com.group1.movebetter.network.bird

import com.group1.movebetter.model.BirdTokens
import com.group1.movebetter.model.Birds
import com.group1.movebetter.model.EmailBody
import com.group1.movebetter.model.Token
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.*

interface BirdAuthService {

    // this service handles all requests which are sent to https://api-auth.prod.birdapp.com/api/v1/auth/

    // call for requesting an authentication mail
    @POST("email")
    suspend fun getAuthToken(
            @Body email: EmailBody
    )

    // call for validating the retrieved magic token
    @POST("magic-link/use")
    suspend fun postAuthTokenAsync(
            @Body token: Token
    ) : BirdTokens

    // call for refreshing the user's access and refresh token
    @POST("refresh/token")
    suspend fun refreshAsync(
            @Header("Authorization") token: String
    ) : BirdTokens

}

