package com.example.falcon.data.repository

import com.example.falcon.data.local.FalconDatabase
import com.example.falcon.data.preferences.FalconPreferencesManager
import com.example.falcon.model.*
import kotlinx.coroutines.flow.Flow

class FalconRepository(
    private val database: FalconDatabase,
    val preferences: FalconPreferencesManager
) {
    private val memoryDao = database.memoryDao()
    private val conversationDao = database.conversationDao()
    private val activityLogDao = database.activityLogDao()

    // Memories
    val allMemories: Flow<List<MemoryItem>> = memoryDao.getAllMemories()

    fun getMemoriesByCategory(category: String): Flow<List<MemoryItem>> =
        memoryDao.getMemoriesByCategory(category)

    fun searchMemories(query: String): Flow<List<MemoryItem>> =
        memoryDao.searchMemories(query)

    suspend fun getRecentMemories(): List<MemoryItem> =
        memoryDao.getRecentMemoriesSync()

    suspend fun insertMemory(memory: MemoryItem): Long =
        memoryDao.insertMemory(memory)

    suspend fun updateMemory(memory: MemoryItem) =
        memoryDao.updateMemory(memory)

    suspend fun deleteMemory(memory: MemoryItem) =
        memoryDao.deleteMemory(memory)

    suspend fun deleteMemoryById(id: Long) =
        memoryDao.deleteById(id)

    suspend fun clearAllMemories() =
        memoryDao.clearAll()

    // Conversation
    val allMessages: Flow<List<ConversationMessage>> = conversationDao.getAllMessages()

    suspend fun getRecentMessages(): List<ConversationMessage> =
        conversationDao.getRecentMessagesSync()

    suspend fun insertMessage(message: ConversationMessage): Long =
        conversationDao.insertMessage(message)

    suspend fun clearConversation() =
        conversationDao.clearAll()

    // Activity Logs
    val allLogs: Flow<List<ActivityLogItem>> = activityLogDao.getAllLogs()

    fun getLogsByCategory(category: String): Flow<List<ActivityLogItem>> =
        activityLogDao.getLogsByCategory(category)

    suspend fun logActivity(category: String, action: String, details: String, status: String): Long =
        activityLogDao.insertLog(
            ActivityLogItem(
                category = category,
                action = action,
                details = details,
                status = status
            )
        )

    suspend fun clearLogs() =
        activityLogDao.clearAll()

    // Settings proxies
    val isOnboardingCompleted: Flow<Boolean> = preferences.isOnboardingCompleted
    val userProfile: Flow<UserProfile> = preferences.userProfile
    val assistantProfile: Flow<AssistantProfile> = preferences.assistantProfile
    val apiConfig: Flow<ApiConfiguration> = preferences.apiConfig
    val orbSettings: Flow<OrbSettings> = preferences.orbSettings

    suspend fun setOnboardingCompleted(completed: Boolean) =
        preferences.setOnboardingCompleted(completed)

    suspend fun saveUserProfile(profile: UserProfile) =
        preferences.saveUserProfile(profile)

    suspend fun saveAssistantProfile(profile: AssistantProfile) =
        preferences.saveAssistantProfile(profile)

    suspend fun saveApiConfig(config: ApiConfiguration) =
        preferences.saveApiConfig(config)

    suspend fun saveOrbSettings(settings: OrbSettings) =
        preferences.saveOrbSettings(settings)
}
