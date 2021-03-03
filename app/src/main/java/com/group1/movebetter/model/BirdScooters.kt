package com.group1.movebetter.model

import com.google.gson.reflect.TypeToken
import com.group1.movebetter.database.DatabaseBird
import java.lang.reflect.Type

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