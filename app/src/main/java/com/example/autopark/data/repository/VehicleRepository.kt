package com.example.autopark.data.repository

import com.example.autopark.data.model.Vehicle
import com.example.autopark.util.TimestampUtils
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class VehicleRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    suspend fun addVehicle(vehicle: Vehicle): Result<String> {
        return try {
            val vehicleMap = hashMapOf(
                "ownerId" to vehicle.ownerId,
                "vehicleNumber" to vehicle.vehicleNumber,
                "vehicleType" to vehicle.vehicleType,
                "color" to vehicle.color,
                "brand" to vehicle.brand,
                "model" to vehicle.model,
                "parkingLicenseValid" to vehicle.parkingLicenseValid,
                "registrationExpiry" to vehicle.registrationExpiry,
                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            val docRef = db.collection("vehicles").add(vehicleMap).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateVehicle(vehicle: Vehicle): Result<Unit> {
        return try {
            val vehicleMap = hashMapOf(
                "ownerId" to vehicle.ownerId,
                "vehicleNumber" to vehicle.vehicleNumber,
                "vehicleType" to vehicle.vehicleType,
                "color" to vehicle.color,
                "brand" to vehicle.brand,
                "model" to vehicle.model,
                "parkingLicenseValid" to vehicle.parkingLicenseValid,
                "registrationExpiry" to vehicle.registrationExpiry,
                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            db.collection("vehicles").document(vehicle.id).update(vehicleMap as Map<String, Any>).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteVehicle(vehicleId: String): Result<Unit> {
        return try {
            db.collection("vehicles").document(vehicleId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVehicle(vehicleId: String): Result<Vehicle> {
        return try {
            val doc = db.collection("vehicles").document(vehicleId).get().await()
            val data = doc.data
            if (data != null) {
                val vehicle = Vehicle(
                    id = doc.id,
                    ownerId = data["ownerId"] as? String ?: "",
                    vehicleNumber = data["vehicleNumber"] as? String ?: "",
                    vehicleType = data["vehicleType"] as? String ?: "",
                    color = data["color"] as? String ?: "",
                    brand = data["brand"] as? String ?: "",
                    model = data["model"] as? String ?: "",
                    parkingLicenseValid = data["parkingLicenseValid"] as? Boolean ?: true,
                    registrationExpiry = (data["registrationExpiry"] as? Number)?.toLong() ?: 0,
                    createdAt = TimestampUtils.toMillis(data["createdAt"]),
                    updatedAt = TimestampUtils.toMillis(data["updatedAt"])
                )
                Result.success(vehicle)
            } else {
                Result.failure(Exception("Vehicle not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOwnerVehicles(ownerId: String): Result<List<Vehicle>> {
        return try {
            val docs = db.collection("vehicles")
                .whereEqualTo("ownerId", ownerId)
                .get()
                .await()
            val vehicles = docs.documents.mapNotNull { doc ->
                try {
                    val data = doc.data
                    if (data != null) {
                        Vehicle(
                            id = doc.id,
                            ownerId = data["ownerId"] as? String ?: "",
                            vehicleNumber = data["vehicleNumber"] as? String ?: "",
                            vehicleType = data["vehicleType"] as? String ?: "",
                            color = data["color"] as? String ?: "",
                            brand = data["brand"] as? String ?: "",
                            model = data["model"] as? String ?: "",
                            parkingLicenseValid = data["parkingLicenseValid"] as? Boolean ?: true,
                            registrationExpiry = (data["registrationExpiry"] as? Number)?.toLong() ?: 0,
                            createdAt = TimestampUtils.toMillis(data["createdAt"]),
                            updatedAt = TimestampUtils.toMillis(data["updatedAt"])
                        )
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
            Result.success(vehicles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getOwnerVehiclesFlow(ownerId: String): Flow<List<Vehicle>> = flow {
        try {
            val docs = db.collection("vehicles")
                .whereEqualTo("ownerId", ownerId)
                .get()
                .await()
            val vehicles = docs.documents.mapNotNull { doc ->
                try {
                    val data = doc.data
                    if (data != null) {
                        Vehicle(
                            id = doc.id,
                            ownerId = data["ownerId"] as? String ?: "",
                            vehicleNumber = data["vehicleNumber"] as? String ?: "",
                            vehicleType = data["vehicleType"] as? String ?: "",
                            color = data["color"] as? String ?: "",
                            brand = data["brand"] as? String ?: "",
                            model = data["model"] as? String ?: "",
                            parkingLicenseValid = data["parkingLicenseValid"] as? Boolean ?: true,
                            registrationExpiry = (data["registrationExpiry"] as? Number)?.toLong() ?: 0,
                            createdAt = TimestampUtils.toMillis(data["createdAt"]),
                            updatedAt = TimestampUtils.toMillis(data["updatedAt"])
                        )
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
            emit(vehicles)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    suspend fun getAllVehicles(): Result<List<Vehicle>> {
        return try {
            val docs = db.collection("vehicles").get().await()
            val vehicles = docs.documents.mapNotNull { doc ->
                try {
                    val data = doc.data
                    if (data != null) {
                        Vehicle(
                            id = doc.id,
                            ownerId = data["ownerId"] as? String ?: "",
                            vehicleNumber = data["vehicleNumber"] as? String ?: "",
                            vehicleType = data["vehicleType"] as? String ?: "",
                            color = data["color"] as? String ?: "",
                            brand = data["brand"] as? String ?: "",
                            model = data["model"] as? String ?: "",
                            parkingLicenseValid = data["parkingLicenseValid"] as? Boolean ?: true,
                            registrationExpiry = (data["registrationExpiry"] as? Number)?.toLong() ?: 0,
                            createdAt = TimestampUtils.toMillis(data["createdAt"]),
                            updatedAt = TimestampUtils.toMillis(data["updatedAt"])
                        )
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
            Result.success(vehicles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
