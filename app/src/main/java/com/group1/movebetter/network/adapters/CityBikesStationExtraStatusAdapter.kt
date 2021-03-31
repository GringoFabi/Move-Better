package com.group1.movebetter.network.adapters

import com.group1.movebetter.model.StationExtraStatus
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import java.lang.Exception

class CityBikesStationExtraStatusAdapter {

    //Adapter to convert an unexpected response to a StationExtraStatus
    @FromJson
    fun fromJson(reader: JsonReader) : StationExtraStatus {
        val status = ArrayList<String>()

        if(reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
            reader.beginObject()
            while(reader.hasNext())
            {
                try {
                    status.add(reader.nextString())
                }catch(e:Exception) {
                    reader.skipValue()
                }
            }
            reader.endObject()
        }
        else
        {
            try {
                status.add(reader.nextString())
            }catch(e:Exception) {
                reader.skipValue()
            }

        }

        return StationExtraStatus(status)
    }

}