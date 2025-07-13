package com.example.rush.database.converters

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LocationConverters {
    
    private val gson = Gson()
    
    @TypeConverter
    fun fromLatLngList(value: List<LatLng>): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toLatLngList(value: String): List<LatLng> {
        return try {
            val listType = object : TypeToken<List<LatLng>>() {}.type
            gson.fromJson(value, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}