package com.example.falcon.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

/**
 * FuturisticGlassSurface
 *
 * A reusable, premium holographic glassmorphism container for futuristic UI elements.
 * Features:
 * - Translucent dark background with subtle multi-layer frosted gradient
 * - Hardware-accelerated subtle blur effect
 * - Thin specular gradient border with bright holographic highlights
 * - Soft diffuse ambient & spot glow
 * - Tactile feedback and optional corner tech accents
 *
 * Used for UI elements such as the Active Task Panel, floating controls, and temporary status overlays.
 */
@Composable
fun FuturisticGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    borderBrush: Brush = Brush.linearGradient(
        colors = listOf(
            FalconCyanBright.copy(alpha = 0.65f),
            FalconCyan.copy(alpha = 0.25f),
            FalconBorder.copy(alpha = 0.4f),
            FalconCyanDim.copy(alpha = 0.15f)
        ),
        start = Offset(0f, 0f),
        end = Offset(400f, 600f)
    ),
    borderWidth: Dp = 1.dp,
    glowColor: Color = FalconCyan,
    glowRadius: Dp = 14.dp,
    glowAlpha: Float = 0.22f,
    backgroundColor: Color = Color(0xD9060B16), // Translucent obsidian tint
    backdropGradient: Brush = Brush.linearGradient(
        colors = listOf(
            Color(0xE60A1324), // Subtle top-left cyan-dark illumination
            Color(0xF2050912), // Deep neural obsidian
            Color(0xFA02050A)  // Translucent bottom dark
        ),
        start = Offset(0f, 0f),
        end = Offset(300f, 500f)
    ),
    contentPadding: PaddingValues = PaddingValues(16.dp),
    blurRadius: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    testTag: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    } else Modifier

    val testTagModifier = if (testTag != null) Modifier.testTag(testTag) else Modifier

    Box(
        modifier = modifier
            .then(testTagModifier)
            .shadow(
                elevation = glowRadius,
                shape = shape,
                ambientColor = glowColor.copy(alpha = glowAlpha * 0.7f),
                spotColor = glowColor.copy(alpha = glowAlpha)
            )
            .clip(shape)
            .border(width = borderWidth, brush = borderBrush, shape = shape)
            .then(clickableModifier)
    ) {
        // 1. Frosted / Blur Layer simulation & background gradient
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(backgroundColor)
                .background(backdropGradient)
                .blur(blurRadius)
        )

        // 2. Subtle specular inner sheen & hairline reflection
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawBehind {
                    // Top hairline specular highlight
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                glowColor.copy(alpha = 0.45f),
                                Color.Transparent
                            )
                        ),
                        start = Offset(size.width * 0.15f, 1f),
                        end = Offset(size.width * 0.85f, 1f),
                        strokeWidth = 1.5f
                    )
                }
        )

        // 3. Foreground Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content
        )
    }
}
