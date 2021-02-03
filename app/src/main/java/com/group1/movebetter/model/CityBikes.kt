package com.group1.movebetter.model

import com.group1.movebetter.network.adapters.CompanyAdapter
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonClass
import com.squareup.moshi.ToJson


data class CityBikes (
        val networks : List<CityBikesNetworks>
)


data class CityBikesNetworks (
        val company: Company,
        val href: String,
        val location: CityBikesLocation,
        val name: String,
        val id: String
        )


data class CityBikesLocation (
        val latitude: Double,
        val city: String,
        val longitude: Double,
        val country: String,
)


data class Company (
        val company: List<String>
)

data class CityBikesNetworksList (
        val network : CityBikesNetwork
)

data class CityBikesNetwork (
        val company: Company,
        val href: String,
        val location: CityBikesLocation,
        val id: String,
        val name: String,
        val stations: List<CityBikesStations>
)


data class CityBikesStations (
        val name: String,
        val timestamp: String,
        val longitude: Double,
        val free_bikes: Long,
        val latitude: Double,
        val empty_slots: Long,
        val id: String,
        val extra: CityBikesStationsExtra
)

data class CityBikesStationsExtra (
        val banking: Boolean,
        val bikes_overflow: Long,
        val dueDate: Long,
        val ebikes: Long,
        val ebikes_overflow: Long,
        val has_ebikes: Boolean,
        val normal_bikes: Long,
        val online: Boolean,
        val slots: Long,
        val status: String,
        val uid: Long
)