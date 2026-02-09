package com.example.autopark.data.repository

import com.example.autopark.data.model.Invoice
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class InvoiceRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    suspend fun addInvoice(invoice: Invoice): Result<String> {
        return try {
            val invoiceMap = hashMapOf(
                "ownerId" to invoice.ownerId,
                "invoiceNumber" to invoice.invoiceNumber,
                "month" to invoice.month,
                "year" to invoice.year,
                "fromDate" to invoice.fromDate,
                "toDate" to invoice.toDate,
                "totalTransactions" to invoice.totalTransactions,
                "totalHours" to invoice.totalHours,
                "totalCharges" to invoice.totalCharges,
                "overdueCharges" to invoice.overdueCharges,
                "totalAmount" to invoice.totalAmount,
                "paymentStatus" to invoice.paymentStatus,
                "paymentDate" to invoice.paymentDate,
                "dueDate" to invoice.dueDate,
                "amountPaid" to invoice.amountPaid,
                "transactionIds" to invoice.transactionIds,
                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            val docRef = db.collection("invoices").add(invoiceMap).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateInvoice(invoice: Invoice): Result<Unit> {
        return try {
            val invoiceMap = hashMapOf(
                "ownerId" to invoice.ownerId,
                "invoiceNumber" to invoice.invoiceNumber,
                "month" to invoice.month,
                "year" to invoice.year,
                "fromDate" to invoice.fromDate,
                "toDate" to invoice.toDate,
                "totalTransactions" to invoice.totalTransactions,
                "totalHours" to invoice.totalHours,
                "totalCharges" to invoice.totalCharges,
                "overdueCharges" to invoice.overdueCharges,
                "totalAmount" to invoice.totalAmount,
                "paymentStatus" to invoice.paymentStatus,
                "paymentDate" to invoice.paymentDate,
                "dueDate" to invoice.dueDate,
                "amountPaid" to invoice.amountPaid,
                "transactionIds" to invoice.transactionIds,
                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            db.collection("invoices").document(invoice.id).update(invoiceMap as Map<String, Any>).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getInvoice(invoiceId: String): Result<Invoice> {
        return try {
            val doc = db.collection("invoices").document(invoiceId).get().await()
            val invoice = doc.toObject(Invoice::class.java)
            if (invoice != null) {
                invoice.id = doc.id
                Result.success(invoice)
            } else {
                Result.failure(Exception("Invoice not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOwnerInvoices(ownerId: String): Result<List<Invoice>> {
        return try {
            val docs = db.collection("invoices")
                .whereEqualTo("ownerId", ownerId)
                .orderBy("month")
                .orderBy("year")
                .get()
                .await()
            val invoices = docs.documents.mapNotNull { doc ->
                doc.toObject(Invoice::class.java)?.apply { id = doc.id }
            }
            Result.success(invoices)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMonthlyInvoice(ownerId: String, month: Int, year: Int): Result<Invoice?> {
        return try {
            val docs = db.collection("invoices")
                .whereEqualTo("ownerId", ownerId)
                .whereEqualTo("month", month)
                .whereEqualTo("year", year)
                .get()
                .await()
            val invoice = docs.documents.mapNotNull { doc ->
                doc.toObject(Invoice::class.java)?.apply { id = doc.id }
            }.firstOrNull()
            Result.success(invoice)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPendingInvoices(ownerId: String): Result<List<Invoice>> {
        return try {
            val docs = db.collection("invoices")
                .whereEqualTo("ownerId", ownerId)
                .whereEqualTo("paymentStatus", "PENDING")
                .get()
                .await()
            val invoices = docs.documents.mapNotNull { doc ->
                doc.toObject(Invoice::class.java)?.apply { id = doc.id }
            }
            Result.success(invoices)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllInvoices(): Result<List<Invoice>> {
        return try {
            val docs = db.collection("invoices").get().await()
            val invoices = docs.documents.mapNotNull { doc ->
                doc.toObject(Invoice::class.java)?.apply { id = doc.id }
            }
            Result.success(invoices)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
