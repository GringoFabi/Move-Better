package com.group1.movebetter.bird_dialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group1.movebetter.repository.Repository
import com.group1.movebetter.view_model.controller.BirdController
import com.group1.movebetter.view_model.controller.MapController

class BirdDialogViewModel(repository: Repository) : ViewModel() {

    // view model class for binding inside the bird dialog

    val birdController: BirdController = BirdController(viewModelScope, repository, MapController())
}