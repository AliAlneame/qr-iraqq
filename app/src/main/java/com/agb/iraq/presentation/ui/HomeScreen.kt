package com.agb.iraq.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear

import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.agb.iraq.data.remote.model.QuotationItem
import com.agb.iraq.presentation.navigation.Screens
import com.agb.iraq.presentation.ui.model.ErpType
import com.agb.iraq.presentation.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    erpType: ErpType,
    navController: NavController,
    viewModel: HomeViewModel
) {
    LaunchedEffect(erpType) { viewModel.setErpType(erpType) }

    val lazyPagingItems = viewModel.quotations.collectAsLazyPagingItems()
    var query by remember { mutableStateOf("") }

    val bgGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF0F172A), // slate-900
            Color(0xFF111827), // gray-900
            Color(0xFF0B1020)  // deep blue-ish
        )
    )

    val appBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("عروض الأسعار", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { lazyPagingItems.refresh() }
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "تحديث")
                    }
                },
                scrollBehavior = appBarScrollBehavior
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .background(bgGradient)
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
            ) {
                Spacer(Modifier.height(12.dp))

                // حقل بحث شكلي (يمكنك ربطه بالفلترة في الـ ViewModel لاحقاً)
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .drawGlassBorder(),
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    placeholder = { Text("ابحث عن عرض بسجل/اسم/معرّف") }
                )

                Spacer(Modifier.height(8.dp))

                // حالات التحميل/الخطأ الأولية
                when (val state = lazyPagingItems.loadState.refresh) {
                    is LoadState.Loading -> {
                        LoadingSection(
                            title = "جارِ تحميل العروض",
                            subtitle = "يرجى الانتظار قليلًا…"
                        )
                    }
                    is LoadState.Error -> {
                        ErrorSection(
                            message = state.error.message ?: "حدث خطأ غير متوقع",
                            onRetry = { lazyPagingItems.retry() }
                        )
                    }
                    else -> {
                        // القائمة
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            items(
                                items = lazyPagingItems.itemSnapshotList.items.filter {
                                    query.isBlank() ||
                                            it.id?.toString()?.contains(query, true) == true ||
                                            it.product?.sku?.contains(query, true) == true ||
                                            it.product?.name?.contains(query, true) == true ||
                                            it.description?.contains(query, true) == true
                                }
                            ) { item ->
                                QuotationCard(
                                    item = item,
                                    onClick = {
                                        viewModel.setCurrentQuotationId(item.id ?: 0)
                                        navController.navigate(Screens.QuotationsScreen.route)
                                    }
                                )
                            }

                            // حالة التحميل أثناء الإلحاق
                            when (lazyPagingItems.loadState.append) {
                                is LoadState.Loading -> item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator(strokeCap = StrokeCap.Round)
                                    }
                                }
                                is LoadState.Error -> item {
                                    val err = (lazyPagingItems.loadState.append as LoadState.Error).error
                                    ErrorInline(
                                        message = err.message ?: "تعذر تحميل المزيد",
                                        onRetry = { lazyPagingItems.retry() }
                                    )
                                }
                                else -> Unit
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuotationCard(
    item: QuotationItem,
    onClick: () -> Unit
) {
    // ألوان زجاجية لطيفة
    val cardColor = Color(0x66FFFFFF) // شفافية خفيفة
    val borderColor = Color(0x22FFFFFF)

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .drawBehind {
                // توهج خفيف
            }
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(20.dp)),
        colors = CardDefaults.elevatedCardColors(containerColor = cardColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFF60A5FA).copy(alpha = 0.35f),
                                    Color(0xFF1D4ED8).copy(alpha = 0.35f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Search, contentDescription = null)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "ID #${item.id ?: "-"}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Customer: ${item.customer?.name ?: "-"}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val sku = item.product?.sku ?: item.product_id?.toString() ?: "-"
                    AnimatedVisibility(visible = sku.isNotBlank(), enter = fadeIn(), exit = fadeOut()) {
                        Text(
                            text = "SKU: $sku",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }

                // شارة كمية
                val qty = (item.quantity ?: 0).toString()
                QuantityBadge(qty)
            }

            Spacer(Modifier.height(10.dp))
            val name = item.product?.name ?: item.description ?: "-"
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(12.dp))
            Divider(color = Color.White.copy(alpha = 0.12f))
            Spacer(Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlassChip(text = "عرض سعر")
                if (!name.isNullOrBlank() && name.length > 18) GlassChip(text = "نصي طويل")
            }
        }
    }
}

@Composable
private fun QuantityBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF34D399).copy(alpha = 0.9f), Color(0xFF10B981).copy(alpha = 0.9f))
                )
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = "الكمية $text", color = Color(0xFF06251A), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun GlassChip(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x33FFFFFF))
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        color = Color(0xFFE2E8F0)
    )
}

@Composable
private fun LoadingSection(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(strokeCap = StrokeCap.Round)
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = Color(0xFFE5E7EB))
        Spacer(Modifier.height(6.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF9CA3AF))
    }
}

@Composable
private fun ErrorSection(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Rounded.Clear, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color(0xFFFCA5A5))
        Spacer(Modifier.height(12.dp))
        Text("تعذر تحميل البيانات", style = MaterialTheme.typography.titleMedium, color = Color(0xFFFFE4E6))
        Spacer(Modifier.height(6.dp))
        Text(message, style = MaterialTheme.typography.bodySmall, color = Color(0xFFFECACA))
        Spacer(Modifier.height(14.dp))
        FilledIconButton(onClick = onRetry) {
            Icon(Icons.Rounded.Refresh, contentDescription = null)
        }
    }
}

@Composable
private fun ErrorInline(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0x22FF6B6B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Rounded.Clear, contentDescription = null, tint = Color(0xFFF87171))
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                color = Color(0xFFFFE4E6)
            )
            TextButton(onClick = onRetry) { Text("إعادة المحاولة") }
        }
    }
}

private fun Modifier.drawGlassBorder(width: Dp = 1.dp) =
    this.border(width, Color(0x22FFFFFF), RoundedCornerShape(16.dp))
