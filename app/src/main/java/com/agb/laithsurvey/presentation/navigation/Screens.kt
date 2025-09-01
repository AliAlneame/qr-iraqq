package com.agb.laithsurvey.presentation.navigation

sealed class Screens(val route: String) {
    data object StartScreen : Screens("StartScreen")
    data object CameraScreen : Screens("CameraScreen")
    data object HomeScreen : Screens("HomeScreen")
    data object QuotationsScreen : Screens("QuotationsScreen")
}