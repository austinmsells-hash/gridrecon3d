========================================================
GRIDRECON3D - ONE MEGA COPY/PASTE PATCH (PHONE FRIENDLY)
========================================================

HOW TO APPLY (ON PHONE, NO GUESSING):

1) In GitHub (mobile web/app):
   - Go to your repo
   - For EACH file path below:
     - Navigate to the file (or create it)
     - Replace its contents completely with the section for that file
     - Commit (you can do this in 1 commit or multiple — either is fine)

2) IMPORTANT:
   - If a file already exists, REPLACE IT ENTIRELY.
   - If it doesn’t exist, CREATE IT at the exact path.

3) After committing:
   - Run your “Build Debug APK” workflow.
   - If it’s RED, paste the log here and I’ll tell you the next step.
   - I cannot “wait and watch” GitHub for you, but you can tell me GREEN/RED.

--------------------------------------------------------
FILES INCLUDED:
- android/app/build.gradle.kts
- android/app/src/main/AndroidManifest.xml
- android/app/src/main/java/com/gridrecon3d/GridReconApp.kt
- android/app/src/main/java/com/gridrecon3d/MainActivity.kt
- android/app/src/main/java/com/gridrecon3d/navigation/AppNavHost.kt
- android/app/src/main/java/com/gridrecon3d/data/CaptureStore.kt
- android/app/src/main/java/com/gridrecon3d/ui/theme/Theme.kt
- android/app/src/main/java/com/gridrecon3d/ui/screens/HomeScreen.kt
- android/app/src/main/java/com/gridrecon3d/ui/screens/MapScreen.kt
- android/app/src/main/java/com/gridrecon3d/ui/screens/CaptureCameraScreen.kt
- android/app/src/main/java/com/gridrecon3d/ui/screens/ModelsScreen.kt
- android/app/src/main/java/com/gridrecon3d/ui/screens/SettingsScreen.kt
- android/app/src/main/res/values/strings.xml
- android/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml
- android/app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml
- android/app/src/main/res/drawable/ic_launcher_background.xml
- android/app/src/main/res/drawable/ic_launcher_foreground.xml

========================================================
FILE: android/app/build.gradle.kts
========================================================
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.gridrecon3d"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.gridrecon3d"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1-beta"
    }

    // CI uses JDK 17
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures { compose = true }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.2")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // CameraX
    implementation("androidx.camera:camera-camera2:1.3.3")
    implementation("androidx.camera:camera-lifecycle:1.3.3")
    implementation("androidx.camera:camera-view:1.3.3")

    // Biometric (optional now, later)
    implementation("androidx.biometric:biometric:1.2.0-alpha05")

    // Networking (optional, later)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Encryption for export bundles (later)
    implementation("net.lingala.zip4j:zip4j:2.11.5")

    // Map (OSM)
    implementation("org.osmdroid:osmdroid-android:6.1.20")

    // 3D viewer (later)
    implementation("io.github.sceneview:sceneview:2.2.1")
}

========================================================
FILE: android/app/src/main/AndroidManifest.xml
========================================================
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Map + tiles -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <!-- GPS (optional – map “my location” later) -->
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

    <!-- Camera -->
    <uses-permission android:name="android.permission.CAMERA" />

    <!-- Torch control -->
    <uses-feature android:name="android.hardware.camera.flash" android:required="false" />

    <application
        android:name=".GridReconApp"
        android:allowBackup="true"
        android:label="@string/app_name"
        android:icon="@mipmap/ic_launcher"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.Material.NoActionBar">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:screenOrientation="portrait">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

    </application>

</manifest>

========================================================
FILE: android/app/src/main/java/com/gridrecon3d/GridReconApp.kt
========================================================
package com.gridrecon3d

import android.app.Application
import org.osmdroid.config.Configuration
import java.io.File

class GridReconApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // OSMDroid REQUIREMENT: set user agent or tiles can be blocked / throttled.
        val cfg = Configuration.getInstance()
        cfg.userAgentValue = packageName

        // Keep cache inside app storage (no legacy storage perms needed)
        val base = File(filesDir, "osmdroid")
        val tile = File(cacheDir, "osmdroid_tiles")
        base.mkdirs()
        tile.mkdirs()
        cfg.osmdroidBasePath = base
        cfg.osmdroidTileCache = tile
    }
}

========================================================
FILE: android/app/src/main/java/com/gridrecon3d/MainActivity.kt
========================================================
package com.gridrecon3d

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.gridrecon3d.navigation.AppNavHost
import com.gridrecon3d.ui.theme.GridReconTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GridReconTheme {
                AppNavHost()
            }
        }
    }
}

========================================================
FILE: android/app/src/main/java/com/gridrecon3d/navigation/AppNavHost.kt
========================================================
package com.gridrecon3d.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gridrecon3d.ui.screens.CaptureCameraScreen
import com.gridrecon3d.ui.screens.HomeScreen
import com.gridrecon3d.ui.screens.MapScreen
import com.gridrecon3d.ui.screens.ModelsScreen
import com.gridrecon3d.ui.screens.SettingsScreen

object Routes {
    const val HOME = "home"
    const val CAPTURE = "capture"
    const val MAP = "map"
    const val MODELS = "models"
    const val SETTINGS = "settings"
}

@Composable
fun AppNavHost(nav: NavHostController = rememberNavController()) {
    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onStartCapture = { nav.navigate(Routes.CAPTURE) },
                onOpenMap = { nav.navigate(Routes.MAP) },
                onOpenModels = { nav.navigate(Routes.MODELS) },
                onOpenSettings = { nav.navigate(Routes.SETTINGS) },
            )
        }
        composable(Routes.CAPTURE) { CaptureCameraScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.MAP) { MapScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.MODELS) { ModelsScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.SETTINGS) { SettingsScreen(onBack = { nav.popBackStack() }) }
    }
}

========================================================
FILE: android/app/src/main/java/com/gridrecon3d/data/CaptureStore.kt
========================================================
package com.gridrecon3d.data

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class CaptureSession(
    val id: String,
    val createdAt: Long,
    val photoCount: Int
)

object CaptureStore {
    private const val DIR = "captures"

    fun createSession(context: Context): CaptureSession {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val folder = File(context.filesDir, "$DIR/$id")
        folder.mkdirs()
        return CaptureSession(id = id, createdAt = now, photoCount = 0)
    }

    fun sessionDir(context: Context, sessionId: String): File {
        val folder = File(context.filesDir, "$DIR/$sessionId")
        folder.mkdirs()
        return folder
    }

    fun listSessions(context: Context): List<CaptureSession> {
        val root = File(context.filesDir, DIR)
        if (!root.exists()) return emptyList()
        return root.listFiles()
            ?.filter { it.isDirectory }
            ?.map { dir ->
                val photos = dir.listFiles()?.count { it.extension.lowercase() == "jpg" } ?: 0
                CaptureSession(
                    id = dir.name,
                    createdAt = dir.lastModified(),
                    photoCount = photos
                )
            }
            ?.sortedByDescending { it.createdAt }
            ?: emptyList()
    }

    fun formatTime(ms: Long): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        return fmt.format(Date(ms))
    }
}

========================================================
FILE: android/app/src/main/java/com/gridrecon3d/ui/theme/Theme.kt
========================================================
package com.gridrecon3d.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape

private val TacticalDark = darkColorScheme(
    primary = Color(0xFFFF2D2D),
    secondary = Color(0xFFB6FF6A),
    background = Color(0xFF070A10),
    surface = Color(0xFF0B1220),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFF081009),
    onBackground = Color(0xFFE6EAF2),
    onSurface = Color(0xFFE6EAF2),
)

private val TacticalLight = lightColorScheme(
    primary = Color(0xFFFF2D2D),
    secondary = Color(0xFF3A7D1C),
    background = Color(0xFFF7F8FB),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF101318),
    onSurface = Color(0xFF101318),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10),
    small = RoundedCornerShape(14),
    medium = RoundedCornerShape(18),
    large = RoundedCornerShape(22),
    extraLarge = RoundedCornerShape(28),
)

@Composable
fun GridReconTheme(
    darkTheme: Boolean = true, // forced tactical dark by default
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme || isSystemInDarkTheme()) TacticalDark else TacticalLight
    MaterialTheme(
        colorScheme = colors,
        shapes = AppShapes,
        content = content
    )
}

========================================================
FILE: android/app/src/main/java/com/gridrecon3d/ui/screens/HomeScreen.kt
========================================================
package com.gridrecon3d.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onStartCapture: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenModels: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.background)
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = cs.surface,
                shape = RoundedCornerShape(22.dp),
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "GridRecon 3D",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Field-ready capture • map • model workflow",
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onSurface.copy(alpha = 0.8f)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Tip: Capture 60–120 photos with overlap. Low light? Torch ON.",
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurface.copy(alpha = 0.75f)
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            TacticalButton(text = "Start Capture", primary = true, onClick = onStartCapture)
            Spacer(Modifier.height(10.dp))
            TacticalButton(text = "Map", onClick = onOpenMap)
            Spacer(Modifier.height(10.dp))
            TacticalButton(text = "Models", onClick = onOpenModels)
            Spacer(Modifier.height(10.dp))
            TacticalButton(text = "Settings", onClick = onOpenSettings)

            Spacer(Modifier.weight(1f))

            Text(
                text = "GRIDRECON // BETA",
                style = MaterialTheme.typography.labelMedium,
                color = cs.onBackground.copy(alpha = 0.55f)
            )
        }
    }
}

@Composable
private fun TacticalButton(
    text: String,
    primary: Boolean = false,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = MaterialTheme.shapes.large,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (primary) cs.primary else cs.surface,
            contentColor = if (primary) cs.onPrimary else cs.onSurface,
        )
    ) {
        Text(text = text, fontWeight = FontWeight.SemiBold)
    }
}

========================================================
FILE: android/app/src/main/java/com/gridrecon3d/ui/screens/MapScreen.kt
========================================================
package com.gridrecon3d.ui.screens

import android.content.Context
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider

private enum class MapMode { MAP, DARK, SAT }

@Composable
fun MapScreen(onBack: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    var mode by remember { mutableStateOf(MapMode.DARK) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.background)
    ) {
        // Top HUD
        Surface(
            color = cs.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Back",
                    color = cs.secondary,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .clickableNoRipple { onBack() }
                )
                Text(
                    text = "Map",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.weight(1f))
                SegButton("Sat", selected = mode == MapMode.SAT) { mode = MapMode.SAT }
                Spacer(Modifier.width(8.dp))
                SegButton("Map", selected = mode == MapMode.MAP) { mode = MapMode.MAP }
                Spacer(Modifier.width(8.dp))
                SegButton("Dark", selected = mode == MapMode.DARK) { mode = MapMode.DARK }
            }
        }

        // Map body
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    buildMapView(ctx, mode)
                },
                update = { mapView ->
                    mapView.setTileSource(tileSourceFor(mode))
                    mapView.invalidate()
                }
            )

            // Subtle vignette to keep labels readable
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x22000000))
            )
        }
    }
}

private fun buildMapView(context: Context, mode: MapMode): MapView {
    val map = MapView(context)
    map.layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )

    map.setMultiTouchControls(true)
    map.setBuiltInZoomControls(false)
    map.setTileSource(tileSourceFor(mode))
    map.controller.setZoom(14.0)
    map.controller.setCenter(org.osmdroid.util.GeoPoint(37.7749, -122.4194)) /