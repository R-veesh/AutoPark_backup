package com.example.autopark.data.repository

import com.example.autopark.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataImportExportRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val gson: Gson
) {

    data class ExportData(
        val users: List<User>,
        val vehicles: List<Vehicle>,
        val parkingLots: List<ParkingLot>,
        val parkingRates: List<ParkingRate>,
        val parkingTransactions: List<ParkingTransaction>,
        val invoices: List<Invoice>,
        val overdueCharges: List<OverdueCharge>
    )

    /**
     * Export all data as JSON
     */
    suspend fun exportAllData(): Result<String> {
        return try {
            withContext(Dispatchers.IO) {
                // Get all collections
                val users = db.collection("users").get().await()
                    .documents.mapNotNull { doc ->
                        doc.toObject(User::class.java)?.apply { id = doc.id }
                    }
                
                val vehicles = db.collection("vehicles").get().await()
                    .documents.mapNotNull { doc ->
                        doc.toObject(Vehicle::class.java)?.apply { id = doc.id }
                    }
                
                val parkingLots = db.collection("parking_lots").get().await()
                    .documents.mapNotNull { doc ->
                        doc.toObject(ParkingLot::class.java)?.apply { id = doc.id }
                    }
                
                val parkingRates = db.collection("parking_rates").get().await()
                    .documents.mapNotNull { doc ->
                        doc.toObject(ParkingRate::class.java)?.apply { id = doc.id }
                    }
                
                val parkingTransactions = db.collection("parking_transactions").get().await()
                    .documents.mapNotNull { doc ->
                        doc.toObject(ParkingTransaction::class.java)?.apply { id = doc.id }
                    }
                
                val invoices = db.collection("invoices").get().await()
                    .documents.mapNotNull { doc ->
                        doc.toObject(Invoice::class.java)?.apply { id = doc.id }
                    }
                
                val overdueCharges = db.collection("overdue_charges").get().await()
                    .documents.mapNotNull { doc ->
                        doc.toObject(OverdueCharge::class.java)?.apply { id = doc.id }
                    }

                val exportData = ExportData(
                    users = users,
                    vehicles = vehicles,
                    parkingLots = parkingLots,
                    parkingRates = parkingRates,
                    parkingTransactions = parkingTransactions,
                    invoices = invoices,
                    overdueCharges = overdueCharges
                )

                val json = gson.toJson(exportData)
                Result.success(json)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Import data from JSON
     */
    suspend fun importData(jsonData: String): Result<ImportResult> {
        return try {
            withContext(Dispatchers.IO) {
                val exportData = gson.fromJson(jsonData, ExportData::class.java)
                var successCount = 0
                var errorCount = 0
                val errors = mutableListOf<String>()

                // Import users
                exportData.users.forEach { user ->
                    try {
                        // Check if user already exists
                        val existingUser = db.collection("users")
                            .whereEqualTo("email", user.email)
                            .get()
                            .await()
                            .documents.firstOrNull()

                        if (existingUser == null) {
                            db.collection("users").add(user).await()
                            successCount++
                        } else {
                            errors.add("User with email ${user.email} already exists")
                            errorCount++
                        }
                    } catch (e: Exception) {
                        errors.add("Failed to import user ${user.email}: ${e.message}")
                        errorCount++
                    }
                }

                // Import parking lots
                exportData.parkingLots.forEach { lot ->
                    try {
                        db.collection("parking_lots").add(lot).await()
                        successCount++
                    } catch (e: Exception) {
                        errors.add("Failed to import parking lot ${lot.name}: ${e.message}")
                        errorCount++
                    }
                }

                // Import parking rates
                exportData.parkingRates.forEach { rate ->
                    try {
                        db.collection("parking_rates").add(rate).await()
                        successCount++
                    } catch (e: Exception) {
                        errors.add("Failed to import parking rate: ${e.message}")
                        errorCount++
                    }
                }

                // Import vehicles
                exportData.vehicles.forEach { vehicle ->
                    try {
                        db.collection("vehicles").add(vehicle).await()
                        successCount++
                    } catch (e: Exception) {
                        errors.add("Failed to import vehicle ${vehicle.vehicleNumber}: ${e.message}")
                        errorCount++
                    }
                }

                // Import transactions
                exportData.parkingTransactions.forEach { transaction ->
                    try {
                        db.collection("parking_transactions").add(transaction).await()
                        successCount++
                    } catch (e: Exception) {
                        errors.add("Failed to import transaction: ${e.message}")
                        errorCount++
                    }
                }

                // Import invoices
                exportData.invoices.forEach { invoice ->
                    try {
                        db.collection("invoices").add(invoice).await()
                        successCount++
                    } catch (e: Exception) {
                        errors.add("Failed to import invoice ${invoice.invoiceNumber}: ${e.message}")
                        errorCount++
                    }
                }

                // Import overdue charges
                exportData.overdueCharges.forEach { charge ->
                    try {
                        db.collection("overdue_charges").add(charge).await()
                        successCount++
                    } catch (e: Exception) {
                        errors.add("Failed to import overdue charge: ${e.message}")
                        errorCount++
                    }
                }

                Result.success(ImportResult(successCount, errorCount, errors))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Export summary data for admin dashboard
     */
    suspend fun exportSummaryData(): Result<String> {
        return try {
            withContext(Dispatchers.IO) {
                val calendar = java.util.Calendar.getInstance()
                val currentMonth = calendar.get(java.util.Calendar.MONTH) + 1
                val currentYear = calendar.get(java.util.Calendar.YEAR)

                // Get current month transactions
                val transactions = db.collection("parking_transactions")
                    .whereGreaterThanOrEqualTo("entryTime", getMonthStart(currentMonth, currentYear))
                    .whereLessThan("entryTime", getMonthEnd(currentMonth, currentYear))
                    .get()
                    .await()
                    .documents.mapNotNull { doc ->
                        doc.toObject(ParkingTransaction::class.java)?.apply { id = doc.id }
                    }

                // Get active vehicles count
                val activeVehicles = db.collection("vehicles")
                    .whereEqualTo("parkingLicenseValid", true)
                    .get()
                    .await()
                    .documents.size

                // Get total users count
                val totalUsers = db.collection("users")
                    .whereEqualTo("role", "driver")
                    .get()
                    .await()
                    .documents.size

                // Get parking lots with availability
                val parkingLots = db.collection("parking_lots").get().await()
                    .documents.mapNotNull { doc ->
                        doc.toObject(ParkingLot::class.java)?.apply { id = doc.id }
                    }

                val summary = mapOf(
                    "exportDate" to System.currentTimeMillis(),
                    "month" to currentMonth,
                    "year" to currentYear,
                    "totalTransactions" to transactions.size,
                    "totalRevenue" to transactions.filter { it.chargeAmount > 0 }.sumOf { it.chargeAmount },
                    "activeVehicles" to activeVehicles,
                    "totalUsers" to totalUsers,
                    "totalParkingLots" to parkingLots.size,
                    "totalParkingSpots" to parkingLots.sumOf { it.totalSpots },
                    "availableParkingSpots" to parkingLots.sumOf { it.availableSpots },
                    "parkingLots" to parkingLots.map { 
                        mapOf(
                            "name" to it.name,
                            "totalSpots" to it.totalSpots,
                            "availableSpots" to it.availableSpots,
                            "occupancyRate" to if (it.totalSpots > 0) {
                                ((it.totalSpots - it.availableSpots).toDouble() / it.totalSpots * 100).toInt()
                            } else 0
                        )
                    }
                )

                val json = gson.toJson(summary)
                Result.success(json)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    data class ImportResult(
        val successCount: Int,
        val errorCount: Int,
        val errors: List<String>
    )

    private fun getMonthStart(month: Int, year: Int): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(year, month - 1, 1, 0, 0, 0)
        return calendar.timeInMillis
    }

    private fun getMonthEnd(month: Int, year: Int): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(year, month, 1, 0, 0, 0)
        return calendar.timeInMillis
    }
}