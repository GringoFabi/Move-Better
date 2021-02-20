package com.group1.movebetter.network.adapters

import com.group1.movebetter.model.CityBikesStationExtraStatus
import com.group1.movebetter.model.Company
import com.group1.movebetter.model.StationName
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import java.lang.Exception

class CityBikesStationExtraStatusAdapter {

    @FromJson
    fun fromJson(reader: JsonReader) : CityBikesStationExtraStatus {
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

        return CityBikesStationExtraStatus(status)
    }

}