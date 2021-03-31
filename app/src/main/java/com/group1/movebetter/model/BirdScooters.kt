package com.group1.movebetter.model

import com.group1.movebetter.database.DatabaseBird
import com.group1.movebetter.database.DatabaseBirdTokens

// data model for the bird api

data class Birds (
        val birds : List<Bird>,
)

data class Bird (
        val id: String,
        val location: BirdLocation,
        val code: String,
        val model: String,
        val vehicle_class: String,
        val captive: Boolean,
        val nest_id: String?,
        val partner_id: String,
        val battery_level: Int,
        val estimated_range: Int,
        val area_key: String,
)


/**
 * Convert App-Model to Database-Model
 */
fun List<Bird>.asDatabaseBirdList(): List<DatabaseBird> {
        return map {
                DatabaseBird(it.id, it.location.latitude, it.location.longitude, it.code, it.model, it.vehicle_class,it.captive,it.nest_id ?: "N/A",it.partner_id,it.battery_level,it.estimated_range,it.area_key)
        }
}

data class BirdLocation (
        val latitude: Double,
        val longitude: Double,
)

data class BirdTokens (
        val access: String,
        val refresh: String,
)

/**
 * Convert App-Model to Database-Model
 * db-key constant set to 1 to store only one Token-pair
 */
fun List<BirdTokens>.asDatabaseBirdTokensList(): List<DatabaseBirdTokens> {
        return map {
                DatabaseBirdTokens("1",it.access,it.refresh)
        }
}

data class Token(val token: String)

data class EmailBody (val email: String)

data class Position(
        val latitude: Double,
        val longitude: Double,
        val altitude: Double,
        val accuracy: Float,
        val speed: Float,
        val heading: Int = -1,
)