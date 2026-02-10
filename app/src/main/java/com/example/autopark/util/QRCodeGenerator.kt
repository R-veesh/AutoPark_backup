package com.example.autopark.util

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

object QRCodeGenerator {

    fun generateQr(data: String): Bitmap {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512)

        val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)

        for (x in 0 until 512) {
            for (y in 0 until 512) {
                bitmap.setPixel(
                    x,
                    y,
                    if (bitMatrix[x, y]) android.graphics.Color.BLACK
                    else android.graphics.Color.WHITE
                )
            }
        }
        return bitmap
    }
}
