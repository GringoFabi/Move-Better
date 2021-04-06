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
import com.group1.movebetter.model.Departure
import com.group1.movebetter.model.Messages
import com.group1.movebetter.model.RouteStation
import com.mapbox.mapboxsdk.geometry.LatLng
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class DBTramAdapter(private val data: List<Departure>,
                    private val openDbApp: () -> Unit,
                    private val navigateTo: (Double, Double) -> Unit,
                    private val selectedStation: LatLng?) : RecyclerView.Adapter<DBTramAdapter.DBTramViewHolder?>() {

    private lateinit var context: Context
    private lateinit var dbIconBitmap: Bitmap

    class DBTramViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cv: CardView
        var station: TextView
        var dbAppIcon: ImageView
        var destinationTime: TextView
        var delay: TextView
        var tram: TextView
        var platform: TextView
        var destination: TextView
        var showVia: TextView
        var messages: TextView
        var button: Button
        var navigateToButton: Button

        init {
            cv = itemView.findViewById(R.id.tramCardView)
            station = itemView.findViewById(R.id.station)
            dbAppIcon = itemView.findViewById(R.id.dbAppIcon)
            destinationTime = itemView.findViewById(R.id.destinationTime)
            delay = itemView.findViewById(R.id.delay)
            tram = itemView.findViewById(R.id.tram)
            platform = itemView.findViewById(R.id.platform)
            destination = itemView.findViewById(R.id.destination)
            showVia = itemView.findViewById(R.id.showVia)
            messages = itemView.findViewById(R.id.messages)
            button = itemView.findViewById(R.id.btnDB)
            navigateToButton = itemView.findViewById(R.id.btnGoToStation)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DBTramViewHolder {
        context = parent.context
        dbIconBitmap = BitmapFactory.decodeResource(context.resources, R.raw.db_app_icon)
        val v = LayoutInflater.from(parent.context).inflate(R.layout.card_view_db_departure_boards, parent, false)
        return DBTramViewHolder(v)
    }

    /**
     * Add information to card view
     */
    override fun onBindViewHolder(holder: DBTramViewHolder, position: Int) {
        val content = SpannableString(data[position].currentStopPlace.name)
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        holder.station.text = content

        setImages(holder)

        setDelay(holder, data[position])
        holder.tram.text = data[position].train.name
        holder.platform.text = data[position].arrival!!.platform
        holder.destination.text = data[position].scheduledDestination

        setDestinationTime(holder, data[position])

        setShowVia(holder, data[position].route)

        setMessages(holder, data[position].messages)

        holder.button.setOnClickListener {
            openDbApp.invoke()
        }

        holder.navigateToButton.setOnClickListener {
            if (selectedStation != null) {
                navigateTo.invoke(selectedStation.latitude, selectedStation.longitude)
            }
        }
    }

    private fun setImages(holder: DBTramViewHolder) {
        holder.dbAppIcon.setImageBitmap(dbIconBitmap)
        holder.dbAppIcon.scaleType = ImageView.ScaleType.CENTER
        holder.dbAppIcon.adjustViewBounds = true
    }

    private fun setMessages(holder: DBTramViewHolder, messages: Messages) {
        var text = ""

        if (!messages.delays.isNullOrEmpty()) {
            for (message in messages.delays) {
                if (message.superseded || text.contains(message.text)) {
                    continue
                }
                if (text == "") {
                    text = message.text
                } else {
                    text += "+++" + message.text
                }
            }
        }

        if (!messages.qos.isNullOrEmpty()) {
            for (message in messages.qos) {
                if (message.superseded || text.contains(message.text)) {
                    continue
                }
                if (text == "") {
                    text = message.text
                } else {
                    text += "+++" + message.text
                }
            }
        }

        holder.messages.text = text
    }

    @SuppressLint("NewApi")
    private fun setDelay(holder: DBTramViewHolder, departure: Departure) {
        val delay = departure.arrival!!.delay

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
    private fun setDestinationTime(holder: DBTramViewHolder, departure: Departure) {
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("GMT")
        val time = dateFormat.parse(departure.arrival!!.time)

        holder.destinationTime.text = "${time.toString().subSequence(11, 16)}"
    }

    private fun setShowVia(holder: DBTramViewHolder, route: List<RouteStation>) {
        var text = ""

        for (routeStation in route) {
            if (routeStation.showVia && text == "") {
                text = routeStation.name
            } else if (routeStation.showVia) {
                text += " - " + routeStation.name
            }
        }

        if (text == "") {
            text = holder.destination.text as String
        }
        holder.showVia.text = text
    }

    override fun getItemCount(): Int {
        return data.size
    }

}