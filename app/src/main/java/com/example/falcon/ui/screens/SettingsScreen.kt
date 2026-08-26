package com.example.falcon.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.falcon.ui.components.FalconGlassSurface
import com.example.falcon.ui.components.FalconSettingRow
import com.example.falcon.ui.viewmodel.FalconMainViewModel
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: FalconMainViewModel,
    onNavigateToSection: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val assistantProfile by viewModel.assistantProfile.collectAsState()
    val apiConfig by viewModel.apiConfig.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FalconDarkBg)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(bottom = 90.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CONTROL MATRIX",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    color = FalconTextPrimary
                )
                Text(
                    text = "System preferences, AI brain & device automation",
                    style = MaterialTheme.typography.bodySmall,
                    color = FalconTextSecondary
                )
            }
        }

        // Section 1: AI IDENTITY & USER
        Text(
            text = "IDENTITY & PROFILES",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            ),
            color = FalconCyan,
            modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
        )

        FalconGlassSurface(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = FalconSurface.copy(alpha = 0.75f)
        ) {
            FalconSettingRow(
                title = "Assistant Identity",
                description = "${assistantProfile.name} • ${assistantProfile.personality}",
                icon = Icons.Default.SmartToy,
                onClick = { onNavigateToSection("assistant_profile") }
            )
            Divider(color = FalconBorder, thickness = 0.5.dp)
            FalconSettingRow(
                title = "User Profile",
                description = "${userProfile.preferredName} • ${userProfile.responsePreference}",
                icon = Icons.Default.Person,
                onClick = { onNavigateToSection("user_profile") }
            )
        }

        // Section 2: AI & NEURAL SUBSYSTEMS
        Text(
            text = "AI ENGINE & SPEECH",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            ),
            color = FalconCyan,
            modifier = Modifier.padding(top = 20.dp, bottom = 6.dp)
        )

        FalconGlassSurface(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = FalconSurface.copy(alpha = 0.75f)
        ) {
            FalconSettingRow(
                title = "AI Brain (Groq API)",
                description = "Model: ${apiConfig.groqModel} • Temp: ${apiConfig.temperature}",
                icon = Icons.Default.Psychology,
                onClick = { onNavigateToSection("ai_config") }
            )
            Divider(color = FalconBorder, thickness = 0.5.dp)
            FalconSettingRow(
                title = "Voice & Speech Subsystem",
                description = "Gemini / Android TTS • Speech Rate 1.05x",
                icon = Icons.Default.RecordVoiceOver,
                onClick = { onNavigateToSection("voice_config") }
            )
        }

        // Section 3: AUTOMATION & PERMISSIONS
        Text(
            text = "SYSTEM & HARDWARE INTEGRATION",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            ),
            color = FalconCyan,
            modifier = Modifier.padding(top = 20.dp, bottom = 6.dp)
        )

        FalconGlassSurface(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = FalconSurface.copy(alpha = 0.75f)
        ) {
            FalconSettingRow(
                title = "Automation Hub & Tools",
                description = "18 active tools (Battery, Volume, Brightness, Apps, Web)",
                icon = Icons.Default.Build,
                onClick = { onNavigateToSection("automation") }
            )
            Divider(color = FalconBorder, thickness = 0.5.dp)
            FalconSettingRow(
                title = "Device Permissions Hub",
                description = "Microphone, Camera, Audio, System Settings",
                icon = Icons.Default.Security,
                onClick = { onNavigateToSection("permissions") }
            )
        }

        // Section 4: INTERFACE & DIAGNOSTICS
        Text(
            text = "INTERFACE & DIAGNOSTICS",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            ),
            color = FalconCyan,
            modifier = Modifier.padding(top = 20.dp, bottom = 6.dp)
        )

        FalconGlassSurface(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = FalconSurface.copy(alpha = 0.75f)
        ) {
            FalconSettingRow(
                title = "Appearance & 3D Orb",
                description = "Particle density, glow intensity, motion dynamics",
                icon = Icons.Default.Palette,
                onClick = { onNavigateToSection("appearance") }
            )
            Divider(color = FalconBorder, thickness = 0.5.dp)
            FalconSettingRow(
                title = "Privacy & Neural Memory Bank",
                description = "Manage local SQLite Room database & data deletion",
                icon = Icons.Default.Lock,
                onClick = { onNavigateToSection("privacy") }
            )
            Divider(color = FalconBorder, thickness = 0.5.dp)
            FalconSettingRow(
                title = "Developer Debug Console",
                description = "Real-time telemetry, model metrics, execution trace",
                icon = Icons.Default.Terminal,
                onClick = { onNavigateToSection("debug") }
            )
            Divider(color = FalconBorder, thickness = 0.5.dp)
            FalconSettingRow(
                title = "About Falcon AI",
                description = "Autonomous AI Operating Layer • v1.0.0",
                icon = Icons.Default.Info,
                onClick = { onNavigateToSection("about") }
            )
        }
    }
}
