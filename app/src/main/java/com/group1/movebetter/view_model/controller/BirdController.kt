package com.group1.movebetter.view_model.controller

import android.location.Location
import com.google.gson.Gson
import com.group1.movebetter.model.*
import com.group1.movebetter.repository.Repository
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class BirdController (private val viewModelScope: CoroutineScope, private val repository: Repository) {

    // TODO: retrieve the tokens from a database
    //private val refresh: Token = Tokens.refresh
    //private val access: Token = Tokens.access


    // sends email to the auth api
    fun getAuthToken(email: String) {
        val body = EmailBody(email)
        viewModelScope.launch {
            repository.getBirdToken(body)
        }
    }

    // sends the token from the verifying mail to the auth api (retrieves the tokens)
    fun postAuthToken(token: String) {
        val body = Token(token)
        viewModelScope.launch {
            repository.postMagicToken(body)
        }
    }

    // sends the refresh-token to the auth api (overwrites the current tokens)
    fun refresh() {
        viewModelScope.launch {
            repository.refresh("Bearer ${repository.myTokens.value!![0].refresh}")
        }
    }

    // sends the access-token to the api (retrieves the scooter)
    fun getBirds(location: Location) {

        // TODO: set rad to width of the screen
        val position = Position(
            location.latitude,
            location.longitude,
            location.altitude,
            location.accuracy,
            location.speed,
        )
        val radius = 1000
        val loc: String = Gson().toJson(position)
        viewModelScope.launch {
            repository.getBirds(location.latitude, location.longitude, radius, "Bearer ${repository.myTokens.value!![0].access}", loc)
        }
    }

    // create feature list for birds
    fun createBirdList(birds: List<Bird>): ArrayList<Feature> {
        val birdsFeature = ArrayList<Feature>()

        for (bird in birds) {
            val feature = createBirdFeature(bird.location, bird.battery_level, bird.estimated_range, bird.vehicle_class)

            birdsFeature.add(feature)
        }

        return birdsFeature
    }

    private fun createBirdFeature(location: BirdLocation, batteryLevel: Int, estimatedRange: Int, vehicleClass: String): Feature {
        val feature = Feature.fromGeometry(Point.fromLngLat(location.longitude, location.latitude))
        feature.addStringProperty("vehicleClass", vehicleClass)
        feature.addNumberProperty("batteryLevel", batteryLevel)
        feature.addNumberProperty("estimatedRange", estimatedRange)
        feature.addStringProperty("provider", "birds")
        return feature
    }

}