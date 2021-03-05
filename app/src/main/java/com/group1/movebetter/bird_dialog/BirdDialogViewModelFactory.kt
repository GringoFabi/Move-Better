package com.group1.movebetter.bird_dialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.group1.movebetter.repository.Repository
import com.group1.movebetter.view_model.MapViewModel

class BirdDialogViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory  {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}