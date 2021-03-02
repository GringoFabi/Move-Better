package com.group1.movebetter.view_model.controller

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.group1.movebetter.model.*
import com.group1.movebetter.repository.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class BirdController (private val viewModelScope: CoroutineScope, private val repository: Repository) {

    // TODO: retrieve the tokens from a database
    private val refresh: Token = Tokens.refresh
    private val access: Token = Tokens.access


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
            repository.refresh("Bearer ${refresh.token}")
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
            repository.getBirds(location.latitude, location.longitude, radius, "Bearer ${refresh.token}", loc)
        }
    }

}