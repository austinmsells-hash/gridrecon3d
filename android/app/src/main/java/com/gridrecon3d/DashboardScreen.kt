package com.gridrecon3d

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DashboardScreen(
    onStartCapture: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenModels: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("GridRecon3D", style = MaterialTheme.typography.headlineSmall)
            Text("Field-ready capture + map + model workflow", style = MaterialTheme.typography.bodyMedium)

            Card {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = onStartCapture, modifier = Modifier.fillMaxWidth()) {
                        Text("Start Capture")
                    }
                    OutlinedButton(onClick = onOpenMap, modifier = Modifier.fillMaxWidth()) {
                        Text("Map")
                    }
                    OutlinedButton(onClick = onOpenModels, modifier = Modifier.fillMaxWidth()) {
                        Text("Models")
                    }
                    OutlinedButton(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth()) {
                        Text("Settings")
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Text(
                "Tip: Capture 60–120 photos with overlap. Keep lighting steady.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
