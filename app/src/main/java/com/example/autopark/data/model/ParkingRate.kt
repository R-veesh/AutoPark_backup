package com.example.autopark.data.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
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

    @get:PropertyName("createdAt")
    @set:PropertyName("createdAt")
    @ServerTimestamp
    var createdAt: Date? = null,

    @get:PropertyName("updatedAt")
    @set:PropertyName("updatedAt")
    @ServerTimestamp
    var updatedAt: Date? = null
)

enum class RateType {
    NORMAL, VIP, HOURLY, OVERNIGHT
}
