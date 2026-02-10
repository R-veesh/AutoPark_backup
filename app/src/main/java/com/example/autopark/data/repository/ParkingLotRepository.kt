package com.example.autopark.data.repository

import com.example.autopark.data.model.ParkingLot
import com.example.autopark.util.TimestampUtils
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ParkingLotRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    suspend fun addParkingLot(lot: ParkingLot): Result<String> {
        return try {
            val lotMap = hashMapOf(
                "name" to lot.name,
                "address" to lot.address,
                "latitude" to lot.latitude,
                "longitude" to lot.longitude,
                "city" to lot.city,
                "state" to lot.state,
                "zipCode" to lot.zipCode,
                "totalSpots" to lot.totalSpots,
                "availableSpots" to lot.availableSpots,
                "description" to lot.description,
                "contactNumber" to lot.contactNumber,
                "openingTime" to lot.openingTime,
                "closingTime" to lot.closingTime,
                "is24Hours" to lot.is24Hours,
                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            val docRef = db.collection("parking_lots").add(lotMap).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateParkingLot(lot: ParkingLot): Result<Unit> {
        return try {
            val lotMap = hashMapOf(
                "name" to lot.name,
                "address" to lot.address,
                "latitude" to lot.latitude,
                "longitude" to lot.longitude,
                "city" to lot.city,
                "state" to lot.state,
                "zipCode" to lot.zipCode,
                "totalSpots" to lot.totalSpots,
                "availableSpots" to lot.availableSpots,
                "description" to lot.description,
                "contactNumber" to lot.contactNumber,
                "openingTime" to lot.openingTime,
                "closingTime" to lot.closingTime,
                "is24Hours" to lot.is24Hours,
                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            db.collection("parking_lots").document(lot.id).update(lotMap as Map<String, Any>).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteParkingLot(lotId: String): Result<Unit> {
        return try {
            db.collection("parking_lots").document(lotId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getParkingLot(lotId: String): Result<ParkingLot> {
        return try {
            val doc = db.collection("parking_lots").document(lotId).get().await()
            val data = doc.data
            if (data != null) {
                val lot = ParkingLot(
                    id = doc.id,
                    name = data["name"] as? String ?: "",
                    address = data["address"] as? String ?: "",
                    latitude = (data["latitude"] as? Number)?.toDouble() ?: 0.0,
                    longitude = (data["longitude"] as? Number)?.toDouble() ?: 0.0,
                    city = data["city"] as? String ?: "",
                    state = data["state"] as? String ?: "",
                    zipCode = data["zipCode"] as? String ?: "",
                    totalSpots = (data["totalSpots"] as? Number)?.toInt() ?: 0,
                    availableSpots = (data["availableSpots"] as? Number)?.toInt() ?: 0,
                    description = data["description"] as? String ?: "",
                    contactNumber = data["contactNumber"] as? String ?: "",
                    openingTime = data["openingTime"] as? String ?: "",
                    closingTime = data["closingTime"] as? String ?: "",
                    is24Hours = data["is24Hours"] as? Boolean ?: false,
                    createdAt = TimestampUtils.toMillis(data["createdAt"]),
                    updatedAt = TimestampUtils.toMillis(data["updatedAt"])
                )
                Result.success(lot)
            } else {
                Result.failure(Exception("Parking lot not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllParkingLots(): Result<List<ParkingLot>> {
        return try {
            val docs = db.collection("parking_lots").get().await()
            val lots = docs.documents.mapNotNull { doc ->
                try {
                    val data = doc.data
                    if (data != null) {
                        ParkingLot(
                            id = doc.id,
                            name = data["name"] as? String ?: "",
                            address = data["address"] as? String ?: "",
                            latitude = (data["latitude"] as? Number)?.toDouble() ?: 0.0,
                            longitude = (data["longitude"] as? Number)?.toDouble() ?: 0.0,
                            city = data["city"] as? String ?: "",
                            state = data["state"] as? String ?: "",
                            zipCode = data["zipCode"] as? String ?: "",
                            totalSpots = (data["totalSpots"] as? Number)?.toInt() ?: 0,
                            availableSpots = (data["availableSpots"] as? Number)?.toInt() ?: 0,
                            description = data["description"] as? String ?: "",
                            contactNumber = data["contactNumber"] as? String ?: "",
                            openingTime = data["openingTime"] as? String ?: "",
                            closingTime = data["closingTime"] as? String ?: "",
                            is24Hours = data["is24Hours"] as? Boolean ?: false,
                            createdAt = TimestampUtils.toMillis(data["createdAt"]),
                            updatedAt = TimestampUtils.toMillis(data["updatedAt"])
                        )
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
            Result.success(lots)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getParkingLotsByCity(city: String): Result<List<ParkingLot>> {
        return try {
            val docs = db.collection("parking_lots")
                .whereEqualTo("city", city)
                .get()
                .await()
            val lots = docs.documents.mapNotNull { doc ->
                try {
                    val data = doc.data
                    if (data != null) {
                        ParkingLot(
                            id = doc.id,
                            name = data["name"] as? String ?: "",
                            address = data["address"] as? String ?: "",
                            latitude = (data["latitude"] as? Number)?.toDouble() ?: 0.0,
                            longitude = (data["longitude"] as? Number)?.toDouble() ?: 0.0,
                            city = data["city"] as? String ?: "",
                            state = data["state"] as? String ?: "",
                            zipCode = data["zipCode"] as? String ?: "",
                            totalSpots = (data["totalSpots"] as? Number)?.toInt() ?: 0,
                            availableSpots = (data["availableSpots"] as? Number)?.toInt() ?: 0,
                            description = data["description"] as? String ?: "",
                            contactNumber = data["contactNumber"] as? String ?: "",
                            openingTime = data["openingTime"] as? String ?: "",
                            closingTime = data["closingTime"] as? String ?: "",
                            is24Hours = data["is24Hours"] as? Boolean ?: false,
                            createdAt = TimestampUtils.toMillis(data["createdAt"]),
                            updatedAt = TimestampUtils.toMillis(data["updatedAt"])
                        )
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
            Result.success(lots)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAvailableSpots(lotId: String, availableSpots: Int): Result<Unit> {
        return try {
            db.collection("parking_lots").document(lotId)
                .update("availableSpots", availableSpots).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
