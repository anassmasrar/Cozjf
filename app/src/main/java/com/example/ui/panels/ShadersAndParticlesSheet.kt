package com.example.ui.panels

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.ParticleConfig
import com.example.model.ParticlePreset
import com.example.model.PolyShaderPreset
import com.example.ui.components.resolveEffectiveParticlePreset
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShadersAndParticlesDialog(
    activeShader: PolyShaderPreset,
    particleConfig: ParticleConfig,
    onSelectShader: (PolyShaderPreset) -> Unit,
    onToggleParticles: () -> Unit,
    onSelectParticlePreset: (ParticlePreset) -> Unit,
    onUpdateParticleParams: (density: Int, speed: Float, size: Float, turbulence: Float) -> Unit,
    onDismiss: () -> Unit
) {
    val effectiveParticle = resolveEffectiveParticlePreset(activeShader, particleConfig)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 580.dp)
                .heightIn(max = 640.dp),
            shape = RoundedCornerShape(8.dp),
            color = PanelSurface,
            border = BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ElevatedSurface)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = SteelBlue, modifier = Modifier.size(18.dp))
                        Column {
                            Text(
                                text = "SHADERS POLYX3D PRO & PARTICULES",
                                style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "12 Shaders inspirés PolyX3D + 17 systèmes de particules adaptatifs",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Fermer", tint = TextSecondary)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "SHADERS POLYX3D AMÉLIORÉS (${activeShader.label})",
                        style = MaterialTheme.typography.labelSmall.copy(color = SteelBlue, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = activeShader.description,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PolyShaderPreset.entries.forEach { preset ->
                            val selected = preset == activeShader
                            val accent = Color(preset.accentHex)
                            Surface(
                                onClick = { onSelectShader(preset) },
                                modifier = Modifier.testTag("shader_preset_${preset.name}"),
                                color = if (selected) accent.copy(alpha = 0.22f) else ElevatedSurface,
                                border = BorderStroke(1.dp, if (selected) accent else BorderHairline),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(accent)
                                    )
                                    Text(
                                        text = preset.shortLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (selected) TextPrimary else TextSecondary,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = BorderHairline)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Filled.Grain, contentDescription = null, tint = TerminalEmerald, modifier = Modifier.size(16.dp))
                            Column {
                                Text(
                                    text = "PARTICULES ADAPTATIVES",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TerminalEmerald, fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Actif : ${effectiveParticle.label}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                                )
                            }
                        }
                        Switch(
                            checked = particleConfig.enabled,
                            onCheckedChange = { onToggleParticles() },
                            modifier = Modifier.testTag("particles_toggle_switch"),
                            colors = SwitchDefaults.colors(checkedThumbColor = VoidBlack, checkedTrackColor = TerminalEmerald)
                        )
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ParticlePreset.entries.forEach { p ->
                            val selected = p == particleConfig.preset
                            Surface(
                                onClick = { onSelectParticlePreset(p) },
                                modifier = Modifier.testTag("particle_preset_${p.name}"),
                                color = if (selected) TerminalEmeraldDim else VoidBlack,
                                border = BorderStroke(1.dp, if (selected) TerminalEmerald else BorderHairline),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = p.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (selected) TerminalEmerald else TextSecondary,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    // Réglages physiques particules
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ElevatedSurface, RoundedCornerShape(6.dp))
                            .border(1.dp, BorderHairline, RoundedCornerShape(6.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Densité : ${particleConfig.density} particules",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                        )
                        Slider(
                            value = particleConfig.density.toFloat(),
                            onValueChange = {
                                onUpdateParticleParams(it.toInt(), particleConfig.speed, particleConfig.size, particleConfig.turbulence)
                            },
                            valueRange = 8f..120f,
                            colors = SliderDefaults.colors(thumbColor = TerminalEmerald, activeTrackColor = TerminalEmerald),
                            modifier = Modifier.height(24.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Vitesse (${String.format("%.1f", particleConfig.speed)}x)", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp))
                                Slider(
                                    value = particleConfig.speed,
                                    onValueChange = { onUpdateParticleParams(particleConfig.density, it, particleConfig.size, particleConfig.turbulence) },
                                    valueRange = 0.2f..2.5f,
                                    colors = SliderDefaults.colors(thumbColor = SteelBlue, activeTrackColor = SteelBlue),
                                    modifier = Modifier.height(24.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Turbulence", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp))
                                Slider(
                                    value = particleConfig.turbulence,
                                    onValueChange = { onUpdateParticleParams(particleConfig.density, particleConfig.speed, particleConfig.size, it) },
                                    valueRange = 0f..1.5f,
                                    colors = SliderDefaults.colors(thumbColor = PrecisionAmber, activeTrackColor = PrecisionAmber),
                                    modifier = Modifier.height(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
