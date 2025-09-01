package com.agb.iraq.presentation.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.agb.iraq.data.remote.model.QuotationItem
import com.agb.iraq.presentation.navigation.Screens
import com.agb.iraq.presentation.ui.componanet.QuotationTopBar
import com.agb.iraq.presentation.ui.viewmodel.HomeViewModel

@Composable
fun QuotationsScreen(
    navController: NavController,
    viewModel: HomeViewModel
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.setQuotationsItems()
    }

    val quotationsItems by viewModel.quotationsItems.collectAsState()
    val scannedSkus by viewModel.scannedSkus.collectAsState()

    Scaffold(
        topBar = { QuotationTopBar(onBackClick = { navController.navigateUp() }) }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(quotationsItems?.data?.items ?: emptyList()) { item ->
                val isScanned = scannedSkus.contains(item.product?.sku)
                QuotationItemView(
                    item = item,
                    isScanned = isScanned,
                    onClick = {
                        navController.navigate(Screens.CameraScreen.route)
                    }
                )
            }
        }
    }

    // checking after each sku
    LaunchedEffect(scannedSkus) {
        if (viewModel.isAllScanned()) {
            Toast.makeText(context, "All items scanned!", Toast.LENGTH_SHORT).show()
            viewModel.confirmQuotation()
        }
    }

    // after confirm
    LaunchedEffect(Unit) {
        viewModel.confirmResult.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            navController.popBackStack()
        }
    }
}

@Composable
private fun QuotationItemView(
    item: QuotationItem,
    isScanned: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isScanned) Color(0xFFB9F6CA) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = onClick
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("ID #${item.id}", style = MaterialTheme.typography.titleMedium)
            Text("SKU: ${item.product?.sku ?: "-"}")
        }
    }
}
