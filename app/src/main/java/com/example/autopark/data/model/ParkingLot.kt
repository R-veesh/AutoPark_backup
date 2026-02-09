package com.example.autopark.data.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ParkingLot(
    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("name")
    @set:PropertyName("name")
    var name: String = "",

    @get:PropertyName("address")
    @set:PropertyName("address")
    var address: String = "",

    @get:PropertyName("latitude")
    @set:PropertyName("latitude")
    var latitude: Double = 0.0,

    @get:PropertyName("longitude")
    @set:PropertyName("longitude")
    var longitude: Double = 0.0,

    @get:PropertyName("city")
    @set:PropertyName("city")
    var city: String = "",

    @get:PropertyName("state")
    @set:PropertyName("state")
    var state: String = "",

    @get:PropertyName("zipCode")
    @set:PropertyName("zipCode")
    var zipCode: String = "",

    @get:PropertyName("totalSpots")
    @set:PropertyName("totalSpots")
    var totalSpots: Int = 0,

    @get:PropertyName("availableSpots")
    @set:PropertyName("availableSpots")
    var availableSpots: Int = 0,

    @get:PropertyName("description")
    @set:PropertyName("description")
    var description: String = "",

    @get:PropertyName("contactNumber")
    @set:PropertyName("contactNumber")
    var contactNumber: String = "",

    @get:PropertyName("openingTime")
    @set:PropertyName("openingTime")
    var openingTime: String = "",

    @get:PropertyName("closingTime")
    @set:PropertyName("closingTime")
    var closingTime: String = "",

    @get:PropertyName("is24Hours")
    @set:PropertyName("is24Hours")
    var is24Hours: Boolean = false,

    @get:PropertyName("createdAt")
    @set:PropertyName("createdAt")
    @ServerTimestamp
    var createdAt: Date? = null,

    @get:PropertyName("updatedAt")
    @set:PropertyName("updatedAt")
    @ServerTimestamp
    var updatedAt: Date? = null
)
