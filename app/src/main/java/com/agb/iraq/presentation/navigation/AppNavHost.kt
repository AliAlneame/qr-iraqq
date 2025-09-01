package com.agb.iraq.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import com.agb.iraq.presentation.qrCamera.CameraViewModel
import com.agb.iraq.presentation.ui.viewmodel.HomeViewModel

@Composable
fun AppNavHost() {
    val navController = LocalNavigationProvider.current
    val cameraViewModel: CameraViewModel = hiltViewModel()
    val homeViewModel: HomeViewModel = hiltViewModel()
    NavHost(
        navController = navController,
        startDestination = Graph.LANDING,
    ) {
        erpNavGraph(navController, cameraViewModel, homeViewModel)
    }
}

object Graph {
    const val LANDING = "landing page"
}
