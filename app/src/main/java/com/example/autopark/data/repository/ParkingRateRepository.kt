package com.example.autopark.data.repository

import com.example.autopark.data.model.ParkingRate
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ParkingRateRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    suspend fun addParkingRate(rate: ParkingRate): Result<String> {
        return try {
            val rateMap = hashMapOf(
                "parkingLotId" to rate.parkingLotId,
                "rateType" to rate.rateType,
                "pricePerHour" to rate.pricePerHour,
                "pricePerDay" to rate.pricePerDay,
                "overnightPrice" to rate.overnightPrice,
                "minChargeAmount" to rate.minChargeAmount,
                "maxChargePerDay" to rate.maxChargePerDay,
                "isActive" to rate.isActive,
                "vipMultiplier" to rate.vipMultiplier,
                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            val docRef = db.collection("parking_rates").add(rateMap).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateParkingRate(rate: ParkingRate): Result<Unit> {
        return try {
            val rateMap = hashMapOf(
                "parkingLotId" to rate.parkingLotId,
                "rateType" to rate.rateType,
                "pricePerHour" to rate.pricePerHour,
                "pricePerDay" to rate.pricePerDay,
                "overnightPrice" to rate.overnightPrice,
                "minChargeAmount" to rate.minChargeAmount,
                "maxChargePerDay" to rate.maxChargePerDay,
                "isActive" to rate.isActive,
                "vipMultiplier" to rate.vipMultiplier,
                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            db.collection("parking_rates").document(rate.id).update(rateMap as Map<String, Any>).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteParkingRate(rateId: String): Result<Unit> {
        return try {
            db.collection("parking_rates").document(rateId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getParkingRate(rateId: String): Result<ParkingRate> {
        return try {
            val doc = db.collection("parking_rates").document(rateId).get().await()
            val rate = doc.toObject(ParkingRate::class.java)
            if (rate != null) {
                rate.id = doc.id
                Result.success(rate)
            } else {
                Result.failure(Exception("Parking rate not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRatesForLot(parkingLotId: String): Result<List<ParkingRate>> {
        return try {
            val docs = db.collection("parking_rates")
                .whereEqualTo("parkingLotId", parkingLotId)
                .get()
                .await()
            val rates = docs.documents.mapNotNull { doc ->
                doc.toObject(ParkingRate::class.java)?.apply { id = doc.id }
            }
            Result.success(rates)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getActiveRatesForLot(parkingLotId: String): Result<List<ParkingRate>> {
        return try {
            val docs = db.collection("parking_rates")
                .whereEqualTo("parkingLotId", parkingLotId)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            val rates = docs.documents.mapNotNull { doc ->
                doc.toObject(ParkingRate::class.java)?.apply { id = doc.id }
            }
            Result.success(rates)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    

    suspend fun getAllParkingRates(): Result<List<ParkingRate>> {
        return try {
            val docs = db.collection("parking_rates").get().await()
            val rates = docs.documents.mapNotNull { doc ->
                doc.toObject(ParkingRate::class.java)?.apply { id = doc.id }
            }
            Result.success(rates)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRateByType(parkingLotId: String, rateType: String): Result<ParkingRate?> {
        return try {
            val docs = db.collection("parking_rates")
                .whereEqualTo("parkingLotId", parkingLotId)
                .whereEqualTo("rateType", rateType)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            val rate = docs.documents.mapNotNull { doc ->
                doc.toObject(ParkingRate::class.java)?.apply { id = doc.id }
            }.firstOrNull()
            Result.success(rate)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
