package minmul.kwpass.shared

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

object QrGenerator {
    private val qrWriter = MultiFormatWriter()

    fun generateQrBitmapInternal(content: String, margin: Int = 1, size: Int = 1): Bitmap? {
        return try {
            val hint = mapOf(
                EncodeHintType.MARGIN to margin
            )
            val bitMatrix: BitMatrix = qrWriter.encode(
                content,
                BarcodeFormat.QR_CODE,
                size, size,
                hint
            )
            val width = bitMatrix.width
            val height = bitMatrix.height

            val pixels = IntArray(width * height)

            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                }
            }
            val bmp = createBitmap(width, height, Bitmap.Config.RGB_565)
            bmp.setPixels(pixels, 0, width, 0, 0, width, height)
            bmp
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

