package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.*
import com.example.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class Vec3(val x: Float, val y: Float, val z: Float)

fun interpolatePose(timeline: TimelineState): KeyframePose {
    val poses = timeline.poses.values.sortedBy { it.frame }
    if (poses.isEmpty()) return KeyframePose(timeline.currentFrame)
    val f = timeline.currentFrame
    if (f <= poses.first().frame) return poses.first()
    if (f >= poses.last().frame) return poses.last()

    var prev = poses.first()
    for (next in poses) {
        if (next.frame >= f) {
            val span = (next.frame - prev.frame).coerceAtLeast(1)
            val rawT = ((f - prev.frame).toFloat() / span).coerceIn(0f, 1f)
            val t = when (timeline.activeStyle) {
                AnimationStyle.STOP_MOTION -> ((rawT * 3f).toInt() / 3f).coerceIn(0f, 1f)
                AnimationStyle.CARTOON_TEMMER -> {
                    // Snappy ease-out with slight bounce overshoot
                    val inv = 1f - rawT
                    1f - inv * inv * inv
                }
                AnimationStyle.ANIME_SHONEN -> {
                    if (rawT < 0.35f) rawT * 0.4f else ((rawT - 0.35f) / 0.65f) * 0.86f + 0.14f
                }
                else -> rawT * rawT * (3f - 2f * rawT)
            }
            return KeyframePose(
                frame = f,
                offsetY = prev.offsetY + (next.offsetY - prev.offsetY) * t,
                rotYDeg = prev.rotYDeg + (next.rotYDeg - prev.rotYDeg) * t,
                scale = prev.scale + (next.scale - prev.scale) * t,
                scaleX = prev.scaleX + (next.scaleX - prev.scaleX) * t,
                scaleY = prev.scaleY + (next.scaleY - prev.scaleY) * t,
                scaleZ = prev.scaleZ + (next.scaleZ - prev.scaleZ) * t,
                rotXDeg = prev.rotXDeg + (next.rotXDeg - prev.rotXDeg) * t,
                rotZDeg = prev.rotZDeg + (next.rotZDeg - prev.rotZDeg) * t,
                limbSwingDeg = prev.limbSwingDeg + (next.limbSwingDeg - prev.limbSwingDeg) * t,
                headBobDeg = prev.headBobDeg + (next.headBobDeg - prev.headBobDeg) * t
            )
        }
        prev = next
    }
    return poses.last()
}

fun project3D(
    p: Vec3,
    center: Offset,
    scale: Float,
    yawDeg: Float,
    pitchDeg: Float
): Pair<Offset, Float> {
    val yaw = Math.toRadians(yawDeg.toDouble()).toFloat()
    val pitch = Math.toRadians(pitchDeg.toDouble()).toFloat()

    val x1 = p.x * cos(yaw) - p.z * sin(yaw)
    val z1 = p.x * sin(yaw) + p.z * cos(yaw)
    val y2 = p.y * cos(pitch) - z1 * sin(pitch)
    val z2 = p.y * sin(pitch) + z1 * cos(pitch)

    val dist = 4.6f
    val factor = dist / (dist + z2).coerceAtLeast(1.1f)
    val sx = center.x + x1 * scale * factor
    val sy = center.y - y2 * scale * factor
    return Offset(sx, sy) to z2
}

fun DrawScope.drawGroundGrid(
    center: Offset,
    scale: Float,
    yawDeg: Float,
    pitchDeg: Float,
    shader: PolyShaderPreset
) {
    val gridColor = when (shader) {
        PolyShaderPreset.HOLOGRAM_CYBER -> Color(0xFF0E3A44)
        PolyShaderPreset.NEON_BLOOM -> Color(0xFF10382B)
        PolyShaderPreset.HORROR_FOG -> Color(0xFF28191C)
        else -> Color(0xFF23252A)
    }
    val range = 5
    val step = 0.55f
    val yFloor = -0.75f

    for (i in -range..range) {
        val coord = i * step
        val (a, _) = project3D(Vec3(coord, yFloor, -range * step), center, scale, yawDeg, pitchDeg)
        val (b, _) = project3D(Vec3(coord, yFloor, range * step), center, scale, yawDeg, pitchDeg)
        drawLine(
            color = if (i == 0) SteelBlue.copy(alpha = 0.5f) else gridColor,
            start = a,
            end = b,
            strokeWidth = if (i == 0) 1.6f else 1f
        )

        val (c, _) = project3D(Vec3(-range * step, yFloor, coord), center, scale, yawDeg, pitchDeg)
        val (d, _) = project3D(Vec3(range * step, yFloor, coord), center, scale, yawDeg, pitchDeg)
        drawLine(
            color = if (i == 0) CrimsonAlert.copy(alpha = 0.5f) else gridColor,
            start = c,
            end = d,
            strokeWidth = if (i == 0) 1.6f else 1f
        )
    }
}

fun DrawScope.drawSceneNodeMesh(
    node: SceneNode,
    center: Offset,
    scaleBase: Float,
    orbitYaw: Float,
    orbitPitch: Float,
    effectiveMode: GeometryMode,
    shading: ViewportShading,
    shader: PolyShaderPreset,
    xRay: Boolean,
    riggingState: RiggingState,
    animPose: KeyframePose,
    subMode: SelectionSubMode,
    isSelected: Boolean
) {
    val animScale = if (isSelected) node.scale * animPose.scale else node.scale
    val sx = if (isSelected) animPose.scaleX else 1f
    val sy = if (isSelected) animPose.scaleY else 1f
    val sz = if (isSelected) animPose.scaleZ else 1f
    val yaw = orbitYaw + node.rotYDeg + (if (isSelected) animPose.rotYDeg else 0f)
    val pitch = orbitPitch + (if (isSelected) animPose.rotXDeg * 0.45f else 0f)
    val weightPaint = riggingState.weightPaintOverlay && isSelected

    if (node.parts.isNotEmpty()) {
        val bonesByRole = riggingState.bones.associateBy { it.partRole }
        val sortedParts = node.parts.map { part ->
            val displaced = applyPartRigAndAnim(part, bonesByRole[part.role], if (isSelected) animPose else KeyframePose(1), sx, sy, sz)
            val depth = project3D(displaced.first, center, scaleBase * animScale, yaw, pitch).second
            Triple(part, displaced, depth)
        }.sortedByDescending { it.third }

        sortedParts.forEach { (part, displaced, _) ->
            val (pos, half) = displaced
            drawPartShape(
                shape = part.shape,
                center = center,
                scale = scaleBase * animScale,
                yawDeg = yaw,
                pitchDeg = pitch,
                offset = pos,
                half = half,
                shading = shading,
                shader = shader,
                xRay = xRay,
                weightPaintOverlay = weightPaint,
                subMode = subMode,
                tint = Color(part.tintHex),
                isSelected = isSelected
            )
        }
        return
    }

    if (effectiveMode == GeometryMode.VOXEL_MODULAIRE) {
        drawVoxelModularStructure(
            center = center,
            scale = scaleBase * animScale,
            yawDeg = yaw,
            pitchDeg = pitch,
            sx = sx,
            sy = sy,
            sz = sz,
            shading = shading,
            shader = shader,
            xRay = xRay,
            weightPaintOverlay = weightPaint,
            subMode = subMode,
            tint = Color(node.tintHex),
            isSelected = isSelected
        )
    } else {
        drawPrimitiveShapeMesh(
            shape = node.shape,
            center = center,
            scale = scaleBase * animScale,
            yawDeg = yaw,
            pitchDeg = pitch,
            sx = sx,
            sy = sy,
            sz = sz,
            riggingState = riggingState,
            animPose = if (isSelected) animPose else KeyframePose(1),
            shading = shading,
            shader = shader,
            xRay = xRay,
            weightPaintOverlay = weightPaint,
            subMode = subMode,
            tint = Color(node.tintHex),
            isSelected = isSelected
        )
    }
}

private fun applyPartRigAndAnim(
    part: SubMeshPart,
    bone: BoneJoint?,
    pose: KeyframePose,
    sx: Float,
    sy: Float,
    sz: Float
): Pair<Vec3, Vec3> {
    val bonePitchRad = Math.toRadians((bone?.pitchDeg ?: 0f).toDouble()).toFloat()
    val boneYawRad = Math.toRadians((bone?.yawDeg ?: 0f).toDouble()).toFloat()
    val swingRad = Math.toRadians(pose.limbSwingDeg.toDouble()).toFloat()
    val headRad = Math.toRadians(pose.headBobDeg.toDouble()).toFloat()

    var ox = part.offsetX * sx
    var oy = part.offsetY * sy
    var oz = part.offsetZ * sz

    when (part.role) {
        "arm_l", "wing_l" -> {
            oy += sin(bonePitchRad + swingRad) * 0.16f
            oz += cos(boneYawRad) * 0.04f + sin(swingRad) * 0.14f
        }
        "arm_r", "wing_r" -> {
            oy += sin(bonePitchRad - swingRad) * 0.16f
            oz += sin(-swingRad) * 0.14f
        }
        "leg_l" -> {
            oz += sin(-swingRad) * 0.18f
            oy += kotlin.math.abs(sin(swingRad)) * 0.05f
        }
        "leg_r" -> {
            oz += sin(swingRad) * 0.18f
            oy += kotlin.math.abs(sin(swingRad)) * 0.05f
        }
        "head" -> {
            ox += sin(boneYawRad) * 0.09f
            oy += sin(headRad) * 0.06f
            oz += sin(bonePitchRad + headRad) * 0.08f
        }
        "tail" -> {
            ox += sin(boneYawRad + swingRad) * 0.14f
            oy += sin(bonePitchRad) * 0.10f
        }
    }

    return Vec3(ox, oy, oz) to Vec3(part.sizeX * sx, part.sizeY * sy, part.sizeZ * sz)
}

private fun DrawScope.drawPartShape(
    shape: PrimitiveShape,
    center: Offset,
    scale: Float,
    yawDeg: Float,
    pitchDeg: Float,
    offset: Vec3,
    half: Vec3,
    shading: ViewportShading,
    shader: PolyShaderPreset,
    xRay: Boolean,
    weightPaintOverlay: Boolean,
    subMode: SelectionSubMode,
    tint: Color,
    isSelected: Boolean
) {
    if (shape == PrimitiveShape.SPHERE) {
        drawSmoothSphere(
            center = center,
            scale = scale * (half.x + half.y) * 0.9f,
            yawDeg = yawDeg,
            pitchDeg = pitchDeg,
            shading = shading,
            shader = shader,
            xRay = xRay,
            weightPaintOverlay = weightPaintOverlay,
            subMode = subMode,
            tint = tint,
            isSelected = isSelected,
            offsetLocal = offset
        )
    } else {
        drawBoxMesh(
            center = center,
            scale = scale,
            yawDeg = yawDeg,
            pitchDeg = pitchDeg,
            halfW = half.x,
            halfH = half.y,
            halfD = half.z,
            shading = shading,
            shader = shader,
            xRay = xRay,
            weightPaintOverlay = weightPaintOverlay,
            subMode = subMode,
            tint = tint,
            isSelected = isSelected,
            offsetLocal = offset
        )
    }
}

private fun DrawScope.drawPrimitiveShapeMesh(
    shape: PrimitiveShape,
    center: Offset,
    scale: Float,
    yawDeg: Float,
    pitchDeg: Float,
    sx: Float,
    sy: Float,
    sz: Float,
    riggingState: RiggingState,
    animPose: KeyframePose,
    shading: ViewportShading,
    shader: PolyShaderPreset,
    xRay: Boolean,
    weightPaintOverlay: Boolean,
    subMode: SelectionSubMode,
    tint: Color,
    isSelected: Boolean
) {
    when (shape) {
        PrimitiveShape.CUBE -> drawBoxMesh(
            center, scale, yawDeg, pitchDeg, 0.55f * sx, 0.55f * sy, 0.55f * sz,
            shading, shader, xRay, weightPaintOverlay, subMode, tint, isSelected
        )
        PrimitiveShape.PLAN -> drawBoxMesh(
            center, scale, yawDeg, pitchDeg, 1.1f * sx, 0.04f * sy, 1.1f * sz,
            shading, shader, xRay, weightPaintOverlay, subMode, tint, isSelected
        )
        PrimitiveShape.SPHERE -> drawSmoothSphere(
            center, scale * ((sx + sy) * 0.5f), yawDeg, pitchDeg,
            shading, shader, xRay, weightPaintOverlay, subMode, tint, isSelected
        )
        PrimitiveShape.CYLINDRE -> drawCylinderOrCone(
            center, scale, yawDeg, pitchDeg, 0.5f * sx, 0.5f * sz, 0.65f * sy,
            shading, shader, xRay, weightPaintOverlay, subMode, tint, isSelected
        )
        PrimitiveShape.CONE -> drawCylinderOrCone(
            center, scale, yawDeg, pitchDeg, 0.01f, 0.58f * sx, 0.7f * sy,
            shading, shader, xRay, weightPaintOverlay, subMode, tint, isSelected
        )
        PrimitiveShape.TORE -> drawTorusMesh(
            center, scale * ((sx + sy) * 0.5f), yawDeg, pitchDeg,
            shading, shader, xRay, weightPaintOverlay, subMode, tint, isSelected
        )
        PrimitiveShape.TORSE -> drawCleanMannequin(
            center, scale, yawDeg, pitchDeg, sx, sy, sz, riggingState, animPose,
            shading, shader, xRay, weightPaintOverlay, subMode, tint, isSelected
        )
    }
}

private fun DrawScope.drawBoxMesh(
    center: Offset,
    scale: Float,
    yawDeg: Float,
    pitchDeg: Float,
    halfW: Float,
    halfH: Float,
    halfD: Float,
    shading: ViewportShading,
    shader: PolyShaderPreset,
    xRay: Boolean,
    weightPaintOverlay: Boolean,
    subMode: SelectionSubMode,
    tint: Color,
    isSelected: Boolean,
    offsetLocal: Vec3 = Vec3(0f, 0f, 0f)
) {
    val rawCorners = listOf(
        Vec3(-halfW + offsetLocal.x, -halfH + offsetLocal.y, -halfD + offsetLocal.z),
        Vec3(halfW + offsetLocal.x, -halfH + offsetLocal.y, -halfD + offsetLocal.z),
        Vec3(halfW + offsetLocal.x, halfH + offsetLocal.y, -halfD + offsetLocal.z),
        Vec3(-halfW + offsetLocal.x, halfH + offsetLocal.y, -halfD + offsetLocal.z),
        Vec3(-halfW + offsetLocal.x, -halfH + offsetLocal.y, halfD + offsetLocal.z),
        Vec3(halfW + offsetLocal.x, -halfH + offsetLocal.y, halfD + offsetLocal.z),
        Vec3(halfW + offsetLocal.x, halfH + offsetLocal.y, halfD + offsetLocal.z),
        Vec3(-halfW + offsetLocal.x, halfH + offsetLocal.y, halfD + offsetLocal.z)
    )
    val projected = rawCorners.map { project3D(it, center, scale, yawDeg, pitchDeg) }
    val faces = listOf(
        listOf(0, 1, 2, 3),
        listOf(4, 5, 6, 7),
        listOf(3, 2, 6, 7),
        listOf(0, 1, 5, 4),
        listOf(1, 5, 6, 2),
        listOf(4, 0, 3, 7)
    ).sortedByDescending { f -> f.map { projected[it].second }.average() }

    val baseAlpha = when {
        shading == ViewportShading.WIREFRAME -> 0f
        xRay -> 0.28f
        else -> 0.9f
    }

    faces.forEachIndexed { idx, f ->
        val pts = f.map { projected[it].first }
        val path = Path().apply {
            moveTo(pts[0].x, pts[0].y)
            lineTo(pts[1].x, pts[1].y)
            lineTo(pts[2].x, pts[2].y)
            lineTo(pts[3].x, pts[3].y)
            close()
        }
        val shade = (idx / 5f).coerceIn(0f, 1f)
        val visual = computeShaderFaceVisual(shader, tint, shade, baseAlpha, isSelected, weightPaintOverlay)
        if (baseAlpha > 0f) {
            drawPath(path, color = visual.faceColor)
        }
        val finalEdge = if (subMode == SelectionSubMode.EDGE) SteelBlue else visual.edgeColor
        drawPath(path, color = finalEdge, style = Stroke(width = visual.edgeWidth))
    }

    if (subMode == SelectionSubMode.VERTEX) {
        projected.forEach { (pt, _) -> drawCircle(SteelBlue, radius = 3f, center = pt) }
    }
}

private fun DrawScope.drawSmoothSphere(
    center: Offset,
    scale: Float,
    yawDeg: Float,
    pitchDeg: Float,
    shading: ViewportShading,
    shader: PolyShaderPreset,
    xRay: Boolean,
    weightPaintOverlay: Boolean,
    subMode: SelectionSubMode,
    tint: Color,
    isSelected: Boolean,
    offsetLocal: Vec3 = Vec3(0f, 0f, 0f)
) {
    val rings = 10
    val segs = 16
    val rSphere = 0.65f
    val gridPoints = Array(rings + 1) { r ->
        val v = r.toFloat() / rings
        val phi = (v - 0.5f) * PI.toFloat()
        val y = sin(phi) * rSphere + offsetLocal.y
        val rad = cos(phi) * rSphere
        Array(segs) { s ->
            val theta = (s.toFloat() / segs) * (2f * PI.toFloat())
            project3D(Vec3(cos(theta) * rad + offsetLocal.x, y, sin(theta) * rad + offsetLocal.z), center, scale, yawDeg, pitchDeg)
        }
    }

    val faceAlpha = when {
        shading == ViewportShading.WIREFRAME -> 0f
        xRay -> 0.25f
        else -> 0.88f
    }

    val quads = mutableListOf<Triple<Path, Float, Float>>()
    for (r in 0 until rings) {
        for (s in 0 until segs) {
            val nextS = (s + 1) % segs
            val (p0, z0) = gridPoints[r][s]
            val (p1, z1) = gridPoints[r][nextS]
            val (p2, z2) = gridPoints[r + 1][nextS]
            val (p3, z3) = gridPoints[r + 1][s]
            val avgZ = (z0 + z1 + z2 + z3) / 4f
            val path = Path().apply {
                moveTo(p0.x, p0.y); lineTo(p1.x, p1.y); lineTo(p2.x, p2.y); lineTo(p3.x, p3.y); close()
            }
            quads.add(Triple(path, avgZ, r.toFloat() / rings))
        }
    }
    quads.sortedByDescending { it.second }.forEach { (path, _, heightRatio) ->
        val visual = computeShaderFaceVisual(shader, tint, heightRatio, faceAlpha, isSelected, weightPaintOverlay)
        if (faceAlpha > 0f) {
            drawPath(path, visual.faceColor)
        }
        drawPath(path, color = visual.edgeColor.copy(alpha = 0.65f), style = Stroke(width = (visual.edgeWidth * 0.75f).coerceAtLeast(0.8f)))
    }

    if (subMode == SelectionSubMode.VERTEX) {
        gridPoints.forEach { row -> row.forEach { (pt, _) -> drawCircle(SteelBlue, radius = 2.2f, center = pt) } }
    }
}

private fun DrawScope.drawCylinderOrCone(
    center: Offset,
    scale: Float,
    yawDeg: Float,
    pitchDeg: Float,
    topRadius: Float,
    bottomRadius: Float,
    halfH: Float,
    shading: ViewportShading,
    shader: PolyShaderPreset,
    xRay: Boolean,
    weightPaintOverlay: Boolean,
    subMode: SelectionSubMode,
    tint: Color,
    isSelected: Boolean
) {
    val segs = 14
    val topRing = Array(segs) { s ->
        val t = (s.toFloat() / segs) * (2f * PI.toFloat())
        project3D(Vec3(cos(t) * topRadius, halfH, sin(t) * topRadius), center, scale, yawDeg, pitchDeg)
    }
    val botRing = Array(segs) { s ->
        val t = (s.toFloat() / segs) * (2f * PI.toFloat())
        project3D(Vec3(cos(t) * bottomRadius, -halfH, sin(t) * bottomRadius), center, scale, yawDeg, pitchDeg)
    }

    val faceAlpha = when {
        shading == ViewportShading.WIREFRAME -> 0f
        xRay -> 0.28f
        else -> 0.88f
    }

    val sideQuads = (0 until segs).map { s ->
        val n = (s + 1) % segs
        val avgZ = (topRing[s].second + topRing[n].second + botRing[n].second + botRing[s].second) / 4f
        val path = Path().apply {
            moveTo(topRing[s].first.x, topRing[s].first.y)
            lineTo(topRing[n].first.x, topRing[n].first.y)
            lineTo(botRing[n].first.x, botRing[n].first.y)
            lineTo(botRing[s].first.x, botRing[s].first.y)
            close()
        }
        Triple(path, avgZ, s)
    }.sortedByDescending { it.second }

    sideQuads.forEach { (path, _, idx) ->
        val shade = ((cos(idx * 0.45f) + 1f) * 0.5f).coerceIn(0f, 1f)
        val visual = computeShaderFaceVisual(shader, tint, shade, faceAlpha, isSelected, weightPaintOverlay)
        if (faceAlpha > 0f) drawPath(path, visual.faceColor)
        drawPath(path, visual.edgeColor, style = Stroke(width = visual.edgeWidth * 0.85f))
    }

    if (subMode == SelectionSubMode.VERTEX) {
        topRing.forEach { (pt, _) -> drawCircle(SteelBlue, radius = 2.4f, center = pt) }
        botRing.forEach { (pt, _) -> drawCircle(SteelBlue, radius = 2.4f, center = pt) }
    }
}

private fun DrawScope.drawTorusMesh(
    center: Offset,
    scale: Float,
    yawDeg: Float,
    pitchDeg: Float,
    shading: ViewportShading,
    shader: PolyShaderPreset,
    xRay: Boolean,
    weightPaintOverlay: Boolean,
    subMode: SelectionSubMode,
    tint: Color,
    isSelected: Boolean
) {
    val majorSegs = 18
    val minorSegs = 8
    val majorR = 0.62f
    val minorR = 0.22f
    val alpha = if (shading == ViewportShading.WIREFRAME) 0f else if (xRay) 0.3f else 0.88f

    val ring = Array(majorSegs) { i ->
        val u = (i.toFloat() / majorSegs) * 2f * PI.toFloat()
        Array(minorSegs) { j ->
            val v = (j.toFloat() / minorSegs) * 2f * PI.toFloat()
            val x = (majorR + minorR * cos(v)) * cos(u)
            val y = minorR * sin(v)
            val z = (majorR + minorR * cos(v)) * sin(u)
            project3D(Vec3(x, y, z), center, scale, yawDeg, pitchDeg)
        }
    }

    for (i in 0 until majorSegs) {
        val ni = (i + 1) % majorSegs
        for (j in 0 until minorSegs) {
            val nj = (j + 1) % minorSegs
            val path = Path().apply {
                moveTo(ring[i][j].first.x, ring[i][j].first.y)
                lineTo(ring[ni][j].first.x, ring[ni][j].first.y)
                lineTo(ring[ni][nj].first.x, ring[ni][nj].first.y)
                lineTo(ring[i][nj].first.x, ring[i][nj].first.y)
                close()
            }
            val shade = ((sin(j.toFloat()) + 1f) * 0.5f).coerceIn(0f, 1f)
            val visual = computeShaderFaceVisual(shader, tint, shade, alpha, isSelected, weightPaintOverlay)
            if (alpha > 0f) drawPath(path, visual.faceColor)
            drawPath(path, visual.edgeColor, style = Stroke(width = 0.9f))
        }
    }
    if (subMode == SelectionSubMode.VERTEX) {
        ring.forEach { r -> r.forEach { (pt, _) -> drawCircle(SteelBlue, 2f, pt) } }
    }
}

private fun DrawScope.drawCleanMannequin(
    center: Offset,
    scale: Float,
    yawDeg: Float,
    pitchDeg: Float,
    sx: Float,
    sy: Float,
    sz: Float,
    riggingState: RiggingState,
    animPose: KeyframePose,
    shading: ViewportShading,
    shader: PolyShaderPreset,
    xRay: Boolean,
    weightPaintOverlay: Boolean,
    subMode: SelectionSubMode,
    tint: Color,
    isSelected: Boolean
) {
    val bonesByRole = riggingState.bones.associateBy { it.partRole }
    val rawParts = listOf(
        SubMeshPart("head", PrimitiveShape.CUBE, 0f, 0.72f, 0f, 0.18f, 0.18f, 0.18f, tint.value.toLong()),
        SubMeshPart("torso", PrimitiveShape.CUBE, 0f, 0.25f, 0f, 0.28f, 0.28f, 0.16f, tint.value.toLong()),
        SubMeshPart("arm_l", PrimitiveShape.CUBE, -0.42f, 0.25f, 0f, 0.10f, 0.26f, 0.10f, tint.value.toLong()),
        SubMeshPart("arm_r", PrimitiveShape.CUBE, 0.42f, 0.25f, 0f, 0.10f, 0.26f, 0.10f, tint.value.toLong()),
        SubMeshPart("leg_l", PrimitiveShape.CUBE, -0.14f, -0.32f, 0f, 0.11f, 0.28f, 0.11f, tint.value.toLong()),
        SubMeshPart("leg_r", PrimitiveShape.CUBE, 0.14f, -0.32f, 0f, 0.11f, 0.28f, 0.11f, tint.value.toLong())
    )
    rawParts.map { part ->
        val (pos, half) = applyPartRigAndAnim(part, bonesByRole[part.role], animPose, sx, sy, sz)
        Triple(pos, half, project3D(pos, center, scale, yawDeg, pitchDeg).second)
    }.sortedByDescending { it.third }.forEach { (pos, half, _) ->
        drawBoxMesh(
            center = center,
            scale = scale,
            yawDeg = yawDeg,
            pitchDeg = pitchDeg,
            halfW = half.x,
            halfH = half.y,
            halfD = half.z,
            shading = shading,
            shader = shader,
            xRay = xRay,
            weightPaintOverlay = weightPaintOverlay,
            subMode = subMode,
            tint = tint,
            isSelected = isSelected,
            offsetLocal = pos
        )
    }
}

private fun DrawScope.drawVoxelModularStructure(
    center: Offset,
    scale: Float,
    yawDeg: Float,
    pitchDeg: Float,
    sx: Float,
    sy: Float,
    sz: Float,
    shading: ViewportShading,
    shader: PolyShaderPreset,
    xRay: Boolean,
    weightPaintOverlay: Boolean,
    subMode: SelectionSubMode,
    tint: Color,
    isSelected: Boolean
) {
    val voxels = listOf(
        Vec3(0f, -0.3f, 0f), Vec3(0.36f, -0.3f, 0f), Vec3(-0.36f, -0.3f, 0f),
        Vec3(0f, -0.3f, 0.36f), Vec3(0f, -0.3f, -0.36f),
        Vec3(0f, 0.06f, 0f), Vec3(0.36f, 0.06f, 0f), Vec3(-0.36f, 0.06f, 0f),
        Vec3(0f, 0.42f, 0f)
    ).map { Vec3(it.x * sx, it.y * sy, it.z * sz) }

    val sorted = voxels.map { v -> v to project3D(v, center, scale, yawDeg, pitchDeg).second }
        .sortedByDescending { it.second }

    for ((c, _) in sorted) {
        drawBoxMesh(
            center = center,
            scale = scale,
            yawDeg = yawDeg,
            pitchDeg = pitchDeg,
            halfW = 0.17f * sx,
            halfH = 0.17f * sy,
            halfD = 0.17f * sz,
            shading = shading,
            shader = shader,
            xRay = xRay,
            weightPaintOverlay = weightPaintOverlay,
            subMode = subMode,
            tint = tint,
            isSelected = isSelected,
            offsetLocal = c
        )
    }
}

fun DrawScope.drawSkeletalIkOverlay(
    center: Offset,
    scale: Float,
    yawDeg: Float,
    pitchDeg: Float,
    riggingState: RiggingState,
    animPose: KeyframePose
) {
    val bones = riggingState.bones
    val visibleCount = riggingState.revealedBonesCount.coerceIn(0, bones.size)
    if (visibleCount == 0) return

    val swing = Math.toRadians(animPose.limbSwingDeg.toDouble()).toFloat()
    val headRad = Math.toRadians(animPose.headBobDeg.toDouble()).toFloat()
    val bonesByRole = bones.associateBy { it.partRole }

    fun bonePoint(role: String, base: Vec3): Offset {
        val b = bonesByRole[role]
        val pRad = Math.toRadians((b?.pitchDeg ?: 0f).toDouble()).toFloat()
        val yRad = Math.toRadians((b?.yawDeg ?: 0f).toDouble()).toFloat()
        val shifted = when (role) {
            "arm_l", "wing_l" -> Vec3(base.x, base.y + sin(pRad + swing) * 0.18f, base.z + sin(swing) * 0.16f)
            "arm_r", "wing_r" -> Vec3(base.x, base.y + sin(pRad - swing) * 0.18f, base.z - sin(swing) * 0.16f)
            "leg_l" -> Vec3(base.x, base.y + kotlin.math.abs(sin(swing)) * 0.06f, base.z - sin(swing) * 0.2f)
            "leg_r" -> Vec3(base.x, base.y + kotlin.math.abs(sin(swing)) * 0.06f, base.z + sin(swing) * 0.2f)
            "head" -> Vec3(base.x + sin(yRad) * 0.1f, base.y + sin(headRad) * 0.06f, base.z + sin(pRad + headRad) * 0.08f)
            "tail" -> Vec3(base.x + sin(yRad + swing) * 0.16f, base.y + sin(pRad) * 0.1f, base.z)
            else -> Vec3(base.x + sin(yRad) * 0.05f, base.y, base.z + sin(pRad) * 0.05f)
        }
        return project3D(shifted, center, scale, yawDeg, pitchDeg).first
    }

    val pelvis = project3D(Vec3(0f, -0.02f, 0f), center, scale, yawDeg, pitchDeg).first
    val spine = bonePoint("torso", Vec3(0f, 0.36f, 0f))
    val head = bonePoint("head", Vec3(0f, 0.72f, 0.04f))
    val lArm = bonePoint("arm_l", Vec3(-0.52f, 0.20f, 0.10f))
    val rArm = bonePoint("arm_r", Vec3(0.52f, 0.20f, -0.10f))
    val lLeg = bonePoint("leg_l", Vec3(-0.20f, -0.58f, 0f))
    val rLeg = bonePoint("leg_r", Vec3(0.20f, -0.58f, 0f))

    val segments = buildList {
        add(Triple(pelvis, spine, bones.getOrNull(1)?.id))
        add(Triple(spine, head, bones.getOrNull(2)?.id))
        add(Triple(spine, lArm, bones.getOrNull(3)?.id))
        add(Triple(spine, rArm, bones.getOrNull(4)?.id))
        add(Triple(pelvis, lLeg, bones.getOrNull(5)?.id))
        add(Triple(pelvis, rLeg, bones.getOrNull(6)?.id))
        if (bonesByRole.containsKey("wing_l")) {
            add(Triple(spine, bonePoint("wing_l", Vec3(-0.80f, 0.44f, 0f)), bonesByRole["wing_l"]?.id))
            add(Triple(spine, bonePoint("wing_r", Vec3(0.80f, 0.44f, 0f)), bonesByRole["wing_r"]?.id))
        }
        if (bonesByRole.containsKey("tail")) {
            add(Triple(pelvis, bonePoint("tail", Vec3(0f, 0.0f, -0.72f)), bonesByRole["tail"]?.id))
        }
    }

    segments.take(visibleCount).forEach { (a, b, boneId) ->
        val selected = boneId == riggingState.selectedBoneId
        drawLine(
            color = if (selected) TerminalEmerald else PrecisionAmber,
            start = a,
            end = b,
            strokeWidth = if (selected) 4.2f else 3.0f,
            cap = StrokeCap.Round
        )
    }

    val joints = listOfNotNull(
        pelvis to bones.getOrNull(0),
        spine to bones.getOrNull(1),
        head to bones.getOrNull(2),
        lArm to bones.getOrNull(3),
        rArm to bones.getOrNull(4),
        lLeg to bones.getOrNull(5),
        rLeg to bones.getOrNull(6)
    ).take(visibleCount)

    joints.forEach { (pt, bone) ->
        val isSel = bone?.id == riggingState.selectedBoneId
        drawCircle(VoidBlack, radius = if (isSel) 7.5f else 5.5f, center = pt)
        drawCircle(
            color = when {
                isSel -> TerminalEmerald
                bone?.ikLocked == true -> SteelBlue
                else -> SlateSilver
            },
            radius = if (isSel) 5.0f else 3.6f,
            center = pt
        )
    }
}

fun DrawScope.drawActiveTransformGizmo(
    tool: ViewportTool,
    center: Offset,
    scale: Float,
    yawDeg: Float,
    pitchDeg: Float
) {
    if (tool != ViewportTool.MOVE && tool != ViewportTool.ROTATE && tool != ViewportTool.SCALE) return

    val origin = project3D(Vec3(0f, 0f, 0f), center, scale, yawDeg, pitchDeg).first
    if (tool == ViewportTool.ROTATE) {
        drawCircle(color = SteelBlue.copy(alpha = 0.7f), radius = scale * 0.9f, center = origin, style = Stroke(2f))
        drawCircle(color = CrimsonAlert.copy(alpha = 0.55f), radius = scale * 0.74f, center = origin, style = Stroke(1.6f))
        return
    }

    val axes = listOf(
        Vec3(0.95f, 0f, 0f) to CrimsonAlert,
        Vec3(0f, 0.95f, 0f) to TerminalEmerald,
        Vec3(0f, 0f, 0.95f) to SteelBlue
    )
    axes.forEach { (vec, col) ->
        val end = project3D(vec, center, scale, yawDeg, pitchDeg).first
        drawLine(color = col, start = origin, end = end, strokeWidth = 2.6f, cap = StrokeCap.Round)
        drawCircle(color = col, radius = if (tool == ViewportTool.SCALE) 4.5f else 5.2f, center = end)
    }
}
