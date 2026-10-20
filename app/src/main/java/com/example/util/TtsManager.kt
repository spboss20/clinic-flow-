package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TtsManager(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isReady = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isReady = true
            tts?.language = Locale.forLanguageTag("en-IN")
        }
    }

    fun speak(text: String, isHindi: Boolean = false) {
        if (!isReady || tts == null) return
        try {
            if (isHindi) {
                val hiLocale = Locale.forLanguageTag("hi-IN")
                val result = tts?.setLanguage(hiLocale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.language = Locale.forLanguageTag("en-IN")
                }
            } else {
                tts?.language = Locale.forLanguageTag("en-IN")
            }
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ClinicFlowTTS")
        } catch (_: Exception) {
            // Graceful fallback
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {
            // Graceful fallback
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {
            // Graceful fallback
        }
        tts = null
        isReady = false
    }
}
