package com.gridrecon3d

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Locale

private enum class BaseLayer { SATELLITE, READABLE, DARK, OSM }

private fun tileSource(name: String, baseUrl: String): ITileSource {
    return object : OnlineTileSourceBase(name, 0, 20, 256, ".png", arrayOf(baseUrl)) {
        override fun getTileURLString(aMapTileIndex: Long): String {
            val z = MapTileIndex.getZoom(aMapTileIndex)
            val x = MapTileIndex.getX(aMapTileIndex)
            val y = MapTileIndex.getY(aMapTileIndex)
            return String.format(Locale.US, "%s%d/%d/%d.png", baseUrl, z, x, y)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var baseLayer by remember { mutableStateOf(BaseLayer.SATELLITE) }
    var hasLocation by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { res ->
        hasLocation = (res[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
                (res[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
        mapViewRef?.let { mv -> if (hasLocation) enableGpsOverlay(mv) }
    }

    fun ensureLocationPermission() {
        val fine = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        hasLocation = fine || coarse
        if (!hasLocation) {
            permissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    LaunchedEffect(Unit) { ensureLocationPermission() }

    // SATELLITE (ArcGIS World Imagery) + Readable + Dark + OSM
    val satellite = remember {
        // NOTE: public tile endpoint; if you want enterprise/guaranteed terms later, we can switch providers.
        tileSource("ESRI_Imagery", "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/")
    }
    val readable = remember { tileSource("CartoVoyager", "https://a.basemaps.cartocdn.com/rastertiles/voyager/") }
    val dark = remember { tileSource("CartoDark", "https://a.basemaps.cartocdn.com/dark_all/") }

    fun applyLayer(mv: MapView) {
        when (baseLayer) {
            BaseLayer.SATELLITE -> mv.setTileSource(satellite)
            BaseLayer.READABLE -> mv.setTileSource(readable)
            BaseLayer.DARK -> mv.setTileSource(dark)
            BaseLayer.OSM -> mv.setTileSource(TileSourceFactory.MAPNIK)
        }
        mv.invalidate()
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Map") },
            navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
            actions = {
                TextButton(onClick = { ensureLocationPermission() }) { Text("GPS") }
            }
        )

        Box(Modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    Configuration.getInstance().userAgentValue = context.packageName
                    MapView(context).apply {
                        setMultiTouchControls(true)
                        controller.setZoom(17.0)
                        controller.setCenter(GeoPoint(37.7749, -122.4194))
                        applyLayer(this)
                        mapViewRef = this
                        if (hasLocation) enableGpsOverlay(this)
                    }
                },
                update = { mv ->
                    mapViewRef = mv
                    applyLayer(mv)
                    if (hasLocation) enableGpsOverlay(mv)
                }
            )

            // Compact HUD layer selector
            ElevatedCard(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(onClick = { baseLayer = BaseLayer.SATELLITE; mapViewRef?.let { applyLayer(it) } }, label = { Text("Sat") })
                    AssistChip(onClick = { baseLayer = BaseLayer.READABLE; mapViewRef?.let { applyLayer(it) } }, label = { Text("Map") })
                    AssistChip(onClick = { baseLayer = BaseLayer.DARK; mapViewRef?.let { applyLayer(it) } }, label = { Text("Dark") })
                    AssistChip(onClick = { baseLayer = BaseLayer.OSM; mapViewRef?.let { applyLayer(it) } }, label = { Text("OSM") })
                }
            }

            // Field controls
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FloatingActionButton(
                    onClick = { mapViewRef?.controller?.zoomIn() },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) { Text("+") }

                FloatingActionButton(
                    onClick = { mapViewRef?.controller?.zoomOut() },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) { Text("–") }

                FloatingActionButton(
                    onClick = {
                        val mv = mapViewRef ?: return@FloatingActionButton
                        val overlay = mv.overlays.firstOrNull { it is MyLocationNewOverlay } as? MyLocationNewOverlay
                        val p = overlay?.myLocation
                        if (p != null) mv.controller.animateTo(p) else mv.controller.animateTo(GeoPoint(37.7749, -122.4194))
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) { Text("◎") }
            }
        }
    }
}

private fun enableGpsOverlay(mv: MapView) {
    if (mv.overlays.any { it is MyLocationNewOverlay }) return
    val overlay = MyLocationNewOverlay(GpsMyLocationProvider(mv.context), mv)
    overlay.enableMyLocation()
    overlay.enableFollowLocation()
    mv.overlays.add(overlay)
}
