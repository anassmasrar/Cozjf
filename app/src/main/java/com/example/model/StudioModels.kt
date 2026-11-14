package com.example.model

import androidx.annotation.DrawableRes
import com.example.R

enum class ControlMode(val label: String, val shortLabel: String) {
    MOBILE_TACTILE("Mobile", "Mobile"),
    PC_POWERHOUSE("Station PC", "Station")
}

// 6 Onglets Clairs d'OmniCore (100% Gratuit, Sans Crédits)
enum class OmniCoreTab(val title: String, val shortTitle: String) {
    MODELISATION("Modélisation", "Modéliser"),
    ANIMATION_RIG("Animation & Rig", "Anim"),
    IA_STUDIO("IA & Moteurs", "IA"),
    JEUX_MODS("Jeux & Mods", "Jeux"),
    COMMUNAUTE("Communauté", "Hub"),
    PARAMETRES_PONTS("Apps & Ponts", "Apps")
}

enum class GamesModsSubTab(val label: String) {
    BLOCS_CODE("Blocs & Code"),
    MINECRAFT_JAR("Mods .mcaddon / .jar"),
    BUILD_NATIF("Play Store .AAB / .EXE")
}

enum class ModelingAiEngine(val label: String, val badge: String, val description: String) {
    MESHY_3D("Meshy 3D", "0 CRÉDIT", "Maillages Smooth & textures PBR"),
    TRIPO_3D("Tripo3D", "0 CRÉDIT", "Conversion forme / image -> 3D"),
    BLOCKBENCH_IA("Blockbench IA", "MINECRAFT", "Blocs & entités cubiques .bbmodel"),
    CLAUDE_CODE("Claude", "ILLIMITÉ", "Rigging live, animations & mods")
}

enum class BlockbenchTargetType(val label: String) {
    BLOCK("Bloc 3D"),
    ENTITY("Entité / Mob"),
    ITEM("Objet / Arme")
}

enum class ModExportFormat(val label: String, val ext: String, val edition: String) {
    MCADDON(".mcaddon", ".mcaddon", "Minecraft Bedrock"),
    JAR(".jar", ".jar", "Minecraft Java"),
    BBMODEL(".bbmodel", ".bbmodel", "Blockbench Projet")
}

enum class AnimationStyle(val label: String, val shortLabel: String, val description: String) {
    CARTOON_TEMMER("Cartoon (Kevin Temmer)", "Cartoon KT", "Squash & Stretch exagéré, rebond élastique & anticipation"),
    REALISTIC_MOCAP("Réaliste MoCap", "Réaliste", "Inertie naturelle, gravité & transfert de poids"),
    ANIME_SHONEN("Anime Action", "Anime", "Poses contrastées, anticipation vive & impact sec"),
    MINECRAFT_GECKO("Blockbench / MC", "Minecraft", "Articulations cubiques rythmées GeckoLib"),
    HORROR_STALKER("Horreur Stalker", "Horreur", "Spasmes asymétriques & approche rampante"),
    STOP_MOTION("Stop-Motion 12fps", "Stop-Motion", "Rendu saccadé artisanal image par image")
}

// Shaders inspirés de PolyX3D par Zarrai (version améliorée OmniCore)
enum class PolyShaderPreset(
    val label: String,
    val shortLabel: String,
    val description: String,
    val accentHex: Long
) {
    STANDARD_PBR("Standard PBR+", "PBR+", "Specular métallique PolyX3D, AO et ombre portée", 0xFF4A9EFF),
    TOON_CEL("Toon Cel Pro", "Toon", "3 bandes cel-shading et contour encre net", 0xFFF59E0B),
    MATCAP_CLAY("MatCap Argile", "MatCap", "Terre cuite studio haute lisibilité de sculpture", 0xFFD97757),
    GLASS_CRYSTAL("Glass Cristal", "Glass", "Transparence réfractive et lueur Fresnel", 0xFF38BDF8),
    HOLOGRAM_CYBER("Hologramme", "Holo", "Scanlines cyan animées et projection filaire", 0xFF22D3EE),
    NEON_BLOOM("Néon Bloom", "Néon", "Arêtes émissives à halo lumineux multi-passes", 0xFF34D399),
    VHS_RETRO("VHS / CRT", "VHS", "Lignes cathodiques et décalage chromatique RGB", 0xFFF43F5E),
    LUMEN_RTGI("Lumen RTGI", "Lumen", "Dôme céleste radial, rebond lumineux & reflets", 0xFF93C5FD),
    HORROR_FOG("Horreur Fog", "Horreur", "Vignette profonde, lueur froide & brume épaisse", 0xFFEF4444),
    GOLD_CHROME("Or & Chrome", "Chrome", "Bandes anisotropes miroir haute brillance", 0xFFFBBF24),
    WATERCOLOR_INK("Aquarelle", "Aquarelle", "Lavis doux et trait d'esquisse dessiné à la main", 0xFFA7F3D0),
    THERMAL_WEIGHT("Thermique", "Thermique", "Dégradé thermique des normales & zones d'effort", 0xFFFB7185)
}

enum class ParticlePreset(val label: String, val category: String) {
    AUTO_SHADER("Auto (Adapté au Shader)", "Auto"),
    SPARKS("Étincelles Métal", "Énergie"),
    FIRE_EMBERS("Feu & Braises", "Élément"),
    SMOKE("Fumée Volumétrique", "Atmosphère"),
    SNOW("Neige & Givre", "Météo"),
    RAIN("Pluie & Orage", "Météo"),
    FIREFLIES("Lucioles Magiques", "Fantaisie"),
    HORROR_ASH("Cendres & Spores", "Horreur"),
    SCULPT_DUST("Poussière d'Argile", "Studio"),
    DATA_STREAM("Bits Cyber / Matrix", "Sci-Fi"),
    STARFIELD("Étoiles & Galaxie", "Espace"),
    BUBBLES("Bulles Fluide", "Liquide"),
    PETALS("Pétales & Feuilles", "Nature"),
    SHOCKWAVE("Ondes de Choc", "Action"),
    GLITCH_PIXELS("Glitch Pixels CRT", "Rétro"),
    PRISM_SHARDS("Éclats Prisme", "Cristal"),
    COMIC_SPEEDLINES("Lignes Cartoon", "Cartoon")
}

data class ParticleConfig(
    val enabled: Boolean = true,
    val preset: ParticlePreset = ParticlePreset.AUTO_SHADER,
    val density: Int = 46,
    val speed: Float = 1.0f,
    val size: Float = 1.0f,
    val turbulence: Float = 0.55f
)

enum class GeometryMode(val label: String, val badge: String) {
    SMOOTH_ORGANIQUE("Smooth", "SMOOTH"),
    VOXEL_MODULAIRE("Voxel", "VOXEL")
}

enum class RenderEngine(val label: String, val shortLabel: String, val telemetry: String) {
    AAA_REALISME("AAA Lumen", "AAA", "38 FPS | PBR+"),
    POLYX3D_MOBILE("PolyX3D+", "PolyX3D", "60 FPS | Shaders")
}

enum class ViewportShading(val label: String) {
    WIREFRAME("Filaire"),
    SOLID("Solide"),
    RENDERED("Rendu")
}

enum class SelectionSubMode(val label: String) {
    OBJECT("Objet"),
    VERTEX("Sommets"),
    EDGE("Arêtes"),
    FACE("Faces")
}

enum class ViewportTool(val label: String) {
    SELECT("Sélection"),
    MOVE("Déplacer"),
    ROTATE("Rotation"),
    SCALE("Échelle"),
    SCULPT("Argile"),
    SMOOTH_BRUSH("Lisser"),
    VOXEL_BRUSH("Voxel"),
    WEIGHT_PAINT("Weight")
}

enum class DrawerDetent {
    COLLAPSED,
    HALF,
    EXPANDED
}

enum class PrimitiveShape(val label: String) {
    CUBE("Cube"),
    SPHERE("Sphère"),
    CYLINDRE("Cylindre"),
    CONE("Cône"),
    TORE("Tore"),
    PLAN("Plan"),
    TORSE("Mannequin")
}

data class SubMeshPart(
    val role: String,
    val shape: PrimitiveShape = PrimitiveShape.CUBE,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val offsetZ: Float = 0f,
    val sizeX: Float = 0.4f,
    val sizeY: Float = 0.4f,
    val sizeZ: Float = 0.4f,
    val tintHex: Long = 0xFF5D7A99
)

data class KeyframePose(
    val frame: Int,
    val offsetY: Float = 0f,
    val rotYDeg: Float = 0f,
    val scale: Float = 1f,
    val scaleX: Float = scale,
    val scaleY: Float = scale,
    val scaleZ: Float = scale,
    val rotXDeg: Float = 0f,
    val rotZDeg: Float = 0f,
    val limbSwingDeg: Float = 0f,
    val headBobDeg: Float = 0f
)

data class PhysicsSimState(
    val active: Boolean = false,
    val clothEnabled: Boolean = true,
    val gravityStrength: Float = 9.81f,
    val windForce: Float = 0.25f
)

data class AiProvider(
    val id: String,
    val name: String,
    val role: String,
    val connected: Boolean,
    val isLocalOffline: Boolean,
    val badge: String = "GRATUIT"
)

data class AiChatMessage(
    val id: String,
    val isUser: Boolean,
    val engineLabel: String = "Claude",
    val text: String,
    val isError: Boolean = false,
    val generatedObjectName: String? = null
)

data class AnimationClip(
    val id: String,
    val name: String,
    val frames: Int,
    val fps: Int,
    val style: AnimationStyle = AnimationStyle.CARTOON_TEMMER
)

data class TimelineState(
    val currentFrame: Int = 1,
    val startFrame: Int = 1,
    val endFrame: Int = 120,
    val isPlaying: Boolean = false,
    val selectedClipId: String = "clip_cartoon_kt",
    val activeStyle: AnimationStyle = AnimationStyle.CARTOON_TEMMER,
    val keyframes: Set<Int> = setOf(1, 30, 60, 90, 120),
    val poses: Map<Int, KeyframePose> = mapOf(
        1 to KeyframePose(1, offsetY = 0f, rotYDeg = 0f, scale = 1f, scaleX = 1.18f, scaleY = 0.82f, scaleZ = 1.18f, rotZDeg = -6f, limbSwingDeg = -22f, headBobDeg = -8f),
        30 to KeyframePose(30, offsetY = 0.68f, rotYDeg = 75f, scale = 1f, scaleX = 0.84f, scaleY = 1.24f, scaleZ = 0.84f, rotZDeg = 8f, limbSwingDeg = 36f, headBobDeg = 14f),
        60 to KeyframePose(60, offsetY = 0.04f, rotYDeg = 180f, scale = 1f, scaleX = 1.22f, scaleY = 0.78f, scaleZ = 1.22f, rotZDeg = -5f, limbSwingDeg = -28f, headBobDeg = -10f),
        90 to KeyframePose(90, offsetY = 0.55f, rotYDeg = 275f, scale = 1f, scaleX = 0.88f, scaleY = 1.16f, scaleZ = 0.88f, rotZDeg = 6f, limbSwingDeg = 30f, headBobDeg = 10f),
        120 to KeyframePose(120, offsetY = 0f, rotYDeg = 360f, scale = 1f, scaleX = 1f, scaleY = 1f, scaleZ = 1f, rotZDeg = 0f, limbSwingDeg = 0f, headBobDeg = 0f)
    ),
    val clips: List<AnimationClip> = listOf(
        AnimationClip("clip_cartoon_kt", "Cartoon Kevin Temmer", 120, 24, AnimationStyle.CARTOON_TEMMER),
        AnimationClip("clip_realistic", "Marche Réaliste", 60, 30, AnimationStyle.REALISTIC_MOCAP),
        AnimationClip("clip_anime", "Dash Anime", 48, 24, AnimationStyle.ANIME_SHONEN),
        AnimationClip("clip_horror", "Stalker Horreur", 90, 24, AnimationStyle.HORROR_STALKER)
    )
)

data class BridgeStatus(
    val connected: Boolean = true,
    val hostName: String = "STATION-PC-RTX",
    val encryptionProtocol: String = "AES-256-GCM P2P",
    val latencyMs: Int = 4,
    val bandwidthMbps: Int = 1480,
    val hybridOffloading: Boolean = true
)

data class SceneNode(
    val id: String,
    val name: String,
    val type: String,
    val depth: Int,
    val visible: Boolean = true,
    val locked: Boolean = false,
    val shape: PrimitiveShape = PrimitiveShape.CUBE,
    val geometryMode: GeometryMode = GeometryMode.SMOOTH_ORGANIQUE,
    val posX: Float = 0f,
    val posY: Float = 0f,
    val posZ: Float = 0f,
    val scale: Float = 1f,
    val rotYDeg: Float = 0f,
    val tintHex: Long = 0xFF64748B,
    val sourceEngine: String = "OmniCore",
    val parts: List<SubMeshPart> = emptyList()
)

enum class BlockCategory(val label: String) {
    EVENEMENTS("Événements"),
    CONDITIONS("Conditions"),
    ACTIONS("Actions"),
    FX("FX")
}

data class LogicBlock(
    val id: String,
    val category: BlockCategory,
    val title: String,
    val paramSummary: String,
    val javaSnippet: String,
    val pythonSnippet: String,
    val glslSnippet: String,
    val nodeTitle: String
)

enum class CodeViewMode(val label: String) {
    GEOMETRY_NODES("Nodes"),
    JAVA("Java"),
    PYTHON("Python"),
    GLSL("GLSL")
}

enum class MinecraftLoader(val label: String) {
    FORGE("Forge"),
    FABRIC("Fabric"),
    NEOFORGE("NeoForge")
}

data class ModBuildState(
    val modId: String = "omnicore_stalker",
    val modName: String = "Void Stalker",
    val version: String = "1.4.2",
    val exportFormat: ModExportFormat = ModExportFormat.MCADDON,
    val loader: MinecraftLoader = MinecraftLoader.NEOFORGE,
    val assetEngine: ModelingAiEngine = ModelingAiEngine.BLOCKBENCH_IA,
    val targetType: BlockbenchTargetType = BlockbenchTargetType.ENTITY,
    val aiModPrompt: String = "",
    val smoothGeckoLibExport: Boolean = true,
    val aiAggression: Float = 0.78f,
    val detectionRadius: Float = 24f,
    val volumetricFogDensity: Float = 0.65f,
    val jumpscareIntensity: Float = 0.85f,
    val triggerProximity: Boolean = true,
    val triggerLineOfSight: Boolean = true,
    val triggerDarkness: Boolean = false,
    val triggerSpatialAudio: Boolean = true,
    val isCompiling: Boolean = false,
    val progress: Float = 0f,
    val buildStage: String = "Prêt (Claude Illimité + Blockbench / Meshy)",
    val lastArtifact: String? = null,
    val generatedPreviewFiles: List<String> = listOf(
        "behavior_pack/manifest.json",
        "behavior_pack/entities/void_stalker.json",
        "resource_pack/models/entity/void_stalker.geo.json",
        "resource_pack/animations/void_stalker.animation.json"
    )
)

data class BoneJoint(
    val id: String,
    val name: String,
    val ikChain: String,
    val pitchDeg: Float,
    val yawDeg: Float,
    val rollDeg: Float,
    val ikLocked: Boolean,
    val partRole: String = "torso"
)

data class RiggingState(
    val isScanning: Boolean = false,
    val scanProgress: Float = 1f,
    val revealedBonesCount: Int = 7,
    val rigArchetype: String = "Bipède IK",
    val liveRigStatus: String = "Claude Rigging Live prêt (0 crédit)",
    val detectedBonesCount: Int = 24,
    val symmetryScore: Float = 99.6f,
    val selectedBoneId: String = "hand_ik_l",
    val weightPaintOverlay: Boolean = false,
    val brushWeight: Float = 0.75f,
    val brushFalloff: Float = 0.45f,
    val bones: List<BoneJoint> = defaultBones()
)

enum class TransferDirection(val label: String) {
    IMPORT("Importer"),
    EXPORT("Exporter")
}

data class IndustryFormat(
    val extension: String,
    val label: String,
    val category: String
)

data class NativeBuildTargetState(
    val isBuildingExe: Boolean = false,
    val exeProgress: Float = 0f,
    val exeStatus: String = "Win64 / Linux Vulkan",
    val exeArtifact: String? = null,
    val isBuildingApk: Boolean = false,
    val apkProgress: Float = 0f,
    val apkStatus: String = "Android ARM64 PolyX3D+",
    val apkArtifact: String? = null,
    val isBuildingAab: Boolean = false,
    val aabProgress: Float = 0f,
    val aabStatus: String = "Google Play App Bundle (.AAB) — 0% frais",
    val aabArtifact: String? = null,
    val playPackageId: String = "com.omnicore.creator.mygame",
    val playAppTitle: String = "Mon Jeu OmniCore",
    val playVersionName: String = "1.0.0",
    val precompileShaders: Boolean = true,
    val stripDebugSymbols: Boolean = true
)

data class DynamicAddon(
    val id: String,
    val name: String,
    val promptOrigin: String,
    val version: String,
    val memoryKb: Int,
    val active: Boolean,
    val hotReloadMs: Int
)

data class CommunityAsset(
    val id: String,
    val title: String,
    val author: String,
    val creditLicense: String,
    val category: String,
    val formatTag: String,
    val polyCount: String,
    val verifiedByBots: Boolean,
    @DrawableRes val thumbnailRes: Int? = null
)

data class BotCheckResult(
    val botName: String,
    val status: String,
    val passed: Boolean
)

data class SdkDocEntry(
    val id: String,
    val moduleName: String,
    val summary: String,
    val endpointSignature: String,
    val exampleCode: String
)

data class TutorialStep(
    val category: String,
    val title: String,
    val description: String,
    val bullets: List<String>,
    val tip: String,
    val targetTab: OmniCoreTab = OmniCoreTab.MODELISATION
)

fun defaultSceneNodes(): List<SceneNode> = listOf(
    SceneNode("col_root", "Scène Principale", "Collection", 0),
    SceneNode("cam_main", "Caméra", "Camera", 1),
    SceneNode(
        id = "mesh_cube",
        name = "Personnage_Studio",
        type = "Mesh",
        depth = 1,
        shape = PrimitiveShape.TORSE,
        geometryMode = GeometryMode.SMOOTH_ORGANIQUE,
        posX = 0f,
        posY = 0f,
        posZ = 0f,
        scale = 1f,
        tintHex = 0xFF5D7A99,
        sourceEngine = "Meshy 3D"
    )
)

fun defaultBones(): List<BoneJoint> = listOf(
    BoneJoint("pelvis", "Pelvis_Root", "Center_Mass", 0f, 0f, 0f, true, "torso"),
    BoneJoint("spine_02", "Spine_02", "Torso_Chain", 4f, 0f, 0f, false, "torso"),
    BoneJoint("neck", "Neck_Head", "LookAt_IK", -6f, 8f, 0f, true, "head"),
    BoneJoint("hand_ik_l", "L_Arm_IK", "Left_Arm", 18f, -12f, 8f, true, "arm_l"),
    BoneJoint("hand_ik_r", "R_Arm_IK", "Right_Arm", -14f, 12f, -8f, true, "arm_r"),
    BoneJoint("foot_ik_l", "L_Leg_IK", "Left_Leg", -6f, 0f, 0f, true, "leg_l"),
    BoneJoint("foot_ik_r", "R_Leg_IK", "Right_Leg", 6f, 0f, 0f, true, "leg_r")
)
