package dev.yveskalume.elevenlabs.tts.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TTSResponse(
    val audio: String? = null,
    @SerialName("isFinal")
    val isFinal: Boolean? = false
)