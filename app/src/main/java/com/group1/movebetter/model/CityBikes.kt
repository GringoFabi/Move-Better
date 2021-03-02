package com.group1.movebetter.model


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

data class CityBikesNetworkList (
        val network : CityBikesNetwork
)

data class CityBikesNetwork (
        val company: Company,
        val href: String,
        val location: CityBikesLocation,
        val id: String,
        val name: String,
        val stations: List<CityBikesStation>
)


data class CityBikesStation (
        val name: String,
        val timestamp: String,
        val longitude: Double,
        val free_bikes: FreeBikes,
        val latitude: Double,
        val empty_slots: EmptySlots,
        val id: String,
        val extra: CityBikesStationExtra
)

data class FreeBikes(
        val amount: Long,
)

data class EmptySlots(
        val amount: Long,
)

data class StationName (
        val name: List<String>
)

data class CityBikesStationExtra (
        val banking: Boolean,
        val bike_uids: List<String>,
        val bikes_overflow: Long,
        val dueDate: Long,
        val ebikes: StationEbikes,
        val ebikes_overflow: Long,
        val has_ebikes: Boolean,
        val normal_bikes: Long,
        val online: Boolean,
        val slots: Long,
        val status: StationExtraStatus,
        val uid: String
)

data class StationEbikes (
        val amount: Long,
)

data class StationExtraStatus (
        val status: List<String>,
)