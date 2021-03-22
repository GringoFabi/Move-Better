package com.group1.movebetter.model

import com.group1.movebetter.database.DatabaseNvvStation

data class NvvStations (
    val stops: List<NvvStation>
)

data class NvvStation (
    val number: String,
    val name: String,
    val lat: Double,
    val lng: Double
)


fun List<NvvStation>.asDatabaseNvvStationList(): List<DatabaseNvvStation> {
    return map {
        DatabaseNvvStation(it.number, it.name, it.lat, it.lng);
    }
}