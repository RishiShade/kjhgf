package com.example.falcon.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.falcon.model.ActivityLogItem
import com.example.falcon.ui.components.FalconGlassSurface
import com.example.falcon.ui.components.FalconIconButton
import com.example.falcon.ui.viewmodel.FalconMainViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ActivityScreen(
    viewModel: FalconMainViewModel,
    modifier: Modifier = Modifier
) {
    val activityLogs by viewModel.activityLogs.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Automation", "AI", "Voice", "Web", "System")

    val filteredLogs = remember(activityLogs, selectedCategory) {
        if (selectedCategory == "All") activityLogs
        else activityLogs.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FalconDarkBg)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ACTIVITY MATRIX",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    color = FalconTextPrimary
                )
                Text(
                    text = "Operational execution log & telemetry",
                    style = MaterialTheme.typography.bodySmall,
                    color = FalconTextSecondary
                )
            }

            if (activityLogs.isNotEmpty()) {
                FalconIconButton(
                    icon = Icons.Default.DeleteSweep,
                    contentDescription = "Clear Activity Logs",
                    onClick = { viewModel.clearLogs() },
                    tint = FalconError,
                    testTag = "clear_activity_btn"
                )
            }
        }

        // Filter Category Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            items(categories) { cat ->
                val isSelected = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) FalconCyan.copy(alpha = 0.2f) else FalconSurfaceVariant)
                        .border(
                            1.dp,
                            if (isSelected) FalconCyan else FalconBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = cat.uppercase(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        ),
                        color = if (isSelected) FalconCyanBright else FalconTextMuted
                    )
                }
            }
        }

        // List of Activity Items
        if (filteredLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.HistoryToggleOff,
                        contentDescription = null,
                        tint = FalconTextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No operational events logged yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FalconTextMuted
                    )
                    Text(
                        text = "Falcon's automated tasks and queries will appear here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = FalconTextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 90.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredLogs, key = { it.id }) { log ->
                    ActivityLogCard(log = log)
                }
            }
        }
    }
}

@Composable
private fun ActivityLogCard(log: ActivityLogItem) {
    val icon = when (log.category.lowercase()) {
        "automation" -> Icons.Default.SmartToy
        "voice" -> Icons.Default.Mic
        "web" -> Icons.Default.Public
        "system" -> Icons.Default.SettingsSuggest
        else -> Icons.Default.AutoAwesome
    }

    val isSuccess = log.status == "SUCCESS"
    val timeStr = remember(log.timestamp) {
        val sdf = SimpleDateFormat("hh:mm:ss a • MMM d", Locale.getDefault())
        sdf.format(Date(log.timestamp))
    }

    FalconGlassSurface(
        shape = RoundedCornerShape(12.dp),
        borderColor = if (isSuccess) FalconBorder else FalconError.copy(alpha = 0.4f),
        backgroundColor = FalconSurface.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(FalconSurfaceVariant)
                    .border(0.5.dp, FalconBorder, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSuccess) FalconCyan else FalconError,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = log.action,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = FalconTextPrimary
                    )

                    // Status Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSuccess) FalconSuccess.copy(alpha = 0.15f) else FalconError.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = log.status,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isSuccess) FalconSuccess else FalconError
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = log.details,
                    style = MaterialTheme.typography.bodyMedium,
                    color = FalconTextSecondary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    ),
                    color = FalconTextMuted
                )
            }
        }
    }
}
