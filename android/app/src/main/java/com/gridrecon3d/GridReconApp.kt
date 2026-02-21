package com.gridrecon3d

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*

private object Routes {
    const val DASH = "dash"
    const val CAPTURE = "capture"
    const val MAP = "map"
    const val MODELS = "models"
    const val SETTINGS = "settings"
}

@Composable
fun GridReconApp() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Routes.DASH) {
        composable(Routes.DASH) {
            DashboardScreen(
                onStartCapture = { nav.navigate(Routes.CAPTURE) },
                onOpenMap = { nav.navigate(Routes.MAP) },
                onOpenModels = { nav.navigate(Routes.MODELS) },
                onOpenSettings = { nav.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.CAPTURE) {
            CaptureCameraScreen(
                mode = "scan",
                jobId = "job_${System.currentTimeMillis()}",
                onBack = { nav.popBackStack() },
                onDone = { nav.popBackStack() }
            )
        }

        composable(Routes.MAP) { MapScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.MODELS) { ModelsScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.SETTINGS) { SettingsScreen(onBack = { nav.popBackStack() }) }
    }
}
