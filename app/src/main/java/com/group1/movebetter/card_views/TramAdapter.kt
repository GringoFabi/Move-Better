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
import com.group1.movebetter.model.Departure
import java.text.DateFormat
import java.text.SimpleDateFormat

class TramAdapter(private val data: List<Departure>) : RecyclerView.Adapter<TramAdapter.TramViewHolder?>() {

    lateinit var context: Context

    class TramViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cv: CardView
        var destinationTime: TextView
        var delay: TextView
        var tram: TextView
        var platform: TextView
        var destination: TextView
        var showVia: TextView
        var messages: TextView
        var button: Button

        init {
            cv = itemView.findViewById(R.id.tramCardView)
            destinationTime = itemView.findViewById(R.id.destinationTime)
            delay = itemView.findViewById(R.id.delay)
            tram = itemView.findViewById(R.id.tram)
            platform = itemView.findViewById(R.id.platform)
            destination = itemView.findViewById(R.id.destination)
            showVia = itemView.findViewById(R.id.showVia)
            messages = itemView.findViewById(R.id.messages)
            button = itemView.findViewById(R.id.btnNVV)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TramViewHolder {
        context = parent.context
        val v = LayoutInflater.from(parent.context).inflate(R.layout.card_view_departure_boards, parent, false)
        return TramViewHolder(v)
    }

    override fun onBindViewHolder(holder: TramViewHolder, position: Int) {
        holder.delay.text = data[position].arrival.delay.toString()
        holder.tram.text = data[position].train.name
        holder.platform.text = data[position].arrival.platform.toString()
        holder.destination.text = data[position].scheduledDestination

        if (data[position].arrival.time != null) {
            val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val time = dateFormat.parse(data[position].arrival?.time).toString()
            holder.destinationTime.text = time.subSequence(0, time.length-12)
        } else {
            holder.destinationTime.text = "N/A"
        }

        holder.showVia.setOnClickListener {
            var text = ""

            for (routeStation in data[position].route) {
                if (routeStation.showVia) {
                    text += routeStation.name + " - "
                }
            }
            holder.showVia.text = text
        }

        holder.button.setOnClickListener {
            Toast.makeText(context, "Go to NVV App", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

}