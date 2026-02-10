package com.autopark.qr

import android.annotation.SuppressLint
import android.media.Image
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class QrScannerAnalyzer(
    private val onQrScanned: (vehicleNumber: String, vehicleId: String) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient()

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage: Image = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val image =
            InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val rawValue = barcode.rawValue ?: continue

                    // ✅ FIXED SPLIT (VERY IMPORTANT)
                    val parts = rawValue.split("\\|".toRegex(), limit = 2)

                    if (parts.size == 2) {
                        val vehicleNumber = parts[0]
                        val vehicleId = parts[1]

                        onQrScanned(vehicleNumber, vehicleId)
                    }
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}
