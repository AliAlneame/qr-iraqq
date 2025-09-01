package com.agb.iraq.presentation.ui.componanet

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotationTopBar(
    title: String = "Quotations",
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = { onBackClick() }) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_revert), // or your own arrow_back icon
                    contentDescription = "Back"
                )
            }
        }
    )
}
