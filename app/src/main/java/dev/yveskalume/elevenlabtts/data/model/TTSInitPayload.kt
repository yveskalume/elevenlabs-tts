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
) {
    constructor(message: String, apiKey: String) : this(
        text = message,
        voiceSettings = VoiceSettings(
            stability = 1.0,
            similarityBoost = 0.8,
            useSpeakerBoost = false
        ),
        generationConfig = GenerationConfig(
            chunkLengthSchedule = listOf(120, 160, 250, 290)
        ),
        apiKey = apiKey
    )
}