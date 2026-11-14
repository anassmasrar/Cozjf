package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun HeaderBridgeBar(
    bridgeStatus: BridgeStatus,
    controlMode: ControlMode,
    activeTab: OmniCoreTab,
    isWorkstationLayout: Boolean,
    showGrid: Boolean,
    xRayMode: Boolean,
    symmetryEnabled: Boolean,
    viewportShading: ViewportShading,
    isSavingCloud: Boolean,
    userDisplayName: String,
    onToggleControlMode: (ControlMode) -> Unit,
    onSelectTab: (OmniCoreTab) -> Unit,
    onOpenBridgeDialog: () -> Unit,
    onOpenImportExport: () -> Unit,
    onNewProject: () -> Unit,
    onSaveToCloud: () -> Unit,
    onOpenCloudProjects: () -> Unit,
    onAddShape: (PrimitiveShape) -> Unit,
    onAddUtilityNode: (String, String) -> Unit,
    onSetShading: (ViewportShading) -> Unit,
    onToggleGrid: () -> Unit,
    onToggleXRay: () -> Unit,
    onToggleSymmetry: () -> Unit,
    onSnapCamera: (Float, Float) -> Unit,
    onResetCamera: () -> Unit,
    onOpenOutliner: () -> Unit,
    onOpenTutorial: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor by animateColorAsState(
        targetValue = if (bridgeStatus.connected) TerminalEmerald else CrimsonAlert,
        label = "bridgeColor"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PanelSurface)
            .border(width = 1.dp, color = BorderHairline)
    ) {
        // Ligne 1 : Marque + Onglets PC (en paysage) + Sauvegarde Cloud + Pont + Mobile/PC
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(ElevatedSurface)
                        .border(1.dp, SteelBlue.copy(alpha = 0.6f), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "OC",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SteelBlue,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Text(
                    text = "OmniCore",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp
                    )
                )
            }

            if (isWorkstationLayout) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OmniCoreTab.entries.forEach { tab ->
                        val selected = tab == activeTab
                        Box(
                            modifier = Modifier
                                .testTag("top_tab_${tab.name}")
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (selected) SteelBlueDim else Color.Transparent)
                                .border(
                                    width = 1.dp,
                                    color = if (selected) SteelBlue else Color.Transparent,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable { onSelectTab(tab) }
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (selected) TextPrimary else TextSecondary,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            // Bouton Sauvegarder Google Cloud
            Surface(
                modifier = Modifier
                    .testTag("header_cloud_save_btn")
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(enabled = !isSavingCloud, onClick = onSaveToCloud),
                color = ElevatedSurface,
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, SteelBlue.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CloudUpload,
                        contentDescription = "Sauvegarder",
                        tint = SteelBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (isSavingCloud) "..." else "Sauver",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary, fontSize = 10.sp)
                    )
                }
            }

            // Wireless Bridge Pill
            Surface(
                modifier = Modifier
                    .testTag("bridge_status_pill")
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(onClick = onOpenBridgeDialog),
                color = ElevatedSurface,
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, BorderHairline)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Icon(
                        imageVector = if (bridgeStatus.connected) Icons.Filled.Link else Icons.Filled.LinkOff,
                        contentDescription = "Pont sans fil",
                        tint = statusColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = if (bridgeStatus.connected) "${bridgeStatus.latencyMs}ms" else "OFF",
                        style = MaterialTheme.typography.labelSmall.copy(color = statusColor, fontSize = 10.sp)
                    )
                }
            }

            // Control Mode Switcher (Mobile vs Station PC)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(VoidBlack)
                    .border(1.dp, BorderHairline, RoundedCornerShape(4.dp))
                    .padding(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ControlModeIcon(
                    selected = controlMode == ControlMode.MOBILE_TACTILE,
                    icon = Icons.Filled.Smartphone,
                    tag = "mode_mobile_btn",
                    desc = "Mode Mobile",
                    onClick = { onToggleControlMode(ControlMode.MOBILE_TACTILE) }
                )
                ControlModeIcon(
                    selected = controlMode == ControlMode.PC_POWERHOUSE,
                    icon = Icons.Filled.Computer,
                    tag = "mode_pc_btn",
                    desc = "Mode Station PC",
                    onClick = { onToggleControlMode(ControlMode.PC_POWERHOUSE) }
                )
            }
        }

        HorizontalDivider(color = BorderHairline)

        // Ligne 2 : Barre de Menus Classique (Fichier | Ajouter | Affichage | Aide)
        ClassicSoftwareMenuBar(
            showGrid = showGrid,
            xRayMode = xRayMode,
            symmetryEnabled = symmetryEnabled,
            viewportShading = viewportShading,
            userDisplayName = userDisplayName,
            onNewProject = onNewProject,
            onSaveToCloud = onSaveToCloud,
            onOpenCloudProjects = onOpenCloudProjects,
            onOpenImportExport = onOpenImportExport,
            onAddShape = onAddShape,
            onAddUtilityNode = onAddUtilityNode,
            onSetShading = onSetShading,
            onToggleGrid = onToggleGrid,
            onToggleXRay = onToggleXRay,
            onToggleSymmetry = onToggleSymmetry,
            onSnapCamera = onSnapCamera,
            onResetCamera = onResetCamera,
            onOpenOutliner = onOpenOutliner,
            onOpenTutorial = onOpenTutorial
        )
    }
}

@Composable
private fun ClassicSoftwareMenuBar(
    showGrid: Boolean,
    xRayMode: Boolean,
    symmetryEnabled: Boolean,
    viewportShading: ViewportShading,
    userDisplayName: String,
    onNewProject: () -> Unit,
    onSaveToCloud: () -> Unit,
    onOpenCloudProjects: () -> Unit,
    onOpenImportExport: () -> Unit,
    onAddShape: (PrimitiveShape) -> Unit,
    onAddUtilityNode: (String, String) -> Unit,
    onSetShading: (ViewportShading) -> Unit,
    onToggleGrid: () -> Unit,
    onToggleXRay: () -> Unit,
    onToggleSymmetry: () -> Unit,
    onSnapCamera: (Float, Float) -> Unit,
    onResetCamera: () -> Unit,
    onOpenOutliner: () -> Unit,
    onOpenTutorial: () -> Unit
) {
    var openMenu by remember { mutableStateOf<String?>(null) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(VoidBlack)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Menu FICHIER
        Box {
            MenuBarTrigger(
                label = "Fichier",
                tag = "menu_fichier_btn",
                active = openMenu == "fichier",
                onClick = { openMenu = "fichier" }
            )
            DropdownMenu(
                expanded = openMenu == "fichier",
                onDismissRequest = { openMenu = null },
                containerColor = ElevatedSurface
            ) {
                DropdownMenuItem(
                    text = { Text("Nouveau projet vide", color = TextPrimary) },
                    leadingIcon = { Icon(Icons.Filled.NoteAdd, null, tint = SteelBlue, modifier = Modifier.size(16.dp)) },
                    onClick = { openMenu = null; onNewProject() }
                )
                DropdownMenuItem(
                    text = { Text("Sauvegarder (Google Cloud)", color = TextPrimary) },
                    leadingIcon = { Icon(Icons.Filled.CloudUpload, null, tint = TerminalEmerald, modifier = Modifier.size(16.dp)) },
                    onClick = { openMenu = null; onSaveToCloud() }
                )
                DropdownMenuItem(
                    text = { Text("Ouvrir mes projets Cloud", color = TextPrimary) },
                    leadingIcon = { Icon(Icons.Filled.FolderOpen, null, tint = SteelBlue, modifier = Modifier.size(16.dp)) },
                    onClick = { openMenu = null; onOpenCloudProjects() }
                )
                HorizontalDivider(color = BorderHairline)
                DropdownMenuItem(
                    text = { Text("Importer / Exporter (.glb, .obj, .fbx...)", color = TextPrimary) },
                    leadingIcon = { Icon(Icons.Filled.ImportExport, null, tint = PrecisionAmber, modifier = Modifier.size(16.dp)) },
                    onClick = { openMenu = null; onOpenImportExport() }
                )
            }
        }

        // Menu AJOUTER
        Box {
            MenuBarTrigger(
                label = "Ajouter",
                tag = "menu_ajouter_btn",
                active = openMenu == "ajouter",
                onClick = { openMenu = "ajouter" }
            )
            DropdownMenu(
                expanded = openMenu == "ajouter",
                onDismissRequest = { openMenu = null },
                containerColor = ElevatedSurface
            ) {
                PrimitiveShape.entries.forEach { shape ->
                    DropdownMenuItem(
                        text = { Text("Forme : ${shape.label}", color = TextPrimary) },
                        leadingIcon = { Icon(Icons.Filled.AddBox, null, tint = SteelBlue, modifier = Modifier.size(16.dp)) },
                        onClick = { openMenu = null; onAddShape(shape) }
                    )
                }
                HorizontalDivider(color = BorderHairline)
                DropdownMenuItem(
                    text = { Text("Lumière Studio", color = TextPrimary) },
                    leadingIcon = { Icon(Icons.Filled.LightMode, null, tint = PrecisionAmber, modifier = Modifier.size(16.dp)) },
                    onClick = { openMenu = null; onAddUtilityNode("Lumière_Studio", "Light") }
                )
                DropdownMenuItem(
                    text = { Text("Caméra", color = TextPrimary) },
                    leadingIcon = { Icon(Icons.Filled.Videocam, null, tint = SlateSilver, modifier = Modifier.size(16.dp)) },
                    onClick = { openMenu = null; onAddUtilityNode("Caméra_Sec", "Camera") }
                )
            }
        }

        // Menu AFFICHAGE
        Box {
            MenuBarTrigger(
                label = "Affichage",
                tag = "menu_affichage_btn",
                active = openMenu == "affichage",
                onClick = { openMenu = "affichage" }
            )
            DropdownMenu(
                expanded = openMenu == "affichage",
                onDismissRequest = { openMenu = null },
                containerColor = ElevatedSurface
            ) {
                ViewportShading.entries.forEach { shading ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Rendu : ${shading.label}" + if (shading == viewportShading) " (actif)" else "",
                                color = if (shading == viewportShading) SteelBlue else TextPrimary
                            )
                        },
                        onClick = { openMenu = null; onSetShading(shading) }
                    )
                }
                HorizontalDivider(color = BorderHairline)
                DropdownMenuItem(
                    text = { Text(if (showGrid) "Masquer la grille" else "Afficher la grille", color = TextPrimary) },
                    onClick = { openMenu = null; onToggleGrid() }
                )
                DropdownMenuItem(
                    text = { Text(if (xRayMode) "Désactiver Rayons X" else "Activer Rayons X", color = TextPrimary) },
                    onClick = { openMenu = null; onToggleXRay() }
                )
                DropdownMenuItem(
                    text = { Text(if (symmetryEnabled) "Désactiver Symétrie X" else "Activer Symétrie X", color = TextPrimary) },
                    onClick = { openMenu = null; onToggleSymmetry() }
                )
                HorizontalDivider(color = BorderHairline)
                DropdownMenuItem(
                    text = { Text("Vue Face", color = TextPrimary) },
                    onClick = { openMenu = null; onSnapCamera(0f, 0f) }
                )
                DropdownMenuItem(
                    text = { Text("Vue Dessus", color = TextPrimary) },
                    onClick = { openMenu = null; onSnapCamera(0f, -72f) }
                )
                DropdownMenuItem(
                    text = { Text("Vue Côté", color = TextPrimary) },
                    onClick = { openMenu = null; onSnapCamera(90f, -10f) }
                )
                DropdownMenuItem(
                    text = { Text("Réinitialiser Caméra (Iso)", color = SteelBlue) },
                    onClick = { openMenu = null; onResetCamera() }
                )
                DropdownMenuItem(
                    text = { Text("Liste des Objets (Scène)", color = TextPrimary) },
                    onClick = { openMenu = null; onOpenOutliner() }
                )
            }
        }

        // Menu AIDE / TUTORIEL
        MenuBarTrigger(
            label = "Aide & Tuto",
            tag = "menu_aide_btn",
            active = false,
            icon = Icons.Outlined.HelpOutline,
            onClick = onOpenTutorial
        )

        Spacer(modifier = Modifier.weight(1f))

        // Quick Import button visible directly in menu bar
        Surface(
            modifier = Modifier
                .testTag("open_import_export_btn")
                .clip(RoundedCornerShape(4.dp))
                .clickable(onClick = onOpenImportExport),
            color = PanelSurface,
            border = BorderStroke(1.dp, BorderHairline),
            shape = RoundedCornerShape(4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Filled.UploadFile, contentDescription = "Importer", tint = SteelBlue, modifier = Modifier.size(13.dp))
                Text("Importer / Export", style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary, fontSize = 10.sp))
            }
        }
    }
}

@Composable
private fun MenuBarTrigger(
    label: String,
    tag: String,
    active: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .testTag(tag)
            .clip(RoundedCornerShape(4.dp))
            .background(if (active) SteelBlueDim else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = label, tint = SteelBlue, modifier = Modifier.size(13.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (active) SteelBlue else SlateSilver,
                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
private fun ControlModeIcon(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tag: String,
    desc: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .testTag(tag)
            .clip(RoundedCornerShape(3.dp))
            .background(if (selected) SteelBlueDim else VoidBlack)
            .clickable(onClick = onClick)
            .padding(horizontal = 7.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = desc,
            tint = if (selected) SteelBlue else TextSecondary,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
fun BridgeTelemetryDialog(
    bridgeStatus: BridgeStatus,
    onToggleConnection: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = PanelSurface,
            border = BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = if (bridgeStatus.connected) TerminalEmerald else CrimsonAlert,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "PONT OMNICORE",
                        style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary)
                    )
                }

                HorizontalDivider(color = BorderHairline)

                BridgeMetricRow("Hôte", bridgeStatus.hostName)
                BridgeMetricRow("Tunnel", bridgeStatus.encryptionProtocol)
                BridgeMetricRow(
                    "Liaison",
                    if (bridgeStatus.connected) "${bridgeStatus.latencyMs} ms | ${bridgeStatus.bandwidthMbps} Mbps" else "HORS LIGNE"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onToggleConnection,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("bridge_toggle_conn_btn"),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (bridgeStatus.connected) CrimsonAlertDim else TerminalEmeraldDim,
                            contentColor = TextPrimary
                        )
                    ) {
                        Text(
                            text = if (bridgeStatus.connected) "Couper" else "Connecter",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Text("Fermer", color = TextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun BridgeMetricRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ElevatedSurface, RoundedCornerShape(4.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
        Text(text = value, style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary))
    }
}
