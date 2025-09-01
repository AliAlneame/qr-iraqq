package com.agb.laithsurvey.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val Blue = Color(0xFF1F93EB)
val DarkGrey = Color(0xFF838C95)
val White = Color(0xFFFFFFFF)
val Shade = Color(0xFF000C1A)
val LightGrey = Color(0xFFEFF0F2)

val LightColors = lightColorScheme(
    primary = Blue,
    secondary = Shade,
    onPrimary = White,
    tertiary = DarkGrey,
    onTertiary = LightGrey,
    background = White,
)