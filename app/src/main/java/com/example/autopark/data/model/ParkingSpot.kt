package com.example.autopark.data.model

import com.google.firebase.firestore.PropertyName

data class ParkingSpot(
    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("spotNumber")
    @set:PropertyName("spotNumber")
    var spotNumber: String = "",

    @get:PropertyName("floor")
    @set:PropertyName("floor")
    var floor: Int = 1,

    @get:PropertyName("isOccupied")
    @set:PropertyName("isOccupied")
    var isOccupied: Boolean = false,

    @get:PropertyName("occupiedBy")
    @set:PropertyName("occupiedBy")
    var occupiedBy: String? = null,

    @get:PropertyName("occupiedSince")
    @set:PropertyName("occupiedSince")
    var occupiedSince: Long? = null,

    @get:PropertyName("vehicleNumber")
    @set:PropertyName("vehicleNumber")
    var vehicleNumber: String? = null
)
