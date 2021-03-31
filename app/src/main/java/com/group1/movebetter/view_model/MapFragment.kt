package com.group1.movebetter.view_model

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil.inflate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.*
import com.group1.movebetter.R
import com.group1.movebetter.database.getDatabase
import com.group1.movebetter.databinding.FragmentMapBinding
import com.group1.movebetter.repository.Repository
import com.group1.movebetter.util.Constants.Companion.DELAY_MILLIS
import com.group1.movebetter.adapters.BikeAdapter
import com.group1.movebetter.adapters.BirdAdapter
import com.group1.movebetter.adapters.DBTramAdapter
import com.group1.movebetter.adapters.NVVTrainAdapter
import com.group1.movebetter.view_model.controller.MenuController
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.Property.NONE
import com.mapbox.mapboxsdk.style.layers.Property.VISIBLE
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.CancellationException
import kotlin.random.Random.Default.nextInt


class MapFragment : Fragment(), OnMapReadyCallback, PermissionsListener, MapboxMap.OnMapClickListener {

    private lateinit var BIKE_STATION_LAYER: String
    private lateinit var BIKE_STATIONS: String
    private lateinit var BIKE_NETWORK_LAYER: String
    private lateinit var BIKE_NETWORKS: String
    private lateinit var SELECTED_MARKER_LAYER: String
    private lateinit var SELECTED_MARKER: String
    private lateinit var BIRD_SCOOTER_LAYER: String
    private lateinit var BIRD_SCOOTER: String
    private lateinit var DB_TRAM_STATION: String
    private lateinit var DB_TRAM_STATION_LAYER: String
    private lateinit var NVV_TRAIN_STATION: String
    private lateinit var NVV_TRAIN_STATION_LAYER: String

    private lateinit var BIKE_STATION_ICON_ID: String
    private lateinit var BIKE_NETWORK_ICON_ID: String
    private lateinit var BIRD_SCOOTER_ICON_ID: String
    private lateinit var DB_TRAM_STATION_ICON_ID: String
    private lateinit var NVV_STATION_ICON_ID: String

    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap
    private var permissionsManager: PermissionsManager? = null

    private lateinit var mapViewModel: MapViewModel

    private lateinit var binding : FragmentMapBinding

    private lateinit var repository : Repository

    private val menuController = MenuController.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
        context?.let { Mapbox.getInstance(it, getString(R.string.mapbox_access_token)) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = inflate(inflater, R.layout.fragment_map, container, false)
        mapView = binding.mapView

        // Get a reference to the ViewModel associated with this fragment.
        val db = getDatabase(context!!)
        var uuid = ""
        runBlocking {
            launch(Dispatchers.IO) {
                uuid = db.databaseDevUuidDao.getDevUuid("1").uuid
            }.join()
        }
        repository = Repository(db, uuid)
        val viewModelFactory = MapViewModelFactory(repository)
        mapViewModel = ViewModelProvider(this, viewModelFactory).get(MapViewModel::class.java)
        binding.mapViewModel = mapViewModel

        binding.lifecycleOwner = this

        initButtons()

        val rv = binding.singleLocationRecyclerView
        rv.setHasFixedSize(true)
        rv.visibility = View.GONE

        val llm = LinearLayoutManager(context)
        rv.layoutManager = llm

        initIds()

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        val currentLocationTask = mapViewModel.mapController.getCurrentLocation(this.requireActivity())

        currentLocationTask.addOnCompleteListener {
            mapViewModel.stadaStationController.getStations()
            mapViewModel.nvvController.getNvvStations()
            refreshNetworkRequests()
        }

        return binding.root
    }

    /**
     * Buttons for the "navigate to nearest bike/scooter/tram station" feature
     */
    private fun initButtons() {
        // Navigate to nearest bike station
        val nearestBike = binding.nearestBike
        nearestBike.setImageBitmap(BitmapFactory.decodeResource(resources, R.raw.bike))
        nearestBike.scaleType = ImageView.ScaleType.CENTER
        nearestBike.adjustViewBounds = true

        nearestBike.setOnClickListener {
            val closestBike = mapViewModel.cityBikeController.nearestBike
            if (closestBike != null) {
                onMapsNavigateTo(closestBike.latitude, closestBike.longitude)
            } else {
                Toast.makeText(context, "No nearest bike found!", Toast.LENGTH_SHORT).show()
            }
        }

        // Navigate to nearest scooter
        val nearestScooter = binding.nearestScooter
        nearestScooter.setImageBitmap(BitmapFactory.decodeResource(resources, R.raw.scooter))
        nearestScooter.scaleType = ImageView.ScaleType.CENTER
        nearestScooter.adjustViewBounds = true

        nearestScooter.setOnClickListener {
            val nearestBird = mapViewModel.birdController.nearestBird
            onMapsNavigateTo(nearestBird!!.location.latitude, nearestBird.location.longitude)
        }

        // Navigate to nearest db tram station
        val nearestTram = binding.nearestTram
        nearestTram.setImageBitmap(BitmapFactory.decodeResource(resources, R.raw.bahnhof))
        nearestTram.scaleType = ImageView.ScaleType.CENTER
        nearestTram.adjustViewBounds = true

        nearestTram.setOnClickListener {
            val nearestStation = mapViewModel.stadaStationController.nearestStation

            for (evaNumbers in nearestStation.evaNumbers) {
                val coordinates = evaNumbers.geographicCoordinates?.coordinates
                if (coordinates != null && evaNumbers.isMain) {
                    onMapsNavigateTo(coordinates[1], coordinates[0])
                    break
                }
            }
        }

        // Navigate to nearest nvv train station
        val nearestTrain = binding.nearestTrain
        nearestTrain.setImageBitmap(BitmapFactory.decodeResource(resources, R.raw.tram))
        nearestTrain.scaleType = ImageView.ScaleType.CENTER
        nearestTrain.adjustViewBounds = true

        nearestTrain.setOnClickListener {
            val nearestStation = mapViewModel.nvvController.nearestStation
            onMapsNavigateTo(nearestStation!!.lat, nearestStation.lng)
        }

        // Navigate to random nearest provider
        val nearestRandom = binding.random
        nearestRandom.setImageBitmap(BitmapFactory.decodeResource(resources, R.raw.random))
        nearestRandom.scaleType = ImageView.ScaleType.CENTER
        nearestRandom.adjustViewBounds = true

        nearestRandom.setOnClickListener {
            val random = nextInt(0, 4)
            if (random == 0) {
                if (mapViewModel.cityBikeController.nearestBike != null) {
                    nearestBike.performClick()
                }
            } else if (random == 1) {
                if (mapViewModel.birdController.nearestBird != null) {
                    nearestScooter.performClick()
                }
            } else if (random == 2) {
                if (mapViewModel.stadaStationController.nearestStation != null) {
                    nearestTram.performClick()
                }
            } else {
                if (mapViewModel.nvvController.nearestStation != null) {
                    nearestTrain.performClick()
                }
            }
            Toast.makeText(context, "The choice of a professional!", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * method for handling the refresh menu item
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.refresh -> {
                instantRefresh()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * method for performing a manual refresh (called through menu item "refresh")
     */
    private fun instantRefresh() {
        delayedRefreshRequestsJob?.cancel(CancellationException("Refresh"))
        if (delayedRefreshRequestsJob?.isCancelled == true) {
            mapViewModel.mapController.getCurrentLocation(activity!!)
            refreshNetworkRequests()
        }
        Toast.makeText(context, "refreshed just now", Toast.LENGTH_SHORT).show()
    }

    private var delayedRefreshRequestsJob: Job? = null

    private fun refreshNetworkRequests() {
        delayedRefreshRequestsJob = lifecycleScope.launch {
            mapViewModel.cityBikeController.getNetworks()
            mapViewModel.birdController.getBirds(mapViewModel.mapController.currentLocation)
            delay(DELAY_MILLIS)
            mapViewModel.mapController.getCurrentLocation(activity!!)
            refreshNetworkRequests()
        }
    }

    /**
     * Init Ids for layers, sources and icons
     */
    private fun initIds() {
        BIKE_STATION_LAYER = resources.getString(R.string.BIKE_STATION_LAYER)
        BIKE_STATIONS = resources.getString(R.string.BIKE_STATION)
        BIKE_NETWORK_LAYER = resources.getString(R.string.BIKE_NETWORK_LAYER)
        BIKE_NETWORKS = resources.getString(R.string.BIKE_NETWORK)
        SELECTED_MARKER = resources.getString(R.string.SELECTED_MARKER)
        SELECTED_MARKER_LAYER = resources.getString(R.string.SELECTED_MARKER_LAYER)
        BIRD_SCOOTER_LAYER = resources.getString(R.string.BIRD_SCOOTER_LAYER)
        BIRD_SCOOTER = resources.getString(R.string.BIRD_SCOOTER)
        DB_TRAM_STATION_LAYER = resources.getString(R.string.DB_TRAM_STATION_LAYER)
        DB_TRAM_STATION = resources.getString(R.string.DB_TRAM_STATION)
        NVV_TRAIN_STATION_LAYER = resources.getString(R.string.NVV_TRAIN_STATION_LAYER)
        NVV_TRAIN_STATION = resources.getString(R.string.NVV_TRAIN_STATION)

        BIKE_STATION_ICON_ID = resources.getString(R.string.BIKE_STATION_ICON_ID)
        BIKE_NETWORK_ICON_ID = resources.getString(R.string.BIKE_NETWORK_ICON_ID)
        BIRD_SCOOTER_ICON_ID = resources.getString(R.string.BIRD_SCOOTER_ICON_ID)
        DB_TRAM_STATION_ICON_ID = resources.getString(R.string.DB_TRAM_STATION_ICON_ID)
        NVV_STATION_ICON_ID = resources.getString(R.string.NVV_TRAIN_STATION_ICON_ID)
    }

    override fun onMapClick(point: LatLng): Boolean {
        val style = mapboxMap.style
        if (style != null) {
            val markerSelected = mapViewModel.mapController.markerSelected
            val pixel = mapboxMap.projection.toScreenLocation(point)

            val bikeNetworks = mapboxMap.queryRenderedFeatures(pixel, BIKE_NETWORK_LAYER)
            val bikeStation = mapboxMap.queryRenderedFeatures(pixel, BIKE_STATION_LAYER)
            val scooter = mapboxMap.queryRenderedFeatures(pixel, BIRD_SCOOTER_LAYER)
            val dbStation = mapboxMap.queryRenderedFeatures(pixel, DB_TRAM_STATION_LAYER)
            val nvvStation = mapboxMap.queryRenderedFeatures(pixel, NVV_TRAIN_STATION_LAYER)
            val selectedFeature = mapboxMap.queryRenderedFeatures(pixel, SELECTED_MARKER_LAYER)

            val selectedMarkerLayer = style.getLayer(SELECTED_MARKER_LAYER) as SymbolLayer

            // when clicked on a bikeNetwork get the stations via REST
            if (bikeNetworks.isNotEmpty()) {
                mapViewModel.mapController.animateCameraPosition(mapboxMap, bikeNetworks[0])
                mapViewModel.mapController.resetSelectedMarkerLayer(style, SELECTED_MARKER)
                mapViewModel.cityBikeController.getNetwork(bikeNetworks[0]!!.getStringProperty("id"))
                return false
            }

            // when clicked on icon which was already clicked on, show card view
            if (selectedFeature.size > 0 && markerSelected) {
                return false
            }

            // when clicked on map and a marker is selected, deselect it
            if (bikeStation.isEmpty() && scooter.isEmpty() && dbStation.isEmpty() && nvvStation.isEmpty()) {
                // when card view is shown and user clicks on map, make it invisible
                setButtonsVisibleRVInvisible()
                if (markerSelected) {
                    mapViewModel.mapController.deselectMarker(selectedMarkerLayer, style, true, SELECTED_MARKER)
                }
                return false
            }

            val source = style.getSourceAs<GeoJsonSource>(SELECTED_MARKER)

            // differentiate what layer was clicked and adapt the icon image
            when {
                bikeStation.isNotEmpty() -> {
                    source?.setGeoJson(FeatureCollection.fromFeature(bikeStation[0]))
                    selectedMarkerLayer.setProperties(iconImage(BIKE_STATION_ICON_ID))
                }
                scooter.isNotEmpty() -> {
                    source?.setGeoJson(FeatureCollection.fromFeature(scooter[0]))
                    selectedMarkerLayer.setProperties(iconImage(BIRD_SCOOTER_ICON_ID))
                }
                dbStation.isNotEmpty() -> {
                    source?.setGeoJson(FeatureCollection.fromFeature(dbStation[0]))
                    selectedMarkerLayer.setProperties(iconImage(DB_TRAM_STATION_ICON_ID))
                }
                nvvStation.isNotEmpty() -> {
                    source?.setGeoJson(FeatureCollection.fromFeature(nvvStation[0]))
                    selectedMarkerLayer.setProperties(iconImage(NVV_STATION_ICON_ID))
                }
            }

            // check if an icon is already selected
            if (markerSelected) {
                mapViewModel.mapController.deselectMarker(selectedMarkerLayer, style, false, SELECTED_MARKER)
            }

            // if clicked on a bike station/ scooter/ db station/ nvv station,
            // make it bigger, show information, and animate camera
            when {
                bikeStation.size > 0 -> {
                    mapViewModel.mapController.animateCameraPosition(mapboxMap, bikeStation[0])
                    mapViewModel.mapController.selectMarker(selectedMarkerLayer)
                    setAdapter(bikeStation[0])
                }
                dbStation.size > 0 -> {
                    mapViewModel.mapController.animateCameraPosition(mapboxMap, dbStation[0])
                    mapViewModel.mapController.selectMarker(selectedMarkerLayer)
                    setAdapter(dbStation[0])
                }
                scooter.size > 0 -> {
                    mapViewModel.mapController.animateCameraPosition(mapboxMap, scooter[0])
                    mapViewModel.mapController.selectMarker(selectedMarkerLayer)
                    setAdapter(scooter[0])
                }
                nvvStation.size > 0 -> {
                    mapViewModel.mapController.animateCameraPosition(mapboxMap, nvvStation[0])
                    mapViewModel.mapController.selectMarker(selectedMarkerLayer)
                    setAdapter(nvvStation[0])
                }
            }
        }
        return true
    }

    /**
     * Make Recycler View invisible and the "navigate to nearest" buttons visible
     */
    private fun setButtonsVisibleRVInvisible() {
        binding.singleLocationRecyclerView.adapter = null
        binding.singleLocationRecyclerView.visibility = View.GONE
        binding.nearestBike.visibility = View.VISIBLE
        if (mapViewModel.birdController.nearestBird != null) {
            binding.nearestScooter.visibility = View.VISIBLE
        }
        binding.nearestTrain.visibility = View.VISIBLE
        binding.nearestTram.visibility = View.VISIBLE
        binding.random.visibility = View.VISIBLE
    }

    /**
     * Make the "navigate to nearest" buttons invisible
     */
    private fun setButtonsInvisible() {
        binding.nearestBike.visibility = View.GONE
        binding.nearestScooter.visibility = View.GONE
        binding.nearestTrain.visibility = View.GONE
        binding.nearestTram.visibility = View.GONE
        binding.random.visibility = View.GONE
    }

    /**
     * Set Adapter of RecyclerView based on provider (bikes, birds, nvv, db)
     */
    private fun setAdapter(feature: Feature?) {
        val provider = feature!!.getStringProperty("provider")
        when {
            provider.equals("bikes") -> {
                setButtonsInvisible()
                binding.singleLocationRecyclerView.adapter = BikeAdapter(arrayListOf(feature), this::openNextBike, this::onMapsNavigateTo)
                binding.singleLocationRecyclerView.visibility = View.VISIBLE
            }
            provider.equals("birds") -> {
                setButtonsInvisible()
                binding.singleLocationRecyclerView.adapter = BirdAdapter(arrayListOf(feature), this::openBird, this::onMapsNavigateTo)
                binding.singleLocationRecyclerView.visibility = View.VISIBLE
            }
            provider.equals("nvv") -> {
                var name = feature.getStringProperty("name")
                if (name.contains("Ri.")) {
                    val index = name.indexOf("Ri.")
                    name = name.substring(0, index)
                }
                mapViewModel.nvvController.setSelectedStation(feature.getNumberProperty("latitude") as Double, feature.getNumberProperty("longitude") as Double)
                mapViewModel.marudorController.getNvvStation("$name Kassel")
            }
            else -> {
                val evaId = (feature.getNumberProperty("evaId") as Double).toLong()
                mapViewModel.stadaStationController.setSelectedStation(evaId)
                mapViewModel.marudorController.getArrival(evaId, 60)
            }
        }
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap

        mapboxMap.setStyle(Style.MAPBOX_STREETS) {
            loadImages(it)
            enableLocationComponent(it)
            setupLayers(it)
            mapboxMap.addOnMapClickListener(this@MapFragment)
        }
    }

    private fun setupLayers(style: Style) {
        // Bike Network Layer
        style.addSource(GeoJsonSource(BIKE_NETWORKS))

        style.addLayer(createLayer(BIKE_NETWORK_LAYER, BIKE_NETWORKS, BIKE_NETWORK_ICON_ID))

        // Bike Stations Layer
        style.addSource(GeoJsonSource(BIKE_STATIONS))

        style.addLayer(createLayer(BIKE_STATION_LAYER, BIKE_STATIONS, BIKE_STATION_ICON_ID))

        // Bird Scooter Layer
        style.addSource(GeoJsonSource(BIRD_SCOOTER))

        style.addLayer(createLayer(BIRD_SCOOTER_LAYER, BIRD_SCOOTER, BIRD_SCOOTER_ICON_ID))

        // Tram Station Layer
        style.addSource(GeoJsonSource(DB_TRAM_STATION))

        style.addLayer(createLayer(DB_TRAM_STATION_LAYER, DB_TRAM_STATION, DB_TRAM_STATION_ICON_ID))

        // NVV Station Layer
        style.addSource(GeoJsonSource(NVV_TRAIN_STATION))

        style.addLayer(createLayer(NVV_TRAIN_STATION_LAYER, NVV_TRAIN_STATION, NVV_STATION_ICON_ID))

        // Selected Icon Layer
        style.addSource(GeoJsonSource(SELECTED_MARKER))

        style.addLayer(SymbolLayer(SELECTED_MARKER_LAYER, SELECTED_MARKER)
                .withProperties(iconSize(0.3f)))

        // Add data to layers
        observers(style)
    }

    private fun createLayer(id: String, src: String, icon: String): SymbolLayer {
        val layer = SymbolLayer(id, src).withProperties(
                iconImage(icon),
                iconSize(0.3f))

        if (id == BIKE_NETWORK_LAYER) {
            layer.withFilter(eq((get("show")), literal(true)))
        }

        return layer
    }

    private fun observers(style: Style) {
        // Observer for bike networks
        repository.getResponseNetworks.observe(viewLifecycleOwner) {
            val networkSource = style.getSourceAs<GeoJsonSource>(BIKE_NETWORKS)
            if (it.isNotEmpty()) {
                val filterList = it.filter { network -> network.location != null && network.location.latitude != -1.0 }
                mapViewModel.cityBikeController.getNearestNetwork(filterList)
                val networks = mapViewModel.cityBikeController.createBikeNetworkList(filterList)
                mapViewModel.mapController.refreshSource(networkSource!!, networks)
            }
        }

        // Observer for bike stations
        repository.getResponseNetwork.observe(viewLifecycleOwner) {
            val networkSource = style.getSourceAs<GeoJsonSource>(BIKE_NETWORKS)
            val stationSource = style.getSourceAs<GeoJsonSource>(BIKE_STATIONS)
            if (it.isNotEmpty()) {
                val networks = mapViewModel.cityBikeController.updateCurrentNetwork(it[0])
                val stations = mapViewModel.cityBikeController.exchangeNetworkWithStations(it[0])

                if (!binding.singleLocationRecyclerView.isVisible) {
                    binding.nearestBike.visibility = View.VISIBLE
                }

                mapViewModel.mapController.refreshSource(networkSource!!, networks)
                mapViewModel.mapController.refreshSource(stationSource!!, stations)
            }
        }

        // Observer for birds (scooters)
        repository.myBirds.observe(viewLifecycleOwner) {
            val birdSource = style.getSourceAs<GeoJsonSource>(BIRD_SCOOTER)
            if (it.isNotEmpty()) {
                val birds = mapViewModel.birdController.createBirdList(it)
                mapViewModel.birdController.getNearestBird(it)

                if (!binding.singleLocationRecyclerView.isVisible) {
                    binding.nearestScooter.visibility = View.VISIBLE
                }

                mapViewModel.mapController.refreshSource(birdSource!!, birds)
            }
        }

        // Observer for db stations
        repository.getResponseStations.observe(viewLifecycleOwner) {
            val stationSource = style.getSourceAs<GeoJsonSource>(DB_TRAM_STATION)
            if (it.isNotEmpty()) {
                val stations = mapViewModel.stadaStationController.createStationList(it)
                mapViewModel.stadaStationController.getNearestStation(it)

                if (!binding.singleLocationRecyclerView.isVisible) {
                    binding.nearestTram.visibility = View.VISIBLE
                }

                mapViewModel.mapController.refreshSource(stationSource!!, stations)
            }
        }

        // Observer for nvv stations
        repository.getNvvStations.observe(viewLifecycleOwner) {
            val stationSource = style.getSourceAs<GeoJsonSource>(NVV_TRAIN_STATION)
            if (it.isNotEmpty()) {
                val stations = mapViewModel.nvvController.createStationList(it)
                mapViewModel.nvvController.getNearestStation(it)

                if (!binding.singleLocationRecyclerView.isVisible) {
                    binding.nearestTrain.visibility = View.VISIBLE
                }

                mapViewModel.mapController.refreshSource(stationSource!!, stations)
            }
        }

        // Observer for stations by term
        repository.getNvvStation.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                val nextNvvStation = mapViewModel.nvvController.nearestEvaId(it.filter { nextNvvStation -> nextNvvStation.id != null })
                if (nextNvvStation != null) {
                    mapViewModel.marudorController.getNvvArrival(nextNvvStation.id)
                }
            }
        }

        // Observer for Departure Board
        repository.getResponseArrival.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                val departures = it.filter { departure -> departure.arrival != null && departure.arrival.time != "N/A" }

                if (mapViewModel.stadaStationController.selectedStation != null) {
                    binding.singleLocationRecyclerView.adapter = DBTramAdapter(
                            departures,
                            this::openDB,
                            this::onMapsNavigateTo,
                            mapViewModel.stadaStationController.selectedStation
                    )
                    setButtonsInvisible()
                    binding.singleLocationRecyclerView.visibility = View.VISIBLE
                }
            }
        }

        // Observer for Nvv Departure Board
        repository.getResponseNvvArrival.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                if (mapViewModel.nvvController.selectedStation != null) {
                    binding.singleLocationRecyclerView.adapter = NVVTrainAdapter(
                            it,
                            this::openNvv,
                            this::onMapsNavigateTo,
                            mapViewModel.nvvController.selectedStation
                    )
                    setButtonsInvisible()
                    binding.singleLocationRecyclerView.visibility = View.VISIBLE
                }
            }
        }

        // Observer for filtering bikes
        menuController!!.cityBikeItem.observe(viewLifecycleOwner) {
            val networkLayer = style.getLayer(BIKE_NETWORK_LAYER) as SymbolLayer
            val stationLayer = style.getLayer(BIKE_STATION_LAYER) as SymbolLayer
            if (it) {
                networkLayer.setProperties(visibility(VISIBLE))
                stationLayer.setProperties(visibility(VISIBLE))
            } else {
                networkLayer.setProperties(visibility(NONE))
                stationLayer.setProperties(visibility(NONE))
            }
        }

        // Observer for filtering db trams
        menuController.marudorItem.observe(viewLifecycleOwner) {
            val tramLayer = style.getLayer(DB_TRAM_STATION_LAYER) as SymbolLayer
            if (it) {
                tramLayer.setProperties(visibility(VISIBLE))
            } else {
                tramLayer.setProperties(visibility(NONE))
            }
        }

        // Observer for filtering nvv trams
        menuController.nvvItem.observe(viewLifecycleOwner) {
            val nvvLayer = style.getLayer(NVV_TRAIN_STATION_LAYER) as SymbolLayer
            if (it) {
                nvvLayer.setProperties(visibility(VISIBLE))
            } else {
                nvvLayer.setProperties(visibility(NONE))
            }
        }

        // Observer for filtering birds
        menuController.birdItem.observe(viewLifecycleOwner) {
            val birdLayer = style.getLayer(BIRD_SCOOTER_LAYER) as SymbolLayer
            if (it) {
                birdLayer.setProperties(visibility(VISIBLE))
            } else {
                birdLayer.setProperties(visibility(NONE))
            }
        }

        // Observer for turning overlay on and off
        menuController.overlayItem.observe(viewLifecycleOwner) {
            val networkLayer = style.getLayer(BIKE_NETWORK_LAYER) as SymbolLayer
            val stationLayer = style.getLayer(BIKE_STATION_LAYER) as SymbolLayer
            val tramLayer = style.getLayer(DB_TRAM_STATION_LAYER) as SymbolLayer
            val nvvLayer = style.getLayer(NVV_TRAIN_STATION_LAYER) as SymbolLayer
            val birdLayer = style.getLayer(BIRD_SCOOTER_LAYER) as SymbolLayer

            networkLayer.setProperties(iconAllowOverlap(it))
            stationLayer.setProperties(iconAllowOverlap(it))
            tramLayer.setProperties(iconAllowOverlap(it))
            nvvLayer.setProperties(iconAllowOverlap(it))
            birdLayer.setProperties(iconAllowOverlap(it))
        }
    }

    /**
     * load images in map
     */
    private fun loadImages(style: Style) {
        style.addImage(BIKE_STATION_ICON_ID, BitmapFactory.decodeResource(resources, R.raw.bike))
        style.addImage(BIKE_NETWORK_ICON_ID, BitmapFactory.decodeResource(resources, R.raw.network))
        style.addImage(BIRD_SCOOTER_ICON_ID, BitmapFactory.decodeResource(resources, R.raw.scooter))
        style.addImage(DB_TRAM_STATION_ICON_ID, BitmapFactory.decodeResource(resources, R.raw.bahnhof))
        style.addImage(NVV_STATION_ICON_ID, BitmapFactory.decodeResource(resources, R.raw.tram))
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(context)) {

            // Create and customize the LocationComponent's options
            val customLocationComponentOptions = LocationComponentOptions.builder(context!!)
                .trackingGesturesManagement(true)
                .accuracyColor(ContextCompat.getColor(context!!, R.color.blue))
                .build()

            val locationComponentActivationOptions = LocationComponentActivationOptions.builder(context!!, loadedMapStyle)
                .locationComponentOptions(customLocationComponentOptions)
                .build()

            // Get an instance of the LocationComponent and then adjust its settings
            mapboxMap.locationComponent.apply {

                // Activate the LocationComponent with options
                activateLocationComponent(locationComponentActivationOptions)

                // Enable to make the LocationComponent visible
                isLocationComponentEnabled = true

                // Set the LocationComponent's camera mode
                cameraMode = CameraMode.TRACKING

                // Set the LocationComponent's render mode
                renderMode = RenderMode.COMPASS
            }
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager!!.requestLocationPermissions(this.activity)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissionsManager!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        Toast.makeText(context, "R.string.user_location_permission_explanation", Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocationComponent(mapboxMap.style!!)
        } else {
            Toast.makeText(context, "R.string.user_location_permission_not_granted", Toast.LENGTH_LONG).show()
            activity!!.finish()
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapboxMap.removeOnMapClickListener(this)
        mapViewModel.mapController.markerAnimator?.cancel()
        mapView.onDestroy()
    }

    // Open other Apps or their link to play store

    /**
     * Open Google-Maps with an active walking Route to given Lat/lng
     */
    private fun onMapsNavigateTo(lat: Double, lng: Double) {
        val gmmIntentUri: Uri = Uri.parse("google.navigation:q=$lat,$lng&mode=w") //mode w for walking
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        if (mapIntent.resolveActivity(context!!.packageManager) != null) {
            startActivity(mapIntent)
        } else {
            openPlayStoreFor("com.google.android.apps.maps")
        }
    }

    /**
     * opens Bird Application or their ref. in PlayStore
     */
    private fun openBird() {
        val intentL = context!!.packageManager.getLaunchIntentForPackage("co.bird.android")

        if (intentL?.resolveActivity(context!!.packageManager) != null) {
            startActivity(intentL)
        } else {
            openPlayStoreFor("co.bird.android")
        }
    }

    /**
     * opens NVV Application or their ref. in PlayStore
     */
    private fun openNvv() {
        val intentL = context!!.packageManager.getLaunchIntentForPackage("de.hafas.android.nvv")

        if (intentL?.resolveActivity(context!!.packageManager) != null) {
            startActivity(intentL)
        } else {
            openPlayStoreFor("de.hafas.android.nvv")
        }
    }

    /**
     * opens NextBike Application or their ref. in PlayStore
     */
    private fun openNextBike() {
        val intentL = context!!.packageManager.getLaunchIntentForPackage("de.nextbike")

        if (intentL?.resolveActivity(context!!.packageManager) != null) {
            startActivity(intentL)
        } else {
            openPlayStoreFor("de.nextbike")
        }
    }

    /**
     * opens DB Application or their ref. in PlayStore
     */
    private fun openDB() {
        val intentL = context!!.packageManager.getLaunchIntentForPackage("de.hafas.android.db")

        if (intentL?.resolveActivity(context!!.packageManager) != null) {
            startActivity(intentL)
        } else {
            openPlayStoreFor("de.hafas.android.db")
        }
    }

    /**
     * opens Playstore Application or their ref. in Browser
     */
    private fun openPlayStoreFor(packageName: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }
}