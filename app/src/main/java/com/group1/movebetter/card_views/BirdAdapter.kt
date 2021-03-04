package com.group1.movebetter.card_views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.group1.movebetter.R
import com.mapbox.geojson.Feature

class BirdAdapter(private val data: ArrayList<Feature>, private val openBirdApp: () -> Unit) : RecyclerView.Adapter<BirdAdapter.BirdViewHolder?>() {

    lateinit var context: Context

    class BirdViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cv: CardView
        var title: TextView
        var estimatedRange: TextView
        var batteryLevelPB: ProgressBar
        var batteryLevel: TextView
        var button: Button

        init {
            cv = itemView.findViewById(R.id.birdCardView)
            title = itemView.findViewById(R.id.birdTitel)
            estimatedRange = itemView.findViewById(R.id.estimatedRange)
            batteryLevelPB = itemView.findViewById(R.id.batteryLevelPB)
            batteryLevel = itemView.findViewById(R.id.batteryLevel)
            button = itemView.findViewById(R.id.btnBirds)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BirdViewHolder {
        context = parent.context
        val v = LayoutInflater.from(parent.context).inflate(R.layout.card_view_birds, parent, false)
        return BirdViewHolder(v)
    }

    override fun onBindViewHolder(holder: BirdViewHolder, position: Int) {
        holder.title.text = data[position].getStringProperty("vehicleClass")
        holder.estimatedRange.text = "Estimated Range = ${data[position].getNumberProperty("estimatedRange")}"
        holder.batteryLevelPB.progress = (data[position].getNumberProperty("batteryLevel") as Double).toInt()
        holder.batteryLevel.text = "Derzeitiger Akkustand = ${data[position].getNumberProperty("batteryLevel")} %"
        holder.button.setOnClickListener {
            openBirdApp.invoke()
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

}