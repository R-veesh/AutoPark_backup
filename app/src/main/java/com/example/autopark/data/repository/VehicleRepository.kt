package com.example.autopark.data.repository

import com.example.autopark.data.model.Vehicle
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
            // Create a map to ensure all fields are properly set
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
            val vehicle = doc.toObject(Vehicle::class.java)
            if (vehicle != null) {
                vehicle.id = doc.id
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
                doc.toObject(Vehicle::class.java)?.apply { id = doc.id }
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
                doc.toObject(Vehicle::class.java)?.apply { id = doc.id }
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
                doc.toObject(Vehicle::class.java)?.apply { id = doc.id }
            }
            Result.success(vehicles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
