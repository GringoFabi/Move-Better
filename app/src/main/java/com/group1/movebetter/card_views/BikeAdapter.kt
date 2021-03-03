package com.group1.movebetter.card_views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView

import androidx.recyclerview.widget.RecyclerView
import com.group1.movebetter.R
import com.mapbox.geojson.Feature


class BikeAdapter(private val data: ArrayList<Feature>) : RecyclerView.Adapter<BikeAdapter.BikeViewHolder?>() {

    lateinit var context: Context

    class BikeViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cv: CardView
        var title: TextView
        var freeBikes: TextView
        var emptySlots: TextView
        var timestamp: TextView
        var button: Button

        init {
            cv = itemView.findViewById(R.id.bikeCardView)
            title = itemView.findViewById(R.id.bikeTitle)
            freeBikes = itemView.findViewById(R.id.freeBikes)
            emptySlots = itemView.findViewById(R.id.emptySlots)
            timestamp = itemView.findViewById(R.id.timestamp)
            button = itemView.findViewById(R.id.btnNextBike)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BikeViewHolder {
        context = parent.context
        val v = LayoutInflater.from(parent.context).inflate(R.layout.card_view_bikes, parent, false)
        return BikeViewHolder(v)
    }

    override fun onBindViewHolder(holder: BikeViewHolder, position: Int) {
        holder.title.text = data[position].getStringProperty("name")
        holder.freeBikes.text = "Free Bikes ${data[position].getNumberProperty("freeBikes")}"
        holder.emptySlots.text = "Empty Slots ${data[position].getNumberProperty("emptySlots")}"
        holder.timestamp.text = "Stand vom ${data[position].getStringProperty("timestamp")}"
        holder.button.setOnClickListener {
            Toast.makeText(context, "Go to Next Bike App", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}