package com.group1.movebetter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.group1.movebetter.model.CityBikesNetworks
import com.group1.movebetter.repository.Repository

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val repository = Repository()
        val viewModelFactory = MainViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
        viewModel.getNetworks()

        viewModel.getResponseNetworks.observe(this, Observer {
                res -> Log.d("Response", res.toString())
        })



        viewModel.getNetworksFiltered()
        viewModel.getResponseNetworkFiltered.observe(this, Observer {
                res -> Log.d("Response", res.toString())
        })

        viewModel.getResponseNetworks.observe(this, Observer {
                res -> res.networks.forEachIndexed { i,it -> if(i<100) Log.d(it.id.toString(), viewModel.getNetwork(it.id).toString())}
        })
    }
}