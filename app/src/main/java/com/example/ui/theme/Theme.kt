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

import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = PrimarySage,
  onPrimary = Color.White,
  secondary = SecondarySage,
  onSecondary = Color(0xFF121212),
  tertiary = TertiarySand,
  onTertiary = Color(0xFF121212),
  background = DarkBackground,
  surface = DarkSurface,
  onBackground = Color(0xFFECECEC),
  onSurface = Color(0xFFECECEC),
  error = MutedRedAccent,
  onError = Color.White
)

private val LightColorScheme = lightColorScheme(
  primary = PrimarySage,
  onPrimary = Color.White,
  secondary = SecondarySage,
  onSecondary = Color.White,
  tertiary = TertiarySand,
  onTertiary = TextNearBlack,
  background = LightBackground,
  surface = LightSurface,
  onBackground = TextNearBlack,
  onSurface = TextNearBlack,
  error = MutedRedAccent,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Preserve handcrafted premium design by default
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
