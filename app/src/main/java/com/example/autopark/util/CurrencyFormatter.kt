package com.example.autopark.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyFormatter {
    private val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    
    init {
        formatter.currency = Currency.getInstance("INR")
    }

    fun formatCurrency(amount: Double): String {
        return formatter.format(amount)
    }

    fun formatAmount(amount: Double, currencyCode: String = "INR"): String {
        val fmt = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        fmt.currency = Currency.getInstance(currencyCode)
        return fmt.format(amount)
    }

    fun formatAsUSD(amount: Double): String {
        val fmt = NumberFormat.getCurrencyInstance(Locale.US)
        fmt.currency = Currency.getInstance("USD")
        return fmt.format(amount)
    }

    fun parseAmount(text: String): Double {
        return try {
            // Remove currency symbol and parse
            text.replace(Regex("[^\\d.]"), "").toDoubleOrNull() ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }

    fun roundToTwoDecimals(value: Double): Double {
        return String.format("%.2f", value).toDouble()
    }
}
