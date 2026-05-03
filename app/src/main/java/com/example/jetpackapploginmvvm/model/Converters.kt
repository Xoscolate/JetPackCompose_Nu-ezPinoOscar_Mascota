package com.example.jetpackapploginmvvm.model

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromList(list: List<Int>?): String = list?.joinToString(",") ?: ""

    @TypeConverter
    fun toList(data: String?): List<Int> {
        if (data.isNullOrEmpty()) return emptyList()
        // mapNotNull asegura que si hay errores en el String, la App NO pete
        return data.split(",").mapNotNull { it.trim().toIntOrNull() }
    }
}