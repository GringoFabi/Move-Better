/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.group1.movebetter

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.group1.movebetter.bird_dialog.BirdDialog
import com.group1.movebetter.view_model.controller.MenuController

class MainActivity : AppCompatActivity() {

    // TODO: show dialog on condition (user wants to use bird)
    val show: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (show) {
            openDialog()
        }
    }

    private fun openDialog() {
        val birdDialog = BirdDialog()
        birdDialog.show(supportFragmentManager, "bird dialog")
    }

    private var menu: Menu? = null
    private val menuController = MenuController.getInstance()

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        this.menu = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.isCheckable) {
            item.isChecked = !item.isChecked
        } else {
            // refresh-method call
        }
        return when (item.itemId) {
            R.id.cityBikes -> {
                menuController!!.cityBikeItem.postValue(item.isChecked)
                true
            }
            R.id.marudor -> {
                menuController!!.marudorItem.postValue(item.isChecked)
                true
            }
            R.id.bird -> {
                // if (tokenUnset) {
                // openDialog()
                menuController!!.birdItem.postValue(item.isChecked)
                // }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
