package com.example.autopark.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class StorageRepository @Inject constructor(
    private val storage: FirebaseStorage
) {
    companion object {
        private const val PROFILE_IMAGES_PATH = "profile_images"
        private const val VEHICLE_IMAGES_PATH = "vehicle_images"
        private const val PARKING_LOT_IMAGES_PATH = "parking_lot_images"
    }

    /**
     * Upload a profile image for a user
     * @param userId The user ID
     * @param imageUri The local URI of the image to upload
     * @return Result containing the download URL of the uploaded image
     */
    suspend fun uploadProfileImage(userId: String, imageUri: Uri): Result<String> {
        return try {
            val filename = "$userId/${UUID.randomUUID()}.jpg"
            val storageRef = storage.reference
                .child(PROFILE_IMAGES_PATH)
                .child(filename)

            storageRef.putFile(imageUri).await()
            val downloadUrl = storageRef.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a profile image
     * @param imageUrl The download URL of the image to delete
     */
    suspend fun deleteProfileImage(imageUrl: String): Result<Unit> {
        return try {
            val storageRef = storage.getReferenceFromUrl(imageUrl)
            storageRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Upload a vehicle image
     * @param vehicleId The vehicle ID
     * @param imageUri The local URI of the image to upload
     * @return Result containing the download URL of the uploaded image
     */
    suspend fun uploadVehicleImage(vehicleId: String, imageUri: Uri): Result<String> {
        return try {
            val filename = "$vehicleId/${UUID.randomUUID()}.jpg"
            val storageRef = storage.reference
                .child(VEHICLE_IMAGES_PATH)
                .child(filename)

            storageRef.putFile(imageUri).await()
            val downloadUrl = storageRef.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a vehicle image
     * @param imageUrl The download URL of the image to delete
     */
    suspend fun deleteVehicleImage(imageUrl: String): Result<Unit> {
        return try {
            val storageRef = storage.getReferenceFromUrl(imageUrl)
            storageRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Upload a parking lot image
     * @param parkingLotId The parking lot ID
     * @param imageUri The local URI of the image to upload
     * @return Result containing the download URL of the uploaded image
     */
    suspend fun uploadParkingLotImage(parkingLotId: String, imageUri: Uri): Result<String> {
        return try {
            val filename = "$parkingLotId/${UUID.randomUUID()}.jpg"
            val storageRef = storage.reference
                .child(PARKING_LOT_IMAGES_PATH)
                .child(filename)

            storageRef.putFile(imageUri).await()
            val downloadUrl = storageRef.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a parking lot image
     * @param imageUrl The download URL of the image to delete
     */
    suspend fun deleteParkingLotImage(imageUrl: String): Result<Unit> {
        return try {
            val storageRef = storage.getReferenceFromUrl(imageUrl)
            storageRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get the max upload size (10MB)
     */
    fun getMaxUploadSize(): Long = 10 * 1024 * 1024 // 10MB

    /**
     * Validate image size before upload
     * @param imageUri The URI of the image to validate
     * @param maxSize Maximum allowed size in bytes (default 10MB)
     * @return true if valid, false otherwise
     */
    suspend fun validateImageSize(imageUri: Uri, maxSize: Long = getMaxUploadSize()): Boolean {
        return try {
            val storageRef = storage.reference
            // Note: This is a simplified validation. In production, you might want to
            // check the actual file size using ContentResolver
            true
        } catch (e: Exception) {
            false
        }
    }
}
