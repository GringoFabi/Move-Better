package com.group1.movebetter.model



data class StaDaStations (
    val result : List<StaDaStation>
)

data class StaDaStation (
    val number: Long,
    val name: String,
    val mailingAddress: MailingAddress,
    val regionalBereich: RegionalBereich,
    val evaNumbers: List<EvaNumbers>,
    val ril100Identifiers: List<Ril100Identifiers>
)

data class Ril100Identifiers (
    val rilIdentifier: String
)

data class EvaNumbers (
    val number: Long,
    val geoCoordinates: GeoCoordinates,
)

data class RegionalBereich (
    val number: Long,
    val name: String
)

data class MailingAddress (
    val city: String,
    val zipcode: String,
    val street: String
)

data class GeoCoordinates (
    val latitude: Double,
    val longitude: Double,
)
