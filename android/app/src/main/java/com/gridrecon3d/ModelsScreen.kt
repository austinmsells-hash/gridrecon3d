package com.gridrecon3d

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ModelsScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = onBack) { Text("Back") }
        Text("Models", style = MaterialTheme.typography.headlineSmall)
        Text("This will list local captures + reconstructions.", style = MaterialTheme.typography.bodyMedium)

        Card {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Next: show jobs + status (captured / uploading / processing / ready).")
                Text("Next: open Viewer (SceneView) for GLB preview.")
                Text("Next: measurements overlay on model (phase 2).")
            }
        }
    }
}
