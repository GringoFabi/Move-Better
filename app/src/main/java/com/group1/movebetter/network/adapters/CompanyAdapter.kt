package com.group1.movebetter.network.adapters

import com.group1.movebetter.model.Company
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import java.lang.Exception

class CompanyAdapter {

    @FromJson
    fun fromJson(reader: JsonReader) : Company {
        val companyList = ArrayList<String>()

        if(reader.peek() == JsonReader.Token.BEGIN_ARRAY) {
            reader.beginArray()
            while(reader.hasNext())
            {
                try {
                    companyList.add(reader.nextString())
                }catch(e:Exception) {
                    reader.skipValue()
                }
            }
            reader.endArray()
        }
        else
        {
            try {
                companyList.add(reader.nextString())
            }catch(e:Exception) {
                reader.skipValue()
            }

        }

        return Company(companyList)
    }

}