package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Language
import com.example.data.TranslationDictionary
import com.example.data.TranslationItem
import com.example.service.PalashGeminiService
import com.example.service.TextToSpeechHelper
import com.example.ui.theme.PalashAccentLime
import com.example.ui.theme.PalashPrimary
import com.example.ui.theme.PalashPrimaryDark
import com.example.ui.theme.PalashSecondary
import kotlinx.coroutines.launch

@Composable
fun TextConverterScreen(
    ttsHelper: TextToSpeechHelper,
    onSaveTranslation: (String, String, Language, Language) -> TranslationItem,
    initialText: String = "",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var sourceLanguage by remember { mutableStateOf(Language.HINDI) }
    var targetLanguage by remember { mutableStateOf(Language.SANTALI_OL_CHIKI) }
    var inputText by remember { mutableStateOf(initialText) }
    var translatedText by remember {
        mutableStateOf(
            if (initialText.isNotBlank()) {
                TranslationDictionary.translate(initialText, sourceLanguage, targetLanguage)
            } else ""
        )
    }
    var statusMessage by remember { mutableStateOf("Ready to translate") }
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    var isBookmarked by remember { mutableStateOf(false) }
    var isTranslating by remember { mutableStateOf(false) }
    var activeEngine by remember { mutableStateOf("Palash Cloud AI Engine") }
    var translationLatencyMs by remember { mutableLongStateOf(0L) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Language Selector Bar with Swap
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Source Language
                LanguageDropdown(
                    selected = sourceLanguage,
                    onSelect = {
                        sourceLanguage = it
                        if (it == targetLanguage) {
                            targetLanguage = if (it == Language.HINDI) Language.SANTALI_OL_CHIKI else Language.HINDI
                        }
                    },
                    modifier = Modifier.weight(1f)
                )

                // Swap Button
                IconButton(
                    onClick = {
                        rotationAngle += 180f
                        val temp = sourceLanguage
                        sourceLanguage = targetLanguage
                        targetLanguage = temp
                        if (translatedText.isNotBlank()) {
                            val prevInput = inputText
                            inputText = translatedText
                            translatedText = prevInput
                        }
                    },
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(CircleShape)
                        .background(PalashPrimary.copy(alpha = 0.1f))
                        .testTag("swap_languages_button")
                ) {
                    val animatedRotation by animateFloatAsState(targetValue = rotationAngle, label = "swap_rotate")
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Swap Languages",
                        tint = PalashPrimary,
                        modifier = Modifier.rotate(animatedRotation)
                    )
                }

                // Target Language
                LanguageDropdown(
                    selected = targetLanguage,
                    onSelect = {
                        targetLanguage = it
                        if (it == sourceLanguage) {
                            sourceLanguage = if (it == Language.HINDI) Language.SANTALI_OL_CHIKI else Language.HINDI
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Preset Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val presets = listOf(
                "नमस्ते", "आप कैसे हैं?", "धन्यवाद", "पानी", "स्कूल", "पलाश का फूल", "मदद चाहिए"
            )
            presets.forEach { preset ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                        .clickable {
                            inputText = preset
                            sourceLanguage = Language.HINDI
                            targetLanguage = Language.SANTALI_OL_CHIKI
                            translatedText = TranslationDictionary.translate(preset, sourceLanguage, targetLanguage)
                            statusMessage = "Translation complete"
                            isBookmarked = false
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = preset,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Input Text Area
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${sourceLanguage.displayName} (${sourceLanguage.nativeName})",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = PalashPrimary
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${inputText.length} / 1000",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (inputText.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    inputText = ""
                                    translatedText = ""
                                    statusMessage = "Ready to translate"
                                    isBookmarked = false
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear input",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = inputText,
                    onValueChange = {
                        if (it.length <= 1000) {
                            inputText = it
                            statusMessage = "Ready to translate"
                        }
                    },
                    placeholder = {
                        Text("Type or paste text to translate...")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .testTag("text_source_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PalashPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Speak Input button
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                ttsHelper.speak(inputText, sourceLanguage)
                            } else {
                                Toast.makeText(context, "Enter text to listen", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Speak input text",
                            tint = PalashPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Sleek Pill Translate Button
                    Button(
                        onClick = {
                            if (inputText.isBlank()) {
                                Toast.makeText(context, "Please enter some text", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isTranslating = true
                            statusMessage = "Translating via Cloud AI API..."
                            coroutineScope.launch {
                                val result = PalashGeminiService.translateText(inputText, sourceLanguage, targetLanguage)
                                translatedText = result.translatedText
                                activeEngine = result.engineName
                                translationLatencyMs = result.latencyMs
                                statusMessage = if (result.isOnlineAi) {
                                    "Synced with Gemini API (${result.latencyMs}ms)"
                                } else {
                                    "Translated via Regional Engine"
                                }
                                isTranslating = false
                                isBookmarked = false
                                onSaveTranslation(inputText, result.translatedText, sourceLanguage, targetLanguage)
                            }
                        },
                        enabled = !isTranslating,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("convert_text_button"),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PalashPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        if (isTranslating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Translating...",
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "Translate",
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Status Indicator with Engine Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (translatedText.isNotEmpty()) PalashAccentLime else PalashPrimary)
                )
                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(PalashPrimary.copy(alpha = 0.08f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (activeEngine.contains("Gemini")) "AI Cloud API" else "Regional Engine",
                    color = PalashPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Output Result Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                if (translatedText.isNotEmpty()) PalashPrimary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${targetLanguage.displayName} (${targetLanguage.nativeName})",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = PalashPrimary
                    )

                    Text(
                        text = targetLanguage.scriptName,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    if (translatedText.isNotEmpty()) {
                        Text(
                            text = translatedText,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = if (targetLanguage == Language.SANTALI_OL_CHIKI) 20.sp else 16.sp,
                                lineHeight = 26.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("translated_text_result")
                        )
                    } else {
                        Text(
                            text = "Translated text will appear here...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action icons: Speak, Copy, Favorite, Share
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Speak Output
                    IconButton(
                        onClick = {
                            if (translatedText.isNotBlank()) {
                                ttsHelper.speak(translatedText, targetLanguage)
                            } else {
                                Toast.makeText(context, "No translation to speak", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Speak translation",
                            tint = PalashPrimary
                        )
                    }

                    // Copy translation
                    IconButton(
                        onClick = {
                            if (translatedText.isNotBlank()) {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Palash AI Translation", translatedText)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Translation copied to clipboard", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Convert text first", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.size(38.dp).testTag("copy_translation_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Translation",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Bookmark
                    IconButton(
                        onClick = {
                            if (translatedText.isNotBlank()) {
                                isBookmarked = !isBookmarked
                                onSaveTranslation(inputText, translatedText, sourceLanguage, targetLanguage)
                                Toast.makeText(
                                    context,
                                    if (isBookmarked) "Saved to favorites" else "Removed from favorites",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Save to favorites",
                            tint = if (isBookmarked) PalashPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageDropdown(
    selected: Language,
    onSelect: (Language) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { expanded = true }
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = selected.flagOrIcon, fontSize = 16.sp)
            Column {
                Text(
                    text = selected.displayName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = selected.nativeName,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Language.entries.forEach { lang ->
                DropdownMenuItem(
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = lang.flagOrIcon, fontSize = 18.sp)
                            Column {
                                Text(
                                    text = lang.displayName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${lang.nativeName} • ${lang.scriptName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    onClick = {
                        onSelect(lang)
                        expanded = false
                    }
                )
            }
        }
    }
}
