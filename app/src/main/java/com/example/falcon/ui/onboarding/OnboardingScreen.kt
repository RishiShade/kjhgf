package com.example.falcon.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.falcon.model.AgentState
import com.example.falcon.ui.components.FalconGlassSurface
import com.example.falcon.ui.components.FalconPillButton
import com.example.falcon.ui.components.FalconTextField
import com.example.falcon.ui.orb.FalconOrb
import com.example.falcon.ui.viewmodel.FalconMainViewModel
import com.example.ui.theme.*

@Composable
fun OnboardingScreen(
    viewModel: FalconMainViewModel,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableStateOf(1) }
    val totalSteps = 8

    val context = LocalContext.current

    // State holders
    var userName by remember { mutableStateOf("Commander") }
    var assistantName by remember { mutableStateOf("Falcon") }
    var personality by remember { mutableStateOf("Intelligent, concise, and futuristic") }
    var groqApiKey by remember { mutableStateOf("") }
    var selectedVoice by remember { mutableStateOf("Kore") }
    var enableMemory by remember { mutableStateOf(true) }

    var hasMicPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasMicPermission = granted
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FalconDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Progress Bar
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "NEURAL INITIALIZATION",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = FalconCyan
                    )
                    Text(
                        text = "PHASE $step / $totalSteps",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = FalconTextMuted
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { step.toFloat() / totalSteps.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = FalconCyanBright,
                    trackColor = FalconSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Center Dynamic Content per step
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    (fadeIn() + slideInHorizontally { it / 2 })
                        .togetherWith(fadeOut() + slideOutHorizontally { -it / 2 })
                },
                label = "onboardingStep"
            ) { currentStep ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (currentStep) {
                        1 -> Step1MeetFalcon()
                        2 -> Step2UserName(userName = userName, onNameChange = { userName = it })
                        3 -> Step3AssistantName(assistantName = assistantName, onNameChange = { assistantName = it })
                        4 -> Step4Personality(selected = personality, onSelect = { personality = it })
                        5 -> Step5GroqBrain(apiKey = groqApiKey, onKeyChange = { groqApiKey = it })
                        6 -> Step6Voice(selectedVoice = selectedVoice, onVoiceSelect = { selectedVoice = it }, onTest = {
                            viewModel.ttsEngine.speak("Falcon vocal synthesizer synchronized.")
                        })
                        7 -> Step7Permissions(hasMic = hasMicPermission, onRequestMic = {
                            micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        })
                        8 -> Step8Memory(enableMemory = enableMemory, onToggle = { enableMemory = it }, assistantName = assistantName)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 1) {
                    TextButton(onClick = { step-- }) {
                        Text(
                            text = "BACK",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = FalconTextMuted
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                FalconPillButton(
                    text = if (step == totalSteps) "INITIALIZE SYSTEM" else "CONTINUE",
                    onClick = {
                        if (step < totalSteps) {
                            step++
                        } else {
                            // Save configured state
                            viewModel.saveUserProfile(userName, userName, "English (US)", "Concise and Direct")
                            viewModel.saveAssistantProfile(assistantName, personality, "Autonomous AI Operating Layer", "Natural and direct", "Always provide concise, helpful answers. Prefer executing system tools over passive replies.")
                            if (groqApiKey.isNotBlank()) {
                                viewModel.saveApiConfig(groqApiKey, "llama-3.3-70b-versatile", "", selectedVoice, 0.6f, 1024, true)
                            }
                            viewModel.completeOnboarding()
                            onComplete()
                        }
                    },
                    testTag = "onboarding_next_btn"
                )
            }
        }
    }
}

@Composable
private fun Step1MeetFalcon() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            FalconOrb(
                state = AgentState.IDLE,
                audioAmplitude = 0.3f,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "I AM FALCON",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = FalconTextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your autonomous AI assistant living inside your device. Let's calibrate your neural interface.",
            style = MaterialTheme.typography.bodyMedium,
            color = FalconTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun Step2UserName(userName: String, onNameChange: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Person, contentDescription = null, tint = FalconCyan, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "WHAT SHOULD I CALL YOU?",
            style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
            color = FalconTextPrimary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Falcon will address you with this name during voice and text interactions.",
            style = MaterialTheme.typography.bodyMedium,
            color = FalconTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))

        FalconTextField(
            value = userName,
            onValueChange = onNameChange,
            placeholder = "e.g. Commander, Alex, Sarah",
            testTag = "onboarding_username"
        )
    }
}

@Composable
private fun Step3AssistantName(assistantName: String, onNameChange: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.SmartToy, contentDescription = null, tint = FalconCyan, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "WHAT SHOULD I CALL MYSELF?",
            style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
            color = FalconTextPrimary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Customize your AI's callsign and identity.",
            style = MaterialTheme.typography.bodyMedium,
            color = FalconTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))

        FalconTextField(
            value = assistantName,
            onValueChange = onNameChange,
            placeholder = "Falcon / Jarvis / Aegis",
            testTag = "onboarding_assistant_name"
        )
    }
}

@Composable
private fun Step4Personality(selected: String, onSelect: (String) -> Unit) {
    val options = listOf(
        "Intelligent, concise, and futuristic" to "Quick, authoritative responses with sci-fi HUD aesthetics.",
        "Warm & Conversational" to "Friendly, expressive, and detailed conversational tone.",
        "Technical & Precise" to "Exact telemetry, execution details, and low-latency feedback."
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Psychology, contentDescription = null, tint = FalconCyan, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "CHOOSE PERSONALITY",
            style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
            color = FalconTextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))

        options.forEach { (title, desc) ->
            val isSelected = selected == title
            FalconGlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onSelect(title) },
                borderColor = if (isSelected) FalconCyan else FalconBorder,
                backgroundColor = if (isSelected) FalconCyan.copy(alpha = 0.12f) else FalconSurfaceVariant
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = title, style = MaterialTheme.typography.titleSmall, color = FalconTextPrimary)
                        Text(text = desc, style = MaterialTheme.typography.bodySmall, color = FalconTextSecondary)
                    }
                    if (isSelected) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = FalconCyan)
                    }
                }
            }
        }
    }
}

@Composable
private fun Step5GroqBrain(apiKey: String, onKeyChange: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Bolt, contentDescription = null, tint = FalconCyanBright, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "CONNECT NEURAL BRAIN",
            style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
            color = FalconTextPrimary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Connect Groq Cloud API for ultra-fast Llama 3.3 reasoning, or proceed with built-in on-device automation.",
            style = MaterialTheme.typography.bodyMedium,
            color = FalconTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        FalconTextField(
            value = apiKey,
            onValueChange = onKeyChange,
            placeholder = "Optional: Paste Groq API Key (gsk_...)",
            testTag = "onboarding_groq_key"
        )
    }
}

@Composable
private fun Step6Voice(selectedVoice: String, onVoiceSelect: (String) -> Unit, onTest: () -> Unit) {
    val voices = listOf("Kore", "Puck", "Fenrir", "Aoede")

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = FalconCyan, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "GIVE FALCON A VOICE",
            style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
            color = FalconTextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            voices.forEach { v ->
                val isSelected = selectedVoice == v
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) FalconCyan.copy(alpha = 0.2f) else FalconSurfaceVariant)
                        .border(1.dp, if (isSelected) FalconCyan else FalconBorder, RoundedCornerShape(12.dp))
                        .clickable { onVoiceSelect(v) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = v,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isSelected) FalconCyanBright else FalconTextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        FalconPillButton(
            text = "TEST VOICE SYNTHESIS",
            icon = Icons.Default.VolumeUp,
            onClick = onTest,
            isPrimary = false,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun Step7Permissions(hasMic: Boolean, onRequestMic: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Security, contentDescription = null, tint = FalconCyan, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "SYSTEM PERMISSIONS",
            style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
            color = FalconTextPrimary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Falcon requires microphone access for real-time voice conversations.",
            style = MaterialTheme.typography.bodyMedium,
            color = FalconTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        FalconGlassSurface(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = FalconSurface.copy(alpha = 0.7f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Microphone Access", style = MaterialTheme.typography.titleSmall, color = FalconTextPrimary)
                if (hasMic) {
                    Text(text = "GRANTED", color = FalconSuccess, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold))
                } else {
                    Button(onClick = onRequestMic, colors = ButtonDefaults.buttonColors(containerColor = FalconCyan)) {
                        Text("GRANT", color = Color(0xFF040710), style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

@Composable
private fun Step8Memory(enableMemory: Boolean, onToggle: (Boolean) -> Unit, assistantName: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Psychology, contentDescription = null, tint = FalconVioletLight, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "NEURAL MEMORY ACTIVATION",
            style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
            color = FalconTextPrimary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Enable persistent on-device memory so $assistantName can learn your preferences and habits.",
            style = MaterialTheme.typography.bodyMedium,
            color = FalconTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

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
                    Text(text = "On-Device Neural Memory", style = MaterialTheme.typography.titleSmall, color = FalconTextPrimary)
                    Text(text = "Secure local SQLite persistence", style = MaterialTheme.typography.bodySmall, color = FalconTextMuted)
                }
                Switch(
                    checked = enableMemory,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(checkedThumbColor = FalconCyanBright, checkedTrackColor = FalconCyanDim)
                )
            }
        }
    }
}
