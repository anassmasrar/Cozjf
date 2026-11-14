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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BlockbenchTargetType
import com.example.model.ModelingAiEngine
import com.example.ui.theme.*
import com.example.viewmodel.OmniStudioUiState

@Composable
fun AiAssistantPanel(
    uiState: OmniStudioUiState,
    onSelectEngine: (ModelingAiEngine) -> Unit,
    onSelectBlockbenchTarget: (BlockbenchTargetType) -> Unit,
    onPromptChange: (String) -> Unit,
    onSendChat: (String?) -> Unit,
    onGenerate3D: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val quickPrompts = listOf(
        "Dragon ailé" to true,
        "Épée rubis" to true,
        "Voiture futuriste" to true,
        "Créature Stalker" to true,
        "Comment faire du rigging live ?" to false
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PanelSurface)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header Moteurs IA Gratuits (0 Crédit)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = SteelBlue, modifier = Modifier.size(18.dp))
                Column {
                    Text(
                        text = "MOTEURS IA STUDIO (0 CRÉDIT • ILLIMITÉ)",
                        style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Meshy 3D, Tripo3D, Blockbench IA & Claude sans aucun abonnement",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                    )
                }
            }
            Surface(
                color = TerminalEmeraldDim,
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, TerminalEmerald.copy(alpha = 0.6f))
            ) {
                Text(
                    text = "100% GRATUIT",
                    style = MaterialTheme.typography.labelSmall.copy(color = TerminalEmerald, fontSize = 9.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }

        // Sélecteur de moteur IA (Meshy 3D / Tripo3D / Blockbench IA / Claude)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ModelingAiEngine.entries.forEach { engine ->
                val selected = engine == uiState.selectedModelingEngine
                Surface(
                    onClick = { onSelectEngine(engine) },
                    modifier = Modifier.testTag("engine_chip_${engine.name}"),
                    color = if (selected) SteelBlueDim else ElevatedSurface,
                    border = BorderStroke(1.dp, if (selected) SteelBlue else BorderHairline),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = engine.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (selected) TextPrimary else TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = engine.badge,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TerminalEmerald,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Text(
                            text = engine.description,
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp)
                        )
                    }
                }
            }
        }

        // Si Blockbench IA sélectionné : choix Bloc 3D vs Entité / Mob vs Objet
        if (uiState.selectedModelingEngine == ModelingAiEngine.BLOCKBENCH_IA) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ElevatedSurface, RoundedCornerShape(4.dp))
                    .border(1.dp, BorderHairline, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CIBLE BLOCKBENCH:",
                    style = MaterialTheme.typography.labelSmall.copy(color = PrecisionAmber, fontSize = 10.sp)
                )
                BlockbenchTargetType.entries.forEach { target ->
                    val active = target == uiState.selectedBlockbenchTarget
                    Surface(
                        onClick = { onSelectBlockbenchTarget(target) },
                        modifier = Modifier.testTag("bb_target_${target.name}"),
                        color = if (active) PrecisionAmberDim else VoidBlack,
                        border = BorderStroke(1.dp, if (active) PrecisionAmber else BorderHairline),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = target.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (active) PrecisionAmber else TextSecondary,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Suggestions rapides 1-clic
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickPrompts.forEach { (preset, is3D) ->
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(enabled = !uiState.aiIsGenerating) {
                            if (is3D) onGenerate3D(preset) else onSendChat(preset)
                        },
                    color = ElevatedSurface,
                    border = BorderStroke(1.dp, BorderHairline),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = preset,
                        style = MaterialTheme.typography.labelSmall.copy(color = SlateSilver),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Fil de conversation & résultats IA
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(VoidBlack, RoundedCornerShape(6.dp))
                .border(1.dp, BorderHairline, RoundedCornerShape(6.dp))
                .verticalScroll(rememberScrollState())
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            uiState.aiMessages.forEach { msg ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = when {
                        msg.isError -> CrimsonAlertDim
                        msg.isUser -> SteelBlueDim.copy(alpha = 0.65f)
                        else -> ElevatedSurface
                    },
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(
                        1.dp,
                        when {
                            msg.isError -> CrimsonAlert
                            msg.isUser -> SteelBlue
                            else -> BorderHairline
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (msg.isUser) "Vous" else msg.engineLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (msg.isUser) SteelBlue else TerminalEmerald,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = msg.text,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary)
                        )
                        if (msg.generatedObjectName != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .background(TerminalEmeraldDim, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Filled.ViewInAr, contentDescription = null, tint = TerminalEmerald, modifier = Modifier.size(13.dp))
                                Text(
                                    text = "Ajouté dans la scène 3D : ${msg.generatedObjectName}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TerminalEmerald)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Saisie + Boutons ("Générer en 3D" via Meshy/Tripo/Blockbench & "Question Claude")
        OutlinedTextField(
            value = uiState.aiPromptInput,
            onValueChange = onPromptChange,
            placeholder = {
                Text(
                    "Décrivez un objet (${uiState.selectedModelingEngine.label}) ou posez une question à Claude...",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                )
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
                .fillMaxWidth()
                .testTag("ai_studio_prompt_input")
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onGenerate3D(null) },
                enabled = uiState.aiPromptInput.isNotBlank() && !uiState.aiIsGenerating,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 46.dp)
                    .testTag("ai_generate_3d_btn"),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SteelBlue, contentColor = VoidBlack)
            ) {
                Icon(Icons.Filled.ViewInAr, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Créer (${uiState.selectedModelingEngine.label})",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            OutlinedButton(
                onClick = { onSendChat(null) },
                enabled = uiState.aiPromptInput.isNotBlank() && !uiState.aiIsGenerating,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 46.dp)
                    .testTag("ai_ask_chat_btn"),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, SteelBlue)
            ) {
                Icon(Icons.Filled.Send, contentDescription = null, tint = SteelBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Question Claude", color = SteelBlue, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
