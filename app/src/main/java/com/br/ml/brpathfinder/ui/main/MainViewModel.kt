package com.br.ml.brpathfinder.ui.main

import android.os.AsyncTask
import android.util.Log
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysisConfig
import androidx.camera.core.ImageProxy
import androidx.databinding.ObservableArrayList
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


class MainViewModel : ViewModel() {
    val detector by lazy { AlgorithmicDetector() }
    var feedback: FeedbackInterface? = null

    // UI
    val boundingBoxes: ObservableArrayList<DetectedObject> = ObservableArrayList()
    val risks: ObservableArrayList<Risk> = ObservableArrayList()

    // Fragment comms
    val analyzedDimens = MutableLiveData<Pair<Int, Int>>()

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

}
