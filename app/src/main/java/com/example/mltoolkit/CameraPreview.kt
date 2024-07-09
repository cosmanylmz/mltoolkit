package com.example.mltoolkit

import android.graphics.Rect
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.ExecutorService

@Composable
fun CameraPreview(cameraExecutor: ExecutorService, onTextRecognized: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    ) { view ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = androidx.camera.core.Preview.Builder().build().also {
                it.setSurfaceProvider(view.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        try {
                            processImageProxy(imageProxy, onTextRecognized)
                        } catch (e: Exception) {
                            Log.e("CameraPreview", "Image analysis failed", e)
                            imageProxy.close()
                        }
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e("CameraPreview", "Binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }
}

@OptIn(ExperimentalGetImage::class)
private fun processImageProxy(imageProxy: ImageProxy, onTextRecognized: (String) -> Unit) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        // Sarı dikdörtgen alanın koordinatları
        val boundingBox = Rect(
            (mediaImage.width * 0.1).toInt(),  // Sol
            (mediaImage.height * 0.5).toInt(), // Üst
            (mediaImage.width * 0.9).toInt(),  // Sağ
            (mediaImage.height * 0.9).toInt()  // Alt
        )

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val filteredText = filterTextByBoundingBox(visionText, boundingBox)
                onTextRecognized(filteredText)
                imageProxy.close()
            }
            .addOnFailureListener { e ->
                Log.e("processImageProxy", "Text recognition failed", e)
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}

private fun filterTextByBoundingBox(visionText: Text, boundingBox: Rect): String {
    val filteredBlocks = visionText.textBlocks.filter { block ->
        block.boundingBox?.let { boundingBox.contains(it) } == true
    }
    return filteredBlocks.joinToString("\n") { it.text }
}
