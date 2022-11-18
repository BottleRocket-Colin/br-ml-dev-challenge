package com.br.ml.pathfinder.compose.ui.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@ExperimentalGetImage
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen() {
    val context = LocalContext.current
//    val lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    // Setup Image Capture use cse for preview
    val imageCapture: ImageCapture = remember {
        ImageCapture.Builder().build()
    }

    //  Set barcode scanner options and build
    val scanner: BarcodeScanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder().setBarcodeFormats(
                Barcode.FORMAT_PDF417
            ).build()
        )
    }

    // Setup ML Kit image analyuzer use case
    val imageAnalysis: ImageAnalysis = remember {
        ImageAnalysis.Builder().build().apply {
            setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                processImage(scanner, imageProxy)
            }
        }
    }

    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )

    if (cameraPermissionState.status.isGranted) {
        CameraPreviewView(
            imageCapture = imageCapture,
            imageAnalysis = imageAnalysis,
            lensFacing = CameraSelector.LENS_FACING_BACK)
    } else {
        Column {
            Text(
                if (cameraPermissionState.status.shouldShowRationale) {
                    "This feature can not be used without camera permissions"
                } else {
                    "Please grant permission for the camera to continue"
                }
            )
            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text("Request Permission")
            }
        }
    }
}

@ExperimentalGetImage
private fun processImage(
    barcodeScanner: BarcodeScanner,
    imageProxy: ImageProxy
) {

    imageProxy.image?.let { image ->
        val inputImage =
            InputImage.fromMediaImage(
                image,
                imageProxy.imageInfo.rotationDegrees
            )

        barcodeScanner.process(inputImage)
            .addOnSuccessListener { barcodeList ->
                val barcode = barcodeList.getOrNull(0)
                barcode?.driverLicense?.let {
                    Log.d("PATH", "First : ${it.firstName}")
                    Log.d("PATH", "Last : ${it.lastName}")
                    Log.d("PATH", "Number : ${it.licenseNumber}")
                    Log.d("PATH", "Type: ${it.documentType}")
                    Log.d("PATH", "Type: ${it.expiryDate}")
                } ?: run {
                    barcode?.rawValue?.let { value ->
                        Log.d("PATH", "Barcode: $value")
//                   TODO - do somthing with this!!!!
                    }
                }
            }
            .addOnFailureListener {
//                TODO - on fail
            }.addOnCompleteListener {
                imageProxy.image?.close()
                imageProxy.close()
            }
    }
}


suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { cameraProvider ->
        cameraProvider.addListener({
            continuation.resume(cameraProvider.get())
        }, ContextCompat.getMainExecutor(this))
    }
}

@Composable
fun CameraPreviewView(
    imageCapture: ImageCapture,
    imageAnalysis: ImageAnalysis,
    lensFacing: Int = CameraSelector.LENS_FACING_BACK,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val preview = Preview.Builder().build()
    val cameraSelector = CameraSelector.Builder()
        .requireLensFacing(lensFacing)
        .build()

    val previewView = remember {
        PreviewView(context)
    }

    LaunchedEffect(key1 = lensFacing) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture,
            imageAnalysis
//         TODO - create use case for image analysis and bind here.
        )
        preview.setSurfaceProvider(previewView.surfaceProvider)
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize()) {}

        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
            verticalArrangement = Arrangement.Bottom
        ) {
//            TODO - CameraControls()
        }

    }
}

@ExperimentalGetImage
@androidx.compose.ui.tooling.preview.Preview
@Composable
fun CameraPreview() {
    CameraScreen()
}