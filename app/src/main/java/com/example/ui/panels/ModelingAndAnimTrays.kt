package com.example.ui.panels

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AnimationStyle
import com.example.model.BlockbenchTargetType
import com.example.model.GeometryMode
import com.example.model.ModelingAiEngine
import com.example.model.PrimitiveShape
import com.example.ui.theme.*
import com.example.viewmodel.OmniStudioUiState
import com.example.viewmodel.OmniStudioViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ModelingTrayPanel(
    uiState: OmniStudioUiState,
    onAddShape: (PrimitiveShape) -> Unit,
    onDeleteSelected: () -> Unit,
    onOpenImportExport: () -> Unit,
    onOpenShadersModal: () -> Unit,
    onSelectEngine: (ModelingAiEngine) -> Unit,
    onSelectBlockbenchTarget: (BlockbenchTargetType) -> Unit,
    onOpenAiTab: () -> Unit,
    onSetGeometryMode: (GeometryMode) -> Unit,
    onSubdivChange: (Int) -> Unit,
    onVoxelResChange: (Int) -> Unit,
    onTogglePhysics: () -> Unit,
    onPromptChange: (String) -> Unit,
    onRunOmniMind: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PanelSurface)
            .verticalScroll(rememberScrollState())
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Section 1 : Formes 3D + Shaders & FX + Importer + Supprimer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AJOUTER UNE FORME",
                style = MaterialTheme.typography.labelSmall.copy(color = SteelBlue, fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Surface(
                    onClick = onOpenShadersModal,
                    modifier = Modifier.testTag("tray_shaders_btn"),
                    color = ElevatedSurface,
                    border = BorderStroke(1.dp, TerminalEmerald.copy(alpha = 0.65f)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = "Shaders & FX", tint = TerminalEmerald, modifier = Modifier.size(12.dp))
                        Text("Shaders & FX", style = MaterialTheme.typography.labelSmall.copy(color = TerminalEmerald, fontSize = 10.sp))
                    }
                }
                Surface(
                    onClick = onOpenImportExport,
                    modifier = Modifier.testTag("tray_import_btn"),
                    color = ElevatedSurface,
                    border = BorderStroke(1.dp, SteelBlue.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(Icons.Filled.UploadFile, contentDescription = "Importer", tint = SteelBlue, modifier = Modifier.size(12.dp))
                        Text("Import", style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary, fontSize = 10.sp))
                    }
                }
                Surface(
                    onClick = onDeleteSelected,
                    modifier = Modifier.testTag("tray_delete_btn"),
                    color = ElevatedSurface,
                    border = BorderStroke(1.dp, CrimsonAlert.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = "Supprimer", tint = CrimsonAlert, modifier = Modifier.size(12.dp))
                        Text("Suppr.", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp))
                    }
                }
            }
        }

        // Boutons directs "+ Cube", "+ Sphère", "+ Cylindre", etc.
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PrimitiveShape.entries.forEach { shape ->
                val selected = shape == uiState.activeShape
                Surface(
                    onClick = { onAddShape(shape) },
                    modifier = Modifier.testTag("add_shape_${shape.name}"),
                    color = if (selected) SteelBlueDim else ElevatedSurface,
                    border = BorderStroke(1.dp, if (selected) SteelBlue else BorderHairline),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = if (selected) SteelBlue else TextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = shape.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (selected) TextPrimary else TextSecondary,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        )
                    }
                }
            }
        }

        // Section 2 : Moteur Modélisation 3D Gratuit (Meshy / Tripo / Blockbench)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "IA 3D :",
                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp)
            )
            listOf(ModelingAiEngine.MESHY_3D, ModelingAiEngine.TRIPO_3D, ModelingAiEngine.BLOCKBENCH_IA).forEach { engine ->
                val active = engine == uiState.selectedModelingEngine
                Surface(
                    onClick = { onSelectEngine(engine) },
                    modifier = Modifier.testTag("tray_engine_${engine.name}"),
                    color = if (active) SteelBlueDim else ElevatedSurface,
                    border = BorderStroke(1.dp, if (active) SteelBlue else BorderHairline),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "${engine.label} (${engine.badge})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (active) TextPrimary else TextSecondary,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                    )
                }
            }
            if (uiState.selectedModelingEngine == ModelingAiEngine.BLOCKBENCH_IA) {
                BlockbenchTargetType.entries.forEach { target ->
                    val activeTarget = target == uiState.selectedBlockbenchTarget
                    Surface(
                        onClick = { onSelectBlockbenchTarget(target) },
                        color = if (activeTarget) PrecisionAmberDim else VoidBlack,
                        border = BorderStroke(1.dp, if (activeTarget) PrecisionAmber else BorderHairline),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = target.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (activeTarget) PrecisionAmber else TextMuted,
                                fontSize = 9.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
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
                value = uiState.omniMindPrompt,
                onValueChange = onPromptChange,
                leadingIcon = {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = SteelBlue, modifier = Modifier.size(16.dp))
                },
                placeholder = {
                    Text("${uiState.selectedModelingEngine.label} (0 crédit) : décrire l'objet...", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted))
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(color = TextPrimary),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SteelBlue,
                    unfocusedBorderColor = BorderHairline,
                    focusedContainerColor = VoidBlack,
                    unfocusedContainerColor = VoidBlack
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("omnimind_prompt_input")
            )
            IconButton(
                onClick = onRunOmniMind,
                enabled = uiState.omniMindPrompt.isNotBlank(),
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (uiState.omniMindPrompt.isNotBlank()) SteelBlueDim else ElevatedSurface)
                    .border(1.dp, if (uiState.omniMindPrompt.isNotBlank()) SteelBlue else BorderHairline, RoundedCornerShape(4.dp))
                    .testTag("omnimind_send_btn")
            ) {
                Icon(Icons.Filled.Send, contentDescription = "Générer avec IA", tint = SteelBlue, modifier = Modifier.size(16.dp))
            }
            Surface(
                modifier = Modifier
                    .height(44.dp)
                    .testTag("open_ai_tab_shortcut_btn")
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(onClick = onOpenAiTab),
                color = SteelBlueDim,
                border = BorderStroke(1.dp, SteelBlue),
                shape = RoundedCornerShape(4.dp)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Studio IA", style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                }
            }
        }

        if (uiState.omniMindStatus != null) {
            Text(
                text = uiState.omniMindStatus,
                style = MaterialTheme.typography.labelSmall.copy(color = TerminalEmerald)
            )
        }

        // Section 3 : Moteur Géométrie (Smooth vs Voxel) + Physique
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(ElevatedSurface, RoundedCornerShape(4.dp))
                    .border(1.dp, BorderHairline, RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (uiState.geometryMode == GeometryMode.SMOOTH_ORGANIQUE) {
                            "Smooth SDF (Subdiv ${uiState.smoothSubdivision})"
                        } else {
                            "Grille Voxel (${uiState.voxelGridResolution}^3)"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary)
                    )
                    Text(
                        text = if (uiState.geometryMode == GeometryMode.SMOOTH_ORGANIQUE) "Passer VOXEL" else "Passer SMOOTH",
                        style = MaterialTheme.typography.labelSmall.copy(color = SteelBlue),
                        modifier = Modifier.clickable {
                            onSetGeometryMode(
                                if (uiState.geometryMode == GeometryMode.SMOOTH_ORGANIQUE)
                                    GeometryMode.VOXEL_MODULAIRE else GeometryMode.SMOOTH_ORGANIQUE
                            )
                        }
                    )
                }
                if (uiState.geometryMode == GeometryMode.SMOOTH_ORGANIQUE) {
                    Slider(
                        value = uiState.smoothSubdivision.toFloat(),
                        onValueChange = { onSubdivChange(it.roundToInt()) },
                        valueRange = 1f..5f,
                        steps = 3,
                        colors = SliderDefaults.colors(thumbColor = SteelBlue, activeTrackColor = SteelBlue),
                        modifier = Modifier.height(24.dp)
                    )
                } else {
                    Row(
                        modifier = Modifier.padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(16, 32, 64).forEach { res ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(3.dp))
                                    .clickable { onVoxelResChange(res) },
                                color = if (uiState.voxelGridResolution == res) SteelBlueDim else VoidBlack,
                                border = BorderStroke(1.dp, if (uiState.voxelGridResolution == res) SteelBlue else BorderHairline),
                                shape = RoundedCornerShape(3.dp)
                            ) {
                                Box(modifier = Modifier.padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
                                    Text("${res}^3", style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary))
                                }
                            }
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(onClick = onTogglePhysics),
                color = if (uiState.physics.active) SteelBlueDim else ElevatedSurface,
                border = BorderStroke(1.dp, if (uiState.physics.active) SteelBlue else BorderHairline),
                shape = RoundedCornerShape(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Waves,
                        contentDescription = "Simulation physique",
                        tint = if (uiState.physics.active) SteelBlue else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (uiState.physics.active) "Physique ON" else "Physique",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (uiState.physics.active) TextPrimary else TextSecondary,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AnimationAndRiggingTrayPanel(
    uiState: OmniStudioUiState,
    viewModel: OmniStudioViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PanelSurface)
            .verticalScroll(rememberScrollState())
    ) {
        TimelinePanel(
            timeline = uiState.timeline,
            compactMode = false,
            onTogglePlay = viewModel::toggleTimelinePlayback,
            onScrubFrame = viewModel::scrubFrame,
            onToggleKeyframe = viewModel::toggleKeyframeAtCurrent,
            onSelectClip = viewModel::selectAnimationClip
        )

        // Claude Animation IA Multi-Styles (Kevin Temmer Tunes, Réaliste, Anime, Blockbench, Horreur, Stop-Motion)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElevatedSurface)
                .border(1.dp, SteelBlue.copy(alpha = 0.4f))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Filled.MovieFilter, contentDescription = null, tint = SteelBlue, modifier = Modifier.size(16.dp))
                Text(
                    text = "CLAUDE ANIMATION IA MULTI-STYLES",
                    style = MaterialTheme.typography.labelSmall.copy(color = SteelBlue, fontWeight = FontWeight.Bold)
                )
            }
            Text(
                text = uiState.timeline.activeStyle.description,
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                AnimationStyle.entries.forEach { style ->
                    val selected = style == uiState.timeline.activeStyle
                    Surface(
                        onClick = { viewModel.selectAnimationStyle(style) },
                        modifier = Modifier.testTag("anim_style_${style.name}"),
                        color = if (selected) SteelBlueDim else VoidBlack,
                        border = BorderStroke(1.dp, if (selected) SteelBlue else BorderHairline),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = style.shortLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (selected) TextPrimary else TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.aiAnimPrompt,
                    onValueChange = viewModel::updateAiAnimPrompt,
                    placeholder = {
                        Text("Ex: Danse cartoon Kevin Temmer, saut acrobatique...", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted))
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall.copy(color = TextPrimary),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_anim_prompt_input")
                )
                Button(
                    onClick = { viewModel.generateAiAnimation() },
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("ai_anim_generate_btn"),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SteelBlue, contentColor = VoidBlack)
                ) {
                    Text("Animer", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Contrôles directs de pose pour créer/modifier une Keyframe à la frame courante
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElevatedSurface)
                .border(1.dp, BorderHairline)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "POSE KEYFRAME (FRAME ${uiState.timeline.currentFrame})",
                style = MaterialTheme.typography.labelSmall.copy(color = PrecisionAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Hauteur Y", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 9.sp))
                    Slider(
                        value = uiState.keyframeEditOffsetY,
                        onValueChange = { viewModel.updateKeyframePoseDraft(it, uiState.keyframeEditRotY, uiState.keyframeEditScale) },
                        valueRange = -0.4f..1.0f,
                        colors = SliderDefaults.colors(thumbColor = PrecisionAmber, activeTrackColor = PrecisionAmber),
                        modifier = Modifier.height(22.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Rot Y (${uiState.keyframeEditRotY.toInt()}°)", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 9.sp))
                    Slider(
                        value = uiState.keyframeEditRotY,
                        onValueChange = { viewModel.updateKeyframePoseDraft(uiState.keyframeEditOffsetY, it, uiState.keyframeEditScale) },
                        valueRange = 0f..360f,
                        colors = SliderDefaults.colors(thumbColor = SteelBlue, activeTrackColor = SteelBlue),
                        modifier = Modifier.height(22.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Squash KT", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 9.sp))
                    Slider(
                        value = uiState.keyframeEditSquashY,
                        onValueChange = { viewModel.updateKeyframePoseDraft(uiState.keyframeEditOffsetY, uiState.keyframeEditRotY, uiState.keyframeEditScale, squashY = it) },
                        valueRange = 0.65f..1.45f,
                        colors = SliderDefaults.colors(thumbColor = TerminalEmerald, activeTrackColor = TerminalEmerald),
                        modifier = Modifier.height(22.dp)
                    )
                }
            }
        }

        RiggingIkPanel(
            riggingState = uiState.rigging,
            onSelectArchetype = viewModel::selectRigArchetype,
            onLaunchAutoRig = viewModel::launchNeuralAutoRig,
            onSelectBone = viewModel::selectBone,
            onUpdateBoneRotation = viewModel::updateSelectedBoneRotation,
            onToggleIkLock = viewModel::toggleBoneIkLock,
            onToggleWeightPaint = viewModel::toggleWeightPaintOverlay,
            onUpdateWeightBrush = viewModel::updateWeightBrush,
            scrollable = false
        )
    }
}
