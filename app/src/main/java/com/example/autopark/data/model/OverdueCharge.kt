package com.example.autopark.data.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class OverdueCharge(
    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("ownerId")
    @set:PropertyName("ownerId")
    var ownerId: String = "",

    @get:PropertyName("ownerName")
    @set:PropertyName("ownerName")
    var ownerName: String = "",

    @get:PropertyName("invoiceId")
    @set:PropertyName("invoiceId")
    var invoiceId: String = "",

    @get:PropertyName("invoiceNumber")
    @set:PropertyName("invoiceNumber")
    var invoiceNumber: String = "",

    @get:PropertyName("originalAmount")
    @set:PropertyName("originalAmount")
    var originalAmount: Double = 0.0,

    @get:PropertyName("lateFeePercentage")
    @set:PropertyName("lateFeePercentage")
    var lateFeePercentage: Double = 0.0,

    @get:PropertyName("lateFeeAmount")
    @set:PropertyName("lateFeeAmount")
    var lateFeeAmount: Double = 0.0,

    @get:PropertyName("totalAmount")
    @set:PropertyName("totalAmount")
    var totalAmount: Double = 0.0,

    @get:PropertyName("totalDueAmount")
    @set:PropertyName("totalDueAmount")
    var totalDueAmount: Double = 0.0,

    @get:PropertyName("overdueDays")
    @set:PropertyName("overdueDays")
    var overdueDays: Int = 0,

    @get:PropertyName("daysOverdue")
    @set:PropertyName("daysOverdue")
    var daysOverdue: Int = 0,

    @get:PropertyName("dueDate")
    @set:PropertyName("dueDate")
    var dueDate: Long = 0,

    @get:PropertyName("status")
    @set:PropertyName("status")
    var status: String = "PENDING",

    @get:PropertyName("paymentStatus")
    @set:PropertyName("paymentStatus")
    var paymentStatus: String = "PENDING",

    @get:PropertyName("paymentDate")
    @set:PropertyName("paymentDate")
    var paymentDate: Long? = null,

    @get:PropertyName("amountPaid")
    @set:PropertyName("amountPaid")
    var amountPaid: Double = 0.0,

    @get:PropertyName("createdAt")
    @set:PropertyName("createdAt")
    @ServerTimestamp
    var createdAt: Date? = null,

    @get:PropertyName("updatedAt")
    @set:PropertyName("updatedAt")
    @ServerTimestamp
    var updatedAt: Date? = null
)
