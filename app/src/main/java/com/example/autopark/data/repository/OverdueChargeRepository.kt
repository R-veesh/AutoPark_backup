package com.example.autopark.data.repository

import com.example.autopark.data.model.OverdueCharge
import com.example.autopark.util.TimestampUtils
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class OverdueChargeRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    suspend fun addOverdueCharge(charge: OverdueCharge): Result<String> {
        return try {
            val chargeMap = hashMapOf(
                "ownerId" to charge.ownerId,
                "ownerName" to charge.ownerName,
                "invoiceId" to charge.invoiceId,
                "invoiceNumber" to charge.invoiceNumber,
                "originalAmount" to charge.originalAmount,
                "lateFeePercentage" to charge.lateFeePercentage,
                "lateFeeAmount" to charge.lateFeeAmount,
                "totalAmount" to charge.totalAmount,
                "totalDueAmount" to charge.totalDueAmount,
                "overdueDays" to charge.overdueDays,
                "daysOverdue" to charge.daysOverdue,
                "dueDate" to charge.dueDate,
                "status" to charge.status,
                "paymentStatus" to charge.paymentStatus,
                "paymentDate" to charge.paymentDate,
                "amountPaid" to charge.amountPaid,
                "transactionId" to charge.transactionId,
                "parkingStatus" to charge.parkingStatus,
                "transactionPaymentStatus" to charge.transactionPaymentStatus,
                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            val docRef = db.collection("overdue_charges").add(chargeMap).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOverdueCharge(charge: OverdueCharge): Result<Unit> {
        return try {
            val chargeMap = hashMapOf(
                "ownerId" to charge.ownerId,
                "ownerName" to charge.ownerName,
                "invoiceId" to charge.invoiceId,
                "invoiceNumber" to charge.invoiceNumber,
                "originalAmount" to charge.originalAmount,
                "lateFeePercentage" to charge.lateFeePercentage,
                "lateFeeAmount" to charge.lateFeeAmount,
                "totalAmount" to charge.totalAmount,
                "totalDueAmount" to charge.totalDueAmount,
                "overdueDays" to charge.overdueDays,
                "daysOverdue" to charge.daysOverdue,
                "dueDate" to charge.dueDate,
                "status" to charge.status,
                "paymentStatus" to charge.paymentStatus,
                "paymentDate" to charge.paymentDate,
                "amountPaid" to charge.amountPaid,
                "transactionId" to charge.transactionId,
                "parkingStatus" to charge.parkingStatus,
                "transactionPaymentStatus" to charge.transactionPaymentStatus,
                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            db.collection("overdue_charges").document(charge.id).update(chargeMap as Map<String, Any>).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOverdueCharge(chargeId: String): Result<OverdueCharge> {
        return try {
            val doc = db.collection("overdue_charges").document(chargeId).get().await()
            val data = doc.data
            if (data != null) {
                val charge = OverdueCharge(
                    id = doc.id,
                    ownerId = data["ownerId"] as? String ?: "",
                    ownerName = data["ownerName"] as? String ?: "",
                    invoiceId = data["invoiceId"] as? String ?: "",
                    invoiceNumber = data["invoiceNumber"] as? String ?: "",
                    originalAmount = (data["originalAmount"] as? Number)?.toDouble() ?: 0.0,
                    lateFeePercentage = (data["lateFeePercentage"] as? Number)?.toDouble() ?: 0.0,
                    lateFeeAmount = (data["lateFeeAmount"] as? Number)?.toDouble() ?: 0.0,
                    totalAmount = (data["totalAmount"] as? Number)?.toDouble() ?: 0.0,
                    totalDueAmount = (data["totalDueAmount"] as? Number)?.toDouble() ?: 0.0,
                    overdueDays = (data["overdueDays"] as? Number)?.toInt() ?: 0,
                    daysOverdue = (data["daysOverdue"] as? Number)?.toInt() ?: 0,
                    dueDate = (data["dueDate"] as? Number)?.toLong() ?: 0,
                    status = data["status"] as? String ?: "PENDING",
                    paymentStatus = data["paymentStatus"] as? String ?: "PENDING",
                    paymentDate = (data["paymentDate"] as? Number)?.toLong(),
                    amountPaid = (data["amountPaid"] as? Number)?.toDouble() ?: 0.0,
                    transactionId = data["transactionId"] as? String ?: "",
                    parkingStatus = data["parkingStatus"] as? String ?: "",
                    transactionPaymentStatus = data["transactionPaymentStatus"] as? String ?: "",
                    createdAt = TimestampUtils.toMillis(data["createdAt"]),
                    updatedAt = TimestampUtils.toMillis(data["updatedAt"])
                )
                Result.success(charge)
            } else {
                Result.failure(Exception("Overdue charge not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOwnerOverdueCharges(ownerId: String): Result<List<OverdueCharge>> {
        return try {
            val docs = db.collection("overdue_charges")
                .whereEqualTo("ownerId", ownerId)
                .get()
                .await()
            val charges = docs.documents.mapNotNull { doc ->
                try {
                    val data = doc.data
                    if (data != null) {
                        OverdueCharge(
                            id = doc.id,
                            ownerId = data["ownerId"] as? String ?: "",
                            ownerName = data["ownerName"] as? String ?: "",
                            invoiceId = data["invoiceId"] as? String ?: "",
                            invoiceNumber = data["invoiceNumber"] as? String ?: "",
                            originalAmount = (data["originalAmount"] as? Number)?.toDouble() ?: 0.0,
                            lateFeePercentage = (data["lateFeePercentage"] as? Number)?.toDouble() ?: 0.0,
                            lateFeeAmount = (data["lateFeeAmount"] as? Number)?.toDouble() ?: 0.0,
                            totalAmount = (data["totalAmount"] as? Number)?.toDouble() ?: 0.0,
                            totalDueAmount = (data["totalDueAmount"] as? Number)?.toDouble() ?: 0.0,
                            overdueDays = (data["overdueDays"] as? Number)?.toInt() ?: 0,
                            daysOverdue = (data["daysOverdue"] as? Number)?.toInt() ?: 0,
                            dueDate = (data["dueDate"] as? Number)?.toLong() ?: 0,
                            status = data["status"] as? String ?: "PENDING",
                            paymentStatus = data["paymentStatus"] as? String ?: "PENDING",
                            paymentDate = (data["paymentDate"] as? Number)?.toLong(),
                            amountPaid = (data["amountPaid"] as? Number)?.toDouble() ?: 0.0,
                            transactionId = data["transactionId"] as? String ?: "",
                            parkingStatus = data["parkingStatus"] as? String ?: "",
                            transactionPaymentStatus = data["transactionPaymentStatus"] as? String ?: "",
                            createdAt = TimestampUtils.toMillis(data["createdAt"]),
                            updatedAt = TimestampUtils.toMillis(data["updatedAt"])
                        )
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
            Result.success(charges)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPendingOverdueCharges(ownerId: String): Result<List<OverdueCharge>> {
        return try {
            // Query without composite index - filter client-side
            val docs = db.collection("overdue_charges")
                .whereEqualTo("ownerId", ownerId)
                .get()
                .await()
            val charges = docs.documents.mapNotNull { doc ->
                try {
                    val data = doc.data
                    if (data != null) {
                        OverdueCharge(
                            id = doc.id,
                            ownerId = data["ownerId"] as? String ?: "",
                            ownerName = data["ownerName"] as? String ?: "",
                            invoiceId = data["invoiceId"] as? String ?: "",
                            invoiceNumber = data["invoiceNumber"] as? String ?: "",
                            originalAmount = (data["originalAmount"] as? Number)?.toDouble() ?: 0.0,
                            lateFeePercentage = (data["lateFeePercentage"] as? Number)?.toDouble() ?: 0.0,
                            lateFeeAmount = (data["lateFeeAmount"] as? Number)?.toDouble() ?: 0.0,
                            totalAmount = (data["totalAmount"] as? Number)?.toDouble() ?: 0.0,
                            totalDueAmount = (data["totalDueAmount"] as? Number)?.toDouble() ?: 0.0,
                            overdueDays = (data["overdueDays"] as? Number)?.toInt() ?: 0,
                            daysOverdue = (data["daysOverdue"] as? Number)?.toInt() ?: 0,
                            dueDate = (data["dueDate"] as? Number)?.toLong() ?: 0,
                            status = data["status"] as? String ?: "PENDING",
                            paymentStatus = data["paymentStatus"] as? String ?: "PENDING",
                            paymentDate = (data["paymentDate"] as? Number)?.toLong(),
                            amountPaid = (data["amountPaid"] as? Number)?.toDouble() ?: 0.0,
                            transactionId = data["transactionId"] as? String ?: "",
                            parkingStatus = data["parkingStatus"] as? String ?: "",
                            transactionPaymentStatus = data["transactionPaymentStatus"] as? String ?: "",
                            createdAt = TimestampUtils.toMillis(data["createdAt"]),
                            updatedAt = TimestampUtils.toMillis(data["updatedAt"])
                        )
                    } else null
                } catch (e: Exception) {
                    null
                }
            }.filter { it.paymentStatus == "PENDING" } // Client-side filter
            Result.success(charges)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChargesByInvoice(invoiceId: String): Result<List<OverdueCharge>> {
        return try {
            val docs = db.collection("overdue_charges")
                .whereEqualTo("invoiceId", invoiceId)
                .get()
                .await()
            val charges = docs.documents.mapNotNull { doc ->
                try {
                    val data = doc.data
                    if (data != null) {
                        OverdueCharge(
                            id = doc.id,
                            ownerId = data["ownerId"] as? String ?: "",
                            ownerName = data["ownerName"] as? String ?: "",
                            invoiceId = data["invoiceId"] as? String ?: "",
                            invoiceNumber = data["invoiceNumber"] as? String ?: "",
                            originalAmount = (data["originalAmount"] as? Number)?.toDouble() ?: 0.0,
                            lateFeePercentage = (data["lateFeePercentage"] as? Number)?.toDouble() ?: 0.0,
                            lateFeeAmount = (data["lateFeeAmount"] as? Number)?.toDouble() ?: 0.0,
                            totalAmount = (data["totalAmount"] as? Number)?.toDouble() ?: 0.0,
                            totalDueAmount = (data["totalDueAmount"] as? Number)?.toDouble() ?: 0.0,
                            overdueDays = (data["overdueDays"] as? Number)?.toInt() ?: 0,
                            daysOverdue = (data["daysOverdue"] as? Number)?.toInt() ?: 0,
                            dueDate = (data["dueDate"] as? Number)?.toLong() ?: 0,
                            status = data["status"] as? String ?: "PENDING",
                            paymentStatus = data["paymentStatus"] as? String ?: "PENDING",
                            paymentDate = (data["paymentDate"] as? Number)?.toLong(),
                            amountPaid = (data["amountPaid"] as? Number)?.toDouble() ?: 0.0,
                            transactionId = data["transactionId"] as? String ?: "",
                            parkingStatus = data["parkingStatus"] as? String ?: "",
                            transactionPaymentStatus = data["transactionPaymentStatus"] as? String ?: "",
                            createdAt = TimestampUtils.toMillis(data["createdAt"]),
                            updatedAt = TimestampUtils.toMillis(data["updatedAt"])
                        )
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
            Result.success(charges)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllOverdueCharges(): Result<List<OverdueCharge>> {
        return try {
            val docs = db.collection("overdue_charges").get().await()
            val charges = docs.documents.mapNotNull { doc ->
                try {
                    val data = doc.data
                    if (data != null) {
                        OverdueCharge(
                            id = doc.id,
                            ownerId = data["ownerId"] as? String ?: "",
                            ownerName = data["ownerName"] as? String ?: "",
                            invoiceId = data["invoiceId"] as? String ?: "",
                            invoiceNumber = data["invoiceNumber"] as? String ?: "",
                            originalAmount = (data["originalAmount"] as? Number)?.toDouble() ?: 0.0,
                            lateFeePercentage = (data["lateFeePercentage"] as? Number)?.toDouble() ?: 0.0,
                            lateFeeAmount = (data["lateFeeAmount"] as? Number)?.toDouble() ?: 0.0,
                            totalAmount = (data["totalAmount"] as? Number)?.toDouble() ?: 0.0,
                            totalDueAmount = (data["totalDueAmount"] as? Number)?.toDouble() ?: 0.0,
                            overdueDays = (data["overdueDays"] as? Number)?.toInt() ?: 0,
                            daysOverdue = (data["daysOverdue"] as? Number)?.toInt() ?: 0,
                            dueDate = (data["dueDate"] as? Number)?.toLong() ?: 0,
                            status = data["status"] as? String ?: "PENDING",
                            paymentStatus = data["paymentStatus"] as? String ?: "PENDING",
                            paymentDate = (data["paymentDate"] as? Number)?.toLong(),
                            amountPaid = (data["amountPaid"] as? Number)?.toDouble() ?: 0.0,
                            transactionId = data["transactionId"] as? String ?: "",
                            parkingStatus = data["parkingStatus"] as? String ?: "",
                            transactionPaymentStatus = data["transactionPaymentStatus"] as? String ?: "",
                            createdAt = TimestampUtils.toMillis(data["createdAt"]),
                            updatedAt = TimestampUtils.toMillis(data["updatedAt"])
                        )
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
            Result.success(charges)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
