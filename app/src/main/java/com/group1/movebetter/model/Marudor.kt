package com.group1.movebetter.model

import com.google.gson.Gson
import com.group1.movebetter.database.DatabaseDeparture


data class Departures (
    val departures: List<Departure>,
)

data class Departure (
    val scheduledDestination: String,
    val train: Train,
    val cancelled: Boolean,
    val messages: Messages,
    val arrival: Arrival?,
    val route: List<RouteStation>,
    val currentStation: CurrentStation
)

data class NvvDepartures(
        val departures: List<NvvDeparture>,
)

data class NvvDeparture(
        val train: Train,
        val finalDestination: String,
        val currentStation: CurrentStation,
        val arrival: Arrival

)

data class CurrentStation (
        val title: String,
        val id: String
)

fun List<Departure>.asDatabaseDepartureList(): List<DatabaseDeparture> {
    return map {
        DatabaseDeparture(scheduledDestination = it.scheduledDestination, trainName =  it.train.name, cancelled =  it.cancelled, messages =  Gson().toJson(it.messages), arrivalTime =  it.arrival?.time ?: "N/A", arrivalPlatform =  it.arrival?.platform ?: "N/A", arrivalDelay =  it.arrival?.delay ?: 0, route =  Gson().toJson(it.route), currentStationTitle =  it.currentStation.title, currentStationId =  it.currentStation.id)
    }
}


// Route of the Train. Important to show it on the board. ShowVia is the short list to display and the full list are all stations.
data class RouteStation(
    val name: String,
    val showVia: Boolean
)

data class Arrival(
    val time: String,
    val platform: String,
    val delay: Long
)

// Messages of delays and other information
data class Messages (
    val delays : List<Message>,
    val qos: List<Message>
)

//Text, Time and a Boolean to determine if it is valid still.
data class Message (
    val text: String,
    val timestamp: String,
    val superseded: Boolean
)

data class Train(
    val name: String
)

data class NextStations(
        val nextStation: List<NextStation>
)

data class NextStation(
        val title: String,
        val id: String,
        val location: Location,
)

data class NextNvvStations(
        val nextStation: List<NextNvvStation>
)


data class NextNvvStation(
        val title: String,
        val id: String,
)

data class Location(
        val latitude: Long,
        val longitude: Long
)