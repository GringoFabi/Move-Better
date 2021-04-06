/*
 * Copyright (C) 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.group1.movebetter.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.group1.movebetter.model.*
import java.lang.reflect.Type


/**
 * Database entities in this file. These are responsible for reading and writing from the
 * database.
 */


@Entity(tableName = "databasecitybikesnetworks")
data class DatabaseCityBikesNetworks constructor(
        @PrimaryKey
        val id: String,
        val company: String,
        val href: String,
        val name: String,
        val locationLatitude: Double,
        val locationCity: String,
        val locationLongitude: Double,
        val locationCountry: String
)

/**
 * Convert Database-Model to App-Model
 */
fun List<DatabaseCityBikesNetworks>.asCityBikesNetworksList(): List<CityBikesNetworks> {
        return map {
                CityBikesNetworks(
                        Gson().fromJson(it.company, Company::class.java),
                        it.href,
                        CityBikesLocation(
                                it.locationLatitude,
                                it.locationCity,
                                it.locationLongitude,
                                it.locationCountry
                        ),
                        it.name,
                        it.id
                )
        }
}



@Entity(tableName = "databasecitybikesnetwork")
data class DatabaseCityBikesNetwork constructor(
        @PrimaryKey
        val key: String,
        val id: String,
        val company: String,
        val href: String,
        val name: String,
        val locationLatitude: Double,
        val locationCity: String,
        val locationLongitude: Double,
        val locationCountry: String,
        val stations: String
)


/**
 * Convert Database-Model to App-Model
 */
fun List<DatabaseCityBikesNetwork>.asCityBikesNetworkList(): List<CityBikesNetwork> {
        return map {
                val listType: Type = object : TypeToken<ArrayList<CityBikesStation>>() {}.type
                CityBikesNetwork(
                        Gson().fromJson(it.company, Company::class.java),
                        it.href,
                        CityBikesLocation(
                                it.locationLatitude,
                                it.locationCity,
                                it.locationLongitude,
                                it.locationCountry
                        ),
                        it.id,
                        it.name,
                        Gson().fromJson(it.stations, listType)
                )
        }
}

@Entity(tableName = "databasestadastation")
data class DatabaseStaDaStation constructor(
        @PrimaryKey
        val number: Long,
        val name: String,
        val addressCity: String,
        val addressZipcode: String,
        val addressStreet: String,
        val regionalBereichNumber: Long,
        val regionalBereichName: String,
        val evaNumbers: String,
        val ril100Identifiers: String
)

/**
 * Convert Database-Model to App-Model
 */
fun List<DatabaseStaDaStation>.asStaDaStationList(): List<StaDaStation> {
        return map {
                val evaListType: Type = object : TypeToken<ArrayList<EvaNumbers>>() {}.type
                val indListType: Type = object : TypeToken<ArrayList<Ril100Identifiers>>() {}.type
                StaDaStation(
                        it.number, it.name, MailingAddress(it.addressCity, it.addressZipcode, it.addressStreet), RegionalBereich(it.regionalBereichNumber,it.regionalBereichName), Gson().fromJson(it.evaNumbers, evaListType), Gson().fromJson(it.ril100Identifiers, indListType)
                )
        }
}

@Entity(tableName = "databasedeparture")
data class DatabaseDeparture (
        @PrimaryKey(autoGenerate = true)
        val id: Long = 0L,
        val scheduledDestination: String,
        val trainName: String,
        val cancelled: Boolean,
        val messages: String,
        val arrivalTime: String,
        val arrivalPlatform: String,
        val arrivalDelay: Long,
        val route: String,
        val currentStationTitle: String,
        val currentStationId: String
)

/**
 * Convert Database-Model to App-Model
 */
fun List<DatabaseDeparture>.asDepartureList(): List<Departure> {
        return map {
                val listType: Type = object : TypeToken<ArrayList<RouteStation>>() {}.type
                Departure(it.scheduledDestination, Train(it.trainName), it.cancelled, Gson().fromJson(it.messages, Messages::class.java), Arrival(it.arrivalTime,it.arrivalPlatform,it.arrivalDelay),Gson().fromJson(it.route, listType), CurrentStation(it.currentStationTitle, it.currentStationId))
        }
}

@Entity(tableName = "databasebird")
data class DatabaseBird (
        @PrimaryKey
        val id: String,
        val locationLatitude: Double = 0.0,
        val locationLongitude: Double = 0.0,
        val code: String,
        val model: String,
        val vehicle_class: String,
        val captive: Boolean,
        val nest_id: String,
        val partner_id: String,
        val battery_level: Int,
        val estimated_range: Int,
        val area_key: String,
)

/**
 * Convert Database-Model to App-Model
 */
fun List<DatabaseBird>.asBirdList(): List<Bird> {
        return map {
                Bird(it.id, BirdLocation(it.locationLatitude,it.locationLongitude), it.code, it.model, it.vehicle_class,it.captive,it.nest_id,it.partner_id,it.battery_level,it.estimated_range,it.area_key)
        }
}

@Entity(tableName = "databasebirdtokens")
data class DatabaseBirdTokens (
        @PrimaryKey
        val key: String,
        val access: String,
        val refresh: String,
)

/**
 * Convert Database-Model to App-Model
 */
fun List<DatabaseBirdTokens>.asBirdTokensList(): List<BirdTokens> {
        return map {
                BirdTokens(it.access,it.refresh)
        }
}

@Entity(tableName = "databasedevuuid")
data class DatabaseDevUuid (
        @PrimaryKey
        val key: String,
        val uuid : String,
)

/**
 * Convert Database-Model to App-Model
 */
fun DatabaseDevUuid.asDevUuid(): DevUuid {
        return DevUuid(this.uuid)
}

@Entity(tableName = "databasenvvstation")
data class DatabaseNvvStation (
        @PrimaryKey
        val number: String,
        val name: String,
        val lat: Double,
        val lng: Double
)

/**
 * Convert Database-Model to App-Model
 */
fun List<DatabaseNvvStation>.asNvvStationList(): List<NvvStation> {
        return map {
                NvvStation(it.number, it.name, it.lat, it.lng)
        }
}


@Entity(tableName = "databasenextnvvstation")
data class DatabaseNextNvvStation(
        @PrimaryKey
        val id: String,
        val title: String,
        val coordinatesLat: Double,
        val coordinatesLng: Double,
)

/**
 * Convert Database-Model to App-Model
 */
fun List<DatabaseNextNvvStation>.asNextNvvStationList(): List<NextNvvStation> {
        return map {
                NextNvvStation(it.title, it.id, NvvLocation(it.coordinatesLat, it.coordinatesLng))
        }
}

@Entity(tableName = "databasenvvdeparture")
data class DatabaseNvvDeparture(
        @PrimaryKey(autoGenerate = true)
        val id: Long = 0L,
        val trainName: String,
        val finalDestination: String,
        val currentStationTitle: String,
        val currentStationId: String,
        val arrivalTime: String,
        val arrivalPlatform: String,
        val arrivalDelay: Long

)

/**
 * Convert Database-Model to App-Model
 */
fun List<DatabaseNvvDeparture>.asNvvDepartureList(): List<NvvDeparture> {
        return map {
                NvvDeparture(Train(it.trainName), it.finalDestination, CurrentStationNvv(it.currentStationTitle,it.currentStationId),Arrival(it.arrivalTime, it.arrivalPlatform, it.arrivalDelay))
        }
}
