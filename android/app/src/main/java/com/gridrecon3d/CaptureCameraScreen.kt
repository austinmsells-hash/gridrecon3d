package com.gridrecon3d

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.compose.ui.viewinterop.AndroidView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureCameraScreen(
    mode: String,
    jobId: String,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCam by remember { mutableStateOf(false) }
    var shots by remember { mutableStateOf(0) }
    var torchOn by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Ready") }

    val camPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCam = granted }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        hasCam = granted
        if (!granted) camPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    var boundCamera by remember { mutableStateOf<Camera?>(null) }

    fun outputDir(context: Context): File {
        val root = File(context.filesDir, "captures/$jobId")
        root.mkdirs()
        return root
    }

    fun takePhoto() {
        val dir = outputDir(ctx)
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
        val f = File(dir, "IMG_$ts.jpg")
        val out = ImageCapture.OutputFileOptions.Builder(f).build()
        status = "Capturing…"
        imageCapture.takePicture(
            out,
            ContextCompat.getMainExecutor(ctx),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    shots += 1
                    status = "Captured $shots"
                }
                override fun onError(exception: ImageCaptureException) {
                    status = "Capture error"
                    Log.e("GridRecon3D", "Capture error", exception)
                }
            }
        )
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("CAPTURE • ${mode.uppercase()}") },
            navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
            actions = {
                Text("Shots: $shots", modifier = Modifier.padding(end = 12.dp))
            }
        )

        Box(Modifier.fillMaxSize()) {
            if (!hasCam) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Camera permission required")
                }
                return@Column
            }

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    val previewView = PreviewView(context).apply {
                        // This avoids some devices rendering black preview in certain conditions
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val selector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            boundCamera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                selector,
                                preview,
                                imageCapture
                            )
                            // Apply current torch state
                            boundCamera?.cameraControl?.enableTorch(torchOn)
                        } catch (e: Exception) {
                            Log.e("GridRecon3D", "Camera bind failed", e)
                            status = "Camera bind failed"
                        }
                    }, ContextCompat.getMainExecutor(context))

                    previewView
                },
                update = {
                    // keep torch state in sync
                    boundCamera?.cameraControl?.enableTorch(torchOn)
                }
            )

            // HUD overlay (grid + reticle like brochure)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val grid = Color(0x33FF2D2D)
                // thirds grid
                drawLine(grid, Offset(w/3f, 0f), Offset(w/3f, h), 1.5f)
                drawLine(grid, Offset(2f*w/3f, 0f), Offset(2f*w/3f, h), 1.5f)
                drawLine(grid, Offset(0f, h/3f), Offset(w, h/3f), 1.5f)
                drawLine(grid, Offset(0f, 2f*h/3f), Offset(w, 2f*h/3f), 1.5f)
                // center reticle
                val c = Offset(w/2f, h/2f)
                drawCircle(Color(0x66FF2D2D), radius = 16f, center = c)
                drawCircle(Color(0xFFFF2D2D), radius = 6f, center = c)
            }

            // Bottom control bar
            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Text(status, modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp))
                    }

                    AssistChip(
                        onClick = { torchOn = !torchOn },
                        label = { Text(if (torchOn) "Torch: ON" else "Torch: OFF") }
                    )
                }

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onDone) { Text("DONE") }

                    Button(
                        onClick = { takePhoto() },
                        contentPadding = PaddingValues(horizontal = 26.dp, vertical = 16.dp)
                    ) { Text("SCAN") }

                    OutlinedButton(
                        onClick = {
                            // simple undo: delete last file if exists
                            val dir = outputDir(ctx)
                            val last = dir.listFiles()?.filter { it.name.endsWith(".jpg") }?.sortedBy { it.name }?.lastOrNull()
                            if (last != null && last.delete()) {
                                shots = maxOf(0, shots - 1)
                                status = "Undo"
                            }
                        }
                    ) { Text("UNDO") }
                }

                Text(
                    "Tip: 60–120 photos, steady overlap. Low light? Turn Torch ON.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
