package com.example.falcon.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.falcon.model.ConversationMessage
import com.example.falcon.ui.components.FalconGlassSurface
import com.example.falcon.ui.components.FalconIconButton
import com.example.falcon.ui.components.FalconTextField
import com.example.falcon.ui.viewmodel.FalconMainViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ConversationScreen(
    viewModel: FalconMainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.conversationMessages.collectAsState()
    val assistantProfile by viewModel.assistantProfile.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Scroll to bottom when messages change
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FalconDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FalconIconButton(
                    icon = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    onClick = onBack,
                    testTag = "conv_back_btn"
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "COMMUNICATION MATRIX",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = FalconTextPrimary
                    )
                    Text(
                        text = "${assistantProfile.name} • Live Neural Link",
                        style = MaterialTheme.typography.bodySmall,
                        color = FalconCyan
                    )
                }
            }

            if (messages.isNotEmpty()) {
                FalconIconButton(
                    icon = Icons.Default.DeleteSweep,
                    contentDescription = "Clear Chat",
                    onClick = { viewModel.clearConversation() },
                    tint = FalconError,
                    testTag = "clear_conv_btn"
                )
            }
        }

        // Messages List
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            if (messages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Forum,
                            contentDescription = null,
                            tint = FalconTextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No communication history yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = FalconTextMuted
                        )
                        Text(
                            text = "Directives sent via voice or text will be indexed here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = FalconTextMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(messages, key = { it.id }) { msg ->
                        MessageBubble(
                            message = msg,
                            assistantName = assistantProfile.name,
                            userName = userProfile.preferredName,
                            onSpeak = { text -> viewModel.ttsEngine.speak(text) }
                        )
                    }
                }
            }
        }

        // Bottom Input Row
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = FalconDarkBgSecondary,
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FalconTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = "Send instruction to ${assistantProfile.name}...",
                    modifier = Modifier.weight(1f),
                    trailingIcon = {
                        if (inputText.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    val text = inputText
                                    inputText = ""
                                    viewModel.processUserDirective(text)
                                    coroutineScope.launch {
                                        if (messages.isNotEmpty()) {
                                            listState.animateScrollToItem(messages.size - 1)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = FalconCyanBright
                                )
                            }
                        }
                    },
                    testTag = "conv_text_field"
                )

                Spacer(modifier = Modifier.width(10.dp))

                IconButton(
                    onClick = { viewModel.toggleVoiceListening() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(FalconCyan)
                        .testTag("conv_mic_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = Color(0xFF040710),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: ConversationMessage,
    assistantName: String,
    userName: String,
    onSpeak: (String) -> Unit
) {
    val isUser = message.role == "USER"
    val isTool = message.role == "TOOL"

    if (isTool) {
        // Collapsible technical tool status row
        var isExpanded by remember { mutableStateOf(false) }
        FalconGlassSurface(
            shape = RoundedCornerShape(8.dp),
            borderColor = FalconBorder,
            backgroundColor = FalconSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = FalconCyanDim,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TOOL: ${message.toolName ?: "System Action"}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = FalconCyan
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = FalconTextMuted,
                    modifier = Modifier.size(14.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 6.dp)) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        color = FalconTextSecondary
                    )
                }
            }
        }
    } else {
        // Regular User or Assistant Message Bubble
        val alignment = if (isUser) Alignment.End else Alignment.Start
        val bgColor = if (isUser) FalconSurfaceVariant.copy(alpha = 0.85f) else FalconSurface.copy(alpha = 0.95f)
        val borderColor = if (isUser) FalconBorder else FalconCyan.copy(alpha = 0.4f)
        val senderLabel = if (isUser) userName.uppercase() else assistantName.uppercase()

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = alignment
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = senderLabel,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = if (isUser) FalconTextMuted else FalconCyan
                )
            }

            Box(
                modifier = Modifier
                    .widthIn(max = 310.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(bgColor)
                    .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = FalconTextPrimary
                    )

                    if (!isUser) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = { onSpeak(message.content) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Read aloud",
                                    tint = FalconCyanDim,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
