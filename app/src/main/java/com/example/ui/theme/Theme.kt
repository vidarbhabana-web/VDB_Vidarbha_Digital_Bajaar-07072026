package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = SaffronPrimary,
    secondary = GoldAccent,
    tertiary = SaffronLight,
    background = RichBlack,
    surface = VelvetDark,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = RichBlack,
    onBackground = PlatinumWhite,
    onSurface = PlatinumWhite
  )

private val LightColorScheme =
  lightColorScheme(
    primary = SaffronPrimary,
    secondary = GoldDark,
    tertiary = SaffronDark,
    background = SoftWhite,
    surface = androidx.compose.ui.graphics.Color.White,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = RichBlack,
    onBackground = RichBlack,
    onSurface = RichBlack
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
