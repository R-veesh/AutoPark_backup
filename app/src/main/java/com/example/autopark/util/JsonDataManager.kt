package com.example.autopark.util

import android.content.Context
import android.net.Uri
import com.example.autopark.data.model.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.*
import java.util.Date

data class ParkingDataExport(
    val users: List<User>,
    val vehicles: List<Vehicle>,
    val parkingLots: List<ParkingLot>,
    val parkingRates: List<ParkingRate>,
    val transactions: List<ParkingTransaction>,
    val invoices: List<Invoice>,
    val overdueCharges: List<OverdueCharge>,
    val exportDate: Long = System.currentTimeMillis()
)

class JsonDataManager(private val context: Context) {
    
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()
    
    fun exportAllData(
        users: List<User>,
        vehicles: List<Vehicle>,
        parkingLots: List<ParkingLot>,
        parkingRates: List<ParkingRate>,
        transactions: List<ParkingTransaction>,
        invoices: List<Invoice>,
        overdueCharges: List<OverdueCharge>,
        outputUri: Uri
    ): Result<Unit> {
        return try {
            val exportData = ParkingDataExport(
                users = users,
                vehicles = vehicles,
                parkingLots = parkingLots,
                parkingRates = parkingRates,
                transactions = transactions,
                invoices = invoices,
                overdueCharges = overdueCharges
            )
            
            val jsonString = gson.toJson(exportData)
            
            context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(jsonString)
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun importData(inputUri: Uri): Result<ParkingDataExport> {
        return try {
            val jsonString = context.contentResolver.openInputStream(inputUri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            } ?: throw IOException("Could not read file")
            
            val type = object : TypeToken<ParkingDataExport>() {}.type
            val data = gson.fromJson<ParkingDataExport>(jsonString, type)
            
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun generateSampleData(): ParkingDataExport {
        val sampleUsers = listOf(
            User(
                id = "user1",
                email = "admin@autopark.com",
                role = "admin",
                name = "Admin User",
                phoneNumber = "1234567890",
                isVIP = false
            ),
            User(
                id = "user2",
                email = "john@example.com",
                role = "driver",
                name = "John Doe",
                phoneNumber = "0987654321",
                isVIP = true,
                totalParkings = 15,
                totalCharges = 450.00
            ),
            User(
                id = "user3",
                email = "jane@example.com",
                role = "driver",
                name = "Jane Smith",
                phoneNumber = "5555555555",
                isVIP = false,
                totalParkings = 8,
                totalCharges = 200.00
            )
        )
        
        val sampleVehicles = listOf(
            Vehicle(
                id = "veh1",
                ownerId = "user2",
                vehicleNumber = "ABC123",
                vehicleType = "Car",
                color = "Blue",
                brand = "Toyota",
                model = "Camry",
                parkingLicenseValid = true
            ),
            Vehicle(
                id = "veh2",
                ownerId = "user2",
                vehicleNumber = "XYZ789",
                vehicleType = "Bike",
                color = "Red",
                brand = "Honda",
                model = "CBR",
                parkingLicenseValid = true
            ),
            Vehicle(
                id = "veh3",
                ownerId = "user3",
                vehicleNumber = "DEF456",
                vehicleType = "Car",
                color = "Black",
                brand = "BMW",
                model = "X5",
                parkingLicenseValid = true
            )
        )
        
        val sampleLots = listOf(
            ParkingLot(
                id = "lot1",
                name = "Downtown Parking",
                address = "123 Main St",
                latitude = 40.7128,
                longitude = -74.0060,
                city = "New York",
                state = "NY",
                zipCode = "10001",
                totalSpots = 100,
                availableSpots = 45,
                description = "Prime downtown location with 24/7 security",
                contactNumber = "555-0100",
                is24Hours = true
            ),
            ParkingLot(
                id = "lot2",
                name = "Shopping Mall Parking",
                address = "456 Market Ave",
                latitude = 40.7589,
                longitude = -73.9851,
                city = "New York",
                state = "NY",
                zipCode = "10019",
                totalSpots = 200,
                availableSpots = 120,
                description = "Convenient mall parking with EV charging",
                contactNumber = "555-0200",
                openingTime = "08:00",
                closingTime = "22:00",
                is24Hours = false
            )
        )
        
        val sampleRates = listOf(
            ParkingRate(
                id = "rate1",
                parkingLotId = "lot1",
                rateType = "NORMAL",
                pricePerHour = 5.00,
                pricePerDay = 30.00,
                overnightPrice = 25.00,
                minChargeAmount = 5.00,
                maxChargePerDay = 50.00,
                vipMultiplier = 1.0
            ),
            ParkingRate(
                id = "rate2",
                parkingLotId = "lot1",
                rateType = "VIP",
                pricePerHour = 3.50,
                pricePerDay = 20.00,
                overnightPrice = 15.00,
                minChargeAmount = 3.50,
                maxChargePerDay = 35.00,
                vipMultiplier = 0.7
            ),
            ParkingRate(
                id = "rate3",
                parkingLotId = "lot2",
                rateType = "NORMAL",
                pricePerHour = 3.00,
                pricePerDay = 25.00,
                overnightPrice = 20.00,
                minChargeAmount = 3.00,
                maxChargePerDay = 40.00,
                vipMultiplier = 1.0
            )
        )
        
        val currentTime = System.currentTimeMillis()
        val sampleTransactions = listOf(
            ParkingTransaction(
                id = "trans1",
                parkingLotId = "lot1",
                vehicleId = "veh1",
                ownerId = "user2",
                vehicleNumber = "ABC123",
                entryTime = currentTime - (24 * 60 * 60 * 1000), // Yesterday
                exitTime = currentTime - (20 * 60 * 60 * 1000),
                duration = 240, // 4 hours
                rateType = "VIP",
                chargeAmount = 14.00,
                status = "COMPLETED",
                paymentMethod = "CARD",
                paymentStatus = "COMPLETED"
            ),
            ParkingTransaction(
                id = "trans2",
                parkingLotId = "lot2",
                vehicleId = "veh3",
                ownerId = "user3",
                vehicleNumber = "DEF456",
                entryTime = currentTime - (2 * 60 * 60 * 1000), // 2 hours ago
                exitTime = null,
                duration = 120,
                rateType = "NORMAL",
                chargeAmount = 6.00,
                status = "ACTIVE",
                paymentMethod = "",
                paymentStatus = "PENDING"
            )
        )
        
        val sampleInvoices = listOf(
            Invoice(
                id = "inv1",
                ownerId = "user2",
                invoiceNumber = "INV-2024-001",
                month = 1,
                year = 2024,
                totalAmount = 150.00,
                totalTransactions = 5,
                paymentStatus = "PAID",
                dueDate = currentTime + (7 * 24 * 60 * 60 * 1000)
            ),
            Invoice(
                id = "inv2",
                ownerId = "user3",
                invoiceNumber = "INV-2024-002",
                month = 1,
                year = 2024,
                totalAmount = 75.00,
                totalTransactions = 3,
                paymentStatus = "PENDING",
                dueDate = currentTime - (5 * 24 * 60 * 60 * 1000) // Overdue
            )
        )
        
        val sampleOverdue = listOf(
            OverdueCharge(
                id = "overdue1",
                ownerId = "user3",
                ownerName = "Jane Smith",
                invoiceId = "inv2",
                invoiceNumber = "INV-2024-002",
                originalAmount = 75.00,
                overdueDays = 5,
                daysOverdue = 5,
                lateFeeAmount = 7.50,
                totalAmount = 82.50,
                totalDueAmount = 82.50,
                dueDate = currentTime - (5 * 24 * 60 * 60 * 1000),
                status = "PENDING",
                paymentStatus = "PENDING"
            )
        )
        
        return ParkingDataExport(
            users = sampleUsers,
            vehicles = sampleVehicles,
            parkingLots = sampleLots,
            parkingRates = sampleRates,
            transactions = sampleTransactions,
            invoices = sampleInvoices,
            overdueCharges = sampleOverdue
        )
    }
    
    fun exportSampleDataToFile(outputUri: Uri): Result<Unit> {
        return try {
            val sampleData = generateSampleData()
            val jsonString = gson.toJson(sampleData)
            
            context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(jsonString)
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
