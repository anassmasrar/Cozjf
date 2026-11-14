package com.example.ui.modals

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.IndustryFormat
import com.example.model.TransferDirection
import com.example.ui.theme.*

@Composable
fun ImportExportModal(
    formats: List<IndustryFormat>,
    direction: TransferDirection,
    selectedFormatExt: String,
    droppedFileName: String?,
    normalizeYUp: Boolean,
    applyTransforms: Boolean,
    lastTransferLog: String?,
    onSetDirection: (TransferDirection) -> Unit,
    onSelectFormat: (String) -> Unit,
    onSimulateDropFile: (String) -> Unit,
    onToggleOptions: (Boolean, Boolean) -> Unit,
    onExecuteTransfer: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val resolvedName = runCatching {
                context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (idx >= 0) cursor.getString(idx) else null
                    } else null
                }
            }.getOrNull() ?: uri.lastPathSegment?.substringAfterLast('/') ?: "Import_3D$selectedFormatExt"

            onSetDirection(TransferDirection.IMPORT)
            onSimulateDropFile(resolvedName)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 680.dp)
                .heightIn(max = 680.dp),
            shape = RoundedCornerShape(8.dp),
            color = PanelSurface,
            border = BorderStroke(1.dp, BorderSubtle),
            tonalElevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.ImportExport, contentDescription = null, tint = PrecisionAmber)
                        Text(
                            text = "IMPORT & EXPORT 3D UNIVERSEL",
                            style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Fermer", tint = TextSecondary)
                    }
                }

                // Direction Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(VoidBlack, RoundedCornerShape(4.dp))
                        .border(1.dp, BorderHairline, RoundedCornerShape(4.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TransferDirection.entries.forEach { dir ->
                        val active = dir == direction
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(3.dp))
                                .clickable { onSetDirection(dir) },
                            color = if (active) SteelBlueDim else VoidBlack,
                            border = BorderStroke(1.dp, if (active) SteelBlue else VoidBlack),
                            shape = RoundedCornerShape(3.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dir.label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = if (active) TextPrimary else TextSecondary
                                    )
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Sélecteur réel de fichier sur le téléphone
                    OutlinedButton(
                        onClick = { documentPickerLauncher.launch(arrayOf("*/*")) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 44.dp)
                            .testTag("pick_device_file_btn"),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, SteelBlue)
                    ) {
                        Icon(Icons.Filled.FolderOpen, contentDescription = null, tint = SteelBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Parcourir les fichiers du téléphone (.glb, .obj, .fbx...)", color = SteelBlue)
                    }

                    // Zone Glisser-Déposer ou sélection rapide
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(VoidBlack)
                            .drawBehind {
                                drawRoundRect(
                                    color = SteelBlue.copy(alpha = 0.7f),
                                    style = Stroke(
                                        width = 2.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                                    ),
                                    cornerRadius = CornerRadius(6.dp.toPx())
                                )
                            }
                            .clickable {
                                onSimulateDropFile("Modele_Studio$selectedFormatExt")
                            }
                            .testTag("drag_drop_zone"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FileUpload,
                                contentDescription = "Zone Glisser-Déposer",
                                tint = SteelBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = droppedFileName ?: "Toucher ici pour charger un modèle exemple ($selectedFormatExt)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (droppedFileName != null) TerminalEmerald else TextPrimary
                                )
                            )
                            Text(
                                text = "L'objet importé s'ajoute directement dans votre scène 3D",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp)
                            )
                        }
                    }

                    Text(
                        text = "FORMATS SUPPORTÉS (${formats.size})",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )

                    formats.chunked(3).forEach { rowFormats ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowFormats.forEach { fmt ->
                                val selected = fmt.extension == selectedFormatExt
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("format_${fmt.extension}")
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable { onSelectFormat(fmt.extension) },
                                    color = if (selected) PrecisionAmberDim else ElevatedSurface,
                                    border = BorderStroke(1.dp, if (selected) PrecisionAmber else BorderHairline),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp)) {
                                        Text(
                                            text = fmt.extension.uppercase(),
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = if (selected) PrecisionAmber else TextPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Text(
                                            text = fmt.label,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = TextSecondary,
                                                fontSize = 9.sp
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                            repeat(3 - rowFormats.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ElevatedSurface, RoundedCornerShape(4.dp))
                            .border(1.dp, BorderHairline, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = normalizeYUp,
                                onCheckedChange = { onToggleOptions(it, applyTransforms) },
                                colors = CheckboxDefaults.colors(checkedColor = SteelBlue)
                            )
                            Text("Axe Y-Up", style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = applyTransforms,
                                onCheckedChange = { onToggleOptions(normalizeYUp, it) },
                                colors = CheckboxDefaults.colors(checkedColor = SteelBlue)
                            )
                            Text("Appliquer Transforms", style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary))
                        }
                    }

                    if (lastTransferLog != null) {
                        Text(
                            text = lastTransferLog,
                            style = MaterialTheme.typography.labelSmall.copy(color = TerminalEmerald)
                        )
                    }
                }

                Button(
                    onClick = onExecuteTransfer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("execute_transfer_btn"),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrecisionAmber,
                        contentColor = VoidBlack
                    )
                ) {
                    Text(
                        text = if (direction == TransferDirection.IMPORT) {
                            "Importer ($selectedFormatExt) dans la Scène"
                        } else {
                            "Exporter la Scène en $selectedFormatExt"
                        },
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
