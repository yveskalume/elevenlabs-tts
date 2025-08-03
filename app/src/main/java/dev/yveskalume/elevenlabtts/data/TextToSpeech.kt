package dev.yveskalume.elevenlabtts.data

import dev.yveskalume.elevenlabs.tts.model.Audio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface TextToSpeech {

    suspend fun speak(message: String, onSuccess: (Audio) -> Unit)

    companion object {
        operator fun invoke(apiKey: String, voiceId: String, modelId: String): TextToSpeech {
            return TextToSpeechImpl(
                apiKey,
                voiceId,
                modelId
            )
        }
    }
}

private class TextToSpeechImpl(
    private val apiKey: String,
    private val voiceId: String,
    private val modelId: String,
    private val sessionHandler: SessionManager = SessionManager.create(apiKey, voiceId, modelId)
) : TextToSpeech {

    override suspend fun speak(message: String, onSuccess: (Audio) -> Unit) {
        withContext(Dispatchers.IO) {
            sessionHandler.sendMessage(
                message,
                onSuccess = onSuccess,
            )
        }
    }
}