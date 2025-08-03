package dev.yveskalume.elevenlabtts.data

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.url
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kotlinx.coroutines.channels.ReceiveChannel

interface NetworkClient {

    val incoming: ReceiveChannel<Frame>?
    suspend fun connect()
    suspend fun send(frame: Frame)

    suspend fun checkSession(): Boolean
    suspend fun close()

    companion object Companion {
        operator fun invoke(voiceId: String, modelId: String): NetworkClient {
            return NetworkClientImpl(voiceId, modelId)
        }
    }
}

private class NetworkClientImpl(
    private val voiceId: String, private val modelId: String,
    private val httpClient: HttpClient = createClient()
) : NetworkClient {

    private var session: DefaultClientWebSocketSession? = null

    override val incoming: ReceiveChannel<Frame>?
        get() = session?.incoming

    override suspend fun connect() {
        val uri =
            "wss://api.elevenlabs.io/v1/text-to-speech/${voiceId}/stream-input?model_id=${modelId}"
        session = httpClient.webSocketSession { url(uri) }
    }

    override suspend fun send(frame: Frame) {
        session?.send(frame)
    }

    override suspend fun checkSession(): Boolean {
        return session != null
    }

    override suspend fun close() {
        session?.close(CloseReason(CloseReason.Codes.NORMAL, "TTS finished"))
        session = null
    }
}
