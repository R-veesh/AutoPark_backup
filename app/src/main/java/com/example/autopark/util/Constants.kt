package com.example.autopark.util

object Constants {
    // Rate Types
    const val RATE_NORMAL = "NORMAL"
    const val RATE_VIP = "VIP"
    const val RATE_HOURLY = "HOURLY"
    const val RATE_OVERNIGHT = "OVERNIGHT"

    // Transaction Status
    const val STATUS_ACTIVE = "ACTIVE"
    const val STATUS_COMPLETED = "COMPLETED"
    const val STATUS_PENDING_PAYMENT = "PENDING_PAYMENT"

    // Payment Status
    const val PAYMENT_PENDING = "PENDING"
    const val PAYMENT_COMPLETED = "COMPLETED"
    const val PAYMENT_FAILED = "FAILED"

    // Payment Methods
    const val PAYMENT_CARD = "CARD"
    const val PAYMENT_CASH = "CASH"
    const val PAYMENT_UPI = "UPI"

    // Invoice Status
    const val INVOICE_PENDING = "PENDING"
    const val INVOICE_PAID = "PAID"
    const val INVOICE_PARTIAL = "PARTIAL"

    // User Roles
    const val ROLE_ADMIN = "admin"
    const val ROLE_DRIVER = "driver"

    // Collection Names
    const val COLLECTION_USERS = "users"
    const val COLLECTION_VEHICLES = "vehicles"
    const val COLLECTION_PARKING_LOTS = "parking_lots"
    const val COLLECTION_PARKING_RATES = "parking_rates"
    const val COLLECTION_PARKING_SPOTS = "parking_spots"
    const val COLLECTION_TRANSACTIONS = "parking_transactions"
    const val COLLECTION_INVOICES = "invoices"
    const val COLLECTION_OVERDUE_CHARGES = "overdue_charges"

    // Default values
    const val LATE_FEE_PERCENTAGE = 10.0
    const val MIN_CHARGE_AMOUNT = 20.0
    const val MAX_CHARGE_PER_DAY = 200.0
    
    // Time constants (in milliseconds)
    const val ONE_MINUTE = 1000 * 60L
    const val ONE_HOUR = ONE_MINUTE * 60
    const val ONE_DAY = ONE_HOUR * 24

    // Overnight hours
    const val OVERNIGHT_START_HOUR = 20 // 8 PM
    const val OVERNIGHT_END_HOUR = 8   // 8 AM
    
    // Invoice settings
    const val INVOICE_DUE_DAYS = 15
    const val OVERDUE_CHARGE_DAYS = 7
}
