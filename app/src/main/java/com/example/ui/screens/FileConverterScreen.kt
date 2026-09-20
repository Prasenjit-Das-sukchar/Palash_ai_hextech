package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.ConvertedDocument
import com.example.data.FileConversionStatus
import com.example.data.Language
import com.example.service.PalashGeminiService
import com.example.service.TextToSpeechHelper
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.PalashAccentLime
import com.example.ui.theme.PalashPrimary
import com.example.ui.theme.PalashSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FileConverterScreen(
    documents: List<ConvertedDocument>,
    onAddDocument: (ConvertedDocument) -> Unit,
    onUpdateDocument: (ConvertedDocument) -> Unit,
    ttsHelper: TextToSpeechHelper,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var activeSourceLanguage by remember { mutableStateOf(Language.SANTALI_OL_CHIKI) }
    var activeTargetLanguage by remember { mutableStateOf(Language.HINDI) }

    var selectedDocumentId by remember { mutableStateOf<String?>(documents.firstOrNull()?.id) }
    var isConverting by remember { mutableStateOf(false) }
    var conversionProgress by remember { mutableStateOf(0f) }
    var viewerDocument by remember { mutableStateOf<ConvertedDocument?>(null) }
    var showAddFileDialog by remember { mutableStateOf(false) }

    // Vice-Versa conversion function for any document
    fun convertViceVersa(doc: ConvertedDocument) {
        val reversedSource = doc.targetLanguage
        val reversedTarget = doc.sourceLanguage
        val inputToReverse = if (doc.translatedPreview.isNotBlank()) doc.translatedPreview else doc.originalPreview

        val reversedDoc = ConvertedDocument(
            name = "ViceVersa_${doc.name}",
            sizeBytes = doc.sizeBytes,
            fileExtension = doc.fileExtension,
            sourceLanguage = reversedSource,
            targetLanguage = reversedTarget,
            status = FileConversionStatus.PROCESSING,
            originalPreview = inputToReverse,
            translatedPreview = ""
        )

        onAddDocument(reversedDoc)
        selectedDocumentId = reversedDoc.id
        Toast.makeText(context, "Converting vice-versa (${reversedSource.displayName} ➔ ${reversedTarget.displayName})...", Toast.LENGTH_SHORT).show()

        coroutineScope.launch {
            val result = PalashGeminiService.translateText(inputToReverse, reversedSource, reversedTarget)
            val completedDoc = reversedDoc.copy(
                status = FileConversionStatus.COMPLETED,
                translatedPreview = result.translatedText
            )
            onUpdateDocument(completedDoc)
            Toast.makeText(context, "Vice-versa conversion completed!", Toast.LENGTH_SHORT).show()
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "document_${System.currentTimeMillis()}.txt"
            val sampleText = if (activeSourceLanguage == Language.SANTALI_OL_CHIKI) {
                "ᱱᱚᱣᱟ ᱫᱚ ᱢᱤᱫ ᱥᱟᱱᱛᱟᱲᱤ ᱚᱞ ᱠᱟᱱᱟ ᱾ ᱯᱚᱞᱟᱥ ᱮᱟᱭ ᱱᱚᱣᱟ ᱫᱚ ᱦᱤᱱᱫᱤ ᱛᱮ ᱛᱚᱨᱡᱚᱢᱟᱭᱟ ᱾"
            } else if (activeSourceLanguage == Language.HINDI) {
                "यह एक सरकारी दस्तावेज़ है। पलाश एआई द्वारा इसे संताली ओल चिकी में रूपांतरित किया जा रहा है।"
            } else {
                "This is an official document converted into regional languages by Palash AI."
            }

            val newDoc = ConvertedDocument(
                name = fileName,
                sizeBytes = 24500,
                fileExtension = fileName.substringAfterLast('.', "TXT").uppercase(),
                sourceLanguage = activeSourceLanguage,
                targetLanguage = activeTargetLanguage,
                status = FileConversionStatus.QUEUED,
                originalPreview = sampleText,
                translatedPreview = ""
            )
            onAddDocument(newDoc)
            selectedDocumentId = newDoc.id
            Toast.makeText(context, "Added $fileName (${activeSourceLanguage.displayName} ➔ ${activeTargetLanguage.displayName})", Toast.LENGTH_SHORT).show()
        }
    }

    val selectedDoc = documents.firstOrNull { it.id == selectedDocumentId } ?: documents.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Document Converter",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Bidirectional file & text translation",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // "Add File" button with dialog popup
            Button(
                onClick = { showAddFileDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PalashPrimary),
                modifier = Modifier.testTag("add_file_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add File", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Language Direction Box with Vice-Versa Swap Button
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Source File Language",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = activeSourceLanguage.displayName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = PalashPrimary
                    )
                }

                // Vice-Versa Swap Button
                IconButton(
                    onClick = {
                        val temp = activeSourceLanguage
                        activeSourceLanguage = activeTargetLanguage
                        activeTargetLanguage = temp
                        Toast.makeText(context, "Direction swapped: ${activeSourceLanguage.displayName} ➔ ${activeTargetLanguage.displayName}", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PalashPrimary.copy(alpha = 0.1f))
                        .testTag("swap_file_direction_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Swap Direction (Vice-Versa)",
                        tint = PalashPrimary
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Target Converted Language",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = activeTargetLanguage.displayName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = PalashPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Conversion progress if active
        AnimatedVisibility(visible = isConverting) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Converting document via AI Translation Engine...", style = MaterialTheme.typography.labelSmall)
                    Text("${(conversionProgress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { conversionProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = PalashAccentLime,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        // File List Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Converted Documents (${documents.size})",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (selectedDoc != null) {
                Text(
                    text = "Selected: ${selectedDoc.name.take(16)}...",
                    style = MaterialTheme.typography.labelSmall,
                    color = PalashPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // List of files
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (documents.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No files added yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Tap 'Add File' above to upload or test files", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            } else {
                items(documents) { doc ->
                    val isSelected = doc.id == selectedDocumentId
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { selectedDocumentId = doc.id }
                            .testTag("file_item_${doc.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) PalashPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (isSelected) PalashPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) PalashPrimary else MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = doc.fileExtension,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = doc.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${doc.formattedSize} • ${doc.sourceLanguage.displayName} ➔ ${doc.targetLanguage.displayName}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            when (doc.status) {
                                                FileConversionStatus.COMPLETED -> AccentGreen.copy(alpha = 0.18f)
                                                FileConversionStatus.PROCESSING -> PalashSecondary.copy(alpha = 0.18f)
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = doc.status.name,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (doc.status) {
                                                FileConversionStatus.COMPLETED -> AccentGreen
                                                FileConversionStatus.PROCESSING -> PalashSecondary
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Vice-Versa Action Bar on Card
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { convertViceVersa(doc) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp), tint = PalashPrimary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Convert Vice-Versa (${doc.targetLanguage.displayName.substringBefore(" ")} ➔ ${doc.sourceLanguage.displayName.substringBefore(" ")})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PalashPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action Buttons: "Convert Selected File" and "View Document"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Convert Button
            Button(
                onClick = {
                    if (selectedDoc != null) {
                        isConverting = true
                        conversionProgress = 0f
                        coroutineScope.launch {
                            for (i in 1..8) {
                                delay(120)
                                conversionProgress = i / 10f
                            }
                            // Call Gemini API translation
                            val result = PalashGeminiService.translateText(
                                selectedDoc.originalPreview,
                                selectedDoc.sourceLanguage,
                                selectedDoc.targetLanguage
                            )
                            conversionProgress = 1.0f
                            isConverting = false
                            val updated = selectedDoc.copy(
                                status = FileConversionStatus.COMPLETED,
                                translatedPreview = result.translatedText
                            )
                            onUpdateDocument(updated)
                            Toast.makeText(context, "Conversion finished for ${selectedDoc.name}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Select or add a file first", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("convert_files_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PalashPrimary),
                enabled = !isConverting && selectedDoc != null
            ) {
                if (isConverting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Convert File", fontWeight = FontWeight.Bold)
                }
            }

            // View Selected File Button
            OutlinedButton(
                onClick = {
                    if (selectedDoc != null) {
                        viewerDocument = selectedDoc
                    } else {
                        Toast.makeText(context, "No file selected", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("view_selected_file_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PalashPrimary)
            ) {
                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("View File", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Add File Dialog with Vice-Versa Selection
    if (showAddFileDialog) {
        AlertDialog(
            onDismissRequest = { showAddFileDialog = false },
            title = {
                Text(
                    text = "Add File for Conversion",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Choose a conversion direction (Vice-Versa enabled):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Direction 1: Santali -> Hindi
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                activeSourceLanguage = Language.SANTALI_OL_CHIKI
                                activeTargetLanguage = Language.HINDI
                                val doc = ConvertedDocument(
                                    name = "Santali_Official_Notice.txt",
                                    sizeBytes = 19200,
                                    fileExtension = "TXT",
                                    sourceLanguage = Language.SANTALI_OL_CHIKI,
                                    targetLanguage = Language.HINDI,
                                    status = FileConversionStatus.QUEUED,
                                    originalPreview = "ᱯᱚᱞᱟᱥ ᱮᱟᱭ ᱛᱮ ᱥᱟᱱᱛᱟᱲᱤ ᱥᱟᱶᱛᱟ ᱨᱮᱱᱟᱜ ᱵᱷᱟᱹᱞᱟᱹᱭ ᱦᱩᱭᱩᱜ ᱠᱟᱱᱟ ᱾ ᱥᱟᱱᱟᱢ ᱦᱚᱲ ᱥᱮᱪᱮᱫᱚᱜ ᱢᱟ ᱾",
                                    translatedPreview = "पलाश एआई से संताली समाज का कल्याण हो रहा है। सभी लोग शिक्षित हों।"
                                )
                                onAddDocument(doc)
                                selectedDocumentId = doc.id
                                showAddFileDialog = false
                                Toast.makeText(context, "Added Santali ➔ Hindi Document", Toast.LENGTH_SHORT).show()
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Santali (Ol Chiki) ➔ Hindi", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Sample Notice in Ol Chiki Script", style = MaterialTheme.typography.bodySmall)
                            }
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = PalashPrimary)
                        }
                    }

                    // Direction 2: Hindi -> Santali (Vice Versa!)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                activeSourceLanguage = Language.HINDI
                                activeTargetLanguage = Language.SANTALI_OL_CHIKI
                                val doc = ConvertedDocument(
                                    name = "Panchayat_Hindi_Notice.txt",
                                    sizeBytes = 22400,
                                    fileExtension = "TXT",
                                    sourceLanguage = Language.HINDI,
                                    targetLanguage = Language.SANTALI_OL_CHIKI,
                                    status = FileConversionStatus.QUEUED,
                                    originalPreview = "ग्राम पंचायत की बैठक कल प्रातः १० बजे पंचायत भवन में होगी। सभी सदस्यों की उपस्थिति अनिवार्य है।",
                                    translatedPreview = "ᱟᱹᱛᱩ ᱯᱟᱧᱪᱟᱭᱮᱛ ᱨᱮᱱᱟᱜ ᱢᱤᱫᱩᱱ ᱜᱟᱯᱟ ᱥᱮᱛᱟᱜ ᱑᱐ ᱵᱟᱡᱟ ᱨᱮ ᱦᱩᱭᱩᱜᱼᱟ ᱾ ᱥᱟᱱᱟᱢ ᱦᱚᱲ ᱥᱮᱴᱮᱨᱚᱜ ᱞᱟᱹᱠᱛᱤᱭᱟᱱ ᱜᱮᱭᱟ ᱾"
                                )
                                onAddDocument(doc)
                                selectedDocumentId = doc.id
                                showAddFileDialog = false
                                Toast.makeText(context, "Added Hindi ➔ Santali (Vice-Versa)", Toast.LENGTH_SHORT).show()
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Hindi ➔ Santali (Ol Chiki) [Vice-Versa]", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PalashPrimary)
                                Text("Convert Hindi Document to Ol Chiki", style = MaterialTheme.typography.bodySmall)
                            }
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = PalashPrimary)
                        }
                    }

                    // Upload from Device
                    Button(
                        onClick = {
                            showAddFileDialog = false
                            filePickerLauncher.launch("*/*")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PalashPrimary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Browse Device Storage (*.txt, *.doc, *.pdf)")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                OutlinedButton(onClick = { showAddFileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal Viewer Dialog for Document with Vice-Versa and Audio
    if (viewerDocument != null) {
        val doc = viewerDocument!!
        var viewTab by remember { mutableIntStateOf(1) } // 0 = Original, 1 = Translated

        Dialog(onDismissRequest = { viewerDocument = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(490.dp)
                    .padding(8.dp)
                    .testTag("file_viewer_modal"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = doc.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = PalashPrimary,
                                maxLines = 1
                            )
                            Text(
                                text = "${doc.sourceLanguage.displayName} ➔ ${doc.targetLanguage.displayName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = { viewerDocument = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TabRow(selectedTabIndex = viewTab) {
                        Tab(
                            selected = viewTab == 0,
                            onClick = { viewTab = 0 },
                            text = { Text("Original (${doc.sourceLanguage.displayName.substringBefore(" ")})") }
                        )
                        Tab(
                            selected = viewTab == 1,
                            onClick = { viewTab = 1 },
                            text = { Text("Translated (${doc.targetLanguage.displayName.substringBefore(" ")})") }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val activeText = if (viewTab == 0) doc.originalPreview else {
                        if (doc.translatedPreview.isNotBlank()) doc.translatedPreview else "Document is not yet converted. Tap 'Convert File' below."
                    }
                    val activeLang = if (viewTab == 0) doc.sourceLanguage else doc.targetLanguage

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = activeText,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = if (activeLang == Language.SANTALI_OL_CHIKI) 19.sp else 15.sp,
                                lineHeight = 24.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Speak Aloud
                        IconButton(
                            onClick = {
                                if (activeText.isNotBlank()) {
                                    ttsHelper.speak(activeText, activeLang)
                                }
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "Listen", tint = PalashPrimary)
                        }

                        // Copy
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("doc_text", activeText))
                                Toast.makeText(context, "Copied text to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = PalashPrimary)
                        }

                        // Convert Vice-Versa button inside modal
                        Button(
                            onClick = {
                                viewerDocument = null
                                convertViceVersa(doc)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PalashSecondary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Convert Vice-Versa", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
