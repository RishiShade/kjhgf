package com.example.falcon.ai

import com.example.falcon.data.repository.FalconRepository
import com.example.falcon.model.*
import com.example.falcon.tools.ToolRegistry
import com.example.falcon.tools.ToolResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class AgentExecutionResult(
    val finalResponseText: String,
    val executedTools: List<ExecutedToolInfo>,
    val latencyMs: Long,
    val taskCompleted: Boolean
)

data class ExecutedToolInfo(
    val toolName: String,
    val arguments: String,
    val result: ToolResult
)

class AgentReasoningEngine(
    private val repository: FalconRepository,
    private val toolRegistry: ToolRegistry,
    private val groqClient: GroqApiClient
) {

    suspend fun processUserRequest(
        userInput: String,
        userProfile: UserProfile,
        assistantProfile: AssistantProfile,
        apiConfig: ApiConfiguration,
        onStateUpdate: (AgentState, String) -> Unit,
        onTaskCreated: (ActiveTask) -> Unit,
        onTaskStepUpdated: (String, StepStatus, String?) -> Unit
    ): AgentExecutionResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        // 1. UNDERSTAND & PLAN
        onStateUpdate(AgentState.THINKING, "Analyzing user directive...")

        // Fetch short-term history and long-term memories
        val recentMemories = repository.getRecentMemories()
        val memoryContext = if (recentMemories.isNotEmpty()) {
            "Stored User Knowledge & Preferences:\n" + recentMemories.take(5).joinToString("\n") {
                "- [${it.category}] ${it.title}: ${it.content}"
            }
        } else {
            "No prior persistent memory recorded."
        }

        val systemPrompt = buildString {
            append("You are ${assistantProfile.name}, an advanced autonomous AI operating layer and assistant for Android.\n")
            append("Role: ${assistantProfile.role}\n")
            append("Personality: ${assistantProfile.personality}\n")
            append("User: ${userProfile.preferredName} (Prefers: ${userProfile.responsePreference})\n")
            append("Custom Directives: ${assistantProfile.customInstructions}\n\n")
            append("$memoryContext\n\n")
            append("MANDATE: Whenever the user requests an action on their device (battery, volume, brightness, opening apps, media, flashlight, settings, search, weather, saving memories), select and invoke the appropriate tool. Respond concisely, authoritatively, and naturally without unnecessary filler words.")
        }

        val toolsJson = buildToolsJson()

        val messages = mutableListOf<GroqMessage>()
        messages.add(GroqMessage(role = "system", content = systemPrompt))

        val recentChat = repository.getRecentMessages()
        for (msg in recentChat.takeLast(6)) {
            val role = if (msg.role == "USER") "user" else "assistant"
            messages.add(GroqMessage(role = role, content = msg.content))
        }
        messages.add(GroqMessage(role = "user", content = userInput))

        // 2. DISPATCH TO GROQ / REASONING
        val response = groqClient.chatCompletion(apiConfig, messages, toolsJson)

        val executedTools = mutableListOf<ExecutedToolInfo>()
        var finalReply = response.content

        // 3. EXECUTE TOOLS IF REQUESTED
        if (response.toolCalls.isNotEmpty()) {
            onStateUpdate(AgentState.PLANNING, "Formulating execution sequence...")

            val taskId = "task_${System.currentTimeMillis()}"
            val taskSteps = response.toolCalls.mapIndexed { index, tc ->
                TaskStep(
                    id = "step_$index",
                    title = humanizeToolName(tc.name),
                    status = StepStatus.PENDING
                )
            }

            val activeTask = ActiveTask(
                id = taskId,
                name = "Executing ${response.toolCalls.first().name.replace("_", " ").capitalize()}",
                steps = taskSteps,
                isRunning = true
            )
            onTaskCreated(activeTask)

            val toolExecutionSummaries = mutableListOf<String>()

            for ((index, toolCall) in response.toolCalls.withIndex()) {
                val stepId = "step_$index"
                val humanName = humanizeToolName(toolCall.name)
                onStateUpdate(AgentState.EXECUTING, "$humanName...")
                onTaskStepUpdated(stepId, StepStatus.RUNNING, null)

                // Execute tool
                val result = toolRegistry.executeTool(toolCall.name, toolCall.arguments)
                executedTools.add(ExecutedToolInfo(toolCall.name, toolCall.arguments, result))

                // Verification & Log
                onStateUpdate(AgentState.VERIFYING, "Verifying ${toolCall.name}...")

                val status = if (result.success) StepStatus.COMPLETED else StepStatus.FAILED
                onTaskStepUpdated(stepId, status, result.message)

                repository.logActivity(
                    category = categorizeTool(toolCall.name),
                    action = humanName,
                    details = result.message,
                    status = if (result.success) "SUCCESS" else "FAILED"
                )

                // Save tool interaction in conversation log
                repository.insertMessage(
                    ConversationMessage(
                        role = "TOOL",
                        content = result.message,
                        toolName = toolCall.name,
                        toolInput = toolCall.arguments,
                        toolOutput = result.message,
                        isCollapsible = true
                    )
                )

                toolExecutionSummaries.add(result.message)
            }

            // Synthesize crisp final message
            if (finalReply.isNullOrBlank()) {
                finalReply = toolExecutionSummaries.joinToString(" ")
            }
        }

        if (finalReply.isNullOrBlank()) {
            finalReply = "Task sequence completed."
        }

        val totalLatency = System.currentTimeMillis() - startTime
        onStateUpdate(AgentState.SUCCESS, "Ready.")

        AgentExecutionResult(
            finalResponseText = finalReply,
            executedTools = executedTools,
            latencyMs = totalLatency,
            taskCompleted = true
        )
    }

    private fun buildToolsJson(): JSONArray {
        val array = JSONArray()
        for (tool in toolRegistry.availableTools) {
            val toolObj = JSONObject().apply {
                put("type", "function")
                put("function", JSONObject().apply {
                    put("name", tool.name)
                    put("description", tool.description)
                    put("parameters", JSONObject().apply {
                        put("type", "object")
                        put("properties", JSONObject())
                    })
                })
            }
            array.put(toolObj)
        }
        return array
    }

    private fun humanizeToolName(name: String): String {
        return when (name) {
            "get_battery_status" -> "Checking Battery Level"
            "get_device_info" -> "Retrieving Device Telemetry"
            "get_network_status" -> "Inspecting Network Status"
            "get_volume" -> "Checking Audio Volume"
            "set_volume" -> "Adjusting Media Volume"
            "get_brightness" -> "Checking Screen Brightness"
            "set_brightness" -> "Setting Screen Brightness"
            "toggle_flashlight" -> "Toggling Flashlight"
            "open_system_settings" -> "Launching System Settings"
            "get_current_time" -> "Reading Chronometer"
            "list_installed_apps" -> "Scanning Installed Apps"
            "open_app" -> "Launching Application"
            "play_media" -> "Resuming Media Playback"
            "pause_media" -> "Pausing Active Media"
            "next_track" -> "Skipping Track"
            "previous_track" -> "Previous Track"
            "get_media_state" -> "Checking Media State"
            "web_search" -> "Searching Live Web"
            "get_weather" -> "Fetching Live Weather"
            "get_news" -> "Fetching News Feed"
            "save_memory" -> "Saving to Neural Memory"
            "search_memory" -> "Searching Neural Memory"
            "get_all_memories" -> "Retrieving Knowledge Matrix"
            "calculator" -> "Evaluating Math Calculation"
            else -> name.replace("_", " ").capitalize()
        }
    }

    private fun categorizeTool(name: String): String {
        return when {
            name.contains("app") || name.contains("media") || name.contains("flashlight") -> "Automation"
            name.contains("battery") || name.contains("volume") || name.contains("brightness") || name.contains("settings") || name.contains("time") -> "System"
            name.contains("web") || name.contains("weather") || name.contains("news") -> "Web"
            name.contains("memory") -> "AI"
            else -> "AI"
        }
    }
}
