package com.example.autopark.data.repository

import com.example.autopark.data.model.OverdueCharge
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
            val charge = doc.toObject(OverdueCharge::class.java)
            if (charge != null) {
                charge.id = doc.id
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
                doc.toObject(OverdueCharge::class.java)?.apply { id = doc.id }
            }
            Result.success(charges)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPendingOverdueCharges(ownerId: String): Result<List<OverdueCharge>> {
        return try {
            val docs = db.collection("overdue_charges")
                .whereEqualTo("ownerId", ownerId)
                .whereEqualTo("paymentStatus", "PENDING")
                .get()
                .await()
            val charges = docs.documents.mapNotNull { doc ->
                doc.toObject(OverdueCharge::class.java)?.apply { id = doc.id }
            }
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
                doc.toObject(OverdueCharge::class.java)?.apply { id = doc.id }
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
                doc.toObject(OverdueCharge::class.java)?.apply { id = doc.id }
            }
            Result.success(charges)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
