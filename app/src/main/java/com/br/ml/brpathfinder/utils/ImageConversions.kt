package com.br.ml.brpathfinder.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.media.Image
import androidx.core.graphics.set
import com.br.ml.brpathfinder.ui.main.MainViewModel
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer


//  & Perf profile this method while at it.  We're spending .06 seconds out of pur overall .1 to .11 second cycle.
//     So there could gains here.
fun convertYUVImageToARGBIntBuffer(image: Image): IntBuffer {
    // TODO  // Convert YUV to RGB, JFIF transform with fixed-point math
    //    // R = Y + 1.402 * (V - 128)
    //    // G = Y - 0.34414 * (U - 128) - 0.71414 * (V - 128)
    //    // B = Y + 1.772 * (U - 128)
    val yBytes = image.planes[0].buffer.run {
        val bytes = ByteArray(capacity())
        get(bytes)
        rewind()
        bytes
    }
    val uBytes = image.planes[1].buffer.run {
        val bytes = ByteArray(capacity())
        get(bytes)
        rewind()
        bytes
    }
    val vBytes = image.planes[2].buffer.run {
        val bytes = ByteArray(capacity())
        get(bytes)
        rewind()
        bytes
    }
    val uvRowStride = image.planes[1].rowStride
    val uvPixelStride = image.planes[1].pixelStride
    val yRowStride = image.planes[0].rowStride
    val yPixelStride = image.planes[0].pixelStride

    val alpha = 255L
    // TODO - add code to allocate buffer on longer term
    val intBuffer: IntBuffer = IntBuffer.allocate(image.width * image.height)
    intBuffer.position(0)

    // TODO - Add rotation based logic here to control the ranges.
    //  Camera appears to be in portrait mode.
    (0 until image.width).forEach { x ->
        (image.height - 1 downTo 0).forEach { y ->
            val yIdx = (y * yRowStride) + (x * yPixelStride)
            val uvIdx = ((y / 2) * uvRowStride) + ((x / 2) * uvPixelStride)

            // TODO - Will moving vals out of loop avoid GC?
            val yb = ((yBytes[yIdx].toUByte().toInt() * 1.164) - 16f).toInt()
            val ub = vBytes[uvIdx].toUByte().toInt() - 128
            val vb = uBytes[uvIdx].toUByte().toInt() - 128
            var r: Int = (yb + (1.596 * (vb))).toInt()
            var g: Int = (yb - (.391 * (ub)) - (.813 * (vb))).toInt()
            var b: Int = (yb + (2.018 * (ub))).toInt()

            // Clip rgb values to 0-255
            r = if (r < 0) 0 else if (r > 255) 255 else r
            g = if (g < 0) 0 else if (g > 255) 255 else g
            b = if (b < 0) 0 else if (b > 255) 255 else b

            intBuffer.put((alpha * 16777216 + r * 65536 + g * 256 + b).toInt())
        }
    }

    intBuffer.flip()
    return intBuffer
}

// Pass in buffer and array to avoid needing to reallocate the memory
fun convertBitmapToByteBuffer(bitmap: Bitmap?, imgData: ByteBuffer, intValues: IntArray) {
    //Clear the Bytebuffer for a new image
    imgData.rewind()
    imgData.order(ByteOrder.nativeOrder())
    bitmap?.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
    // Convert the image to floating point.
    var pixel = 0
    for (i in 0 until MainViewModel.DIM_IMG_SIZE_X) {
        for (j in 0 until MainViewModel.DIM_IMG_SIZE_Y) {
            val currPixel = intValues[pixel++]
            imgData.putFloat(((currPixel shr 16 and 0xFF) - MainViewModel.IMAGE_MEAN) / MainViewModel.IMAGE_STD)
            imgData.putFloat(((currPixel shr 8 and 0xFF) - MainViewModel.IMAGE_MEAN) / MainViewModel.IMAGE_STD)
            imgData.putFloat(((currPixel and 0xFF) - MainViewModel.IMAGE_MEAN) / MainViewModel.IMAGE_STD)
        }
    }
}

fun convertFloatArrayToBitmap(
    floats: FloatArray,
    bitmap: Bitmap,
    imageWidth: Int = 320
) {
    val minPixel = floats.minOrNull() ?: 0f
    val maxPixel = floats.maxOrNull() ?: 0f
    var pixelVal = 0f

    floats.forEachIndexed { index, pixel ->
        val y = index / imageWidth
        val x = index.rem(imageWidth)
        pixelVal = (pixel - minPixel) / maxPixel
        bitmap[x, y] = Color.rgb(pixelVal, 0f, 1f - pixelVal)
    }
}

