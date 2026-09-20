package com.example.service

import android.content.Context
import android.util.Log
import com.example.data.ConvertedDocument
import com.example.data.FileConversionStatus
import com.example.data.Language
import com.example.data.RealtimeSyncEvent
import com.example.data.TranslationItem
import com.example.data.UserProfile
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object FirebaseBackendManager {
    private const val TAG = "FirebaseBackendManager"
    const val FIREBASE_PROJECT_ID = "prolog-2020"
    const val FIREBASE_CONSOLE_URL = "https://console.firebase.google.com/u/0/project/prolog-2020/overview"

    private var auth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _syncStatus = MutableStateFlow("Initializing Firebase backend...")
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    private val _firebaseUser = MutableStateFlow<FirebaseUser?>(null)
    val firebaseUser: StateFlow<FirebaseUser?> = _firebaseUser.asStateFlow()

    private val _cloudTranslationCount = MutableStateFlow(0)
    val cloudTranslationCount: StateFlow<Int> = _cloudTranslationCount.asStateFlow()

    private val _cloudDocumentCount = MutableStateFlow(0)
    val cloudDocumentCount: StateFlow<Int> = _cloudDocumentCount.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(System.currentTimeMillis())
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()

    private var translationsListener: ListenerRegistration? = null
    private var documentsListener: ListenerRegistration? = null

    fun initialize(context: Context) {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setProjectId(FIREBASE_PROJECT_ID)
                    .setApplicationId("1:781557417239:android:palashprolog2020")
                    .setStorageBucket("prolog-2020.appspot.com")
                    .setApiKey("AIzaSyAjmA0dEZ6DgqYAVVbTYSOahbqQwyVGnIo")
                    .build()
                FirebaseApp.initializeApp(context, options)
                Log.d(TAG, "FirebaseApp manually initialized with project $FIREBASE_PROJECT_ID")
            } else {
                Log.d(TAG, "FirebaseApp initialized from google-services.json")
            }

            // Setup Auth
            try {
                auth = FirebaseAuth.getInstance()
                _firebaseUser.value = auth?.currentUser
                auth?.addAuthStateListener { fbAuth ->
                    _firebaseUser.value = fbAuth.currentUser
                    if (fbAuth.currentUser != null) {
                        _syncStatus.value = "Authenticated: ${fbAuth.currentUser?.email ?: fbAuth.currentUser?.uid}"
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "FirebaseAuth init error: ${e.message}")
            }

            // Setup Firestore
            try {
                firestore = FirebaseFirestore.getInstance()
                _isConnected.value = true
                _syncStatus.value = "Connected to Firestore ($FIREBASE_PROJECT_ID)"
            } catch (e: Exception) {
                Log.e(TAG, "FirebaseFirestore init error: ${e.message}")
                _syncStatus.value = "Firestore offline fallback ready"
            }

        } catch (e: Exception) {
            Log.e(TAG, "Overall Firebase init failed: ${e.message}")
            _syncStatus.value = "Offline mode (Local persistence active)"
        }
    }

    // AUTHENTICATION MANAGEMENT
    fun signInWithEmail(
        email: String,
        pass: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val currentAuth = auth
        if (currentAuth == null) {
            onResult(false, "Firebase Auth not available in environment")
            return
        }
        currentAuth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                _firebaseUser.value = it.user
                _syncStatus.value = "Signed in as ${it.user?.email}"
                onResult(true, "Successfully signed in with Firebase")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Sign in failed: ${e.message}")
                onResult(false, e.localizedMessage ?: "Authentication failed")
            }
    }

    fun registerWithEmail(
        email: String,
        pass: String,
        displayName: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val currentAuth = auth
        if (currentAuth == null) {
            onResult(false, "Firebase Auth not available in environment")
            return
        }
        currentAuth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null && displayName.isNotBlank()) {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName)
                        .build()
                    user.updateProfile(profileUpdates)
                }
                _firebaseUser.value = user
                _syncStatus.value = "Registered & Connected: $email"
                onResult(true, "Firebase account created successfully")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Registration failed: ${e.message}")
                onResult(false, e.localizedMessage ?: "Registration failed")
            }
    }

    fun signInAnonymously(onResult: (Boolean, String) -> Unit) {
        val currentAuth = auth
        if (currentAuth == null) {
            onResult(false, "Firebase Auth not available")
            return
        }
        currentAuth.signInAnonymously()
            .addOnSuccessListener {
                _firebaseUser.value = it.user
                _syncStatus.value = "Guest session authenticated via Firebase"
                onResult(true, "Guest sign-in successful")
            }
            .addOnFailureListener { e ->
                onResult(false, e.localizedMessage ?: "Guest login failed")
            }
    }

    fun signOut() {
        auth?.signOut()
        _firebaseUser.value = null
        _syncStatus.value = "Signed out from Firebase backend"
    }

    // DATABASE MANAGEMENT (FIRESTORE)
    fun syncTranslationToCloud(
        item: TranslationItem,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        val fs = firestore ?: return
        val uid = auth?.currentUser?.uid ?: "guest_user"
        val email = auth?.currentUser?.email ?: "anonymous@prolog-2020.firebase"

        val data = hashMapOf(
            "id" to item.id,
            "sourceText" to item.sourceText,
            "translatedText" to item.translatedText,
            "sourceLanguageCode" to item.sourceLanguage.code,
            "targetLanguageCode" to item.targetLanguage.code,
            "sourceLanguage" to item.sourceLanguage.displayName,
            "targetLanguage" to item.targetLanguage.displayName,
            "timestamp" to item.timestamp,
            "engineUsed" to item.engineUsed,
            "isFavorite" to item.isFavorite,
            "userId" to uid,
            "userEmail" to email,
            "projectId" to FIREBASE_PROJECT_ID
        )

        fs.collection("translations")
            .document(item.id)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                _lastSyncTime.value = System.currentTimeMillis()
                _cloudTranslationCount.value += 1
                onComplete?.invoke(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Firestore write translation error: ${e.message}")
                onComplete?.invoke(false)
            }
    }

    fun syncDocumentToCloud(
        doc: ConvertedDocument,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        val fs = firestore ?: return
        val uid = auth?.currentUser?.uid ?: "guest_user"

        val data = hashMapOf(
            "id" to doc.id,
            "name" to doc.name,
            "sizeBytes" to doc.sizeBytes,
            "fileExtension" to doc.fileExtension,
            "originalPreview" to doc.originalPreview,
            "translatedPreview" to doc.translatedPreview,
            "sourceLanguageCode" to doc.sourceLanguage.code,
            "targetLanguageCode" to doc.targetLanguage.code,
            "status" to doc.status.name,
            "timestamp" to doc.timestamp,
            "userId" to uid,
            "projectId" to FIREBASE_PROJECT_ID
        )

        fs.collection("documents")
            .document(doc.id)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                _lastSyncTime.value = System.currentTimeMillis()
                _cloudDocumentCount.value += 1
                onComplete?.invoke(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Firestore write document error: ${e.message}")
                onComplete?.invoke(false)
            }
    }

    fun syncRealtimeEventToCloud(event: RealtimeSyncEvent) {
        val fs = firestore ?: return
        val uid = auth?.currentUser?.uid ?: "guest_user"

        val data = hashMapOf(
            "id" to event.id,
            "type" to event.type,
            "summary" to event.summary,
            "timestamp" to event.timestamp,
            "status" to event.status,
            "userId" to uid,
            "projectId" to FIREBASE_PROJECT_ID
        )

        fs.collection("sync_events")
            .document(event.id)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                _lastSyncTime.value = System.currentTimeMillis()
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "Firestore log event cached locally: ${e.message}")
            }
    }

    fun testDatabasePing(onResult: (Boolean, String) -> Unit) {
        val fs = firestore
        if (fs == null) {
            onResult(false, "Firestore instance not available")
            return
        }

        val testData = hashMapOf(
            "testTime" to System.currentTimeMillis(),
            "status" to "ONLINE",
            "project" to FIREBASE_PROJECT_ID,
            "client" to "PALASH AI Android Client",
            "activeUser" to (auth?.currentUser?.email ?: "guest")
        )

        fs.collection("system_health")
            .document("ping_verification")
            .set(testData, SetOptions.merge())
            .addOnSuccessListener {
                _isConnected.value = true
                _syncStatus.value = "Live Firestore connection verified ($FIREBASE_PROJECT_ID)"
                _lastSyncTime.value = System.currentTimeMillis()
                onResult(true, "Successfully communicated with Firebase project: $FIREBASE_PROJECT_ID")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Ping failed: ${e.message}")
                onResult(false, e.localizedMessage ?: "Connection ping failed")
            }
    }

    fun listenToCloudData(
        onTranslationsUpdated: (List<TranslationItem>) -> Unit,
        onDocumentsUpdated: (List<ConvertedDocument>) -> Unit
    ) {
        val fs = firestore ?: return

        translationsListener?.remove()
        documentsListener?.remove()

        try {
            translationsListener = fs.collection("translations")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.d(TAG, "Translations snapshot error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshots != null) {
                        val items = snapshots.documents.mapNotNull { doc ->
                            try {
                                val srcCode = doc.getString("sourceLanguageCode") ?: "hi"
                                val tgtCode = doc.getString("targetLanguageCode") ?: "sat-olck"
                                TranslationItem(
                                    id = doc.getString("id") ?: doc.id,
                                    sourceText = doc.getString("sourceText") ?: "",
                                    translatedText = doc.getString("translatedText") ?: "",
                                    sourceLanguage = Language.fromCode(srcCode),
                                    targetLanguage = Language.fromCode(tgtCode),
                                    timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                    isFavorite = doc.getBoolean("isFavorite") ?: false,
                                    engineUsed = doc.getString("engineUsed") ?: "Gemini 2.5 Flash",
                                    isCloudSynced = true
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        _cloudTranslationCount.value = items.size
                        if (items.isNotEmpty()) {
                            onTranslationsUpdated(items)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.d(TAG, "Listen translations setup failed: ${e.message}")
        }

        try {
            documentsListener = fs.collection("documents")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(30)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.d(TAG, "Documents snapshot error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshots != null) {
                        val docs = snapshots.documents.mapNotNull { doc ->
                            try {
                                val srcCode = doc.getString("sourceLanguageCode") ?: "en"
                                val tgtCode = doc.getString("targetLanguageCode") ?: "sat-olck"
                                ConvertedDocument(
                                    id = doc.getString("id") ?: doc.id,
                                    name = doc.getString("name") ?: "Document",
                                    sizeBytes = doc.getLong("sizeBytes") ?: 1024L,
                                    fileExtension = doc.getString("fileExtension") ?: "txt",
                                    sourceLanguage = Language.fromCode(srcCode),
                                    targetLanguage = Language.fromCode(tgtCode),
                                    status = try {
                                        FileConversionStatus.valueOf(doc.getString("status") ?: "COMPLETED")
                                    } catch (e: Exception) {
                                        FileConversionStatus.COMPLETED
                                    },
                                    originalPreview = doc.getString("originalPreview") ?: "",
                                    translatedPreview = doc.getString("translatedPreview") ?: "",
                                    timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        _cloudDocumentCount.value = docs.size
                        if (docs.isNotEmpty()) {
                            onDocumentsUpdated(docs)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.d(TAG, "Listen documents setup failed: ${e.message}")
        }
    }

    fun cleanup() {
        translationsListener?.remove()
        documentsListener?.remove()
    }
}
