package com.example.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.firebase.onGoogleSignInClicked
import com.example.ui.theme.*

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onContinueWithoutSignIn: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VoidBlack)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            color = PanelSurface,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SteelBlueDim)
                        .border(1.dp, SteelBlue, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Public,
                        contentDescription = "Creator Hub",
                        tint = SteelBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "Accéder au Creator Hub",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = "La création 3D, l'animation et l'IA sont accessibles sans compte. Connectez votre compte Google uniquement pour explorer, importer ou publier sur le Creator Hub et sauvegarder dans le Cloud.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        lineHeight = 18.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ElevatedSurface, RoundedCornerShape(6.dp))
                        .border(1.dp, BorderHairline, RoundedCornerShape(6.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = TerminalEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Assets vérifiés par bots • Crédits auteurs respectés • Sauvegarde Cloud",
                        style = MaterialTheme.typography.labelSmall.copy(color = SlateSilver)
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.labelSmall.copy(color = CrimsonAlert),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        onGoogleSignInClicked(
                            context = context,
                            credentialManager = credentialManager,
                            onAuthSuccess = {
                                isLoading = false
                                onAuthSuccess()
                            },
                            onAuthError = { err ->
                                isLoading = false
                                errorMessage = err
                            },
                            scope = coroutineScope,
                            onAuthCancelled = {
                                isLoading = false
                                errorMessage = "Connexion annulée. Sélectionnez un compte Google pour continuer."
                            }
                        )
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("google_sign_in_btn"),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SteelBlue,
                        contentColor = VoidBlack
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = VoidBlack,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Login,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Se connecter avec Google",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                if (onContinueWithoutSignIn != null) {
                    TextButton(
                        onClick = onContinueWithoutSignIn,
                        modifier = Modifier.testTag("continue_guest_btn")
                    ) {
                        Text(
                            text = "Retourner à la Modélisation sans compte",
                            style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary)
                        )
                    }
                }
            }
        }
    }
}
