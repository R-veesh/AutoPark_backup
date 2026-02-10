package com.example.autopark.data.repository

import com.example.autopark.data.model.User
import com.example.autopark.util.TimestampUtils
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserManagementRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val docs = db.collection("users").get().await()
            val users = docs.documents.mapNotNull { doc ->
                try {
                    val data = doc.data
                    if (data != null) {
                        User(
                            id = doc.id,
                            email = data["email"] as? String ?: "",
                            role = data["role"] as? String ?: "",
                            name = data["name"] as? String ?: "",
                            phoneNumber = data["phoneNumber"] as? String ?: "",
                            address = data["address"] as? String ?: "",
                            city = data["city"] as? String ?: "",
                            state = data["state"] as? String ?: "",
                            zipCode = data["zipCode"] as? String ?: "",
                            licenseNumber = data["licenseNumber"] as? String ?: "",
                            licenseExpiry = (data["licenseExpiry"] as? Number)?.toLong() ?: 0,
                            profilePictureUrl = data["profilePictureUrl"] as? String,
                            parkingLicenseValid = data["parkingLicenseValid"] as? Boolean ?: true,
                            createdAt = TimestampUtils.toMillis(data["createdAt"]),
                            updatedAt = TimestampUtils.toMillis(data["updatedAt"]),
                            lastLoginAt = TimestampUtils.toMillis(data["lastLoginAt"]),
                            isVIP = data["isVIP"] as? Boolean ?: false,
                            totalSpent = (data["totalSpent"] as? Number)?.toDouble() ?: 0.0,
                            totalParkings = (data["totalParkings"] as? Number)?.toInt() ?: 0,
                            totalCharges = (data["totalCharges"] as? Number)?.toDouble() ?: 0.0,
                            loyaltyPoints = (data["loyaltyPoints"] as? Number)?.toInt() ?: 0,
                            preferredParkingLot = data["preferredParkingLot"] as? String,
                            notificationSettings = (data["notificationSettings"] as? Map<String, Boolean>) ?: mapOf(
                                "parking_reminders" to true,
                                "payment_reminders" to true,
                                "promotional_notifications" to false
                            )
                        )
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllDrivers(): Result<List<User>> {
        return try {
            val docs = db.collection("users")
                .whereEqualTo("role", "driver")
                .get()
                .await()
            val users = docs.documents.mapNotNull { doc ->
                try {
                    val data = doc.data
                    if (data != null) {
                        User(
                            id = doc.id,
                            email = data["email"] as? String ?: "",
                            role = data["role"] as? String ?: "",
                            name = data["name"] as? String ?: "",
                            phoneNumber = data["phoneNumber"] as? String ?: "",
                            address = data["address"] as? String ?: "",
                            city = data["city"] as? String ?: "",
                            state = data["state"] as? String ?: "",
                            zipCode = data["zipCode"] as? String ?: "",
                            licenseNumber = data["licenseNumber"] as? String ?: "",
                            licenseExpiry = (data["licenseExpiry"] as? Number)?.toLong() ?: 0,
                            profilePictureUrl = data["profilePictureUrl"] as? String,
                            parkingLicenseValid = data["parkingLicenseValid"] as? Boolean ?: true,
                            createdAt = TimestampUtils.toMillis(data["createdAt"]),
                            updatedAt = TimestampUtils.toMillis(data["updatedAt"]),
                            lastLoginAt = TimestampUtils.toMillis(data["lastLoginAt"]),
                            isVIP = data["isVIP"] as? Boolean ?: false,
                            totalSpent = (data["totalSpent"] as? Number)?.toDouble() ?: 0.0,
                            totalParkings = (data["totalParkings"] as? Number)?.toInt() ?: 0,
                            totalCharges = (data["totalCharges"] as? Number)?.toDouble() ?: 0.0,
                            loyaltyPoints = (data["loyaltyPoints"] as? Number)?.toInt() ?: 0,
                            preferredParkingLot = data["preferredParkingLot"] as? String,
                            notificationSettings = (data["notificationSettings"] as? Map<String, Boolean>) ?: mapOf(
                                "parking_reminders" to true,
                                "payment_reminders" to true,
                                "promotional_notifications" to false
                            )
                        )
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserById(userId: String): Result<User> {
        return try {
            val doc = db.collection("users").document(userId).get().await()
            val data = doc.data
            if (data != null) {
                val user = User(
                    id = doc.id,
                    email = data["email"] as? String ?: "",
                    role = data["role"] as? String ?: "",
                    name = data["name"] as? String ?: "",
                    phoneNumber = data["phoneNumber"] as? String ?: "",
                    address = data["address"] as? String ?: "",
                    city = data["city"] as? String ?: "",
                    state = data["state"] as? String ?: "",
                    zipCode = data["zipCode"] as? String ?: "",
                    licenseNumber = data["licenseNumber"] as? String ?: "",
                    licenseExpiry = (data["licenseExpiry"] as? Number)?.toLong() ?: 0,
                    profilePictureUrl = data["profilePictureUrl"] as? String,
                    parkingLicenseValid = data["parkingLicenseValid"] as? Boolean ?: true,
                    createdAt = TimestampUtils.toMillis(data["createdAt"]),
                    updatedAt = TimestampUtils.toMillis(data["updatedAt"]),
                    lastLoginAt = TimestampUtils.toMillis(data["lastLoginAt"]),
                    isVIP = data["isVIP"] as? Boolean ?: false,
                    totalSpent = (data["totalSpent"] as? Number)?.toDouble() ?: 0.0,
                    totalParkings = (data["totalParkings"] as? Number)?.toInt() ?: 0,
                    totalCharges = (data["totalCharges"] as? Number)?.toDouble() ?: 0.0,
                    loyaltyPoints = (data["loyaltyPoints"] as? Number)?.toInt() ?: 0,
                    preferredParkingLot = data["preferredParkingLot"] as? String,
                    notificationSettings = (data["notificationSettings"] as? Map<String, Boolean>) ?: mapOf(
                        "parking_reminders" to true,
                        "payment_reminders" to true,
                        "promotional_notifications" to false
                    )
                )
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            db.collection("users").document(user.id).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            db.collection("users").document(userId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
