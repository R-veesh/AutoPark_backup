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
            snapshot.documents.mapNotNull { it.toObject(ParkingSpot::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateParkingSpotOccupancy(
        spotId: String,
        isOccupied: Boolean,
        vehicleNumber: String?
    ) {
        try {
            val updates = mutableMapOf<String, Any?>(
                "isOccupied" to isOccupied
            )
            
            if (isOccupied) {
                updates["occupiedSince"] = System.currentTimeMillis()
                updates["vehicleNumber"] = vehicleNumber ?: ""
            } else {
                updates["occupiedSince"] = null
                updates["vehicleNumber"] = ""
            }
            
            db.collection("parking_spots").document(spotId).update(updates).await()
        } catch (e: Exception) {
            throw e
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
                    it.toObject(ParkingSpot::class.java)
                } ?: emptyList()
                
                trySend(spots)
            }
        
        awaitClose { listener.remove() }
    }
}