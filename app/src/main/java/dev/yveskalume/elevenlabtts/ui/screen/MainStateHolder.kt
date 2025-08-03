package dev.yveskalume.elevenlabtts.ui.screen

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dev.yveskalume.elevenlabtts.data.TextToSpeech
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun rememberMainStateHolder(): MainStateHolder {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    return remember {
        MainStateHolder(context = context, coroutineScope = coroutineScope)
    }
}

class MainStateHolder(private val context: Context, private val coroutineScope: CoroutineScope) {

    private val _state = MutableStateFlow(MainState())
    val state = _state.asStateFlow()

    private val exoPlayer = ExoPlayer.Builder(context).build()

    private val tts = TextToSpeech(
        apiKey = " ",
        voiceId = " ",
        modelId = "eleven_flash_v2_5"
    )

    fun speak() {
        coroutineScope.launch {
            _state.update { it.copy(isLoading = true) }
            val text = state.value.text
            tts.speak(text) { chunk ->
                addAudioChunkToPlaylist(chunk.audioBase64)
                setUpPlayerIfItsFirstTime()
            }
        }
    }

    fun updateText(text: String) {
        _state.update { it.copy(text = text) }
    }

    fun playOrPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }


    private fun addAudioChunkToPlaylist(audioBase64: String) {
        try {
            val tempFile = createTemFile(audioBase64)
            val mediaItem = MediaItem.fromUri(tempFile.toURI().toString())
            coroutineScope.launch(Dispatchers.Main) {
                exoPlayer.addMediaItem(mediaItem)
            }
            Log.e("addAudioChunkToPlaylist", "Audio chunk added to playlist")
        } catch (e: Exception) {
            Log.e(e.message, "Error adding audio chunk to playlist", e)
            _state.update { it.copy(isLoading = false, error = e.message) }
        }
    }

    private fun createTemFile(audioBase64: String): File {
        val decodedBytes = Base64.decode(audioBase64, Base64.DEFAULT)
        val tempFile = File.createTempFile("audio", ".mp3", context.cacheDir)
        val fos = FileOutputStream(tempFile)
        fos.write(decodedBytes)
        fos.close()
        return tempFile
    }

    private fun setUpPlayerIfItsFirstTime() {
        coroutineScope.launch(Dispatchers.Main) {
            if (!exoPlayer.isPlaying && exoPlayer.mediaItemCount > 0) {
                exoPlayer.prepare()
                exoPlayer.play()
                _state.update {
                    it.copy(isPlaying = true)
                }

                exoPlayer.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_ENDED) {
                            _state.update {
                                it.copy(isPlaying = false)
                            }
                            exoPlayer.removeListener(this)
                            exoPlayer.clearMediaItems()
                        }
                    }
                })
            }
            _state.update { it.copy(isLoading = false) }
        }
    }
}