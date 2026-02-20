package com.gridrecon3d

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

@Composable
fun MainScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("GridRecon3D", color = Color.White)

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D121A)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(12.dp)) {
                Text("Recommended Settings", color = Color.Cyan)

                Text("Aperture: f/8", color = Color.White)
                Text("ISO: 100", color = Color.White)
                Text("Shutter: 1/125", color = Color.White)
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D121A)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(12.dp)) {
                Text("Capture Plan", color = Color.Cyan)
                Text("• Walk around object", color = Color.White)
                Text("• Keep steady motion", color = Color.White)
                Text("• Overlap shots", color = Color.White)
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D121A)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(12.dp)) {
                Text("Cautions", color = Color.Cyan)
                Text("• Avoid motion blur", color = Color.White)
                Text("• Keep lighting consistent", color = Color.White)
            }
        }
    }
}
