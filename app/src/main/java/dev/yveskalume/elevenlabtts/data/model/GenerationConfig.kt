package dev.yveskalume.elevenlabs.tts.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GenerationConfig(
    @SerialName("chunk_length_schedule")
    val chunkLengthSchedule: List<Int> = listOf(120, 160, 250, 290)
)