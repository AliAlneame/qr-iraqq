package com.agb.laithsurvey.presentation.qrCamera

import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.agb.laithsurvey.presentation.ui.viewmodel.HomeViewModel
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.Locale
import java.util.regex.Pattern

@Composable
fun BarcodeScannerScreen(
    viewModel: HomeViewModel,
    navController: NavHostController
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }

    // لمنع التكرار: لا تعالج أكثر من نتيجة واحدة
    val handledResult = remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        ) { view ->
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(view.surfaceProvider)
                }

                val barcodeScanner = BarcodeScanning.getClient()
                val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                            if (!handledResult.value) {
                                processImageForCode(
                                    imageProxy = imageProxy,
                                    barcodeScanner = barcodeScanner,
                                    // إن لم نجد باركود، نحاول OCR
                                    textFallback = { proxy, onSuccess, onError ->
                                        processImageForNumbers(
                                            imageProxy = proxy,
                                            onSuccessScan = onSuccess,
                                            onErrorScan = onError,
                                            recognizer = textRecognizer
                                        )
                                    },
                                    onSuccessScan = { code ->
                                        if (handledResult.value) return@processImageForCode
                                        handledResult.value = true

                                        val items = viewModel.quotationsItems.value?.data?.items ?: emptyList()
                                        val match = items.any { it.product?.sku.equals(code, ignoreCase = true) }

                                        if (match) {
                                            viewModel.markAsScanned(code)
                                            Toast.makeText(context, "Matched: $code", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Not found: $code", Toast.LENGTH_SHORT).show()
                                        }
                                        navController.popBackStack()
                                    },
                                    onErrorScan = { error ->
                                        if (!handledResult.value) {
                                            Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            } else {
                                imageProxy.close()
                            }
                        }
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageAnalysis
                )
            }, ContextCompat.getMainExecutor(context))
        }

        // Overlay UI
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(250.dp)) {
                val cornerRadius = 16.dp.toPx()
                val strokeWidth = 6.dp.toPx()
                drawRoundRect(
                    color = Color.White,
                    topLeft = androidx.compose.ui.geometry.Offset.Zero,
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
                    style = Stroke(width = strokeWidth)
                )
            }
            Text(
                text = "Align the QR or Number within the box",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp)
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
        imageProxy.close()
        return
    }
    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

    barcodeScanner.process(image)
        .addOnSuccessListener { barcodes ->
            val code = barcodes.firstOrNull()?.rawValue
            if (!code.isNullOrBlank()) {
                onSuccessScan(code.trim())
                imageProxy.close()
            } else {
                // لا يوجد باركود: جرّب OCR للأرقام
                textFallback(imageProxy, onSuccessScan, onErrorScan)
            }
        }
        .addOnFailureListener { e ->
            // في حال فشل الباركود، نجرب OCR
            textFallback(imageProxy, onSuccessScan, onErrorScan)
        }
}

/**
 * OCR لاستخراج أرقام فقط من النص.
 * - يدعم أرقام عربية-هندية ٠١٢٣٤٥٦٧٨٩ بتحويلها إلى 0-9
 * - يلتقط أطول متتالية أرقام كأفضل تخمين (يمكن تعديله حسب حاجتك)
 */
@OptIn(ExperimentalGetImage::class)
private fun processImageForNumbers(
    imageProxy: ImageProxy,
    recognizer: com.google.mlkit.vision.text.TextRecognizer,
    onSuccessScan: (code: String) -> Unit,
    onErrorScan: (error: String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close()
        return
    }
    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

    recognizer.process(image)
        .addOnSuccessListener { result ->
            val fullText = result.text ?: ""
            val normalized = normalizeDigits(fullText)

            // التقط جميع المتتاليات الرقمية
            val pattern = Pattern.compile("\\d+")
            val matcher = pattern.matcher(normalized)

            var best: String? = null
            while (matcher.find()) {
                val candidate = matcher.group()
                if (best == null || candidate.length > best.length) {
                    best = candidate
                }
            }

            if (!best.isNullOrBlank()) {
                onSuccessScan(best!!)
            } else {
                onErrorScan("No numbers detected")
            }
        }
        .addOnFailureListener { e ->
            onErrorScan(e.message ?: "OCR failed")
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}

/**
 * يحوّل الأرقام العربية-الهندية إلى 0-9
 * ٠١٢٣٤٥٦٧٨٩ → 0123456789
 */
private fun normalizeDigits(input: String): String {
    if (input.isEmpty()) return input
    val sb = StringBuilder(input.length)
    for (ch in input) {
        val mapped = when (ch) {
            '٠' -> '0'
            '١' -> '1'
            '٢' -> '2'
            '٣' -> '3'
            '٤' -> '4'
            '٥' -> '5'
            '٦' -> '6'
            '٧' -> '7'
            '٨' -> '8'
            '٩' -> '9'
            else -> ch
        }
        sb.append(mapped)
    }
    return sb.toString().trim().replace("\\s+".toRegex(), " ")
}
