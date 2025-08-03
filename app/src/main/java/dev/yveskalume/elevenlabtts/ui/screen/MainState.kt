package dev.yveskalume.elevenlabtts.ui.screen


data class MainState(
    val text: String = "",
    val isLoading: Boolean = false,
    val isPlaying: Boolean = false,
    val error: String? = null
)