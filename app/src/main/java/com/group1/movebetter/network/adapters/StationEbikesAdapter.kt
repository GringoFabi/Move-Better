package com.group1.movebetter.network.adapters

import com.group1.movebetter.model.StationEbikes
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import java.lang.Exception

class StationEbikesAdapter {

    @FromJson
    fun fromJson(reader: JsonReader) : StationEbikes {
        var amount = 0L

        if(reader.peek() == JsonReader.Token.BOOLEAN) {
            try {
                if(reader.nextBoolean())
                {
                    amount = 1
                }
            }catch(e:Exception) {
                reader.skipValue()
            }
        }
        else
        {
            try {
                amount = reader.nextLong()
            }catch(e:Exception) {
                reader.skipValue()
            }

        }

        return StationEbikes(amount)
    }

}