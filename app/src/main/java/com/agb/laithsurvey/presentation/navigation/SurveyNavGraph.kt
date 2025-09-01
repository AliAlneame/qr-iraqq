package com.agb.laithsurvey.presentation.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.agb.laithsurvey.presentation.qrCamera.BarcodeScannerScreen
import com.agb.laithsurvey.presentation.qrCamera.CameraViewModel
import com.agb.laithsurvey.presentation.shared.StartScreen
import com.agb.laithsurvey.presentation.ui.HomeScreen
import com.agb.laithsurvey.presentation.ui.QuotationsScreen
import com.agb.laithsurvey.presentation.ui.model.ErpType
import com.agb.laithsurvey.presentation.ui.viewmodel.HomeViewModel

fun NavGraphBuilder.erpNavGraph(
    navController: NavHostController,
    cameraViewModel: CameraViewModel,
    homeViewModel: HomeViewModel
) {
    navigation(
        startDestination = Screens.StartScreen.route,
        route = Graph.LANDING
    ) {

        composable(
            route = "${Screens.HomeScreen.route}/{erpType}",
            arguments = listOf(navArgument("erpType") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val erpTypeArg = backStackEntry.arguments?.getString("erpType")
            val erpType = erpTypeArg?.let { ErpType.valueOf(it) } ?: ErpType.QUOTATIONS
            HomeScreen(erpType = erpType, navController = navController, homeViewModel)
        }

        composable(Screens.QuotationsScreen.route) {
            QuotationsScreen(navController = navController, viewModel = homeViewModel)
        }

        composable(Screens.StartScreen.route) {
            StartScreen(navController = navController, viewModel = cameraViewModel)
        }
        composable(Screens.CameraScreen.route) {
            BarcodeScannerScreen(navController = navController, viewModel = homeViewModel)
        }
    }
}

fun NavController.navigateToHome(erpType: ErpType) {
    when (erpType) {
        ErpType.QUOTATIONS -> this.navigate("${Screens.HomeScreen.route}/${ErpType.QUOTATIONS.name}")
        ErpType.PURCHASES -> this.navigate("${Screens.HomeScreen.route}/${ErpType.PURCHASES.name}")
    }
}

class Args {
    companion object{
        const val ERP_TYP = "erpType"
//        const val
    }
}