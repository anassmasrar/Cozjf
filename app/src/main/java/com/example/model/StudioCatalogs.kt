package com.example.model

import com.example.R

fun defaultTutorialSteps(): List<TutorialStep> = listOf(
    TutorialStep(
        category = "Bases 3D",
        title = "1. Navigation 3D fluide & Caméra",
        description = "Maîtrisez la vue 3D dès la première seconde, que vous soyez sur téléphone ou station PC.",
        bullets = listOf(
            "1 doigt glissé : orbite naturelle (glisser vers le bas affiche le dessus de l'objet).",
            "2 doigts pincés ou boutons + / - : zoom précis sans déformer la perspective.",
            "Boutons Face / Dessus / Côté / Iso en haut à droite : recadrage instantané.",
            "Gizmo XYZ coloré (Rouge X, Vert Y, Bleu Z) : touchez-le pour réinitialiser la vue."
        ),
        tip = "Astuce : activez la Grille ou le mode Rayons X dans le menu « Affichage » pour aligner vos pièces au millimètre.",
        targetTab = OmniCoreTab.MODELISATION
    ),
    TutorialStep(
        category = "Modélisation IA",
        title = "2. Meshy 3D, Tripo3D & Blockbench (0 Crédit)",
        description = "OmniCore remplace les systèmes à crédits payants par 3 moteurs spécialisés en accès libre et illimité.",
        bullets = listOf(
            "Meshy 3D : idéal pour les objets organiques, créatures, armes et textures PBR Smooth.",
            "Tripo3D : parfait pour transformer une silhouette ou un croquis en volume multi-pièces.",
            "Blockbench IA : choisissez entre « Bloc 3D » ou « Entité / Mob » Minecraft avec hiérarchie de cubes.",
            "Chaque génération ajoute automatiquement ses sous-parties articulées dans la scène."
        ),
        tip = "Astuce : tapez « Dragon ailé », « Épée rubis », « Creeper » ou « Robot Mech » dans Meshy/Blockbench pour voir les sous-pièces créées.",
        targetTab = OmniCoreTab.IA_STUDIO
    ),
    TutorialStep(
        category = "Shaders & FX",
        title = "3. Shaders PolyX3D Pro & Particules Adaptatives",
        description = "Sublimez vos rendus grâce aux 12 shaders inspirés de PolyX3D par Zarrai et aux 17 systèmes de particules.",
        bullets = listOf(
            "Toon Cel Pro : aplats nets avec contour d'encre noir pour vos animations cartoon.",
            "MatCap Argile : éclairage de sculpture chaud idéal pour vérifier les volumes.",
            "Glass Cristal, Hologramme, Néon Bloom, VHS CRT, Lumen RTGI, Horreur Fog, Or & Chrome, Aquarelle, Thermique.",
            "En mode « Auto (Adapté au Shader) », les particules changent automatiquement de couleur, forme et physique selon le shader actif."
        ),
        tip = "Astuce : combinez « Toon Cel Pro » + « Lignes Cartoon » ou « Horreur Fog » + « Cendres & Spores » depuis le panneau Shaders & FX.",
        targetTab = OmniCoreTab.MODELISATION
    ),
    TutorialStep(
        category = "Rigging Live",
        title = "4. Claude Auto-Rigging en Direct & Weight Paint",
        description = "Visualisez Claude construire votre squelette os par os directement sur votre modèle 3D.",
        bullets = listOf(
            "Dans l'onglet « Anim », choisissez l'archétype (Bipède, Créature/Dragon, Quadrupède, Blockbench) ou laissez Claude détecter.",
            "Appuyez sur « Claude Auto-Rigging Live » : chaque os (Pelvis, Spine, Cou, Bras IK, Jambes IK) apparaît en direct sur le maillage.",
            "Sélectionnez n'importe quelle articulation dans la liste et bougez Pitch / Yaw / Roll : le membre 3D bouge instantanément.",
            "Activez la vue « Weight Painting » pour visualiser les zones d'influence thermiques."
        ),
        tip = "Astuce : verrouillez les cibles IK (icône cadenas) sur les pieds pour ancrer le personnage au sol pendant ses animations.",
        targetTab = OmniCoreTab.ANIMATION_RIG
    ),
    TutorialStep(
        category = "Animation IA",
        title = "5. Styles d'Animation (Kevin Temmer Tunes, Réaliste...)",
        description = "Demandez à Claude de générer des animations complètes selon le style exact de votre projet.",
        bullets = listOf(
            "Cartoon (Kevin Temmer Tunes) : Squash & Stretch exagéré, anticipation rapide, rebond élastique et balancement expressif.",
            "Réaliste MoCap : poids du corps naturel, pas fluides et gravité réaliste.",
            "Anime Action : poses clés très contrastées avec maintien dramatique et impact sec.",
            "Blockbench/MC, Horreur Stalker et Stop-Motion 12 FPS : adaptés aux mods Minecraft et jeux indépendants."
        ),
        tip = "Astuce : cliquez sur « + Keyframe » à n'importe quelle frame et ajustez les curseurs Y / Rotation / Squash pour affiner la courbe en direct.",
        targetTab = OmniCoreTab.ANIMATION_RIG
    ),
    TutorialStep(
        category = "Mods Minecraft",
        title = "6. Mods Illimités Claude (.mcaddon & .jar)",
        description = "Créez et exportez de véritables archives de mods pour Minecraft Bedrock et Java sans aucune limite.",
        bullets = listOf(
            "Choisissez le format : .mcaddon (Bedrock Edition) ou .jar (Java NeoForge / Fabric / Forge) ou .bbmodel.",
            "Choisissez l'IA d'actifs selon le besoin du joueur : Blockbench IA (cubique) ou Meshy 3D (Smooth GeckoLib).",
            "Décrivez votre mob, boss ou bloc à Claude : il génère le manifeste, les comportements IA, la géométrie et le code.",
            "Exportez directement l'archive ZIP (.mcaddon / .jar / .bbmodel) sur votre téléphone via le bouton d'enregistrement."
        ),
        tip = "Astuce : activez les déclencheurs Proximité + Obscurité + Brouillard dans le sous-panneau Horreur pour créer un mod type Stalker.",
        targetTab = OmniCoreTab.JEUX_MODS
    ),
    TutorialStep(
        category = "Logique & Code",
        title = "7. Blocs Logiques, Nodes, Java, Python & GLSL",
        description = "Assemblez les comportements de vos jeux sans écrire une ligne, ou inspectez le code généré en direct.",
        bullets = listOf(
            "Empilez des blocs : Événement (Raycast / Tick) -> Condition (Distance / Lumière) -> Action (Poursuite IK) -> FX.",
            "Basculez instantanément entre la vue Geometry Nodes, Java, Python et Shader GLSL.",
            "Chaque bloc ajouté met à jour le graphe nodal et le script généré."
        ),
        tip = "Astuce : commencez toujours par un bloc « Événement » pour déclencher votre boucle de jeu à 60 Hz.",
        targetTab = OmniCoreTab.JEUX_MODS
    ),
    TutorialStep(
        category = "Play Store Gratuit",
        title = "8. Publier gratuitement sur Google Play Store (.AAB)",
        description = "OmniCore ne prend aucune commission et prépare votre jeu pour une publication directe sur Android et PC.",
        bullets = listOf(
            "Dans « Jeux & Mods > Play Store .AAB / .EXE », renseignez votre Package ID (ex: com.studio.monjeu).",
            "Générez le bundle Android .AAB conforme (ARM64, Shaders précompilés, zéro permission abusive).",
            "Exportez aussi votre .APK de test direct et votre exécutable PC .EXE Vulkan.",
            "Suivez la checklist Play Console intégrée pour publier gratuitement sans frais OmniCore."
        ),
        tip = "Astuce : cochez « Shaders SPIR-V » et « Strip Debug » avant la compilation finale pour réduire la taille de votre jeu de 40 %.",
        targetTab = OmniCoreTab.JEUX_MODS
    ),
    TutorialStep(
        category = "Ponts & Apps",
        title = "9. Écosystème Ouvert, Pont Sans Fil & Sauvegarde",
        description = "Connectez toutes vos applications gratuitement et synchronisez votre station PC sans câble.",
        bullets = listOf(
            "Activez les connexions gratuites : Meshy, Tripo3D, Claude, Blockbench, Blender, Godot, Unity, GitHub et Play Console.",
            "Le Pont Sans Fil P2P (AES-256-GCM) délègue les calculs lourds à votre PC tout en gardant le contrôle tactile sur mobile.",
            "La connexion Google dans « Hub » ou « Apps » reste optionnelle : requise uniquement pour le Creator Hub et le Cloud."
        ),
        tip = "Astuce : vous pouvez aussi injecter des micro-solveurs personnalisés à chaud via la Méta-IA sans redémarrer OmniCore.",
        targetTab = OmniCoreTab.PARAMETRES_PONTS
    )
)

fun defaultAiProviders(): List<AiProvider> = listOf(
    AiProvider("ai_meshy", "Meshy 3D (0 Crédit)", "Maillages Smooth & PBR illimités", connected = true, isLocalOffline = true, badge = "0 CRÉDIT"),
    AiProvider("ai_tripo", "Tripo3D (0 Crédit)", "Conversion Image / Forme -> 3D", connected = true, isLocalOffline = true, badge = "0 CRÉDIT"),
    AiProvider("ai_blockbench", "Blockbench IA", "Blocs & Entités Minecraft (.bbmodel)", connected = true, isLocalOffline = true, badge = "GRATUIT"),
    AiProvider("ai_claude", "Claude Rig & Code", "Rigging live, Animations KT & Mods .mcaddon/.jar", connected = true, isLocalOffline = true, badge = "ILLIMITÉ"),
    AiProvider("app_blender", "Pont Blender / Maya", "Import & Export .blend / .fbx / .glb", connected = true, isLocalOffline = true, badge = "GRATUIT"),
    AiProvider("app_engines", "Godot / Unity / Unreal", "Export scène PBR + Squelette IK", connected = true, isLocalOffline = true, badge = "GRATUIT"),
    AiProvider("app_play", "Google Play Console", "Package .AAB signé prêt à publier", connected = true, isLocalOffline = false, badge = "0% FRAIS")
)

fun defaultBlockCatalog(): List<LogicBlock> = listOf(
    LogicBlock(
        id = "evt_detect",
        category = BlockCategory.EVENEMENTS,
        title = "Détection Joueur",
        paramSummary = "24m | Raycast",
        javaSnippet = "if (entity.canSeePlayer(24.0f)) {",
        pythonSnippet = "if scene.raycast_target('Player', max_dist=24.0):",
        glslSnippet = "float detectMask = step(length(uPlayerPos - vWorldPos), 24.0);",
        nodeTitle = "Event: Raycast"
    ),
    LogicBlock(
        id = "evt_tick",
        category = BlockCategory.EVENEMENTS,
        title = "Tick Physique 60Hz",
        paramSummary = "dt = 0.016s",
        javaSnippet = "@SubscribeEvent public void onTick(EntityTickEvent e) {",
        pythonSnippet = "def on_physics_step(dt: float):",
        glslSnippet = "uniform float uTime;",
        nodeTitle = "Event: Tick"
    ),
    LogicBlock(
        id = "cond_dist",
        category = BlockCategory.CONDITIONS,
        title = "Distance < 6.5m",
        paramSummary = "Seuil Proche",
        javaSnippet = "  if (distToTarget < 6.5d) {",
        pythonSnippet = "    if vector_dist(self_pos, target_pos) < 6.5:",
        glslSnippet = "  float crit = smoothstep(6.5, 1.0, distToCam);",
        nodeTitle = "Dist < 6.5m"
    ),
    LogicBlock(
        id = "cond_dark",
        category = BlockCategory.CONDITIONS,
        title = "Lumière <= 4",
        paramSummary = "Zone Sombre",
        javaSnippet = "  if (level.getMaxLocalRawBrightness(pos) <= 4) {",
        pythonSnippet = "    if world.sample_light(pos) <= 0.25:",
        glslSnippet = "  float darkMask = 1.0 - step(0.25, uAmbientLight);",
        nodeTitle = "Low Light"
    ),
    LogicBlock(
        id = "act_chase",
        category = BlockCategory.ACTIONS,
        title = "Poursuite NavMesh IK",
        paramSummary = "1.45x | Stride",
        javaSnippet = "    navigation.moveTo(target, 1.45D);\n    ikSolver.solveFootPlacement();",
        pythonSnippet = "        nav_agent.set_target(target_pos, speed=1.45)\n        ik_rig.update_stride()",
        glslSnippet = "  vec3 displaced = vPos + normal * sin(uTime * 8.0) * 0.03;",
        nodeTitle = "NavMesh IK"
    ),
    LogicBlock(
        id = "act_jumpscare",
        category = BlockCategory.ACTIONS,
        title = "Lock Caméra Jumpscare",
        paramSummary = "0.8s | Shake",
        javaSnippet = "    ClientPacket.sendJumpscareLock(player, 800);",
        pythonSnippet = "        camera.lock_orientation(entity.head_bone, duration=0.8)",
        glslSnippet = "  vec2 shake = vec2(sin(uTime * 90.0), cos(uTime * 75.0)) * 0.015;",
        nodeTitle = "Camera Lock"
    ),
    LogicBlock(
        id = "fx_fog",
        category = BlockCategory.FX,
        title = "Brouillard Volumétrique",
        paramSummary = "Densité 0.65",
        javaSnippet = "    FogRenderer.setDensity(0.65f, 0x0A0C0E);",
        pythonSnippet = "        volume.set_scattering(density=0.65, albedo=(0.04, 0.05, 0.06))",
        glslSnippet = "  float fogFactor = 1.0 - exp(-dist * 0.65);\n  fragColor = mix(fragColor, vec4(0.04, 0.05, 0.06, 1.0), fogFactor);",
        nodeTitle = "Volume Fog"
    ),
    LogicBlock(
        id = "fx_pulse",
        category = BlockCategory.FX,
        title = "Squash Cartoon Temmer",
        paramSummary = "Élastique 24fps",
        javaSnippet = "    RigAnim.applySquashStretch(mesh, 1.24f, 0.78f);",
        pythonSnippet = "        rig.set_squash_stretch(sx=1.24, sy=0.78, overshoot=0.18)",
        glslSnippet = "  vPos.y *= 1.0 + sin(uTime * 12.0) * 0.18;",
        nodeTitle = "Cartoon Squash"
    )
)

fun defaultIndustryFormats(): List<IndustryFormat> = listOf(
    IndustryFormat(".mcaddon", "Minecraft Bedrock", "Mod Pack"),
    IndustryFormat(".jar", "Minecraft Java Mod", "Forge/Fabric"),
    IndustryFormat(".bbmodel", "Blockbench", "Voxel/Entité"),
    IndustryFormat(".gltf", "GLTF 2.0", "Web / PBR"),
    IndustryFormat(".glb", "GLB Binary", "Packagé"),
    IndustryFormat(".obj", "Wavefront OBJ", "Maillage"),
    IndustryFormat(".fbx", "Autodesk FBX", "Animation"),
    IndustryFormat(".blend", "Blender", "Natif"),
    IndustryFormat(".prisma", "Prisma3D", "Mobile"),
    IndustryFormat(".stl", "STL Mesh", "CAD"),
    IndustryFormat(".aab", "Play Store Bundle", "Android"),
    IndustryFormat(".apk", "Android APK", "Mobile")
)

fun defaultDynamicAddons(): List<DynamicAddon> = listOf(
    DynamicAddon("addon_poly_fx", "PolyX3DParticles.kt", "Particules adaptatives par Shader", "v2.0", 240, true, 6),
    DynamicAddon("addon_fluid", "MetaFluidSolver60FPS.kt", "Méta-IA Fluide Temps Réel", "v1.1", 280, true, 8),
    DynamicAddon("addon_temmer", "CartoonSquashIK.kt", "Solveur Squash & Stretch Temmer", "v1.4", 196, true, 7)
)

fun defaultCommunityAssets(): List<CommunityAsset> = listOf(
    CommunityAsset(
        id = "asset_mech",
        title = "Mech MK-IV",
        author = "K. Vanecek",
        creditLicense = "CC-BY-4.0",
        category = "Meshes 3D",
        formatTag = "FBX / GLB",
        polyCount = "42.6K",
        verifiedByBots = true,
        thumbnailRes = R.drawable.img_asset_mech_1794673257798
    ),
    CommunityAsset(
        id = "asset_stalker",
        title = "Stalker Rigged",
        author = "M. Dvorak",
        creditLicense = "MIT Open",
        category = "Mods Horreur",
        formatTag = "MCADDON / JAR",
        polyCount = "28.1K",
        verifiedByBots = true,
        thumbnailRes = R.drawable.img_asset_horror_entity_1794673259292
    ),
    CommunityAsset(
        id = "asset_fortress",
        title = "Bastion Voxel",
        author = "Atelier_Nord",
        creditLicense = "CC0",
        category = "Meshes 3D",
        formatTag = "BBMODEL / OBJ",
        polyCount = "16.8K",
        verifiedByBots = true,
        thumbnailRes = R.drawable.img_asset_voxel_fortress_1794673260109
    ),
    CommunityAsset(
        id = "asset_ik_quad",
        title = "IK Quadrupède",
        author = "S. Takahashi",
        creditLicense = "Apache-2.0",
        category = "Rigs IK",
        formatTag = "GLTF",
        polyCount = "36 Bones",
        verifiedByBots = true,
        thumbnailRes = null
    ),
    CommunityAsset(
        id = "asset_fog_shader",
        title = "PolyX3D Shader Pack",
        author = "L. Moreau",
        creditLicense = "MIT",
        category = "Shaders",
        formatTag = "GLSL",
        polyCount = "12 Presets",
        verifiedByBots = true,
        thumbnailRes = null
    )
)

fun defaultSdkDocs(): List<SdkDocEntry> = listOf(
    SdkDocEntry(
        id = "sdk_scene",
        moduleName = "MeshyTripoBridge",
        summary = "Génération 3D illimitée (0 crédit) vers maillages Smooth PBR ou Blockbench.",
        endpointSignature = "omni.ai.generateMesh(engine: MESHY_3D | TRIPO_3D | BLOCKBENCH_IA): SceneNode",
        exampleCode = """
            val mesh = OmniAiEngine.generateModel3D(
                prompt = "Dragon mécanique ailé",
                engine = ModelingAiEngine.MESHY_3D
            )
            scene.insertMultiPart(mesh)
        """.trimIndent()
    ),
    SdkDocEntry(
        id = "sdk_claude_rig",
        moduleName = "ClaudeLiveRigAndAnim",
        summary = "Rigging en direct sur viewport + animations multi-styles (Cartoon KT, Réaliste, Horreur).",
        endpointSignature = "claude.rigAndAnimate(node, style: AnimationStyle): RigSolution",
        exampleCode = """
            val rig = OmniAiEngine.planClaudeRig(activeNode)
            val anim = OmniAiEngine.generateClaudeAnimation(
                prompt = "Saut joyeux élastique",
                style = AnimationStyle.CARTOON_TEMMER
            )
        """.trimIndent()
    ),
    SdkDocEntry(
        id = "sdk_mod",
        moduleName = "MinecraftPackagerSDK",
        summary = "Export réel ZIP .mcaddon (Bedrock), .jar (Java NeoForge/Fabric) et .bbmodel.",
        endpointSignature = "ModPackageExporter.writeZipArchive(files, outputStream)",
        exampleCode = """
            val files = OmniAiEngine.generateClaudeModProject(
                prompt = "Boss Stalker des cavernes",
                format = ModExportFormat.MCADDON,
                engine = ModelingAiEngine.BLOCKBENCH_IA
            )
            ModPackageExporter.writeZipArchive(files, uriOutputStream)
        """.trimIndent()
    ),
    SdkDocEntry(
        id = "sdk_play",
        moduleName = "FreePlayStoreBundler",
        summary = "Préparation complète Android App Bundle (.AAB) & APK sans frais de plateforme.",
        endpointSignature = "OmniBundler.exportPlayStoreAab(packageId, precompileShaders = true)",
        exampleCode = """
            val aab = OmniBundler.buildPlayBundle(
                packageId = "com.omnicore.creator.mygame",
                stripDebug = true
            )
        """.trimIndent()
    )
)
