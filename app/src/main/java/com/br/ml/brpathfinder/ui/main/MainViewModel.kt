package com.br.ml.brpathfinder.ui.main

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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
import com.br.ml.brpathfinder.collision.AlgorithmicDetector
import com.br.ml.brpathfinder.feedback.FeedbackInterface
import com.br.ml.brpathfinder.models.DetectedObject
import com.br.ml.brpathfinder.models.Risk
import com.google.firebase.ml.custom.*
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions
import com.jakewharton.rxrelay2.BehaviorRelay
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.time.LocalDateTime
import kotlin.math.max
import kotlin.math.min


class MainViewModel : ViewModel() {
    var modelFile: ByteArray? = null
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

    // Relays
    private val tfRelay = BehaviorRelay.create<Bitmap>()

    init {
        // TODO - Connect disposables
        tfRelay.subscribe { depthMapTFLite(it) }
    }

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
                if (busy) return  // Don't process ML Kit while TF Lite is running
                // Notify fragment
                // TODO - Handle rotation here - this is for fixed portrait
                analyzedDimens.postValue(Pair(imageProxy.height , imageProxy.width))

//                return  // TODO - connect this to ui

                // Store values for detector use
                // TODO - Handle rotation here - this is for fixed portrait
                detector.width = imageProxy.height
                detector.height = imageProxy.width

                val timestamp = System.currentTimeMillis()
                parkImage(mediaImage, timestamp) // Must run before FirebaseVisionImage.fromMediaImage
                val image = FirebaseVisionImage.fromMediaImage(mediaImage, imageRotation)

                objectDetector.processImage(image)
                    .addOnSuccessListener { detectedObjects ->
                        Log.d("CCS", "ML Kit Detected: ${detectedObjects.size}")
                        postParkedImage(detectedObjects.size, timestamp)
                        // TODO - SG - Add ML kit frame time to UI below MLkit view.
                        //    -- Also add the ML Kit detection count to UI below as well

                        val objects = detectedObjects.map {
                            DetectedObject(
                                it.trackingId ?: 0,
                                // TODO - check to dee if we need to adjust rect based off rotation ???
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

    fun parkImage(image: Image, timestamp: Long) {
        // TODO - SG - if both UI switches are off this step can be skipped.

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
        parkedBitmap = Bitmap.createBitmap(image.height, image.width, Bitmap.Config.ARGB_8888)
        parkedBitmap?.copyPixelsFromBuffer(intBuffer)
        Log.d("CCS","Bitmap parked for timestamp: $timestamp")
    }

    fun postParkedImage(size: Int, timestamp: Long) {
        Log.d("CCS","Posting bitmap for timestamp: $timestamp")

        // TODO - SG - Connect this to UI switch
        val bitmapDrawable = BitmapDrawable(parkedBitmap)
        mlDrawable.postValue(bitmapDrawable)

        // todo - ccs - move this to constant
        // TODO - SG - Connect to UI Switch
        if (size > 0 && (timestamp - 1000 > lastDMC)) {
            lastDMC = timestamp
            parkedBitmap?.scale(640, 480)?.let { tfRelay.accept(it) }
        }
    }

//    private val modelFilename = "depth_trained30_quant.tflite"
    private val modelFilename = "depth_trained25.tflite"

    private var parkedBitmap: Bitmap? = null
    private var lastDMC = 0L
    // Firebase Interpreter setup
//    private val fireBaseLocalModelSource = FirebaseCustomLocalModel.Builder().setAssetFilePath("depth_trained25.tflite").build()
    private val fireBaseLocalModelSource = FirebaseCustomLocalModel.Builder().setAssetFilePath(modelFilename).build()

    // Registering the model loaded above with the ModelManager Singleton
    private val interpreter = FirebaseModelInterpreter.getInstance(
        FirebaseModelInterpreterOptions.Builder(fireBaseLocalModelSource).build())

    // Store hard coded input option to avoid GC/re alloc per frame
    private val inputOutputOptions = FirebaseModelInputOutputOptions.Builder()
        .setInputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1, 480, 640, 3))
        .setOutputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1, 240, 320, 1))
        .build()

    // Only alloc paint once
    private val rectPaint = Paint()

    // Used to avoid submitting while active
    private var busy = false


    // TF LIte Interpreter Setup
    private val tfInterpreter  by lazy {
        val buffer = ByteBuffer.allocateDirect(modelFile?.size ?: 0).apply { put(modelFile) }
        Interpreter(buffer, Interpreter.Options()
//            .setNumThreads(4)
            .addDelegate(GpuDelegate())
        )
    }

    private fun depthMapTFLite(bitmap: Bitmap) {
        if (busy) return
        busy = true
        Log.d("CCS", "DMTF - Setup  ${LocalDateTime.now()}")

        val input = (convertBitmapToByteBuffer(bitmap).rewind() as? ByteBuffer)?.asFloatBuffer()
        val inputArray = arrayOf(input)

        Log.d("CCS", "DMTF - Start TF ${LocalDateTime.now()}")

        val output = FloatBuffer.allocate(1 * 240 * 320 * 1)
        val outputMap = mapOf(0 to output)

        tfInterpreter.runForMultipleInputsOutputs(inputArray, outputMap)

        Log.d("CCS", "DMTF - End TF ${LocalDateTime.now()}")

        val floats = outputMap[0]?.array()

        val minPixel = floats?.min() ?: 0f
        val maxPixel = floats?.max() ?: 0f

        val canvas = Canvas(bitmap)
        floats?.forEachIndexed { index, pixel ->
            val x = index / 320
            val y= index.rem(320)
            drawOutputPixel(floatArrayOf(pixel), minPixel, maxPixel, canvas, y, x)
        }
        val bitmapDrawable = BitmapDrawable(bitmap)
        tfDrawable.postValue(bitmapDrawable)
        busy = false
    }


    private fun depthMapMLKit(bitmap: Bitmap) {
        interpreter
        // TODO - look into some kind of throttle to free resources for .25->.5 seconds between runs.
        //  needs to be enough time for MLKit to do it's thing and get good results.
        //  Or maybe a mode where it's just MLkit until it see an object then fires off the depth,
        //  we could reconcile timing differences later.
        if (busy) return
        busy = true
        Log.d("CCS", "DMC - Start  ${LocalDateTime.now()}")

        val inputs = FirebaseModelInputs.Builder()
            .add(convertBitmapToByteBuffer(bitmap)) // add() as many input arrays as your model requires
            .build()

        Log.d("CCS", "DMC - Enter Interpreter  ${LocalDateTime.now()}")

        interpreter?.run(inputs, inputOutputOptions)
            ?.addOnSuccessListener {
                Log.d("CCS", "DMC - Interpreter Success  ${LocalDateTime.now()}")
                // TODO - SG - Add elapsed depth map time to ui below ML kit output image

                val output = it.getOutput<Array<Array<Array<FloatArray>>>>(0)
                val canvas = Canvas(bitmap)
                var minPixel = 999f
                var maxPixel = -999f

                // TODO - Look at faster way of doing this the vs forEach
                output[0].forEach { row ->
                    row.forEach { pixel ->
                        minPixel = min(minPixel, pixel[0])
                        maxPixel = max(maxPixel, pixel[0])
                    }
                }

                output[0].forEachIndexed { x, row ->
                    row.forEachIndexed { y, pixel ->
                        drawOutputPixel(pixel, minPixel, maxPixel, canvas, y, x)
                    }
                }

                Log.d("CCS", "DMC - Post Drawable Interpreter  ${LocalDateTime.now()}")

                val bitmapDrawable = BitmapDrawable(bitmap)
                tfDrawable.postValue(bitmapDrawable)
                busy = false
            }
            ?.addOnFailureListener {
                busy = false
                Log.d("CCS", "DMC Failure - ${it.localizedMessage}")
                //The interpreter failed to identify a Pokemon
            }
    }

    // TODO - Check to see if this needs to scale since drawable will scale to imageView....
    private fun drawOutputPixel(
        pixel: FloatArray,
        minPixel: kotlin.Float,
        maxPixel: kotlin.Float,
        canvas: Canvas,
        y: Int,
        x: Int
    ) {
        val pixelVal = (pixel[0] - minPixel) / maxPixel
        rectPaint.color = Color.rgb(pixelVal, 0f, 1f - pixelVal)
        rectPaint.strokeWidth = 5.0f
        canvas.drawPoint(y.toFloat() * 2, x.toFloat() * 2, rectPaint)
        canvas.drawPoint(y.toFloat() * 2, x.toFloat() * 2 + 1, rectPaint)
        canvas.drawPoint(y.toFloat() * 2 + 1, x.toFloat() * 2, rectPaint)
        canvas.drawPoint(y.toFloat() * 2 + 1, x.toFloat() * 2 + 1, rectPaint)
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
