package com.example.ui.panels

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.ModPackageExporter
import com.example.model.BlockbenchTargetType
import com.example.model.MinecraftLoader
import com.example.model.ModBuildState
import com.example.model.ModExportFormat
import com.example.model.ModelingAiEngine
import com.example.ui.theme.*

@Composable
fun ModAtelierPanel(
    modState: ModBuildState,
    onSelectFormat: (ModExportFormat) -> Unit,
    onSelectAssetEngine: (ModelingAiEngine) -> Unit,
    onSelectTargetType: (BlockbenchTargetType) -> Unit,
    onModAiPromptChange: (String) -> Unit,
    onGenerateModWithClaude: () -> Unit,
    onUpdateConfig: (modId: String, modName: String, version: String, loader: MinecraftLoader) -> Unit,
    onUpdateHorror: (
        aggression: Float,
        radius: Float,
        fogDensity: Float,
        jumpscareIntensity: Float,
        proximity: Boolean,
        los: Boolean,
        darkness: Boolean,
        spatialAudio: Boolean
    ) -> Unit,
    onCompileJar: () -> Unit,
    onProvideArchiveBytes: () -> ByteArray,
    onExportFeedback: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val saveArchiveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            val bytes = onProvideArchiveBytes()
            val res = ModPackageExporter.writeBytesToUri(context, uri, bytes)
            res.onSuccess { size ->
                onExportFeedback("Archive ${modState.exportFormat.ext} enregistrée ($size octets)")
            }.onFailure { err ->
                onExportFeedback("Erreur export : ${err.localizedMessage}")
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PanelSurface)
            .verticalScroll(rememberScrollState())
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "CLAUDE MODS ILLIMITÉS (.MCADDON / .JAR / .BBMODEL)",
            style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
        )

        // Claude + Blockbench / Meshy Generator Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElevatedSurface, RoundedCornerShape(6.dp))
                .border(1.dp, SteelBlue.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = SteelBlue, modifier = Modifier.size(16.dp))
                Text(
                    text = "GÉNÉRATEUR CLAUDE ILLIMITÉ + MODÈLES IA",
                    style = MaterialTheme.typography.labelSmall.copy(color = SteelBlue, fontWeight = FontWeight.Bold)
                )
            }

            // Format de sortie (.mcaddon / .jar / .bbmodel)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ModExportFormat.entries.forEach { fmt ->
                    val selected = fmt == modState.exportFormat
                    Surface(
                        onClick = { onSelectFormat(fmt) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("mod_fmt_${fmt.name}"),
                        color = if (selected) SteelBlueDim else VoidBlack,
                        border = BorderStroke(1.dp, if (selected) SteelBlue else BorderHairline),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = fmt.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (selected) TextPrimary else TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = fmt.edition,
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 8.sp)
                            )
                        }
                    }
                }
            }

            // Choix IA Modèle (Blockbench IA vs Meshy 3D) + Cible (Bloc vs Entité)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(ModelingAiEngine.BLOCKBENCH_IA, ModelingAiEngine.MESHY_3D).forEach { eng ->
                    val selected = eng == modState.assetEngine
                    Surface(
                        onClick = { onSelectAssetEngine(eng) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("mod_engine_${eng.name}"),
                        color = if (selected) TerminalEmeraldDim else VoidBlack,
                        border = BorderStroke(1.dp, if (selected) TerminalEmerald else BorderHairline),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = eng.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (selected) TerminalEmerald else TextSecondary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BlockbenchTargetType.entries.forEach { target ->
                    val selected = target == modState.targetType
                    Surface(
                        onClick = { onSelectTargetType(target) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("mod_target_${target.name}"),
                        color = if (selected) PrecisionAmberDim else VoidBlack,
                        border = BorderStroke(1.dp, if (selected) PrecisionAmber else BorderHairline),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = target.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (selected) PrecisionAmber else TextSecondary,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = modState.aiModPrompt,
                    onValueChange = onModAiPromptChange,
                    placeholder = {
                        Text("Décrivez votre mob, boss ou bloc à Claude...", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted))
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall.copy(color = TextPrimary),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("mod_claude_prompt_input")
                )
                Button(
                    onClick = onGenerateModWithClaude,
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("mod_claude_generate_btn"),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SteelBlue, contentColor = VoidBlack)
                ) {
                    Text("Générer", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Manifest Configuration
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElevatedSurface, RoundedCornerShape(6.dp))
                .border(1.dp, BorderHairline, RoundedCornerShape(6.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "CONFIGURATION DU PACK / MOD",
                style = MaterialTheme.typography.labelSmall.copy(color = SteelBlue)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IndustrialTextField(
                    value = modState.modId,
                    label = "Mod ID",
                    onValueChange = { onUpdateConfig(it, modState.modName, modState.version, modState.loader) },
                    modifier = Modifier.weight(1f)
                )
                IndustrialTextField(
                    value = modState.version,
                    label = "Version",
                    onValueChange = { onUpdateConfig(modState.modId, modState.modName, it, modState.loader) },
                    modifier = Modifier.weight(0.8f)
                )
            }

            IndustrialTextField(
                value = modState.modName,
                label = "Nom du Mod",
                onValueChange = { onUpdateConfig(modState.modId, it, modState.version, modState.loader) },
                modifier = Modifier.fillMaxWidth()
            )

            if (modState.exportFormat == ModExportFormat.JAR) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LOADER:",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                    MinecraftLoader.entries.forEach { loader ->
                        val selected = loader == modState.loader
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("loader_${loader.name}")
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    onUpdateConfig(modState.modId, modState.modName, modState.version, loader)
                                },
                            color = if (selected) PrecisionAmberDim else VoidBlack,
                            border = BorderStroke(1.dp, if (selected) PrecisionAmber else BorderHairline),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = loader.label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = if (selected) PrecisionAmber else TextSecondary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Sous-panneau Horreur & Comportements IA
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElevatedSurface, RoundedCornerShape(6.dp))
                .border(1.dp, CrimsonAlert.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.WarningAmber,
                    contentDescription = null,
                    tint = CrimsonAlert,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "COMPORTEMENTS IA & MODULE HORREUR",
                    style = MaterialTheme.typography.labelSmall.copy(color = CrimsonAlert)
                )
            }

            HorrorSliderRow(
                label = "Agressivité IA",
                valueText = "${(modState.aiAggression * 100).toInt()}%",
                value = modState.aiAggression,
                onValueChange = {
                    onUpdateHorror(
                        it,
                        modState.detectionRadius,
                        modState.volumetricFogDensity,
                        modState.jumpscareIntensity,
                        modState.triggerProximity,
                        modState.triggerLineOfSight,
                        modState.triggerDarkness,
                        modState.triggerSpatialAudio
                    )
                }
            )

            HorrorSliderRow(
                label = "Brouillard Volumétrique",
                valueText = "${(modState.volumetricFogDensity * 100).toInt()}%",
                value = modState.volumetricFogDensity,
                onValueChange = {
                    onUpdateHorror(
                        modState.aiAggression,
                        modState.detectionRadius,
                        it,
                        modState.jumpscareIntensity,
                        modState.triggerProximity,
                        modState.triggerLineOfSight,
                        modState.triggerDarkness,
                        modState.triggerSpatialAudio
                    )
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TriggerToggleChip(
                    label = "Proximité",
                    checked = modState.triggerProximity,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onUpdateHorror(
                            modState.aiAggression,
                            modState.detectionRadius,
                            modState.volumetricFogDensity,
                            modState.jumpscareIntensity,
                            !modState.triggerProximity,
                            modState.triggerLineOfSight,
                            modState.triggerDarkness,
                            modState.triggerSpatialAudio
                        )
                    }
                )
                TriggerToggleChip(
                    label = "Ligne de Vue",
                    checked = modState.triggerLineOfSight,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onUpdateHorror(
                            modState.aiAggression,
                            modState.detectionRadius,
                            modState.volumetricFogDensity,
                            modState.jumpscareIntensity,
                            modState.triggerProximity,
                            !modState.triggerLineOfSight,
                            modState.triggerDarkness,
                            modState.triggerSpatialAudio
                        )
                    }
                )
                TriggerToggleChip(
                    label = "Obscurité",
                    checked = modState.triggerDarkness,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onUpdateHorror(
                            modState.aiAggression,
                            modState.detectionRadius,
                            modState.volumetricFogDensity,
                            modState.jumpscareIntensity,
                            modState.triggerProximity,
                            modState.triggerLineOfSight,
                            !modState.triggerDarkness,
                            modState.triggerSpatialAudio
                        )
                    }
                )
            }
        }

        // Compilation + Liste fichiers générés + Sauvegarde ZIP (.mcaddon / .jar / .bbmodel)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(VoidBlack, RoundedCornerShape(6.dp))
                .border(1.dp, BorderHairline, RoundedCornerShape(6.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onCompileJar,
                enabled = !modState.isCompiling,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("compile_jar_btn"),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrecisionAmber,
                    contentColor = VoidBlack
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Build,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (modState.isCompiling) "Packaging ${modState.exportFormat.ext} en cours..." else "Compiler Archive ${modState.exportFormat.ext}",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            LinearProgressIndicator(
                progress = { modState.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (modState.progress >= 1f) TerminalEmerald else PrecisionAmber,
                trackColor = CardSurface
            )

            Text(
                text = modState.buildStage,
                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
            )

            modState.generatedPreviewFiles.forEach { filePath ->
                Text(
                    text = "• $filePath",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                )
            }

            if (modState.lastArtifact != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TerminalEmeraldDim, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = TerminalEmerald,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "ARCHIVE PRÊTE : ${modState.lastArtifact}",
                        style = MaterialTheme.typography.labelSmall.copy(color = TerminalEmerald)
                    )
                }
            }

            OutlinedButton(
                onClick = {
                    val fileName = "${modState.modId.ifBlank { "omnicore_mod" }}_${modState.version}${modState.exportFormat.ext}"
                    saveArchiveLauncher.launch(fileName)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 44.dp)
                    .testTag("export_mod_file_btn"),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, TerminalEmerald)
            ) {
                Icon(Icons.Filled.Download, contentDescription = null, tint = TerminalEmerald, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Enregistrer ${modState.exportFormat.ext} sur l'appareil", color = TerminalEmerald)
            }
        }
    }
}

@Composable
private fun IndustrialTextField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        singleLine = true,
        textStyle = MaterialTheme.typography.labelMedium.copy(color = TextPrimary),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SteelBlue,
            unfocusedBorderColor = BorderHairline,
            focusedContainerColor = VoidBlack,
            unfocusedContainerColor = VoidBlack,
            focusedLabelColor = SteelBlue,
            unfocusedLabelColor = TextSecondary
        ),
        modifier = modifier
    )
}

@Composable
private fun HorrorSliderRow(
    label: String,
    valueText: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary))
            Text(valueText, style = MaterialTheme.typography.labelMedium.copy(color = CrimsonAlert))
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = CrimsonAlert,
                activeTrackColor = CrimsonAlert,
                inactiveTrackColor = CardSurface
            ),
            modifier = Modifier.height(28.dp)
        )
    }
}

@Composable
private fun TriggerToggleChip(
    label: String,
    checked: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
        color = if (checked) CrimsonAlertDim else VoidBlack,
        border = BorderStroke(1.dp, if (checked) CrimsonAlert else BorderHairline),
        shape = RoundedCornerShape(4.dp)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (checked) TextPrimary else TextSecondary,
                    fontSize = 10.sp
                )
            )
        }
    }
}
