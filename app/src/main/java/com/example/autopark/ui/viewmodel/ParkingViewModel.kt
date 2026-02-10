package com.example.autopark.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopark.auth.AuthManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ParkingViewModel : ViewModel() {

    private val TAG = "ParkingViewModel"
    private val db = FirebaseFirestore.getInstance()

    /** Confirm vehicle entry by admin */
    fun confirmVehicle(vehicleNumber: String, vehicleId: String) {
        val user = AuthManager.currentUser()

        if (user == null) {
            Log.e(TAG, "User not authenticated")
            return
        }

        val data = hashMapOf(
            "vehicleNumber" to vehicleNumber,
            "vehicleId" to vehicleId,
            "adminId" to user.uid,
            "status" to "IN",
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("active_parking")
            .document(vehicleId)
            .set(data)
            .addOnSuccessListener {
                Log.d(TAG, "Vehicle added successfully: $vehicleNumber")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to add vehicle: ${e.message}")
            }
    }

    /** Process QR scan for vehicle entry/exit */
    fun processQr(vehicleId: String, vehicleNumber: String) {
        viewModelScope.launch {
            checkActiveParking(vehicleId, vehicleNumber)
        }
    }

    /** Check if vehicle already has active parking */
    private fun checkActiveParking(vehicleId: String, vehicleNumber: String) {
        db.collection("parkingTransactions")
            .whereEqualTo("vehicleId", vehicleId)
            .whereEqualTo("status", "ACTIVE")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    Log.d(TAG, "No active parking found. Creating new entry for $vehicleNumber")
                    createEntry(vehicleId, vehicleNumber)
                } else {
                    Log.d(TAG, "Active parking found. Exiting vehicle $vehicleNumber")
                    exitParking(snapshot.documents[0].id)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error checking active parking: ${e.message}")
            }
    }

    /** Create a new parking entry */
    private fun createEntry(vehicleId: String, vehicleNumber: String) {
        val data = hashMapOf(
            "vehicleId" to vehicleId,
            "vehicleNumber" to vehicleNumber,
            "entryTime" to Timestamp.now(),
            "exitTime" to null,
            "status" to "ACTIVE"
        )

        db.collection("parkingTransactions")
            .add(data)
            .addOnSuccessListener {
                Log.d(TAG, "Parking entry created for $vehicleNumber")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to create parking entry: ${e.message}")
            }
    }

    /** Exit vehicle from parking */
    private fun exitParking(transactionId: String) {
        db.collection("parkingTransactions")
            .document(transactionId)
            .update(
                mapOf(
                    "exitTime" to Timestamp.now(),
                    "status" to "COMPLETED"
                )
            )
            .addOnSuccessListener {
                Log.d(TAG, "Vehicle exited successfully: $transactionId")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to exit parking: ${e.message}")
            }
    }
}
