package com.group1.movebetter.network.adapters

import com.group1.movebetter.model.*
import com.mapbox.mapboxsdk.style.expressions.Expression.let
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import java.lang.Exception

class NvvDeparturesAdapter {
    @FromJson
    fun fromJson(reader: JsonReader, jsonAdapter: JsonAdapter<NvvDeparture>): NvvDepartures {
        val list = ArrayList<NvvDeparture>()
        if (reader.hasNext()) {
            val token = reader.peek()
            if (token == JsonReader.Token.BEGIN_ARRAY) {
                reader.beginArray()
                while (reader.hasNext()) {
                    val response = jsonAdapter.fromJsonValue(reader.readJsonValue())
                    if (response != null) {
                        list.add(response)
                    }
                }
                reader.endArray()
            }
        }
        return NvvDepartures(list.toList())
    }
}