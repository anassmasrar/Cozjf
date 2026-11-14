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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DynamicAddon
import com.example.ui.theme.*

@Composable
fun AddonsHotReloadPanel(
    promptInput: String,
    addons: List<DynamicAddon>,
    hotReloadLogs: List<String>,
    onPromptChange: (String) -> Unit,
    onGenerateAndInject: () -> Unit,
    onToggleAddon: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val quickPrompts = listOf(
        "Générateur de ponts suspendus",
        "Auto-retopologie quads symétrique",
        "Shader pluie sur visière"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PanelSurface)
            .verticalScroll(rememberScrollState())
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "HUB D'ADDONS DYNAMIQUES & HOT-RELOADING",
                style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier
                    .background(TerminalEmeraldDim, RoundedCornerShape(4.dp))
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(TerminalEmerald)
                )
                Text(
                    text = "HOT-RELOAD ACTIF",
                    style = MaterialTheme.typography.labelSmall.copy(color = TerminalEmerald, fontSize = 9.sp)
                )
            }
        }

        // Extension Request Console
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElevatedSurface, RoundedCornerShape(6.dp))
                .border(1.dp, BorderHairline, RoundedCornerShape(6.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "CONSOLE DE REQUÊTE DE FONCTIONNALITÉ",
                style = MaterialTheme.typography.labelSmall.copy(color = SteelBlue)
            )

            OutlinedTextField(
                value = promptInput,
                onValueChange = onPromptChange,
                placeholder = {
                    Text(
                        "Ex: Ajouter un opérateur de fracture Voronoi en temps réel...",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                    )
                },
                textStyle = MaterialTheme.typography.bodySmall.copy(color = TextPrimary),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SteelBlue,
                    unfocusedBorderColor = BorderHairline,
                    focusedContainerColor = VoidBlack,
                    unfocusedContainerColor = VoidBlack
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("addon_prompt_input")
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickPrompts.forEach { preset ->
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onPromptChange(preset) },
                        color = VoidBlack,
                        border = BorderStroke(1.dp, BorderHairline),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = preset,
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Button(
                onClick = onGenerateAndInject,
                enabled = promptInput.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("inject_addon_btn"),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SteelBlue,
                    contentColor = VoidBlack
                )
            ) {
                Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Générer & Injecter en Direct (Hot-Reload)",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        // Active Dynamic Modules
        Text(
            text = "MODULES INJECTÉS (${addons.size})",
            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
        )

        addons.forEach { addon ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ElevatedSurface, RoundedCornerShape(4.dp))
                    .border(1.dp, if (addon.active) TerminalEmerald.copy(alpha = 0.45f) else BorderHairline, RoundedCornerShape(4.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Extension,
                        contentDescription = null,
                        tint = if (addon.active) TerminalEmerald else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = addon.name,
                            style = MaterialTheme.typography.labelMedium.copy(color = TextPrimary)
                        )
                        Text(
                            text = "${addon.promptOrigin} | ${addon.memoryKb} Ko | ${addon.hotReloadMs} ms",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp)
                        )
                    }
                }
                Switch(
                    checked = addon.active,
                    onCheckedChange = { onToggleAddon(addon.id) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = VoidBlack,
                        checkedTrackColor = TerminalEmerald
                    )
                )
            }
        }

        // Live Injection Monitor Stream
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(VoidBlack, RoundedCornerShape(6.dp))
                .border(1.dp, BorderHairline, RoundedCornerShape(6.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "MONITEUR D'INJECTION EN DIRECT",
                style = MaterialTheme.typography.labelSmall.copy(color = PrecisionAmber)
            )
            hotReloadLogs.take(5).forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (line.contains("LIVE") || line.contains("ACTIF")) TerminalEmerald else TextSecondary,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}
