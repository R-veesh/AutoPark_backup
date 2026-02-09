package com.example.autopark.data.repository

import com.example.autopark.data.model.Invoice
import com.example.autopark.data.model.OverdueCharge
import com.example.autopark.util.Constants
import com.example.autopark.util.ParkingChargeCalculator
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvoiceGenerationService @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val transactionRepository: ParkingTransactionRepository,
    private val overdueChargeRepository: OverdueChargeRepository,
    private val authRepository: AuthRepository,
    private val db: FirebaseFirestore
) {

    /**
     * Generate monthly invoices for all users
     */
    suspend fun generateMonthlyInvoices(month: Int, year: Int): Result<Int> {
        return try {
            val generatedCount = mutableListOf<String>()
            
            // Get all users
            val usersResult = authRepository.getAllUsers()
            val users = usersResult.getOrNull() ?: emptyList()
            
            for (user in users) {
                if (user.role == "driver") {
                    val result = generateUserMonthlyInvoice(user.id, month, year)
                    if (result.isSuccess) {
                        generatedCount.add(user.id)
                    }
                }
            }
            
            Result.success(generatedCount.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generate monthly invoice for a specific user
     */
    suspend fun generateUserMonthlyInvoice(userId: String, month: Int, year: Int): Result<Invoice> {
        return try {
            // Check if invoice already exists for this month
            val existingInvoiceResult = invoiceRepository.getMonthlyInvoice(userId, month, year)
            val existingInvoice = existingInvoiceResult.getOrNull()
            
            if (existingInvoice != null) {
                // Update existing invoice instead of creating new one
                val updatedInvoice = calculateInvoiceFromTransactions(existingInvoice.id, userId, month, year)
                invoiceRepository.updateInvoice(updatedInvoice)
                Result.success(updatedInvoice)
            } else {
                // Create new invoice
                val newInvoice = calculateInvoiceFromTransactions(null, userId, month, year)
                val invoiceIdResult = invoiceRepository.addInvoice(newInvoice)
                val invoiceId = invoiceIdResult.getOrThrow()
                val completedInvoice = newInvoice.copy(id = invoiceId)
                Result.success(completedInvoice)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Calculate invoice from user's monthly transactions
     */
    private suspend fun calculateInvoiceFromTransactions(
        invoiceId: String?,
        userId: String, 
        month: Int, 
        year: Int
    ): Invoice {
        
        // Get transactions for the month
        val transactionsResult = transactionRepository.getMonthlyTransactions(userId, month, year)
        val transactions = transactionsResult.getOrNull() ?: emptyList()
        
        // Get user details
        val userResult = authRepository.getUserData(userId)
        val user = userResult.getOrNull()
        
        // Calculate month boundaries
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1, 0, 0, 0)
        val fromDate = calendar.timeInMillis
        
        calendar.set(year, month - 1, calendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
        val toDate = calendar.timeInMillis
        
        // Calculate invoice totals
        var totalCharges = 0.0
        var totalHours = 0L
        val transactionIds = mutableListOf<String>()
        var completedTransactions = 0
        
        transactions.forEach { transaction ->
            if (transaction.status == "COMPLETED") {
                totalCharges += transaction.chargeAmount
                totalHours += transaction.duration
                completedTransactions++
            }
            transactionIds.add(transaction.id)
        }
        
        // Generate invoice number
        val invoiceNumber = generateInvoiceNumber(month, year, user?.name ?: "")
        
        // Calculate due date (15 days from end of month)
        val dueDate = toDate + (15L * 24 * 60 * 60 * 1000)
        
        return Invoice(
            id = invoiceId ?: "",
            ownerId = userId,
            invoiceNumber = invoiceNumber,
            month = month,
            year = year,
            fromDate = fromDate,
            toDate = toDate,
            totalTransactions = transactions.size,
            totalHours = totalHours,
            totalCharges = totalCharges,
            overdueCharges = 0.0, // Will be calculated separately
            totalAmount = totalCharges,
            paymentStatus = if (completedTransactions > 0) "PENDING" else "PAID",
            dueDate = dueDate,
            amountPaid = 0.0,
            transactionIds = transactionIds
        )
    }

    /**
     * Generate unique invoice number
     */
    private fun generateInvoiceNumber(month: Int, year: Int, userName: String): String {
        val userInitials = userName.split(" ").take(2).joinToString("") { it.first().uppercase() }
        val monthStr = String.format("%02d", month)
        return "INV-$userInitials-$monthStr$year-${System.currentTimeMillis() % 1000}"
    }

    /**
     * Check for overdue invoices and generate overdue charges
     */
    suspend fun processOverdueInvoices(): Result<Int> {
        return try {
            val overdueCount = mutableListOf<String>()
            val currentTime = System.currentTimeMillis()
            
            // Get all users
            val usersResult = authRepository.getAllUsers()
            val users = usersResult.getOrNull() ?: emptyList()
            
            for (user in users) {
                if (user.role == "driver") {
                    val result = processUserOverdueInvoices(user.id, currentTime)
                    if (result.isSuccess) {
                        overdueCount.add(user.id)
                    }
                }
            }
            
            Result.success(overdueCount.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Process overdue invoices for a specific user
     */
    private suspend fun processUserOverdueInvoices(userId: String, currentTime: Long): Result<List<OverdueCharge>> {
        return try {
            val pendingInvoicesResult = invoiceRepository.getPendingInvoices(userId)
            val pendingInvoices = pendingInvoicesResult.getOrNull() ?: emptyList()
            
            val overdueCharges = mutableListOf<OverdueCharge>()
            
            pendingInvoices.forEach { invoice ->
                if (currentTime > invoice.dueDate && invoice.totalAmount > invoice.amountPaid) {
                    val overdueDays = ((currentTime - invoice.dueDate) / (24 * 60 * 60 * 1000)).toInt()
                    
                    if (overdueDays > 0) {
                        // Check if overdue charge already exists
                        val existingChargesResult = overdueChargeRepository.getChargesByInvoice(invoice.id)
                        val existingCharges = existingChargesResult.getOrNull() ?: emptyList()
                        
                        if (existingCharges.isEmpty()) {
                            val overdueCharge = OverdueCharge(
                                ownerId = userId,
                                ownerName = "", // Will be populated from user data
                                invoiceId = invoice.id,
                                invoiceNumber = invoice.invoiceNumber,
                                originalAmount = invoice.totalAmount,
                                lateFeePercentage = Constants.LATE_FEE_PERCENTAGE,
                                lateFeeAmount = ParkingChargeCalculator.calculateOverdueCharge(
                                    invoice.totalAmount, 
                                    overdueDays, 
                                    Constants.LATE_FEE_PERCENTAGE
                                ),
                                totalAmount = invoice.totalAmount + ParkingChargeCalculator.calculateOverdueCharge(
                                    invoice.totalAmount, 
                                    overdueDays, 
                                    Constants.LATE_FEE_PERCENTAGE
                                ),
                                totalDueAmount = invoice.totalAmount - invoice.amountPaid,
                                overdueDays = overdueDays,
                                daysOverdue = overdueDays,
                                dueDate = invoice.dueDate,
                                status = "PENDING",
                                paymentStatus = "PENDING"
                            )
                            
                            val chargeIdResult = overdueChargeRepository.addOverdueCharge(overdueCharge)
                            if (chargeIdResult.isSuccess) {
                                overdueCharges.add(overdueCharge)
                            }
                        }
                    }
                }
            }
            
            Result.success(overdueCharges)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}