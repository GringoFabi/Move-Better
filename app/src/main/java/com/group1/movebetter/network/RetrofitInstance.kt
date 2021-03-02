package com.group1.movebetter.network


import com.group1.movebetter.network.adapters.*
import com.group1.movebetter.util.Constants.Companion.URL_CITYBIKES
import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
                .baseUrl(URL_CITYBIKES)
                .addConverterFactory(
                        MoshiConverterFactory.create(
                        Moshi.Builder().
                        add(CompanyAdapter()).
                        add(CityBikesStationExtraStatusAdapter()).
                        add(StationEbikesAdapter()).
                        add(EmptySlotsAdapter()).
                        add(FreeBikesAdapter()).
                        build())
                ).
                build()
    }


    val apiCityBikes: CityBikesService by lazy {
        retrofit.create(CityBikesService::class.java)
    }

    val apiStadaStations: StadaService by lazy {
        retrofit.create(StadaService::class.java)
    }

    val apiMarudor: MarudorService by lazy {
        retrofit.create(MarudorService::class.java)
    }
}