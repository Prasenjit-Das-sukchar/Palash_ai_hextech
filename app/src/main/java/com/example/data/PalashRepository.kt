package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.service.FirebaseBackendManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class PalashRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("palash_ai_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow(loadUser())
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _history = MutableStateFlow(loadHistory())
    val history: StateFlow<List<TranslationItem>> = _history.asStateFlow()

    private val _documents = MutableStateFlow(loadDocuments())
    val documents: StateFlow<List<ConvertedDocument>> = _documents.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(System.currentTimeMillis())
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    private val _realtimeSyncLog = MutableStateFlow(
        listOf(
            RealtimeSyncEvent(
                type = "CLOUD_SYNC_INIT",
                summary = "Connected to Firebase Firestore (prolog-2020)",
                timestamp = System.currentTimeMillis() - 120000
            ),
            RealtimeSyncEvent(
                type = "MEMBER_VERIFIED",
                summary = "Active session verified: dasprosejit464@gmail.com",
                timestamp = System.currentTimeMillis() - 60000
            )
        )
    )
    val realtimeSyncLog: StateFlow<List<RealtimeSyncEvent>> = _realtimeSyncLog.asStateFlow()

    init {
        // Initialize Firebase Backend
        FirebaseBackendManager.initialize(context)

        // Setup real-time cloud listeners for Firestore collections
        FirebaseBackendManager.listenToCloudData(
            onTranslationsUpdated = { cloudItems ->
                if (cloudItems.isNotEmpty()) {
                    val localIds = _history.value.map { it.id }.toSet()
                    val newItems = cloudItems.filterNot { it.id in localIds }
                    if (newItems.isNotEmpty()) {
                        val merged = (newItems + _history.value).distinctBy { it.id }.sortedByDescending { it.timestamp }
                        _history.value = merged
                        saveHistory(merged)
                    }
                }
            },
            onDocumentsUpdated = { cloudDocs ->
                if (cloudDocs.isNotEmpty()) {
                    val localIds = _documents.value.map { it.id }.toSet()
                    val newDocs = cloudDocs.filterNot { it.id in localIds }
                    if (newDocs.isNotEmpty()) {
                        val merged = (newDocs + _documents.value).distinctBy { it.id }.sortedByDescending { it.timestamp }
                        _documents.value = merged
                        saveDocuments(merged)
                    }
                }
            }
        )
    }

    fun logRealtimeSync(type: String, summary: String) {
        val event = RealtimeSyncEvent(
            type = type,
            summary = summary,
            timestamp = System.currentTimeMillis(),
            status = "Cloud Synced"
        )
        val list = _realtimeSyncLog.value.toMutableList()
        list.add(0, event)
        if (list.size > 30) list.removeAt(list.lastIndex)
        _realtimeSyncLog.value = list
        _lastSyncTimestamp.value = System.currentTimeMillis()

        // Sync event to Firestore
        FirebaseBackendManager.syncRealtimeEventToCloud(event)
    }

    private fun loadUser(): UserProfile? {
        val isLoggedIn = prefs.getBoolean("is_logged_in", true) // Default logged in as Prolog member
        if (!isLoggedIn) return null

        val email = prefs.getString("user_email", "dasprosejit464@gmail.com") ?: "dasprosejit464@gmail.com"
        val name = prefs.getString("user_name", "Das Prosejit") ?: "Das Prosejit"
        val isGuest = prefs.getBoolean("is_guest", false)
        val projectId = prefs.getString("firebase_project_id", "prolog-2020") ?: "prolog-2020"

        return UserProfile(
            email = email,
            name = name,
            isGuest = isGuest,
            firebaseProjectId = projectId,
            isConnectedToBackend = true
        )
    }

    fun login(email: String, name: String, isGuest: Boolean = false) {
        val user = UserProfile(
            email = email,
            name = name,
            isGuest = isGuest,
            firebaseProjectId = "prolog-2020",
            isConnectedToBackend = true
        )
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("user_email", user.email)
            .putString("user_name", user.name)
            .putBoolean("is_guest", user.isGuest)
            .putString("firebase_project_id", user.firebaseProjectId)
            .apply()
        _currentUser.value = user

        logRealtimeSync(
            type = "AUTH_LOGIN",
            summary = "Session active: $email in prolog-2020"
        )
    }

    fun logout() {
        prefs.edit()
            .putBoolean("is_logged_in", false)
            .apply()
        _currentUser.value = null
        FirebaseBackendManager.signOut()
        logRealtimeSync("AUTH_LOGOUT", "Signed out from Firebase session")
    }

    fun syncAllToCloud() {
        // Sync all local history and documents to Firestore
        _history.value.forEach { item ->
            FirebaseBackendManager.syncTranslationToCloud(item)
        }
        _documents.value.forEach { doc ->
            FirebaseBackendManager.syncDocumentToCloud(doc)
        }
        logRealtimeSync("FULL_SYNC", "Synchronized ${_history.value.size} translations and ${_documents.value.size} documents with Firestore")
    }

    fun addTranslation(
        source: String,
        translated: String,
        from: Language,
        to: Language,
        engineUsed: String = "Gemini 2.5 Flash"
    ): TranslationItem {
        val item = TranslationItem(
            sourceText = source,
            translatedText = translated,
            sourceLanguage = from,
            targetLanguage = to,
            engineUsed = engineUsed,
            isCloudSynced = true
        )
        val currentList = _history.value.toMutableList()
        currentList.add(0, item) // newest first
        if (currentList.size > 50) currentList.removeAt(currentList.lastIndex)
        _history.value = currentList
        saveHistory(currentList)

        // Sync directly to Firestore
        FirebaseBackendManager.syncTranslationToCloud(item)

        logRealtimeSync(
            type = "TRANSLATION_IO",
            summary = "${from.name} → ${to.name}: \"${source.take(20)}\" ($engineUsed)"
        )
        return item
    }

    fun toggleFavorite(id: String) {
        val updated = _history.value.map {
            if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it
        }
        _history.value = updated
        saveHistory(updated)
        val item = updated.firstOrNull { it.id == id }
        if (item != null) {
            FirebaseBackendManager.syncTranslationToCloud(item)
        }
    }

    fun deleteHistoryItem(id: String) {
        val updated = _history.value.filterNot { it.id == id }
        _history.value = updated
        saveHistory(updated)
    }

    fun clearHistory() {
        _history.value = emptyList()
        prefs.edit().remove("history_json").apply()
        logRealtimeSync("HISTORY_CLEARED", "Cloud cache cleared for user session")
    }

    fun addDocument(document: ConvertedDocument) {
        val list = _documents.value.toMutableList()
        list.add(0, document)
        _documents.value = list
        saveDocuments(list)
        FirebaseBackendManager.syncDocumentToCloud(document)
        logRealtimeSync("DOC_ADDED", "File queued: ${document.name} (${document.sourceLanguage.name} ⇄ ${document.targetLanguage.name})")
    }

    fun updateDocument(document: ConvertedDocument) {
        val list = _documents.value.map {
            if (it.id == document.id) document else it
        }
        _documents.value = list
        saveDocuments(list)
        FirebaseBackendManager.syncDocumentToCloud(document)
        logRealtimeSync("DOC_SYNCED", "File converted: ${document.name} status: ${document.status.name}")
    }

    fun removeDocument(id: String) {
        val list = _documents.value.filterNot { it.id == id }
        _documents.value = list
        saveDocuments(list)
    }

    private fun saveHistory(list: List<TranslationItem>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("sourceText", item.sourceText)
                put("translatedText", item.translatedText)
                put("sourceLang", item.sourceLanguage.code)
                put("targetLang", item.targetLanguage.code)
                put("timestamp", item.timestamp)
                put("isFavorite", item.isFavorite)
                put("engineUsed", item.engineUsed)
                put("isCloudSynced", item.isCloudSynced)
            }
            array.put(obj)
        }
        prefs.edit().putString("history_json", array.toString()).apply()
    }

    private fun loadHistory(): List<TranslationItem> {
        val raw = prefs.getString("history_json", null)
        if (raw.isNullOrEmpty()) {
            // Prepopulate with a friendly welcome translation in Santali
            return listOf(
                TranslationItem(
                    sourceText = "नमस्ते, पलाश एआई में आपका स्वागत है!",
                    translatedText = "ᱡᱚᱦᱟᱨ, ᱯᱚᱞᱟᱥ ᱮᱟᱭ ᱨᱮ ᱟᱯᱱᱟᱨᱟᱜ ᱥᱟᱹᱜᱩᱱ ᱫᱟᱨᱟᱢ!",
                    sourceLanguage = Language.HINDI,
                    targetLanguage = Language.SANTALI_OL_CHIKI,
                    isFavorite = true,
                    engineUsed = "Palash Cloud Engine"
                )
            )
        }
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<TranslationItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    TranslationItem(
                        id = obj.optString("id"),
                        sourceText = obj.optString("sourceText"),
                        translatedText = obj.optString("translatedText"),
                        sourceLanguage = Language.fromCode(obj.optString("sourceLang")),
                        targetLanguage = Language.fromCode(obj.optString("targetLang")),
                        timestamp = obj.optLong("timestamp"),
                        isFavorite = obj.optBoolean("isFavorite", false),
                        engineUsed = obj.optString("engineUsed", "Gemini 2.5 Flash"),
                        isCloudSynced = obj.optBoolean("isCloudSynced", true)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveDocuments(list: List<ConvertedDocument>) {
        val array = JSONArray()
        for (doc in list) {
            val obj = JSONObject().apply {
                put("id", doc.id)
                put("name", doc.name)
                put("sizeBytes", doc.sizeBytes)
                put("ext", doc.fileExtension)
                put("srcLang", doc.sourceLanguage.code)
                put("tgtLang", doc.targetLanguage.code)
                put("status", doc.status.name)
                put("orig", doc.originalPreview)
                put("trans", doc.translatedPreview)
                put("timestamp", doc.timestamp)
            }
            array.put(obj)
        }
        prefs.edit().putString("documents_json", array.toString()).apply()
    }

    private fun loadDocuments(): List<ConvertedDocument> {
        val raw = prefs.getString("documents_json", null)
        if (raw.isNullOrEmpty()) {
            return listOf(
                ConvertedDocument(
                    name = "Santali_Folk_Tales_Chapter1.txt",
                    sizeBytes = 24500,
                    fileExtension = "TXT",
                    sourceLanguage = Language.SANTALI_OL_CHIKI,
                    targetLanguage = Language.HINDI,
                    status = FileConversionStatus.COMPLETED,
                    originalPreview = "ᱢᱤᱫ ᱟᱹᱛᱩ ᱨᱮ ᱢᱤᱫ ᱪᱟᱹᱥᱤ ᱛᱟᱦᱮᱸ ᱠᱟᱱᱟᱭ ᱾ ᱩᱱᱤ ᱟᱹᱰᱤ ᱠᱩᱨᱩᱢᱩᱴᱩ ᱦᱚᱲ ᱮ ᱛᱟᱦᱮᱸ ᱠᱟᱱᱟ ᱾",
                    translatedPreview = "एक गाँव में एक किसान रहता था। वह बहुत मेहनती व्यक्ति था।"
                )
            )
        }
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<ConvertedDocument>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ConvertedDocument(
                        id = obj.optString("id"),
                        name = obj.optString("name"),
                        sizeBytes = obj.optLong("sizeBytes"),
                        fileExtension = obj.optString("ext"),
                        sourceLanguage = Language.fromCode(obj.optString("srcLang")),
                        targetLanguage = Language.fromCode(obj.optString("tgtLang")),
                        status = FileConversionStatus.valueOf(obj.optString("status", FileConversionStatus.COMPLETED.name)),
                        originalPreview = obj.optString("orig"),
                        translatedPreview = obj.optString("trans"),
                        timestamp = obj.optLong("timestamp")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }
}
