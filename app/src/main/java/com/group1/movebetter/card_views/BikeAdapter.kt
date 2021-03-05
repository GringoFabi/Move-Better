package com.group1.movebetter.card_views

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

import android.widget.TextView
import androidx.cardview.widget.CardView

import androidx.recyclerview.widget.RecyclerView
import com.group1.movebetter.R
import com.mapbox.geojson.Feature
import java.text.DateFormat
import java.text.SimpleDateFormat


class BikeAdapter(private val data: ArrayList<Feature>, private val openNextBikeApp: () -> Unit, private val navigateTo: (Double, Double) -> Unit) : RecyclerView.Adapter<BikeAdapter.BikeViewHolder?>() {

    lateinit var context: Context

    class BikeViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cv: CardView
        var title: TextView
        var freeBikes: TextView
        var emptySlots: TextView
        var timestamp: TextView
        var button: Button
        var gotoButton: Button

        init {
            cv = itemView.findViewById(R.id.bikeCardView)
            title = itemView.findViewById(R.id.bikeTitle)
            freeBikes = itemView.findViewById(R.id.freeBikes)
            emptySlots = itemView.findViewById(R.id.emptySlots)
            timestamp = itemView.findViewById(R.id.timestamp)
            button = itemView.findViewById(R.id.btnNextBike)
            gotoButton = itemView.findViewById(R.id.btnGoToStation)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BikeViewHolder {
        context = parent.context
        val v = LayoutInflater.from(parent.context).inflate(R.layout.card_view_bikes, parent, false)
        return BikeViewHolder(v)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: BikeViewHolder, position: Int) {
        holder.title.text = data[position].getStringProperty("name")
        holder.freeBikes.text = "Free Bikes ${data[position].getNumberProperty("freeBikes")}"
        holder.emptySlots.text = "Empty Slots ${data[position].getNumberProperty("emptySlots")}"
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val time = dateFormat.parse(data[position].getStringProperty("timestamp")).toString()
        holder.timestamp.text = "Stand vom ${time.subSequence(0, 16)}"
        holder.button.setOnClickListener {
            openNextBikeApp.invoke()
        }
        holder.gotoButton.setOnClickListener {
            val lat = data[position].getNumberProperty("latitude").toDouble()
            val lng = data[position].getNumberProperty("longitude").toDouble()
            navigateTo.invoke(lat, lng)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}