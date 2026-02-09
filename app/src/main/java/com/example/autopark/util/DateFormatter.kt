package com.example.autopark.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateFormatter {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    fun formatDate(timestamp: Long): String {
        return try {
            dateFormat.format(Date(timestamp))
        } catch (e: Exception) {
            ""
        }
    }

    fun formatTime(timestamp: Long): String {
        return try {
            timeFormat.format(Date(timestamp))
        } catch (e: Exception) {
            ""
        }
    }

    fun formatDateTime(timestamp: Long): String {
        return try {
            dateTimeFormat.format(Date(timestamp))
        } catch (e: Exception) {
            ""
        }
    }

    fun formatMonthYear(month: Int, year: Int): String {
        return try {
            val calendar = Calendar.getInstance()
            calendar.set(year, month - 1, 1)
            monthYearFormat.format(calendar.time)
        } catch (e: Exception) {
            ""
        }
    }

    fun getDurationString(startTime: Long, endTime: Long): String {
        val durationMs = endTime - startTime
        val durationMinutes = durationMs / (1000 * 60)
        val hours = durationMinutes / 60
        val minutes = durationMinutes % 60

        return when {
            hours > 0 -> "$hours h $minutes m"
            else -> "$minutes m"
        }
    }

    fun calculateDurationMinutes(startTime: Long, endTime: Long): Long {
        return (endTime - startTime) / (1000 * 60)
    }

    fun calculateDurationHours(startTime: Long, endTime: Long): Double {
        val durationMs = endTime - startTime
        return durationMs.toDouble() / (1000 * 60 * 60)
    }

    fun isToday(timestamp: Long): Boolean {
        val today = Calendar.getInstance()
        val givenDate = Calendar.getInstance()
        givenDate.timeInMillis = timestamp

        return today.get(Calendar.YEAR) == givenDate.get(Calendar.YEAR) &&
               today.get(Calendar.DAY_OF_YEAR) == givenDate.get(Calendar.DAY_OF_YEAR)
    }

    fun getMonthAndYear(): Pair<Int, Int> {
        val calendar = Calendar.getInstance()
        return Pair(calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR))
    }
}
