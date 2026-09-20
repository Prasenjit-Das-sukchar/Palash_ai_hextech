package com.example.data

import java.util.UUID

data class UserProfile(
    val id: String = "user_${UUID.randomUUID().toString().take(8)}",
    val name: String = "Das Prosejit",
    val email: String = "dasprosejit464@gmail.com",
    val isGuest: Boolean = false,
    val firebaseProjectId: String = "prolog-2020",
    val isConnectedToBackend: Boolean = true,
    val token: String = "p2020_${UUID.randomUUID().toString().take(12)}",
    val memberSince: String = "2026"
)

data class TranslationItem(
    val id: String = UUID.randomUUID().toString(),
    val sourceText: String,
    val translatedText: String,
    val sourceLanguage: Language,
    val targetLanguage: Language,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val engineUsed: String = "Gemini 2.5 Flash",
    val isCloudSynced: Boolean = true
)

data class RealtimeSyncEvent(
    val id: String = UUID.randomUUID().toString(),
    val type: String, // "TEXT_TRANSLATE", "VOICE_INPUT_OUTPUT", "AI_LENS_OCR", "FILE_VICE_VERSA"
    val summary: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Cloud Synced"
)

enum class FileConversionStatus {
    QUEUED,
    PROCESSING,
    COMPLETED,
    FAILED
}

data class ConvertedDocument(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val sizeBytes: Long,
    val fileExtension: String,
    val sourceLanguage: Language,
    val targetLanguage: Language,
    val status: FileConversionStatus = FileConversionStatus.QUEUED,
    val originalPreview: String = "",
    val translatedPreview: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedSize: String
        get() = when {
            sizeBytes < 1024 -> "$sizeBytes B"
            sizeBytes < 1024 * 1024 -> "${sizeBytes / 1024} KB"
            else -> String.format("%.1f MB", sizeBytes / (1024.0 * 1024.0))
        }
}

data class LensScanResult(
    val id: String = UUID.randomUUID().toString(),
    val detectedText: String,
    val translatedText: String,
    val sourceLanguage: Language,
    val targetLanguage: Language,
    val confidence: Float = 0.96f,
    val timestamp: Long = System.currentTimeMillis()
)
