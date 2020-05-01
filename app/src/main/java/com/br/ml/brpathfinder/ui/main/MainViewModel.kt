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
import androidx.core.graphics.scale
import androidx.databinding.ObservableArrayList
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.br.ml.brpathfinder.collision.AlgorithmicDetector
import com.br.ml.brpathfinder.feedback.FeedbackInterface
import com.br.ml.brpathfinder.models.DetectedObject
import com.br.ml.brpathfinder.models.Direction
import com.br.ml.brpathfinder.models.Frame
import com.br.ml.brpathfinder.models.Risk
import com.br.ml.brpathfinder.utils.convertBitmapToByteBuffer
import com.br.ml.brpathfinder.utils.convertFloatArrayToBitmap
import com.br.ml.brpathfinder.utils.convertYUVImageToARGBIntBuffer
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.schedulers.Schedulers
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.time.LocalDateTime


class MainViewModel : ViewModel() {

    companion object {
        /** Dimensions of inputs.  */
        const val DIM_IMG_SIZE_X = 480
        const val DIM_IMG_SIZE_Y = 640
        const val DIM_BATCH_SIZE = 1
        const val DIM_PIXEL_SIZE = 3
        const val IMAGE_MEAN = 128
        const val IMAGE_STD = 128.0f
        private const val dmTime = 1000

        // TODO - gather output constants
    }

    var modelFile: ByteArray? = null
    val detector by lazy { AlgorithmicDetector() }
    val feedbacks = mutableListOf<FeedbackInterface>()

    //  Heavy allocations
    private val intValues = IntArray(DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y)
    private var imgData = ByteBuffer.allocateDirect(DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE * 4)
    private val output = FloatBuffer.allocate(1 * 240 * 320 * 1)
    private val outputMap = mapOf(0 to output)
    private val outBitmap = Bitmap.createBitmap(320, 240, Bitmap.Config.ARGB_8888)


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


    // TF LIte Interpreter Setup
    private val tfInterpreter  by lazy {
        val buffer = ByteBuffer.allocateDirect(modelFile?.size ?: 0).apply { put(modelFile) }
        Interpreter(buffer, Interpreter.Options()
            // TODO - Setup fall back so emulator can use CPU when first GPU exception is thrown.... maybe...
//            .setNumThreads(4)
            .addDelegate(GpuDelegate())
        )
    }

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

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////
    fun parkImage(image: Image, timestamp: Long) {
        // TODO - SG - if both UI switches are off this step can be skipped.
        Log.d("CCS","Beginning park for timestamp: $timestamp")

        val intBuffer: IntBuffer = convertYUVImageToARGBIntBuffer(image)
        parkedBitmap = Bitmap.createBitmap(image.height, image.width, Bitmap.Config.ARGB_8888)
        parkedBitmap?.copyPixelsFromBuffer(intBuffer)
        Log.d("CCS","Bitmap parked for timestamp: $timestamp")
    }

    fun postParkedImage(size: Int, timestamp: Long) {
        Log.d("CCS","Posting bitmap for timestamp: $timestamp")

        // TODO - SG - Connect this to UI switch
        mlDrawable.postValue(BitmapDrawable(parkedBitmap))

        // TODO - SG - Connect to UI Switch
        if (size > 0 && (timestamp - dmTime > lastDMC)) {
            lastDMC = timestamp
            parkedBitmap?.scale(640, 480)?.let { tfRelay.accept(it) }
        }
    }



    private fun depthMapTFLite(bitmap: Bitmap) {
        if (tfBusy) return
        tfBusy = true
        Log.d("CCS", "DMTF - Setup  ${LocalDateTime.now()}")

        convertBitmapToByteBuffer(bitmap, imgData, intValues)
        val input = (imgData.rewind() as? ByteBuffer)?.asFloatBuffer()
        val inputArray = arrayOf(input)
        output.rewind()

        Log.d("CCS", "DMTF - Start TF ${LocalDateTime.now()}")
        // TODO maybe a try catch to see if emualtor is crashing and use non GPU
        tfInterpreter.runForMultipleInputsOutputs(inputArray, outputMap)
        Log.d("CCS", "DMTF - End TF ${LocalDateTime.now()}")

        outputMap[0]?.array()?.let {
            convertFloatArrayToBitmap(it, outBitmap)
            tfDrawable.postValue(BitmapDrawable(outBitmap))
        }
        tfBusy = false
        Log.d("CCS", "DMTF - End func ${LocalDateTime.now()}")
    }
}
