package com.group1.movebetter.network


import com.group1.movebetter.BuildConfig
import com.group1.movebetter.network.adapters.*
import com.group1.movebetter.util.Constants.Companion.URL_CITYBIKES
import com.group1.movebetter.network.adapters.CityBikesStationExtraStatusAdapter
import com.group1.movebetter.network.adapters.CompanyAdapter
import com.group1.movebetter.network.adapters.StationEbikesAdapter
import com.group1.movebetter.network.bird.BirdAuthService
import com.group1.movebetter.network.bird.BirdInterceptor
import com.group1.movebetter.network.bird.BirdService
import com.group1.movebetter.util.Constants.Companion.BIRD_AUTH_URL
import com.group1.movebetter.util.Constants.Companion.BIRD_URL
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
                .baseUrl(URL_CITYBIKES)
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .addConverterFactory(
                        MoshiConverterFactory.create(
                        Moshi.Builder().
                        add(CompanyAdapter()).
                        add(CityBikesStationExtraStatusAdapter()).
                        add(StationEbikesAdapter()).
                        add(EmptySlotsAdapter()).
                        add(FreeBikesAdapter()).
                        add(NextStationsAdapter()).
                        build())
                ).build()
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

    // modified interceptor class for adding the standard headers to all bird-requests
    private val client = OkHttpClient.Builder().apply {
        addInterceptor(BirdInterceptor())
    }.build()

    // auth api
    private val birdAuthRetrofit by lazy {
        Retrofit.Builder()
                .baseUrl(BIRD_AUTH_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    val birdAuthApi: BirdAuthService by lazy {
        birdAuthRetrofit.create(BirdAuthService::class.java)
    }

    // normal api
    private val birdRetrofit by lazy {
        Retrofit.Builder()
                .baseUrl(BIRD_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

    }

    val birdApi: BirdService by lazy {
        birdRetrofit.create(BirdService::class.java)
    }
}