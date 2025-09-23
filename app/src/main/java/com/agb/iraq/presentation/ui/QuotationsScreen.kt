package com.agb.iraq.presentation.ui

import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.agb.iraq.data.remote.model.QuotationItem
import com.agb.iraq.presentation.ui.viewmodel.HomeViewModel

// CameraX + ML Kit
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.ArrayList
import java.util.regex.Pattern

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotationsScreen(
    navController: NavController,
    viewModel: HomeViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    // تحميل بيانات عرض السعر
    LaunchedEffect(Unit) {
        viewModel.setQuotationsItems()
    }

    val quotationsItems by viewModel.quotationsItems.collectAsState()
    val products by viewModel.products.collectAsState()
    val scannedSkus by viewModel.scannedSkus.collectAsState()
    var items by remember(key1 = quotationsItems) { mutableStateOf(quotationsItems?.data?.items ?: emptyList()) }

    var currentItemSku by remember { mutableStateOf("") }

    // خريطة حالة قابلة للملاحظة + محفوظة (Saver آمن للـ Bundle)
    val localCounts = rememberCountStateMap()

    // مفاتيح موحّدة لكل عنصر
    fun countKey(item: QuotationItem): String {
        return when {
            item.id != null -> "ID:${item.id}"
            !item.product?.sku.isNullOrBlank() -> "SKU:${item.product?.sku!!.trim().lowercase()}"
            item.product_id != null -> "PID:${item.product_id}"
            else -> "ROW:${item.hashCode()}"
        }
    }

    // البحث عن العنصر بواسطة كود ممسوح
    fun findItemByScannedCode(codeRaw: String, currentItemSku: String): QuotationItem? {
        val newProducts = viewModel.fetchProducts(
            sku = codeRaw,
            warehouseId = items.find { it.product?.sku == currentItemSku }?.warehouse_id ?: 9
        )
        val updatedItem = newProducts.find { it.sku == codeRaw }
        return if (updatedItem != null) {
            items = items.map { item ->
                if (item.product?.sku == currentItemSku) {
                    item.copy(
                        product = item.product.copy(
                            name = updatedItem.name,
                            sku = updatedItem.sku
                        )
                    )
                } else {
                    item
                }
            }
//            updatedItem
            items.find { it.product?.sku == currentItemSku }
        } else {
            null
        }
    }
//        }

//        val code = codeRaw.trim()
//        if (code.isBlank()) return null
//        return items.firstOrNull { it.product?.sku?.equals(code, ignoreCase = true) == true }
//            ?: items.firstOrNull { it.product_id?.toString()?.equals(code, ignoreCase = true) == true }
//    }

    // توافق مع savedStateHandle لو أرسِلت مسحة من شاشة قديمة
    val backStackEntry by navController.currentBackStackEntryAsState()
    DisposableEffect(backStackEntry) {
        val handle = backStackEntry?.savedStateHandle
        val live = handle?.getLiveData<String>("scannedSku")
        val obs = Observer<String> { skuRaw ->
            handleScannedCode(
                code = skuRaw ?: return@Observer,
                items = items,
                scannedSkus = scannedSkus,
                localCounts = localCounts,
                findItemByScannedCode = {
                    findItemByScannedCode(it, currentItemSku)
                },
                countKey = ::countKey,
                onToast = { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
            )
        }
        live?.observeForever(obs)
        onDispose { live?.removeObserver(obs) }
    }

    // احتساب الاكتمال
    fun isItemSatisfiedLocal(item: QuotationItem): Boolean {
        val key = countKey(item)
        val required = (item.quantity ?: 1).coerceAtLeast(1)
        val serverKey = item.product?.sku ?: item.product_id?.toString() ?: ""
        val serverDone = scannedSkus.contains(serverKey)
        val local = (localCounts[key] ?: 0).coerceAtMost(required)
        return serverDone || local >= required
    }

    val satisfiedCount = items.count { isItemSatisfiedLocal(it) }
    val totalCount = items.size
    val progress = if (totalCount == 0) 0f else satisfiedCount.toFloat() / totalCount.toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600, easing = LinearEasing),
        label = "progressAnim"
    )

    val bg = Brush.linearGradient(listOf(Color(0xFF0F172A), Color(0xFF0B1020)))

    // رسالة تأكيد من الـ ViewModel
    LaunchedEffect(Unit) {
        viewModel.confirmResult.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            navController.popBackStack()
        }
    }

    // فتح/إغلاق الماسح المضمّن
    var scanning by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تفاصيل عرض السعر", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        },
        bottomBar = {
            val allDone = totalCount > 0 && satisfiedCount == totalCount
            Surface(tonalElevation = 6.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp) // رفع المسافة
                        .navigationBarsPadding(),                      // يرفع الزر فوق شريط النظام
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مكتمل: $satisfiedCount / $totalCount",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { viewModel.confirmQuotation() },
                        enabled = allDone,
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("تأكيد", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
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
                    progress = animatedProgress,
                    onStartScan = { if (items.isNotEmpty()) scanning = true }
                )

                Spacer(Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(items) { item ->
                        val key = countKey(item)
                        val required = (item.quantity ?: 1).coerceAtLeast(1)
                        val serverKey = item.product?.sku ?: item.product_id?.toString() ?: ""
                        val serverDone = scannedSkus.contains(serverKey)
                        val localScanned = (localCounts[key] ?: 0).coerceAtMost(required)
                        val scannedForUi = if (serverDone) required else localScanned
                        val isSatisfied = serverDone || scannedForUi >= required

                        QuotationLineCard(
                            item = item,
                            scanned = scannedForUi,
                            required = required,
                            isSatisfied = isSatisfied,
                            onClick = {
                                viewModel.fetchProducts(
                                    item.product?.sku ?: "",
                                    item.warehouse_id ?: 9
                                )
                                currentItemSku = item.product?.sku ?: ""
                                scanning = true
                            }
                        )
                    }
                }
            }

            if (scanning) {
                InlineScannerOverlay(
                    onClose = { scanning = false },
                    onCodeScanned = { code ->
                        handleScannedCode(
                            code = code,
                            items = items,
                            scannedSkus = scannedSkus,
                            localCounts = localCounts,
                            findItemByScannedCode = {
                                findItemByScannedCode(it, currentItemSku)
                            },
                            countKey = ::countKey,
                            onToast = { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                )
            }
        }
    }
}

/** خريطة حالة محفوظة وقابلة للملاحظة (Saver يُرجع ArrayList<Pair<String,Int>>) */
@Composable
private fun rememberCountStateMap(): SnapshotStateMap<String, Int> {
    return rememberSaveable(
        saver = Saver<SnapshotStateMap<String, Int>, ArrayList<Pair<String, Int>>>(
            save = { stateMap ->
                ArrayList(stateMap.entries.map { it.key to it.value })
            },
            restore = { list ->
                mutableStateMapOf<String, Int>().apply {
                    list.forEach { put(it.first, it.second) }
                }
            }
        )
    ) {
        mutableStateMapOf()
    }
}

/** معالجة الكود الممسوح وتحديث العداد */
private fun handleScannedCode(
    code: String,
    items: List<QuotationItem>,
    scannedSkus: Set<String>,
    localCounts: SnapshotStateMap<String, Int>,
    findItemByScannedCode: (String) -> QuotationItem?,
    countKey: (QuotationItem) -> String,
    onToast: (String) -> Unit
) {
    val item = findItemByScannedCode(code)
    if (item == null) {
        onToast("الرمز غير موجود في هذا العرض: $code")
        return
    }
    val key = countKey(item)
    val required = (item.quantity ?: 1).coerceAtLeast(1)
    val serverKey = item.product?.sku ?: item.product_id?.toString() ?: ""
    val serverDone = scannedSkus.contains(serverKey)

    if (serverDone) {
        localCounts[key] = required
        onToast("العنصر مكتمل مسبقًا")
        return
    }

    val current = localCounts[key] ?: 0
    val next = (current + 1).coerceAtMost(required)
    localCounts[key] = next
    onToast("تم المسح: $next / $required")
}

/** رأس التقدّم */
@Composable
private fun ProgressHeader(
    scanned: Int,
    total: Int,
    progress: Float,
    onStartScan: () -> Unit
) {
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
                Spacer(Modifier.width(10.dp))
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
                FilledTonalButton(
                    onClick = { if (total > 0) onStartScan() },
                    enabled = total > 0
                ) {
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScanStateChip(isSatisfied)
            }
        }
    }
}

@Composable
private fun StatusDot(done: Boolean) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(if (done) Color(0xFF10B981) else Color(0xFFF59E0B))
    )
}

@Composable
private fun QuantityPill(scanned: Int, required: Int) {
    val label = "${scanned.coerceAtMost(required)} / $required"
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

@Composable
private fun ScanStateChip(done: Boolean) {
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

/* =========================
   ماسح مضمّن داخل الشاشة
   ========================= */

@Composable
private fun InlineScannerOverlay(
    onClose: () -> Unit,
    onCodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }

    var lastValue by rememberSaveable { mutableStateOf<String?>(null) }
    var lastTime by rememberSaveable { mutableStateOf(0L) }
    val minGapMs = 700L

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Rounded.Close, contentDescription = "إغلاق", tint = Color.White)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                "وضع المسح المتواصل",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
        }

        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp, bottom = 80.dp)
        ) { view ->
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(view.surfaceProvider)
                }

                val barcodeScanner = BarcodeScanning.getClient()
                val textRecognizer =
                    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                            processImageForCode(
                                imageProxy = imageProxy,
                                barcodeScanner = barcodeScanner,
                                textFallback = { proxy, onSuccess, onError ->
                                    processImageForNumbers(
                                        imageProxy = proxy,
                                        recognizer = textRecognizer,
                                        onSuccessScan = onSuccess,
                                        onErrorScan = onError
                                    )
                                },
                                onSuccessScan = { raw ->
                                    val now = System.currentTimeMillis()
                                    val value = raw.trim()
                                    val tooSoonSame =
                                        (lastValue != null && lastValue == value && (now - lastTime) < minGapMs)
                                    if (!tooSoonSame && value.isNotEmpty()) {
                                        lastValue = value
                                        lastTime = now
                                        onCodeScanned(value)
                                    }
                                },
                                onErrorScan = { /* تجاهل لتقليل الإزعاج */ }
                            )
                        }
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner, cameraSelector, preview, imageAnalysis
                    )
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "فشل تشغيل الكاميرا: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }, ContextCompat.getMainExecutor(context))
        }

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(260.dp)) {
                val cornerRadius = 16.dp.toPx()
                val strokeWidth = 6.dp.toPx()
                drawRoundRect(
                    color = Color.White,
                    topLeft = androidx.compose.ui.geometry.Offset.Zero,
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                        cornerRadius,
                        cornerRadius
                    ),
                    style = Stroke(width = strokeWidth)
                )
            }
            Text(
                text = "ضع QR أو الرقم داخل الإطار\n(المسح مستمر)",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 90.dp)
            )
        }
    }
}

@OptIn(ExperimentalGetImage::class)
private fun processImageForCode(
    imageProxy: ImageProxy,
    barcodeScanner: BarcodeScanner,
    textFallback: (ImageProxy, (String) -> Unit, (String) -> Unit) -> Unit,
    onSuccessScan: (code: String) -> Unit,
    onErrorScan: (error: String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close(); return
    }
    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

    barcodeScanner.process(image)
        .addOnSuccessListener { barcodes ->
            val code = barcodes.firstOrNull()?.rawValue
            if (!code.isNullOrBlank()) {
                onSuccessScan(code.trim())
                imageProxy.close()
            } else {
                textFallback(imageProxy, onSuccessScan, onErrorScan)
            }
        }
        .addOnFailureListener {
            textFallback(imageProxy, onSuccessScan, onErrorScan)
        }
}

@OptIn(ExperimentalGetImage::class)
private fun processImageForNumbers(
    imageProxy: ImageProxy,
    recognizer: com.google.mlkit.vision.text.TextRecognizer,
    onSuccessScan: (code: String) -> Unit,
    onErrorScan: (error: String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close(); return
    }
    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

    recognizer.process(image)
        .addOnSuccessListener { result ->
            val fullText = result.text ?: ""
            val normalized = normalizeDigits(fullText)
            val pattern = Pattern.compile("\\d+")
            val matcher = pattern.matcher(normalized)
            var best: String? = null
            while (matcher.find()) {
                val candidate = matcher.group()
                if (best == null || candidate.length > best.length) best = candidate
            }
            if (!best.isNullOrBlank()) onSuccessScan(best!!) else onErrorScan("لم يتم العثور على أرقام")
        }
        .addOnFailureListener { e -> onErrorScan(e.message ?: "فشل التعرف النصي") }
        .addOnCompleteListener { imageProxy.close() }
}

/** يحوّل الأرقام العربية/الفارسية إلى 0-9 العادية */
private fun normalizeDigits(input: String): String {
    if (input.isEmpty()) return input
    val sb = StringBuilder(input.length)
    for (ch in input) {
        val mapped = when (ch) {
            '٠' -> '0'; '١' -> '1'; '٢' -> '2'; '٣' -> '3'; '٤' -> '4'
            '٥' -> '5'; '٦' -> '6'; '٧' -> '7'; '٨' -> '8'; '٩' -> '9'
            '۰' -> '0'; '۱' -> '1'; '۲' -> '2'; '۳' -> '3'; '۴' -> '4'
            '۵' -> '5'; '۶' -> '6'; '۷' -> '7'; '۸' -> '8'; '۹' -> '9'
            else -> ch
        }
        sb.append(mapped)
    }
    return sb.toString().trim().replace("\\s+".toRegex(), " ")
}
