package com.agb.iraq.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.agb.iraq.data.remote.model.QuotationItem
import com.agb.iraq.presentation.navigation.Screens
import com.agb.iraq.presentation.ui.componanet.QuotationTopBar
import com.agb.iraq.presentation.ui.model.ErpType
import com.agb.iraq.presentation.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    erpType: ErpType,
    navController: NavController,
    viewModel: HomeViewModel
) {
    LaunchedEffect(erpType) {
        viewModel.setErpType(erpType)
    }
    val lazyPagingItems = viewModel.quotations.collectAsLazyPagingItems()
    Scaffold(
        topBar = { QuotationTopBar(onBackClick = {navController.navigateUp()}) }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(lazyPagingItems.itemSnapshotList.items) { item ->
                QuotationItemView(
                    item = item,
                    navController = navController,
                    onClick = { id ->
                        viewModel.setCurrentQuotationId(id)
                    }
                )
            }

            lazyPagingItems.apply {
                when {
                    loadState.append is LoadState.Loading -> {
                        item { CircularProgressIndicator(
                            modifier = Modifier.padding(16.dp)
                        ) }
                    }
                    loadState.refresh is LoadState.Loading -> {
                        item { CircularProgressIndicator(
                            modifier = Modifier.padding(16.dp)
                        ) }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuotationItemView(
    item: QuotationItem,
    navController: NavController,
    onClick: (id: Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = {
            onClick(item.id ?: 0)
            navController.navigate(Screens.QuotationsScreen.route)
        }
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = "ID #${item.id}", style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
