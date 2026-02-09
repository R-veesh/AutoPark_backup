package com.example.autopark.data.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Vehicle(
    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("ownerId")
    @set:PropertyName("ownerId")
    var ownerId: String = "",

    @get:PropertyName("vehicleNumber")
    @set:PropertyName("vehicleNumber")
    var vehicleNumber: String = "",

    @get:PropertyName("vehicleType")
    @set:PropertyName("vehicleType")
    var vehicleType: String = "", // Car, Bike, Truck, etc.

    @get:PropertyName("color")
    @set:PropertyName("color")
    var color: String = "",

    @get:PropertyName("brand")
    @set:PropertyName("brand")
    var brand: String = "",

    @get:PropertyName("model")
    @set:PropertyName("model")
    var model: String = "",

    @get:PropertyName("parkingLicenseValid")
    @set:PropertyName("parkingLicenseValid")
    var parkingLicenseValid: Boolean = true,

    @get:PropertyName("registrationExpiry")
    @set:PropertyName("registrationExpiry")
    var registrationExpiry: Long = 0,

    @get:PropertyName("createdAt")
    @set:PropertyName("createdAt")
    @ServerTimestamp
    var createdAt: Date? = null,

    @get:PropertyName("updatedAt")
    @set:PropertyName("updatedAt")
    @ServerTimestamp
    var updatedAt: Date? = null
)
