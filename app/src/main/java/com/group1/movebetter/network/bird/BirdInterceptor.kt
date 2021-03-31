package com.group1.movebetter.network.bird

import okhttp3.Interceptor
import okhttp3.Response

/**
 * custom interceptor for adding all required headers to each bird api call
 */
class BirdInterceptor(uuid : String): Interceptor {
    private val deviceID = uuid

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
                .newBuilder()
                .addHeader("App-version", "4.119.0")
                .addHeader("Content-Type", "application/json")
                .addHeader("Device-Id", deviceID)
                .addHeader("Platform", "android")
                .addHeader("User-Agent", "Bird/4.119.0(co.bird.Ride; build:3; iOS 14.3.0) Alamofire/5.2.2")
                .build()
        return chain.proceed(request)
    }

}