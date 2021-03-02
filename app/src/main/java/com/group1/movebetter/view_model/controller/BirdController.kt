package com.group1.movebetter.view_model.controller

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.group1.movebetter.model.*
import com.group1.movebetter.repository.Repository
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class BirdController (private val viewModelScope: CoroutineScope, private val repository: Repository) {

    // TODO: retrieve the tokens from a database
    private val refresh: Token = Tokens.refresh
    private val access: Token = Tokens.access

    private val _myResponse: MutableLiveData<EmailBody> = MutableLiveData()
    val myResponse: LiveData<EmailBody>
        get() = _myResponse

    private val _myTokens: MutableLiveData<BirdTokens> = MutableLiveData()
    val myTokens: LiveData<BirdTokens>
        get() = _myTokens

    private val _myBirds: MutableLiveData<Birds> = MutableLiveData()
    val myBirds: LiveData<Birds>
        get() = _myBirds

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
            val response = repository.postMagicToken(body)
            _myTokens.value = response
        }
    }

    // sends the refresh-token to the auth api (overwrites the current tokens)
    fun refresh() {
        viewModelScope.launch {
            val response = repository.refresh("Bearer ${refresh.token}")
            _myTokens.value = response
        }
    }

    // sends the access-token to the api (retrieves the scooter)
    fun getBirds(location: Location) {
        viewModelScope.launch {
            // TODO: set rad to width of the screen
            val position = Position(
                    location.latitude,
                    location.longitude,
                    location.altitude,
                    location.accuracy,
                    location.speed,
            )

            val loc: String = Gson().toJson(position)
            val radius = 1000

            val response = repository.getBirds(
                    location.latitude,
                    location.longitude,
                    radius, "Bearer ${access.token}", loc)

            _myBirds.value = response
        }
    }

    // create feature list for birds
    fun createBirdList(birds: Birds?): ArrayList<Feature> {
        val birdsFeature = ArrayList<Feature>()

        for (bird in birds!!.birds) {
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