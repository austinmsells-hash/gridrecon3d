package com.gridrecon3d

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun GridReconApp() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "lock") {
        composable("lock") { LockScreen(onUnlocked = { nav.navigate("consent") { popUpTo("lock"){ inclusive = true } } }) }
        composable("consent") { ConsentScreen(onAccepted = { nav.navigate("home") { popUpTo("consent"){ inclusive = true } } }) }
        composable("home") { HomeScreen(
            onNewScan = { nav.navigate("mode") },
            onModels = { nav.navigate("models") },
            onSettings = { nav.navigate("settings") }
        ) }
        composable("mode") { ModeSelectScreen(
            onBack = { nav.popBackStack() },
            onStart = { mode -> val jid = LocalJobs.newJobId(); nav.navigate("capturecam/$mode/$jid") }
        ) }
        composable("capturecam/{mode}/{jobId}") { backStack ->
                val mode = backStack.arguments?.getString("mode") ?: "object"
                val jobId = backStack.arguments?.getString("jobId") ?: "local"
                CaptureCameraScreen(mode = mode, jobId = jobId, onBack = { nav.popBackStack() }, onDone = { files ->
                    val ctx = androidx.compose.ui.platform.LocalContext.current
                LocalJobs.upsert(ctx, LocalJob(jobId = jobId, mode = mode, createdAt = System.currentTimeMillis(), status = "LOCAL", shotCount = files.size))
                    nav.navigate("uploadlocal/$jobId")
                })
            }
            
            composable("uploadlocal/{jobId}") { bs ->
                val jid = bs.arguments?.getString("jobId") ?: ""
                UploadLocalScreen(jobId = jid, onBack = { nav.popBackStack() }, onGoModels = { nav.navigate("models") })
            }

            composable("capture/{mode}") { backStack ->
            val mode = backStack.arguments?.getString("mode") ?: "object"
            CaptureScreen(mode = mode, onBack = { nav.popBackStack() }, onDone = { nav.navigate("upload") })
        }
        composable("upload") { UploadScreen(
            onBack = { nav.popBackStack() },
            onView = { nav.navigate("viewer") }
        ) }
        composable("viewer") { ViewerScreen(onBack = { nav.popBackStack() }, onExport = { nav.navigate("export") }) }
        composable("export") { ExportScreen(onBack = { nav.popBackStack() }) }
        composable("models") { ModelsScreen(onBack = { nav.popBackStack() }, onOpen = { nav.navigate("viewer") }) }
        composable("settings") { SettingsScreen(onBack = { nav.popBackStack() }) }
    }
}
