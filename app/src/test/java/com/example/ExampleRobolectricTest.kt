package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.model.AnimationStyle
import com.example.model.BlockbenchTargetType
import com.example.model.ModExportFormat
import com.example.model.ModelingAiEngine
import com.example.model.PolyShaderPreset
import com.example.ui.auth.AuthScreen
import com.example.ui.theme.OmniLinkTheme
import com.example.ui.workspace.OmniStudioScreen
import com.example.viewmodel.OmniStudioViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("OmniCore", appName)
    }

    @Test
    fun `auth screen displays google sign in button`() {
        composeTestRule.setContent {
            OmniLinkTheme {
                AuthScreen(
                    onAuthSuccess = {},
                    onContinueWithoutSignIn = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Accéder au Creator Hub").assertIsDisplayed()
        composeTestRule.onNodeWithTag("google_sign_in_btn").assertIsDisplayed()
        composeTestRule.onNodeWithTag("continue_guest_btn").assertIsDisplayed()
    }

    @Test
    fun `omnicore guest mode allows all studio tools and gates creator hub`() {
        val viewModel = OmniStudioViewModel(
            currentUserId = null,
            userDisplayName = "Créateur",
            userEmail = ""
        )
        composeTestRule.setContent {
            OmniLinkTheme {
                OmniStudioScreen(viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()

        // 1. Modélisation + Shaders PolyX3D Pro + Particules adaptatives sans compte
        composeTestRule.onNodeWithText("OmniCore").assertIsDisplayed()
        composeTestRule.onNodeWithTag("menu_fichier_btn").assertIsDisplayed()
        composeTestRule.onNodeWithTag("menu_ajouter_btn").assertIsDisplayed()
        composeTestRule.onNodeWithTag("menu_affichage_btn").assertIsDisplayed()
        composeTestRule.onNodeWithTag("menu_aide_btn").assertIsDisplayed()
        composeTestRule.onNodeWithTag("viewport_3d_canvas").assertIsDisplayed()

        // Ouvrir le modal Shaders PolyX3D Pro & Particules et sélectionner Toon Cel
        composeTestRule.onNodeWithTag("viewport_shaders_fx_btn").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("shader_preset_TOON_CEL").performClick()
        composeTestRule.waitForIdle()
        assertEquals(PolyShaderPreset.TOON_CEL, viewModel.uiState.value.activeShader)
        composeTestRule.onNodeWithText("SHADERS POLYX3D PRO & PARTICULES").assertIsDisplayed()
        // Fermer le dialog Shaders via le bouton Retour ou close
        viewModel.toggleShadersModal(false)
        composeTestRule.waitForIdle()

        val initialCount = viewModel.uiState.value.sceneNodes.size
        composeTestRule.onNodeWithTag("add_shape_SPHERE").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals(initialCount + 1, viewModel.uiState.value.sceneNodes.size)

        // Ouvrir et fermer les tutoriels détaillés depuis le menu Aide
        composeTestRule.onNodeWithTag("menu_aide_btn").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("tutorial_next_btn").assertIsDisplayed()
        composeTestRule.onNodeWithText("Fermer").performClick()
        composeTestRule.waitForIdle()

        // 2. Animation Multi-Styles (Kevin Temmer Cartoon) + Keyframes + Claude Rigging Live
        composeTestRule.onNodeWithTag("nav_tab_ANIMATION_RIG").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("timeline_play_btn").assertIsDisplayed()
        composeTestRule.onNodeWithTag("timeline_keyframe_btn").assertIsDisplayed()
        composeTestRule.onNodeWithTag("ai_anim_prompt_input").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("anim_style_CARTOON_TEMMER").assertIsDisplayed()
        viewModel.selectAnimationStyle(AnimationStyle.HORROR_STALKER)
        if (viewModel.uiState.value.timeline.isPlaying) {
            viewModel.toggleTimelinePlayback()
        }
        composeTestRule.waitForIdle()
        assertEquals(AnimationStyle.HORROR_STALKER, viewModel.uiState.value.timeline.activeStyle)

        viewModel.scrubFrame(15)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("timeline_keyframe_btn").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertTrue(15 in viewModel.uiState.value.timeline.keyframes)
        composeTestRule.onNodeWithTag("auto_rig_neural_btn").performScrollTo().assertIsDisplayed()

        // 3. Onglet Moteurs IA gratuits (Meshy, Tripo, Blockbench IA, Claude)
        composeTestRule.onNodeWithTag("nav_tab_IA_STUDIO").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("engine_chip_MESHY_3D").assertIsDisplayed()
        composeTestRule.onNodeWithTag("engine_chip_TRIPO_3D").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("engine_chip_BLOCKBENCH_IA").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("bb_target_BLOCK").assertIsDisplayed()
        assertEquals(ModelingAiEngine.BLOCKBENCH_IA, viewModel.uiState.value.selectedModelingEngine)
        viewModel.selectBlockbenchTarget(BlockbenchTargetType.BLOCK)
        viewModel.generate3DObjectWithAi("Bloc Portail Rubis")
        composeTestRule.waitForIdle()
        assertTrue(viewModel.uiState.value.sceneNodes.any { it.name.contains("Bloc", ignoreCase = true) })
        composeTestRule.onNodeWithTag("ai_studio_prompt_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("ai_generate_3d_btn").assertIsDisplayed()
        composeTestRule.onNodeWithTag("ai_ask_chat_btn").assertIsDisplayed()

        // 4. Jeux & Mods (.mcaddon / .jar / .bbmodel + Play Store .AAB gratuit)
        composeTestRule.onNodeWithTag("nav_tab_JEUX_MODS").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("code_mode_JAVA").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("subtab_MINECRAFT_JAR").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("mod_fmt_MCADDON").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("mod_fmt_JAR").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals(ModExportFormat.JAR, viewModel.uiState.value.modBuild.exportFormat)
        composeTestRule.onNodeWithTag("compile_jar_btn").performScrollTo().assertIsDisplayed()

        composeTestRule.onNodeWithTag("subtab_BUILD_NATIF").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("compile_aab_btn").performScrollTo().assertIsDisplayed()

        // 5. Creator Hub demande une connexion Google en mode invité et permet de revenir en Modélisation
        composeTestRule.onNodeWithTag("nav_tab_COMMUNAUTE").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("google_sign_in_btn").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("continue_guest_btn").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("viewport_3d_canvas").assertIsDisplayed()

        // 6. Applications Connectées & Ponts en mode invité
        composeTestRule.onNodeWithTag("nav_tab_PARAMETRES_PONTS").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("settings_sign_in_btn").assertIsDisplayed()
        composeTestRule.onNodeWithTag("addon_prompt_input").performScrollTo().assertIsDisplayed()

        // Import / Export Universel Modal
        composeTestRule.onNodeWithTag("open_import_export_btn").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("pick_device_file_btn").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("drag_drop_zone").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `omnicore signed in user unlocks creator hub and cloud sign out`() {
        val viewModel = OmniStudioViewModel(
            currentUserId = "test_uid",
            userDisplayName = "Test Créateur",
            userEmail = "test@omnicore.dev"
        )
        composeTestRule.setContent {
            OmniLinkTheme {
                OmniStudioScreen(viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()

        // Communauté déverrouillée une fois connecté
        composeTestRule.onNodeWithTag("nav_tab_COMMUNAUTE").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("hub_search_input").performScrollTo().assertIsDisplayed()

        // Paramètres affiche la sauvegarde Cloud et la déconnexion
        composeTestRule.onNodeWithTag("nav_tab_PARAMETRES_PONTS").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("settings_cloud_save_btn").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_sign_out_btn").assertIsDisplayed()
    }
}
