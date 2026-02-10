package com.example.autopark.data.repository

import android.util.Log
import com.example.autopark.data.model.ParkingRate
import com.example.autopark.util.TimestampUtils
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ParkingRateRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    private fun parseParkingRateFromDocument(docId: String, data: Map<String, Any?>?): ParkingRate? {
        if (data == null) return null
        return try {
            ParkingRate(
                id = docId,
                parkingLotId = data["parkingLotId"] as? String ?: "",
                rateType = data["rateType"] as? String ?: "",
                pricePerHour = (data["pricePerHour"] as? Number)?.toDouble() ?: 0.0,
                pricePerDay = (data["pricePerDay"] as? Number)?.toDouble() ?: 0.0,
                overnightPrice = (data["overnightPrice"] as? Number)?.toDouble() ?: 0.0,
                minChargeAmount = (data["minChargeAmount"] as? Number)?.toDouble() ?: 0.0,
                maxChargePerDay = (data["maxChargePerDay"] as? Number)?.toDouble() ?: 0.0,
                isActive = data["isActive"] as? Boolean ?: true,
                vipMultiplier = (data["vipMultiplier"] as? Number)?.toDouble() ?: 1.0,
                createdAt = TimestampUtils.toMillis(data["createdAt"]),
                updatedAt = TimestampUtils.toMillis(data["updatedAt"])
            )
        } catch (e: Exception) {
            Log.e("ParkingRateRepo", "Error parsing parking rate: ${e.message}")
            null
        }
    }

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
            val data = doc.data
            if (data != null) {
                val rate = parseParkingRateFromDocument(doc.id, data)
                if (rate != null) {
                    Result.success(rate)
                } else {
                    Result.failure(Exception("Failed to parse parking rate"))
                }
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
                parseParkingRateFromDocument(doc.id, doc.data)
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
                parseParkingRateFromDocument(doc.id, doc.data)
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
                parseParkingRateFromDocument(doc.id, doc.data)
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
                parseParkingRateFromDocument(doc.id, doc.data)
            }.firstOrNull()
            Result.success(rate)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
