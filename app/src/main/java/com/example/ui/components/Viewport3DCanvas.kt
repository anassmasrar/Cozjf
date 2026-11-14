package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun Viewport3DCanvas(
    sceneNodes: List<SceneNode>,
    selectedNodeId: String,
    geometryMode: GeometryMode,
    renderEngine: RenderEngine,
    viewportShading: ViewportShading,
    activeShader: PolyShaderPreset,
    particleConfig: ParticleConfig,
    activeTool: ViewportTool,
    selectionSubMode: SelectionSubMode,
    riggingState: RiggingState,
    timelineState: TimelineState,
    showGrid: Boolean,
    xRayMode: Boolean,
    symmetryEnabled: Boolean,
    orbitYaw: Float,
    orbitPitch: Float,
    cameraZoom: Float,
    showSideToolbars: Boolean,
    onOrbitDrag: (Float, Float) -> Unit,
    onSnapCamera: (Float, Float) -> Unit,
    onZoomDelta: (Float) -> Unit,
    onResetCamera: () -> Unit,
    onGeometryModeChange: (GeometryMode) -> Unit,
    onRenderEngineChange: (RenderEngine) -> Unit,
    onShadingChange: (ViewportShading) -> Unit,
    onOpenShadersModal: () -> Unit,
    onSelectTool: (ViewportTool) -> Unit,
    onSelectSubMode: (SelectionSubMode) -> Unit,
    onToggleGrid: () -> Unit,
    onToggleXRay: () -> Unit,
    onToggleSymmetry: () -> Unit,
    onToggleWeightPaint: () -> Unit,
    onOpenMobileOutliner: () -> Unit,
    onQuickAddCube: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedNode = sceneNodes.find { it.id == selectedNodeId }
    val selectedNodeName = selectedNode?.name ?: "Scène"
    val visibleMeshes = sceneNodes.filter { it.visible && (it.type == "Mesh" || it.type == "Imported") }
    val animPose = interpolatePose(timelineState)
    val effectiveParticle = resolveEffectiveParticlePreset(activeShader, particleConfig)

    val fxTransition = rememberInfiniteTransition(label = "viewportFx")
    val fxTimeSec by fxTransition.animateFloat(
        initialValue = 0f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fxClock"
    )

    Box(
        modifier = modifier
            .clipToBounds()
            .background(VoidBlack)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    if (pan != Offset.Zero) {
                        onOrbitDrag(pan.x * 0.45f, -pan.y * 0.35f)
                    }
                    if (zoom != 1f) {
                        onZoomDelta((zoom - 1f) * 0.9f)
                    }
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("viewport_3d_canvas")
        ) {
            val w = size.width
            val h = size.height
            val center = Offset(w * 0.5f, h * 0.54f)
            val scaleBase = minOf(w, h) * 0.24f * cameraZoom

            drawShaderBackground(activeShader, w, h)

            if (showGrid) {
                drawGroundGrid(center, scaleBase, orbitYaw, orbitPitch, activeShader)
            }

            visibleMeshes.forEach { node ->
                val isSelected = node.id == selectedNodeId
                val animOffsetY = if (isSelected) animPose.offsetY else 0f
                val animRotY = if (isSelected) animPose.rotYDeg else 0f
                val animScale = if (isSelected) node.scale * animPose.scale else node.scale
                val nodeCenter = project3D(
                    Vec3(node.posX, node.posY + animOffsetY, node.posZ),
                    center,
                    scaleBase,
                    orbitYaw,
                    orbitPitch
                ).first
                val effectiveMode = if (isSelected) geometryMode else node.geometryMode

                drawSceneNodeMesh(
                    node = node,
                    center = nodeCenter,
                    scaleBase = scaleBase,
                    orbitYaw = orbitYaw,
                    orbitPitch = orbitPitch,
                    effectiveMode = effectiveMode,
                    shading = viewportShading,
                    shader = activeShader,
                    xRay = xRayMode,
                    riggingState = riggingState,
                    animPose = animPose,
                    subMode = selectionSubMode,
                    isSelected = isSelected
                )

                if (isSelected) {
                    if (riggingState.weightPaintOverlay || riggingState.isScanning) {
                        drawSkeletalIkOverlay(
                            center = nodeCenter,
                            scale = scaleBase * animScale,
                            yawDeg = orbitYaw + animRotY,
                            pitchDeg = orbitPitch,
                            riggingState = riggingState,
                            animPose = animPose
                        )
                    }
                    drawActiveTransformGizmo(
                        tool = activeTool,
                        center = nodeCenter,
                        scale = scaleBase * animScale,
                        yawDeg = orbitYaw,
                        pitchDeg = orbitPitch
                    )
                }
            }

            drawAdaptiveParticles(
                shader = activeShader,
                config = particleConfig,
                width = w,
                height = h,
                fxTimeSec = fxTimeSec
            )

            drawShaderPostOverlay(activeShader, w, h, fxTimeSec)
        }

        // Top-Left Info + Quick "+ Forme" & "Shaders / FX" triggers
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = if (showSideToolbars) 54.dp else 12.dp, top = 46.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = "$selectedNodeName • ${selectedNode?.sourceEngine ?: "Studio"} (${visibleMeshes.size})",
                style = MaterialTheme.typography.labelSmall.copy(color = SlateSilver, fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = "Shader: ${activeShader.shortLabel} • FX: ${if (particleConfig.enabled) effectiveParticle.label else "OFF"}",
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(
                    modifier = Modifier
                        .testTag("viewport_quick_add_btn")
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(onClick = onQuickAddCube),
                    color = FrostedGlassBg,
                    border = BorderStroke(1.dp, SteelBlue.copy(alpha = 0.65f)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Ajouter Cube", tint = SteelBlue, modifier = Modifier.size(12.dp))
                        Text("+ Forme", style = MaterialTheme.typography.labelSmall.copy(color = SteelBlue, fontSize = 10.sp))
                    }
                }
                Surface(
                    modifier = Modifier
                        .testTag("viewport_shaders_fx_btn")
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(onClick = onOpenShadersModal),
                    color = FrostedGlassBg,
                    border = BorderStroke(1.dp, Color(activeShader.accentHex).copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = "Shaders & Particules", tint = Color(activeShader.accentHex), modifier = Modifier.size(12.dp))
                        Text("Shaders & FX", style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary, fontSize = 10.sp))
                    }
                }
            }
            if (riggingState.isScanning) {
                Surface(
                    color = SteelBlueDim.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, SteelBlue)
                ) {
                    Text(
                        text = riggingState.liveRigStatus,
                        style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary, fontSize = 9.sp),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // Top Floating Viewport Overlay Bar (Smooth/Voxel + Shading + PolyX3D/AAA)
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = FrostedGlassBg,
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, BorderHairline)
            ) {
                Row(modifier = Modifier.padding(2.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    CompactToggleChip(
                        selected = geometryMode == GeometryMode.SMOOTH_ORGANIQUE,
                        label = "Smooth",
                        tag = "geo_smooth_btn",
                        onClick = { onGeometryModeChange(GeometryMode.SMOOTH_ORGANIQUE) }
                    )
                    CompactToggleChip(
                        selected = geometryMode == GeometryMode.VOXEL_MODULAIRE,
                        label = "Voxel",
                        tag = "geo_voxel_btn",
                        onClick = { onGeometryModeChange(GeometryMode.VOXEL_MODULAIRE) }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = FrostedGlassBg,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, BorderHairline)
                ) {
                    Row(modifier = Modifier.padding(2.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        ShadingIconBtn(
                            selected = viewportShading == ViewportShading.WIREFRAME,
                            icon = Icons.Outlined.GridOn,
                            desc = "Filaire",
                            onClick = { onShadingChange(ViewportShading.WIREFRAME) }
                        )
                        ShadingIconBtn(
                            selected = viewportShading == ViewportShading.SOLID,
                            icon = Icons.Filled.Circle,
                            desc = "Solide",
                            onClick = { onShadingChange(ViewportShading.SOLID) }
                        )
                        ShadingIconBtn(
                            selected = viewportShading == ViewportShading.RENDERED,
                            icon = Icons.Filled.WbSunny,
                            desc = "Rendu",
                            onClick = { onShadingChange(ViewportShading.RENDERED) }
                        )
                    }
                }

                Surface(
                    color = FrostedGlassBg,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, BorderHairline)
                ) {
                    Row(modifier = Modifier.padding(2.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        CompactToggleChip(
                            selected = renderEngine == RenderEngine.POLYX3D_MOBILE,
                            label = "PolyX3D+",
                            tag = "render_polyx3d_btn",
                            onClick = { onRenderEngineChange(RenderEngine.POLYX3D_MOBILE) }
                        )
                        CompactToggleChip(
                            selected = renderEngine == RenderEngine.AAA_REALISME,
                            label = "AAA",
                            tag = "render_aaa_btn",
                            onClick = { onRenderEngineChange(RenderEngine.AAA_REALISME) }
                        )
                    }
                }
            }
        }

        // Top-Right Navigation Axis Gizmo + Clear Camera View Buttons
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 46.dp, end = if (showSideToolbars) 54.dp else 12.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            NavigationAxisOrb(
                yawDeg = orbitYaw,
                pitchDeg = orbitPitch,
                onSnapIso = onResetCamera
            )
            Surface(
                color = FrostedGlassBg,
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, BorderHairline)
            ) {
                Row(modifier = Modifier.padding(2.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    CameraSnapChip("Face", "cam_front_btn") { onSnapCamera(0f, 0f) }
                    CameraSnapChip("Dessus", "cam_top_btn") { onSnapCamera(0f, -72f) }
                    CameraSnapChip("Côté", "cam_side_btn") { onSnapCamera(90f, -10f) }
                    CameraSnapChip("Iso", "cam_iso_btn") { onResetCamera() }
                }
            }
        }

        if (showSideToolbars) {
            Surface(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 6.dp),
                color = FrostedGlassBg,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, BorderHairline)
            ) {
                Column(
                    modifier = Modifier.padding(3.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SideToolBtn(Icons.Outlined.NearMe, activeTool == ViewportTool.SELECT, "Sélection") { onSelectTool(ViewportTool.SELECT) }
                    SideToolBtn(Icons.Filled.OpenWith, activeTool == ViewportTool.MOVE, "Déplacer") { onSelectTool(ViewportTool.MOVE) }
                    SideToolBtn(Icons.AutoMirrored.Filled.RotateRight, activeTool == ViewportTool.ROTATE, "Rotation") { onSelectTool(ViewportTool.ROTATE) }
                    SideToolBtn(Icons.Filled.AspectRatio, activeTool == ViewportTool.SCALE, "Échelle") { onSelectTool(ViewportTool.SCALE) }
                    HorizontalDivider(modifier = Modifier.width(24.dp), color = BorderHairline)
                    SideToolBtn(Icons.Outlined.Flip, symmetryEnabled, "Symétrie") { onToggleSymmetry() }
                    SideToolBtn(Icons.Outlined.Layers, xRayMode, "Rayons X") { onToggleXRay() }
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 6.dp),
                color = FrostedGlassBg,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, BorderHairline)
            ) {
                Column(
                    modifier = Modifier.padding(3.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SideToolBtn(Icons.Filled.AutoFixHigh, activeTool == ViewportTool.SCULPT, "Argile") { onSelectTool(ViewportTool.SCULPT) }
                    SideToolBtn(Icons.Filled.BlurOn, activeTool == ViewportTool.SMOOTH_BRUSH, "Lisser") { onSelectTool(ViewportTool.SMOOTH_BRUSH) }
                    SideToolBtn(Icons.Filled.ViewInAr, activeTool == ViewportTool.VOXEL_BRUSH, "Voxel") { onSelectTool(ViewportTool.VOXEL_BRUSH) }
                    SideToolBtn(Icons.Filled.Brush, riggingState.weightPaintOverlay, "Weight") { onToggleWeightPaint() }
                    HorizontalDivider(modifier = Modifier.width(24.dp), color = BorderHairline)
                    SideToolBtn(Icons.Outlined.AccountTree, false, "Scène") { onOpenMobileOutliner() }
                    SideToolBtn(Icons.Outlined.Grid4x4, showGrid, "Grille") { onToggleGrid() }
                }
            }
        }

        // Bottom-Left Selection Sub-Mode Dock
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp),
            color = FrostedGlassBg,
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, BorderHairline)
        ) {
            Row(modifier = Modifier.padding(2.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                SelectionSubModeIconBtn(selectionSubMode == SelectionSubMode.OBJECT, Icons.Filled.ViewInAr, "Objet") { onSelectSubMode(SelectionSubMode.OBJECT) }
                SelectionSubModeIconBtn(selectionSubMode == SelectionSubMode.VERTEX, Icons.Filled.ScatterPlot, "Sommets") { onSelectSubMode(SelectionSubMode.VERTEX) }
                SelectionSubModeIconBtn(selectionSubMode == SelectionSubMode.EDGE, Icons.Filled.Timeline, "Arêtes") { onSelectSubMode(SelectionSubMode.EDGE) }
                SelectionSubModeIconBtn(selectionSubMode == SelectionSubMode.FACE, Icons.Outlined.CropSquare, "Faces") { onSelectSubMode(SelectionSubMode.FACE) }
            }
        }

        // Bottom-Right Zoom & Reset Controls
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp),
            color = FrostedGlassBg,
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, BorderHairline)
        ) {
            Row(modifier = Modifier.padding(2.dp)) {
                IconButton(onClick = { onZoomDelta(0.15f) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.ZoomIn, contentDescription = "Zoom +", tint = TextSecondary, modifier = Modifier.size(15.dp))
                }
                IconButton(onClick = { onZoomDelta(-0.15f) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.ZoomOut, contentDescription = "Zoom -", tint = TextSecondary, modifier = Modifier.size(15.dp))
                }
                IconButton(onClick = onResetCamera, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.CenterFocusStrong, contentDescription = "Recentrer", tint = SteelBlue, modifier = Modifier.size(15.dp))
                }
            }
        }
    }
}

@Composable
private fun NavigationAxisOrb(
    yawDeg: Float,
    pitchDeg: Float,
    onSnapIso: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(onClick = onSnapIso),
        color = FrostedGlassBg,
        shape = CircleShape,
        border = BorderStroke(1.dp, BorderHairline)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val c = Offset(size.width / 2f, size.height / 2f)
            val r = size.minDimension * 0.36f
            val axes = listOf(
                Vec3(1f, 0f, 0f) to CrimsonAlert,
                Vec3(0f, 1f, 0f) to TerminalEmerald,
                Vec3(0f, 0f, 1f) to SteelBlue
            )
            axes.forEach { (v, col) ->
                val (end, _) = project3D(v, c, r, yawDeg, pitchDeg)
                drawLine(col, c, end, strokeWidth = 2.2f, cap = StrokeCap.Round)
                drawCircle(col, radius = 3.8f, center = end)
            }
        }
    }
}

@Composable
private fun CameraSnapChip(label: String, tag: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .testTag(tag)
            .clip(RoundedCornerShape(3.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 5.dp, vertical = 3.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = SlateSilver, fontSize = 9.sp))
    }
}

@Composable
private fun CompactToggleChip(selected: Boolean, label: String, tag: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .testTag(tag)
            .clip(RoundedCornerShape(3.dp))
            .background(if (selected) SteelBlueDim else Color.Transparent)
            .border(1.dp, if (selected) SteelBlue else Color.Transparent, RoundedCornerShape(3.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (selected) SteelBlue else TextSecondary,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        )
    }
}

@Composable
private fun ShadingIconBtn(selected: Boolean, icon: ImageVector, desc: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(if (selected) SteelBlueDim else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = desc, tint = if (selected) SteelBlue else TextSecondary, modifier = Modifier.size(13.dp))
    }
}

@Composable
private fun SideToolBtn(icon: ImageVector, selected: Boolean, desc: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (selected) SteelBlueDim else Color.Transparent)
            .border(1.dp, if (selected) SteelBlue else Color.Transparent, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = desc, tint = if (selected) SteelBlue else SlateSilver, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun SelectionSubModeIconBtn(selected: Boolean, icon: ImageVector, desc: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(if (selected) SteelBlueDim else Color.Transparent)
            .border(1.dp, if (selected) SteelBlue else Color.Transparent, RoundedCornerShape(3.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = desc, tint = if (selected) SteelBlue else TextSecondary, modifier = Modifier.size(15.dp))
    }
}
