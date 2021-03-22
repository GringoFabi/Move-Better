package com.group1.movebetter.network.adapters

import com.group1.movebetter.model.EmptySlots
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import java.lang.Exception

class EmptySlotsAdapter {

    @FromJson
    fun fromJson(reader: JsonReader) : EmptySlots {
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
        return EmptySlots(amount)
    }

}