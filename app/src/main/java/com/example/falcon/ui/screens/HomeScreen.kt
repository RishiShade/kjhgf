package com.example.falcon.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.falcon.model.AgentState
import com.example.falcon.ui.components.*
import com.example.falcon.ui.orb.FalconOrb
import com.example.falcon.ui.viewmodel.FalconMainViewModel
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: FalconMainViewModel,
    onOpenConversation: () -> Unit,
    onOpenActivity: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val agentState by viewModel.agentState.collectAsState()
    val statusDetail by viewModel.statusDetail.collectAsState()
    val audioAmplitude by viewModel.audioAmplitude.collectAsState()
    val activeTask by viewModel.activeTask.collectAsState()
    val quickResponse by viewModel.quickResponse.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val assistantProfile by viewModel.assistantProfile.collectAsState()
    val orbSettings by viewModel.orbSettings.collectAsState()
    val partialTranscript by viewModel.speechRecognizer.partialTranscript.collectAsState()

    var showTextInput by remember { mutableStateOf(false) }
    var textPrompt by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FalconDarkBg)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -30) {
                        // Swipe up -> open conversation
                        onOpenConversation()
                    } else if (dragAmount > 30) {
                        // Swipe down -> open activity
                        onOpenActivity()
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. TOP HUD HEADER
            FalconHUDHeader(
                assistantName = assistantProfile.name,
                state = agentState,
                onOpenConversation = onOpenConversation,
                onOpenSettings = onOpenSettings
            )

            // 2. ACTIVE FLOATING TASK PANEL (if task is active)
            AnimatedVisibility(
                visible = activeTask != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                activeTask?.let { task ->
                    FalconTaskPanel(
                        task = task,
                        onCancel = { viewModel.cancelActiveTask() },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            }

            // 3. CENTER HERO 3D PARTICLE ORB
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                FalconOrb(
                    state = agentState,
                    audioAmplitude = audioAmplitude,
                    particleDensity = orbSettings.particleDensity,
                    reduceMotion = orbSettings.reduceMotion,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .fillMaxHeight(0.85f),
                    onTap = { viewModel.toggleVoiceListening() },
                    onLongPress = { viewModel.startListening() }
                )
            }

            // 4. DYNAMIC STATUS & CONTEXTUAL FEEDBACK
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dynamic State Title
                Text(
                    text = agentState.label,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = when (agentState) {
                        AgentState.ERROR -> FalconError
                        AgentState.SUCCESS -> FalconSuccess
                        AgentState.LISTENING -> FalconCyanBright
                        AgentState.THINKING, AgentState.PLANNING -> FalconVioletLight
                        AgentState.SPEAKING -> FalconCyanBright
                        else -> FalconCyan
                    },
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Detail message / Subtitle
                Text(
                    text = if (agentState == AgentState.LISTENING && partialTranscript.isNotBlank()) {
                        "\"$partialTranscript\""
                    } else {
                        statusDetail
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = FalconTextSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )

                // Voice Waveform during active listening
                AnimatedVisibility(
                    visible = agentState == AgentState.LISTENING,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    FalconWaveform(
                        amplitude = audioAmplitude,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }

                // Quick Response Card (Tap to view in conversation)
                AnimatedVisibility(
                    visible = quickResponse != null && agentState != AgentState.LISTENING,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    quickResponse?.let { resp ->
                        FuturisticGlassSurface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            shape = RoundedCornerShape(14.dp),
                            borderBrush = Brush.linearGradient(
                                listOf(
                                    FalconCyanBright.copy(alpha = 0.5f),
                                    FalconCyanDim.copy(alpha = 0.2f),
                                    FalconBorder
                                )
                            ),
                            glowColor = FalconCyan,
                            glowRadius = 12.dp,
                            glowAlpha = 0.2f,
                            backgroundColor = FalconDarkBgSecondary.copy(alpha = 0.9f),
                            contentPadding = PaddingValues(14.dp),
                            onClick = { onOpenConversation() },
                            testTag = "quick_response_overlay"
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = resp,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = FalconTextPrimary,
                                    maxLines = 2,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = "View Log",
                                    tint = FalconCyanBright,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. INPUT CONTROLS (Microphone Hero Button & Quick Text Input)
            AnimatedVisibility(visible = showTextInput) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FalconTextField(
                        value = textPrompt,
                        onValueChange = { textPrompt = it },
                        placeholder = "Instruct Falcon (e.g. battery, brightness 50%, open spotify)...",
                        modifier = Modifier.weight(1f),
                        trailingIcon = {
                            if (textPrompt.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        viewModel.processUserDirective(textPrompt)
                                        textPrompt = ""
                                        showTextInput = false
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Send",
                                        tint = FalconCyan
                                    )
                                }
                            }
                        },
                        testTag = "home_text_input"
                    )
                }
            }

            // Floating Controls Bar
            FuturisticGlassSurface(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .wrapContentWidth(),
                shape = RoundedCornerShape(32.dp),
                borderBrush = Brush.linearGradient(
                    listOf(
                        FalconCyanBright.copy(alpha = 0.4f),
                        FalconBorder.copy(alpha = 0.3f),
                        FalconCyan.copy(alpha = 0.15f)
                    )
                ),
                glowColor = FalconCyan,
                glowRadius = 14.dp,
                glowAlpha = 0.18f,
                backgroundColor = FalconDarkBgSecondary.copy(alpha = 0.85f),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Text Input Toggle
                    FalconIconButton(
                        icon = if (showTextInput) Icons.Default.Close else Icons.Default.Keyboard,
                        contentDescription = "Toggle Keyboard Input",
                        onClick = { showTextInput = !showTextInput },
                        testTag = "toggle_keyboard_btn"
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Hero Mic Action Button
                    val isListening = agentState == AgentState.LISTENING
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                if (isListening) Brush.radialGradient(listOf(FalconCyanBright, FalconViolet))
                                else Brush.radialGradient(listOf(FalconCyan, FalconCyanDim))
                            )
                            .clickable { viewModel.toggleVoiceListening() }
                            .testTag("voice_listen_hero_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.GraphicEq else Icons.Default.Mic,
                            contentDescription = "Microphone",
                            tint = Color(0xFF040710),
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Stop / Cancel button
                    FalconIconButton(
                        icon = Icons.Default.Stop,
                        contentDescription = "Stop speech / task",
                        onClick = {
                            viewModel.cancelActiveTask()
                        },
                        tint = FalconError,
                        testTag = "stop_action_btn"
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp)) // Space for floating bottom nav
        }
    }
}
