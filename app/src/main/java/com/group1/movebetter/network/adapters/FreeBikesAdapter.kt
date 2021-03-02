package com.group1.movebetter.network.adapters

import com.group1.movebetter.model.Company
import com.group1.movebetter.model.FreeBikes
import com.group1.movebetter.model.StationEbikes
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import java.lang.Exception

class FreeBikesAdapter {

    @FromJson
    fun fromJson(reader: JsonReader) : FreeBikes {
        var amount = -1L

        when {
            reader.peek() == JsonReader.Token.NULL -> {
                reader.skipValue()
            }
            reader.peek() == JsonReader.Token.NUMBER -> {
                try {
                    amount = reader.nextLong()
                }catch(e:Exception) {
                    reader.skipValue()
                }
            }
            else -> {
                reader.skipValue()
            }
        }

        return FreeBikes(amount)
    }

}