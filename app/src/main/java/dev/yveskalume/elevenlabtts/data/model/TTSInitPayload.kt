package dev.yveskalume.elevenlabs.tts.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TTSInitPayload(
    @SerialName("text")
    val text: String,

    @SerialName("voice_settings")
    val voiceSettings: VoiceSettings,

    @SerialName("generation_config")
    val generationConfig: GenerationConfig,

    @SerialName("xi_api_key")
    val apiKey: String
)