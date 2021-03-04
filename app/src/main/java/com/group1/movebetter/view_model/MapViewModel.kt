package com.group1.movebetter.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group1.movebetter.repository.Repository
import com.group1.movebetter.view_model.controller.BirdController
import com.group1.movebetter.view_model.controller.MapController
import com.group1.movebetter.view_model.controller.CityBikeController
import com.group1.movebetter.view_model.controller.MarudorController
import com.group1.movebetter.view_model.controller.StadaStationController

class MapViewModel(repository: Repository) : ViewModel() {
    val mapController: MapController = MapController(viewModelScope, repository)
    val cityBikeController: CityBikeController = CityBikeController(viewModelScope, repository)
    val stadaStationController: StadaStationController = StadaStationController(viewModelScope, repository)
    val marudorController: MarudorController = MarudorController(viewModelScope, repository)
    val birdController: BirdController = BirdController(viewModelScope, repository)
}