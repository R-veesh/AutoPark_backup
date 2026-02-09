package com.example.autopark.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class DailyReport(
    val date: Long = 0,
    val totalParkings: Int = 0,
    val totalRevenue: Double = 0.0,
    val averageParking: Double = 0.0,
    val peakHours: String = ""
)

data class MonthlyReport(
    val month: Int = 0,
    val year: Int = 0,
    val totalParkings: Int = 0,
    val totalRevenue: Double = 0.0,
    val totalOwners: Int = 0,
    val totalVehicles: Int = 0,
    val averageChargePerParking: Double = 0.0
)

class ReportRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val transactionRepository: ParkingTransactionRepository
) {
    suspend fun generateMonthlyReport(month: Int, year: Int): Result<MonthlyReport> {
        return try {
            // Get all transactions for the month
            val calendar = java.util.Calendar.getInstance()
            calendar.set(year, month - 1, 1, 0, 0, 0)
            val startOfMonth = calendar.timeInMillis
            calendar.set(year, month, 1, 0, 0, 0)
            val startOfNextMonth = calendar.timeInMillis

            val snapshot = db.collection("parking_transactions")
                .whereGreaterThanOrEqualTo("entryTime", startOfMonth)
                .whereLessThan("entryTime", startOfNextMonth)
                .get()
                .await()

            val transactions = snapshot.documents.size
            var totalRevenue = 0.0
            val owners = mutableSetOf<String>()

            snapshot.documents.forEach { doc ->
                val charge = doc.getDouble("chargeAmount") ?: 0.0
                val ownerId = doc.getString("ownerId") ?: ""
                totalRevenue += charge
                owners.add(ownerId)
            }

            val avgCharge = if (transactions > 0) totalRevenue / transactions else 0.0

            // Count total vehicles
            val vehicleSnapshot = db.collection("vehicles").get().await()
            val totalVehicles = vehicleSnapshot.documents.size

            Result.success(
                MonthlyReport(
                    month = month,
                    year = year,
                    totalParkings = transactions,
                    totalRevenue = totalRevenue,
                    totalOwners = owners.size,
                    totalVehicles = totalVehicles,
                    averageChargePerParking = avgCharge
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateLotMonthlyReport(lotId: String, month: Int, year: Int): Result<MonthlyReport> {
        return try {
            val calendar = java.util.Calendar.getInstance()
            calendar.set(year, month - 1, 1, 0, 0, 0)
            val startOfMonth = calendar.timeInMillis
            calendar.set(year, month, 1, 0, 0, 0)
            val startOfNextMonth = calendar.timeInMillis

            val snapshot = db.collection("parking_transactions")
                .whereEqualTo("parkingLotId", lotId)
                .whereGreaterThanOrEqualTo("entryTime", startOfMonth)
                .whereLessThan("entryTime", startOfNextMonth)
                .get()
                .await()

            val transactions = snapshot.documents.size
            var totalRevenue = 0.0
            val owners = mutableSetOf<String>()
            val vehicles = mutableSetOf<String>()

            snapshot.documents.forEach { doc ->
                val charge = doc.getDouble("chargeAmount") ?: 0.0
                val ownerId = doc.getString("ownerId") ?: ""
                val vehicleId = doc.getString("vehicleId") ?: ""
                totalRevenue += charge
                owners.add(ownerId)
                vehicles.add(vehicleId)
            }

            val avgCharge = if (transactions > 0) totalRevenue / transactions else 0.0

            Result.success(
                MonthlyReport(
                    month = month,
                    year = year,
                    totalParkings = transactions,
                    totalRevenue = totalRevenue,
                    totalOwners = owners.size,
                    totalVehicles = vehicles.size,
                    averageChargePerParking = avgCharge
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRevenueStats(): Result<Map<String, Double>> {
        return try {
            val snapshot = db.collection("parking_transactions").get().await()
            
            var totalRevenue = 0.0
            var normalRevenue = 0.0
            var vipRevenue = 0.0
            
            snapshot.documents.forEach { doc ->
                val charge = doc.getDouble("chargeAmount") ?: 0.0
                val rateType = doc.getString("rateType") ?: ""
                
                totalRevenue += charge
                when (rateType) {
                    "NORMAL" -> normalRevenue += charge
                    "VIP" -> vipRevenue += charge
                }
            }
            
            Result.success(
                mapOf(
                    "total" to totalRevenue,
                    "normal" to normalRevenue,
                    "vip" to vipRevenue
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
