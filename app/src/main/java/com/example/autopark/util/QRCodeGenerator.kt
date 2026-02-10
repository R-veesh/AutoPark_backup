package com.example.autopark.util

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.autopark.data.model.QRCodeData
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.security.MessageDigest

object QRCodeGenerator {

    fun generateQr(data: String): Bitmap? {
        return try {
            if (data.isBlank()) {
                Log.e("QRCodeGenerator", "Cannot generate QR code with empty data")
                return null
            }

            val writer = QRCodeWriter()
            val hints = hashMapOf<EncodeHintType, Any>()
            hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
            hints[EncodeHintType.MARGIN] = 1

            val size = 512
            val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, size, size, hints)
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

            for (y in 0 until size) {
                for (x in 0 until size) {
                    bitmap.setPixel(
                        x,
                        y,
                        if (bitMatrix[x, y]) android.graphics.Color.BLACK
                        else android.graphics.Color.WHITE
                    )
                }
            }
            Log.d("QRCodeGenerator", "QR code generated successfully for data: $data")
            bitmap
        } catch (e: Exception) {
            Log.e("QRCodeGenerator", "Error generating QR code", e)
            null
        }
    }
    
    /**
     * Generate QR code bitmap from string
     * @param data The data to encode in the QR code
     * @param size The size of the generated QR code (default 512x512)
     * @return Bitmap containing the QR code or null if generation fails
     */
    fun generateQRCode(data: String, size: Int = 512): Bitmap? = try {
        val writer = QRCodeWriter()
        val hints = mapOf(EncodeHintType.MARGIN to 1)
        val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, size, size, hints)
        
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        
        bitmap
    } catch (e: Exception) {
        Log.e("QRCodeGenerator", "Error generating QR code", e)
        null
    }
    
    /**
     * Generate security hash for QR code
     * @param userId The user ID
     * @param timestamp The timestamp
     * @return Base64 encoded SHA-256 hash
     */
    fun generateSecurityHash(userId: String, timestamp: Long): String = try {
        val input = "$userId|$timestamp|AUTOPARK_SECRET_KEY"
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = messageDigest.digest(input.toByteArray())
        Base64.encodeToString(hashedBytes, Base64.NO_WRAP)
    } catch (e: Exception) {
        Log.e("QRCodeGenerator", "Error generating security hash", e)
        ""
    }
    
    /**
     * Create QRCodeData object with current timestamp
     * @param userId The user ID
     * @param vehicleNumber The vehicle number
     * @param qrType The type of QR code - "ENTRY" or "EXIT"
     * @return QRCodeData object with generated security hash
     */
    fun createQRCodeData(userId: String, vehicleNumber: String, qrType: String = "ENTRY"): QRCodeData {
        val timestamp = System.currentTimeMillis()
        val hash = generateSecurityHash(userId, timestamp)
        
        return QRCodeData(
            userId = userId,
            vehicleNumber = vehicleNumber,
            timestamp = timestamp,
            securityHash = hash,
            qrType = qrType
        )
    }
}