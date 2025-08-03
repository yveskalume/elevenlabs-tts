package dev.yveskalume.elevenlabs.tts.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VoiceSettings(
    @SerialName("stability")
    val stability: Double = 0.5,
    @SerialName("similarity_boost")
    val similarityBoost: Double = 0.8,
    @SerialName("use_speaker_boost")
    val useSpeakerBoost: Boolean = false
)