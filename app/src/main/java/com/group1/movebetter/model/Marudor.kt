package com.group1.movebetter.model


data class Departures (
    val departures: List<Departure>,
)

data class Departure (
    val scheduledDestination: String,
    val train: Train,
    val cancelled: Boolean,
    val messages: Messages,
    val arrival: Arrival,
    val route: List<RouteStation>,
)

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