package com.gridrecon3d

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppRoot(onExit = { finish() })
                }
            }
        }
    }
}

private sealed class Screen {
    data class Capture(val mode: String, val jobId: String) : Screen()
    data class Review(val jobId: String, val shots: List<File>) : Screen()
}

@Composable
private fun AppRoot(onExit: () -> Unit) {
    var screen by remember { mutableStateOf<Screen>(Screen.Capture("scan", "job_${System.currentTimeMillis()}")) }

    when (val s = screen) {
        is Screen.Capture -> CameraGate(
            onExit = onExit,
            mode = s.mode,
            jobId = s.jobId,
            onDone = { shots ->
                screen = Screen.Review(jobId = s.jobId, shots = shots)
            }
        )

        is Screen.Review -> CaptureReviewScreen(
            jobId = s.jobId,
            shots = s.shots,
            onNewCapture = {
                screen = Screen.Capture("scan", "job_${System.currentTimeMillis()}")
            },
            onExit = onExit
        )
    }
}

@Composable
private fun CameraGate(
    onExit: () -> Unit,
    mode: String,
    jobId: String,
    onDone: (List<File>) -> Unit
) {
    var hasCameraPermission by remember { mutableStateOf(false) }

    val requestPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        requestPermission.launch(Manifest.permission.CAMERA)
    }

    if (hasCameraPermission) {
        CaptureCameraScreen(
            mode = mode,
            jobId = jobId,
            onBack = onExit,
            onDone = onDone
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("GridRecon3D", style = MaterialTheme.typography.headlineSmall)
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF0D121A))) {
                Column(
                    Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Camera permission is required to capture photos.", color = Color.White)
                    Button(onClick = { requestPermission.launch(Manifest.permission.CAMERA) }) {
                        Text("Grant Camera Permission")
                    }
                    OutlinedButton(onClick = onExit) { Text("Exit") }
                }
            }
        }
    }
}
