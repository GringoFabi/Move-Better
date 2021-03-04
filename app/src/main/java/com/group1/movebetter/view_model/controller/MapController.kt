package com.group1.movebetter.view_model.controller

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.group1.movebetter.view_model.MapFragment
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlin.math.*


class MapController() {

    fun refreshSource(source: GeoJsonSource, featureList: ArrayList<Feature>) {
        source.setGeoJson(FeatureCollection.fromFeatures(featureList))
    }

    fun haversineFormular(destination: Location): Double {

        // Haversine formular (see more here: https://en.wikipedia.org/wiki/Haversine_formula)

        val start = currentLocation
        val earthRadius = 6371
        val distanceLatitude = degreeToRadial(start.latitude - destination.latitude)
        val distanceLongitude = degreeToRadial(start.longitude - destination.longitude)

        val a = sin(distanceLatitude / 2) * sin(distanceLatitude / 2) +
                cos(degreeToRadial(destination.latitude)) * cos(degreeToRadial(start.latitude)) *
                sin(distanceLongitude / 2) * sin(distanceLongitude / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a));
        val d = earthRadius * c; // Distance in km

        return d
    }

    private fun degreeToRadial(degree: Double): Double {
        return degree * (Math.PI/180)
    }

    fun getLocation(lat: Double, lng: Double) : Location {
        val location = Location("default")
        location.latitude = lat
        location.longitude = lng
        return location
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(activity: FragmentActivity, context: Context?, mapFragment: MapFragment) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

        if (!PermissionsManager.areLocationPermissionsGranted(context)) {
            permissionsManager = PermissionsManager(mapFragment)
            permissionsManager!!.requestLocationPermissions(activity)
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                currentLocation = location
            }
        }
    }

    // nearest-network logic

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var permissionsManager: PermissionsManager? = null
    var currentLocation: Location = initLocation()

    private fun initLocation() : Location {

        // Setting default location to Berlin

        val location = Location(LocationManager.GPS_PROVIDER)
        location.longitude = 13.404954
        location.latitude = 52.520008
        return location
    }
}