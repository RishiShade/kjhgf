package com.example.falcon.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AgentState(val label: String, val subtitle: String) {
    IDLE("READY", "FALCON standing by"),
    LISTENING("LISTENING", "Receiving audio stream..."),
    THINKING("THINKING", "Processing neural reasoning..."),
    PLANNING("PLANNING", "Formulating action sequence..."),
    EXECUTING("EXECUTING", "Running system automation..."),
    VERIFYING("VERIFYING", "Verifying system state..."),
    SPEAKING("SPEAKING", "Synthesizing vocal response..."),
    SUCCESS("COMPLETE", "Task executed successfully"),
    ERROR("ATTENTION", "An anomaly occurred")
}

@Entity(tableName = "memories")
data class MemoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String, // "Personal", "Preference", "Workflow", "Fact"
    val title: String,
    val content: String,
    val source: String = "User Interaction",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "conversation_messages")
data class ConversationMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val role: String, // "USER", "ASSISTANT", "TOOL", "SYSTEM"
    val content: String,
    val toolName: String? = null,
    val toolInput: String? = null,
    val toolOutput: String? = null,
    val isCollapsible: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "activity_logs")
data class ActivityLogItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String, // "Automation", "AI", "Voice", "Web", "System"
    val action: String,
    val details: String,
    val status: String, // "SUCCESS", "FAILED", "RUNNING"
    val timestamp: Long = System.currentTimeMillis()
)

data class TaskStep(
    val id: String,
    val title: String,
    val status: StepStatus = StepStatus.PENDING,
    val details: String? = null
)

enum class StepStatus {
    PENDING, RUNNING, COMPLETED, FAILED
}

data class ActiveTask(
    val id: String,
    val name: String,
    val steps: List<TaskStep>,
    val isRunning: Boolean = true
)

data class UserProfile(
    val name: String = "Commander",
    val preferredName: String = "Commander",
    val language: String = "English (US)",
    val responsePreference: String = "Concise and Direct"
)

data class AssistantProfile(
    val name: String = "Falcon",
    val personality: String = "Intelligent, concise, and futuristic",
    val role: String = "Autonomous AI Operating Layer",
    val speakingStyle: String = "Natural and direct",
    val customInstructions: String = "Always provide concise, helpful answers. Prefer executing system tools over passive replies."
)

data class ApiConfiguration(
    val groqApiKey: String = "",
    val groqModel: String = "llama-3.3-70b-versatile",
    val geminiApiKey: String = "",
    val geminiVoice: String = "Kore",
    val temperature: Float = 0.6f,
    val maxTokens: Int = 1024,
    val isStreaming: Boolean = true
)

data class OrbSettings(
    val particleDensity: Float = 1.0f,
    val glowIntensity: Float = 1.0f,
    val motionSpeed: Float = 1.0f,
    val reduceMotion: Boolean = false,
    val developerMode: Boolean = false
)
