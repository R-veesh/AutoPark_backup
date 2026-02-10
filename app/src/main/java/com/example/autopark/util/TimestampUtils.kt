package com.example.autopark.util

import com.google.firebase.Timestamp
import java.util.Date

/**
 * Utility class for handling Firebase timestamp conversions
 */
object TimestampUtils {
    
    /**
     * Convert any timestamp value (Timestamp, Long, or Date) to Long (milliseconds)
     */
    fun toMillis(value: Any?): Long? {
        return when (value) {
            is Timestamp -> value.toDate().time
            is Long -> value
            is Date -> value.time
            else -> null
        }
    }
    
    /**
     * Convert any timestamp value to Date
     */
    fun toDate(value: Any?): Date? {
        return when (value) {
            is Timestamp -> value.toDate()
            is Long -> Date(value)
            is Date -> value
            else -> null
        }
    }
    
    /**
     * Safely get Long timestamp from Firebase document field
     */
    fun getLongTimestamp(map: Map<String, Any?>, field: String): Long? {
        return toMillis(map[field])
    }
}
