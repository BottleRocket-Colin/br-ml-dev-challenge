package com.br.ml.brpathfinder.ui.main

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.Image
import android.os.AsyncTask
import android.util.Log
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysisConfig
import androidx.camera.core.ImageProxy
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.br.ml.brpathfinder.collision.AlgorithmicDetector
import com.br.ml.brpathfinder.feedback.FeedbackInterface
import com.br.ml.brpathfinder.models.DetectedObject
import com.br.ml.brpathfinder.models.Frame
import com.br.ml.brpathfinder.models.Risk
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions
import java.nio.IntBuffer


class MainViewModel : ViewModel() {
    val detector by lazy { AlgorithmicDetector() }
    var feedback: FeedbackInterface? = null

    // UI
    val boundingBoxes: ObservableArrayList<DetectedObject> = ObservableArrayList()
    val risks: ObservableArrayList<Risk> = ObservableArrayList()
    val mlKitImage: ObservableField<Drawable> = ObservableField()

    // Fragment comms
    val analyzedDimens = MutableLiveData<Pair<Int, Int>>()
    val mlDrawable = MutableLiveData<Drawable>()

    ///////////////////////////////////////////////////////////////////////////
    // Gravity sensor setup
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Image analysis setup
    ///////////////////////////////////////////////////////////////////////////
    // fixme - this pattern is broken in alpha-08
    private val imageAnalysisConfig = ImageAnalysisConfig.Builder()
        .setTargetResolution(Size(1280, 960))
        .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
        .build()
    val imageAnalysis = ImageAnalysis(imageAnalysisConfig)

    // Live detection and tracking
    private val options = FirebaseVisionObjectDetectorOptions.Builder()
        .setDetectorMode(FirebaseVisionObjectDetectorOptions.STREAM_MODE)
        .enableMultipleObjects()
        .build()

    val objectDetector = FirebaseVision.getInstance().getOnDeviceObjectDetector(options)

    private val analyzer = BRImageAnalyzer()
    inner class BRImageAnalyzer : ImageAnalysis.Analyzer {
        private fun degreesToFirebaseRotation(degrees: Int): Int = when(degrees) {
            0 -> FirebaseVisionImageMetadata.ROTATION_0
            90 -> FirebaseVisionImageMetadata.ROTATION_90
            180 -> FirebaseVisionImageMetadata.ROTATION_180
            270 -> FirebaseVisionImageMetadata.ROTATION_270
            else -> throw Exception("Rotation must be 0, 90, 180, or 270.")
        }

        // Connect main analysis loop
        override fun analyze(imageProxy: ImageProxy?, degrees: Int) {
            val mediaImage = imageProxy?.image
            val imageRotation = degreesToFirebaseRotation(degrees)
            if (mediaImage != null) {
                // Notify fragment
                analyzedDimens.postValue(Pair(imageProxy.width , imageProxy.width))
                // FIXME - this is posting before bounding boxes will update
                postImage(mediaImage)

                // Store values for detector use
                detector.width = imageProxy.width
                detector.height = imageProxy.height
                val timestamp = System.currentTimeMillis()

                val image = FirebaseVisionImage.fromMediaImage(mediaImage, imageRotation)
                // Pass image to an ML Kit Vision API
                objectDetector.processImage(image)
                    .addOnSuccessListener { detectedObjects ->
                        val objects = detectedObjects.map {
                            DetectedObject(
                                it.trackingId ?: 0,
                                it.boundingBox
                            )
                        }

                        // Update UI overlay
                        boundingBoxes.clear()
                        boundingBoxes.addAll(objects)

                        // Add frame to detector
                        detector.addFrame(Frame(
                            objects = objects,
                            timestamp = timestamp
                        ))

                        // Run detection and pass results to feedback engine
                        detector.runDetection { list ->
                            risks.addAll(list)

                            risks.maxBy { it.severity }?.let { maxRisk ->
                                feedback?.signalUser(maxRisk)
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("CCS", "FAIL!!!")
                    }
            }
        }
    }

    init {
        imageAnalysis.setAnalyzer(AsyncTask.THREAD_POOL_EXECUTOR, analyzer)
    }

    fun postImage(image: Image) {

        // TODO  // Convert YUV to RGB, JFIF transform with fixed-point math
        //    // R = Y + 1.402 * (V - 128)
        //    // G = Y - 0.34414 * (U - 128) - 0.71414 * (V - 128)
        //    // B = Y + 1.772 * (U - 128)
        val yBytes =  image.planes[0].buffer.run {
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
        (0 until image.width).forEach { x->
            (image.height-1 downTo 0).forEach { y ->
                val yIdx = (y * yRowStride) + (x * yPixelStride)
                val uvIdx = ((y/2) * uvRowStride) + ((x/2) * uvPixelStride)

                // TODO - Will moving vals out of loop avoid GC?
                val yb = ((yBytes[yIdx].toUByte().toInt() * 1.164) - 16f).toInt()
                val ub = vBytes[uvIdx].toUByte().toInt() - 128
                val vb = uBytes[uvIdx].toUByte().toInt() - 128
                var r: Int = (yb + (1.596 * (vb))).toInt()
                var g: Int = (yb - (.391 * (ub)) - (.813 * (vb))).toInt()
                var b: Int = (yb + (2.018 * (ub))).toInt()
                // TODO - Move this to constants and prehaps saved profiles....
                //  Also - check out other profiles for better look?
//                var r: Int = (yb + (1.140 * (vb))).toInt()
//                var g: Int = (yb - (.395 * (ub)) - (.581 * (vb))).toInt()
//                var b: Int = (yb + (2.032 * (ub))).toInt()
//                var r: Int = (yb + (1.402 * (vb))).toInt()
//                var g: Int = (yb - (.34414 * (ub)) - (.71414 * (vb ))).toInt()
//                var b: Int = (yb + (1.772 * (ub))).toInt()

                // Clip rgb values to 0-255
                // TODO - Look into bit mask to make faster?
                r = if (r < 0) 0 else if (r > 255) 255 else r
                g = if (g < 0) 0 else if (g > 255) 255 else g
                b = if (b < 0) 0 else if (b > 255) 255 else b

                // Pre-compute alpha multiplied out.
                intBuffer.put((alpha * 16777216 + r * 65536 + g * 256 + b).toInt())
            }
        }

        intBuffer.flip()
        val bitmap = Bitmap.createBitmap(image.height, image.width, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(intBuffer)

        val bitmapDrawable = BitmapDrawable(bitmap)
        mlDrawable.postValue(bitmapDrawable)
    }

}
