package com.group1.movebetter.model



data class StaDaStations (
    val result : List<StaDaStation>
)

data class StaDaStation (
    val number: Long,
    val name: String,
    val mailingAddress: MailingAddress,
    val regionalbereich: RegionalBereich,
    val evaNumbers: List<EvaNumbers>,
    val ril100Identifiers: List<Ril100Identifiers>
)

// List of Ril100 Identifiers (Short string for the name of the Station) and a Boolean isMain to check which Identifier is the latest.
data class Ril100Identifiers (
    val rilIdentifier: String,
    val isMain: Boolean
)

// List of Numbers and a Boolean isMain to check which number is the latest.
data class EvaNumbers (
    val number: Long,
    val geographicCoordinates: GeoCoordinates,
    val isMain: Boolean
)

// Region name, most likely unimportant
data class RegionalBereich (
    val number: Long,
    val name: String
)

data class MailingAddress (
    val city: String,
    val zipcode: String,
    val street: String
)

// List of Doubles which should have 2 doubles only.
data class GeoCoordinates (
    val coordinates: List<Double>,
)
