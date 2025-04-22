package com.shverma.snapdetect.faceDetect

import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.compose.runtime.MutableState
import androidx.compose.ui.geometry.Size
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions


val faceDetector = FaceDetection.getClient(
    FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .enableTracking()
        .build()
)

@androidx.annotation.OptIn(ExperimentalGetImage::class)
fun analyzeImage(
    imageProxy: ImageProxy,
    detectedFaces: MutableState<List<Face>>,
    imageSize: MutableState<Size>,
    rotation: MutableState<Int>
) {
    val mediaImage = imageProxy.image ?: return
    val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

    imageSize.value = Size(imageProxy.width.toFloat(), imageProxy.height.toFloat())
    rotation.value = imageProxy.imageInfo.rotationDegrees

    faceDetector.process(inputImage)
        .addOnSuccessListener { faces ->
            detectedFaces.value = faces
        }
        .addOnFailureListener {
            Log.e("FaceDetection", "Face detection failed", it)
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}
