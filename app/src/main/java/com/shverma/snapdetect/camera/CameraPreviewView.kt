package com.shverma.snapdetect.camera

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.face.Face
import com.shverma.snapdetect.faceDetect.analyzeImage


@Composable
fun CameraPreviewView(
    onFrameCaptured: (ImageProxy) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    LaunchedEffect(Unit) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()

        val preview = Preview.Builder().build().apply {
            setSurfaceProvider(previewView.surfaceProvider)
        }

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(
                    ContextCompat.getMainExecutor(context),
                    onFrameCaptured
                )
            }

        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageAnalysis
        )
    }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    )
}


@Composable
fun CameraWithOverlay(
    faces: List<Face>,
    imageSize: Size,
    rotation: Int
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val previewWidth = imageSize.width
        val previewHeight = imageSize.height

        // Flip width/height if image was rotated
        val (imageW, imageH) = if (rotation == 0 || rotation == 180) {
            previewWidth to previewHeight
        } else {
            previewHeight to previewWidth
        }

        val scaleX = size.width / imageW
        val scaleY = size.height / imageH

        faces.forEach { face ->
            val rect = face.boundingBox

            val left = rect.left * scaleX
            val top = rect.top * scaleY
            val right = rect.right * scaleX
            val bottom = rect.bottom * scaleY

            // Mirror horizontally for front camera
            val mirroredLeft = size.width - right
            val mirroredRight = size.width - left

            drawRect(
                color = Color.Red,
                topLeft = Offset(mirroredLeft, top),
                size = Size(mirroredRight - mirroredLeft, bottom - top),
                style = Stroke(width = 4f)
            )
        }
    }
}


@Composable
fun FaceDetectionScreen() {
    val detectedFaces = remember { mutableStateOf<List<Face>>(emptyList()) }
    val imageSize = remember { mutableStateOf(Size.Zero) }
    val rotation = remember { mutableIntStateOf(0) }

    Box(Modifier.fillMaxSize()) {
        CameraPreviewView { imageProxy ->
            analyzeImage(imageProxy, detectedFaces, imageSize, rotation)
        }
        CameraWithOverlay(
            faces = detectedFaces.value,
            imageSize = imageSize.value,
            rotation = rotation.intValue
        )
    }
}
