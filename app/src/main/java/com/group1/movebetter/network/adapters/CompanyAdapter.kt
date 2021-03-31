package com.group1.movebetter.network.adapters

import com.group1.movebetter.model.Company
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import java.lang.Exception

class CompanyAdapter {
    //Adapter to convert an unexpected response to a proper Company. Response can be a String or a list of Strings
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