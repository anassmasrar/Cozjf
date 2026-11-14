package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.repository.OmniAiEngine
import com.example.data.repository.ProjectRepository
import com.example.firebase.attemptAutoSignIn
import com.example.firebase.authStateFlow
import com.example.firebase.configureAppCheck
import com.example.firebase.onGoogleSignInClicked
import com.example.firebase.signOutGoogle
import com.example.ui.theme.OmniLinkTheme
import com.example.ui.workspace.OmniStudioScreen
import com.example.viewmodel.OmniStudioViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // App Check DOIT être configuré avant toute initialisation Firestore / AI Logic / ViewModel
        configureAppCheck(this, intent)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        setContent {
            OmniLinkTheme {
                val coroutineScope = rememberCoroutineScope()
                val credentialManager = remember { CredentialManager.create(this@MainActivity) }
                val currentUser by Firebase.auth.authStateFlow()
                    .collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)

                LaunchedEffect(Unit) {
                    attemptAutoSignIn(
                        context = this@MainActivity,
                        credentialManager = credentialManager,
                        onAuthSuccess = {},
                        onUnauthenticated = {},
                        scope = coroutineScope
                    )
                }

                // L'application démarre directement sans connexion obligatoire.
                // La connexion Google déverrouille le Creator Hub (Communauté) et la sauvegarde Cloud.
                val uid = currentUser?.uid
                val scopeKey = uid ?: "guest"
                key(scopeKey) {
                    val studioViewModel: OmniStudioViewModel = viewModel(
                        key = scopeKey,
                        factory = viewModelFactory {
                            initializer {
                                val repo = if (uid != null) {
                                    val dbId = getString(R.string.firestore_database_id)
                                    ProjectRepository(FirebaseFirestore.getInstance(dbId))
                                } else {
                                    null
                                }
                                OmniStudioViewModel(
                                    projectRepository = repo,
                                    aiEngine = OmniAiEngine(),
                                    currentUserId = uid,
                                    userDisplayName = currentUser?.displayName,
                                    userEmail = currentUser?.email
                                )
                            }
                        }
                    )
                    OmniStudioScreen(
                        viewModel = studioViewModel,
                        onSignIn = {
                            onGoogleSignInClicked(
                                context = this@MainActivity,
                                credentialManager = credentialManager,
                                onAuthSuccess = {},
                                onAuthError = {},
                                scope = coroutineScope
                            )
                        },
                        onSignOut = {
                            signOutGoogle(
                                credentialManager = credentialManager,
                                scope = coroutineScope
                            )
                        }
                    )
                }
            }
        }
    }
}
