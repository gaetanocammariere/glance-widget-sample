package com.telepass.glancesample

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val Blue200 = Color(0xFF448aff)
val LightBlue = Color(0xFF83b9ff)
val DarkBlue200 = Color(0xFF005ecb)

val Indigo900 = Color(0xFF1a237e)
val LightIndigo900 = Color(0xFF534bae)
val DarkIndigo900 = Color(0xFF000051)

val LightColorScheme = lightColorScheme(
    primary = Blue200,
    secondary = LightBlue,
    tertiary = DarkBlue200
)

val DarkColorScheme = darkColorScheme(
    primary = Indigo900,
    secondary = LightIndigo900,
    tertiary = DarkIndigo900
)