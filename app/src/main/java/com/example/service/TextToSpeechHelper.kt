package com.example.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.data.Language
import com.example.data.TranslationDictionary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TextToSpeechHelper(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            try {
                tts?.language = Locale("hi", "IN")
            } catch (e: Exception) {
                tts?.language = Locale.getDefault()
            }
            tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }
            })
        } else {
            Log.e("TextToSpeechHelper", "TTS Initialization failed")
        }
    }

    fun speak(text: String, language: Language) {
        if (!isInitialized || text.isBlank()) return

        val textToSpeak = when (language) {
            Language.SANTALI_OL_CHIKI -> {
                // Transliterate Ol Chiki to phonetic sounds so standard Android TTS produces clear audio
                TranslationDictionary.olChikiToPhoneticLatin(text)
            }
            Language.HO, Language.MUNDARI -> {
                TranslationDictionary.olChikiToPhoneticLatin(text)
            }
            else -> text
        }

        val locale = when (language) {
            Language.HINDI -> Locale("hi", "IN")
            Language.ENGLISH -> Locale.US
            Language.SANTALI_LATIN -> Locale("en", "IN")
            Language.SANTALI_OL_CHIKI, Language.HO, Language.MUNDARI -> Locale("en", "IN")
        }

        try {
            val res = tts?.setLanguage(locale)
            if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.language = Locale.getDefault()
            }
            val utteranceId = "palash_${System.currentTimeMillis()}"
            tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (e: Exception) {
            Log.e("TextToSpeechHelper", "Error speaking text", e)
            _isSpeaking.value = false
        }
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}

