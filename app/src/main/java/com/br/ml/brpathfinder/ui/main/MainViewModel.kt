package com.br.ml.brpathfinder.ui.main

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.Image
import android.os.AsyncTask
import android.util.Log
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysisConfig
import androidx.camera.core.ImageProxy
import androidx.core.graphics.scale
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.br.ml.brpathfinder.R
import com.br.ml.brpathfinder.collision.AlgorithmicDetector
import com.br.ml.brpathfinder.feedback.FeedbackInterface
import com.br.ml.brpathfinder.models.DetectedObject
import com.br.ml.brpathfinder.models.Frame
import com.br.ml.brpathfinder.models.Risk
import com.google.firebase.ml.custom.*
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions
import java.lang.Float
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer
import java.time.LocalDateTime


class MainViewModel : ViewModel() {
    val detector by lazy { AlgorithmicDetector() }
    var feedback: FeedbackInterface? = null

    private val intValues = IntArray(DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y)
    private var imgData: ByteBuffer = ByteBuffer.allocateDirect(DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE * 4)

    // UI
    val boundingBoxes: ObservableArrayList<DetectedObject> = ObservableArrayList()
    val risks: ObservableArrayList<Risk> = ObservableArrayList()
    val mlKitImage: ObservableField<Drawable> = ObservableField()

    // Fragment comms
    val analyzedDimens = MutableLiveData<Pair<Int, Int>>()
    val mlDrawable = MutableLiveData<Drawable>()
    val tfDrawable = MutableLiveData<Drawable>()

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
                postImage(mediaImage)

                // Store values for detector use
                detector.width = imageProxy.width
                detector.height = imageProxy.height
                val timestamp = System.currentTimeMillis()

                val image = FirebaseVisionImage.fromMediaImage(mediaImage, imageRotation)
                // Pass image to an ML Kit Vision API
                objectDetector.processImage(image)
                    .addOnSuccessListener { detectedObjects ->
                        Log.d("CCS", "ML Kit Detected: ${detectedObjects.size}")

                        val objects = detectedObjects.map {
                            DetectedObject(
                                it.trackingId ?: 0,
                                it.boundingBox
                            )
                        }

                        // Update UI overlay
                        boundingBoxes.clear()
                        boundingBoxes.addAll(objects)

                        // TODO - Reconnect risk detection when we can get data from distance map into it.
                        // Add frame to detector
//                        detector.addFrame(Frame(
//                            objects = objects,
//                            timestamp = timestamp
//                        ))

                        // Run detection and pass results to feedback engine
//                        detector.runDetection { list ->
//                            risks.addAll(list)
//
//                            risks.maxBy { it.severity }?.let { maxRisk ->
//                                feedback?.signalUser(maxRisk)
//                            }
//                        }
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

        depthMapConversion(bitmap.scale(640, 480))
    }

    private var busy = false
    private fun depthMapConversion(bitmap: Bitmap) {
        if (busy) return
        busy = true
        Log.d("CCS", "Start DMC  ${LocalDateTime.now()}")
        
        val optionsB = BitmapFactory.Options()
        optionsB.inMutable = true
        //var bitmap =  BitmapFactory.decodeResource(resources, R.drawable.test8, optionsB)
        val fireBaseLocalModelSource = FirebaseCustomLocalModel.Builder().setAssetFilePath("depth_trained25.tflite").build()
        //Registering the model loaded above with the ModelManager Singleton

        val options = FirebaseModelInterpreterOptions.Builder(fireBaseLocalModelSource).build()
        val interpreter = FirebaseModelInterpreter.getInstance(options)

        val inputOutputOptions = FirebaseModelInputOutputOptions.Builder()
            .setInputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1, 480, 640, 3))
            .setOutputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1, 240, 320, 1))
            .build()

        val inputs = FirebaseModelInputs.Builder()
            .add(convertBitmapToByteBuffer(bitmap)) // add() as many input arrays as your model requires
            .build()

        Log.d("CCS", "Enter Interpreter  ${LocalDateTime.now()}")

        interpreter?.run(inputs, inputOutputOptions)
            ?.addOnSuccessListener {
                //img.setImageDrawable()
                val output = it.getOutput<Array<Array<Array<FloatArray>>>>(0)
                Log.d("CCS", "Interpreter Success  ${LocalDateTime.now()}")

                val rectPaint = Paint()
                val canvas = Canvas(bitmap)

                var minPixel = 999f
                var maxPixel = -999f
                output[0].forEachIndexed { x, row ->
                    row.forEachIndexed { y, pixel ->
                        minPixel = Float.min(minPixel, pixel[0])
                        maxPixel = Float.max(maxPixel, pixel[0])
                    }
                }

                output[0].forEachIndexed { x, row ->
                    row.forEachIndexed { y, pixel ->

                        val pixelVal = (pixel[0] - minPixel ) / maxPixel
                        rectPaint.color = Color.rgb(pixelVal, 0f , 1f - pixelVal)
                        rectPaint.strokeWidth = 5.0f
                        canvas.drawPoint(y.toFloat() * 2, x.toFloat() * 2, rectPaint)
                        canvas.drawPoint(y.toFloat() * 2, x.toFloat() * 2 + 1, rectPaint)
                        canvas.drawPoint(y.toFloat()* 2 + 1, x.toFloat() * 2, rectPaint)
                        canvas.drawPoint(y.toFloat()* 2 + 1, x.toFloat() * 2 + 1, rectPaint)
                    }
                }

                Log.d("CCS", "Post Drawable Interpreter  ${LocalDateTime.now()}")

                val bitmapDrawable = BitmapDrawable(bitmap)
                tfDrawable.postValue(bitmapDrawable)
                busy = false
            }
            ?.addOnFailureListener {
                busy = false
                //The interpreter failed to identify a Pokemon
            }
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap?): ByteBuffer {
        //Clear the Bytebuffer for a new image
        imgData.rewind()
        imgData.order(ByteOrder.nativeOrder())
        bitmap?.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        // Convert the image to floating point.
        var pixel = 0
        for (i in 0 until DIM_IMG_SIZE_X) {
            for (j in 0 until DIM_IMG_SIZE_Y) {
                val currPixel = intValues[pixel++]
                imgData.putFloat(((currPixel shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                imgData.putFloat(((currPixel shr 8 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                imgData.putFloat(((currPixel and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
            }
        }
        return imgData
    }

    companion object {
        /** Dimensions of inputs.  */
        const val DIM_IMG_SIZE_X = 480
        const val DIM_IMG_SIZE_Y = 640
        const val DIM_BATCH_SIZE = 1
        const val DIM_PIXEL_SIZE = 3
        const val IMAGE_MEAN = 128
        private const val IMAGE_STD = 128.0f
    }

}
