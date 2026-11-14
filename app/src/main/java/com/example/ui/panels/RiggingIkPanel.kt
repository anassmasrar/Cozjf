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
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RiggingState
import com.example.ui.theme.*

@Composable
fun RiggingIkPanel(
    riggingState: RiggingState,
    onSelectArchetype: (String) -> Unit,
    onLaunchAutoRig: () -> Unit,
    onSelectBone: (String) -> Unit,
    onUpdateBoneRotation: (Float, Float, Float) -> Unit,
    onToggleIkLock: (String) -> Unit,
    onToggleWeightPaint: () -> Unit,
    onUpdateWeightBrush: (Float, Float) -> Unit,
    scrollable: Boolean = true,
    modifier: Modifier = Modifier
) {
    val selectedBone = riggingState.bones.find { it.id == riggingState.selectedBoneId }
        ?: riggingState.bones.first()
    val archetypes = listOf("Bipède IK", "Créature / Dragon IK", "Quadrupède IK", "Entité Blockbench IK")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(if (scrollable) Modifier.fillMaxSize().verticalScroll(rememberScrollState()) else Modifier)
            .background(PanelSurface)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "CLAUDE RIGGING LIVE & CINÉMATIQUE INVERSE (IK)",
            style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary)
        )

        // Claude Auto-Rigging Live Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElevatedSurface, RoundedCornerShape(6.dp))
                .border(1.dp, BorderHairline, RoundedCornerShape(6.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CLAUDE LIVE SKELETON BUILDER",
                    style = MaterialTheme.typography.labelSmall.copy(color = SteelBlue, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${riggingState.revealedBonesCount}/${riggingState.bones.size} OS EN DIRECT",
                    style = MaterialTheme.typography.labelSmall.copy(color = TerminalEmerald, fontWeight = FontWeight.Bold)
                )
            }

            // Choix du type de squelette
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                archetypes.forEach { arch ->
                    val active = arch == riggingState.rigArchetype
                    Surface(
                        onClick = { onSelectArchetype(arch) },
                        modifier = Modifier.testTag("rig_arch_${arch.take(6)}"),
                        color = if (active) SteelBlueDim else VoidBlack,
                        border = BorderStroke(1.dp, if (active) SteelBlue else BorderHairline),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = arch,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (active) TextPrimary else TextSecondary,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Button(
                onClick = onLaunchAutoRig,
                enabled = !riggingState.isScanning,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("auto_rig_neural_btn"),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SteelBlue,
                    contentColor = VoidBlack
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.AccessibilityNew,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (riggingState.isScanning) "Claude pose le squelette en direct..." else "Claude Auto-Rigging Live (Voir en direct)",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Text(
                text = riggingState.liveRigStatus,
                style = MaterialTheme.typography.labelSmall.copy(color = TerminalEmerald, fontSize = 10.sp)
            )

            if (riggingState.isScanning) {
                LinearProgressIndicator(
                    progress = { riggingState.scanProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp),
                    color = SteelBlue,
                    trackColor = CardSurface
                )
            }
        }

        // Weight Painting Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElevatedSurface, RoundedCornerShape(6.dp))
                .border(
                    1.dp,
                    if (riggingState.weightPaintOverlay) PrecisionAmber else BorderHairline,
                    RoundedCornerShape(6.dp)
                )
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Brush,
                        contentDescription = null,
                        tint = PrecisionAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "VUE WEIGHT PAINTING & SQUELETTE",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary)
                    )
                }
                Switch(
                    checked = riggingState.weightPaintOverlay,
                    onCheckedChange = { onToggleWeightPaint() },
                    modifier = Modifier.testTag("weight_paint_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = VoidBlack,
                        checkedTrackColor = PrecisionAmber
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Poids: ${String.format("%.2f", riggingState.brushWeight)}",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                    Slider(
                        value = riggingState.brushWeight,
                        onValueChange = { onUpdateWeightBrush(it, riggingState.brushFalloff) },
                        colors = SliderDefaults.colors(thumbColor = PrecisionAmber, activeTrackColor = PrecisionAmber),
                        modifier = Modifier.height(26.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Falloff: ${String.format("%.2f", riggingState.brushFalloff)}",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                    Slider(
                        value = riggingState.brushFalloff,
                        onValueChange = { onUpdateWeightBrush(riggingState.brushWeight, it) },
                        colors = SliderDefaults.colors(thumbColor = SteelBlue, activeTrackColor = SteelBlue),
                        modifier = Modifier.height(26.dp)
                    )
                }
            }
        }

        // IK Joint Hierarchy & Live Rotation Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElevatedSurface, RoundedCornerShape(6.dp))
                .border(1.dp, BorderHairline, RoundedCornerShape(6.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "ARTICULATIONS LIVE (${riggingState.bones.size} OS • SYMÉTRIE ${riggingState.symmetryScore}%)",
                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
            )

            riggingState.bones.forEachIndexed { idx, bone ->
                val isSelected = bone.id == selectedBone.id
                val revealed = idx < riggingState.revealedBonesCount
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) SteelBlueDim.copy(alpha = 0.6f) else VoidBlack)
                        .border(1.dp, if (isSelected) SteelBlue else BorderHairline, RoundedCornerShape(4.dp))
                        .clickable { onSelectBone(bone.id) }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (revealed) bone.name else "${bone.name} (en attente)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (isSelected) TextPrimary else if (revealed) TextSecondary else TextMuted
                            )
                        )
                        Text(
                            text = "${bone.ikChain} | P:${bone.pitchDeg.toInt()}° Y:${bone.yawDeg.toInt()}°",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                        )
                    }
                    IconButton(
                        onClick = { onToggleIkLock(bone.id) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (bone.ikLocked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                            contentDescription = "Verrouiller cible IK",
                            tint = if (bone.ikLocked) TerminalEmerald else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = BorderHairline)

            Text(
                text = "ORIENTATION EN DIRECT : ${selectedBone.name}",
                style = MaterialTheme.typography.labelSmall.copy(color = SteelBlue)
            )

            BoneAxisSlider(
                axis = "Pitch (X)",
                value = selectedBone.pitchDeg,
                onChange = { onUpdateBoneRotation(it, selectedBone.yawDeg, selectedBone.rollDeg) }
            )
            BoneAxisSlider(
                axis = "Yaw (Y)",
                value = selectedBone.yawDeg,
                onChange = { onUpdateBoneRotation(selectedBone.pitchDeg, it, selectedBone.rollDeg) }
            )
            BoneAxisSlider(
                axis = "Roll (Z)",
                value = selectedBone.rollDeg,
                onChange = { onUpdateBoneRotation(selectedBone.pitchDeg, selectedBone.yawDeg, it) }
            )
        }
    }
}

@Composable
private fun BoneAxisSlider(
    axis: String,
    value: Float,
    onChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = axis,
            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
            modifier = Modifier.width(64.dp)
        )
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = -90f..90f,
            colors = SliderDefaults.colors(thumbColor = SteelBlue, activeTrackColor = SteelBlue),
            modifier = Modifier.weight(1f).height(26.dp)
        )
        Text(
            text = "${value.toInt()}°",
            style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary),
            modifier = Modifier.width(38.dp)
        )
    }
}
