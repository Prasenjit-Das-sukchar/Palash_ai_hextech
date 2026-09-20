package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.PalashRepository
import com.example.service.TextToSpeechHelper
import com.example.ui.components.AuthDialog
import com.example.ui.components.PalashDrawerContent
import com.example.ui.components.PalashScreen
import com.example.ui.components.PalashTopBar
import com.example.ui.components.RobotNodeHub
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.AiLensScreen
import com.example.ui.screens.DatabaseManagementScreen
import com.example.ui.screens.FileConverterScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.SpeechConverterScreen
import com.example.ui.screens.TextConverterScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

  private lateinit var repository: PalashRepository
  private lateinit var ttsHelper: TextToSpeechHelper

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    repository = PalashRepository(applicationContext)
    ttsHelper = TextToSpeechHelper(applicationContext)

    setContent {
      MyApplicationTheme {
        PalashApp(
          repository = repository,
          ttsHelper = ttsHelper
        )
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    ttsHelper.shutdown()
  }
}

@Composable
fun PalashApp(
  repository: PalashRepository,
  ttsHelper: TextToSpeechHelper
) {
  val currentUser by repository.currentUser.collectAsState()
  val history by repository.history.collectAsState()
  val documents by repository.documents.collectAsState()
  val realtimeSyncLog by repository.realtimeSyncLog.collectAsState()

  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()

  var currentScreen by remember { mutableStateOf(PalashScreen.HUB) }
  var showAuthDialog by remember { mutableStateOf(false) }
  var textConverterInitialText by remember { mutableStateOf("") }

  // Handle system back button
  BackHandler(enabled = drawerState.isOpen || currentScreen != PalashScreen.HUB) {
    if (drawerState.isOpen) {
      scope.launch { drawerState.close() }
    } else if (currentScreen != PalashScreen.HUB) {
      currentScreen = PalashScreen.HUB
    }
  }

  ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
      PalashDrawerContent(
        currentScreen = currentScreen,
        user = currentUser,
        onSelectScreen = { screen ->
          currentScreen = screen
          scope.launch { drawerState.close() }
        },
        onOpenAuth = {
          scope.launch { drawerState.close() }
          showAuthDialog = true
        }
      )
    }
  ) {
    Scaffold(
      modifier = Modifier
        .fillMaxSize()
        .testTag("main_scaffold"),
      contentWindowInsets = WindowInsets.safeDrawing,
      topBar = {
        val screenTitle = when (currentScreen) {
          PalashScreen.HUB -> null
          PalashScreen.TEXT_CONVERTER -> "Text Converter"
          PalashScreen.SPEECH_CONVERTER -> "Speech & Voice"
          PalashScreen.AI_LENS -> "AI Lens OCR"
          PalashScreen.FILE_CONVERTER -> "Document Converter"
          PalashScreen.DATABASE_MANAGEMENT -> "Database & Backend"
          PalashScreen.HISTORY -> "History & Cloud"
          PalashScreen.ABOUT -> "About Palash AI"
        }
        PalashTopBar(
          user = currentUser,
          onMenuClick = {
            scope.launch { drawerState.open() }
          },
          onLoginClick = {
            showAuthDialog = true
          },
          canNavigateBack = currentScreen != PalashScreen.HUB,
          onBackClick = {
            currentScreen = PalashScreen.HUB
          },
          screenTitle = screenTitle
        )
      },
      containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
          .imePadding()
          .background(
            Brush.verticalGradient(
              colors = listOf(
                MaterialTheme.colorScheme.background,
                MaterialTheme.colorScheme.surface
              )
            )
          )
      ) {
        when (currentScreen) {
          PalashScreen.HUB -> {
            Column(
              modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
            ) {
              RobotNodeHub(
                onOpenTextConverter = {
                  textConverterInitialText = ""
                  currentScreen = PalashScreen.TEXT_CONVERTER
                },
                onOpenSpeechConverter = {
                  currentScreen = PalashScreen.SPEECH_CONVERTER
                },
                onOpenAiLens = {
                  currentScreen = PalashScreen.AI_LENS
                },
                onOpenFileConverter = {
                  currentScreen = PalashScreen.FILE_CONVERTER
                },
                onWordOfDayClick = { word ->
                  textConverterInitialText = word
                  currentScreen = PalashScreen.TEXT_CONVERTER
                },
                onOpenDatabase = {
                  currentScreen = PalashScreen.DATABASE_MANAGEMENT
                }
              )
              Spacer(modifier = Modifier.height(24.dp))
            }
          }

          PalashScreen.DATABASE_MANAGEMENT -> {
            DatabaseManagementScreen(
              repository = repository,
              onOpenAuth = { showAuthDialog = true }
            )
          }

          PalashScreen.TEXT_CONVERTER -> {
            TextConverterScreen(
              ttsHelper = ttsHelper,
              initialText = textConverterInitialText,
              onSaveTranslation = { src, tgt, from, to ->
                repository.addTranslation(src, tgt, from, to)
              }
            )
          }

          PalashScreen.SPEECH_CONVERTER -> {
            SpeechConverterScreen(
              ttsHelper = ttsHelper,
              onSaveTranslation = { src, tgt, from, to ->
                repository.addTranslation(src, tgt, from, to)
              }
            )
          }

          PalashScreen.AI_LENS -> {
            AiLensScreen(
              ttsHelper = ttsHelper,
              onSaveTranslation = { src, tgt, from, to ->
                repository.addTranslation(src, tgt, from, to)
              },
              onOpenInTextConverter = { text ->
                textConverterInitialText = text
                currentScreen = PalashScreen.TEXT_CONVERTER
              }
            )
          }

          PalashScreen.FILE_CONVERTER -> {
            FileConverterScreen(
              documents = documents,
              onAddDocument = { repository.addDocument(it) },
              onUpdateDocument = { repository.updateDocument(it) },
              ttsHelper = ttsHelper
            )
          }

          PalashScreen.HISTORY -> {
            HistoryScreen(
              history = history,
              ttsHelper = ttsHelper,
              onToggleFavorite = { repository.toggleFavorite(it) },
              onDeleteItem = { repository.deleteHistoryItem(it) },
              onClearHistory = { repository.clearHistory() }
            )
          }

          PalashScreen.ABOUT -> {
            AboutScreen()
          }
        }
      }
    }

    if (showAuthDialog) {
      AuthDialog(
        currentUser = currentUser,
        onDismiss = { showAuthDialog = false },
        onLogin = { email, name, isGuest ->
          repository.login(email, name, isGuest)
        },
        onLogout = {
          repository.logout()
        }
      )
    }
  }
}

