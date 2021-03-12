package com.group1.movebetter.network.bird

import com.group1.movebetter.model.BirdTokens
import com.group1.movebetter.model.Birds
import com.group1.movebetter.model.EmailBody
import com.group1.movebetter.model.Token
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.*

interface BirdAuthService {

    @POST("email")
    suspend fun getAuthToken(
            @Body email: EmailBody
    )

    @POST("magic-link/use")
    suspend fun postAuthTokenAsync(
            @Body token: Token
    ) : BirdTokens

    @POST("refresh/token")
    suspend fun refreshAsync(
            @Header("Authorization") token: String
    ) : BirdTokens

}

