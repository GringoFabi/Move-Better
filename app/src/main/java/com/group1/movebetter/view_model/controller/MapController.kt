package com.group1.movebetter.view_model.controller

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.group1.movebetter.model.*
import com.group1.movebetter.repository.Repository
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import java.util.function.Predicate
import kotlin.math.*
import com.group1.movebetter.model.CityBikes
import com.group1.movebetter.model.CityBikesLocation
import com.group1.movebetter.model.CityBikesNetworks
import com.group1.movebetter.view_model.MapFragment
import kotlinx.coroutines.CoroutineScope


class MapController(private val viewModelScope: CoroutineScope, private val repository: Repository) {

    fun refreshSource(source: GeoJsonSource, featureList: ArrayList<Feature>) {
        source.setGeoJson(FeatureCollection.fromFeatures(featureList))
    }
}