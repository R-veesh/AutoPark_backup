package com.example.autopark.data.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Invoice(
    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("ownerId")
    @set:PropertyName("ownerId")
    var ownerId: String = "",

    @get:PropertyName("invoiceNumber")
    @set:PropertyName("invoiceNumber")
    var invoiceNumber: String = "",

    @get:PropertyName("month")
    @set:PropertyName("month")
    var month: Int = 0,

    @get:PropertyName("year")
    @set:PropertyName("year")
    var year: Int = 0,

    @get:PropertyName("fromDate")
    @set:PropertyName("fromDate")
    var fromDate: Long = 0,

    @get:PropertyName("toDate")
    @set:PropertyName("toDate")
    var toDate: Long = 0,

    @get:PropertyName("totalTransactions")
    @set:PropertyName("totalTransactions")
    var totalTransactions: Int = 0,

    @get:PropertyName("totalHours")
    @set:PropertyName("totalHours")
    var totalHours: Long = 0,

    @get:PropertyName("totalCharges")
    @set:PropertyName("totalCharges")
    var totalCharges: Double = 0.0,

    @get:PropertyName("overdueCharges")
    @set:PropertyName("overdueCharges")
    var overdueCharges: Double = 0.0,

    @get:PropertyName("totalAmount")
    @set:PropertyName("totalAmount")
    var totalAmount: Double = 0.0,

    @get:PropertyName("paymentStatus")
    @set:PropertyName("paymentStatus")
    var paymentStatus: String = "",

    @get:PropertyName("paymentDate")
    @set:PropertyName("paymentDate")
    var paymentDate: Long? = null,

    @get:PropertyName("dueDate")
    @set:PropertyName("dueDate")
    var dueDate: Long = 0,

    @get:PropertyName("amountPaid")
    @set:PropertyName("amountPaid")
    var amountPaid: Double = 0.0,

    @get:PropertyName("transactionIds")
    @set:PropertyName("transactionIds")
    var transactionIds: List<String> = emptyList(),

    @get:PropertyName("createdAt")
    @set:PropertyName("createdAt")
    @ServerTimestamp
    var createdAt: Date? = null,

    @get:PropertyName("updatedAt")
    @set:PropertyName("updatedAt")
    @ServerTimestamp
    var updatedAt: Date? = null
)

enum class InvoicePaymentStatus {
    PENDING, PAID, PARTIAL
}
