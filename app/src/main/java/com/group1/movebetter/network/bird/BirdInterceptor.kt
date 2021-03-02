package com.group1.movebetter.network.bird

import okhttp3.Interceptor
import okhttp3.Response

class BirdInterceptor: Interceptor {

    // TODO: retrieve the device-id from a database
    private val deviceID = "bb657dc4-8538-48f1-9058-85dfdd89c98d"

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