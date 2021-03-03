package com.group1.movebetter.model

import com.google.gson.Gson
import com.group1.movebetter.database.DatabaseCityBikesNetworks
import com.group1.movebetter.database.DatabaseStaDaStation


data class StaDaStations (
    val result : List<StaDaStation>
)

data class StaDaStation (
    val number: Long,
    val name: String,
    val mailingAddress: MailingAddress?,
    val regionalbereich: RegionalBereich,
    val evaNumbers: List<EvaNumbers>,
    val ril100Identifiers: List<Ril100Identifiers>
)


fun List<StaDaStation>.asDatabaseStaDaStationList(): List<DatabaseStaDaStation> {
    return map {
        DatabaseStaDaStation(it.number, it.name, it.mailingAddress.city, it.mailingAddress.zipcode, it.mailingAddress.street, it.regionalbereich.number, it.regionalbereich.name, Gson().toJson(it.evaNumbers), Gson().toJson(it.ril100Identifiers))
    }
}


// List of Ril100 Identifiers (Short string for the name of the Station) and a Boolean isMain to check which Identifier is the latest.
data class Ril100Identifiers (
    val rilIdentifier: String,
    val isMain: Boolean
)

// List of Numbers and a Boolean isMain to check which number is the latest.
data class EvaNumbers (
    val number: Long,
    val geographicCoordinates: GeoCoordinates?,
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
