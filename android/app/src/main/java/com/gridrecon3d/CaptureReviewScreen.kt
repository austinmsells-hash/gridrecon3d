package com.gridrecon3d

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
fun CaptureReviewScreen(
    jobId: String,
    shots: List<File>,
    onNewCapture: () -> Unit,
    onExit: () -> Unit
) {
    Surface(color = Color.Black) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "CAPTURE • REVIEW",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )

            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF0D121A))) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Session: $jobId", color = Color.White)
                    Text("Shots: ${shots.size}", color = Color.White)
                    Text(
                        "Saved under app storage (captures/$jobId).",
                        color = Color(0xFF90A4AE)
                    )
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF0D121A))) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Files", color = Color.White, style = MaterialTheme.typography.titleMedium)

                    if (shots.isEmpty()) {
                        Text("No images captured.", color = Color(0xFFCF6679))
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 260.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(shots) { f ->
                                Text("• ${f.name}", color = Color.White)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onNewCapture,
                    modifier = Modifier.weight(1f)
                ) { Text("NEW CAPTURE") }

                OutlinedButton(
                    onClick = onExit,
                    modifier = Modifier.weight(1f)
                ) { Text("EXIT") }
            }
        }
    }
}
