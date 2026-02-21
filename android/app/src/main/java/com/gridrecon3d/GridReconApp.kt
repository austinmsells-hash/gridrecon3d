package com.gridrecon3d

import androidx.compose.runtime.Composable
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GridReconApp() {
    Surface(modifier = Modifier.fillMaxSize()) {
        MainScreen()
    }
}
