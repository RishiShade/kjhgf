package com.example.falcon.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.falcon.ai.AgentReasoningEngine
import com.example.falcon.ai.GroqApiClient
import com.example.falcon.data.local.FalconDatabase
import com.example.falcon.data.preferences.FalconPreferencesManager
import com.example.falcon.data.repository.FalconRepository
import com.example.falcon.model.*
import com.example.falcon.tools.ToolRegistry
import com.example.falcon.voice.FalconSpeechRecognizer
import com.example.falcon.voice.FalconTtsEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FalconMainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FalconDatabase.getInstance(application)
    private val preferences = FalconPreferencesManager(application)
    val repository = FalconRepository(db, preferences)
    val toolRegistry = ToolRegistry(application, repository)
    private val groqClient = GroqApiClient()
    private val agentEngine = AgentReasoningEngine(repository, toolRegistry, groqClient)

    val speechRecognizer = FalconSpeechRecognizer(application)
    val ttsEngine = FalconTtsEngine(application, viewModelScope)

    // UI States
    private val _agentState = MutableStateFlow(AgentState.IDLE)
    val agentState: StateFlow<AgentState> = _agentState

    private val _statusDetail = MutableStateFlow("FALCON standing by")
    val statusDetail: StateFlow<String> = _statusDetail

    private val _activeTask = MutableStateFlow<ActiveTask?>(null)
    val activeTask: StateFlow<ActiveTask?> = _activeTask

    private val _quickResponse = MutableStateFlow<String?>(null)
    val quickResponse: StateFlow<String?> = _quickResponse

    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab

    private val _activeSecondaryScreen = MutableStateFlow<String?>(null)
    val activeSecondaryScreen: StateFlow<String?> = _activeSecondaryScreen

    // Data from Repository
    val isOnboardingCompleted: StateFlow<Boolean> = repository.isOnboardingCompleted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    val assistantProfile: StateFlow<AssistantProfile> = repository.assistantProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AssistantProfile())

    val apiConfig: StateFlow<ApiConfiguration> = repository.apiConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ApiConfiguration())

    val orbSettings: StateFlow<OrbSettings> = repository.orbSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OrbSettings())

    val conversationMessages: StateFlow<List<ConversationMessage>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activityLogs: StateFlow<List<ActivityLogItem>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memories: StateFlow<List<MemoryItem>> = repository.allMemories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combined audio amplitude for the 3D Orb
    val audioAmplitude: StateFlow<Float> = combine(
        speechRecognizer.audioAmplitude,
        ttsEngine.speakingAmplitude
    ) { micAmp, ttsAmp ->
        maxOf(micAmp, ttsAmp)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    fun selectTab(index: Int) {
        _currentTab.value = index
        _activeSecondaryScreen.value = null
    }

    fun openSecondaryScreen(screen: String) {
        _activeSecondaryScreen.value = screen
    }

    fun closeSecondaryScreen() {
        _activeSecondaryScreen.value = null
    }

    fun startListening() {
        ttsEngine.stop()
        _agentState.value = AgentState.LISTENING
        _statusDetail.value = "Listening to audio input..."

        speechRecognizer.startListening(
            onResult = { recognizedText ->
                processUserDirective(recognizedText)
            },
            onError = { error ->
                _agentState.value = AgentState.IDLE
                _statusDetail.value = error
            }
        )
    }

    fun stopListening() {
        speechRecognizer.stopListening()
        if (_agentState.value == AgentState.LISTENING) {
            _agentState.value = AgentState.IDLE
            _statusDetail.value = "FALCON standing by"
        }
    }

    fun toggleVoiceListening() {
        if (_agentState.value == AgentState.LISTENING) {
            stopListening()
        } else {
            startListening()
        }
    }

    fun processUserDirective(text: String) {
        if (text.isBlank()) return

        stopListening()
        ttsEngine.stop()

        viewModelScope.launch {
            // Save user message to Room
            repository.insertMessage(
                ConversationMessage(
                    role = "USER",
                    content = text
                )
            )

            val result = agentEngine.processUserRequest(
                userInput = text,
                userProfile = userProfile.value,
                assistantProfile = assistantProfile.value,
                apiConfig = apiConfig.value,
                onStateUpdate = { state, detail ->
                    _agentState.value = state
                    _statusDetail.value = detail
                },
                onTaskCreated = { task ->
                    _activeTask.value = task
                },
                onTaskStepUpdated = { stepId, status, details ->
                    _activeTask.update { current ->
                        current?.copy(
                            steps = current.steps.map {
                                if (it.id == stepId) it.copy(status = status, details = details) else it
                            }
                        )
                    }
                }
            )

            // Save assistant message to Room
            repository.insertMessage(
                ConversationMessage(
                    role = "ASSISTANT",
                    content = result.finalResponseText
                )
            )

            _quickResponse.value = result.finalResponseText

            // Voice synthesis with Gemini / Android TTS
            _agentState.value = AgentState.SPEAKING
            _statusDetail.value = "Speaking response..."
            ttsEngine.speak(result.finalResponseText) {
                _agentState.value = AgentState.IDLE
                _statusDetail.value = "FALCON standing by"
            }
        }
    }

    fun cancelActiveTask() {
        speechRecognizer.stopListening()
        ttsEngine.stop()
        _activeTask.value = null
        _agentState.value = AgentState.IDLE
        _statusDetail.value = "Task sequence aborted."
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            repository.setOnboardingCompleted(true)
        }
    }

    fun saveUserProfile(name: String, preferredName: String, language: String, responsePref: String) {
        viewModelScope.launch {
            repository.saveUserProfile(
                UserProfile(
                    name = name,
                    preferredName = preferredName,
                    language = language,
                    responsePreference = responsePref
                )
            )
        }
    }

    fun saveAssistantProfile(name: String, personality: String, role: String, speakingStyle: String, customInstructions: String) {
        viewModelScope.launch {
            repository.saveAssistantProfile(
                AssistantProfile(
                    name = name,
                    personality = personality,
                    role = role,
                    speakingStyle = speakingStyle,
                    customInstructions = customInstructions
                )
            )
        }
    }

    fun saveApiConfig(groqKey: String, groqModel: String, geminiKey: String, geminiVoice: String, temp: Float, maxTokens: Int, streaming: Boolean) {
        viewModelScope.launch {
            repository.saveApiConfig(
                ApiConfiguration(
                    groqApiKey = groqKey,
                    groqModel = groqModel,
                    geminiApiKey = geminiKey,
                    geminiVoice = geminiVoice,
                    temperature = temp,
                    maxTokens = maxTokens,
                    isStreaming = streaming
                )
            )
        }
    }

    fun saveOrbSettings(density: Float, glow: Float, speed: Float, reduceMotion: Boolean, devMode: Boolean) {
        viewModelScope.launch {
            repository.saveOrbSettings(
                OrbSettings(
                    particleDensity = density,
                    glowIntensity = glow,
                    motionSpeed = speed,
                    reduceMotion = reduceMotion,
                    developerMode = devMode
                )
            )
        }
    }

    fun addMemory(category: String, title: String, content: String) {
        viewModelScope.launch {
            repository.insertMemory(
                MemoryItem(
                    category = category,
                    title = title,
                    content = content,
                    source = "User Entry"
                )
            )
            repository.logActivity("AI", "Added Memory", "Stored '$title'", "SUCCESS")
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            repository.deleteMemoryById(id)
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch {
            repository.clearAllMemories()
        }
    }

    fun clearConversation() {
        viewModelScope.launch {
            repository.clearConversation()
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer.stopListening()
        ttsEngine.release()
    }
}
