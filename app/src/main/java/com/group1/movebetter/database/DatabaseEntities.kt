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
import com.group1.movebetter.model.CityBikes
import com.group1.movebetter.model.CityBikesLocation
import com.group1.movebetter.model.CityBikesNetworks
import com.group1.movebetter.model.Company
import com.google.gson.Gson


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
        val locationCountry: String)


fun List<DatabaseCityBikesNetworks>.asCityBikesNetworksList(): List<CityBikesNetworks> {
        return map {
                CityBikesNetworks(Gson().fromJson(it.company, Company::class.java), it.href, CityBikesLocation(it.locationLatitude, it.locationCity, it.locationLongitude, it.locationCountry), it.name, it.id)
        }
}
