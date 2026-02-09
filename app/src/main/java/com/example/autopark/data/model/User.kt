package com.example.autopark.data.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class User(
    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("email")
    @set:PropertyName("email")
    var email: String = "",

    @get:PropertyName("role")
    @set:PropertyName("role")
    var role: String = "",

    @get:PropertyName("name")
    @set:PropertyName("name")
    var name: String = "",

    @get:PropertyName("phoneNumber")
    @set:PropertyName("phoneNumber")
    var phoneNumber: String = "",

    @get:PropertyName("address")
    @set:PropertyName("address")
    var address: String = "",

    @get:PropertyName("city")
    @set:PropertyName("city")
    var city: String = "",

    @get:PropertyName("state")
    @set:PropertyName("state")
    var state: String = "",

    @get:PropertyName("zipCode")
    @set:PropertyName("zipCode")
    var zipCode: String = "",

    @get:PropertyName("licenseNumber")
    @set:PropertyName("licenseNumber")
    var licenseNumber: String = "",

@get:PropertyName("licenseExpiry")
    @set:PropertyName("licenseExpiry")
    var licenseExpiry: Long = 0,

    // Additional fields for user details
    @get:PropertyName("profilePictureUrl")
    @set:PropertyName("profilePictureUrl")
    var profilePictureUrl: String? = null,

    @get:PropertyName("parkingLicenseValid")
    @set:PropertyName("parkingLicenseValid")
    var parkingLicenseValid: Boolean = true,

    @get:PropertyName("createdAt")
    @set:PropertyName("createdAt")
    @ServerTimestamp
    var createdAt: Date? = null,

    @get:PropertyName("updatedAt")
    @set:PropertyName("updatedAt")
    @ServerTimestamp
    var updatedAt: Date? = null,

    @get:PropertyName("lastLoginAt")
    @set:PropertyName("lastLoginAt")
    @ServerTimestamp
    var lastLoginAt: Date? = null,

    @get:PropertyName("isVIP")
    @set:PropertyName("isVIP")
    var isVIP: Boolean = false,

    @get:PropertyName("totalSpent")
    @set:PropertyName("totalSpent")
    var totalSpent: Double = 0.0,

    @get:PropertyName("totalParkings")
    @set:PropertyName("totalParkings")
    var totalParkings: Int = 0,

    @get:PropertyName("totalCharges")
    @set:PropertyName("totalCharges")
    var totalCharges: Double = 0.0,

    @get:PropertyName("loyaltyPoints")
    @set:PropertyName("loyaltyPoints")
    var loyaltyPoints: Int = 0,

    // User preferences
    @get:PropertyName("preferredParkingLot")
    @set:PropertyName("preferredParkingLot")
    var preferredParkingLot: String? = null,

    @get:PropertyName("notificationSettings")
    @set:PropertyName("notificationSettings")
    var notificationSettings: Map<String, Boolean> = mapOf(
        "parking_reminders" to true,
        "payment_reminders" to true,
        "promotional_notifications" to false
    )
)
