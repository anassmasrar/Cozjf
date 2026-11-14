package com.example.ui.panels

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.OmniStudioUiState
import com.example.viewmodel.OmniStudioViewModel

@Composable
fun SettingsAndBridgesPanel(
    uiState: OmniStudioUiState,
    viewModel: OmniStudioViewModel,
    onSignIn: () -> Unit = {},
    onSignOut: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isSignedIn = uiState.currentUserId != null
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PanelSurface)
            .verticalScroll(rememberScrollState())
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section 0 : Compte Google & Sauvegarde Cloud Firestore (Optionnel — requis pour Creator Hub)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElevatedSurface, RoundedCornerShape(6.dp))
                .border(1.dp, SteelBlue.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = SteelBlue, modifier = Modifier.size(28.dp))
                    Column {
                        Text(
                            text = if (isSignedIn) uiState.userDisplayName else "Mode Studio Libre (Sans compte)",
                            style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isSignedIn) {
                                uiState.userEmail.ifBlank { "Compte Google connecté • Cloud & Hub actifs" }
                            } else {
                                "Connexion Google requise uniquement pour le Creator Hub & Cloud"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                        )
                    }
                }
                if (isSignedIn) {
                    OutlinedButton(
                        onClick = onSignOut,
                        modifier = Modifier.testTag("settings_sign_out_btn"),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, CrimsonAlert.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = CrimsonAlert, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Déconnexion", color = CrimsonAlert, style = MaterialTheme.typography.labelSmall)
                    }
                } else {
                    Button(
                        onClick = onSignIn,
                        modifier = Modifier.testTag("settings_sign_in_btn"),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SteelBlue, contentColor = VoidBlack)
                    ) {
                        Text("Connexion Google", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.saveCurrentProjectToCloud() },
                    enabled = !uiState.isSavingCloud,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp)
                        .testTag("settings_cloud_save_btn"),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SteelBlue, contentColor = VoidBlack)
                ) {
                    Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (uiState.isSavingCloud) "Envoi..." else "Sauvegarder", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { viewModel.toggleCloudProjectsModal(true) },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp)
                        .testTag("settings_cloud_projects_btn"),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, SteelBlue)
                ) {
                    Icon(Icons.Filled.FolderOpen, contentDescription = null, tint = SteelBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mes Projets (${uiState.savedProjects.size})", color = SteelBlue)
                }
            }
        }

        // Section 1: Pont Sans Fil & Hybrid Offloading
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Filled.Link, contentDescription = null, tint = SteelBlue, modifier = Modifier.size(16.dp))
                    Text("PONT SANS FIL TÉLÉPHONE-PC", style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary))
                }
                Switch(
                    checked = uiState.bridgeStatus.connected,
                    onCheckedChange = { viewModel.toggleBridgeConnection() },
                    colors = SwitchDefaults.colors(checkedThumbColor = VoidBlack, checkedTrackColor = TerminalEmerald)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VoidBlack, RoundedCornerShape(4.dp))
                    .border(1.dp, BorderHairline, RoundedCornerShape(4.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Computer, contentDescription = null, tint = SlateSilver, modifier = Modifier.size(15.dp))
                    Column {
                        Text("Hybrid Offloading", style = MaterialTheme.typography.labelMedium.copy(color = TextPrimary))
                        Text(
                            text = "Mobile = Tactile | PC = Path Tracing & Build",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp)
                        )
                    }
                }
                Switch(
                    checked = uiState.bridgeStatus.hybridOffloading,
                    onCheckedChange = { viewModel.toggleHybridOffloading() },
                    colors = SwitchDefaults.colors(checkedThumbColor = VoidBlack, checkedTrackColor = SteelBlue)
                )
            }
        }

        // Section 2: Applications & Moteurs IA Connectés (100% Gratuit)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = SteelBlue, modifier = Modifier.size(16.dp))
                    Text("APPLICATIONS & IA CONNECTÉES", style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary))
                }
                Text(
                    text = "0 CRÉDIT • ILLIMITÉ",
                    style = MaterialTheme.typography.labelSmall.copy(color = TerminalEmerald, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                )
            }

            uiState.aiProviders.forEach { provider ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(VoidBlack)
                        .border(1.dp, if (provider.connected) SteelBlue.copy(alpha = 0.5f) else BorderHairline, RoundedCornerShape(4.dp))
                        .clickable { viewModel.toggleAiProvider(provider.id) }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (provider.connected) TerminalEmerald else TextMuted)
                        )
                        Column {
                            Text(
                                text = provider.name,
                                style = MaterialTheme.typography.labelMedium.copy(color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = provider.role,
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp)
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = TerminalEmeraldDim,
                            shape = RoundedCornerShape(3.dp)
                        ) {
                            Text(
                                text = provider.badge,
                                style = MaterialTheme.typography.labelSmall.copy(color = TerminalEmerald, fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = if (provider.connected) "ACTIF" else "INACTIF",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (provider.connected) TerminalEmerald else TextMuted
                            )
                        )
                    }
                }
            }
        }

        // Section 3: Méta-IA & Addons Hot-Reloading
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElevatedSurface, RoundedCornerShape(6.dp))
                .border(1.dp, BorderHairline, RoundedCornerShape(6.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Filled.Bolt, contentDescription = null, tint = PrecisionAmber, modifier = Modifier.size(16.dp))
                Text("MÉTA-IA & ADDONS HOT-RELOAD", style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary))
            }

            OutlinedTextField(
                value = uiState.addonPromptInput,
                onValueChange = viewModel::updateAddonPrompt,
                placeholder = {
                    Text("Ex: Solveur fluide 60 FPS ou générateur d'arbres...", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted))
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(color = TextPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("addon_prompt_input")
            )

            Button(
                onClick = viewModel::generateAndInjectAddon,
                enabled = uiState.addonPromptInput.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 44.dp)
                    .testTag("inject_addon_btn"),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SteelBlue, contentColor = VoidBlack)
            ) {
                Text("Déployer en Mémoire (Sans Redémarrage)", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            }

            uiState.dynamicAddons.forEach { addon ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(VoidBlack, RoundedCornerShape(4.dp))
                        .border(1.dp, BorderHairline, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(addon.name, style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary))
                        Text("${addon.promptOrigin} | ${addon.hotReloadMs}ms", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp))
                    }
                    Switch(
                        checked = addon.active,
                        onCheckedChange = { viewModel.toggleAddonActive(addon.id) },
                        colors = SwitchDefaults.colors(checkedThumbColor = VoidBlack, checkedTrackColor = TerminalEmerald)
                    )
                }
            }
        }

        // Section 4: Noyau Libre & API Ouverte (SDK)
        SystemSdkPanel(
            sdkDocs = uiState.sdkDocs,
            selectedSdkId = uiState.selectedSdkId,
            onSelectSdkModule = viewModel::selectSdkModule,
            scrollable = false
        )
    }
}
