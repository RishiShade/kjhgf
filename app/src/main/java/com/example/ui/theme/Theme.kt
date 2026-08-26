package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FalconColorScheme = darkColorScheme(
  primary = FalconCyan,
  onPrimary = Color(0xFF040710),
  primaryContainer = Color(0xFF082F49),
  onPrimaryContainer = FalconCyanBright,
  secondary = FalconVioletLight,
  onSecondary = Color(0xFF1E0836),
  secondaryContainer = Color(0xFF3B0764),
  onSecondaryContainer = Color(0xFFF3E8FF),
  tertiary = FalconCyanDim,
  onTertiary = Color.White,
  background = FalconDarkBg,
  onBackground = FalconTextPrimary,
  surface = FalconSurface,
  onSurface = FalconTextPrimary,
  surfaceVariant = FalconSurfaceVariant,
  onSurfaceVariant = FalconTextSecondary,
  outline = FalconBorder,
  error = FalconError,
  onError = Color.White,
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = FalconColorScheme,
    typography = Typography,
    content = content
  )
}

@Composable
fun FalconTheme(
  content: @Composable () -> Unit,
) {
  MyApplicationTheme(content = content)
}
