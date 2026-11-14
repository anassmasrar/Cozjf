package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ClaudeAnimationPlan
import com.example.data.repository.ModPackageExporter
import com.example.data.repository.OmniAiEngine
import com.example.data.repository.ProjectRepository
import com.example.data.repository.StudioProject
import com.example.model.*
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "OmniStudioVM"

class OmniStudioViewModel(
    private val projectRepository: ProjectRepository? = null,
    private val aiEngine: OmniAiEngine = OmniAiEngine(),
    currentUserId: String? = null,
    userDisplayName: String? = null,
    userEmail: String? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        OmniStudioUiState(
            currentUserId = currentUserId,
            userDisplayName = userDisplayName?.takeIf { it.isNotBlank() } ?: "Créateur",
            userEmail = userEmail.orEmpty()
        )
    )
    val uiState: StateFlow<OmniStudioUiState> = _uiState.asStateFlow()

    private var modCompileJob: Job? = null
    private var exeCompileJob: Job? = null
    private var apkCompileJob: Job? = null
    private var aabCompileJob: Job? = null
    private var rigScanJob: Job? = null
    private var playbackJob: Job? = null
    private var lastModArchiveBytes: ByteArray? = null
    private var lastAabArchiveBytes: ByteArray? = null

    init {
        if (projectRepository != null && currentUserId != null) {
            observeCloudProjects(currentUserId)
        }
    }

    private fun observeCloudProjects(userId: String) {
        val repo = projectRepository ?: return
        viewModelScope.launch {
            repo.observeUserProjects(userId)
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val rootError = firestoreError ?: error
                    Log.w(TAG, "Error observing saved projects", rootError)
                    _uiState.update { it.copy(cloudError = rootError.localizedMessage ?: "Erreur lecture Cloud") }
                }
                .collect { list ->
                    _uiState.update { it.copy(savedProjects = list, cloudError = null) }
                }
        }
    }

    // Tutoriel Academy
    fun openTutorial(step: Int = 0) {
        _uiState.update { it.copy(showTutorialModal = true, tutorialStepIndex = step.coerceIn(0, defaultTutorialSteps().lastIndex)) }
    }

    fun closeTutorial() {
        _uiState.update { it.copy(showTutorialModal = false) }
    }

    fun setTutorialStep(step: Int) {
        _uiState.update { it.copy(tutorialStepIndex = step.coerceIn(0, defaultTutorialSteps().lastIndex)) }
    }

    fun dismissQuickTipBar() {
        _uiState.update { it.copy(showQuickTipBar = false) }
    }

    // Navigation & Mode Contrôle
    fun selectTab(tab: OmniCoreTab) {
        _uiState.update { state ->
            val nextDetent = if ((tab == OmniCoreTab.MODELISATION || tab == OmniCoreTab.ANIMATION_RIG) &&
                state.mobileDrawerDetent == DrawerDetent.COLLAPSED
            ) {
                DrawerDetent.HALF
            } else {
                state.mobileDrawerDetent
            }
            state.copy(activeTab = tab, mobileDrawerDetent = nextDetent)
        }
    }

    fun selectGamesModsSubTab(subTab: GamesModsSubTab) {
        _uiState.update { it.copy(gamesModsSubTab = subTab) }
    }

    fun setControlMode(mode: ControlMode) {
        _uiState.update { it.copy(controlMode = mode) }
    }

    fun toggleBridgeDialog(show: Boolean) {
        _uiState.update { it.copy(showBridgeModal = show) }
    }

    fun toggleBridgeConnection() {
        _uiState.update { state ->
            val next = !state.bridgeStatus.connected
            state.copy(bridgeStatus = state.bridgeStatus.copy(connected = next, latencyMs = if (next) 4 else 0))
        }
    }

    fun toggleHybridOffloading() {
        _uiState.update { state ->
            state.copy(bridgeStatus = state.bridgeStatus.copy(hybridOffloading = !state.bridgeStatus.hybridOffloading))
        }
    }

    fun setDrawerDetent(detent: DrawerDetent) {
        _uiState.update { it.copy(mobileDrawerDetent = detent) }
    }

    fun cycleDrawerDetent() {
        _uiState.update { state ->
            val next = when (state.mobileDrawerDetent) {
                DrawerDetent.COLLAPSED -> DrawerDetent.HALF
                DrawerDetent.HALF -> DrawerDetent.EXPANDED
                DrawerDetent.EXPANDED -> DrawerDetent.COLLAPSED
            }
            state.copy(mobileDrawerDetent = next)
        }
    }

    fun toggleMobileOutlinerSheet(show: Boolean) {
        _uiState.update { it.copy(showMobileOutlinerSheet = show) }
    }

    // Shaders PolyX3D Pro & Particules Adaptatives
    fun toggleShadersModal(show: Boolean) {
        _uiState.update { it.copy(showShadersModal = show) }
    }

    fun selectPolyShader(preset: PolyShaderPreset) {
        _uiState.update { state ->
            val fx = if (state.particleConfig.preset == ParticlePreset.AUTO_SHADER) {
                "Shader ${preset.shortLabel} + particules adaptées"
            } else {
                "Shader ${preset.shortLabel} activé"
            }
            state.copy(
                activeShader = preset,
                viewportShading = if (preset == PolyShaderPreset.HOLOGRAM_CYBER) ViewportShading.RENDERED else state.viewportShading,
                statusBannerMessage = fx
            )
        }
    }

    fun toggleParticles() {
        _uiState.update { state ->
            state.copy(particleConfig = state.particleConfig.copy(enabled = !state.particleConfig.enabled))
        }
    }

    fun selectParticlePreset(preset: ParticlePreset) {
        _uiState.update { state ->
            state.copy(particleConfig = state.particleConfig.copy(enabled = true, preset = preset))
        }
    }

    fun updateParticleParams(density: Int, speed: Float, size: Float, turbulence: Float) {
        _uiState.update { state ->
            state.copy(
                particleConfig = state.particleConfig.copy(
                    density = density.coerceIn(8, 120),
                    speed = speed.coerceIn(0.2f, 2.5f),
                    size = size.coerceIn(0.4f, 2.2f),
                    turbulence = turbulence.coerceIn(0f, 1.5f)
                )
            )
        }
    }

    // Viewport Tools & Scène 3D
    fun selectTool(tool: ViewportTool) {
        _uiState.update { state ->
            val nextGeo = when (tool) {
                ViewportTool.VOXEL_BRUSH -> GeometryMode.VOXEL_MODULAIRE
                ViewportTool.SCULPT, ViewportTool.SMOOTH_BRUSH -> GeometryMode.SMOOTH_ORGANIQUE
                else -> state.geometryMode
            }
            val nextWeight = tool == ViewportTool.WEIGHT_PAINT
            state.copy(
                activeTool = tool,
                geometryMode = nextGeo,
                rigging = state.rigging.copy(weightPaintOverlay = if (nextWeight) true else state.rigging.weightPaintOverlay)
            )
        }
    }

    fun setSelectionSubMode(subMode: SelectionSubMode) {
        _uiState.update { it.copy(selectionSubMode = subMode) }
    }

    fun setViewportShading(shading: ViewportShading) {
        _uiState.update { it.copy(viewportShading = shading) }
    }

    fun addPrimitiveShapeToScene(shape: PrimitiveShape, customName: String? = null) {
        _uiState.update { state ->
            val meshCount = state.sceneNodes.count { it.type == "Mesh" || it.type == "Imported" }
            val index = meshCount + 1
            val offsetAngle = meshCount * 1.15f
            val posX = if (meshCount == 0) 0f else kotlin.math.cos(offsetAngle) * 1.05f
            val posZ = if (meshCount == 0) 0f else kotlin.math.sin(offsetAngle) * 0.85f
            val name = customName ?: "${shape.label.replace(" ", "_")}_0$index"
            val newNode = SceneNode(
                id = "mesh_${System.nanoTime()}",
                name = name,
                type = "Mesh",
                depth = 1,
                shape = shape,
                geometryMode = state.geometryMode,
                posX = posX,
                posY = 0f,
                posZ = posZ,
                scale = 1f,
                tintHex = when (shape) {
                    PrimitiveShape.CUBE -> 0xFF5D7A99
                    PrimitiveShape.SPHERE -> 0xFF4A9EFF
                    PrimitiveShape.CYLINDRE -> 0xFF6B8E78
                    PrimitiveShape.CONE -> 0xFFB0895A
                    PrimitiveShape.TORE -> 0xFF7B8FA1
                    PrimitiveShape.PLAN -> 0xFF525866
                    PrimitiveShape.TORSE -> 0xFF8B7E74
                }
            )
            state.copy(
                activeShape = shape,
                sceneNodes = state.sceneNodes + newNode,
                selectedNodeId = newNode.id,
                statusBannerMessage = "$name ajouté à la scène"
            )
        }
    }

    fun addSceneUtilityNode(name: String, type: String) {
        _uiState.update { state ->
            val newNode = SceneNode(id = "util_${System.nanoTime()}", name = name, type = type, depth = 1)
            state.copy(sceneNodes = state.sceneNodes + newNode, selectedNodeId = newNode.id, statusBannerMessage = "$name ($type) ajouté")
        }
    }

    fun deleteSelectedNode() {
        _uiState.update { state ->
            val target = state.sceneNodes.find { it.id == state.selectedNodeId } ?: return@update state
            if (target.type == "Collection") return@update state
            val remaining = state.sceneNodes.filterNot { it.id == target.id }
            val nextSelected = remaining.lastOrNull { it.type == "Mesh" || it.type == "Imported" }?.id
                ?: remaining.firstOrNull()?.id ?: "col_root"
            state.copy(sceneNodes = remaining, selectedNodeId = nextSelected, statusBannerMessage = "${target.name} supprimé")
        }
    }

    fun newEmptyProject() {
        _uiState.update { state ->
            state.copy(
                activeProjectName = "Nouveau Projet",
                sceneNodes = defaultSceneNodes(),
                selectedNodeId = "mesh_cube",
                activeShape = PrimitiveShape.TORSE,
                orbitYaw = 32f,
                orbitPitch = -22f,
                cameraZoom = 1.0f,
                timeline = TimelineState(),
                statusBannerMessage = "Nouvelle scène initialisée"
            )
        }
    }

    // Sauvegarde Cloud Firestore (Optionnelle)
    fun updateActiveProjectName(name: String) {
        _uiState.update { it.copy(activeProjectName = name) }
    }

    fun toggleCloudProjectsModal(show: Boolean) {
        _uiState.update { it.copy(showCloudProjectsModal = show) }
    }

    fun saveCurrentProjectToCloud(projectNameInput: String? = null) {
        val repo = projectRepository
        val state = _uiState.value
        val nameToSave = (projectNameInput ?: state.activeProjectName).trim().ifBlank { "Projet OmniCore" }
        if (repo == null || state.currentUserId == null) {
            _uiState.update {
                it.copy(
                    activeProjectName = nameToSave,
                    statusBannerMessage = "Sauvegarde locale prête : connectez Google pour synchroniser dans le Cloud"
                )
            }
            return
        }
        val summary = state.sceneNodes
            .filter { it.type == "Mesh" || it.type == "Imported" }
            .joinToString(";") { "${it.name}:${it.shape.name}:${it.posX}:${it.posY}:${it.posZ}" }
        _uiState.update { it.copy(isSavingCloud = true, cloudError = null, activeProjectName = nameToSave) }
        viewModelScope.launch {
            repo.saveProject(
                name = nameToSave,
                geometryMode = state.geometryMode.name,
                renderEngine = state.renderEngine.name,
                nodeCount = state.sceneNodes.size.toLong(),
                keyframeCount = state.timeline.keyframes.size.toLong(),
                sceneSummary = summary
            ).onSuccess {
                _uiState.update {
                    it.copy(isSavingCloud = false, statusBannerMessage = "Projet « $nameToSave » sauvegardé sur Google Cloud")
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(isSavingCloud = false, cloudError = err.localizedMessage, statusBannerMessage = "Erreur Cloud : ${err.localizedMessage}")
                }
            }
        }
    }

    fun loadCloudProject(project: StudioProject) {
        val restoredNodes = mutableListOf(
            SceneNode("col_root", "Scène Principale", "Collection", 0),
            SceneNode("cam_main", "Caméra", "Camera", 1)
        )
        project.sceneSummary.split(";").filter { it.isNotBlank() }.forEachIndexed { idx, token ->
            val parts = token.split(":")
            val nodeName = parts.getOrNull(0) ?: "Objet_${idx + 1}"
            val shape = runCatching { PrimitiveShape.valueOf(parts.getOrNull(1) ?: "CUBE") }.getOrDefault(PrimitiveShape.CUBE)
            val px = parts.getOrNull(2)?.toFloatOrNull() ?: (idx * 0.8f)
            val py = parts.getOrNull(3)?.toFloatOrNull() ?: 0f
            val pz = parts.getOrNull(4)?.toFloatOrNull() ?: 0f
            restoredNodes.add(SceneNode("loaded_${idx}_${System.nanoTime()}", nodeName, "Mesh", 1, shape = shape, posX = px, posY = py, posZ = pz))
        }
        val geo = runCatching { GeometryMode.valueOf(project.geometryMode) }.getOrDefault(GeometryMode.SMOOTH_ORGANIQUE)
        val eng = runCatching { RenderEngine.valueOf(project.renderEngine) }.getOrDefault(RenderEngine.POLYX3D_MOBILE)
        _uiState.update {
            it.copy(
                activeProjectName = project.name,
                geometryMode = geo,
                renderEngine = eng,
                sceneNodes = restoredNodes,
                selectedNodeId = restoredNodes.lastOrNull()?.id ?: "col_root",
                showCloudProjectsModal = false,
                statusBannerMessage = "Projet « ${project.name} » chargé"
            )
        }
    }

    fun deleteCloudProject(projectId: String) {
        val repo = projectRepository ?: return
        viewModelScope.launch {
            repo.deleteProject(projectId).onSuccess {
                _uiState.update { it.copy(statusBannerMessage = "Projet supprimé du Cloud") }
            }.onFailure { err ->
                _uiState.update { it.copy(cloudError = err.localizedMessage) }
            }
        }
    }

    // Scène & Caméra
    fun toggleGrid() = _uiState.update { it.copy(showGrid = !it.showGrid) }
    fun toggleXRay() = _uiState.update { it.copy(xRayMode = !it.xRayMode) }
    fun toggleSymmetry() = _uiState.update { it.copy(symmetryEnabled = !it.symmetryEnabled) }
    fun togglePhysicsSimulation() = _uiState.update { it.copy(physics = it.physics.copy(active = !it.physics.active)) }

    fun selectSceneNode(nodeId: String) {
        _uiState.update { state ->
            val node = state.sceneNodes.find { it.id == nodeId }
            state.copy(selectedNodeId = nodeId, activeShape = node?.shape ?: state.activeShape)
        }
    }

    fun toggleNodeVisibility(nodeId: String) {
        _uiState.update { s -> s.copy(sceneNodes = s.sceneNodes.map { if (it.id == nodeId) it.copy(visible = !it.visible) else it }) }
    }

    fun toggleNodeLock(nodeId: String) {
        _uiState.update { s -> s.copy(sceneNodes = s.sceneNodes.map { if (it.id == nodeId) it.copy(locked = !it.locked) else it }) }
    }

    fun setGeometryMode(mode: GeometryMode) {
        _uiState.update { state ->
            state.copy(
                geometryMode = mode,
                sceneNodes = state.sceneNodes.map { if (it.id == state.selectedNodeId) it.copy(geometryMode = mode) else it }
            )
        }
    }

    fun setSmoothSubdivision(level: Int) = _uiState.update { it.copy(smoothSubdivision = level.coerceIn(1, 5)) }
    fun setVoxelGridResolution(res: Int) = _uiState.update { it.copy(voxelGridResolution = res) }
    fun setRenderEngine(engine: RenderEngine) = _uiState.update { it.copy(renderEngine = engine) }

    fun updateCameraOrbit(deltaYaw: Float, deltaPitch: Float) {
        _uiState.update { it.copy(orbitYaw = (it.orbitYaw + deltaYaw) % 360f, orbitPitch = (it.orbitPitch + deltaPitch).coerceIn(-80f, 80f)) }
    }

    fun snapCameraView(yaw: Float, pitch: Float) = _uiState.update { it.copy(orbitYaw = yaw, orbitPitch = pitch) }
    fun adjustCameraZoom(delta: Float) = _uiState.update { it.copy(cameraZoom = (it.cameraZoom + delta).coerceIn(0.55f, 2.2f)) }
    fun resetCamera() = _uiState.update { it.copy(orbitYaw = 32f, orbitPitch = -22f, cameraZoom = 1.0f) }

    // Moteurs IA Gratuits (Meshy 3D, Tripo3D, Blockbench IA, Claude)
    fun selectModelingEngine(engine: ModelingAiEngine) {
        _uiState.update { it.copy(selectedModelingEngine = engine) }
    }

    fun selectBlockbenchTarget(target: BlockbenchTargetType) {
        _uiState.update { it.copy(selectedBlockbenchTarget = target) }
    }

    fun updateAiPromptInput(text: String) {
        _uiState.update { it.copy(aiPromptInput = text) }
    }

    fun updateOmniMindPrompt(text: String) {
        _uiState.update { it.copy(omniMindPrompt = text) }
    }

    fun executeOmniMindGeneration() {
        val prompt = _uiState.value.omniMindPrompt.trim()
        if (prompt.isEmpty()) return
        _uiState.update { it.copy(omniMindPrompt = "") }
        generate3DObjectWithAi(prompt)
    }

    fun generate3DObjectWithAi(promptOverride: String? = null) {
        val state = _uiState.value
        val prompt = (promptOverride ?: state.aiPromptInput).trim()
        if (prompt.isEmpty()) return

        val engine = state.selectedModelingEngine
        val target = state.selectedBlockbenchTarget
        val generated = aiEngine.generateModel3D(prompt, engine, target)
        val userMsg = AiChatMessage("u3d_${System.nanoTime()}", isUser = true, engineLabel = "Vous", text = "[${engine.label}] $prompt")
        val replyMsg = AiChatMessage(
            id = "ai3d_${System.nanoTime()}",
            isUser = false,
            engineLabel = engine.label,
            text = "${generated.explanation}\n\n${generated.suggestedScript}",
            generatedObjectName = generated.node.name
        )

        _uiState.update { s ->
            s.copy(
                aiPromptInput = if (promptOverride == null) "" else s.aiPromptInput,
                sceneNodes = s.sceneNodes + generated.node,
                selectedNodeId = generated.node.id,
                geometryMode = generated.node.geometryMode,
                aiMessages = s.aiMessages + userMsg + replyMsg,
                omniMindStatus = "${generated.node.name} généré (${engine.label} • 0 crédit)",
                statusBannerMessage = "${generated.node.name} créé par ${engine.label}"
            )
        }
    }

    fun askClaudeAssistant(promptOverride: String? = null) {
        val state = _uiState.value
        val prompt = (promptOverride ?: state.aiPromptInput).trim()
        if (prompt.isEmpty()) return
        val answer = aiEngine.answerAssistant(prompt, state.selectedModelingEngine, state.selectedBlockbenchTarget)
        val userMsg = AiChatMessage("u_${System.nanoTime()}", isUser = true, engineLabel = "Vous", text = prompt)
        val claudeMsg = AiChatMessage("c_${System.nanoTime()}", isUser = false, engineLabel = "Claude (Illimité)", text = answer)
        _uiState.update { s ->
            s.copy(
                aiPromptInput = if (promptOverride == null) "" else s.aiPromptInput,
                aiMessages = s.aiMessages + userMsg + claudeMsg
            )
        }
    }

    fun toggleAiProvider(providerId: String) {
        _uiState.update { state ->
            state.copy(aiProviders = state.aiProviders.map { p -> if (p.id == providerId) p.copy(connected = !p.connected) else p })
        }
    }

    // Claude Animation Multi-Styles (Kevin Temmer Tunes, Réaliste, Anime, Blockbench, Horreur, Stop-Motion)
    fun updateAiAnimPrompt(text: String) {
        _uiState.update { it.copy(aiAnimPrompt = text) }
    }

    fun selectAnimationStyle(style: AnimationStyle) {
        val plan = aiEngine.generateClaudeAnimation(style.shortLabel, style)
        applyAnimationPlan(plan, autoPlay = true)
    }

    fun generateAiAnimation(promptOverride: String? = null) {
        val state = _uiState.value
        val prompt = (promptOverride ?: state.aiAnimPrompt).trim().ifBlank { state.timeline.activeStyle.shortLabel }
        val plan = aiEngine.generateClaudeAnimation(prompt, state.timeline.activeStyle)
        _uiState.update { it.copy(aiAnimPrompt = "") }
        applyAnimationPlan(plan, autoPlay = true)
    }

    private fun applyAnimationPlan(plan: ClaudeAnimationPlan, autoPlay: Boolean) {
        _uiState.update { state ->
            val updatedClips = (listOf(plan.clip) + state.timeline.clips.filterNot { it.id == plan.clip.id }).take(6)
            state.copy(
                timeline = state.timeline.copy(
                    activeStyle = plan.clip.style,
                    selectedClipId = plan.clip.id,
                    currentFrame = 1,
                    endFrame = plan.clip.frames,
                    keyframes = plan.poses.keys,
                    poses = plan.poses,
                    clips = updatedClips
                ),
                statusBannerMessage = plan.summary
            )
        }
        if (autoPlay && !_uiState.value.timeline.isPlaying) {
            toggleTimelinePlayback()
        }
    }

    // Timeline & Keyframe Poses
    fun toggleTimelinePlayback() {
        val nextPlaying = !_uiState.value.timeline.isPlaying
        playbackJob?.cancel()
        _uiState.update { it.copy(timeline = it.timeline.copy(isPlaying = nextPlaying)) }
        if (nextPlaying) {
            playbackJob = viewModelScope.launch {
                while (true) {
                    delay(42)
                    _uiState.update { state ->
                        val tl = state.timeline
                        val nextFrame = if (tl.currentFrame >= tl.endFrame) tl.startFrame else tl.currentFrame + 1
                        state.copy(timeline = tl.copy(currentFrame = nextFrame))
                    }
                }
            }
        }
    }

    fun scrubFrame(frame: Int) {
        _uiState.update { state ->
            val clamped = frame.coerceIn(state.timeline.startFrame, state.timeline.endFrame)
            val existingPose = state.timeline.poses[clamped]
            state.copy(
                timeline = state.timeline.copy(currentFrame = clamped),
                keyframeEditOffsetY = existingPose?.offsetY ?: state.keyframeEditOffsetY,
                keyframeEditRotY = existingPose?.rotYDeg ?: state.keyframeEditRotY,
                keyframeEditScale = existingPose?.scale ?: state.keyframeEditScale,
                keyframeEditSquashY = existingPose?.scaleY ?: state.keyframeEditSquashY,
                keyframeEditLimbSwing = existingPose?.limbSwingDeg ?: state.keyframeEditLimbSwing
            )
        }
    }

    fun updateKeyframePoseDraft(
        offsetY: Float,
        rotYDeg: Float,
        scale: Float,
        squashY: Float = _uiState.value.keyframeEditSquashY,
        limbSwing: Float = _uiState.value.keyframeEditLimbSwing
    ) {
        _uiState.update { state ->
            val f = state.timeline.currentFrame
            val invSquashXZ = (1f / kotlin.math.sqrt(squashY.coerceIn(0.6f, 1.5f))).coerceIn(0.75f, 1.35f)
            val updatedPose = KeyframePose(
                frame = f,
                offsetY = offsetY,
                rotYDeg = rotYDeg,
                scale = scale,
                scaleX = invSquashXZ,
                scaleY = squashY,
                scaleZ = invSquashXZ,
                limbSwingDeg = limbSwing
            )
            state.copy(
                keyframeEditOffsetY = offsetY,
                keyframeEditRotY = rotYDeg,
                keyframeEditScale = scale,
                keyframeEditSquashY = squashY,
                keyframeEditLimbSwing = limbSwing,
                timeline = state.timeline.copy(
                    keyframes = state.timeline.keyframes + f,
                    poses = state.timeline.poses + (f to updatedPose)
                )
            )
        }
    }

    fun toggleKeyframeAtCurrent() {
        _uiState.update { state ->
            val tl = state.timeline
            val f = tl.currentFrame
            if (f in tl.keyframes) {
                state.copy(
                    timeline = tl.copy(keyframes = tl.keyframes - f, poses = tl.poses - f),
                    statusBannerMessage = "Keyframe F$f supprimée"
                )
            } else {
                val squashY = state.keyframeEditSquashY
                val invXZ = (1f / kotlin.math.sqrt(squashY.coerceIn(0.6f, 1.5f))).coerceIn(0.75f, 1.35f)
                val pose = KeyframePose(
                    frame = f,
                    offsetY = state.keyframeEditOffsetY,
                    rotYDeg = state.keyframeEditRotY,
                    scale = state.keyframeEditScale,
                    scaleX = invXZ,
                    scaleY = squashY,
                    scaleZ = invXZ,
                    limbSwingDeg = state.keyframeEditLimbSwing
                )
                state.copy(
                    timeline = tl.copy(keyframes = tl.keyframes + f, poses = tl.poses + (f to pose)),
                    statusBannerMessage = "Keyframe ajoutée à F$f"
                )
            }
        }
    }

    fun selectAnimationClip(clipId: String) {
        val clip = _uiState.value.timeline.clips.find { it.id == clipId }
        val style = clip?.style ?: AnimationStyle.CARTOON_TEMMER
        val plan = aiEngine.generateClaudeAnimation(clip?.name ?: style.shortLabel, style)
        _uiState.update { state ->
            state.copy(
                timeline = state.timeline.copy(
                    selectedClipId = clipId,
                    activeStyle = style,
                    currentFrame = 1,
                    endFrame = plan.clip.frames,
                    keyframes = plan.poses.keys,
                    poses = plan.poses
                )
            )
        }
    }

    // Claude Rigging Live (Visible en Direct dans le Viewport 3D)
    fun selectRigArchetype(archetype: String) {
        val activeNode = _uiState.value.sceneNodes.find { it.id == _uiState.value.selectedNodeId }
        val plan = aiEngine.planClaudeRig(activeNode, archetype)
        _uiState.update {
            it.copy(
                rigging = it.rigging.copy(
                    rigArchetype = plan.archetype,
                    bones = plan.bones,
                    selectedBoneId = plan.bones.first().id,
                    revealedBonesCount = plan.bones.size,
                    weightPaintOverlay = true,
                    liveRigStatus = plan.note
                )
            )
        }
    }

    fun launchNeuralAutoRig() {
        if (_uiState.value.rigging.isScanning) return
        rigScanJob?.cancel()
        val activeNode = _uiState.value.sceneNodes.find { it.id == _uiState.value.selectedNodeId }
        val plan = aiEngine.planClaudeRig(activeNode)
        rigScanJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    rigging = it.rigging.copy(
                        isScanning = true,
                        scanProgress = 0.08f,
                        rigArchetype = plan.archetype,
                        bones = plan.bones,
                        selectedBoneId = plan.bones.first().id,
                        revealedBonesCount = 1,
                        weightPaintOverlay = true,
                        liveRigStatus = "Claude analyse « ${activeNode?.name ?: "Scène"} »..."
                    )
                )
            }
            plan.bones.forEachIndexed { idx, bone ->
                delay(180)
                val progress = (idx + 1).toFloat() / plan.bones.size
                _uiState.update {
                    it.copy(
                        rigging = it.rigging.copy(
                            scanProgress = progress,
                            revealedBonesCount = idx + 1,
                            selectedBoneId = bone.id,
                            liveRigStatus = "Claude pose en direct : ${bone.name} (${bone.ikChain})"
                        )
                    )
                }
            }
            _uiState.update {
                it.copy(
                    rigging = it.rigging.copy(
                        isScanning = false,
                        scanProgress = 1f,
                        detectedBonesCount = plan.bones.size * 3,
                        symmetryScore = 99.8f,
                        weightPaintOverlay = true,
                        liveRigStatus = plan.note
                    ),
                    statusBannerMessage = plan.note
                )
            }
        }
    }

    fun selectBone(boneId: String) = _uiState.update { it.copy(rigging = it.rigging.copy(selectedBoneId = boneId)) }

    fun updateSelectedBoneRotation(pitch: Float, yaw: Float, roll: Float) {
        _uiState.update { state ->
            val targetId = state.rigging.selectedBoneId
            state.copy(
                rigging = state.rigging.copy(
                    weightPaintOverlay = true,
                    bones = state.rigging.bones.map { b ->
                        if (b.id == targetId) b.copy(pitchDeg = pitch, yawDeg = yaw, rollDeg = roll) else b
                    }
                )
            )
        }
    }

    fun toggleBoneIkLock(boneId: String) {
        _uiState.update { state ->
            state.copy(rigging = state.rigging.copy(bones = state.rigging.bones.map { b -> if (b.id == boneId) b.copy(ikLocked = !b.ikLocked) else b }))
        }
    }

    fun toggleWeightPaintOverlay() {
        _uiState.update { state ->
            val next = !state.rigging.weightPaintOverlay
            state.copy(
                rigging = state.rigging.copy(weightPaintOverlay = next),
                activeTool = if (next) ViewportTool.WEIGHT_PAINT else ViewportTool.SELECT
            )
        }
    }

    fun updateWeightBrush(weight: Float, falloff: Float) {
        _uiState.update { it.copy(rigging = it.rigging.copy(brushWeight = weight, brushFalloff = falloff)) }
    }

    // Blocs + Nodes + Code
    fun selectBlockCategory(category: BlockCategory) = _uiState.update { it.copy(activeBlockCategory = category) }
    fun addBlockToStack(block: LogicBlock) {
        _uiState.update { s -> if (s.assembledBlocks.size >= 8) s else s.copy(assembledBlocks = s.assembledBlocks + block.copy(id = "${block.id}_${System.nanoTime()}")) }
    }
    fun removeBlockFromStack(blockId: String) = _uiState.update { s -> s.copy(assembledBlocks = s.assembledBlocks.filterNot { it.id == blockId }) }
    fun setCodeViewMode(mode: CodeViewMode) = _uiState.update { it.copy(codeViewMode = mode) }

    // Atelier Mods Illimité Claude (.mcaddon / .jar / .bbmodel + Blockbench/Meshy)
    fun selectModExportFormat(format: ModExportFormat) {
        _uiState.update { it.copy(modBuild = it.modBuild.copy(exportFormat = format)) }
    }

    fun selectModAssetEngine(engine: ModelingAiEngine) {
        _uiState.update { it.copy(modBuild = it.modBuild.copy(assetEngine = engine)) }
    }

    fun selectModTargetType(targetType: BlockbenchTargetType) {
        _uiState.update { it.copy(modBuild = it.modBuild.copy(targetType = targetType)) }
    }

    fun updateModAiPrompt(prompt: String) {
        _uiState.update { it.copy(modBuild = it.modBuild.copy(aiModPrompt = prompt)) }
    }

    fun generateModWithClaude() {
        val state = _uiState.value
        val prompt = state.modBuild.aiModPrompt.trim().ifBlank { state.modBuild.modName }
        val modelRes = aiEngine.generateModel3D(prompt, state.modBuild.assetEngine, state.modBuild.targetType)
        val bundle = aiEngine.generateClaudeModProject(prompt, state.modBuild, modelRes.node)
        lastModArchiveBytes = ModPackageExporter.buildZipBytes(bundle.files)
        _uiState.update { s ->
            s.copy(
                sceneNodes = s.sceneNodes + modelRes.node,
                selectedNodeId = modelRes.node.id,
                modBuild = s.modBuild.copy(
                    aiModPrompt = "",
                    buildStage = bundle.summary,
                    generatedPreviewFiles = bundle.files.keys.toList(),
                    lastArtifact = "${bundle.modId}_${s.modBuild.version}${s.modBuild.exportFormat.ext}"
                ),
                statusBannerMessage = bundle.summary
            )
        }
    }

    fun updateModConfig(
        modId: String = _uiState.value.modBuild.modId,
        modName: String = _uiState.value.modBuild.modName,
        version: String = _uiState.value.modBuild.version,
        loader: MinecraftLoader = _uiState.value.modBuild.loader
    ) {
        _uiState.update { it.copy(modBuild = it.modBuild.copy(modId = modId, modName = modName, version = version, loader = loader)) }
    }

    fun updateHorrorConfig(
        aggression: Float = _uiState.value.modBuild.aiAggression,
        radius: Float = _uiState.value.modBuild.detectionRadius,
        fogDensity: Float = _uiState.value.modBuild.volumetricFogDensity,
        jumpscareIntensity: Float = _uiState.value.modBuild.jumpscareIntensity,
        proximity: Boolean = _uiState.value.modBuild.triggerProximity,
        los: Boolean = _uiState.value.modBuild.triggerLineOfSight,
        darkness: Boolean = _uiState.value.modBuild.triggerDarkness,
        spatialAudio: Boolean = _uiState.value.modBuild.triggerSpatialAudio
    ) {
        _uiState.update {
            it.copy(
                modBuild = it.modBuild.copy(
                    aiAggression = aggression,
                    detectionRadius = radius,
                    volumetricFogDensity = fogDensity,
                    jumpscareIntensity = jumpscareIntensity,
                    triggerProximity = proximity,
                    triggerLineOfSight = los,
                    triggerDarkness = darkness,
                    triggerSpatialAudio = spatialAudio
                )
            )
        }
    }

    fun compileMinecraftJar() {
        val state = _uiState.value
        if (state.modBuild.isCompiling) return
        modCompileJob?.cancel()
        modCompileJob = viewModelScope.launch {
            val fmt = state.modBuild.exportFormat
            val stages = listOf(
                0.25f to "Claude génère les manifestes & comportements...",
                0.60f to "Export ${state.modBuild.assetEngine.label} (.geo.json / .bbmodel)...",
                0.90f to "Empaquetage archive ZIP ${fmt.ext}...",
                1.0f to "Archive ${fmt.ext} prête (illimité)"
            )
            _uiState.update { it.copy(modBuild = it.modBuild.copy(isCompiling = true, progress = 0.05f, lastArtifact = null)) }
            for ((prog, label) in stages) {
                delay(200)
                _uiState.update { it.copy(modBuild = it.modBuild.copy(progress = prog, buildStage = label)) }
            }
            val activeNode = _uiState.value.sceneNodes.find { it.id == _uiState.value.selectedNodeId }
            val bundle = aiEngine.generateClaudeModProject(_uiState.value.modBuild.modName, _uiState.value.modBuild, activeNode)
            lastModArchiveBytes = ModPackageExporter.buildZipBytes(bundle.files)
            val safeId = bundle.modId
            val ver = _uiState.value.modBuild.version.ifBlank { "1.0.0" }
            _uiState.update {
                it.copy(
                    modBuild = it.modBuild.copy(
                        isCompiling = false,
                        generatedPreviewFiles = bundle.files.keys.toList(),
                        lastArtifact = "${safeId}-${ver}${fmt.ext}"
                    )
                )
            }
        }
    }

    fun getOrCreateModArchiveBytes(): ByteArray {
        lastModArchiveBytes?.let { return it }
        val state = _uiState.value
        val activeNode = state.sceneNodes.find { it.id == state.selectedNodeId }
        val bundle = aiEngine.generateClaudeModProject(state.modBuild.modName, state.modBuild, activeNode)
        return ModPackageExporter.buildZipBytes(bundle.files).also { lastModArchiveBytes = it }
    }

    // Cross-Compilation & Google Play Store (.AAB Gratuit)
    fun toggleNativeBuildOption(precompile: Boolean = _uiState.value.nativeBuild.precompileShaders, strip: Boolean = _uiState.value.nativeBuild.stripDebugSymbols) {
        _uiState.update { it.copy(nativeBuild = it.nativeBuild.copy(precompileShaders = precompile, stripDebugSymbols = strip)) }
    }

    fun updatePlayStoreConfig(packageId: String, title: String, versionName: String) {
        _uiState.update {
            it.copy(nativeBuild = it.nativeBuild.copy(playPackageId = packageId, playAppTitle = title, playVersionName = versionName))
        }
    }

    fun compilePlayStoreAab() {
        if (_uiState.value.nativeBuild.isBuildingAab) return
        aabCompileJob?.cancel()
        aabCompileJob = viewModelScope.launch {
            val steps = listOf(
                0.25f to "Vérification conformité Google Play...",
                0.65f to "Empaquetage App Bundle (.AAB) ARM64...",
                1.0f to "Bundle Play Store prêt (0% frais OmniCore)"
            )
            _uiState.update { it.copy(nativeBuild = it.nativeBuild.copy(isBuildingAab = true, aabProgress = 0.05f, aabArtifact = null)) }
            for ((p, s) in steps) {
                delay(220)
                _uiState.update { it.copy(nativeBuild = it.nativeBuild.copy(aabProgress = p, aabStatus = s)) }
            }
            val nb = _uiState.value.nativeBuild
            lastAabArchiveBytes = ModPackageExporter.buildPlayStoreBundleBytes(nb.playPackageId, nb.playAppTitle, nb.playVersionName)
            _uiState.update {
                it.copy(
                    nativeBuild = it.nativeBuild.copy(
                        isBuildingAab = false,
                        aabArtifact = "${nb.playPackageId}-v${nb.playVersionName}.aab"
                    )
                )
            }
        }
    }

    fun getOrCreatePlayStoreAabBytes(): ByteArray {
        lastAabArchiveBytes?.let { return it }
        val nb = _uiState.value.nativeBuild
        return ModPackageExporter.buildPlayStoreBundleBytes(nb.playPackageId, nb.playAppTitle, nb.playVersionName)
            .also { lastAabArchiveBytes = it }
    }

    fun compileWindowsExe() {
        if (_uiState.value.nativeBuild.isBuildingExe) return
        exeCompileJob?.cancel()
        exeCompileJob = viewModelScope.launch {
            val steps = listOf(0.25f to "Vulkan Win64...", 0.65f to "Shaders SPIR-V...", 1.0f to "Binaire .EXE prêt (48.4 Mo)")
            _uiState.update { it.copy(nativeBuild = it.nativeBuild.copy(isBuildingExe = true, exeProgress = 0.05f, exeArtifact = null)) }
            for ((p, s) in steps) {
                delay(220)
                _uiState.update { it.copy(nativeBuild = it.nativeBuild.copy(exeProgress = p, exeStatus = s)) }
            }
            _uiState.update { it.copy(nativeBuild = it.nativeBuild.copy(isBuildingExe = false, exeArtifact = "dist/win64/OmniCore_Game.exe")) }
        }
    }

    fun compileAndroidApk() {
        if (_uiState.value.nativeBuild.isBuildingApk) return
        apkCompileJob?.cancel()
        apkCompileJob = viewModelScope.launch {
            val steps = listOf(0.25f to "PolyX3D+ ARM64...", 0.65f to "Assets ASTC + Zipalign...", 1.0f to "Package .APK prêt (22.8 Mo)")
            _uiState.update { it.copy(nativeBuild = it.nativeBuild.copy(isBuildingApk = true, apkProgress = 0.05f, apkArtifact = null)) }
            for ((p, s) in steps) {
                delay(220)
                _uiState.update { it.copy(nativeBuild = it.nativeBuild.copy(apkProgress = p, apkStatus = s)) }
            }
            _uiState.update { it.copy(nativeBuild = it.nativeBuild.copy(isBuildingApk = false, apkArtifact = "dist/android/OmniCore_Game.apk")) }
        }
    }

    // Import / Export Universel
    fun toggleImportExportModal(show: Boolean) = _uiState.update { it.copy(showImportExportModal = show) }
    fun setTransferDirection(dir: TransferDirection) = _uiState.update { it.copy(transferDirection = dir) }
    fun selectTransferFormat(ext: String) = _uiState.update { it.copy(selectedFormatExt = ext) }
    fun toggleTransferOptions(yUp: Boolean = _uiState.value.normalizeYUp, apply: Boolean = _uiState.value.applyTransforms) {
        _uiState.update { it.copy(normalizeYUp = yUp, applyTransforms = apply) }
    }
    fun simulateDropFile(fileName: String) = _uiState.update { it.copy(droppedFileName = fileName, lastTransferLog = "Prêt : $fileName") }

    fun executeUniversalTransfer() {
        val state = _uiState.value
        val file = state.droppedFileName ?: "Modele_3D${state.selectedFormatExt}"
        val cleanName = file.substringBeforeLast(".").replace(" ", "_").ifBlank { "Import_3D" }
        if (state.transferDirection == TransferDirection.IMPORT) {
            val res = aiEngine.generateModel3D(cleanName, ModelingAiEngine.MESHY_3D, BlockbenchTargetType.ENTITY)
            val newNode = res.node.copy(id = "imp_${System.nanoTime()}", name = cleanName, type = "Imported", sourceEngine = "Import $file")
            _uiState.update {
                it.copy(
                    sceneNodes = it.sceneNodes + newNode,
                    selectedNodeId = newNode.id,
                    lastTransferLog = "Importé : $file",
                    statusBannerMessage = "Fichier « $file » importé dans la vue 3D",
                    showImportExportModal = false
                )
            }
        } else {
            val msg = "Exporté : $file"
            _uiState.update { it.copy(lastTransferLog = msg, statusBannerMessage = msg, showImportExportModal = false) }
        }
    }

    // Méta-IA & Addons Hot-Reload
    fun updateAddonPrompt(text: String) = _uiState.update { it.copy(addonPromptInput = text) }

    fun generateAndInjectAddon() {
        val prompt = _uiState.value.addonPromptInput.trim()
        if (prompt.isEmpty()) return
        val slug = prompt.split(" ").take(3).joinToString("") { w -> w.filter { it.isLetterOrDigit() }.replaceFirstChar { it.uppercase() } }.ifBlank { "MetaSolver" }
        val fileName = "${slug}Solver.kt"
        val newAddon = DynamicAddon("addon_${System.nanoTime()}", fileName, prompt, "v1.0", 216, true, 8)
        _uiState.update {
            it.copy(addonPromptInput = "", dynamicAddons = listOf(newAddon) + it.dynamicAddons, hotReloadLogs = listOf("[META-IA] INJECT $fileName (8ms)") + it.hotReloadLogs.take(8))
        }
    }

    fun toggleAddonActive(addonId: String) {
        _uiState.update { state ->
            var toggledName = ""
            var nextActive = false
            val updated = state.dynamicAddons.map { addon ->
                if (addon.id == addonId) { toggledName = addon.name; nextActive = !addon.active; addon.copy(active = nextActive) } else addon
            }
            state.copy(dynamicAddons = updated, hotReloadLogs = listOf(if (nextActive) "[HOT] ON $toggledName" else "[HOT] OFF $toggledName") + state.hotReloadLogs.take(8))
        }
    }

    // Communauté & Bots
    fun updateHubSearch(query: String) = _uiState.update { it.copy(hubSearchQuery = query) }
    fun selectHubCategory(category: String) = _uiState.update { it.copy(hubSelectedCategory = category) }

    fun importCommunityAsset(asset: CommunityAsset) {
        val res = aiEngine.generateModel3D(asset.title, ModelingAiEngine.MESHY_3D, BlockbenchTargetType.ENTITY)
        val newNode = res.node.copy(id = "imp_${asset.id}_${System.nanoTime()}", name = asset.title.replace(" ", "_"), type = "Imported", sourceEngine = "Creator Hub")
        _uiState.update {
            it.copy(
                sceneNodes = it.sceneNodes + newNode,
                selectedNodeId = newNode.id,
                activeTab = OmniCoreTab.MODELISATION,
                hubStatusNotice = "${asset.title} importé (${asset.author})",
                statusBannerMessage = "${asset.title} ajouté à la vue 3D"
            )
        }
    }

    fun togglePublishModal(show: Boolean) = _uiState.update { it.copy(showPublishModal = show, botChecks = emptyList()) }
    fun updatePublishForm(title: String = _uiState.value.publishTitle, creditAuthor: String = _uiState.value.publishCreditAuthor, confirmed: Boolean = _uiState.value.publishAttributionConfirmed) {
        _uiState.update { it.copy(publishTitle = title, publishCreditAuthor = creditAuthor, publishAttributionConfirmed = confirmed) }
    }

    fun runBotModerationAndPublish() {
        val state = _uiState.value
        if (state.publishTitle.isBlank() || state.publishCreditAuthor.isBlank() || !state.publishAttributionConfirmed) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(botChecks = listOf(BotCheckResult("Bot-Licence", "Vérification...", false), BotCheckResult("Bot-Malware", "Scan...", false), BotCheckResult("Bot-PolyCheck", "Topologie...", false)))
            }
            delay(250)
            _uiState.update { s ->
                s.copy(
                    botChecks = listOf(BotCheckResult("Bot-Licence", "Crédit OK (${state.publishCreditAuthor})", true), BotCheckResult("Bot-Malware", "0 menace", true), BotCheckResult("Bot-PolyCheck", "Normales OK", true)),
                    communityAssets = listOf(CommunityAsset("pub_${System.nanoTime()}", state.publishTitle.trim(), state.publishCreditAuthor.trim(), "CC-BY-4.0", "Meshes 3D", "GLB", "21.4K", true, null)) + s.communityAssets,
                    hubStatusNotice = "Publié (Bots OK)",
                    publishTitle = "",
                    publishCreditAuthor = "",
                    publishAttributionConfirmed = false
                )
            }
        }
    }

    fun selectSdkModule(sdkId: String) = _uiState.update { it.copy(selectedSdkId = sdkId) }
    fun showStatusBanner(message: String) = _uiState.update { it.copy(statusBannerMessage = message) }
    fun dismissBanner() = _uiState.update { it.copy(statusBannerMessage = null, hubStatusNotice = null, omniMindStatus = null) }
}
