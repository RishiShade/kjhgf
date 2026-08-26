package com.example.falcon.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.falcon.model.AgentState
import com.example.falcon.model.ApiConfiguration
import com.example.falcon.model.AssistantProfile
import com.example.falcon.model.OrbSettings
import com.example.falcon.model.UserProfile
import com.example.falcon.ui.components.FalconGlassSurface
import com.example.falcon.ui.components.FalconIconButton
import com.example.falcon.ui.components.FalconPillButton
import com.example.falcon.ui.components.FalconTextField
import com.example.falcon.ui.orb.FalconOrb
import com.example.falcon.ui.viewmodel.FalconMainViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SettingsDetailContainer(
    section: String,
    viewModel: FalconMainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (section) {
        "assistant_profile" -> AssistantProfileScreen(viewModel, onBack, modifier)
        "user_profile" -> UserProfileScreen(viewModel, onBack, modifier)
        "ai_config" -> AiConfigScreen(viewModel, onBack, modifier)
        "voice_config" -> VoiceConfigScreen(viewModel, onBack, modifier)
        "automation" -> AutomationScreen(viewModel, onBack, modifier)
        "permissions" -> PermissionsScreen(viewModel, onBack, modifier)
        "appearance" -> AppearanceScreen(viewModel, onBack, modifier)
        "privacy" -> PrivacyScreen(viewModel, onBack, modifier)
        "debug" -> DebugScreen(viewModel, onBack, modifier)
        "about" -> AboutScreen(viewModel, onBack, modifier)
        else -> Box(modifier = Modifier.fillMaxSize())
    }
}

// 1. ASSISTANT PROFILE SCREEN
@Composable
fun AssistantProfileScreen(
    viewModel: FalconMainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentProfile by viewModel.assistantProfile.collectAsState()
    var name by remember(currentProfile) { mutableStateOf(currentProfile.name) }
    var personality by remember(currentProfile) { mutableStateOf(currentProfile.personality) }
    var role by remember(currentProfile) { mutableStateOf(currentProfile.role) }
    var speakingStyle by remember(currentProfile) { mutableStateOf(currentProfile.speakingStyle) }
    var instructions by remember(currentProfile) { mutableStateOf(currentProfile.customInstructions) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FalconDarkBg)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(bottom = 40.dp)
    ) {
        TopDetailBar(title = "ASSISTANT IDENTITY", onBack = onBack)

        FalconTextField(
            value = name,
            onValueChange = { name = it },
            label = "Assistant Name",
            placeholder = "Falcon"
        )
        Spacer(modifier = Modifier.height(14.dp))

        FalconTextField(
            value = personality,
            onValueChange = { personality = it },
            label = "Personality Archetype",
            placeholder = "Intelligent, concise, and futuristic"
        )
        Spacer(modifier = Modifier.height(14.dp))

        FalconTextField(
            value = role,
            onValueChange = { role = it },
            label = "Autonomous Role",
            placeholder = "Autonomous AI Operating Layer"
        )
        Spacer(modifier = Modifier.height(14.dp))

        FalconTextField(
            value = speakingStyle,
            onValueChange = { speakingStyle = it },
            label = "Speaking Style",
            placeholder = "Natural and direct"
        )
        Spacer(modifier = Modifier.height(14.dp))

        FalconTextField(
            value = instructions,
            onValueChange = { instructions = it },
            label = "Custom Directives & Guidelines",
            placeholder = "Always provide concise, helpful answers. Prefer executing system tools over passive replies.",
            singleLine = false,
            modifier = Modifier.heightIn(min = 120.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        FalconPillButton(
            text = "SAVE ASSISTANT PROFILE",
            onClick = {
                viewModel.saveAssistantProfile(name, personality, role, speakingStyle, instructions)
                onBack()
            },
            modifier = Modifier.fillMaxWidth(),
            testTag = "save_assistant_profile_btn"
        )
    }
}

// 2. USER PROFILE SCREEN
@Composable
fun UserProfileScreen(
    viewModel: FalconMainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentProfile by viewModel.userProfile.collectAsState()
    var name by remember(currentProfile) { mutableStateOf(currentProfile.name) }
    var preferredName by remember(currentProfile) { mutableStateOf(currentProfile.preferredName) }
    var language by remember(currentProfile) { mutableStateOf(currentProfile.language) }
    var responsePref by remember(currentProfile) { mutableStateOf(currentProfile.responsePreference) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FalconDarkBg)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(bottom = 40.dp)
    ) {
        TopDetailBar(title = "USER PROFILE", onBack = onBack)

        FalconTextField(
            value = name,
            onValueChange = { name = it },
            label = "Full Name",
            placeholder = "Alex Mercer"
        )
        Spacer(modifier = Modifier.height(14.dp))

        FalconTextField(
            value = preferredName,
            onValueChange = { preferredName = it },
            label = "How Falcon Addresses You",
            placeholder = "Commander / Alex"
        )
        Spacer(modifier = Modifier.height(14.dp))

        FalconTextField(
            value = language,
            onValueChange = { language = it },
            label = "Primary Language",
            placeholder = "English (US)"
        )
        Spacer(modifier = Modifier.height(14.dp))

        FalconTextField(
            value = responsePref,
            onValueChange = { responsePref = it },
            label = "Response Style Preference",
            placeholder = "Concise and Direct"
        )

        Spacer(modifier = Modifier.height(24.dp))

        FalconPillButton(
            text = "SAVE USER PROFILE",
            onClick = {
                viewModel.saveUserProfile(name, preferredName, language, responsePref)
                onBack()
            },
            modifier = Modifier.fillMaxWidth(),
            testTag = "save_user_profile_btn"
        )
    }
}

// 3. AI CONFIG SCREEN
@Composable
fun AiConfigScreen(
    viewModel: FalconMainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentConfig by viewModel.apiConfig.collectAsState()
    var groqKey by remember(currentConfig) { mutableStateOf(currentConfig.groqApiKey) }
    var groqModel by remember(currentConfig) { mutableStateOf(currentConfig.groqModel) }
    var geminiKey by remember(currentConfig) { mutableStateOf(currentConfig.geminiApiKey) }
    var temperature by remember(currentConfig) { mutableStateOf(currentConfig.temperature) }
    var maxTokens by remember(currentConfig) { mutableStateOf(currentConfig.maxTokens) }
    var isStreaming by remember(currentConfig) { mutableStateOf(currentConfig.isStreaming) }

    val models = listOf("llama-3.3-70b-versatile", "llama-3.1-8b-instant", "mixtral-8x7b-32768")
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FalconDarkBg)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(bottom = 40.dp)
    ) {
        TopDetailBar(title = "AI BRAIN (GROQ)", onBack = onBack)

        FalconTextField(
            value = groqKey,
            onValueChange = { groqKey = it },
            label = "Groq API Key",
            placeholder = "gsk_...",
            testTag = "groq_api_key_input"
        )
        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "REASONING MODEL",
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = FalconTextSecondary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        models.forEach { m ->
            val isSelected = groqModel == m
            FalconGlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { groqModel = m },
                borderColor = if (isSelected) FalconCyan else FalconBorder,
                backgroundColor = if (isSelected) FalconCyan.copy(alpha = 0.1f) else FalconSurfaceVariant
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = m, color = if (isSelected) FalconCyanBright else FalconTextPrimary)
                    if (isSelected) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = FalconCyan)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Temperature Slider
        Text(
            text = "TEMPERATURE: %.2f".format(temperature),
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = FalconTextSecondary
        )
        Slider(
            value = temperature,
            onValueChange = { temperature = it },
            valueRange = 0.0f..1.0f,
            colors = SliderDefaults.colors(
                thumbColor = FalconCyanBright,
                activeTrackColor = FalconCyan,
                inactiveTrackColor = FalconSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        FalconTextField(
            value = geminiKey,
            onValueChange = { geminiKey = it },
            label = "Gemini API Key (Voice & Fallback)",
            placeholder = "AIzaSy..."
        )

        Spacer(modifier = Modifier.height(24.dp))

        FalconPillButton(
            text = "SAVE CONFIGURATION",
            onClick = {
                viewModel.saveApiConfig(groqKey, groqModel, geminiKey, currentConfig.geminiVoice, temperature, maxTokens, isStreaming)
                onBack()
            },
            modifier = Modifier.fillMaxWidth(),
            testTag = "save_ai_config_btn"
        )
    }
}

// 4. VOICE CONFIG SCREEN
@Composable
fun VoiceConfigScreen(
    viewModel: FalconMainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentConfig by viewModel.apiConfig.collectAsState()
    var geminiVoice by remember(currentConfig) { mutableStateOf(currentConfig.geminiVoice) }
    val voices = listOf("Kore", "Puck", "Fenrir", "Aoede")

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FalconDarkBg)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(bottom = 40.dp)
    ) {
        TopDetailBar(title = "VOICE & SPEECH", onBack = onBack)

        Text(
            text = "SYNTHESIS VOICE PROFILE",
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = FalconTextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        voices.forEach { v ->
            val isSelected = geminiVoice == v
            FalconGlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { geminiVoice = v },
                borderColor = if (isSelected) FalconCyan else FalconBorder,
                backgroundColor = if (isSelected) FalconCyan.copy(alpha = 0.1f) else FalconSurfaceVariant
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = v, style = MaterialTheme.typography.titleMedium, color = FalconTextPrimary)
                        Text(text = "Natural neural vocal synthesis", style = MaterialTheme.typography.bodySmall, color = FalconTextMuted)
                    }
                    if (isSelected) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = FalconCyan)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        FalconGlassSurface(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = FalconSurface.copy(alpha = 0.7f)
        ) {
            Text(
                text = "TEST VOICE SYNTHESIS",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                color = FalconCyan
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Falcon features low-latency voice synthesis with responsive 3D Orb particle modulation.",
                style = MaterialTheme.typography.bodySmall,
                color = FalconTextSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            FalconPillButton(
                text = "PLAY SAMPLE AUDIO",
                icon = Icons.Default.VolumeUp,
                onClick = {
                    viewModel.ttsEngine.speak("Falcon autonomous neural link active. All system telemetry standing by.")
                },
                isPrimary = false,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        FalconPillButton(
            text = "APPLY VOICE SETTINGS",
            onClick = {
                viewModel.saveApiConfig(currentConfig.groqApiKey, currentConfig.groqModel, currentConfig.geminiApiKey, geminiVoice, currentConfig.temperature, currentConfig.maxTokens, currentConfig.isStreaming)
                onBack()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// 5. AUTOMATION HUB SCREEN
@Composable
fun AutomationScreen(
    viewModel: FalconMainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tools = viewModel.toolRegistry.availableTools

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FalconDarkBg)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = 20.dp)
    ) {
        TopDetailBar(title = "AUTOMATION MATRIX", onBack = onBack)

        Text(
            text = "ACTIVE TOOLS & CAPABILITIES (${tools.size})",
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
            color = FalconCyan,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(tools) { tool ->
                FalconGlassSurface(
                    shape = RoundedCornerShape(10.dp),
                    backgroundColor = FalconSurface.copy(alpha = 0.7f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(FalconCyan.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = tool.category.name,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = FalconCyan
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = tool.name,
                                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = FontFamily.Monospace),
                                    color = FalconTextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tool.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = FalconTextSecondary
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Active",
                            tint = FalconSuccess,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// 6. PERMISSIONS SCREEN
@Composable
fun PermissionsScreen(
    viewModel: FalconMainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var hasMicPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasMicPermission = granted
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FalconDarkBg)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(bottom = 40.dp)
    ) {
        TopDetailBar(title = "PERMISSIONS & SECURITY", onBack = onBack)

        Text(
            text = "DEVICE ACCESS STATUS",
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
            color = FalconCyan,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Microphone
        PermissionRow(
            title = "Microphone Access",
            description = "Required for live voice input and audio waveform stream",
            isGranted = hasMicPermission,
            onRequest = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Camera / Flashlight
        PermissionRow(
            title = "Camera & Flashlight",
            description = "Required for autonomous torch automation and vision capabilities",
            isGranted = hasCameraPermission,
            onRequest = { cameraLauncher.launch(Manifest.permission.CAMERA) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // System Settings
        PermissionRow(
            title = "Android System Settings",
            description = "Adjusting brightness, volume, and launching system toggles",
            isGranted = true,
            onRequest = {
                val intent = Intent(Settings.ACTION_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                context.startActivity(intent)
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        FalconGlassSurface(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = FalconSurface.copy(alpha = 0.7f)
        ) {
            Text(
                text = "OPEN APP SETTINGS",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                color = FalconCyan
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Open system app settings to grant granular overlay or write permissions.",
                style = MaterialTheme.typography.bodySmall,
                color = FalconTextSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            FalconPillButton(
                text = "MANAGE SYSTEM PERMISSIONS",
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                },
                isPrimary = false,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PermissionRow(
    title: String,
    description: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    FalconGlassSurface(
        shape = RoundedCornerShape(12.dp),
        borderColor = if (isGranted) FalconBorder else FalconError.copy(alpha = 0.4f),
        backgroundColor = FalconSurface.copy(alpha = 0.75f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isGranted) FalconSuccess else FalconError)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = title, style = MaterialTheme.typography.titleSmall, color = FalconTextPrimary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = FalconTextSecondary)
            }

            if (isGranted) {
                Text(
                    text = "ACTIVE",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = FalconSuccess,
                        fontWeight = FontWeight.Bold
                    )
                )
            } else {
                Button(
                    onClick = onRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = FalconCyan),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "GRANT",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF040710)
                    )
                }
            }
        }
    }
}

// 7. APPEARANCE SCREEN
@Composable
fun AppearanceScreen(
    viewModel: FalconMainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSettings by viewModel.orbSettings.collectAsState()
    var density by remember(currentSettings) { mutableStateOf(currentSettings.particleDensity) }
    var glow by remember(currentSettings) { mutableStateOf(currentSettings.glowIntensity) }
    var speed by remember(currentSettings) { mutableStateOf(currentSettings.motionSpeed) }
    var reduceMotion by remember(currentSettings) { mutableStateOf(currentSettings.reduceMotion) }
    var devMode by remember(currentSettings) { mutableStateOf(currentSettings.developerMode) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FalconDarkBg)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(bottom = 40.dp)
    ) {
        TopDetailBar(title = "APPEARANCE & ORB", onBack = onBack)

        // Live Orb Mini-Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(FalconSurfaceVariant)
                .border(1.dp, FalconBorder, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            FalconOrb(
                state = AgentState.IDLE,
                audioAmplitude = 0.2f,
                particleDensity = density,
                reduceMotion = reduceMotion,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Particle Density Slider
        Text(
            text = "PARTICLE DENSITY: %.1fx".format(density),
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = FalconTextSecondary
        )
        Slider(
            value = density,
            onValueChange = { density = it },
            valueRange = 0.5f..2.0f,
            colors = SliderDefaults.colors(thumbColor = FalconCyanBright, activeTrackColor = FalconCyan)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Glow Intensity Slider
        Text(
            text = "GLOW INTENSITY: %.1fx".format(glow),
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = FalconTextSecondary
        )
        Slider(
            value = glow,
            onValueChange = { glow = it },
            valueRange = 0.5f..1.5f,
            colors = SliderDefaults.colors(thumbColor = FalconCyanBright, activeTrackColor = FalconCyan)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Reduce Motion Toggle
        FalconGlassSurface(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = FalconSurface.copy(alpha = 0.7f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Reduce Motion", style = MaterialTheme.typography.titleSmall, color = FalconTextPrimary)
                    Text(text = "Minimizes particle orbit speed for battery saving", style = MaterialTheme.typography.bodySmall, color = FalconTextMuted)
                }
                Switch(
                    checked = reduceMotion,
                    onCheckedChange = { reduceMotion = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = FalconCyanBright, checkedTrackColor = FalconCyanDim)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        FalconPillButton(
            text = "SAVE ORB SETTINGS",
            onClick = {
                viewModel.saveOrbSettings(density, glow, speed, reduceMotion, devMode)
                onBack()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// 8. PRIVACY SCREEN
@Composable
fun PrivacyScreen(
    viewModel: FalconMainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FalconDarkBg)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(bottom = 40.dp)
    ) {
        TopDetailBar(title = "PRIVACY & STORAGE", onBack = onBack)

        FalconGlassSurface(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = FalconSurface.copy(alpha = 0.7f)
        ) {
            Text(
                text = "LOCAL-FIRST ARCHITECTURE",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                color = FalconCyan
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "All neural memories, operational logs, and conversation matrices are stored exclusively on-device in an encrypted Room SQLite database.",
                style = MaterialTheme.typography.bodySmall,
                color = FalconTextSecondary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        FalconGlassSurface(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = FalconSurface.copy(alpha = 0.7f)
        ) {
            Text(
                text = "DATA PURGING CONTROLS",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                color = FalconError
            )
            Spacer(modifier = Modifier.height(12.dp))

            FalconPillButton(
                text = "PURGE NEURAL MEMORY",
                onClick = { viewModel.clearAllMemories() },
                isPrimary = false,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            FalconPillButton(
                text = "CLEAR ACTIVITY LOGS",
                onClick = { viewModel.clearLogs() },
                isPrimary = false,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            FalconPillButton(
                text = "CLEAR CHAT CONVERSATION",
                onClick = { viewModel.clearConversation() },
                isPrimary = false,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// 9. DEBUG SCREEN
@Composable
fun DebugScreen(
    viewModel: FalconMainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val apiConfig by viewModel.apiConfig.collectAsState()
    val agentState by viewModel.agentState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FalconDarkBg)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(bottom = 40.dp)
    ) {
        TopDetailBar(title = "DEBUG CONSOLE", onBack = onBack)

        FalconGlassSurface(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFF030712)
        ) {
            Text(
                text = "=== FALCON SYSTEM TELEMETRY ===",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = FalconCyan
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = buildString {
                    append("STATE: ${agentState.name}\n")
                    append("LLM_PROVIDER: Groq Cloud API\n")
                    append("MODEL: ${apiConfig.groqModel}\n")
                    append("TEMPERATURE: ${apiConfig.temperature}\n")
                    append("MAX_TOKENS: ${apiConfig.maxTokens}\n")
                    append("VOICE_ENGINE: Gemini / Android TTS (1.05x)\n")
                    append("DATABASE: SQLite Room v1 (falcon_neural_db)\n")
                    append("TOOLS_REGISTERED: 18 active executors\n")
                    append("BUILD_TARGET: Android 15 (API 35)")
                },
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = FalconSuccess,
                    fontSize = 11.sp,
                    lineHeight = 18.sp
                )
            )
        }
    }
}

// 10. ABOUT SCREEN
@Composable
fun AboutScreen(
    viewModel: FalconMainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FalconDarkBg)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopDetailBar(title = "ABOUT FALCON", onBack = onBack)

        Spacer(modifier = Modifier.height(30.dp))

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(FalconSurfaceVariant)
                .border(2.dp, FalconCyan, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = FalconCyanBright,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "FALCON AI",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = FalconTextPrimary
        )

        Text(
            text = "Autonomous AI Operating Layer for Android",
            style = MaterialTheme.typography.bodyMedium,
            color = FalconCyan
        )

        Spacer(modifier = Modifier.height(24.dp))

        FalconGlassSurface(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = FalconSurface.copy(alpha = 0.7f)
        ) {
            Text(
                text = "VERSION: 1.0.0 (Release Build)",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = FalconTextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Falcon is designed to think, understand, and actuate system automation directly on-device with seamless conversational intelligence.",
                style = MaterialTheme.typography.bodySmall,
                color = FalconTextSecondary
            )
        }
    }
}

@Composable
private fun TopDetailBar(
    title: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FalconIconButton(
            icon = Icons.Default.ArrowBack,
            contentDescription = "Back",
            onClick = onBack,
            testTag = "detail_back_btn"
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = FalconTextPrimary
        )
    }
}
