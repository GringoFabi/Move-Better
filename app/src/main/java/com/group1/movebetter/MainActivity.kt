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

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.group1.movebetter.bird_dialog.BirdDialog
import com.group1.movebetter.view_model.controller.MenuController

class MainActivity : AppCompatActivity() {

    private val FINE_LOCATION_CODE = 1

    // TODO: show dialog on condition (user wants to use bird)
    val show: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permissions are granted", Toast.LENGTH_SHORT).show()
            startApp()
        } else {
            requestFineLocationPermission()
        }
    }

    private fun startApp() {
        setContentView(R.layout.activity_main)
        if (show) {
            openDialog()
        }
    }

    private fun closeApp() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask()
        }
    }

    private fun requestFineLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissionDialog().show()
        } else {
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, FINE_LOCATION_CODE)
        }
    }

    private fun permissionDialog(): AlertDialog {
        return AlertDialog.Builder(this)
            .setTitle("Standort Erlaubnis")
            .setMessage("Wir benötigen diese Erlaubnis, um dir deine Position auf der Karte zeigen zu können.")
            .setPositiveButton("erlauben") { _, _ ->
                requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, FINE_LOCATION_CODE)
            }
            .setNegativeButton("ablehnen") { _, _ ->
                closeApp()
            }
            .create()

    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        if (requestCode == FINE_LOCATION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Erlaubnis erteilt", Toast.LENGTH_SHORT).show()
                startApp()
            } else {
                Toast.makeText(this, "Erlaubnis verweigert", Toast.LENGTH_SHORT).show()
                closeApp()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun requestPermission(permissionName: String, permissionRequestCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permissionName), permissionRequestCode)
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
            R.id.refresh -> {
                false
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
