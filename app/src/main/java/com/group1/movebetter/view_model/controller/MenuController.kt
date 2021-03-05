package com.group1.movebetter.view_model.controller

import androidx.lifecycle.MutableLiveData

class MenuController {

    val cityBikeItem: MutableLiveData<Boolean> = MutableLiveData(true)

    val marudorItem: MutableLiveData<Boolean> = MutableLiveData(false)

    val birdItem: MutableLiveData<Boolean> = MutableLiveData(false)

    companion object {
        private var instance: MenuController? = null

        fun getInstance(): MenuController? {
            if (instance == null) {
                synchronized(MenuController::class.java) {
                    if (instance == null) {
                        instance = MenuController()
                    }
                }
            }
            return instance
        }
    }
}