package com.example.ui.workspace

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.*
import com.example.ui.auth.AuthScreen
import com.example.ui.components.BridgeTelemetryDialog
import com.example.ui.components.HeaderBridgeBar
import com.example.ui.components.Viewport3DCanvas
import com.example.ui.modals.CloudProjectsDialog
import com.example.ui.modals.ImportExportModal
import com.example.ui.modals.TutorialDialog
import com.example.ui.panels.*
import com.example.ui.theme.*
import com.example.viewmodel.OmniStudioUiState
import com.example.viewmodel.OmniStudioViewModel

@Composable
fun OmniStudioScreen(
    viewModel: OmniStudioViewModel,
    onSignIn: () -> Unit = {},
    onSignOut: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = VoidBlack,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(VoidBlack)
        ) {
            val availableWidth = maxWidth
            val isLandscape = maxWidth > maxHeight
            val isWorkstationLayout = isLandscape || uiState.controlMode == ControlMode.PC_POWERHOUSE

            Column(modifier = Modifier.fillMaxSize()) {
                HeaderBridgeBar(
                    bridgeStatus = uiState.bridgeStatus,
                    controlMode = uiState.controlMode,
                    activeTab = uiState.activeTab,
                    isWorkstationLayout = isWorkstationLayout,
                    showGrid = uiState.showGrid,
                    xRayMode = uiState.xRayMode,
                    symmetryEnabled = uiState.symmetryEnabled,
                    viewportShading = uiState.viewportShading,
                    isSavingCloud = uiState.isSavingCloud,
                    userDisplayName = uiState.userDisplayName,
                    onToggleControlMode = viewModel::setControlMode,
                    onSelectTab = viewModel::selectTab,
                    onOpenBridgeDialog = { viewModel.toggleBridgeDialog(true) },
                    onOpenImportExport = { viewModel.toggleImportExportModal(true) },
                    onNewProject = viewModel::newEmptyProject,
                    onSaveToCloud = { viewModel.saveCurrentProjectToCloud() },
                    onOpenCloudProjects = { viewModel.toggleCloudProjectsModal(true) },
                    onAddShape = { viewModel.addPrimitiveShapeToScene(it) },
                    onAddUtilityNode = viewModel::addSceneUtilityNode,
                    onSetShading = viewModel::setViewportShading,
                    onToggleGrid = viewModel::toggleGrid,
                    onToggleXRay = viewModel::toggleXRay,
                    onToggleSymmetry = viewModel::toggleSymmetry,
                    onSnapCamera = viewModel::snapCameraView,
                    onResetCamera = viewModel::resetCamera,
                    onOpenOutliner = { viewModel.toggleMobileOutlinerSheet(true) },
                    onOpenTutorial = { viewModel.openTutorial(0) }
                )

                // Bandeau d'aide débutant compact et fermable sur les écrans 3D
                if (uiState.showQuickTipBar && (uiState.activeTab == OmniCoreTab.MODELISATION || uiState.activeTab == OmniCoreTab.ANIMATION_RIG)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ElevatedSurface)
                            .border(1.dp, BorderHairline)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Astuce : Glissez pour tourner • Menu « Ajouter » ou boutons ci-dessous pour créer des formes",
                            style = MaterialTheme.typography.labelSmall.copy(color = SlateSilver, fontSize = 10.sp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Tuto",
                                style = MaterialTheme.typography.labelSmall.copy(color = SteelBlue, fontWeight = FontWeight.Bold),
                                modifier = Modifier
                                    .testTag("quick_tip_tuto_btn")
                                    .clip(RoundedCornerShape(3.dp))
                                    .clickable { viewModel.openTutorial(0) }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                            IconButton(onClick = viewModel::dismissQuickTipBar, modifier = Modifier.size(22.dp)) {
                                Icon(Icons.Filled.Close, contentDescription = "Masquer astuce", tint = TextMuted, modifier = Modifier.size(13.dp))
                            }
                        }
                    }
                }

                if (uiState.statusBannerMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TerminalEmeraldDim.copy(alpha = 0.4f))
                            .border(1.dp, TerminalEmerald.copy(alpha = 0.4f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.statusBannerMessage ?: "",
                            style = MaterialTheme.typography.labelSmall.copy(color = TerminalEmerald),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = viewModel::dismissBanner, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Filled.Close, contentDescription = "Fermer", tint = TextSecondary, modifier = Modifier.size(13.dp))
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (isWorkstationLayout) {
                        WorkstationBody(
                            uiState = uiState,
                            isCompactWidth = availableWidth < 640.dp,
                            viewModel = viewModel,
                            onSignIn = onSignIn,
                            onSignOut = onSignOut
                        )
                    } else {
                        MobilePortraitBody(
                            uiState = uiState,
                            viewModel = viewModel,
                            onSignIn = onSignIn,
                            onSignOut = onSignOut
                        )
                    }
                }

                if (!isWorkstationLayout) {
                    OmniCoreBottomNavBar(
                        activeTab = uiState.activeTab,
                        onSelectTab = viewModel::selectTab
                    )
                }
            }

            if (uiState.showBridgeModal) {
                BridgeTelemetryDialog(
                    bridgeStatus = uiState.bridgeStatus,
                    onToggleConnection = viewModel::toggleBridgeConnection,
                    onDismiss = { viewModel.toggleBridgeDialog(false) }
                )
            }

            if (uiState.showImportExportModal) {
                ImportExportModal(
                    formats = defaultIndustryFormats(),
                    direction = uiState.transferDirection,
                    selectedFormatExt = uiState.selectedFormatExt,
                    droppedFileName = uiState.droppedFileName,
                    normalizeYUp = uiState.normalizeYUp,
                    applyTransforms = uiState.applyTransforms,
                    lastTransferLog = uiState.lastTransferLog,
                    onSetDirection = viewModel::setTransferDirection,
                    onSelectFormat = viewModel::selectTransferFormat,
                    onSimulateDropFile = viewModel::simulateDropFile,
                    onToggleOptions = viewModel::toggleTransferOptions,
                    onExecuteTransfer = viewModel::executeUniversalTransfer,
                    onDismiss = { viewModel.toggleImportExportModal(false) }
                )
            }

            if (uiState.showPublishModal) {
                PublishAssetModal(
                    title = uiState.publishTitle,
                    creditAuthor = uiState.publishCreditAuthor,
                    attributionConfirmed = uiState.publishAttributionConfirmed,
                    botChecks = uiState.botChecks,
                    onFormChange = viewModel::updatePublishForm,
                    onRunModerationAndPublish = viewModel::runBotModerationAndPublish,
                    onDismiss = { viewModel.togglePublishModal(false) }
                )
            }

            if (uiState.showMobileOutlinerSheet) {
                MobileOutlinerDialog(
                    nodes = uiState.sceneNodes,
                    selectedNodeId = uiState.selectedNodeId,
                    geometryMode = uiState.geometryMode,
                    subdivision = uiState.smoothSubdivision,
                    voxelRes = uiState.voxelGridResolution,
                    onSelectNode = viewModel::selectSceneNode,
                    onToggleVisibility = viewModel::toggleNodeVisibility,
                    onToggleLock = viewModel::toggleNodeLock,
                    onSubdivChange = viewModel::setSmoothSubdivision,
                    onVoxelResChange = viewModel::setVoxelGridResolution,
                    onDismiss = { viewModel.toggleMobileOutlinerSheet(false) }
                )
            }

            if (uiState.showTutorialModal) {
                TutorialDialog(
                    stepIndex = uiState.tutorialStepIndex,
                    onSetStep = viewModel::setTutorialStep,
                    onJumpToTab = { tab ->
                        viewModel.closeTutorial()
                        viewModel.selectTab(tab)
                    },
                    onDismiss = viewModel::closeTutorial
                )
            }

            if (uiState.showShadersModal) {
                ShadersAndParticlesDialog(
                    activeShader = uiState.activeShader,
                    particleConfig = uiState.particleConfig,
                    onSelectShader = viewModel::selectPolyShader,
                    onToggleParticles = viewModel::toggleParticles,
                    onSelectParticlePreset = viewModel::selectParticlePreset,
                    onUpdateParticleParams = viewModel::updateParticleParams,
                    onDismiss = { viewModel.toggleShadersModal(false) }
                )
            }

            if (uiState.showCloudProjectsModal) {
                CloudProjectsDialog(
                    currentUserId = uiState.currentUserId,
                    activeProjectName = uiState.activeProjectName,
                    userEmail = uiState.userEmail,
                    projects = uiState.savedProjects,
                    isSaving = uiState.isSavingCloud,
                    cloudError = uiState.cloudError,
                    onProjectNameChange = viewModel::updateActiveProjectName,
                    onSaveProject = { viewModel.saveCurrentProjectToCloud() },
                    onLoadProject = viewModel::loadCloudProject,
                    onDeleteProject = viewModel::deleteCloudProject,
                    onDismiss = { viewModel.toggleCloudProjectsModal(false) }
                )
            }
        }
    }
}

@Composable
private fun MobilePortraitBody(
    uiState: OmniStudioUiState,
    viewModel: OmniStudioViewModel,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit
) {
    when (uiState.activeTab) {
        OmniCoreTab.MODELISATION, OmniCoreTab.ANIMATION_RIG -> {
            val effectiveRig = if (uiState.activeTab == OmniCoreTab.ANIMATION_RIG) {
                uiState.rigging.copy(weightPaintOverlay = true)
            } else {
                uiState.rigging
            }

            val drawerWeight by animateFloatAsState(
                targetValue = when (uiState.mobileDrawerDetent) {
                    DrawerDetent.COLLAPSED -> 0.09f
                    DrawerDetent.HALF -> 0.44f
                    DrawerDetent.EXPANDED -> 0.68f
                },
                animationSpec = spring(),
                label = "portraitDrawer"
            )

            Column(modifier = Modifier.fillMaxSize()) {
                Viewport3DCanvas(
                    sceneNodes = uiState.sceneNodes,
                    selectedNodeId = uiState.selectedNodeId,
                    geometryMode = uiState.geometryMode,
                    renderEngine = uiState.renderEngine,
                    viewportShading = uiState.viewportShading,
                    activeShader = uiState.activeShader,
                    particleConfig = uiState.particleConfig,
                    activeTool = uiState.activeTool,
                    selectionSubMode = uiState.selectionSubMode,
                    riggingState = effectiveRig,
                    timelineState = uiState.timeline,
                    showGrid = uiState.showGrid,
                    xRayMode = uiState.xRayMode,
                    symmetryEnabled = uiState.symmetryEnabled,
                    orbitYaw = uiState.orbitYaw,
                    orbitPitch = uiState.orbitPitch,
                    cameraZoom = uiState.cameraZoom,
                    showSideToolbars = true,
                    onOrbitDrag = viewModel::updateCameraOrbit,
                    onSnapCamera = viewModel::snapCameraView,
                    onZoomDelta = viewModel::adjustCameraZoom,
                    onResetCamera = viewModel::resetCamera,
                    onGeometryModeChange = viewModel::setGeometryMode,
                    onRenderEngineChange = viewModel::setRenderEngine,
                    onShadingChange = viewModel::setViewportShading,
                    onOpenShadersModal = { viewModel.toggleShadersModal(true) },
                    onSelectTool = viewModel::selectTool,
                    onSelectSubMode = viewModel::setSelectionSubMode,
                    onToggleGrid = viewModel::toggleGrid,
                    onToggleXRay = viewModel::toggleXRay,
                    onToggleSymmetry = viewModel::toggleSymmetry,
                    onToggleWeightPaint = viewModel::toggleWeightPaintOverlay,
                    onOpenMobileOutliner = { viewModel.toggleMobileOutlinerSheet(true) },
                    onQuickAddCube = { viewModel.addPrimitiveShapeToScene(PrimitiveShape.CUBE) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f - drawerWeight)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(drawerWeight)
                        .background(PanelSurface)
                        .border(1.dp, BorderHairline, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                ) {
                    DrawerHandleHeader(
                        label = uiState.activeTab.title.uppercase(),
                        detent = uiState.mobileDrawerDetent,
                        onCycleDetent = viewModel::cycleDrawerDetent,
                        onDragDelta = { dy ->
                            if (dy < -20f) viewModel.setDrawerDetent(DrawerDetent.EXPANDED)
                            else if (dy > 20f) viewModel.setDrawerDetent(DrawerDetent.COLLAPSED)
                        }
                    )
                    if (uiState.mobileDrawerDetent != DrawerDetent.COLLAPSED) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            if (uiState.activeTab == OmniCoreTab.MODELISATION) {
                                ModelingTrayPanel(
                                    uiState = uiState,
                                    onAddShape = { viewModel.addPrimitiveShapeToScene(it) },
                                    onDeleteSelected = viewModel::deleteSelectedNode,
                                    onOpenImportExport = { viewModel.toggleImportExportModal(true) },
                                    onOpenShadersModal = { viewModel.toggleShadersModal(true) },
                                    onSelectEngine = viewModel::selectModelingEngine,
                                    onSelectBlockbenchTarget = viewModel::selectBlockbenchTarget,
                                    onOpenAiTab = { viewModel.selectTab(OmniCoreTab.IA_STUDIO) },
                                    onSetGeometryMode = viewModel::setGeometryMode,
                                    onSubdivChange = viewModel::setSmoothSubdivision,
                                    onVoxelResChange = viewModel::setVoxelGridResolution,
                                    onTogglePhysics = viewModel::togglePhysicsSimulation,
                                    onPromptChange = viewModel::updateOmniMindPrompt,
                                    onRunOmniMind = viewModel::executeOmniMindGeneration
                                )
                            } else {
                                AnimationAndRiggingTrayPanel(uiState = uiState, viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }

        OmniCoreTab.IA_STUDIO -> AiAssistantPanel(
            uiState = uiState,
            onSelectEngine = viewModel::selectModelingEngine,
            onSelectBlockbenchTarget = viewModel::selectBlockbenchTarget,
            onPromptChange = viewModel::updateAiPromptInput,
            onSendChat = viewModel::askClaudeAssistant,
            onGenerate3D = viewModel::generate3DObjectWithAi
        )

        OmniCoreTab.JEUX_MODS -> GamesAndModsHubPanel(
            uiState = uiState,
            isWidePane = false,
            viewModel = viewModel
        )

        OmniCoreTab.COMMUNAUTE -> {
            if (uiState.currentUserId == null) {
                AuthScreen(
                    onAuthSuccess = {},
                    onContinueWithoutSignIn = { viewModel.selectTab(OmniCoreTab.MODELISATION) }
                )
            } else {
                CreatorHubPanel(
                    searchQuery = uiState.hubSearchQuery,
                    selectedCategory = uiState.hubSelectedCategory,
                    assets = uiState.communityAssets,
                    statusNotice = uiState.hubStatusNotice,
                    onSearchChange = viewModel::updateHubSearch,
                    onSelectCategory = viewModel::selectHubCategory,
                    onImportAsset = viewModel::importCommunityAsset,
                    onOpenPublishModal = { viewModel.togglePublishModal(true) }
                )
            }
        }

        OmniCoreTab.PARAMETRES_PONTS -> SettingsAndBridgesPanel(
            uiState = uiState,
            viewModel = viewModel,
            onSignIn = onSignIn,
            onSignOut = onSignOut
        )
    }
}

@Composable
private fun WorkstationBody(
    uiState: OmniStudioUiState,
    isCompactWidth: Boolean,
    viewModel: OmniStudioViewModel,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit
) {
    when (uiState.activeTab) {
        OmniCoreTab.MODELISATION, OmniCoreTab.ANIMATION_RIG -> {
            val effectiveRig = if (uiState.activeTab == OmniCoreTab.ANIMATION_RIG) {
                uiState.rigging.copy(weightPaintOverlay = true)
            } else {
                uiState.rigging
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (isCompactWidth) Modifier.horizontalScroll(rememberScrollState()) else Modifier)
            ) {
                val workspaceMod = if (isCompactWidth) Modifier.width(920.dp) else Modifier.fillMaxWidth()

                Row(modifier = workspaceMod.fillMaxHeight()) {
                    VerticalToolShelf(
                        activeTool = uiState.activeTool,
                        onSelectTool = viewModel::selectTool
                    )

                    Column(modifier = Modifier.weight(0.67f).fillMaxHeight()) {
                        Viewport3DCanvas(
                            sceneNodes = uiState.sceneNodes,
                            selectedNodeId = uiState.selectedNodeId,
                            geometryMode = uiState.geometryMode,
                            renderEngine = uiState.renderEngine,
                            viewportShading = uiState.viewportShading,
                            activeShader = uiState.activeShader,
                            particleConfig = uiState.particleConfig,
                            activeTool = uiState.activeTool,
                            selectionSubMode = uiState.selectionSubMode,
                            riggingState = effectiveRig,
                            timelineState = uiState.timeline,
                            showGrid = uiState.showGrid,
                            xRayMode = uiState.xRayMode,
                            symmetryEnabled = uiState.symmetryEnabled,
                            orbitYaw = uiState.orbitYaw,
                            orbitPitch = uiState.orbitPitch,
                            cameraZoom = uiState.cameraZoom,
                            showSideToolbars = false,
                            onOrbitDrag = viewModel::updateCameraOrbit,
                            onSnapCamera = viewModel::snapCameraView,
                            onZoomDelta = viewModel::adjustCameraZoom,
                            onResetCamera = viewModel::resetCamera,
                            onGeometryModeChange = viewModel::setGeometryMode,
                            onRenderEngineChange = viewModel::setRenderEngine,
                            onShadingChange = viewModel::setViewportShading,
                            onOpenShadersModal = { viewModel.toggleShadersModal(true) },
                            onSelectTool = viewModel::selectTool,
                            onSelectSubMode = viewModel::setSelectionSubMode,
                            onToggleGrid = viewModel::toggleGrid,
                            onToggleXRay = viewModel::toggleXRay,
                            onToggleSymmetry = viewModel::toggleSymmetry,
                            onToggleWeightPaint = viewModel::toggleWeightPaintOverlay,
                            onOpenMobileOutliner = {},
                            onQuickAddCube = { viewModel.addPrimitiveShapeToScene(PrimitiveShape.CUBE) },
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        )

                        HorizontalDivider(color = BorderHairline)

                        TimelinePanel(
                            timeline = uiState.timeline,
                            compactMode = true,
                            onTogglePlay = viewModel::toggleTimelinePlayback,
                            onScrubFrame = viewModel::scrubFrame,
                            onToggleKeyframe = viewModel::toggleKeyframeAtCurrent,
                            onSelectClip = viewModel::selectAnimationClip,
                            modifier = Modifier.height(84.dp)
                        )
                    }

                    VerticalDivider(color = BorderHairline)

                    Column(
                        modifier = Modifier
                            .weight(0.33f)
                            .fillMaxHeight()
                            .background(PanelSurface)
                    ) {
                        OutlinerTreePane(
                            nodes = uiState.sceneNodes,
                            selectedNodeId = uiState.selectedNodeId,
                            geometryMode = uiState.geometryMode,
                            subdivision = uiState.smoothSubdivision,
                            voxelRes = uiState.voxelGridResolution,
                            onSelectNode = viewModel::selectSceneNode,
                            onToggleVisibility = viewModel::toggleNodeVisibility,
                            onToggleLock = viewModel::toggleNodeLock,
                            onSubdivChange = viewModel::setSmoothSubdivision,
                            onVoxelResChange = viewModel::setVoxelGridResolution,
                            modifier = Modifier.fillMaxWidth().weight(0.38f)
                        )

                        HorizontalDivider(color = BorderHairline)

                        Box(modifier = Modifier.fillMaxWidth().weight(0.62f)) {
                            if (uiState.activeTab == OmniCoreTab.MODELISATION) {
                                ModelingTrayPanel(
                                    uiState = uiState,
                                    onAddShape = { viewModel.addPrimitiveShapeToScene(it) },
                                    onDeleteSelected = viewModel::deleteSelectedNode,
                                    onOpenImportExport = { viewModel.toggleImportExportModal(true) },
                                    onOpenShadersModal = { viewModel.toggleShadersModal(true) },
                                    onSelectEngine = viewModel::selectModelingEngine,
                                    onSelectBlockbenchTarget = viewModel::selectBlockbenchTarget,
                                    onOpenAiTab = { viewModel.selectTab(OmniCoreTab.IA_STUDIO) },
                                    onSetGeometryMode = viewModel::setGeometryMode,
                                    onSubdivChange = viewModel::setSmoothSubdivision,
                                    onVoxelResChange = viewModel::setVoxelGridResolution,
                                    onTogglePhysics = viewModel::togglePhysicsSimulation,
                                    onPromptChange = viewModel::updateOmniMindPrompt,
                                    onRunOmniMind = viewModel::executeOmniMindGeneration
                                )
                            } else {
                                AnimationAndRiggingTrayPanel(uiState = uiState, viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }

        OmniCoreTab.IA_STUDIO -> AiAssistantPanel(
            uiState = uiState,
            onSelectEngine = viewModel::selectModelingEngine,
            onSelectBlockbenchTarget = viewModel::selectBlockbenchTarget,
            onPromptChange = viewModel::updateAiPromptInput,
            onSendChat = viewModel::askClaudeAssistant,
            onGenerate3D = viewModel::generate3DObjectWithAi
        )

        OmniCoreTab.JEUX_MODS -> GamesAndModsHubPanel(
            uiState = uiState,
            isWidePane = true,
            viewModel = viewModel
        )

        OmniCoreTab.COMMUNAUTE -> {
            if (uiState.currentUserId == null) {
                AuthScreen(
                    onAuthSuccess = {},
                    onContinueWithoutSignIn = { viewModel.selectTab(OmniCoreTab.MODELISATION) }
                )
            } else {
                CreatorHubPanel(
                    searchQuery = uiState.hubSearchQuery,
                    selectedCategory = uiState.hubSelectedCategory,
                    assets = uiState.communityAssets,
                    statusNotice = uiState.hubStatusNotice,
                    onSearchChange = viewModel::updateHubSearch,
                    onSelectCategory = viewModel::selectHubCategory,
                    onImportAsset = viewModel::importCommunityAsset,
                    onOpenPublishModal = { viewModel.togglePublishModal(true) }
                )
            }
        }

        OmniCoreTab.PARAMETRES_PONTS -> SettingsAndBridgesPanel(
            uiState = uiState,
            viewModel = viewModel,
            onSignIn = onSignIn,
            onSignOut = onSignOut
        )
    }
}

@Composable
private fun OmniCoreBottomNavBar(
    activeTab: OmniCoreTab,
    onSelectTab: (OmniCoreTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PanelSurface)
            .border(1.dp, BorderHairline)
            .padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OmniCoreTab.entries.forEach { tab ->
            val selected = tab == activeTab
            val icon = when (tab) {
                OmniCoreTab.MODELISATION -> Icons.Filled.ViewInAr
                OmniCoreTab.ANIMATION_RIG -> Icons.Filled.AccessibilityNew
                OmniCoreTab.IA_STUDIO -> Icons.Filled.AutoAwesome
                OmniCoreTab.JEUX_MODS -> Icons.Filled.SportsEsports
                OmniCoreTab.COMMUNAUTE -> Icons.Outlined.Public
                OmniCoreTab.PARAMETRES_PONTS -> Icons.Outlined.Tune
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .testTag("nav_tab_${tab.name}")
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (selected) SteelBlueDim.copy(alpha = 0.7f) else Color.Transparent)
                    .clickable { onSelectTab(tab) }
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = tab.title,
                    tint = if (selected) SteelBlue else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = tab.shortTitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (selected) TextPrimary else TextSecondary,
                        fontSize = 9.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DrawerHandleHeader(
    label: String,
    detent: DrawerDetent,
    onCycleDetent: () -> Unit,
    onDragDelta: (Float) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ElevatedSurface)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDragDelta(dragAmount.y)
                }
            }
            .clickable(onClick = onCycleDetent)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("mobile_drawer_handle"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = SteelBlue, fontSize = 10.sp)
        )
        Box(
            modifier = Modifier
                .width(34.dp)
                .height(3.5.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(BorderSubtle)
        )
        Icon(
            imageVector = if (detent == DrawerDetent.EXPANDED) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
            contentDescription = "Basculer tiroir",
            tint = TextSecondary,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun VerticalToolShelf(
    activeTool: ViewportTool,
    onSelectTool: (ViewportTool) -> Unit
) {
    Column(
        modifier = Modifier
            .width(44.dp)
            .fillMaxHeight()
            .background(PanelSurface)
            .border(1.dp, BorderHairline)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ViewportTool.entries.forEach { tool ->
            val selected = tool == activeTool
            val icon: ImageVector = when (tool) {
                ViewportTool.SELECT -> Icons.Outlined.NearMe
                ViewportTool.MOVE -> Icons.Filled.OpenWith
                ViewportTool.ROTATE -> Icons.Filled.RotateRight
                ViewportTool.SCALE -> Icons.Filled.AspectRatio
                ViewportTool.SCULPT -> Icons.Filled.AutoFixHigh
                ViewportTool.SMOOTH_BRUSH -> Icons.Filled.BlurOn
                ViewportTool.VOXEL_BRUSH -> Icons.Filled.ViewInAr
                ViewportTool.WEIGHT_PAINT -> Icons.Filled.Brush
            }
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (selected) SteelBlueDim else Color.Transparent)
                    .border(1.dp, if (selected) SteelBlue else Color.Transparent, RoundedCornerShape(4.dp))
                    .clickable { onSelectTool(tool) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = tool.label,
                    tint = if (selected) SteelBlue else SlateSilver,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun OutlinerTreePane(
    nodes: List<SceneNode>,
    selectedNodeId: String,
    geometryMode: GeometryMode,
    subdivision: Int,
    voxelRes: Int,
    onSelectNode: (String) -> Unit,
    onToggleVisibility: (String) -> Unit,
    onToggleLock: (String) -> Unit,
    onSubdivChange: (Int) -> Unit,
    onVoxelResChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(PanelSurface)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("OUTLINER", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
            Text(
                text = if (geometryMode == GeometryMode.SMOOTH_ORGANIQUE) "Subdiv $subdivision" else "${voxelRes}^3",
                style = MaterialTheme.typography.labelSmall.copy(color = SteelBlue)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            nodes.forEach { node ->
                val selected = node.id == selectedNodeId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (selected) SteelBlueDim else ElevatedSurface)
                        .clickable { onSelectNode(node.id) }
                        .padding(start = (6 + node.depth * 10).dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = node.name,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (selected) TextPrimary else TextSecondary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Row {
                        IconButton(onClick = { onToggleVisibility(node.id) }, modifier = Modifier.size(22.dp)) {
                            Icon(
                                imageVector = if (node.visible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = "Visibilité",
                                tint = if (node.visible) TextSecondary else TextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        IconButton(onClick = { onToggleLock(node.id) }, modifier = Modifier.size(22.dp)) {
                            Icon(
                                imageVector = if (node.locked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                                contentDescription = "Verrouiller",
                                tint = if (node.locked) SteelBlue else TextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MobileOutlinerDialog(
    nodes: List<SceneNode>,
    selectedNodeId: String,
    geometryMode: GeometryMode,
    subdivision: Int,
    voxelRes: Int,
    onSelectNode: (String) -> Unit,
    onToggleVisibility: (String) -> Unit,
    onToggleLock: (String) -> Unit,
    onSubdivChange: (Int) -> Unit,
    onVoxelResChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = PanelSurface,
            border = BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(modifier = Modifier.fillMaxWidth().height(340.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ElevatedSurface)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("SCÈNE", style = MaterialTheme.typography.labelLarge.copy(color = TextPrimary))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Fermer", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
                OutlinerTreePane(
                    nodes = nodes,
                    selectedNodeId = selectedNodeId,
                    geometryMode = geometryMode,
                    subdivision = subdivision,
                    voxelRes = voxelRes,
                    onSelectNode = onSelectNode,
                    onToggleVisibility = onToggleVisibility,
                    onToggleLock = onToggleLock,
                    onSubdivChange = onSubdivChange,
                    onVoxelResChange = onVoxelResChange,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
