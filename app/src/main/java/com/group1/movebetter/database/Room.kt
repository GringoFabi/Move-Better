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

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CityBikesNetworksDao {
    @Query("select * from databasecitybikesnetworks")
    fun getCityBikesNetworks(): LiveData<List<DatabaseCityBikesNetworks>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll( citybikesnetworks: List<DatabaseCityBikesNetworks>)
}

@Dao
interface CityBikesNetworkDao {
    @Query("select * from databasecitybikesnetwork")
    fun getCityBikesNetwork(): LiveData<List<DatabaseCityBikesNetwork>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll( citybikesnetwork: List<DatabaseCityBikesNetwork>)
}

@Dao
interface StaDaStationDao {
    @Query("select * from databasestadastation")
    fun getStaDaStation(): LiveData<List<DatabaseStaDaStation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll( stadastation: List<DatabaseStaDaStation>)
}

@Dao
interface DatabaseDepartureDao {
    @Query("select * from databasedeparture")
    fun getDeparture(): LiveData<List<DatabaseDeparture>>

    @Query("delete from databasedeparture")
    fun clearTable()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll( departures: List<DatabaseDeparture>)
}

@Dao
interface DatabaseBirdDao {
    @Query("select * from databasebird")
    fun getBird(): LiveData<List<DatabaseBird>>

    @Query("delete from databasebird")
    fun clearTable()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll( birds: List<DatabaseBird>)
}

@Dao
interface DatabaseBirdTokensDao {
    @Query("select * from databasebirdtokens where `key` = :id")
    fun getBirdToken(id: String): DatabaseBirdTokens

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll( birdtokens: List<DatabaseBirdTokens>)
}

@Dao
interface DatabaseDevUuidDao {
    @Query("select * from databasedevuuid where `key` = :id")
    fun getDevUuid(id: String): DatabaseDevUuid

    @Query("select * from databasedevuuid")
    fun getAllDevUuid(): LiveData<List<DatabaseDevUuid>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll( uuids: List<DatabaseDevUuid>)
}


@Dao
interface DatabaseNvvStationDao {
    @Query("select * from databasenvvstation")
    fun getStations(): LiveData<List<DatabaseNvvStation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll( stations: List<DatabaseNvvStation>)
}

@Dao
interface DatabaseNextNvvStationDao {
    @Query("select * from databasenextnvvstation")
    fun getNextStations(): LiveData<List<DatabaseNextNvvStation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll( stations: List<DatabaseNextNvvStation>)
}

@Dao
interface DatabaseNvvDepartureDao {
    @Query("select * from databasenvvdeparture")
    fun getDeparture(): LiveData<List<DatabaseNvvDeparture>>

    @Query("delete from databasenvvdeparture")
    fun clearTable()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll( departures: List<DatabaseNvvDeparture>)
}

@Database(entities = [DatabaseCityBikesNetworks::class, DatabaseCityBikesNetwork::class, DatabaseStaDaStation::class, DatabaseDeparture::class, DatabaseBird::class, DatabaseBirdTokens::class, DatabaseDevUuid::class, DatabaseNvvStation::class, DatabaseNextNvvStation::class, DatabaseNvvDeparture::class], version = 11)
abstract class MyDatabase: RoomDatabase() {
    abstract val cityBikesNetworksDao: CityBikesNetworksDao
    abstract val cityBikesNetworkDao: CityBikesNetworkDao
    abstract val staDaStationDao: StaDaStationDao
    abstract val databaseDepartureDao: DatabaseDepartureDao
    abstract val databaseBirdDao: DatabaseBirdDao
    abstract val databaseBirdTokensDao: DatabaseBirdTokensDao
    abstract val databaseDevUuidDao: DatabaseDevUuidDao
    abstract val databaseNvvStationDao: DatabaseNvvStationDao
    abstract val databaseNextNvvStationDao: DatabaseNextNvvStationDao
    abstract val databaseNvvDepartureDao: DatabaseNvvDepartureDao
}

private lateinit var INSTANCE: MyDatabase

fun getDatabase(context: Context): MyDatabase {
    synchronized(MyDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
                    MyDatabase::class.java,
                    "mydb").build()
        }
    }
    return INSTANCE
}
