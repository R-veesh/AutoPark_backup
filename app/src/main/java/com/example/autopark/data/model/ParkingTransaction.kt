package com.example.autopark.data.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ParkingTransaction(
    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("parkingLotId")
    @set:PropertyName("parkingLotId")
    var parkingLotId: String = "",

    @get:PropertyName("vehicleId")
    @set:PropertyName("vehicleId")
    var vehicleId: String = "",

    @get:PropertyName("ownerId")
    @set:PropertyName("ownerId")
    var ownerId: String = "",

    @get:PropertyName("vehicleNumber")
    @set:PropertyName("vehicleNumber")
    var vehicleNumber: String = "",

    @get:PropertyName("entryTime")
    @set:PropertyName("entryTime")
    var entryTime: Long = 0,

    @get:PropertyName("exitTime")
    @set:PropertyName("exitTime")
    var exitTime: Long? = null,

    @get:PropertyName("duration")
    @set:PropertyName("duration")
    var duration: Long = 0,

    @get:PropertyName("rateType")
    @set:PropertyName("rateType")
    var rateType: String = "",

    @get:PropertyName("chargeAmount")
    @set:PropertyName("chargeAmount")
    var chargeAmount: Double = 0.0,

    @get:PropertyName("status")
    @set:PropertyName("status")
    var status: String = "",

    @get:PropertyName("paymentMethod")
    @set:PropertyName("paymentMethod")
    var paymentMethod: String = "",

    @get:PropertyName("paymentStatus")
    @set:PropertyName("paymentStatus")
    var paymentStatus: String = "",

    @get:PropertyName("notes")
    @set:PropertyName("notes")
    var notes: String = "",

    @get:PropertyName("createdAt")
    @set:PropertyName("createdAt")
    @ServerTimestamp
    var createdAt: Date? = null,

    @get:PropertyName("updatedAt")
    @set:PropertyName("updatedAt")
    @ServerTimestamp
    var updatedAt: Date? = null
)

enum class TransactionStatus {
    ACTIVE, COMPLETED, PENDING_PAYMENT
}

enum class PaymentStatus {
    PENDING, COMPLETED, FAILED
}
