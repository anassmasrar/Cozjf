package com.example.ui.panels

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.ModPackageExporter
import com.example.model.NativeBuildTargetState
import com.example.ui.theme.*

@Composable
fun BuildNatifPanel(
    buildState: NativeBuildTargetState,
    onToggleOptions: (precompile: Boolean, strip: Boolean) -> Unit,
    onUpdatePlayConfig: (packageId: String, title: String, versionName: String) -> Unit,
    onCompilePlayAab: () -> Unit,
    onProvideAabBytes: () -> ByteArray,
    onExportFeedback: (String) -> Unit,
    onCompileExe: () -> Unit,
    onCompileApk: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val saveAabLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            val bytes = onProvideAabBytes()
            val res = ModPackageExporter.writeBytesToUri(context, uri, bytes)
            res.onSuccess { size ->
                onExportFeedback("Bundle Play Store .AAB exporté ($size octets)")
            }.onFailure { err ->
                onExportFeedback("Erreur export .AAB : ${err.localizedMessage}")
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
            text = "PUBLICATION PLAY STORE GRATUITE (.AAB) & BUILD NATIF",
            style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
        )

        // Build Pipeline Options
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElevatedSurface, RoundedCornerShape(6.dp))
                .border(1.dp, BorderHairline, RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = buildState.precompileShaders,
                    onCheckedChange = { onToggleOptions(it, buildState.stripDebugSymbols) },
                    colors = CheckboxDefaults.colors(checkedColor = SteelBlue)
                )
                Text("Shaders SPIR-V", style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = buildState.stripDebugSymbols,
                    onCheckedChange = { onToggleOptions(buildState.precompileShaders, it) },
                    colors = CheckboxDefaults.colors(checkedColor = SteelBlue)
                )
                Text("Strip Debug", style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary))
            }
        }

        // Target 0: Google Play Store App Bundle (.AAB) — 100% Gratuit sans commission OmniCore
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = ElevatedSurface,
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(1.dp, TerminalEmerald.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(TerminalEmerald.copy(alpha = 0.16f))
                            .border(1.dp, TerminalEmerald.copy(alpha = 0.5f), RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Storefront, contentDescription = null, tint = TerminalEmerald, modifier = Modifier.size(18.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PUBLIER SUR GOOGLE PLAY STORE (.AAB)",
                            style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "0% commission OmniCore • Conforme ARM64 & Play Console",
                            style = MaterialTheme.typography.labelSmall.copy(color = TerminalEmerald, fontSize = 10.sp)
                        )
                    }
                }

                OutlinedTextField(
                    value = buildState.playPackageId,
                    onValueChange = { onUpdatePlayConfig(it, buildState.playAppTitle, buildState.playVersionName) },
                    label = { Text("Package ID (ex: com.studio.jeu)", style = MaterialTheme.typography.labelSmall) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.labelMedium.copy(color = TextPrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = buildState.playAppTitle,
                        onValueChange = { onUpdatePlayConfig(buildState.playPackageId, it, buildState.playVersionName) },
                        label = { Text("Nom du jeu", style = MaterialTheme.typography.labelSmall) },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.labelMedium.copy(color = TextPrimary),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = buildState.playVersionName,
                        onValueChange = { onUpdatePlayConfig(buildState.playPackageId, buildState.playAppTitle, it) },
                        label = { Text("Version", style = MaterialTheme.typography.labelSmall) },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.labelMedium.copy(color = TextPrimary),
                        modifier = Modifier.weight(0.6f)
                    )
                }

                Button(
                    onClick = onCompilePlayAab,
                    enabled = !buildState.isBuildingAab,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("compile_aab_btn"),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TerminalEmerald, contentColor = VoidBlack)
                ) {
                    Text(
                        text = if (buildState.isBuildingAab) "Préparation .AAB..." else "Générer Bundle Play Store (.AAB Gratuit)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                LinearProgressIndicator(
                    progress = { buildState.aabProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = TerminalEmerald,
                    trackColor = CardSurface
                )

                Text(
                    text = buildState.aabStatus,
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                )

                if (buildState.aabArtifact != null) {
                    OutlinedButton(
                        onClick = { saveAabLauncher.launch(buildState.aabArtifact) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 42.dp)
                            .testTag("export_aab_file_btn"),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, TerminalEmerald)
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null, tint = TerminalEmerald, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Enregistrer ${buildState.aabArtifact}", color = TerminalEmerald)
                    }
                }
            }
        }

        // Target 1: Desktop .EXE
        NativeTargetCard(
            title = "EXÉCUTABLE BUREAU WINDOWS (.EXE)",
            subtitle = "Architecture x86_64 | Moteur Vulkan / DX12 | AAA RTGI",
            buttonLabel = if (buildState.isBuildingExe) "Compilation .EXE..." else "Compiler Exécutable (.EXE)",
            buttonTag = "compile_exe_btn",
            icon = Icons.Filled.DesktopWindows,
            accentColor = SteelBlue,
            isBuilding = buildState.isBuildingExe,
            progress = buildState.exeProgress,
            status = buildState.exeStatus,
            artifact = buildState.exeArtifact,
            onBuild = onCompileExe
        )

        // Target 2: Mobile .APK
        NativeTargetCard(
            title = "APPLICATION MOBILE ANDROID (.APK)",
            subtitle = "Architecture ARM64-v8a | PolyX3D+ 60+ FPS | Signature APK v3",
            buttonLabel = if (buildState.isBuildingApk) "Compilation .APK..." else "Compiler Application (.APK)",
            buttonTag = "compile_apk_btn",
            icon = Icons.Filled.Android,
            accentColor = PrecisionAmber,
            isBuilding = buildState.isBuildingApk,
            progress = buildState.apkProgress,
            status = buildState.apkStatus,
            artifact = buildState.apkArtifact,
            onBuild = onCompileApk
        )
    }
}

@Composable
private fun NativeTargetCard(
    title: String,
    subtitle: String,
    buttonLabel: String,
    buttonTag: String,
    icon: ImageVector,
    accentColor: Color,
    isBuilding: Boolean,
    progress: Float,
    status: String,
    artifact: String?,
    onBuild: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ElevatedSurface,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, BorderHairline)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(accentColor.copy(alpha = 0.16f))
                        .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text(title, style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary))
                    Text(subtitle, style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                }
            }

            Button(
                onClick = onBuild,
                enabled = !isBuilding,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag(buttonTag),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = VoidBlack
                )
            ) {
                Text(
                    text = buttonLabel,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = accentColor,
                trackColor = CardSurface
            )

            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
            )

            if (artifact != null) {
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
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = artifact,
                        style = MaterialTheme.typography.labelSmall.copy(color = TerminalEmerald)
                    )
                }
            }
        }
    }
}
