package com.example.data.repository

import com.example.model.*
import java.util.UUID
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class GeneratedModelResult(
    val node: SceneNode,
    val explanation: String,
    val suggestedScript: String
)

data class ClaudeRigPlan(
    val archetype: String,
    val bones: List<BoneJoint>,
    val note: String
)

data class ClaudeAnimationPlan(
    val clip: AnimationClip,
    val poses: Map<Int, KeyframePose>,
    val summary: String
)

data class GeneratedModBundle(
    val modId: String,
    val modName: String,
    val files: Map<String, String>,
    val summary: String
)

class OmniAiEngine {

    fun generateModel3D(
        prompt: String,
        engine: ModelingAiEngine,
        targetType: BlockbenchTargetType = BlockbenchTargetType.ENTITY
    ): GeneratedModelResult {
        val lower = prompt.lowercase()
        val isBlockbench = engine == ModelingAiEngine.BLOCKBENCH_IA
        val cleanTitle = prompt.trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .take(3)
            .joinToString("_") { w ->
                w.filter { it.isLetterOrDigit() }.replaceFirstChar { it.uppercase() }
            }
            .ifBlank { "Objet_${engine.label.replace(" ", "")}" }

        val parts = when {
            isBlockbench && targetType == BlockbenchTargetType.BLOCK -> buildBlockbenchBlockParts(lower)
            lower.contains("dragon") || lower.contains("wyvern") || lower.contains("ail") -> buildDragonParts(isBlockbench)
            lower.contains("épée") || lower.contains("epee") || lower.contains("sword") || lower.contains("lame") -> buildSwordParts(isBlockbench)
            lower.contains("voiture") || lower.contains("car") || lower.contains("moto") || lower.contains("vaisseau") -> buildVehicleParts(isBlockbench)
            lower.contains("arbre") || lower.contains("tree") || lower.contains("forêt") -> buildTreeParts(isBlockbench)
            lower.contains("maison") || lower.contains("château") || lower.contains("chateau") || lower.contains("tour") -> buildArchitectureParts(isBlockbench)
            lower.contains("loup") || lower.contains("chien") || lower.contains("chat") || lower.contains("cheval") || lower.contains("quad") -> buildQuadrupedParts(isBlockbench)
            lower.contains("stalker") || lower.contains("monstre") || lower.contains("horreur") || lower.contains("zombie") || lower.contains("creeper") -> buildMonsterParts(isBlockbench)
            else -> buildCharacterOrMechParts(lower, isBlockbench)
        }

        val geoMode = if (isBlockbench) GeometryMode.VOXEL_MODULAIRE else GeometryMode.SMOOTH_ORGANIQUE
        val baseShape = if (isBlockbench) PrimitiveShape.CUBE else when {
            lower.contains("sph") || lower.contains("planète") -> PrimitiveShape.SPHERE
            lower.contains("dragon") || lower.contains("perso") || lower.contains("mech") || lower.contains("monstre") -> PrimitiveShape.TORSE
            else -> PrimitiveShape.CUBE
        }

        val node = SceneNode(
            id = "ai_${engine.name.lowercase()}_${System.nanoTime()}",
            name = cleanTitle,
            type = "Mesh",
            depth = 1,
            shape = baseShape,
            geometryMode = geoMode,
            scale = 1f,
            tintHex = parts.firstOrNull()?.tintHex ?: 0xFF4A9EFF,
            sourceEngine = if (isBlockbench) "Blockbench (${targetType.label})" else engine.label,
            parts = parts
        )

        val engineNote = when (engine) {
            ModelingAiEngine.MESHY_3D -> "Généré par Meshy 3D (0 crédit) — ${parts.size} sous-maillages Smooth PBR assemblés."
            ModelingAiEngine.TRIPO_3D -> "Généré par Tripo3D (0 crédit) — Silhouette multi-volumes reconstruite (${parts.size} pièces)."
            ModelingAiEngine.BLOCKBENCH_IA -> "Généré par Blockbench IA (${targetType.label}) — ${parts.size} cubes prêts pour .mcaddon / .bbmodel."
            ModelingAiEngine.CLAUDE_CODE -> "Généré par Claude 3D (illimité) — ${parts.size} composants structurés et riggables."
        }

        val script = if (isBlockbench) {
            "// Blockbench IA (${targetType.label})\n" +
                "\"format_version\": \"1.20.0\",\n" +
                "\"identifier\": \"omnicore:${cleanTitle.lowercase()}\",\n" +
                "\"cubes\": ${parts.size}"
        } else {
            "// ${engine.label} (0 crédit) -> SceneNode\n" +
                "scene.insertMesh(name = \"$cleanTitle\", parts = ${parts.size}, mode = \"${geoMode.name}\")"
        }

        return GeneratedModelResult(
            node = node,
            explanation = "$engineNote Modèle « $cleanTitle » inséré directement dans le Viewport 3D.",
            suggestedScript = script
        )
    }

    fun planClaudeRig(node: SceneNode?, archetypeOverride: String? = null): ClaudeRigPlan {
        val roles = node?.parts?.map { it.role }?.toSet().orEmpty()
        val archetype = archetypeOverride ?: when {
            "wing_l" in roles || "tail" in roles -> "Créature / Dragon IK"
            roles.count { it.startsWith("leg_") } >= 4 -> "Quadrupède IK"
            node?.geometryMode == GeometryMode.VOXEL_MODULAIRE -> "Entité Blockbench IK"
            else -> "Bipède IK"
        }

        val bones = when {
            archetype.contains("Dragon") || archetype.contains("Créature") -> listOf(
                BoneJoint("pelvis", "Pelvis_Root", "Center_Mass", 0f, 0f, 0f, true, "torso"),
                BoneJoint("spine_02", "Spine_Torso", "Torso_Chain", 8f, 0f, 0f, false, "torso"),
                BoneJoint("neck", "Neck_Head", "LookAt_IK", -12f, 10f, 0f, true, "head"),
                BoneJoint("wing_l", "L_Wing_IK", "Left_Wing", -24f, -16f, 22f, true, "wing_l"),
                BoneJoint("wing_r", "R_Wing_IK", "Right_Wing", -24f, 16f, -22f, true, "wing_r"),
                BoneJoint("tail_ik", "Tail_Spline_IK", "Tail_Chain", 14f, -18f, 0f, false, "tail"),
                BoneJoint("foot_ik_l", "L_Claw_IK", "Left_Leg", -6f, 0f, 0f, true, "leg_l"),
                BoneJoint("foot_ik_r", "R_Claw_IK", "Right_Leg", 6f, 0f, 0f, true, "leg_r")
            )
            archetype.contains("Quadrupède") -> listOf(
                BoneJoint("pelvis", "Pelvis_Root", "Center_Mass", 0f, 0f, 0f, true, "torso"),
                BoneJoint("spine_02", "Spine_Back", "Spine_Chain", 4f, 0f, 0f, false, "torso"),
                BoneJoint("neck", "Head_Jaw", "LookAt_IK", -8f, 12f, 0f, true, "head"),
                BoneJoint("hand_ik_l", "FL_Paw_IK", "Front_Left", 12f, 0f, 0f, true, "arm_l"),
                BoneJoint("hand_ik_r", "FR_Paw_IK", "Front_Right", -12f, 0f, 0f, true, "arm_r"),
                BoneJoint("foot_ik_l", "BL_Paw_IK", "Back_Left", -10f, 0f, 0f, true, "leg_l"),
                BoneJoint("foot_ik_r", "BR_Paw_IK", "Back_Right", 10f, 0f, 0f, true, "leg_r"),
                BoneJoint("tail_ik", "Tail_Tip", "Tail_IK", 18f, 8f, 0f, false, "tail")
            )
            else -> defaultBones()
        }

        val targetName = node?.name ?: "Objet_3D"
        return ClaudeRigPlan(
            archetype = archetype,
            bones = bones,
            note = "Claude Rigging Live : ${bones.size} chaînes IK ($archetype) générées et liées à « $targetName »."
        )
    }

    fun generateClaudeAnimation(prompt: String, style: AnimationStyle): ClaudeAnimationPlan {
        val id = "claude_${style.name.lowercase()}_${System.nanoTime()}"
        val shortTopic = prompt.trim().ifBlank { style.shortLabel }.take(24)

        val (frames, fps, poses) = when (style) {
            AnimationStyle.CARTOON_TEMMER -> {
                // Squash & Stretch exagéré, anticipation négative puis saut élastique + overshoot
                val map = mapOf(
                    1 to KeyframePose(1, offsetY = -0.10f, rotYDeg = -14f, scale = 1f, scaleX = 1.28f, scaleY = 0.72f, scaleZ = 1.28f, rotZDeg = -12f, limbSwingDeg = -32f, headBobDeg = -16f),
                    14 to KeyframePose(14, offsetY = 0.78f, rotYDeg = 42f, scale = 1f, scaleX = 0.78f, scaleY = 1.32f, scaleZ = 0.78f, rotZDeg = 14f, limbSwingDeg = 46f, headBobDeg = 20f),
                    28 to KeyframePose(28, offsetY = -0.06f, rotYDeg = 120f, scale = 1f, scaleX = 1.30f, scaleY = 0.70f, scaleZ = 1.30f, rotZDeg = -10f, limbSwingDeg = -38f, headBobDeg = -18f),
                    44 to KeyframePose(44, offsetY = 0.62f, rotYDeg = 240f, scale = 1f, scaleX = 0.84f, scaleY = 1.22f, scaleZ = 0.84f, rotZDeg = 11f, limbSwingDeg = 40f, headBobDeg = 14f),
                    60 to KeyframePose(60, offsetY = 0.0f, rotYDeg = 360f, scale = 1f, scaleX = 1.12f, scaleY = 0.88f, scaleZ = 1.12f, rotZDeg = 0f, limbSwingDeg = 0f, headBobDeg = 0f)
                )
                Triple(60, 24, map)
            }
            AnimationStyle.REALISTIC_MOCAP -> {
                val map = mapOf(
                    1 to KeyframePose(1, offsetY = 0.0f, rotYDeg = -6f, scale = 1f, rotXDeg = 4f, limbSwingDeg = -20f, headBobDeg = 2f),
                    15 to KeyframePose(15, offsetY = 0.09f, rotYDeg = 0f, scale = 1f, rotXDeg = 6f, limbSwingDeg = 22f, headBobDeg = -2f),
                    30 to KeyframePose(30, offsetY = 0.0f, rotYDeg = 6f, scale = 1f, rotXDeg = 4f, limbSwingDeg = -20f, headBobDeg = 2f),
                    45 to KeyframePose(45, offsetY = 0.09f, rotYDeg = 0f, scale = 1f, rotXDeg = 5f, limbSwingDeg = 22f, headBobDeg = -2f),
                    60 to KeyframePose(60, offsetY = 0.0f, rotYDeg = -6f, scale = 1f, rotXDeg = 4f, limbSwingDeg = -20f, headBobDeg = 2f)
                )
                Triple(60, 30, map)
            }
            AnimationStyle.ANIME_SHONEN -> {
                val map = mapOf(
                    1 to KeyframePose(1, offsetY = -0.14f, rotYDeg = -35f, scale = 1f, scaleX = 1.14f, scaleY = 0.86f, scaleZ = 1.14f, rotXDeg = 18f, limbSwingDeg = -45f, headBobDeg = -10f),
                    12 to KeyframePose(12, offsetY = 0.52f, rotYDeg = 95f, scale = 1f, scaleX = 0.86f, scaleY = 1.18f, scaleZ = 0.86f, rotXDeg = -12f, limbSwingDeg = 55f, headBobDeg = 12f),
                    24 to KeyframePose(24, offsetY = 0.44f, rotYDeg = 180f, scale = 1f, scaleX = 0.92f, scaleY = 1.10f, scaleZ = 0.92f, rotZDeg = 14f, limbSwingDeg = 48f, headBobDeg = 8f),
                    48 to KeyframePose(48, offsetY = 0.0f, rotYDeg = 360f, scale = 1f, rotXDeg = 0f, limbSwingDeg = 0f, headBobDeg = 0f)
                )
                Triple(48, 24, map)
            }
            AnimationStyle.MINECRAFT_GECKO -> {
                val map = mapOf(
                    1 to KeyframePose(1, offsetY = 0.0f, rotYDeg = 0f, scale = 1f, limbSwingDeg = -34f, headBobDeg = 6f),
                    15 to KeyframePose(15, offsetY = 0.14f, rotYDeg = 0f, scale = 1f, limbSwingDeg = 34f, headBobDeg = -6f),
                    30 to KeyframePose(30, offsetY = 0.0f, rotYDeg = 0f, scale = 1f, limbSwingDeg = -34f, headBobDeg = 6f),
                    45 to KeyframePose(45, offsetY = 0.14f, rotYDeg = 0f, scale = 1f, limbSwingDeg = 34f, headBobDeg = -6f),
                    60 to KeyframePose(60, offsetY = 0.0f, rotYDeg = 0f, scale = 1f, limbSwingDeg = 0f, headBobDeg = 0f)
                )
                Triple(60, 20, map)
            }
            AnimationStyle.HORROR_STALKER -> {
                val map = mapOf(
                    1 to KeyframePose(1, offsetY = 0.0f, rotYDeg = -12f, scale = 1f, rotZDeg = 18f, limbSwingDeg = -14f, headBobDeg = 28f),
                    20 to KeyframePose(20, offsetY = 0.08f, rotYDeg = 16f, scale = 1f, scaleX = 0.94f, scaleY = 1.08f, scaleZ = 0.94f, rotZDeg = -22f, limbSwingDeg = 26f, headBobDeg = -24f),
                    35 to KeyframePose(35, offsetY = 0.24f, rotYDeg = -4f, scale = 1f, rotZDeg = 12f, limbSwingDeg = -36f, headBobDeg = 32f),
                    60 to KeyframePose(60, offsetY = 0.02f, rotYDeg = 8f, scale = 1f, rotZDeg = -14f, limbSwingDeg = 18f, headBobDeg = -18f),
                    90 to KeyframePose(90, offsetY = 0.0f, rotYDeg = 0f, scale = 1f, rotZDeg = 0f, limbSwingDeg = 0f, headBobDeg = 0f)
                )
                Triple(90, 24, map)
            }
            AnimationStyle.STOP_MOTION -> {
                val map = mapOf(
                    1 to KeyframePose(1, offsetY = 0f, rotYDeg = 0f, scale = 1f, rotZDeg = -8f, limbSwingDeg = -24f),
                    12 to KeyframePose(12, offsetY = 0.34f, rotYDeg = 90f, scale = 1f, scaleX = 0.9f, scaleY = 1.12f, scaleZ = 0.9f, rotZDeg = 8f, limbSwingDeg = 28f),
                    24 to KeyframePose(24, offsetY = 0.04f, rotYDeg = 180f, scale = 1f, scaleX = 1.14f, scaleY = 0.88f, scaleZ = 1.14f, rotZDeg = -6f, limbSwingDeg = -20f),
                    36 to KeyframePose(36, offsetY = 0.30f, rotYDeg = 270f, scale = 1f, rotZDeg = 8f, limbSwingDeg = 24f),
                    48 to KeyframePose(48, offsetY = 0f, rotYDeg = 360f, scale = 1f, rotZDeg = 0f, limbSwingDeg = 0f)
                )
                Triple(48, 12, map)
            }
        }

        val clip = AnimationClip(
            id = id,
            name = "${style.shortLabel}: $shortTopic",
            frames = frames,
            fps = fps,
            style = style
        )
        return ClaudeAnimationPlan(
            clip = clip,
            poses = poses,
            summary = "Claude Animation (${style.label}) générée : ${poses.size} keyframes à $fps FPS."
        )
    }

    fun generateClaudeModProject(
        prompt: String,
        modState: ModBuildState,
        activeNode: SceneNode?
    ): GeneratedModBundle {
        val rawId = modState.modId.ifBlank { "omnicore_mod" }
            .lowercase()
            .replace(Regex("[^a-z0-9_]"), "_")
        val modName = modState.modName.ifBlank { "OmniCore Mod" }
        val desc = prompt.trim().ifBlank { "Mod généré en accès illimité par Claude + ${modState.assetEngine.label}" }
        val entityId = (activeNode?.name ?: rawId).lowercase().replace(Regex("[^a-z0-9_]"), "_")
        val files = linkedMapOf<String, String>()

        val bpUuid1 = UUID.nameUUIDFromBytes("${rawId}_bp_header".toByteArray()).toString()
        val bpUuid2 = UUID.nameUUIDFromBytes("${rawId}_bp_module".toByteArray()).toString()
        val rpUuid1 = UUID.nameUUIDFromBytes("${rawId}_rp_header".toByteArray()).toString()
        val rpUuid2 = UUID.nameUUIDFromBytes("${rawId}_rp_module".toByteArray()).toString()

        val geoJson = buildGeckoOrBedrockGeoJson(entityId, activeNode, modState.assetEngine)
        val animJson = """
            {
              "format_version": "1.8.0",
              "animations": {
                "animation.$entityId.stalk": {
                  "loop": true,
                  "animation_length": 2.0,
                  "bones": {
                    "body": { "rotation": { "0.0": [0, 0, 0], "1.0": [5, 0, 4], "2.0": [0, 0, 0] } },
                    "head": { "rotation": { "0.0": [-6, -10, 0], "1.0": [6, 10, 0], "2.0": [-6, -10, 0] } }
                  }
                }
              }
            }
        """.trimIndent()

        when (modState.exportFormat) {
            ModExportFormat.MCADDON -> {
                files["behavior_pack/manifest.json"] = """
                    {
                      "format_version": 2,
                      "header": {
                        "name": "$modName BP",
                        "description": "$desc",
                        "uuid": "$bpUuid1",
                        "version": [1, 0, 0],
                        "min_engine_version": [1, 20, 0]
                      },
                      "modules": [
                        { "type": "data", "uuid": "$bpUuid2", "version": [1, 0, 0] }
                      ]
                    }
                """.trimIndent()
                files["resource_pack/manifest.json"] = """
                    {
                      "format_version": 2,
                      "header": {
                        "name": "$modName RP",
                        "description": "Assets ${modState.assetEngine.label} (${modState.targetType.label})",
                        "uuid": "$rpUuid1",
                        "version": [1, 0, 0],
                        "min_engine_version": [1, 20, 0]
                      },
                      "modules": [
                        { "type": "resources", "uuid": "$rpUuid2", "version": [1, 0, 0] }
                      ]
                    }
                """.trimIndent()
                if (modState.targetType == BlockbenchTargetType.BLOCK) {
                    files["behavior_pack/blocks/${entityId}.json"] = """
                        {
                          "format_version": "1.20.20",
                          "minecraft:block": {
                            "description": { "identifier": "$rawId:$entityId" },
                            "components": {
                              "minecraft:destructible_by_mining": { "seconds_to_destroy": 1.8 },
                              "minecraft:light_emission": ${(modState.volumetricFogDensity * 15).toInt()},
                              "minecraft:geometry": "geometry.$entityId"
                            }
                          }
                        }
                    """.trimIndent()
                } else {
                    files["behavior_pack/entities/${entityId}.json"] = """
                        {
                          "format_version": "1.20.0",
                          "minecraft:entity": {
                            "description": {
                              "identifier": "$rawId:$entityId",
                              "is_spawnable": true,
                              "is_summonable": true
                            },
                            "components": {
                              "minecraft:health": { "value": 40, "max": 40 },
                              "minecraft:movement": { "value": ${(0.22f + modState.aiAggression * 0.18f)} },
                              "minecraft:attack": { "damage": ${(4 + modState.jumpscareIntensity * 8).toInt()} },
                              "minecraft:behavior.nearest_attackable_target": {
                                "priority": 1,
                                "within_radius": ${modState.detectionRadius}
                              }
                            }
                          }
                        }
                    """.trimIndent()
                }
                files["resource_pack/models/entity/${entityId}.geo.json"] = geoJson
                files["resource_pack/animations/${entityId}.animation.json"] = animJson
            }

            ModExportFormat.JAR -> {
                val className = entityId.split("_").joinToString("") { s -> s.replaceFirstChar { it.uppercase() } }
                files["META-INF/mods.toml"] = """
                    modLoader="javafml"
                    loaderVersion="[4,)"
                    license="MIT"
                    [[mods]]
                    modId="$rawId"
                    version="${modState.version}"
                    displayName="$modName"
                    description='''$desc'''
                """.trimIndent()
                files["src/main/java/com/omnicore/$rawId/${className}Entity.java"] = """
                    package com.omnicore.$rawId;

                    // Généré par Claude Code Illimité (${modState.loader.label} + ${modState.assetEngine.label})
                    public class ${className}Entity {
                        public static final String TARGET_TYPE = "${modState.targetType.name}";
                        public static final float AI_AGGRESSION = ${modState.aiAggression}f;
                        public static final float DETECTION_RADIUS = ${modState.detectionRadius}f;
                        public static final boolean PROXIMITY_TRIGGER = ${modState.triggerProximity};
                        public static final boolean DARKNESS_TRIGGER = ${modState.triggerDarkness};
                    }
                """.trimIndent()
                files["assets/$rawId/geo/${entityId}.geo.json"] = geoJson
                files["assets/$rawId/animations/${entityId}.animation.json"] = animJson
            }

            ModExportFormat.BBMODEL -> {
                files["${entityId}.bbmodel"] = buildBbModelJson(entityId, modName, activeNode)
            }
        }

        return GeneratedModBundle(
            modId = rawId,
            modName = modName,
            files = files,
            summary = "Claude a généré ${files.size} fichier(s) ${modState.exportFormat.ext} (${modState.assetEngine.label} • ${modState.targetType.label})."
        )
    }

    fun answerAssistant(
        prompt: String,
        engine: ModelingAiEngine,
        targetType: BlockbenchTargetType
    ): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("temmer") || lower.contains("cartoon") || lower.contains("anim") ->
                "Claude Animation (${AnimationStyle.CARTOON_TEMMER.label}) activé sans limite :\n" +
                    "1. Squash & Stretch dynamique (compression 0.72x au sol -> étirement 1.32x en saut).\n" +
                    "2. Anticipation rapide sur les premières frames avant le rebond.\n" +
                    "3. Balancement des bras et tête synchronisé en direct dans la vue 3D.\n" +
                    "Astuce : ouvrez l'onglet « Anim » et touchez « Cartoon KT » pour voir le mouvement immédiatement."

            lower.contains("rig") || lower.contains("os") || lower.contains("squelette") ->
                "Claude Rigging Live (0 crédit) :\n" +
                    "• Détecte automatiquement les membres (Bipède, Dragon/Ailes, Quadrupède, Blockbench).\n" +
                    "• Affiche chaque os et cible IK en direct dans le viewport 3D.\n" +
                    "• Faites glisser Pitch (X) / Yaw (Y) sur n'importe quel os dans « Anim » pour animer le modèle en temps réel."

            lower.contains("shader") || lower.contains("particule") || lower.contains("polyx") ->
                "Shaders PolyX3D Pro & Particules adaptatives :\n" +
                    "• 12 shaders disponibles (Standard PBR+, Toon Cel Pro, MatCap Argile, Glass Cristal, Hologramme, Néon Bloom, VHS, Lumen RTGI, Horreur Fog, Or/Chrome, Aquarelle, Thermique).\n" +
                    "• 17 systèmes de particules qui changent automatiquement de palette et de physique selon le shader choisi."

            lower.contains("mcaddon") || lower.contains("jar") || lower.contains("mod") || lower.contains("blockbench") ->
                "Atelier Mods Claude + Blockbench/Meshy :\n" +
                    "• Pour Minecraft Bedrock : choisissez « .mcaddon » + « Blockbench IA » (Bloc 3D ou Entité).\n" +
                    "• Pour Minecraft Java (NeoForge/Fabric/Forge) : choisissez « .jar » + « Blockbench IA » ou « Meshy 3D » Smooth.\n" +
                    "• Exportez ensuite l'archive ZIP réelle directement depuis l'onglet « Jeux > Mods .mcaddon / .jar »."

            lower.contains("play store") || lower.contains("aab") || lower.contains("publier") ->
                "Publication gratuite sur Google Play Store :\n" +
                    "• OmniCore génère votre Android App Bundle (.AAB) et APK ARM64 sans aucun frais ni commission.\n" +
                    "• Rendez-vous dans « Jeux > Play Store .AAB / .EXE » pour configurer votre Package ID et exporter le bundle."

            else ->
                "${engine.label} (${targetType.label}) est prêt en accès 100% gratuit et illimité :\n" +
                    "• Touchez « Générer 3D » pour créer un objet multi-pièces directement dans le viewport.\n" +
                    "• Utilisez Meshy 3D / Tripo3D pour les maillages Smooth PBR, Blockbench IA pour les blocs/entités Minecraft, et Claude pour le Rigging Live, les animations et les mods .mcaddon / .jar."
        }
    }

    private fun buildGeckoOrBedrockGeoJson(
        entityId: String,
        node: SceneNode?,
        engine: ModelingAiEngine
    ): String {
        val parts = node?.parts?.takeIf { it.isNotEmpty() } ?: buildCharacterOrMechParts(entityId, true)
        val cubesJson = parts.joinToString(",\n") { part ->
            val ox = (part.offsetX * 16f).toInt()
            val oy = ((part.offsetY + 0.5f) * 16f).toInt()
            val oz = (part.offsetZ * 16f).toInt()
            val sx = (part.sizeX * 24f).coerceAtLeast(2f).toInt()
            val sy = (part.sizeY * 24f).coerceAtLeast(2f).toInt()
            val sz = (part.sizeZ * 24f).coerceAtLeast(2f).toInt()
            """            { "origin": [$ox, $oy, $oz], "size": [$sx, $sy, $sz], "uv": [0, 0] }"""
        }
        return """
            {
              "format_version": "1.12.0",
              "minecraft:geometry": [
                {
                  "description": {
                    "identifier": "geometry.$entityId",
                    "texture_width": 64,
                    "texture_height": 64,
                    "source_engine": "${engine.label}"
                  },
                  "bones": [
                    {
                      "name": "root",
                      "pivot": [0, 0, 0],
                      "cubes": [
            $cubesJson
                      ]
                    }
                  ]
                }
              ]
            }
        """.trimIndent()
    }

    private fun buildBbModelJson(entityId: String, modName: String, node: SceneNode?): String {
        val parts = node?.parts?.takeIf { it.isNotEmpty() } ?: buildCharacterOrMechParts(entityId, true)
        val elements = parts.mapIndexed { idx, p ->
            val fx = (p.offsetX - p.sizeX) * 16f
            val fy = (p.offsetY + 0.6f - p.sizeY) * 16f
            val fz = (p.offsetZ - p.sizeZ) * 16f
            val tx = (p.offsetX + p.sizeX) * 16f
            val ty = (p.offsetY + 0.6f + p.sizeY) * 16f
            val tz = (p.offsetZ + p.sizeZ) * 16f
            """    { "name": "${p.role}_$idx", "from": [$fx, $fy, $fz], "to": [$tx, $ty, $tz], "uuid": "bb_$idx" }"""
        }.joinToString(",\n")
        return """
            {
              "meta": { "format_version": "4.10", "model_format": "bedrock", "box_uv": true },
              "name": "$modName",
              "geometry_name": "$entityId",
              "resolution": { "width": 64, "height": 64 },
              "elements": [
            $elements
              ]
            }
        """.trimIndent()
    }

    private fun buildDragonParts(voxel: Boolean): List<SubMeshPart> {
        val s = if (voxel) PrimitiveShape.CUBE else PrimitiveShape.SPHERE
        val b = PrimitiveShape.CUBE
        return listOf(
            SubMeshPart("torso", b, 0f, 0.1f, 0f, 0.34f, 0.24f, 0.48f, 0xFF991B1B),
            SubMeshPart("head", b, 0f, 0.52f, 0.56f, 0.20f, 0.18f, 0.24f, 0xFFDC2626),
            SubMeshPart("wing_l", b, -0.74f, 0.42f, 0f, 0.52f, 0.06f, 0.36f, 0xFFEF4444),
            SubMeshPart("wing_r", b, 0.74f, 0.42f, 0f, 0.52f, 0.06f, 0.36f, 0xFFEF4444),
            SubMeshPart("tail", s, 0f, 0.02f, -0.68f, 0.12f, 0.12f, 0.42f, 0xFF7F1D1D),
            SubMeshPart("leg_l", b, -0.22f, -0.32f, 0.12f, 0.11f, 0.24f, 0.11f, 0xFFB91C1C),
            SubMeshPart("leg_r", b, 0.22f, -0.32f, 0.12f, 0.11f, 0.24f, 0.11f, 0xFFB91C1C)
        )
    }

    private fun buildSwordParts(voxel: Boolean): List<SubMeshPart> = listOf(
        SubMeshPart("blade", PrimitiveShape.CUBE, 0f, 0.48f, 0f, if (voxel) 0.12f else 0.09f, 0.62f, 0.04f, 0xFF38BDF8),
        SubMeshPart("guard", PrimitiveShape.CUBE, 0f, -0.14f, 0f, 0.36f, 0.06f, 0.09f, 0xFFF59E0B),
        SubMeshPart("handle", PrimitiveShape.CYLINDRE, 0f, -0.38f, 0f, 0.06f, 0.22f, 0.06f, 0xFF78350F),
        SubMeshPart("pommel", if (voxel) PrimitiveShape.CUBE else PrimitiveShape.SPHERE, 0f, -0.62f, 0f, 0.09f, 0.09f, 0.09f, 0xFFFBBF24)
    )

    private fun buildVehicleParts(voxel: Boolean): List<SubMeshPart> = listOf(
        SubMeshPart("torso", PrimitiveShape.CUBE, 0f, 0f, 0f, 0.56f, 0.20f, 0.88f, 0xFF3B82F6),
        SubMeshPart("head", PrimitiveShape.CUBE, 0f, 0.28f, -0.08f, 0.42f, 0.16f, 0.44f, 0xFF93C5FD),
        SubMeshPart("wheel", if (voxel) PrimitiveShape.CUBE else PrimitiveShape.CYLINDRE, -0.58f, -0.22f, 0.48f, 0.12f, 0.18f, 0.18f, 0xFF1F2937),
        SubMeshPart("wheel", if (voxel) PrimitiveShape.CUBE else PrimitiveShape.CYLINDRE, 0.58f, -0.22f, 0.48f, 0.12f, 0.18f, 0.18f, 0xFF1F2937),
        SubMeshPart("wheel", if (voxel) PrimitiveShape.CUBE else PrimitiveShape.CYLINDRE, -0.58f, -0.22f, -0.48f, 0.12f, 0.18f, 0.18f, 0xFF1F2937),
        SubMeshPart("wheel", if (voxel) PrimitiveShape.CUBE else PrimitiveShape.CYLINDRE, 0.58f, -0.22f, -0.48f, 0.12f, 0.18f, 0.18f, 0xFF1F2937)
    )

    private fun buildTreeParts(voxel: Boolean): List<SubMeshPart> = listOf(
        SubMeshPart("trunk", if (voxel) PrimitiveShape.CUBE else PrimitiveShape.CYLINDRE, 0f, -0.25f, 0f, 0.16f, 0.46f, 0.16f, 0xFF78350F),
        SubMeshPart("crown", if (voxel) PrimitiveShape.CUBE else PrimitiveShape.SPHERE, 0f, 0.36f, 0f, 0.54f, 0.38f, 0.54f, 0xFF10B981),
        SubMeshPart("crown_top", if (voxel) PrimitiveShape.CUBE else PrimitiveShape.CONE, 0f, 0.72f, 0f, 0.36f, 0.28f, 0.36f, 0xFF34D399)
    )

    private fun buildArchitectureParts(voxel: Boolean): List<SubMeshPart> = listOf(
        SubMeshPart("base", PrimitiveShape.CUBE, 0f, -0.05f, 0f, 0.64f, 0.48f, 0.64f, 0xFF64748B),
        SubMeshPart("roof", if (voxel) PrimitiveShape.CUBE else PrimitiveShape.CONE, 0f, 0.62f, 0f, 0.72f, 0.30f, 0.72f, 0xFFB45309),
        SubMeshPart("door", PrimitiveShape.CUBE, 0f, -0.26f, 0.65f, 0.18f, 0.26f, 0.04f, 0xFF451A03)
    )

    private fun buildBlockbenchBlockParts(lower: String): List<SubMeshPart> {
        val coreColor = when {
            lower.contains("diam") -> 0xFF22D3EE
            lower.contains("or") || lower.contains("gold") -> 0xFFFBBF24
            lower.contains("red") || lower.contains("rubis") -> 0xFFEF4444
            lower.contains("emeraude") || lower.contains("herbe") -> 0xFF10B981
            else -> 0xFF64748B
        }
        return listOf(
            SubMeshPart("block_core", PrimitiveShape.CUBE, 0f, 0f, 0f, 0.56f, 0.56f, 0.56f, 0xFF334155),
            SubMeshPart("block_trim_top", PrimitiveShape.CUBE, 0f, 0.50f, 0f, 0.58f, 0.10f, 0.58f, coreColor),
            SubMeshPart("block_crystal", PrimitiveShape.CUBE, 0f, 0.05f, 0.56f, 0.22f, 0.22f, 0.06f, coreColor),
            SubMeshPart("block_crystal_b", PrimitiveShape.CUBE, 0.56f, 0.05f, 0f, 0.06f, 0.22f, 0.22f, coreColor)
        )
    }

    private fun buildQuadrupedParts(voxel: Boolean): List<SubMeshPart> = listOf(
        SubMeshPart("torso", PrimitiveShape.CUBE, 0f, 0.08f, 0f, 0.30f, 0.22f, 0.56f, 0xFF78716C),
        SubMeshPart("head", PrimitiveShape.CUBE, 0f, 0.34f, 0.56f, 0.20f, 0.20f, 0.22f, 0xFFA8A29E),
        SubMeshPart("arm_l", PrimitiveShape.CUBE, -0.22f, -0.30f, 0.36f, 0.09f, 0.26f, 0.09f, 0xFF57534E),
        SubMeshPart("arm_r", PrimitiveShape.CUBE, 0.22f, -0.30f, 0.36f, 0.09f, 0.26f, 0.09f, 0xFF57534E),
        SubMeshPart("leg_l", PrimitiveShape.CUBE, -0.22f, -0.30f, -0.36f, 0.09f, 0.26f, 0.09f, 0xFF57534E),
        SubMeshPart("leg_r", PrimitiveShape.CUBE, 0.22f, -0.30f, -0.36f, 0.09f, 0.26f, 0.09f, 0xFF57534E),
        SubMeshPart("tail", if (voxel) PrimitiveShape.CUBE else PrimitiveShape.CYLINDRE, 0f, 0.22f, -0.64f, 0.07f, 0.07f, 0.22f, 0xFFD6D3D1)
    )

    private fun buildMonsterParts(voxel: Boolean): List<SubMeshPart> = listOf(
        SubMeshPart("head", PrimitiveShape.CUBE, 0f, 0.78f, 0.04f, 0.20f, 0.22f, 0.20f, 0xFF1E293B),
        SubMeshPart("torso", PrimitiveShape.CUBE, 0f, 0.26f, 0f, 0.26f, 0.32f, 0.16f, 0xFF0F172A),
        SubMeshPart("arm_l", PrimitiveShape.CUBE, -0.42f, 0.20f, 0.10f, 0.08f, 0.42f, 0.08f, 0xFF991B1B),
        SubMeshPart("arm_r", PrimitiveShape.CUBE, 0.42f, 0.20f, 0.10f, 0.08f, 0.42f, 0.08f, 0xFF991B1B),
        SubMeshPart("leg_l", PrimitiveShape.CUBE, -0.14f, -0.36f, 0f, 0.10f, 0.32f, 0.10f, 0xFF334155),
        SubMeshPart("leg_r", PrimitiveShape.CUBE, 0.14f, -0.36f, 0f, 0.10f, 0.32f, 0.10f, 0xFF334155)
    )

    private fun buildCharacterOrMechParts(lower: String, voxel: Boolean): List<SubMeshPart> {
        val accent = if (lower.contains("mech") || lower.contains("robot")) 0xFF4A9EFF else 0xFF60A5FA
        val shape = if (voxel) PrimitiveShape.CUBE else PrimitiveShape.CUBE
        return listOf(
            SubMeshPart("head", shape, 0f, 0.72f, 0f, 0.18f, 0.18f, 0.18f, 0xFFE2E8F0),
            SubMeshPart("torso", shape, 0f, 0.25f, 0f, 0.28f, 0.28f, 0.16f, accent),
            SubMeshPart("arm_l", shape, -0.42f, 0.25f, 0f, 0.10f, 0.26f, 0.10f, 0xFF94A3B8),
            SubMeshPart("arm_r", shape, 0.42f, 0.25f, 0f, 0.10f, 0.26f, 0.10f, 0xFF94A3B8),
            SubMeshPart("leg_l", shape, -0.14f, -0.32f, 0f, 0.11f, 0.28f, 0.11f, 0xFF334155),
            SubMeshPart("leg_r", shape, 0.14f, -0.32f, 0f, 0.11f, 0.28f, 0.11f, 0xFF334155)
        )
    }
}
