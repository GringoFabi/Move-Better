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



@Database(entities = [DatabaseCityBikesNetworks::class], version = 1)
abstract class MyDatabase: RoomDatabase() {
    abstract val cityBikesNetworksDao: CityBikesNetworksDao
}

private lateinit var INSTANCE: MyDatabase

fun getDatabase(context: Context): MyDatabase {
    synchronized(MyDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
                    MyDatabase::class.java,
                    "citybikesnetworks").build()
        }
    }
    return INSTANCE
}
