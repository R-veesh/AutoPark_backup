package com.example.autopark.data

import android.content.Context
import com.example.autopark.data.model.ParkingLot
import com.example.autopark.data.model.ParkingRate
import com.example.autopark.data.repository.ParkingLotRepository
import com.example.autopark.data.repository.ParkingRateRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SampleDataInitializer {
    companion object {
        suspend fun initializeSampleData(
            db: FirebaseFirestore
        ) {
            try {
                // Check if parking lots already exist
                val lotsSnapshot = db.collection("parking_lots").get().await()
                if (lotsSnapshot.documents.isNotEmpty()) {
                    // Data already exists
                    return
                }

                val sampleParkingLots = listOf(
                    ParkingLot(
                        name = "City Center Parking",
                        address = "123 Main Street, Downtown",
                        city = "New York",
                        state = "NY",
                        zipCode = "10001",
                        totalSpots = 150,
                        availableSpots = 120,
                        latitude = 40.7128,
                        longitude = -74.0060,
                        contactNumber = "+1234567890",
                        is24Hours = true,
                        description = "Premium parking facility in city center"
                    ),
                    ParkingLot(
                        name = "Airport Parking",
                        address = "JFK International Airport",
                        city = "New York",
                        state = "NY",
                        zipCode = "11430",
                        totalSpots = 500,
                        availableSpots = 350,
                        latitude = 40.6413,
                        longitude = -73.7781,
                        contactNumber = "+1234567891",
                        is24Hours = true,
                        openingTime = "00:00",
                        closingTime = "23:59",
                        description = "Convenient airport parking"
                    ),
                    ParkingLot(
                        name = "Mall Parking",
                        address = "456 Shopping Ave",
                        city = "New York",
                        state = "NY",
                        zipCode = "10002",
                        totalSpots = 200,
                        availableSpots = 180,
                        latitude = 40.7200,
                        longitude = -73.9950,
                        contactNumber = "+1234567892",
                        is24Hours = false,
                        openingTime = "06:00",
                        closingTime = "23:00",
                        description = "Shopping mall parking with extended hours"
                    )
                )

                // Add parking lots
                for (lot in sampleParkingLots) {
                    db.collection("parking_lots").add(lot).await()
                }

                // Get the added lot IDs and add rates
                val lotsSnapshot2 = db.collection("parking_lots").get().await()
                val lots = lotsSnapshot2.documents.mapNotNull { doc ->
                    val lot = doc.toObject(ParkingLot::class.java)
                    if (lot != null) doc.id to lot else null
                }

                // Add sample rates for each parking lot
                for ((lotId, lot) in lots) {
                    val rates = listOf(
                        ParkingRate(
                            parkingLotId = lotId,
                            rateType = "NORMAL",
                            pricePerHour = 5.0,
                            pricePerDay = 40.0,
                            overnightPrice = 25.0,
                            minChargeAmount = 5.0,
                            maxChargePerDay = 100.0,
                            isActive = true,
                            vipMultiplier = 1.0
                        ),
                        ParkingRate(
                            parkingLotId = lotId,
                            rateType = "VIP",
                            pricePerHour = 10.0,
                            pricePerDay = 75.0,
                            overnightPrice = 50.0,
                            minChargeAmount = 10.0,
                            maxChargePerDay = 150.0,
                            isActive = true,
                            vipMultiplier = 1.5
                        ),
                        ParkingRate(
                            parkingLotId = lotId,
                            rateType = "HOURLY",
                            pricePerHour = 5.0,
                            pricePerDay = 40.0,
                            overnightPrice = 25.0,
                            minChargeAmount = 5.0,
                            maxChargePerDay = 100.0,
                            isActive = true
                        ),
                        ParkingRate(
                            parkingLotId = lotId,
                            rateType = "OVERNIGHT",
                            pricePerHour = 0.0,
                            pricePerDay = 0.0,
                            overnightPrice = 20.0,
                            minChargeAmount = 20.0,
                            maxChargePerDay = 50.0,
                            isActive = true
                        )
                    )

                    for (rate in rates) {
                        db.collection("parking_rates").add(rate).await()
                    }
                }
            } catch (e: Exception) {
                // Log error but don't crash the app
                android.util.Log.e("SampleDataInit", "Error initializing sample data", e)
            }
        }
    }
}
