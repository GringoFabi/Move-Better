package com.group1.movebetter.network


import com.group1.movebetter.network.adapters.CompanyAdapter
import com.group1.movebetter.util.Constants.Companion.BASE_URL
import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().add(CompanyAdapter()).build()))
                .build()
    }


    val api: CityBikesService by lazy {
        retrofit.create(CityBikesService::class.java)
    }
}