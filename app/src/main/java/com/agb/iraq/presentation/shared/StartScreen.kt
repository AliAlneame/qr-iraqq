package com.agb.iraq.presentation.shared

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check

import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.agb.iraq.R
import com.agb.iraq.presentation.base.EventHandler
import com.agb.iraq.presentation.navigation.Screens
import com.agb.iraq.presentation.navigation.navigateToHome
import com.agb.iraq.presentation.qrCamera.CameraViewModel
import com.agb.iraq.presentation.ui.model.ErpType

@Composable
fun StartScreen(viewModel: CameraViewModel, navController: NavHostController) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    EventHandler(viewModel.effect) { effect, _ ->
        when (effect) {
            is SharedUiEffect.ShowToast -> {
                Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    StartContent(state, viewModel, navController)
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun StartContent(
    state: SharedUiState,
    listener: SharedInteractionListener,
    navController: NavHostController
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(checkCameraPermission(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            hasPermission = true
            navController.navigate(Screens.CameraScreen.route)
        } else {
            Toast.makeText(context, "تم رفض إذن الكاميرا", Toast.LENGTH_SHORT).show()
        }
    }

    // خلفية فاتحة مبهجة (حتى لو النظام داكن نجبرها فاتحة بما إنك ما تحب الداكن)
    val pastelGradient = Brush.verticalGradient(
        listOf(
            Color(0xFFF8FAFF), // أبيض أزرق خفيف جدًا
            Color(0xFFEFF5FF)  // أزرق ثلجي
        )
    )

    // حركة دخول بسيطة للكارد
    var alpha = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(Unit) {
        alpha.animateTo(
            1f,
            animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing)
        )
    }

    Scaffold { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(pastelGradient)
        ) {
            // يمكن استخدام صورة خفيفة جدًا كزخرفة بالخلفية (اختياري)
            Image(
                painter = painterResource(id = R.drawable.ad),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
            )

            // المحتوى
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 420.dp)
                        .graphicsLayer { this.alpha = alpha.value }
                        .shadow(10.dp, RoundedCornerShape(26.dp), clip = true),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(26.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {

                        // شارة دائرية لطيفة أعلى الكارد
                        Box(
                            modifier = Modifier
                                .size(82.dp)
                                .background(Color(0xFFE8F2FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                                tint = Color(0xFF2563EB), // أزرق واضح
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        // عنوان رئيسي
//                        Text(
//                            text = "AGB — Smart Scanner",
//                            fontSize = 22.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            color = Color(0xFF0F172A),
//                            textAlign = TextAlign.Center
//                        )

                        // وصف عربي واضح
//                        Text(
//                            text = "اختر الوحدة التي تريدها. تصميم فاتح وواضح لتجربة مريحة وسريعة.",
//                            fontSize = 14.sp,
//                            color = Color(0xFF475569),
//                            textAlign = TextAlign.Center,
//                            lineHeight = 18.sp,
//                            modifier = Modifier.padding(horizontal = 8.dp)
//                        )

                        // شبكة الإجراءات (بطاقتان نظيفتان)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ActionTile(
                                title = "Quotations",
                                subtitle = "العروض وحالاتها",
                                icon = Icons.Outlined.Check,
                                accent = Color(0xFF16A34A), // أخضر
                                onClick = {
                                    if (hasPermission) {
                                        navController.navigateToHome(ErpType.QUOTATIONS)
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                }
                            )
                            ActionTile(
                                title = "Purchases",
                                subtitle = "مشتريات الموردين",
                                icon = Icons.Outlined.ShoppingCart,
                                accent = Color(0xFFDC2626), // أحمر
                                onClick = {
                                    if (hasPermission) {
                                        navController.navigateToHome(ErpType.PURCHASES)
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                }
                            )
                        }

                        // زر أساسي أزرق لفتح الكاميرا مباشرة
//                        ElevatedButton(
//                            onClick = {
//                                if (hasPermission) {
//                                    navController.navigate(Screens.CameraScreen.route)
//                                } else {
//                                    permissionLauncher.launch(Manifest.permission.CAMERA)
//                                }
//                            },
//                            modifier = Modifier.fillMaxWidth(),
//                            colors = ButtonDefaults.elevatedButtonColors(
//                                containerColor = Color(0xFF2563EB), // أزرق جميل
//                                contentColor = Color.White
//                            )
//                        ) {
//                            Icon(
//                                imageVector = Icons.Outlined.Check,
//                                contentDescription = null,
//                                modifier = Modifier.padding(end = 8.dp)
//                            )
//                            Text("فتح الماسح الفوري", fontSize = 16.sp)
//                        }

                        AnimatedVisibility(visible = !hasPermission, enter = fadeIn(), exit = fadeOut()) {
                            Text(
                                text = "ستحتاج لمنح إذن الوصول للكاميرا لاستخدام المسح.",
                                color = Color(0xFF2563EB),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * بطاقة إجراء نظيفة على خلفية بيضاء مع ظل خفيف ولهجة لونية على الأيقونة.
 */
@Composable
private fun ActionTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 92.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // أيقونة ضمن دائرة ملونة فاتحة
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(accent.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column(
                modifier = Modifier
                    .padding(start = 66.dp, end = 8.dp)
                    .align(Alignment.CenterStart)
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

// فحص إذن الكاميرا
fun checkCameraPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
}
