package com.example.autopark.data.model

import com.google.firebase.firestore.PropertyName
import java.util.Date

data class ParkingRate(
    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("parkingLotId")
    @set:PropertyName("parkingLotId")
    var parkingLotId: String = "",

    @get:PropertyName("rateType")
    @set:PropertyName("rateType")
    var rateType: String = "",

    @get:PropertyName("pricePerHour")
    @set:PropertyName("pricePerHour")
    var pricePerHour: Double = 0.0,

    @get:PropertyName("pricePerDay")
    @set:PropertyName("pricePerDay")
    var pricePerDay: Double = 0.0,

    @get:PropertyName("overnightPrice")
    @set:PropertyName("overnightPrice")
    var overnightPrice: Double = 0.0,

    @get:PropertyName("minChargeAmount")
    @set:PropertyName("minChargeAmount")
    var minChargeAmount: Double = 0.0,

    @get:PropertyName("maxChargePerDay")
    @set:PropertyName("maxChargePerDay")
    var maxChargePerDay: Double = 0.0,

    @get:PropertyName("isActive")
    @set:PropertyName("isActive")
    var isActive: Boolean = true,

    @get:PropertyName("vipMultiplier")
    @set:PropertyName("vipMultiplier")
    var vipMultiplier: Double = 1.0,

    // Store timestamps as Long to avoid deserialization issues
    @get:PropertyName("createdAt")
    @set:PropertyName("createdAt")
    var createdAt: Long? = null,

    @get:PropertyName("updatedAt")
    @set:PropertyName("updatedAt")
    var updatedAt: Long? = null
) {
    // Helper methods to get dates
    fun getCreatedAtDate(): Date? = createdAt?.let { Date(it) }
    fun getUpdatedAtDate(): Date? = updatedAt?.let { Date(it) }
}

enum class RateType {
    NORMAL, VIP, HOURLY, OVERNIGHT
}
