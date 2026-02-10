package com.example.autopark.data.repository

import com.example.autopark.data.model.ParkingSpot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ParkingRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    suspend fun getParkingSpots(): List<ParkingSpot> {
        return try {
            val snapshot = db.collection("parking_spots").get().await()
            snapshot.documents.mapNotNull { 
                it.toObject(ParkingSpot::class.java)?.apply { id = it.id }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getParkingSpotsByLotId(parkingLotId: String): List<ParkingSpot> {
        return try {
            val snapshot = db.collection("parking_spots")
                .whereEqualTo("parkingLotId", parkingLotId)
                .get()
                .await()
            snapshot.documents.mapNotNull { 
                it.toObject(ParkingSpot::class.java)?.apply { id = it.id }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getParkingSpotsByLotIdStream(parkingLotId: String): Flow<List<ParkingSpot>> = callbackFlow {
        val listener = db.collection("parking_spots")
            .whereEqualTo("parkingLotId", parkingLotId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val spots = snapshot?.documents?.mapNotNull {
                    it.toObject(ParkingSpot::class.java)?.apply { id = it.id }
                } ?: emptyList()
                
                trySend(spots)
            }
        
        awaitClose { listener.remove() }
    }

    suspend fun updateParkingSpotOccupancy(
        spotId: String,
        isOccupied: Boolean,
        occupiedBy: String?,
        vehicleNumber: String?
    ): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any?>(
                "isOccupied" to isOccupied
            )
            
            if (isOccupied) {
                updates["occupiedSince"] = System.currentTimeMillis()
                updates["occupiedBy"] = occupiedBy ?: ""
                updates["vehicleNumber"] = vehicleNumber ?: ""
            } else {
                updates["occupiedSince"] = null
                updates["occupiedBy"] = null
                updates["vehicleNumber"] = null
            }
            
            db.collection("parking_spots").document(spotId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reserveParkingSpot(
        spotId: String,
        userId: String,
        vehicleNumber: String
    ): Result<Unit> {
        return try {
            val updates = hashMapOf<String, Any>(
                "isOccupied" to true,
                "occupiedSince" to System.currentTimeMillis(),
                "occupiedBy" to userId,
                "vehicleNumber" to vehicleNumber
            )
            
            db.collection("parking_spots").document(spotId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getParkingSpotsStream(): Flow<List<ParkingSpot>> = callbackFlow {
        val listener = db.collection("parking_spots")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val spots = snapshot?.documents?.mapNotNull {
                    it.toObject(ParkingSpot::class.java)?.apply { id = it.id }
                } ?: emptyList()
                
                trySend(spots)
            }
        
        awaitClose { listener.remove() }
    }
}