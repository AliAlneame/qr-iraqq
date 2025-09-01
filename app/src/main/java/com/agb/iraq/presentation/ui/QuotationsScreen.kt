package com.agb.iraq.presentation.ui

import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import androidx.lifecycle.observe
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.agb.iraq.data.remote.model.QuotationItem
import com.agb.iraq.presentation.navigation.Screens
import com.agb.iraq.presentation.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotationsScreen(
    navController: NavController,
    viewModel: HomeViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) { viewModel.setQuotationsItems() }

    val quotationsItems by viewModel.quotationsItems.collectAsState()
    // نستخدم الـ Set الموجود فقط للعرض إن كان فيه SKU معلّم أصلاً من منطقك الحالي
    val scannedSkus by viewModel.scannedSkus.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val items = quotationsItems?.data?.items ?: emptyList()

    // عدّاد محلّي لكل SKU: لا علاقة له بالـ ViewModel
    val localCounts = remember { mutableStateMapOf<String, Int>() }

    // استلام نتيجة المسح من شاشة الكاميرا عبر savedStateHandle (واجهة فقط)
    val backStackEntry by navController.currentBackStackEntryAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(backStackEntry) {
        backStackEntry?.savedStateHandle
            ?.getLiveData<String>("scannedSku")
            ?.observe(lifecycleOwner) { sku ->
                if (!sku.isNullOrBlank()) {
                    val key = sku.trim()
                    val current = localCounts[key] ?: 0
                    localCounts[key] = current + 1
                    val req = items.find { it.product?.sku == key || it.product_id?.toString() == key }?.quantity ?: 1
                    Toast.makeText(context, "تم المسح: ${localCounts[key] ?: 0} / $req", Toast.LENGTH_SHORT).show()

                    // لا نغير الـ ViewModel؛ نكتفي بالإكمال محليًا
                    if (isAllSatisfiedLocal(items, localCounts, scannedSkus)) {
                        Toast.makeText(context, "تم مسح جميع العناصر ✅", Toast.LENGTH_SHORT).show()
                        viewModel.confirmQuotation() // نؤكد مباشرة
                    }
                }
            }
    }

    // تقدّم عام: العناصر المكتملة محليًا أو مسبقًا (من الـSet)
    val satisfiedCount = items.count { isItemSatisfiedLocal(it, localCounts, scannedSkus) }
    val totalCount = items.size
    val progress = if (totalCount == 0) 0f else satisfiedCount.toFloat() / totalCount.toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600, easing = LinearEasing),
        label = "progressAnim"
    )

    val bg = Brush.linearGradient(listOf(Color(0xFF0F172A), Color(0xFF0B1020)))

    // بعد التأكيد (كما عندك)
    LaunchedEffect(Unit) {
        viewModel.confirmResult.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تفاصيل عرض السعر", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .background(bg)
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
            ) {
                Spacer(Modifier.height(10.dp))

                ProgressHeader(
                    scanned = satisfiedCount,
                    total = totalCount,
                    progress = animatedProgress
                )

                Spacer(Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(items) { item ->
                        val sku = item.product?.sku ?: item.product_id?.toString() ?: "-"
                        val required = (item.quantity ?: 1).coerceAtLeast(1)
                        // لو الـViewModel معلّم الـSKU مسبقًا، نعتبره مكتمل في العرض
                        val serverDone = scannedSkus.contains(sku)
                        val localScanned = localCounts[sku] ?: 0
                        val scannedForUi = if (serverDone) required else localScanned
                        val isSatisfied = serverDone || scannedForUi >= required

                        QuotationLineCard(
                            item = item,
                            scanned = scannedForUi,
                            required = required,
                            isSatisfied = isSatisfied,
                            onClick = {
                                // افتح الكاميرا للمسح المتكرر (واجهة فقط)
                                navController.navigate(Screens.CameraScreen.route)
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun isItemSatisfiedLocal(
    item: QuotationItem,
    localCounts: Map<String, Int>,
    scannedSkus: Set<String>
): Boolean {
    val sku = item.product?.sku ?: item.product_id?.toString() ?: return false
    val required = (item.quantity ?: 1).coerceAtLeast(1)
    val serverDone = scannedSkus.contains(sku)
    val local = localCounts[sku] ?: 0
    return serverDone || local >= required
}

private fun isAllSatisfiedLocal(
    items: List<QuotationItem>,
    localCounts: Map<String, Int>,
    scannedSkus: Set<String>
): Boolean = items.isNotEmpty() && items.all { isItemSatisfiedLocal(it, localCounts, scannedSkus) }

@Composable
private fun ProgressHeader(scanned: Int, total: Int, progress: Float) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(18.dp)),
        colors = CardDefaults.elevatedCardColors(containerColor = Color(0x33FFFFFF)),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFFA7F3D0).copy(alpha = 0.35f),
                                    Color(0xFF34D399).copy(alpha = 0.35f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF065F46))
                }
                Spacer(Modifier.size(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "التقدّم",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "$scanned / $total عناصر مكتملة",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8)
                    )
                }
                FilledTonalButton(onClick = { /* اختياري */ }, enabled = total > 0) {
                    Icon(Icons.Rounded.Search, contentDescription = null)
                    Spacer(Modifier.size(6.dp))
                    Text("ابدأ المسح")
                }
            }

            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(12.dp)),
                trackColor = Color(0x22FFFFFF),
                color = Color(0xFF34D399)
            )
        }
    }
}

@Composable
private fun QuotationLineCard(
    item: QuotationItem,
    scanned: Int,
    required: Int,
    isSatisfied: Boolean,
    onClick: () -> Unit
) {
    val baseColor = if (isSatisfied) Color(0x3322C55E) else Color(0x33FFFFFF)
    val borderColor = if (isSatisfied) Color(0x4422C55E) else Color(0x22FFFFFF)
    val titleColor = if (isSatisfied) Color(0xFF064E3B) else Color(0xFFE2E8F0)
    val noteColor = if (isSatisfied) Color(0xFF047857) else Color(0xFF94A3B8)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, borderColor, RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = baseColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusDot(isSatisfied)
                Spacer(Modifier.size(8.dp))
                Text(
                    text = "ID #${item.id ?: "-"}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = titleColor
                )
                Spacer(Modifier.weight(1f))
                QuantityPill(scanned = scanned, required = required)
            }

            Spacer(Modifier.height(8.dp))
            val sku = item.product?.sku ?: item.product_id?.toString() ?: "-"
            val name = item.product?.name ?: item.description ?: "-"
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "SKU: $sku",
                style = MaterialTheme.typography.bodyMedium,
                color = noteColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(10.dp))
            Divider(color = Color.White.copy(alpha = 0.10f))
            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ScanStateChip(isSatisfied)
            }
        }
    }
}

@Composable private fun StatusDot(done: Boolean) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(if (done) Color(0xFF10B981) else Color(0xFFF59E0B))
    )
}

@Composable private fun QuantityPill(scanned: Int, required: Int) {
    val label = "$scanned / $required"
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF60A5FA).copy(alpha = 0.85f),
                        Color(0xFF3B82F6).copy(alpha = 0.85f)
                    )
                )
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) { Text("الكمية $label", color = Color(0xFF06122B), fontWeight = FontWeight.SemiBold) }
}

@Composable private fun ScanStateChip(done: Boolean) {
    val text = if (done) "مكتمل" else "بانتظار المسح"
    val bg = if (done) Color(0x3310B981) else Color(0x33FBBF24)
    val fg = if (done) Color(0xFF065F46) else Color(0xFF92400E)
    Text(
        text = text,
        color = fg,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, fg.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    )
}
