package com.example.ui.modals

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.credentials.CredentialManager
import com.example.data.repository.StudioProject
import com.example.firebase.onGoogleSignInClicked
import com.example.model.defaultTutorialSteps
import com.example.ui.theme.*

@Composable
fun TutorialDialog(
    stepIndex: Int,
    onSetStep: (Int) -> Unit,
    onJumpToTab: (com.example.model.OmniCoreTab) -> Unit,
    onDismiss: () -> Unit
) {
    val steps = defaultTutorialSteps()
    val safeIndex = stepIndex.coerceIn(0, steps.lastIndex)
    val current = steps[safeIndex]

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 560.dp)
                .heightIn(max = 620.dp),
            shape = RoundedCornerShape(8.dp),
            color = PanelSurface,
            border = BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        Icon(Icons.AutoMirrored.Outlined.HelpOutline, contentDescription = null, tint = SteelBlue)
                        Column {
                            Text(
                                text = "ACADÉMIE & TUTORIELS OMNICORE",
                                style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Guides complets étape par étape (Modélisation, Rig, Anim KT, Shaders, Mods & Play Store)",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Fermer", tint = TextSecondary)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    steps.forEachIndexed { idx, _ ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (idx <= safeIndex) SteelBlue else ElevatedSurface)
                                .clickable { onSetStep(idx) }
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth()
                        .background(ElevatedSurface, RoundedCornerShape(6.dp))
                        .border(1.dp, BorderHairline, RoundedCornerShape(6.dp))
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = SteelBlueDim,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, SteelBlue)
                        ) {
                            Text(
                                text = current.category.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(color = SteelBlue, fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                        TextButton(
                            onClick = { onJumpToTab(current.targetTab) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Ouvrir l'outil", style = MaterialTheme.typography.labelSmall.copy(color = TerminalEmerald, fontWeight = FontWeight.Bold))
                        }
                    }

                    Text(
                        text = current.title,
                        style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = current.description,
                        style = MaterialTheme.typography.bodySmall.copy(color = SlateSilver, lineHeight = 18.sp)
                    )

                    current.bullets.forEach { bullet ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(VoidBlack, RoundedCornerShape(4.dp))
                                .border(1.dp, BorderHairline, RoundedCornerShape(4.dp))
                                .padding(horizontal = 9.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("•", style = MaterialTheme.typography.labelMedium.copy(color = SteelBlue, fontWeight = FontWeight.Bold))
                            Text(
                                text = bullet,
                                style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary, fontSize = 11.sp, lineHeight = 16.sp)
                            )
                        }
                    }

                    Surface(
                        color = TerminalEmeraldDim.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, TerminalEmerald.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = current.tip,
                            style = MaterialTheme.typography.labelSmall.copy(color = TerminalEmerald, lineHeight = 16.sp),
                            modifier = Modifier.padding(9.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { if (safeIndex > 0) onSetStep(safeIndex - 1) else onDismiss() },
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Text(if (safeIndex > 0) "Précédent" else "Fermer", color = TextSecondary)
                    }

                    Text(
                        text = "${safeIndex + 1} / ${steps.size}",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                    )

                    Button(
                        onClick = { if (safeIndex < steps.lastIndex) onSetStep(safeIndex + 1) else onDismiss() },
                        modifier = Modifier.testTag("tutorial_next_btn"),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SteelBlue, contentColor = VoidBlack)
                    ) {
                        Text(
                            text = if (safeIndex < steps.lastIndex) "Suivant" else "Compris",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CloudProjectsDialog(
    currentUserId: String?,
    activeProjectName: String,
    userEmail: String,
    projects: List<StudioProject>,
    isSaving: Boolean,
    cloudError: String?,
    onProjectNameChange: (String) -> Unit,
    onSaveProject: () -> Unit,
    onLoadProject: (StudioProject) -> Unit,
    onDeleteProject: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    var signInError by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 560.dp)
                .heightIn(max = 620.dp),
            shape = RoundedCornerShape(8.dp),
            color = PanelSurface,
            border = BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
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
                        Icon(Icons.Filled.CloudDone, contentDescription = null, tint = TerminalEmerald)
                        Column {
                            Text(
                                text = "MES PROJETS GOOGLE CLOUD",
                                style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                            )
                            if (userEmail.isNotBlank()) {
                                Text(
                                    text = userEmail,
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                                )
                            }
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Fermer", tint = TextSecondary)
                    }
                }

                if (currentUserId == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ElevatedSurface, RoundedCornerShape(6.dp))
                            .border(1.dp, BorderHairline, RoundedCornerShape(6.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Connectez votre compte Google (optionnel) pour sauvegarder vos scènes dans le Cloud et accéder au Creator Hub.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                        if (signInError != null) {
                            Text(
                                text = signInError ?: "",
                                style = MaterialTheme.typography.labelSmall.copy(color = CrimsonAlert)
                            )
                        }
                        Button(
                            onClick = {
                                signInError = null
                                onGoogleSignInClicked(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onAuthSuccess = {},
                                    onAuthError = { signInError = it },
                                    scope = scope,
                                    onAuthCancelled = { signInError = "Connexion annulée." }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 46.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SteelBlue, contentColor = VoidBlack)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Se connecter avec Google", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = activeProjectName,
                            onValueChange = onProjectNameChange,
                            label = { Text("Nom du projet", color = TextSecondary) },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall.copy(color = TextPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cloud_project_name_input")
                        )
                        Button(
                            onClick = onSaveProject,
                            enabled = !isSaving,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("cloud_save_confirm_btn"),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SteelBlue, contentColor = VoidBlack)
                        ) {
                            Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(if (isSaving) "..." else "Sauver", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (cloudError != null) {
                        Text(
                            text = cloudError,
                            style = MaterialTheme.typography.labelSmall.copy(color = CrimsonAlert)
                        )
                    }

                    HorizontalDivider(color = BorderHairline)

                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (projects.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(VoidBlack, RoundedCornerShape(6.dp))
                                    .border(1.dp, BorderHairline, RoundedCornerShape(6.dp))
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Aucun projet sauvegardé pour l'instant. Touchez « Sauver » pour enregistrer cette scène sur votre compte Google.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            }
                        } else {
                            projects.forEach { proj ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(ElevatedSurface)
                                        .border(1.dp, BorderHairline, RoundedCornerShape(6.dp))
                                        .clickable { onLoadProject(proj) }
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = proj.name,
                                            style = MaterialTheme.typography.labelLarge.copy(color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                        )
                                        Text(
                                            text = "${proj.nodeCount} objets • ${proj.keyframeCount} keyframes • ${proj.geometryMode}",
                                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Ouvrir",
                                            style = MaterialTheme.typography.labelSmall.copy(color = SteelBlue, fontWeight = FontWeight.Bold)
                                        )
                                        IconButton(
                                            onClick = { onDeleteProject(proj.id) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Filled.DeleteOutline, contentDescription = "Supprimer", tint = CrimsonAlert, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
