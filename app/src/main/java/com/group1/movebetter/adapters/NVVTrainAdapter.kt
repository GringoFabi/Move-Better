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
import com.group1.movebetter.model.*
import com.mapbox.mapboxsdk.geometry.LatLng
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class NVVTrainAdapter(private val data: List<NvvDeparture>,
                      private val openNvvApp: () -> Unit,
                      private val navigateTo: (Double, Double) -> Unit,
                      private val selectedStation: LatLng?) : RecyclerView.Adapter<NVVTrainAdapter.NVVTrainViewHolder?>() {

    private lateinit var context: Context
    private lateinit var nvvIconBitmap: Bitmap

    class NVVTrainViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cv: CardView
        var station: TextView
        var nvvAppIcon: ImageView
        var destinationTime: TextView
        var delay: TextView
        var train: TextView
        var destination: TextView
        var button: Button
        var navigateToButton: Button

        init {
            cv = itemView.findViewById(R.id.trainCardView)
            station = itemView.findViewById(R.id.station)
            nvvAppIcon = itemView.findViewById(R.id.nvvAppIcon)
            destinationTime = itemView.findViewById(R.id.destinationTime)
            delay = itemView.findViewById(R.id.delay)
            train = itemView.findViewById(R.id.train)
            destination = itemView.findViewById(R.id.destination)
            button = itemView.findViewById(R.id.btnNVV)
            navigateToButton = itemView.findViewById(R.id.btnGoToStation)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NVVTrainViewHolder {
        context = parent.context
        nvvIconBitmap = BitmapFactory.decodeResource(context.resources, R.raw.nvv_app_icon)
        val v = LayoutInflater.from(parent.context).inflate(R.layout.card_view_nvv_departure_boards, parent, false)
        return NVVTrainViewHolder(v)
    }

    // Add information to card view
    override fun onBindViewHolder(holder: NVVTrainViewHolder, position: Int) {
        val content = SpannableString(data[position].currentStation.title)
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        holder.station.text = content

        holder.nvvAppIcon.setImageBitmap(nvvIconBitmap)
        holder.nvvAppIcon.scaleType = ImageView.ScaleType.CENTER
        holder.nvvAppIcon.adjustViewBounds = true

        setDelay(holder, data[position])
        holder.train.text = data[position].train.name
        holder.destination.text = data[position].finalDestination

        setDestinationTime(holder, data[position])

        holder.button.setOnClickListener {
            openNvvApp.invoke()
        }

        holder.navigateToButton.setOnClickListener {
            if (selectedStation != null) {
                navigateTo.invoke(selectedStation.latitude, selectedStation.longitude)
            }
        }
    }

    @SuppressLint("NewApi")
    private fun setDelay(holder: NVVTrainViewHolder, departure: NvvDeparture) {
        val delay = departure.arrival.delay

        if (delay <= 0) {
            holder.delay.setTextColor(context.getColor(R.color.green))
            holder.destinationTime.setTextColor(context.getColor(R.color.green))
        } else {
            holder.delay.setTextColor(context.getColor(R.color.red))
            holder.destinationTime.setTextColor(context.getColor(R.color.red))
        }

        holder.delay.text = "+${delay}"
    }

    @SuppressLint("SimpleDateFormat")
    private fun setDestinationTime(holder: NVVTrainViewHolder, departure: NvvDeparture) {
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("GMT")
        val time = dateFormat.parse(departure.arrival!!.time)

        holder.destinationTime.text = "${time.toString().subSequence(11, 16)}"
    }

    override fun getItemCount(): Int {
        return data.size
    }

}