package com.br.ml.brpathfinder.ui.main

import android.graphics.Bitmap
import android.graphics.Color
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
import androidx.core.graphics.set
import androidx.databinding.ObservableArrayList
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.br.ml.brpathfinder.collision.AlgorithmicDetector
import com.br.ml.brpathfinder.feedback.FeedbackInterface
import com.br.ml.brpathfinder.models.DetectedObject
import com.br.ml.brpathfinder.models.Direction
import com.br.ml.brpathfinder.models.Frame
import com.br.ml.brpathfinder.models.Risk
import com.google.firebase.ml.custom.*
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.schedulers.Schedulers
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.time.LocalDateTime


class MainViewModel : ViewModel() {
    var modelFile: ByteArray? = null
    val detector by lazy { AlgorithmicDetector() }
    val feedbacks = mutableListOf<FeedbackInterface>()

    private val intValues = IntArray(DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y)
    private var imgData: ByteBuffer = ByteBuffer.allocateDirect(DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE * 4)

    private val dmTime = 1000

    // UI
    val risks = ObservableArrayList<Risk>()
    val history = ObservableArrayList<Frame>()

    // Fragment comms
    val analyzedDimens = MutableLiveData<Pair<Int, Int>>()
    val mlDrawable = MutableLiveData<Drawable>()
    val tfDrawable = MutableLiveData<Drawable>()

    // Relays
    private val tfRelay = BehaviorRelay.create<Bitmap>()

    // State
    private var pixelVal = 0f
    private var tfBusy = false
    private var parkedBitmap: Bitmap? = null
    private var lastDMC = 0L

    init {
        // TODO - Connect disposables
        tfRelay.observeOn(Schedulers.newThread()).subscribe { depthMapTFLite(it) }
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
                // Notify fragment
                // TODO - Handle rotation here - this is for fixed portrait
                analyzedDimens.postValue(Pair(imageProxy.height , imageProxy.width))

//                return  // TODO - connect this to ui toggle

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

                        // TODO- replace this with real risk logic.
                        feedbacks.forEach { feedback ->
                            feedback.signalUser(Risk(Direction.BOTH, detectedObjects.size / 6f, 1))
                        }

                        detector.addFrame(
                            Frame(
                                objects = detectedObjects.map {
                                    DetectedObject(it.trackingId ?: 0, it.boundingBox)
                                },
                                timestamp = timestamp
                            )
                        )
                        history.clear()
                        history.addAll(detector.frameHistory)

                        // TODO - Reconnect risk detection when we can get data from distance map into it.
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
                        // TODO - proper error handling
                        Log.e("CCS", "FAIL!!!")
                    }
            }
        }
    }

    init {
        imageAnalysis.setAnalyzer(AsyncTask.THREAD_POOL_EXECUTOR, analyzer)
    }

    // TODO -Move YUC converter to utils class
    //  & Perf profile this method while at it.  We're spending .06 seconds out of pur overall .1 to .11 second cycle.
    //     So there could gains here.
    fun parkImage(image: Image, timestamp: Long) {
        Log.d("CCS","Beginning park for timestamp: $timestamp")

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

        // TODO - SG - Connect to UI Switch
        if (size > 0 && (timestamp - dmTime > lastDMC)) {
            lastDMC = timestamp
            parkedBitmap?.scale(640, 480)?.let { tfRelay.accept(it) }
        }
    }

//    private val modelFilename = "depth_trained30_quant.tflite"
    private val modelFilename = "depth_trained25.tflite"

    // TF LIte Interpreter Setup
    private val tfInterpreter  by lazy {
        val buffer = ByteBuffer.allocateDirect(modelFile?.size ?: 0).apply { put(modelFile) }
        Interpreter(buffer, Interpreter.Options()
            // TODO - Setup fall back so emulator can use CPU when first GPU exception is thrown.... maybe...
//            .setNumThreads(4)
            .addDelegate(GpuDelegate())
        )
    }

    private fun depthMapTFLite(bitmap: Bitmap) {
        if (tfBusy) return
        tfBusy = true
        Log.d("CCS", "DMTF - Setup  ${LocalDateTime.now()}")

        val input = (convertBitmapToByteBuffer(bitmap).rewind() as? ByteBuffer)?.asFloatBuffer()
        val inputArray = arrayOf(input)

        Log.d("CCS", "DMTF - Start TF ${LocalDateTime.now()}")

        val output = FloatBuffer.allocate(1 * 240 * 320 * 1)
        val outputMap = mapOf(0 to output)

        // TODO maybe a try catch to see if emualtor is crashing and use non GPU
        tfInterpreter.runForMultipleInputsOutputs(inputArray, outputMap)
        Log.d("CCS", "DMTF - End TF ${LocalDateTime.now()}")

        val floats = outputMap[0]?.array()

        val minPixel = floats?.min() ?: 0f
        val maxPixel = floats?.max() ?: 0f

        // TODO - Fix orientation of TF ML image.
        val outBitmap = Bitmap.createBitmap(320, 240, bitmap.config)

        floats?.forEachIndexed { index, pixel ->
            val x = index / 320
            val y = index.rem(320)
            pixelVal = (pixel - minPixel) / maxPixel
            outBitmap[y, x] = Color.rgb(pixelVal, 0f, 1f - pixelVal)
        }
        val bitmapDrawable = BitmapDrawable(outBitmap)
        tfDrawable.postValue(bitmapDrawable)
        tfBusy = false
        Log.d("CCS", "DMTF - End func ${LocalDateTime.now()}")
    }


    // TODO - move to utils
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
