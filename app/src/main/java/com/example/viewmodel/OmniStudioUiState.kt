package com.example.viewmodel

import com.example.data.repository.StudioProject
import com.example.model.*

data class OmniStudioUiState(
    // Compte Google & Projets Cloud (Firestore — optionnel pour Creator Hub)
    val currentUserId: String? = null,
    val userDisplayName: String = "Créateur",
    val userEmail: String = "",
    val activeProjectName: String = "Scène Studio 01",
    val savedProjects: List<StudioProject> = emptyList(),
    val isSavingCloud: Boolean = false,
    val cloudError: String? = null,
    val showCloudProjectsModal: Boolean = false,

    // Tutoriel & Aide débutant
    val showTutorialModal: Boolean = false,
    val tutorialStepIndex: Int = 0,
    val showQuickTipBar: Boolean = true,

    // Navigation Principale
    val activeTab: OmniCoreTab = OmniCoreTab.MODELISATION,
    val gamesModsSubTab: GamesModsSubTab = GamesModsSubTab.BLOCS_CODE,
    val controlMode: ControlMode = ControlMode.MOBILE_TACTILE,

    // Pont Téléphone-PC & Hybrid Offloading
    val bridgeStatus: BridgeStatus = BridgeStatus(),
    val showBridgeModal: Boolean = false,
    val mobileDrawerDetent: DrawerDetent = DrawerDetent.HALF,
    val showMobileOutlinerSheet: Boolean = false,

    // Viewport 3D, Shaders PolyX3D Pro & Particules Adaptatives
    val activeTool: ViewportTool = ViewportTool.MOVE,
    val selectionSubMode: SelectionSubMode = SelectionSubMode.OBJECT,
    val viewportShading: ViewportShading = ViewportShading.SOLID,
    val activeShader: PolyShaderPreset = PolyShaderPreset.STANDARD_PBR,
    val particleConfig: ParticleConfig = ParticleConfig(),
    val showShadersModal: Boolean = false,
    val activeShape: PrimitiveShape = PrimitiveShape.CUBE,
    val showGrid: Boolean = true,
    val xRayMode: Boolean = false,
    val symmetryEnabled: Boolean = false,
    val physics: PhysicsSimState = PhysicsSimState(),
    val sceneNodes: List<SceneNode> = defaultSceneNodes(),
    val selectedNodeId: String = "mesh_cube",
    val geometryMode: GeometryMode = GeometryMode.SMOOTH_ORGANIQUE,
    val smoothSubdivision: Int = 3,
    val voxelGridResolution: Int = 32,
    val renderEngine: RenderEngine = RenderEngine.POLYX3D_MOBILE,
    val orbitYaw: Float = 32f,
    val orbitPitch: Float = -22f,
    val cameraZoom: Float = 1.0f,

    // Moteurs IA Gratuits 0 Crédit (Meshy 3D, Tripo3D, Blockbench IA, Claude)
    val selectedModelingEngine: ModelingAiEngine = ModelingAiEngine.MESHY_3D,
    val selectedBlockbenchTarget: BlockbenchTargetType = BlockbenchTargetType.ENTITY,
    val aiPromptInput: String = "",
    val aiIsGenerating: Boolean = false,
    val aiStreamingText: String = "",
    val aiErrorMessage: String? = null,
    val aiMessages: List<AiChatMessage> = listOf(
        AiChatMessage(
            id = "welcome_ai",
            isUser = false,
            engineLabel = "Meshy / Tripo / Blockbench / Claude",
            text = "Accès 100% gratuit, 0 crédit et illimité.\n• Meshy 3D & Tripo3D pour vos objets Smooth PBR.\n• Blockbench IA pour blocs & entités Minecraft.\n• Claude pour le Rigging en direct, les animations multi-styles et les mods .mcaddon / .jar."
        )
    ),
    val omniMindPrompt: String = "",
    val omniMindStatus: String? = null,

    // Animation Multi-Styles & Keyframes + Rigging Live
    val timeline: TimelineState = TimelineState(),
    val rigging: RiggingState = RiggingState(),
    val aiAnimPrompt: String = "",
    val keyframeEditOffsetY: Float = 0f,
    val keyframeEditRotY: Float = 0f,
    val keyframeEditScale: Float = 1f,
    val keyframeEditSquashY: Float = 1f,
    val keyframeEditLimbSwing: Float = 0f,

    // Jeux & Mods Illimités
    val blockCatalog: List<LogicBlock> = defaultBlockCatalog(),
    val activeBlockCategory: BlockCategory = BlockCategory.EVENEMENTS,
    val assembledBlocks: List<LogicBlock> = listOf(
        defaultBlockCatalog()[0],
        defaultBlockCatalog()[2],
        defaultBlockCatalog()[4],
        defaultBlockCatalog()[6]
    ),
    val codeViewMode: CodeViewMode = CodeViewMode.GEOMETRY_NODES,
    val modBuild: ModBuildState = ModBuildState(),
    val nativeBuild: NativeBuildTargetState = NativeBuildTargetState(),

    // Import / Export Universel
    val showImportExportModal: Boolean = false,
    val transferDirection: TransferDirection = TransferDirection.IMPORT,
    val selectedFormatExt: String = ".glb",
    val droppedFileName: String? = null,
    val normalizeYUp: Boolean = true,
    val applyTransforms: Boolean = true,
    val lastTransferLog: String? = null,

    // Applications Connectées Gratuites, Méta-IA & SDK
    val aiProviders: List<AiProvider> = defaultAiProviders(),
    val addonPromptInput: String = "",
    val dynamicAddons: List<DynamicAddon> = defaultDynamicAddons(),
    val hotReloadLogs: List<String> = listOf(
        "[LIVE] PolyX3DParticles.kt (6ms)",
        "[LIVE] CartoonSquashIK.kt (7ms)",
        "[LIVE] MetaFluidSolver60FPS.kt (8ms)"
    ),
    val sdkDocs: List<SdkDocEntry> = defaultSdkDocs(),
    val selectedSdkId: String = "sdk_scene",

    // Communauté & Modération Bots
    val hubSearchQuery: String = "",
    val hubSelectedCategory: String = "Tous",
    val communityAssets: List<CommunityAsset> = defaultCommunityAssets(),
    val showPublishModal: Boolean = false,
    val publishTitle: String = "",
    val publishCreditAuthor: String = "",
    val publishAttributionConfirmed: Boolean = false,
    val botChecks: List<BotCheckResult> = emptyList(),
    val hubStatusNotice: String? = null,
    val statusBannerMessage: String? = null
)
