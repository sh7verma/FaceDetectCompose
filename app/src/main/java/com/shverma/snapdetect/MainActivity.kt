package com.shverma.snapdetect

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.shverma.snapdetect.camera.FaceDetectionScreen
import com.shverma.snapdetect.ui.theme.SnapDetectTheme


@androidx.camera.core.ExperimentalGetImage
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SnapDetectTheme {
                val permissionState =
                    rememberPermissionState(permission = Manifest.permission.CAMERA)

                LaunchedEffect(Unit) {
                    if (!permissionState.status.isGranted) {
                        permissionState.launchPermissionRequest()
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
                    when {
                        permissionState.status.isGranted -> {
                            FaceDetectionScreen()
                        }

                        permissionState.status.shouldShowRationale -> {
                            PermissionExplanationUI {
                                permissionState.launchPermissionRequest()
                            }
                        }

                        else -> {
                            PermissionDeniedUI()
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun PermissionExplanationUI(onRequest: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Camera permission is needed to detect faces.")
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRequest) {
            Text("Grant Permission")
        }
    }
}

@Composable
fun PermissionDeniedUI() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Camera permission was permanently denied. Please enable it in settings.")
    }
}
