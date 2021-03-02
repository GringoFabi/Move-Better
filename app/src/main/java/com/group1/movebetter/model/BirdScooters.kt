package com.group1.movebetter.model

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
        val nest_id: String,
        val partner_id: String,
        val battery_level: Int,
        val estimated_range: Int,
        val area_key: String,
)

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