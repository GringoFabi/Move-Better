package com.group1.movebetter.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView

import android.widget.TextView
import androidx.cardview.widget.CardView

import androidx.recyclerview.widget.RecyclerView
import com.group1.movebetter.R
import com.mapbox.geojson.Feature
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class BikeAdapter(private val data: ArrayList<Feature>, private val openNextBikeApp: () -> Unit, private val navigateTo: (Double, Double) -> Unit) : RecyclerView.Adapter<BikeAdapter.BikeViewHolder?>() {

    private lateinit var context: Context
    private lateinit var bikeIconBitmap: Bitmap

    class BikeViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cv: CardView
        var title: TextView
        var bikeAppIcon: ImageView
        var freeBikes: TextView
        var emptySlots: TextView
        var timestamp: TextView
        var button: Button
        var gotoButton: Button

        init {
            cv = itemView.findViewById(R.id.bikeCardView)
            title = itemView.findViewById(R.id.bikeTitle)
            bikeAppIcon = itemView.findViewById(R.id.bikeAppIcon)
            freeBikes = itemView.findViewById(R.id.freeBikes)
            emptySlots = itemView.findViewById(R.id.emptySlots)
            timestamp = itemView.findViewById(R.id.timestamp)
            button = itemView.findViewById(R.id.btnNextBike)
            gotoButton = itemView.findViewById(R.id.btnGoToStation)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BikeViewHolder {
        context = parent.context
        bikeIconBitmap = BitmapFactory.decodeResource(context.resources, R.raw.nextbike_app_icon)
        val v = LayoutInflater.from(parent.context).inflate(R.layout.card_view_bikes, parent, false)
        return BikeViewHolder(v)
    }

    @SuppressLint("SimpleDateFormat")
    // Add information to card view
    override fun onBindViewHolder(holder: BikeViewHolder, position: Int) {
        val content = SpannableString(data[position].getStringProperty("name"))
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        holder.title.text = content

        holder.bikeAppIcon.setImageBitmap(bikeIconBitmap)
        holder.bikeAppIcon.scaleType = ImageView.ScaleType.CENTER
        holder.bikeAppIcon.adjustViewBounds = true

        holder.freeBikes.text = "Free Bikes: ${data[position].getNumberProperty("freeBikes")}"
        holder.emptySlots.text = "Empty Slots: ${data[position].getNumberProperty("emptySlots")}"

        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("GMT")
        val time = dateFormat.parse(data[position].getStringProperty("timestamp"))
        holder.timestamp.text = "Stand vom ${time.toString().subSequence(0, 16)}"

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