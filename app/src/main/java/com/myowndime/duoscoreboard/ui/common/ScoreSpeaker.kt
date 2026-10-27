package com.myowndime.duoscoreboard.ui.common

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.ConfigurationCompat
import java.util.Locale

/**
 * Reads scores out loud through the speech engine already installed on the device.
 *
 * Uses no permission at all - `TextToSpeech` only needs the package visibility entry
 * declared in the manifest. Nothing is sent anywhere; the engine runs on-device.
 */
class ScoreSpeaker(context: Context, locale: Locale) {

    private var engine: TextToSpeech? = null
    private var ready = false

    init {
        engine = TextToSpeech(context.applicationContext) { status ->
            val current = engine
            if (status == TextToSpeech.SUCCESS && current != null) {
                val result = current.setLanguage(locale)
                ready = result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED
            }
        }
    }

    fun speak(text: String) {
        if (!ready) return
        engine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
    }

    fun release() {
        ready = false
        engine?.stop()
        engine?.shutdown()
        engine = null
    }

    private companion object {
        const val UTTERANCE_ID = "duo_scoreboard_score"
    }
}

/**
 * Creates a speaker only while [enabled] is true, and shuts the engine down as soon
 * as the setting is switched off or the screen leaves the composition.
 */
@Composable
fun rememberScoreSpeaker(enabled: Boolean): ScoreSpeaker? {
    val context = LocalContext.current
    val locale = ConfigurationCompat.getLocales(LocalConfiguration.current)[0]
        ?: Locale.getDefault()

    val speaker = remember(enabled, locale) {
        if (enabled) ScoreSpeaker(context, locale) else null
    }
    DisposableEffect(speaker) {
        onDispose { speaker?.release() }
    }
    return speaker
}
