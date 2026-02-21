package com.gridrecon3d

import android.content.Context
import android.os.Environment
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CaptureCameraScreen(
    mode: String,
    jobId: String,
    onBack: () -> Unit,
    onDone: (List<File>) -> Unit
) {
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember { PreviewView(ctx).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    var shots by remember { mutableStateOf<List<File>>(emptyList()) }
    var status by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
        cameraProviderFuture.addListener(
            {
                val cameraProvider = cameraProviderFuture.get()
                val preview = androidx.camera.core.Preview.Builder().build().apply {
                    setSurfaceProvider(previewView.surfaceProvider)
                }
                val cap = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
                imageCapture = cap

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        cap
                    )
                } catch (_: Exception) {
                    status = "Camera init failed."
                }
            },
            ContextCompat.getMainExecutor(ctx)
        )
    }

    fun captureOne() {
        val cap = imageCapture ?: return
        val outDir = File(ctx.getExternalFilesDir(null), "captures/$jobId").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
        val file = File(outDir, "IMG_$stamp.jpg")
        val output = ImageCapture.OutputFileOptions.Builder(file).build()

        status = "Capturing…"
        cap.takePicture(
            output,
            ContextCompat.getMainExecutor(ctx),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    shots = shots + file
                    status = null
                }

                override fun onError(exception: ImageCaptureException) {
                    status = "Error: ${exception.message}"
                }
            }
        )
    }

    fun undoLast() {
        if (shots.isEmpty()) return
        val last = shots.last()
        try { last.delete() } catch (_: Exception) {}
        shots = shots.dropLast(1)
    }

    val accent = Color(0xFFFF2D2D)

    Column(Modifier.fillMaxSize().background(Color.Black)) {

        // Top bar
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onBack) { Text("Back", color = Color.White) }
            Text("CAPTURE · ${mode.uppercase()}", color = accent)
            Text("Shots: ${shots.size}", color = Color.White)
        }

        // Camera preview + overlay grid
        Box(Modifier.weight(1f).fillMaxWidth()) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )

            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val c = Color(0x66FF2D2D)

                // rule-of-thirds grid
                drawLine(c, Offset(w / 3f, 0f), Offset(w / 3f, h), 1.5f)
                drawLine(c, Offset(2f * w / 3f, 0f), Offset(2f * w / 3f, h), 1.5f)
                drawLine(c, Offset(0f, h / 3f), Offset(w, h / 3f), 1.5f)
                drawLine(c, Offset(0f, 2f * h / 3f), Offset(w, 2f * h / 3f), 1.5f)

                // center reticle
                drawCircle(Color(0x99FF2D2D), radius = 10f, center = Offset(w / 2f, h / 2f))
                drawCircle(Color(0x33FF2D2D), radius = 28f, center = Offset(w / 2f, h / 2f))
            }
        }

        if (status != null) {
            Box(Modifier.fillMaxWidth().padding(12.dp)) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xAA0B0F14))) {
                    Text(status!!, color = Color.White, modifier = Modifier.padding(10.dp))
                }
            }
        }

        // Bottom controls
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = { onDone(shots) },
                enabled = shots.isNotEmpty()
            ) { Text("DONE") }

            Button(
                onClick = { captureOne() },
                modifier = Modifier.size(72.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent)
            ) { Text("•", color = Color.Black) }

            OutlinedButton(
                onClick = { undoLast() },
                enabled = shots.isNotEmpty()
            ) { Text("UNDO") }
        }

        Text(
            "Tip: 3+ photos = best results. More overlap improves detail.",
            color = Color(0xFF90A4AE),
            modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 12.dp)
        )
    }
}
