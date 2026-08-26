package com.example.falcon.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.BuildConfig
import com.example.falcon.model.ApiConfiguration
import com.example.falcon.model.AssistantProfile
import com.example.falcon.model.OrbSettings
import com.example.falcon.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "falcon_settings")

class FalconPreferencesManager(private val context: Context) {

    private object PreferencesKeys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_PREFERRED_NAME = stringPreferencesKey("user_preferred_name")
        val USER_LANGUAGE = stringPreferencesKey("user_language")
        val USER_RESPONSE_PREF = stringPreferencesKey("user_response_pref")

        val ASSISTANT_NAME = stringPreferencesKey("assistant_name")
        val ASSISTANT_PERSONALITY = stringPreferencesKey("assistant_personality")
        val ASSISTANT_ROLE = stringPreferencesKey("assistant_role")
        val ASSISTANT_SPEAKING_STYLE = stringPreferencesKey("assistant_speaking_style")
        val ASSISTANT_CUSTOM_INSTRUCTIONS = stringPreferencesKey("assistant_custom_instructions")

        val GROQ_API_KEY = stringPreferencesKey("groq_api_key")
        val GROQ_MODEL = stringPreferencesKey("groq_model")
        val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val GEMINI_VOICE = stringPreferencesKey("gemini_voice")
        val AI_TEMPERATURE = floatPreferencesKey("ai_temperature")
        val AI_MAX_TOKENS = intPreferencesKey("ai_max_tokens")
        val AI_STREAMING = booleanPreferencesKey("ai_streaming")

        val ORB_PARTICLE_DENSITY = floatPreferencesKey("orb_particle_density")
        val ORB_GLOW_INTENSITY = floatPreferencesKey("orb_glow_intensity")
        val ORB_MOTION_SPEED = floatPreferencesKey("orb_motion_speed")
        val ORB_REDUCE_MOTION = booleanPreferencesKey("orb_reduce_motion")
        val DEVELOPER_MODE = booleanPreferencesKey("developer_mode")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .catch { emitEmptyOnException(it) }
        .map { it[PreferencesKeys.ONBOARDING_COMPLETED] ?: false }

    val userProfile: Flow<UserProfile> = context.dataStore.data
        .catch { emitEmptyOnException(it) }
        .map { prefs ->
            UserProfile(
                name = prefs[PreferencesKeys.USER_NAME] ?: "Commander",
                preferredName = prefs[PreferencesKeys.USER_PREFERRED_NAME] ?: "Commander",
                language = prefs[PreferencesKeys.USER_LANGUAGE] ?: "English (US)",
                responsePreference = prefs[PreferencesKeys.USER_RESPONSE_PREF] ?: "Concise and Direct"
            )
        }

    val assistantProfile: Flow<AssistantProfile> = context.dataStore.data
        .catch { emitEmptyOnException(it) }
        .map { prefs ->
            AssistantProfile(
                name = prefs[PreferencesKeys.ASSISTANT_NAME] ?: "Falcon",
                personality = prefs[PreferencesKeys.ASSISTANT_PERSONALITY] ?: "Intelligent, concise, and futuristic",
                role = prefs[PreferencesKeys.ASSISTANT_ROLE] ?: "Autonomous AI Operating Layer",
                speakingStyle = prefs[PreferencesKeys.ASSISTANT_SPEAKING_STYLE] ?: "Natural and direct",
                customInstructions = prefs[PreferencesKeys.ASSISTANT_CUSTOM_INSTRUCTIONS] ?: "Always provide concise, helpful answers. Prefer executing system tools over passive replies."
            )
        }

    val apiConfig: Flow<ApiConfiguration> = context.dataStore.data
        .catch { emitEmptyOnException(it) }
        .map { prefs ->
            val defaultGemini = try { BuildConfig.GEMINI_API_KEY } catch (e: Throwable) { "" }
            val defaultGroq = try { BuildConfig.GROQ_API_KEY } catch (e: Throwable) { "" }
            ApiConfiguration(
                groqApiKey = prefs[PreferencesKeys.GROQ_API_KEY] ?: defaultGroq,
                groqModel = prefs[PreferencesKeys.GROQ_MODEL] ?: "llama-3.3-70b-versatile",
                geminiApiKey = prefs[PreferencesKeys.GEMINI_API_KEY] ?: defaultGemini,
                geminiVoice = prefs[PreferencesKeys.GEMINI_VOICE] ?: "Kore",
                temperature = prefs[PreferencesKeys.AI_TEMPERATURE] ?: 0.6f,
                maxTokens = prefs[PreferencesKeys.AI_MAX_TOKENS] ?: 1024,
                isStreaming = prefs[PreferencesKeys.AI_STREAMING] ?: true
            )
        }

    val orbSettings: Flow<OrbSettings> = context.dataStore.data
        .catch { emitEmptyOnException(it) }
        .map { prefs ->
            OrbSettings(
                particleDensity = prefs[PreferencesKeys.ORB_PARTICLE_DENSITY] ?: 1.0f,
                glowIntensity = prefs[PreferencesKeys.ORB_GLOW_INTENSITY] ?: 1.0f,
                motionSpeed = prefs[PreferencesKeys.ORB_MOTION_SPEED] ?: 1.0f,
                reduceMotion = prefs[PreferencesKeys.ORB_REDUCE_MOTION] ?: false,
                developerMode = prefs[PreferencesKeys.DEVELOPER_MODE] ?: false
            )
        }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.USER_NAME] = profile.name
            prefs[PreferencesKeys.USER_PREFERRED_NAME] = profile.preferredName
            prefs[PreferencesKeys.USER_LANGUAGE] = profile.language
            prefs[PreferencesKeys.USER_RESPONSE_PREF] = profile.responsePreference
        }
    }

    suspend fun saveAssistantProfile(profile: AssistantProfile) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.ASSISTANT_NAME] = profile.name
            prefs[PreferencesKeys.ASSISTANT_PERSONALITY] = profile.personality
            prefs[PreferencesKeys.ASSISTANT_ROLE] = profile.role
            prefs[PreferencesKeys.ASSISTANT_SPEAKING_STYLE] = profile.speakingStyle
            prefs[PreferencesKeys.ASSISTANT_CUSTOM_INSTRUCTIONS] = profile.customInstructions
        }
    }

    suspend fun saveApiConfig(config: ApiConfiguration) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.GROQ_API_KEY] = config.groqApiKey
            prefs[PreferencesKeys.GROQ_MODEL] = config.groqModel
            prefs[PreferencesKeys.GEMINI_API_KEY] = config.geminiApiKey
            prefs[PreferencesKeys.GEMINI_VOICE] = config.geminiVoice
            prefs[PreferencesKeys.AI_TEMPERATURE] = config.temperature
            prefs[PreferencesKeys.AI_MAX_TOKENS] = config.maxTokens
            prefs[PreferencesKeys.AI_STREAMING] = config.isStreaming
        }
    }

    suspend fun saveOrbSettings(settings: OrbSettings) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.ORB_PARTICLE_DENSITY] = settings.particleDensity
            prefs[PreferencesKeys.ORB_GLOW_INTENSITY] = settings.glowIntensity
            prefs[PreferencesKeys.ORB_MOTION_SPEED] = settings.motionSpeed
            prefs[PreferencesKeys.ORB_REDUCE_MOTION] = settings.reduceMotion
            prefs[PreferencesKeys.DEVELOPER_MODE] = settings.developerMode
        }
    }

    private fun emitEmptyOnException(exception: Throwable): Preferences {
        if (exception is IOException) {
            return emptyPreferences()
        }
        throw exception
    }
}
