package dev.yveskalume.elevenlabtts.data

import android.util.Log
import dev.yveskalume.elevenlabs.tts.model.Audio
import dev.yveskalume.elevenlabs.tts.model.TTSInitPayload
import dev.yveskalume.elevenlabs.tts.model.TTSResponse
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

interface SessionManager {

    suspend fun sendMessage(
        message: String,
        onSuccess: (audio: Audio) -> Unit,
    )

    companion object {
        fun create(apiKey: String, voiceId: String, modelId: String): SessionManager {
            val client = NetworkClient(voiceId, modelId)
            return SessionManagerImpl(apiKey, client)
        }
    }
}

private class SessionManagerImpl(
    private val apiKey: String,
    private val client: NetworkClient
) : SessionManager {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private lateinit var onSuccess: (Audio) -> Unit

    override suspend fun sendMessage(
        message: String,
        onSuccess: (audio: Audio) -> Unit,
    ) {
        this.onSuccess = onSuccess

        if (!client.checkSession()) {
            client.connect()
            sendFirstMessage(message)
            receiveLoop()
        } else {
            client.send(Frame.Text(buildJson(message)))
        }
        sendCloseMessage()
    }


    private suspend fun sendCloseMessage() {
        client.send(Frame.Text(buildJson("")))
    }

    private fun buildJson(message: String): String {
        return buildJsonObject {
            put("message", message)
        }.toString()
    }

    private suspend fun sendFirstMessage(message: String) {
        val init = TTSInitPayload(message, apiKey)
        val value = json.encodeToString<TTSInitPayload>(init)
        client.send(Frame.Text(value))
    }

    private fun receiveLoop() {
        CoroutineScope(Dispatchers.IO).launch {
            val incoming = client.incoming ?: return@launch
            for (frame in incoming) {
                if (frame is Frame.Text) handleTTSResponse(frame)
            }
        }
    }

    private suspend fun handleTTSResponse(
        frame: Frame.Text,
    ) {
        val response = json.decodeFromString<TTSResponse>(frame.readText())
        response.audio?.let { onSuccess(Audio(it)) }
        if (response.isFinal == true) {
            client.close()
        }
    }
}
