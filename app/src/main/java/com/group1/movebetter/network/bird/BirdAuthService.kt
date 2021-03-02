package com.group1.movebetter.network.bird

import com.group1.movebetter.model.BirdTokens
import com.group1.movebetter.model.Birds
import com.group1.movebetter.model.EmailBody
import com.group1.movebetter.model.Token
import retrofit2.http.*

interface BirdAuthService {

    // TODO: eventually add handling for the response codes
    @POST("email")
    suspend fun getAuthToken(
            @Body email: EmailBody
    )

    @POST("magic-link/use")
    suspend fun postAuthToken(
            @Body token: Token
    ) : BirdTokens

    @POST("refresh/token")
    suspend fun refresh(
            @Header("Authorization") token: String
    ) : BirdTokens

}

