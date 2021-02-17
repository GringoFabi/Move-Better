package com.group1.movebetter.nextbike

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.group1.movebetter.repository.Repository

class MapViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
