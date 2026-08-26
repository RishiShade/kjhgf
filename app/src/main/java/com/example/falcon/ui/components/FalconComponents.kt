package com.example.falcon.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.falcon.model.ActiveTask
import com.example.falcon.model.AgentState
import com.example.falcon.model.StepStatus
import com.example.ui.theme.*
import kotlin.random.Random

@Composable
fun FalconGlassSurface(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    borderColor: Color = FalconBorderGlow,
    backgroundColor: Color = FalconSurface.copy(alpha = 0.85f),
    content: @Composable ColumnScope.() -> Unit
) {
    FuturisticGlassSurface(
        modifier = modifier,
        shape = shape,
        borderBrush = Brush.linearGradient(
            listOf(
                borderColor,
                borderColor.copy(alpha = 0.4f),
                FalconBorder
            )
        ),
        backgroundColor = backgroundColor,
        glowColor = FalconCyan,
        glowRadius = 10.dp,
        glowAlpha = 0.15f,
        content = content
    )
}

@Composable
fun FalconHUDHeader(
    assistantName: String,
    state: AgentState,
    onOpenConversation: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Assistant Identity + State Pill
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = assistantName.uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = FalconTextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                FalconStatusBadge(state = state)
            }
            Text(
                text = "NEURAL OS v1.0 • ONLINE",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                ),
                color = FalconTextMuted
            )
        }

        // Right Actions: Quick Log & Settings Icons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FalconIconButton(
                icon = Icons.Default.ChatBubbleOutline,
                contentDescription = "Conversation Logs",
                onClick = onOpenConversation,
                testTag = "open_conversation_btn"
            )
            FalconIconButton(
                icon = Icons.Default.Tune,
                contentDescription = "Settings",
                onClick = onOpenSettings,
                testTag = "open_settings_btn"
            )
        }
    }
}

@Composable
fun FalconStatusBadge(state: AgentState) {
    val (dotColor, textColor, text) = when (state) {
        AgentState.ERROR -> Triple(FalconError, FalconError, "ATTENTION")
        AgentState.SUCCESS -> Triple(FalconSuccess, FalconSuccess, "COMPLETE")
        AgentState.LISTENING -> Triple(FalconCyanBright, FalconCyanBright, "LISTENING")
        AgentState.THINKING -> Triple(FalconVioletLight, FalconVioletLight, "REASONING")
        AgentState.PLANNING -> Triple(FalconVioletLight, FalconVioletLight, "PLANNING")
        AgentState.EXECUTING -> Triple(FalconCyan, FalconCyan, "EXECUTING")
        AgentState.VERIFYING -> Triple(FalconCyanBright, FalconCyanBright, "VERIFYING")
        AgentState.SPEAKING -> Triple(FalconCyanBright, FalconCyanBright, "SPEAKING")
        else -> Triple(FalconSuccess, FalconTextSecondary, "ONLINE")
    }

    val infiniteTransition = rememberInfiniteTransition(label = "badgePulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badgeAlpha"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(dotColor.copy(alpha = 0.12f))
            .border(0.75.dp, dotColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(dotColor.copy(alpha = if (state != AgentState.IDLE) alpha else 1f))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                fontSize = 9.sp,
                letterSpacing = 1.sp
            ),
            color = textColor
        )
    }
}

@Composable
fun FalconIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "icon_btn",
    tint: Color = FalconCyan
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(FalconSurfaceVariant.copy(alpha = 0.6f))
            .border(1.dp, FalconBorder, RoundedCornerShape(12.dp))
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun FalconPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isPrimary: Boolean = true,
    enabled: Boolean = true,
    testTag: String = "pill_button"
) {
    val bgBrush = if (isPrimary) {
        Brush.horizontalGradient(listOf(FalconCyan, FalconCyanDim))
    } else {
        Brush.horizontalGradient(listOf(FalconSurfaceVariant, FalconSurface))
    }

    val contentColor = if (isPrimary) Color(0xFF040710) else FalconCyan

    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = contentColor,
            disabledContainerColor = FalconSurfaceVariant.copy(alpha = 0.5f),
            disabledContentColor = FalconTextMuted
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (enabled) bgBrush else Brush.linearGradient(listOf(FalconSurfaceVariant, FalconSurfaceVariant)))
            .border(1.dp, if (isPrimary) FalconCyanBright.copy(alpha = 0.8f) else FalconBorder, RoundedCornerShape(24.dp))
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = contentColor
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                ),
                color = contentColor
            )
        }
    }
}

@Composable
fun FalconTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    label: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    testTag: String = "falcon_text_field",
    singleLine: Boolean = true
) {
    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                ),
                color = FalconTextSecondary,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag),
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = FalconTextMuted
                )
            },
            trailingIcon = trailingIcon,
            singleLine = singleLine,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = FalconTextPrimary,
                unfocusedTextColor = FalconTextPrimary,
                focusedBorderColor = FalconCyan,
                unfocusedBorderColor = FalconBorder,
                focusedContainerColor = FalconSurfaceVariant.copy(alpha = 0.7f),
                unfocusedContainerColor = FalconSurface.copy(alpha = 0.7f),
                cursorColor = FalconCyan
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun FalconSettingRow(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "setting_row",
    trailingContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(FalconSurfaceVariant)
                .border(0.75.dp, FalconBorder, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = FalconCyan,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                color = FalconTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = FalconTextSecondary
            )
        }

        if (trailingContent != null) {
            trailingContent()
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = FalconTextMuted,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun FalconTaskPanel(
    task: ActiveTask,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    FuturisticGlassSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        borderBrush = Brush.linearGradient(
            listOf(
                FalconCyanBright,
                FalconCyan.copy(alpha = 0.5f),
                FalconBorder
            )
        ),
        glowColor = FalconCyan,
        glowRadius = 16.dp,
        glowAlpha = 0.3f,
        backgroundColor = FalconDarkBgSecondary.copy(alpha = 0.92f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(FalconCyanBright)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AUTONOMOUS TASK MATRIX",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    color = FalconCyan
                )
            }
            TextButton(
                onClick = onCancel,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "ABORT",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = FalconError
                )
            }
        }

        Text(
            text = task.name,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = FalconTextPrimary,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        task.steps.forEach { step ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (step.status) {
                    StepStatus.COMPLETED -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = FalconSuccess,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    StepStatus.RUNNING -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = FalconCyanBright
                        )
                    }
                    StepStatus.FAILED -> {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = "Failed",
                            tint = FalconError,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    StepStatus.PENDING -> {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .border(1.5.dp, FalconTextMuted, CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = step.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = when (step.status) {
                            StepStatus.RUNNING -> FalconCyanBright
                            StepStatus.COMPLETED -> FalconTextPrimary
                            StepStatus.FAILED -> FalconError
                            StepStatus.PENDING -> FalconTextMuted
                        }
                    )
                )
            }
        }
    }
}

@Composable
fun FalconWaveform(
    amplitude: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(28.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val barCount = 18
        for (i in 0 until barCount) {
            val offset = (i - barCount / 2f).let { it * it } / 30f
            val baseHeight = (4f + (amplitude * 24f * (1f - offset))).coerceIn(4f, 26f)

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(baseHeight.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(FalconCyanBright, FalconVioletLight)
                        )
                    )
            )
        }
    }
}

@Composable
fun FalconBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Triple("HOME", Icons.Default.Lens, 0),
        Triple("ACTIVITY", Icons.Default.History, 1),
        Triple("MEMORY", Icons.Default.Psychology, 2),
        Triple("SETTINGS", Icons.Default.Settings, 3)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(FalconSurface.copy(alpha = 0.92f))
                .border(1.dp, FalconBorderGlow, RoundedCornerShape(24.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { (label, icon, index) ->
                val isSelected = selectedTab == index
                val activeBg = if (isSelected) FalconCyan.copy(alpha = 0.15f) else Color.Transparent
                val activeBorder = if (isSelected) FalconCyan.copy(alpha = 0.5f) else Color.Transparent
                val iconTint = if (isSelected) FalconCyanBright else FalconTextMuted

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(activeBg)
                        .border(1.dp, activeBorder, RoundedCornerShape(18.dp))
                        .clickable { onTabSelected(index) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("nav_tab_$index"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                    AnimatedVisibility(visible = isSelected) {
                        Row {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp
                                ),
                                color = FalconCyanBright
                            )
                        }
                    }
                }
            }
        }
    }
}
