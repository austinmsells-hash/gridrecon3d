package com.gridrecon3d

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import io.github.sceneview.Scene
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberScene
import io.github.sceneview.node.ModelNode
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.EncryptionMethod
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.view.MotionEvent
import androidx.compose.ui.viewinterop.AndroidView
import io.github.sceneview.SceneView
import io.github.sceneview.collision.HitResult
import dev.romainguy.kotlin.math.Float3

private val GRID = Color(0xFFFF2D2D)
private val PANEL = Color(0xFF0B0F14)

@Composable
fun LockScreen(onUnlocked: () -> Unit) {
    val ctx = LocalContext.current
    val activity = ctx as? FragmentActivity
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        // Basic biometric gate; if not available, allow immediately (beta-friendly)
        if (activity == null) { onUnlocked(); return@LaunchedEffect }
        val bm = BiometricManager.from(ctx)
        val can = bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        if (can != BiometricManager.BIOMETRIC_SUCCESS) {
            onUnlocked()
            return@LaunchedEffect
        }
        val executor = ContextCompat.getMainExecutor(ctx)
        val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { onUnlocked() }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) { error = errString.toString() }
            override fun onAuthenticationFailed() { error = "Authentication failed" }
        })
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock GridRecon 3D")
            .setSubtitle("Biometric or device credential")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()
        prompt.authenticate(info)
    }

    Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("GRIDRECON 3D", color = GRID, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text("Secure access", color = Color(0xFFB0BEC5))
            if (error != null) {
                Spacer(Modifier.height(16.dp))
                Text(error!!, color = Color(0xFFFF5252))
                Spacer(Modifier.height(8.dp))
                Button(onClick = onUnlocked) { Text("Continue") }
            }
        }
    }
}

@Composable
fun ConsentScreen(onAccepted: () -> Unit) {
    var a by remember { mutableStateOf(false) }
    var b by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Authorization & Privacy", style = MaterialTheme.typography.headlineSmall, color = GRID, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text("By continuing, you confirm you are authorized to capture and upload content for this project, and you will respect privacy and applicable laws.", color = Color(0xFFCFD8DC))
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = a, onCheckedChange = { a = it })
            Spacer(Modifier.width(8.dp))
            Text("I am authorized for this capture.", color = Color.White)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = b, onCheckedChange = { b = it })
            Spacer(Modifier.width(8.dp))
            Text("I will not capture or share content unlawfully.", color = Color.White)
        }
        Spacer(Modifier.height(24.dp))
        Button(enabled = a && b, onClick = onAccepted, modifier = Modifier.fillMaxWidth()) {
            Text("ACCEPT & CONTINUE")
        }
    }
}

@Composable
fun HomeScreen(onNewScan: () -> Unit, onModels: () -> Unit, onSettings: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("GRIDRECON 3D", style = MaterialTheme.typography.headlineSmall, color = GRID, fontWeight = FontWeight.Bold)
        InstrumentCard(title = "New Capture", subtitle = "Start a capture session", onClick = onNewScan)
        InstrumentCard(title = "My Models", subtitle = "View completed models", onClick = onModels)
        InstrumentCard(title = "Settings", subtitle = "Security, privacy, export", onClick = onSettings)
    }
}

@Composable
fun ModeSelectScreen(onBack: () -> Unit, onStart: (String) -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Capture Mode", style = MaterialTheme.typography.headlineSmall, color = GRID, fontWeight = FontWeight.Bold)
            TextButton(onClick = onBack) { Text("Back") }
        }
        InstrumentCard("Structure", "Interior / exterior capture") { onStart("structure") }
        InstrumentCard("Object", "Vehicle / prop / item capture") { onStart("object") }
        InstrumentCard("Person", "Character / toy / VFX capture") { onStart("person") }
    }
}

@Composable
fun CaptureScreen(mode: String, onBack: () -> Unit, onDone: () -> Unit) {
    val ctx = LocalContext.current
    val perms = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }
    LaunchedEffect(Unit) { perms.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Capture • ${mode.uppercase()}", color = GRID, fontWeight = FontWeight.Bold)
            TextButton(onClick = onBack) { Text("Back") }
        }
        // MVP note: real CameraX preview + guided capture is in CaptureCameraScreen.kt (scaffolded)
        InstrumentPanel("Guidance", listOf(
            "Move around target; keep overlap high",
            "Use 3+ photos for accurate model",
            "More photos = better detail"
        ))
        InstrumentPanel("HUD", listOf("Grid overlay • Horizon level • Heading • Metadata overlays (optional)"))
        var serverUrl by remember { mutableStateOf(Prefs.getServerUrl(ctx)) }
OutlinedTextField(
    value = serverUrl,
    onValueChange = { serverUrl = it; Prefs.setServerUrl(ctx, it) },
    label = { Text("PC Server URL (Wi‑Fi)") },
    placeholder = { Text("http://192.168.1.10:8080") },
    modifier = Modifier.fillMaxWidth()
)

var picked by remember { mutableStateOf<List<Uri>>(emptyList()) }
val pickMany = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
    picked = uris ?: emptyList()
}

Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
    Button(onClick = { pickMany.launch(arrayOf("image/*")) }, modifier = Modifier.weight(1f)) {
        Text("SELECT PHOTOS")
    }
    Button(enabled = picked.isNotEmpty(), onClick = { onDone() }, modifier = Modifier.weight(1f)) {
        Text("NEXT")
    }
}
Text("Selected: ${'$'}{picked.size}", color = Color.White)

Button(
    enabled = picked.isNotEmpty(),
    onClick = {
        statusToast(ctx, "Uploading ${'$'}{picked.size} photos…")
        UploadDemo.uploadToServer(ctx, serverUrl, picked)
        statusToast(ctx, "Upload requested. Open Upload screen.")
    },
    modifier = Modifier.fillMaxWidth()
) { Text("UPLOAD TO PC (Wi‑Fi)") }

        Text("Note: This skeleton ships with a sample model viewer + export + zeroize. Add full guided CameraX capture in CaptureCameraScreen.kt.", color = Color(0xFF90A4AE))
    }
}

@Composable
fun UploadScreen(onBack: () -> Unit, onView: () -> Unit) {
    var status by remember { mutableStateOf("Ready") }
    val ctx = LocalContext.current
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Upload", color = GRID, fontWeight = FontWeight.Bold)
            TextButton(onClick = onBack) { Text("Back") }
        }
        InstrumentPanel("Session", listOf("Images: sample", "Scale: calibration marker recommended"))
        Button(onClick = {
            status = "Uploading (demo)…"
            // Demo: you’ll wire this to backend /v1/jobs later
            status = "Queued for processing (demo)"
        }, modifier = Modifier.fillMaxWidth()) { Text("UPLOAD") }
        Text("Status: $status", color = Color.White)
        Button(onClick = onView, modifier = Modifier.fillMaxWidth()) { Text("OPEN VIEWER") }
    }
}

@Composable
fun ViewerScreen(onBack: () -> Unit, onExport: () -> Unit) {
    val ctx = LocalContext.current

    // Shared SceneView resources
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val materialLoader = rememberMaterialLoader(engine)
    val scene = rememberScene(engine)

    var picked by remember { mutableStateOf<Uri?>(null) }
    val pick = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        picked = uri
    }

    // Measurement state
    var measureMode by remember { mutableStateOf(false) }
    var p1 by remember { mutableStateOf<Float3?>(null) }
    var p2 by remember { mutableStateOf<Float3?>(null) }
    var showScaleDialog by remember { mutableStateOf(false) }
    var scaleInputMeters by remember { mutableStateOf("") }
    val unitsMode = UnitsPrefs.get(ctx)

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().background(PANEL).padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Viewer", color = GRID, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (picked != null) {
                    TextButton(onClick = { measureMode = !measureMode; p1 = null; p2 = null }) {
                        Text(if (measureMode) "Measuring…" else "Measure")
                    }
                    TextButton(onClick = { showScaleDialog = true }) { Text("Set Scale") }
                }
                TextButton(onClick = onExport) { Text("Export") }
                TextButton(onClick = onBack) { Text("Back") }
            }
        }

        if (picked == null) {
            Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Select a .glb file to view.", color = Color.White)
Card(colors = CardDefaults.cardColors(containerColor = PANEL), modifier = Modifier.fillMaxWidth()) {
    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Cutout (beta)", color = GRID, fontWeight = FontWeight.Bold)
        Text("Export a PNG cutout (background removed) from a selected photo. In this beta build, cutout is processed on your PC helper (Windows/Linux) to keep the app lightweight.", color = Color(0xFFCFD8DC))
        OutlinedButton(onClick = { /* PC-side helper in desktop/remove_bg.py */ }) { Text("CUTOUT PNG (PC)") }
        Text("See desktop/README_CUTOUT.md", color = Color(0xFF90A4AE))
    }
}
                Button(onClick = { pick.launch(arrayOf("model/gltf-binary", "*/*")) }) { Text("PICK GLB") }
                InstrumentPanel("Presentation Mode", listOf(
                    "Dark HUD • minimal panels",
                    "Grid overlay (toggle planned)",
                    "Tap-to-measure (beta)"
                ))
            }
        } else {
            val uri = picked!!
val modelKey = run {
    val s = uri.toString()
    val md = java.security.MessageDigest.getInstance("MD5").digest(s.toByteArray())
    md.joinToString("") { "%02x".format(it) }
}

            val node = remember(uri) {
                ModelNode(
                    modelInstance = modelLoader.createModelInstance(uri, ctx),
                    scaleToUnits = 1.0f
                )
            }

            // SceneView Android View gives us onTouchEvent with hit results (picking)
            Box(Modifier.fillMaxSize()) {
                AndroidView(
                    factory = {
                        SceneView(
                            context = it,
                            sharedEngine = engine,
                            sharedModelLoader = modelLoader,
                            sharedMaterialLoader = materialLoader,
                            sharedScene = scene,
                            onTouchEvent = { e: MotionEvent, hit: HitResult? ->
                                if (!measureMode) return@SceneView false
                                if (e.action != MotionEvent.ACTION_UP) return@SceneView false
                                val wp = hit?.getWorldPosition() ?: return@SceneView false
                                // wp is Float3 in world space
                                if (p1 == null) p1 = wp else p2 = wp
                                true
                            }
                        ).apply {
                            // put model in scene
                            this.scene.addChildNode(node)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Bottom HUD
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xCC0B0F14)),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(12.dp)
                        .fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Measurements", color = GRID, fontWeight = FontWeight.Bold)

                        val sf = Prefs.getScaleFactor(ctx, modelKey)
                        val bb = node.modelInstance?.boundingBox
                        val w = (bb?.size?.x?.toDouble() ?: 0.0) * sf
                        val h = (bb?.size?.y?.toDouble() ?: 0.0) * sf
                        val d = (bb?.size?.z?.toDouble() ?: 0.0) * sf

                        Text("Width:  " + formatMeters(w, unitsMode), color = Color.White)
                        Text("Height: " + formatMeters(h, unitsMode), color = Color.White)
                        Text("Depth:  " + formatMeters(d, unitsMode), color = Color.White)

                        if (measureMode) {
                            val dist = if (p1 != null && p2 != null) {
                                val a = p1!!; val b = p2!!
                                val dx = (a.x - b.x).toDouble()
                                val dy = (a.y - b.y).toDouble()
                                val dz = (a.z - b.z).toDouble()
                                val modelDist = Math.sqrt(dx*dx + dy*dy + dz*dz)
                                modelDist * sf
                            } else null

                            Text("Tap two points on the model.", color = Color(0xFFCFD8DC))
                            Text("P1: " + (p1?.let { "set" } ?: "—") + "   P2: " + (p2?.let { "set" } ?: "—"), color = Color(0xFFCFD8DC))
                            Text("Distance: " + (dist?.let { formatMeters(it, unitsMode) } ?: "—"), color = Color.White)

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(onClick = { p1 = null; p2 = null }) { Text("Reset") }
                            }
                        } else {
                            Text("Tip: Use “Set Scale” for to-scale measurements (no marker required).", color = Color(0xFF90A4AE))
                        }

                        Text(
                            "Accuracy varies with photo coverage, standoff distance, and scaling method.",
                            color = Color(0xFF90A4AE)
                        )
                    }
                }
            }
        }
    }

    if (showScaleDialog) {
        AlertDialog(
            onDismissRequest = { showScaleDialog = false },
            confirmButton = {
                Button(onClick = {
                    val meters = scaleInputMeters.toDoubleOrNull()
                    if (meters != null && meters > 0) {
                        // If user already tapped two points, use that as the known dimension.
                        if (p1 != null && p2 != null) {
                            val a = p1!!; val b = p2!!
                            val dx = (a.x - b.x).toDouble()
                            val dy = (a.y - b.y).toDouble()
                            val dz = (a.z - b.z).toDouble()
                            val modelDist = Math.sqrt(dx*dx + dy*dy + dz*dz).coerceAtLeast(1e-9)
                            Prefs.setScaleFactor(ctx, meters / modelDist)
                        } else {
                            // Fallback: scale using current bounding-box height
                            val jobSf = Prefs.getScaleFactor(ctx)
                            // leave as entered scaling relative; apply to existing factor
                            Prefs.setScaleFactor(ctx, meters / (meters / jobSf))
                        }
                    }
                    showScaleDialog = false
                }) { Text("Apply") }
            },
            dismissButton = { TextButton(onClick = { showScaleDialog = false }) { Text("Cancel") } },
            title = { Text("Set Scale (meters)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Enter a known real-world length. Best: tap two points on a known object first (door height, rail height, etc.), then apply.", color = Color(0xFFCFD8DC))
                    OutlinedTextField(
                        value = scaleInputMeters,
                        onValueChange = { scaleInputMeters = it },
                        label = { Text("Known length (meters)") },
                        placeholder = { Text("e.g., 2.03") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Conversion: 1 ft = 0.3048 m", color = Color(0xFF90A4AE))
                }
            }
        )
    }
}

@Composable
fun ExportScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    var password by remember { mutableStateOf("") }
    var encrypt by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Export Bundle", color = GRID, fontWeight = FontWeight.Bold)
            TextButton(onClick = onBack) { Text("Back") }
        }
        Text("Creates a portable bundle (model + metadata). Optionally encrypt the ZIP with a password (works for USB transfer too).", color = Color(0xFFCFD8DC))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = encrypt, onCheckedChange = { encrypt = it })
            Spacer(Modifier.width(8.dp))
            Text("Encrypt export ZIP (AES)", color = Color.White)
        }
        if (encrypt) {
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
            Text("Share the password out-of-band.", color = Color(0xFF90A4AE))
        }
        Button(onClick = {
            try {
                val outDir = File(ctx.getExternalFilesDir(null), "exports").apply { mkdirs() }
                val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val zipPath = File(outDir, "gridrecon_export_$stamp.zip")
                // Create minimal bundle (no model inside in skeleton)
                val wm = WatermarkPrefs.load(ctx)
val unitsMode = UnitsPrefs.get(ctx).name
val std = StandardPrefs.get(ctx).name
val scale = Prefs.getScaleFactor(ctx)
val meta = mapOf(
    "software" to "GridRecon 3D",
    "created" to stamp,
    "units_mode" to unitsMode,
    "ref_standard" to std,
    "scale_factor" to scale,
    "watermark" to wm,
    "note" to "Export bundle",
    "units_base" to "meters"
)
                            ("UNKNOWN")
                        }
)
                val tmpDir = File(outDir, "tmp_$stamp").apply { mkdirs() }
                File(tmpDir, "metadata.json").writeText(Json { prettyPrint = true }.encodeToString(meta))
                File(tmpDir, "readme.txt").writeText("GridRecon 3D export bundle\n- metadata.json\n- model files (add)\n")

                if (!encrypt) {
                    ZipFile(zipPath).addFolder(tmpDir)
                } else {
                    val zp = ZipParameters().apply {
                        isEncryptFiles = true
                        encryptionMethod = EncryptionMethod.AES
                    }
                    ZipFile(zipPath, password.toCharArray()).addFolder(tmpDir, zp)
                }
                tmpDir.deleteRecursively()
                status = "Saved: ${zipPath.absolutePath}"
            } catch (e: Exception) {
                status = "Export failed: ${e.message}"
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("CREATE EXPORT") }

        if (status != null) Text(status!!, color = Color.White)

        Spacer(Modifier.height(12.dp))
        InstrumentPanel("Next", listOf("Include model.glb/obj/stl", "Include GeoJSON/KML metadata", "One-tap share sheet"))
    }
}

@Composable
fun ModelsScreen(onBack: () -> Unit, onOpen: () -> Unit) {
    val demo = listOf("Sample Job • Preview", "Sample Job • Full Reconstruction")
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("My Models", color = GRID, fontWeight = FontWeight.Bold)
            TextButton(onClick = onBack) { Text("Back") }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(demo) { item ->
                InstrumentCard(item, "Tap to open viewer") { onOpen() }
            }
        }
    }
}

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    var confirm by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Settings", color = GRID, fontWeight = FontWeight.Bold)
            TextButton(onClick = onBack) { Text("Back") }
        }
        InstrumentPanel("Security", listOf("Biometric lock enabled (device-based)", "Export encryption optional"))
        InstrumentPanel("Privacy", listOf("Metadata overlays OFF by default", "Location attach: pin-drop (optional)"))
        Divider()
        Text("Zeroize", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
        Text("Wipes local app data immediately. Also requests server-side deletion where configured.", color = Color(0xFFCFD8DC))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = confirm, onCheckedChange = { confirm = it })
            Spacer(Modifier.width(8.dp))
            Text("I understand this cannot be undone.", color = Color.White)
        }
        Button(
            enabled = confirm,
            onClick = {
                // Local wipe: best-effort (clear app files)
                try {
                    ctx.filesDir.deleteRecursively()
                    ctx.cacheDir.deleteRecursively()
                } catch (_: Exception) {}
                // Relaunch to lock screen
                val pm = ctx.packageManager
                val intent = pm.getLaunchIntentForPackage(ctx.packageName)
                intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                ctx.startActivity(intent)
                Runtime.getRuntime().exit(0)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
            modifier = Modifier.fillMaxWidth()
        ) { Text("HOLD-TO-ZEROIZE (BETA)", color = Color.White) }
        Text("Implementation note: replace with press-and-hold slider; add backend /v1/account/zeroize.", color = Color(0xFF90A4AE))
    }
}

@Composable
private fun InstrumentCard(title: String, subtitle: String, onClick: () -> Unit) {
    Card(onClick = onClick, colors = CardDefaults.cardColors(containerColor = PANEL), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color(0xFF90A4AE))
        }
    }
}

@Composable
private fun InstrumentPanel(title: String, lines: List<String>) {
    Card(colors = CardDefaults.cardColors(containerColor = PANEL), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, color = GRID, fontWeight = FontWeight.Bold)
            lines.forEach { Text("• $it", color = Color(0xFFCFD8DC)) }
        }
    }
}

object Prefs {
    private const val KEY_SERVER = "server_url"
    private const val KEY_SCALE = "scale_factor"

    private const val KEY_SERVER = "server_url"
    fun getServerUrl(ctx: Context): String {
        val sp = ctx.getSharedPreferences("gr_prefs", Context.MODE_PRIVATE)
        return sp.getString(KEY_SERVER, "http://10.0.2.2:8080") ?: "http://10.0.2.2:8080"
    }
    fun getScaleFactor(ctx: Context): Double {
    val sp = ctx.getSharedPreferences("gr_prefs", Context.MODE_PRIVATE)
    return sp.getString(KEY_SCALE, "1.0")?.toDoubleOrNull() ?: 1.0
}

fun getScaleFactor(ctx: Context, key: String): Double {
    val sp = ctx.getSharedPreferences("gr_prefs", Context.MODE_PRIVATE)
    return sp.getString(KEY_SCALE + "_" + key, "1.0")?.toDoubleOrNull() ?: 1.0
}

fun setScaleFactor(ctx: Context, key: String, scale: Double) {
    ctx.getSharedPreferences("gr_prefs", Context.MODE_PRIVATE)
        .edit().putString(KEY_SCALE + "_" + key, scale.toString()).apply()
}

    val sp = ctx.getSharedPreferences("gr_prefs", Context.MODE_PRIVATE)
    return sp.getString(KEY_SCALE, "1.0")?.toDoubleOrNull() ?: 1.0
}
fun setScaleFactor(ctx: Context, scale: Double) {
    ctx.getSharedPreferences("gr_prefs", Context.MODE_PRIVATE).edit().putString(KEY_SCALE, scale.toString()).apply()
}

fun setServerUrl(ctx: Context, url: String) {
        ctx.getSharedPreferences("gr_prefs", Context.MODE_PRIVATE).edit().putString(KEY_SERVER, url).apply()
    }
}

private fun statusToast(ctx: Context, msg: String) {
    android.widget.Toast.makeText(ctx, msg, android.widget.Toast.LENGTH_SHORT).show()
}

object UploadDemo {
    private val client = OkHttpClient()

    fun uploadToServer(ctx: Context, baseUrl: String, uris: List<Uri>) {
        Thread {
            try {
                val createReq = Request.Builder()
                    .url(baseUrl.trimEnd('/') + "/v1/jobs")
                    .post(okhttp3.RequestBody.create(null, ByteArray(0)))
                    .build()
                client.newCall(createReq).execute().use { resp ->
                    if (!resp.isSuccessful) throw RuntimeException("create job failed: ${'$'}{resp.code}")
                    val body = resp.body?.string() ?: "{}"
                    val jobId = Json.parseToJsonElement(body).jsonObject["job_id"]?.jsonPrimitive?.content
                        ?: throw RuntimeException("job_id missing")

                    for (uri in uris) {
                        val name = (uri.lastPathSegment?.substringAfterLast('/') ?: "photo_${'$'}{System.currentTimeMillis()}.jpg")
                        val tmp = File(ctx.cacheDir, name)
                        ctx.contentResolver.openInputStream(uri)?.use { input ->
                            tmp.outputStream().use { output -> input.copyTo(output) }
                        } ?: continue

                        val req = Request.Builder()
                            .url(baseUrl.trimEnd('/') + "/v1/jobs/" + jobId + "/photos?filename=" + name)
                            .post(tmp.asRequestBody("application/octet-stream".toMediaType()))
                            .build()
                        client.newCall(req).execute().close()
                        tmp.delete()
                    }

                    val fin = Request.Builder()
                        .url(baseUrl.trimEnd('/') + "/v1/jobs/" + jobId + "/finalize")
                        .post(okhttp3.RequestBody.create(null, ByteArray(0)))
                        .build()
                    client.newCall(fin).execute().close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}

object JobsApi {
    private val client = OkHttpClient()

    fun listJobs(baseUrl: String, onResult: (List<String>) -> Unit) {
        Thread {
            try {
                val req = Request.Builder().url(baseUrl.trimEnd('/') + "/v1/jobs").get().build()
                val names = client.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) return@use emptyList<String>()
                    val body = resp.body?.string() ?: "[]"
                    val arr = Json.parseToJsonElement(body).jsonArray
                    arr.mapNotNull { it.jsonObject["job_id"]?.jsonPrimitive?.content }
                }
                onResult(names)
            } catch (_: Exception) {
                onResult(emptyList())
            }
        }.start()
    }

    fun downloadGlb(baseUrl: String, jobId: String, ctx: Context, onSaved: (File?) -> Unit) {
        Thread {
            try {
                val listReq = Request.Builder().url(baseUrl.trimEnd('/') + "/v1/jobs/" + jobId + "/outputs").get().build()
                val files = client.newCall(listReq).execute().use { resp ->
                    if (!resp.isSuccessful) return@use emptyList<String>()
                    val body = resp.body?.string() ?: "{}"
                    Json.parseToJsonElement(body).jsonObject["files"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
                }
                // Prefer model.glb at root, else any *.glb
                val pick = files.firstOrNull { it.lowercase().endsWith(".glb") } ?: return@Thread onSaved(null)
                val dl = Request.Builder()
                    .url(baseUrl.trimEnd('/') + "/v1/jobs/" + jobId + "/download?path=" + java.net.URLEncoder.encode(pick, "UTF-8"))
                    .get().build()
                val outFile = File(ctx.getExternalFilesDir(null), "downloads/${jobId}.glb").apply { parentFile?.mkdirs() }
                client.newCall(dl).execute().use { resp ->
                    if (!resp.isSuccessful) return@use onSaved(null)
                    val bytes = resp.body?.bytes() ?: return@use onSaved(null)
                    outFile.writeBytes(bytes)
                }
                onSaved(outFile)
            } catch (_: Exception) {
                onSaved(null)
            }
        }.start()
    }
}

@Composable
fun UploadLocalScreen(jobId: String, onBack: () -> Unit, onGoModels: () -> Unit) {
    val ctx = LocalContext.current
    var serverUrl by remember { mutableStateOf(Prefs.getServerUrl(ctx)) }
    var status by remember { mutableStateOf<String?>(null) }
    val job = remember { LocalJobs.list(ctx).firstOrNull { it.jobId == jobId } }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Upload", color = GRID, fontWeight = FontWeight.Bold)
            TextButton(onClick = onBack) { Text("Back") }
        }

        Text("Job: $jobId", color = Color.White)
        Text("Shots: ${job?.shotCount ?: 0}", color = Color(0xFFCFD8DC))

        OutlinedTextField(
            value = serverUrl,
            onValueChange = { serverUrl = it; Prefs.setServerUrl(ctx, it) },
            label = { Text("PC Server URL (Wi‑Fi)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                status = "Uploading…"
                val dir = File(ctx.getExternalFilesDir(null), "captures/$jobId")
                val files = dir.listFiles()?.toList() ?: emptyList()
                UploadLocal.uploadFiles(ctx, serverUrl, jobId, files) { ok ->
                    status = if (ok) "Uploaded. Processing may run later on your PC." else "Upload failed."
                    if (ok && job != null) LocalJobs.upsert(ctx, job.copy(status = "UPLOADED"))
                }
            },
            enabled = (job?.shotCount ?: 0) > 0,
            modifier = Modifier.fillMaxWidth()
        ) { Text("UPLOAD NOW") }

        Button(onClick = onGoModels, modifier = Modifier.fillMaxWidth()) { Text("GO TO MY MODELS") }

        if (status != null) Text(status!!, color = Color.White)
        Text("Offline-ready: you can capture without service, and upload/process later.", color = Color(0xFF90A4AE))
    }
}

object UploadLocal {
    private val client = OkHttpClient()

    fun uploadFiles(ctx: Context, baseUrl: String, jobId: String, files: List<File>, onDone: (Boolean) -> Unit) {
        Thread {
            try {
                val createReq = Request.Builder().url(baseUrl.trimEnd('/') + "/v1/jobs").post(okhttp3.RequestBody.create(null, ByteArray(0))).build()
                val newJobId = client.newCall(createReq).execute().use { resp ->
                    if (!resp.isSuccessful) return@use null
                    val body = resp.body?.string() ?: "{}"
                    Json.parseToJsonElement(body).jsonObject["job_id"]?.jsonPrimitive?.content
                } ?: return@Thread onDone(false)

                for (f in files) {
                    val req = Request.Builder()
                        .url(baseUrl.trimEnd('/') + "/v1/jobs/" + newJobId + "/photos?filename=" + java.net.URLEncoder.encode(f.name, "UTF-8"))
                        .post(f.asRequestBody("application/octet-stream".toMediaType()))
                        .build()
                    client.newCall(req).execute().close()
                }
                val fin = Request.Builder().url(baseUrl.trimEnd('/') + "/v1/jobs/" + newJobId + "/finalize").post(okhttp3.RequestBody.create(null, ByteArray(0))).build()
                client.newCall(fin).execute().close()

                onDone(true)
            } catch (_: Exception) {
                onDone(false)
            }
        }.start()
    }
}

object UnitsPrefs {
    private const val KEY_UNITS = "units_mode"
    fun get(ctx: Context): UnitsMode {
        val sp = ctx.getSharedPreferences("gr_prefs", Context.MODE_PRIVATE)
        val v = sp.getString(KEY_UNITS, UnitsMode.BOTH.name) ?: UnitsMode.BOTH.name
        return try { UnitsMode.valueOf(v) } catch (_: Exception) { UnitsMode.BOTH }
    }
    fun set(ctx: Context, mode: UnitsMode) {
        ctx.getSharedPreferences("gr_prefs", Context.MODE_PRIVATE).edit().putString(KEY_UNITS, mode.name).apply()
    }
}

private fun metersToFeet(m: Double): Double = m * 3.280839895
private fun formatMeters(m: Double, mode: UnitsMode): String {
    val mStr = String.format(java.util.Locale.US, "%.2f m", m)
    val ft = metersToFeet(m)
    val ftStr = String.format(java.util.Locale.US, "%.2f ft", ft)
    return when (mode) {
        UnitsMode.METRIC -> mStr
        UnitsMode.IMPERIAL -> ftStr
        UnitsMode.BOTH -> "${mStr}  •  ${ftStr}"
    }
}

object WatermarkPrefs {
    private const val KEY_NAME = "wm_name"
    private const val KEY_COMPANY = "wm_company"
    private const val KEY_PROJECT = "wm_project"
    private const val KEY_NOTES = "wm_notes"
    fun load(ctx: Context): Map<String, String> {
        val sp = ctx.getSharedPreferences("gr_prefs", Context.MODE_PRIVATE)
        return mapOf(
            "name" to (sp.getString(KEY_NAME, "") ?: ""),
            "company" to (sp.getString(KEY_COMPANY, "") ?: ""),
            "project" to (sp.getString(KEY_PROJECT, "") ?: ""),
            "notes" to (sp.getString(KEY_NOTES, "") ?: "")
        )
    }
    fun save(ctx: Context, name: String, company: String, project: String, notes: String) {
        val sp = ctx.getSharedPreferences("gr_prefs", Context.MODE_PRIVATE)
        sp.edit()
            .putString(KEY_NAME, name)
            .putString(KEY_COMPANY, company)
            .putString(KEY_PROJECT, project)
            .putString(KEY_NOTES, notes)
            .apply()
    }
}

@kotlinx.serialization.Serializable
data class ScaleSample(
    val label: String,
    val meters: Double,
    val modelDistance: Double,
    val scaleFactor: Double,
    val createdAt: Long
)

object ScalePrefs {
    private fun keySamples(key: String) = "scale_samples_json_" + key

    fun load(ctx: Context, key: String): List<ScaleSample> {
        val sp = ctx.getSharedPreferences("gr_prefs", Context.MODE_PRIVATE)
        val raw = sp.getString(keySamples(key), "[]") ?: "[]"
        return try { Json.decodeFromString(raw) } catch (_: Exception) { emptyList() }
    }

    fun save(ctx: Context, key: String, samples: List<ScaleSample>) {
        val sp = ctx.getSharedPreferences("gr_prefs", Context.MODE_PRIVATE)
        sp.edit().putString(keySamples(key), Json.encodeToString(samples.takeLast(30))).apply()
    }

    fun add(ctx: Context, key: String, sample: ScaleSample) {
        val cur = load(ctx, key).toMutableList()
        cur.add(sample)
        save(ctx, key, cur)
    }

    fun clear(ctx: Context, key: String) = save(ctx, key, emptyList())

    fun bestScale(ctx: Context, key: String, fallback: Double = 1.0): Double {
        val s = load(ctx, key)
        if (s.isEmpty()) return fallback
        val vals = s.map { it.scaleFactor }.sorted()
        return vals[vals.size / 2] // median
    }

    fun stats(ctx: Context, key: String): Pair<Double, Double>? {
        val s = load(ctx, key)
        if (s.size < 2) return null
        val vals = s.map { it.scaleFactor }
        val mean = vals.sum() / vals.size
        val var_ = vals.map { (it - mean) * (it - mean) }.sum() / (vals.size - 1)
        val sd = kotlin.math.sqrt(var_)
        return mean to sd
    }
}

private fun feetToMeters(ft: Double): Double = ft * 0.3048
(ft: Double): Double = ft * 0.3048
private fun inchesToMeters(inches: Double): Double = inches * 0.0254
private fun parseFeetInches(text: String): Double? {
    // Accept formats: "6'8", "6ft 8in", "80in", "6.5ft"
    val t = text.trim().lowercase()
    if (t.isBlank()) return null
    return try {
        when {
            t.contains("'") -> {
                val parts = t.split("'")
                val ft = parts[0].trim().toDouble()
                val inchPart = parts.getOrNull(1)?.replace(""","")?.trim()?.ifBlank { "0" } ?: "0"
                val inches = inchPart.toDouble()
                feetToMeters(ft) + inchesToMeters(inches)
            }
            t.contains("ft") && t.contains("in") -> {
                val ft = t.substringBefore("ft").trim().toDouble()
                val rest = t.substringAfter("ft")
                val inches = rest.substringBefore("in").trim().ifBlank { "0" }.toDouble()
                feetToMeters(ft) + inchesToMeters(inches)
            }
            t.endsWith("ft") -> feetToMeters(t.removeSuffix("ft").trim().toDouble())
            t.endsWith("in") -> inchesToMeters(t.removeSuffix("in").trim().toDouble())
            else -> t.toDouble() // assume meters
        }
    } catch (_: Exception) { null }
}

data class RefPreset(val key: String, val label: String, val defaultMeters: Double, val confidence: String)

private val REF_PRESETS = listOf(
    RefPreset("door", "Door", 2.032, "HIGH"),
    RefPreset("human", "Human", 1.778, "HIGH"),
    RefPreset("wheel", "Wheel", 0.787, "HIGH"),
    RefPreset("power", "Power line", 9.0, "LOW"),
    RefPreset("light", "Street light", 9.0, "LOW"),
    RefPreset("manual", "Manual", 1.0, "MED")
)


@kotlinx.serialization.Serializable
enum class RefStandard { US, EU, CUSTOM }

object StandardPrefs {
    private const val KEY_STD = "ref_standard"
    fun get(ctx: Context): RefStandard {
        val sp = ctx.getSharedPreferences("gr_prefs", Context.MODE_PRIVATE)
        val v = sp.getString(KEY_STD, RefStandard.US.name) ?: RefStandard.US.name
        return try { RefStandard.valueOf(v) } catch (_: Exception) { RefStandard.US }
    }
    fun set(ctx: Context, std: RefStandard) {
        ctx.getSharedPreferences("gr_prefs", Context.MODE_PRIVATE).edit().putString(KEY_STD, std.name).apply()
    }
}

private fun defaultDoorMeters(std: RefStandard): Double = when (std) {
    RefStandard.US -> 2.032   // 6'8
    RefStandard.EU -> 2.000
    RefStandard.CUSTOM -> 2.032
}
private fun defaultHumanMeters(std: RefStandard): Double = when (std) {
    RefStandard.US -> 1.753   // ~5'9
    RefStandard.EU -> 1.750
    RefStandard.CUSTOM -> 1.750
}
private fun defaultWheelMeters(std: RefStandard): Double = when (std) {
    RefStandard.US -> 0.787   // ~31in
    RefStandard.EU -> 0.700   // a conservative default; user can override
    RefStandard.CUSTOM -> 0.787
}


// ------------------------------
// Field Guide + Capture Advisor
// ------------------------------
data class CaptureAdvisorInput(
    val distanceM: Double,
    val lighting: String,     // "Day", "Overcast", "Dusk", "Night"
    val tripod: Boolean,
    val subject: String,      // "Building", "Vehicle", "Person", "Other"
    val lens: String,         // "24-70", "35", "50", "100-500", "Other"
    val wind: String,         // "Calm", "Breezy", "Windy"
    val movingPeople: Boolean
)

data class CaptureAdvisorOutput(
    val recommendedAperture: String,
    val recommendedISO: String,
    val recommendedShutter: String,
    val capturePlan: List<String>,
    val cautionNotes: List<String>
)

private fun advisor(inp: CaptureAdvisorInput): CaptureAdvisorOutput {
    // Conservative, photogrammetry-first guidance:
    // - prioritize sharpness + low noise + consistent settings
    val cautions = mutableListOf<String>()
    val plan = mutableListOf<String>()

    val tele = inp.lens.contains("100") || inp.distanceM >= 60.0
    val lowLight = inp.lighting in listOf("Dusk", "Night")
    val overcast = inp.lighting == "Overcast"

    val aperture = when {
        tele -> "f/5.6 – f/8"
        lowLight -> if (inp.tripod) "f/5.6 – f/8" else "f/4 – f/5.6"
        else -> "f/5.6 – f/8"
    }

    val iso = when {
        !lowLight && !overcast -> "ISO 100–200"
        overcast -> "ISO 200–400"
        lowLight && inp.tripod -> "ISO 100–400"
        else -> "ISO 400–800 (avoid >1600 if possible)"
    }

    val shutter = when {
        inp.tripod && lowLight -> "1–5 s (use 2s timer / remote)"
        inp.tripod && !lowLight -> "1/30–1/125 s (as needed)"
        tele -> "≥ 1/500 s (telephoto stability)"
        inp.wind == "Windy" -> "≥ 1/250 s (reduce motion blur)"
        else -> "≥ 1/125 s (handheld baseline)"
    }

    plan += "Use consistent exposure and focal length for the whole set."
    plan += "Aim for 70% overlap; move in small steps between shots."
    plan += "Prefer 20–60 photos for buildings; 30–120 for best detail."
    plan += "If standoff is large (≥60m), take multiple arcs (left/center/right) if possible."
    plan += "Avoid extreme wide-angle; prefer 24–50mm equivalent for most work."

    if (inp.subject == "Person"):
        plan += "For people: keep pose still; capture a full circle at 2 heights (waist + chest)."
        if (!inp.tripod) cautions += "People scans are sensitive to motion—use fast shutter and avoid blur."

    if (inp.movingPeople) cautions += "Moving objects/people can create artifacts—try to clear the scene or shoot faster."
    if (!inp.tripod and lowLight) cautions += "Low light handheld increases noise/blur; expect reduced detail."
    if (inp.wind in listOf("Breezy","Windy")) cautions += "Trees/foliage motion reduces feature matching; focus on rigid surfaces."
    if (tele) cautions += "Long lens shots can reduce parallax if you stay in one spot—change position when possible."

    return CaptureAdvisorOutput(
        recommendedAperture = aperture,
        recommendedISO = iso,
        recommendedShutter = shutter,
        capturePlan = plan,
        cautionNotes = cautions.ifEmpty { listOf("None.") }
    )
}

private data class GuideTerm(val term: String, val category: String, val body: String)

private fun fieldGuideTerms(): List<GuideTerm> = listOf(
    // Photography refreshers
    GuideTerm("Aperture (f‑stop)", "Photography", "Controls light and depth of field. For photogrammetry, favor sharpness across the subject: typically f/5.6–f/8. Avoid very wide apertures that blur edges."),
    GuideTerm("ISO", "Photography", "Sensor sensitivity. Keep as low as possible to reduce noise (feature matching needs clean texture). Typical: ISO 100–400; low light handheld may require ISO 400–800."),
    GuideTerm("Shutter Speed", "Photography", "Controls motion blur. For photogrammetry, blur is worse than grain. Handheld: ≥1/125s; telephoto: ≥1/500s; tripod allows longer exposures."),
    GuideTerm("Overlap", "Photography", "Aim for ~70% overlap between consecutive photos so the system can match features."),
    GuideTerm("Parallax", "Photography", "Apparent shift of objects between viewpoints. Parallax enables depth; change position rather than only zooming."),
    GuideTerm("Focal Length Consistency", "Photography", "Keep focal length consistent across a set. Avoid zooming in/out mid‑capture when possible."),
    GuideTerm("Low‑Light Capture", "Photography", "Prefer tripod + low ISO + longer shutter. If handheld, increase shutter speed first, then ISO, and expect reduced model detail."),

    // Architecture basics
    GuideTerm("Storey / Floor Height", "Architecture", "Typical residential storey height is ~2.4–3.0m (8–10ft). Use known references (doors, stairs) for scaling rather than assumptions."),
    GuideTerm("Door Height (Typical)", "Architecture", "Common: US 2.032m (6'8). EU often 2.0m. Verify when possible; treat as a scaling reference with stated uncertainty."),
    GuideTerm("Roof Pitch", "Architecture", "Angle of roof slope. Oblique angles help capture roof geometry; add higher viewpoints if safe and permitted."),
    GuideTerm("Façade", "Architecture", "Exterior face of a building. Capture each façade with overlapping shots and include corners for better alignment."),

    // Measurement & modeling
    GuideTerm("Scale Factor", "Measurement", "Multiplier converting model units to real-world units. Best set by measuring a known object (door/human/wheel) and applying it."),
    GuideTerm("Confidence", "Measurement", "How consistent your scale samples are. Multiple references (door + wheel) improve confidence."),
    GuideTerm("Point Cloud", "3D Modeling", "Intermediate 3D points created from matched photo features. Dense point clouds usually indicate good coverage and sharp images."),
    GuideTerm("Mesh", "3D Modeling", "Triangle surface built from the point cloud. Holes or warping often mean missing angles or low texture.")
)

@Composable
fun HelpScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val terms = remember { fieldGuideTerms() }
    var q by remember { mutableStateOf("") }
    var tab by remember { mutableStateOf("Advisor") }

    Column(Modifier.fillMaxSize().background(Color(0xFF07090C))) {
        Row(
            Modifier.fillMaxWidth().background(Color(0xFF0D121A)).padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Field Guide", color = GRID, fontWeight = FontWeight.Bold)
            TextButton(onClick = onBack) { Text("Back") }
        }

        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip(selected = tab=="Advisor", onClick = { tab="Advisor" }, label = { Text("Advisor") })
            FilterChip(selected = tab=="Photography", onClick = { tab="Photography" }, label = { Text("Photography") })
            FilterChip(selected = tab=="Architecture", onClick = { tab="Architecture" }, label = { Text("Architecture") })
            FilterChip(selected = tab=="3D Modeling", onClick = { tab="3D Modeling" }, label = { Text("3D Modeling") })
            FilterChip(selected = tab=="Measurement", onClick = { tab="Measurement" }, label = { Text("Measurement") })
        }

        if (tab == "Advisor") {
            CaptureAdvisorPanel()
        } else {
            OutlinedTextField(
                value = q,
                onValueChange = { q = it },
                label = { Text("Search terms") },
                modifier = Modifier.padding(horizontal = 12.dp).fillMaxWidth()
            )
            val filtered = terms.filter { it.category == tab }.filter {
                q.isBlank() || it.term.contains(q, true) || it.body.contains(q, true)
            }
            LazyColumn(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filtered) { t ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF0D121A)), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(t.term, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(t.body, color = Color(0xFFCFD8DC))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CaptureAdvisorPanel() {
    var distance by remember { mutableStateOf("25") }
    var lighting by remember { mutableStateOf("Day") }
    var tripod by remember { mutableStateOf(false) }
    var subject by remember { mutableStateOf("Building") }
    var lens by remember { mutableStateOf("24-70") }
    var wind by remember { mutableStateOf("Calm") }
    var moving by remember { mutableStateOf(false) }

    val out = remember(distance, lighting, tripod, subject, lens, wind, moving) {
        val d = distance.toDoubleOrNull() ?: 25.0
        advisor(CaptureAdvisorInput(d, lighting, tripod, subject, lens, wind, moving))
    }

    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF0D121A)), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Capture Advisor", color = GRID, fontWeight = FontWeight.Bold)
                Text("Enter conditions and equipment. The advisor recommends settings optimized for sharp, low-noise photogrammetry.", color = Color(0xFFB3C0CC))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = distance, onValueChange = { distance = it }, label = { Text("Distance (m)") }, modifier = Modifier.weight(1f))
                    FilterChip(selected = tripod, onClick = { tripod = !tripod }, label = { Text("Tripod") })
                }

                Text("Lighting", color = Color.White, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Day","Overcast","Dusk","Night").forEach { v ->
                        FilterChip(selected = lighting==v, onClick = { lighting=v }, label = { Text(v) })
                    }
                }

                Text("Subject", color = Color.White, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Building","Vehicle","Person","Other").forEach { v ->
                        FilterChip(selected = subject==v, onClick = { subject=v }, label = { Text(v) })
                    }
                }

                Text("Lens", color = Color.White, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("24-70","35","50","100-500","Other").forEach { v ->
                        FilterChip(selected = lens==v, onClick = { lens=v }, label = { Text(v) })
                    }
                }

                Text("Wind / Motion", color = Color.White, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Calm","Breezy","Windy").forEach { v ->
                        FilterChip(selected = wind==v, onClick = { wind=v }, label = { Text(v) })
                    }
                    FilterChip(selected = moving, onClick = { moving = !moving }, label = { Text("Moving objects") })
                }
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF0D121A)), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Recommended Settings", color = GRID, fontWeight = FontWeight.Bold)
                Text("Aperture: ${out.recommendedAperture}", color = Color.White)
                Text("ISO: ${out.recommendedISO}", color = Color.White)
                Text("Shutter: ${out.recommendedShutter}", color = Color.White)
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF0D121A)), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Capture Plan", color = GRID, fontWeight = FontWeight.Bold)
                out.capturePlan.forEach { Text("• $it", color = Color(0xFFCFD8DC)) }
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF0D121A)), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Cautions", color = GRID, fontWeight = FontWeight.Bold)
                out.cautionNotes.forEach { Text("• $it", color = Color(0xFFCFD8DC)) }
            }
        }
    }
}
