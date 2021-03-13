package com.group1.movebetter.model

import com.google.gson.Gson
import com.group1.movebetter.database.DatabaseCityBikesNetworks
import com.group1.movebetter.database.DatabaseStaDaStation


data class NvvStations (
    val stops : List<NvvStation>
)

data class NvvStation (
    val number: String,
    val name: String,
    val lat: String,
    val lng: String
)
