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
 * Database entities go in this file. These are responsible for reading and writing from the
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

fun List<DatabaseStaDaStation>.asStaDaStationList(): List<StaDaStation> {
        return map {
                val evaListType: Type = object : TypeToken<ArrayList<EvaNumbers>>() {}.type
                val indListType: Type = object : TypeToken<ArrayList<Ril100Identifiers>>() {}.type
                StaDaStation(
                        it.number, it.name, MailingAddress(it.addressCity, it.addressZipcode, it.addressStreet), RegionalBereich(it.regionalBereichNumber,it.regionalBereichName), Gson().fromJson(it.evaNumbers, evaListType), Gson().fromJson(it.ril100Identifiers, indListType)
                )
        }
}