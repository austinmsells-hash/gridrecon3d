package com.gridrecon3d

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

@Composable
fun MapScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Map") },
            navigationIcon = {
                TextButton(onClick = onBack) { Text("Back") }
            }
        )

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                // OSMDroid needs a user agent
                Configuration.getInstance().userAgentValue = ctx.packageName

                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(17.0)
                    controller.setCenter(GeoPoint(37.7749, -122.4194)) // default center; change later to GPS
                }
            }
        )
    }
}
