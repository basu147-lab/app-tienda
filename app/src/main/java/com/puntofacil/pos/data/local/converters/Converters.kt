package com.puntofacil.pos.data.local.converters

import androidx.room.TypeConverter
import java.math.BigDecimal
import java.util.Date

/**
 * Convertidores de tipo para Room Database
 * Maneja la conversión entre tipos complejos y tipos primitivos
 */
class Converters {
    
    /**
     * Convertidores para Date
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    /**
     * Convertidores para BigDecimal
     */
    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): String? {
        return value?.toString()
    }
    
    @TypeConverter
    fun toBigDecimal(value: String?): BigDecimal? {
        return if (value.isNullOrBlank()) {
            null
        } else {
            try {
                BigDecimal(value)
            } catch (e: NumberFormatException) {
                null
            }
        }
    }
    
    /**
     * Convertidores para List<String> (para permisos, códigos de respaldo, etc.)
     */
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.joinToString(",")
    }
    
    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return if (value.isNullOrBlank()) {
            null
        } else {
            value.split(",").map { it.trim() }
        }
    }
}