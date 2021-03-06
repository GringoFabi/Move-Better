package com.group1.movebetter.view_model.controller

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationManager
import androidx.core.animation.doOnEnd
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlin.math.*


class MapController {

    var markerAnimator: ValueAnimator? = null
    var markerSelected = false

    /**
     * Reset selected-marker-source
     */
    fun resetSelectedMarkerLayer(style: Style, selectedMarker: String) {
        val source = style.getSourceAs<GeoJsonSource>(selectedMarker)
        source?.setGeoJson(FeatureCollection.fromFeatures(arrayOf()))
    }

    /**
     * Deselect Marker animation (reset the marker size to original)
     */
    fun deselectMarker(iconLayer: SymbolLayer, style: Style, clickedOnMap: Boolean, selectedMarker: String) {
        markerAnimator!!.setObjectValues(0.6f, 0.3f)
        markerAnimator!!.duration = 300
        markerAnimator!!.addUpdateListener { animator ->
            iconLayer.setProperties(
                    PropertyFactory.iconSize(animator.animatedValue as Float)
            )
        }
        if (clickedOnMap) {
            markerAnimator!!.doOnEnd {
                resetSelectedMarkerLayer(style, selectedMarker)
            }
        }
        markerAnimator!!.start()
        markerSelected = false
    }

    /**
     * Select Marker animation (make marker bigger)
     */
    fun selectMarker(iconLayer: SymbolLayer) {
        markerAnimator = ValueAnimator()
        markerAnimator!!.setObjectValues(0.3f, 0.6f)
        markerAnimator!!.duration = 300
        markerAnimator!!.addUpdateListener { animator ->
            iconLayer.setProperties(
                    PropertyFactory.iconSize(animator.animatedValue as Float)
            )
        }
        markerAnimator!!.start()
        markerSelected = true
    }

    /**
     * method for animating camera position
     */
    fun animateCameraPosition(mapboxMap: MapboxMap, feature: Feature?) {
        val latitude = feature!!.getNumberProperty("latitude") as Double
        val longitude = feature.getNumberProperty("longitude") as Double
        val builder = CameraPosition.Builder()
                .target(LatLng(latitude, longitude))

        val provider = feature.getStringProperty("provider")

        if (provider == "bikes" || provider == "birds") {
            builder.zoom(12.0)
        } else if (provider == "nvv") {
            builder.zoom(15.0)
        }

        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(builder.build()), 500)
    }

    /**
     * Refresh marker source
     */
    fun refreshSource(source: GeoJsonSource, featureList: ArrayList<Feature>) {
        source.setGeoJson(FeatureCollection.fromFeatures(featureList))
    }

    /**
     * method for calculating the relative distances between to geo coordinates
     */
    fun haversineFormular(destination: Location, start: Location = currentLocation) : Double {

        // Haversine formular (see more here: https://en.wikipedia.org/wiki/Haversine_formula)

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

    /**
     * method for constructing a location object on given data
     */
    fun getLocation(lat: Double, lng: Double) : Location {
        val location = Location("default")
        location.latitude = lat
        location.longitude = lng
        return location
    }

    /**
     * method for requesting the user's current location
     */
    @SuppressLint("MissingPermission")
    fun getCurrentLocation(activity: FragmentActivity): Task<Location> {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

        return fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                currentLocation = location
            }
        }
    }

    // nearest-network logic

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var currentLocation: Location = initLocation()

    /**
     * method for setting a default location which is used when Location Permissions are not granted
     */
    private fun initLocation() : Location {

        // Setting default location to Berlin

        val location = Location(LocationManager.GPS_PROVIDER)
        location.longitude = 13.404954
        location.latitude = 52.520008
        return location
    }
}