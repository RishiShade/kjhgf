package com.example.falcon.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.falcon.model.MemoryItem
import com.example.falcon.ui.components.FalconGlassSurface
import com.example.falcon.ui.components.FalconIconButton
import com.example.falcon.ui.components.FalconPillButton
import com.example.falcon.ui.components.FalconTextField
import com.example.falcon.ui.viewmodel.FalconMainViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MemoryScreen(
    viewModel: FalconMainViewModel,
    modifier: Modifier = Modifier
) {
    val memories by viewModel.memories.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "Personal", "Preference", "Workflow", "Fact")

    val filteredMemories = remember(memories, searchQuery, selectedCategory) {
        memories.filter { mem ->
            val matchCategory = selectedCategory == "All" || mem.category.equals(selectedCategory, ignoreCase = true)
            val matchQuery = searchQuery.isBlank() ||
                    mem.title.contains(searchQuery, ignoreCase = true) ||
                    mem.content.contains(searchQuery, ignoreCase = true)
            matchCategory && matchQuery
        }
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
                    text = "NEURAL MEMORY",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    color = FalconTextPrimary
                )
                Text(
                    text = "Long-term AI knowledge bank & user context",
                    style = MaterialTheme.typography.bodySmall,
                    color = FalconTextSecondary
                )
            }

            FalconIconButton(
                icon = Icons.Default.Add,
                contentDescription = "Add New Memory",
                onClick = { showAddDialog = true },
                testTag = "add_memory_btn"
            )
        }

        // Search Bar
        FalconTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = "Search memories, preferences, facts...",
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = FalconTextMuted)
                    }
                } else {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = FalconCyan)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            testTag = "memory_search_input"
        )

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

        // Memory Items List
        if (filteredMemories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = FalconTextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isNotBlank()) "No memories match '$searchQuery'" else "No neural memories saved yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FalconTextMuted
                    )
                    Text(
                        text = "Say 'Falcon, remember that I like concise answers' or tap + above.",
                        style = MaterialTheme.typography.bodySmall,
                        color = FalconTextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 90.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredMemories, key = { it.id }) { mem ->
                    MemoryItemCard(
                        memory = mem,
                        onDelete = { viewModel.deleteMemory(mem.id) }
                    )
                }
            }
        }
    }

    // Add Memory Dialog
    if (showAddDialog) {
        AddMemoryDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { cat, title, content ->
                viewModel.addMemory(cat, title, content)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun MemoryItemCard(
    memory: MemoryItem,
    onDelete: () -> Unit
) {
    val timeStr = remember(memory.timestamp) {
        val sdf = SimpleDateFormat("MMM d, yyyy • hh:mm a", Locale.getDefault())
        sdf.format(Date(memory.timestamp))
    }

    FalconGlassSurface(
        shape = RoundedCornerShape(12.dp),
        borderColor = FalconBorder,
        backgroundColor = FalconSurface.copy(alpha = 0.75f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Category Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(FalconVioletLight.copy(alpha = 0.15f))
                            .border(0.5.dp, FalconVioletLight.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = memory.category.uppercase(),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = FalconVioletLight
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = memory.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = FalconTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = memory.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = FalconTextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Source: ${memory.source}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = FalconTextMuted
                    )
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

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Memory",
                    tint = FalconTextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun AddMemoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (category: String, title: String, content: String) -> Unit
) {
    var category by remember { mutableStateOf("Preference") }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    val categories = listOf("Personal", "Preference", "Workflow", "Fact")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FalconDarkBgSecondary,
        title = {
            Text(
                text = "STORE IN NEURAL MEMORY",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = FalconCyan
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Category Picker
                Text(
                    text = "CATEGORY",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = FalconTextSecondary
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        val isSelected = category == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) FalconCyan.copy(alpha = 0.2f) else FalconSurfaceVariant)
                                .border(1.dp, if (isSelected) FalconCyan else FalconBorder, RoundedCornerShape(8.dp))
                                .clickable { category = cat }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isSelected) FalconCyanBright else FalconTextMuted,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                FalconTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Title",
                    placeholder = "e.g. Favorite Music App",
                    testTag = "memory_dialog_title"
                )

                FalconTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = "Memory Fact / Detail",
                    placeholder = "e.g. User prefers Spotify over other players.",
                    singleLine = false,
                    modifier = Modifier.heightIn(min = 90.dp),
                    testTag = "memory_dialog_content"
                )
            }
        },
        confirmButton = {
            FalconPillButton(
                text = "SAVE MEMORY",
                onClick = {
                    if (content.isNotBlank()) {
                        onConfirm(category, if (title.isBlank()) "User Preference" else title, content)
                    }
                },
                enabled = content.isNotBlank(),
                testTag = "save_memory_confirm_btn"
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = FalconTextMuted, fontFamily = FontFamily.Monospace)
            }
        }
    )
}
