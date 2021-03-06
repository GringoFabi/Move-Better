package com.group1.movebetter.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group1.movebetter.repository.Repository
import com.group1.movebetter.view_model.controller.*

class MapViewModel(repository: Repository) : ViewModel() {
    val mapController: MapController = MapController()
    val cityBikeController: CityBikeController = CityBikeController(viewModelScope, repository, mapController)
    val stadaStationController: StadaStationController = StadaStationController(viewModelScope, repository, mapController)
    val nvvController: NvvController = NvvController(viewModelScope, repository, mapController)
    val marudorController: MarudorController = MarudorController(viewModelScope, repository)
    val birdController: BirdController = BirdController(viewModelScope, repository, mapController)
}