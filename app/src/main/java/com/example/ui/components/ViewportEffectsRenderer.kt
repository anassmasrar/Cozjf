package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.ParticleConfig
import com.example.model.ParticlePreset
import com.example.model.PolyShaderPreset
import com.example.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun resolveEffectiveParticlePreset(
    shader: PolyShaderPreset,
    config: ParticleConfig
): ParticlePreset {
    if (config.preset != ParticlePreset.AUTO_SHADER) return config.preset
    return when (shader) {
        PolyShaderPreset.STANDARD_PBR -> ParticlePreset.SPARKS
        PolyShaderPreset.TOON_CEL -> ParticlePreset.COMIC_SPEEDLINES
        PolyShaderPreset.MATCAP_CLAY -> ParticlePreset.SCULPT_DUST
        PolyShaderPreset.GLASS_CRYSTAL -> ParticlePreset.PRISM_SHARDS
        PolyShaderPreset.HOLOGRAM_CYBER -> ParticlePreset.DATA_STREAM
        PolyShaderPreset.NEON_BLOOM -> ParticlePreset.SHOCKWAVE
        PolyShaderPreset.VHS_RETRO -> ParticlePreset.GLITCH_PIXELS
        PolyShaderPreset.LUMEN_RTGI -> ParticlePreset.FIREFLIES
        PolyShaderPreset.HORROR_FOG -> ParticlePreset.HORROR_ASH
        PolyShaderPreset.GOLD_CHROME -> ParticlePreset.SPARKS
        PolyShaderPreset.WATERCOLOR_INK -> ParticlePreset.PETALS
        PolyShaderPreset.THERMAL_WEIGHT -> ParticlePreset.FIRE_EMBERS
    }
}

data class ShaderStyleVisual(
    val faceColor: Color,
    val edgeColor: Color,
    val edgeWidth: Float,
    val alpha: Float,
    val specularHighlight: Color?
)

fun computeShaderFaceVisual(
    shader: PolyShaderPreset,
    baseTint: Color,
    shadeFactor: Float,
    baseAlpha: Float,
    isSelected: Boolean,
    weightPaintOverlay: Boolean
): ShaderStyleVisual {
    if (weightPaintOverlay) {
        val heat = Color(
            red = (0.35f + 0.65f * shadeFactor).coerceIn(0f, 1f),
            green = (0.85f * (1f - shadeFactor)).coerceIn(0f, 1f),
            blue = (0.9f * (1f - shadeFactor)).coerceIn(0f, 1f),
            alpha = baseAlpha
        )
        return ShaderStyleVisual(heat, PrecisionAmber, if (isSelected) 1.7f else 1.0f, baseAlpha, null)
    }

    return when (shader) {
        PolyShaderPreset.STANDARD_PBR -> {
            val l = (0.5f + 0.5f * shadeFactor).coerceIn(0.35f, 1f)
            ShaderStyleVisual(
                faceColor = Color(baseTint.red * l, baseTint.green * l, baseTint.blue * l, baseAlpha),
                edgeColor = if (isSelected) SteelBlue else SlateSilver.copy(alpha = 0.65f),
                edgeWidth = if (isSelected) 1.5f else 1.0f,
                alpha = baseAlpha,
                specularHighlight = Color.White.copy(alpha = 0.18f)
            )
        }
        PolyShaderPreset.TOON_CEL -> {
            val band = when {
                shadeFactor > 0.72f -> 1.0f
                shadeFactor > 0.42f -> 0.72f
                else -> 0.46f
            }
            ShaderStyleVisual(
                faceColor = Color((baseTint.red * band).coerceIn(0f, 1f), (baseTint.green * band).coerceIn(0f, 1f), (baseTint.blue * band).coerceIn(0f, 1f), baseAlpha),
                edgeColor = Color(0xFF0A0A0C),
                edgeWidth = if (isSelected) 2.6f else 2.2f,
                alpha = baseAlpha,
                specularHighlight = Color.White.copy(alpha = 0.30f)
            )
        }
        PolyShaderPreset.MATCAP_CLAY -> {
            val clay = Color(0xFFD97757)
            val l = (0.48f + 0.52f * shadeFactor).coerceIn(0.4f, 1f)
            ShaderStyleVisual(
                faceColor = Color(clay.red * l, clay.green * l, clay.blue * l, baseAlpha),
                edgeColor = Color(0xFF5C2E20).copy(alpha = 0.7f),
                edgeWidth = 1.1f,
                alpha = baseAlpha,
                specularHighlight = Color(0xFFFFD8C2).copy(alpha = 0.25f)
            )
        }
        PolyShaderPreset.GLASS_CRYSTAL -> {
            val crystal = Color(0xFF38BDF8)
            val l = (0.65f + 0.35f * shadeFactor).coerceIn(0.5f, 1f)
            ShaderStyleVisual(
                faceColor = Color(crystal.red * l, crystal.green * l, crystal.blue * l, (baseAlpha * 0.36f).coerceAtMost(0.42f)),
                edgeColor = Color(0xFFBAE6FD).copy(alpha = 0.9f),
                edgeWidth = 1.6f,
                alpha = 0.38f,
                specularHighlight = Color.White.copy(alpha = 0.45f)
            )
        }
        PolyShaderPreset.HOLOGRAM_CYBER -> {
            ShaderStyleVisual(
                faceColor = Color(0xFF06B6D4).copy(alpha = (baseAlpha * 0.24f).coerceAtMost(0.28f)),
                edgeColor = Color(0xFF22D3EE),
                edgeWidth = 1.5f,
                alpha = 0.28f,
                specularHighlight = Color(0xFFA5F3FC).copy(alpha = 0.25f)
            )
        }
        PolyShaderPreset.NEON_BLOOM -> {
            val l = (0.6f + 0.4f * shadeFactor).coerceIn(0.5f, 1f)
            ShaderStyleVisual(
                faceColor = Color(0xFF062E24).copy(alpha = baseAlpha),
                edgeColor = Color(0xFF34D399).copy(alpha = l),
                edgeWidth = 2.4f,
                alpha = baseAlpha,
                specularHighlight = Color(0xFF6EE7B7).copy(alpha = 0.35f)
            )
        }
        PolyShaderPreset.VHS_RETRO -> {
            val l = (0.45f + 0.55f * shadeFactor).coerceIn(0.4f, 1f)
            ShaderStyleVisual(
                faceColor = Color(baseTint.red * l * 0.95f, baseTint.green * l * 0.85f, (baseTint.blue * l * 1.15f).coerceAtMost(1f), baseAlpha),
                edgeColor = Color(0xFFF43F5E).copy(alpha = 0.8f),
                edgeWidth = 1.4f,
                alpha = baseAlpha,
                specularHighlight = Color(0xFF38BDF8).copy(alpha = 0.2f)
            )
        }
        PolyShaderPreset.LUMEN_RTGI -> {
            val warm = (0.55f + 0.45f * shadeFactor).coerceIn(0.45f, 1f)
            ShaderStyleVisual(
                faceColor = Color(
                    (baseTint.red * warm + 0.08f).coerceAtMost(1f),
                    (baseTint.green * warm + 0.07f).coerceAtMost(1f),
                    (baseTint.blue * warm + 0.11f).coerceAtMost(1f),
                    baseAlpha
                ),
                edgeColor = Color(0xFF93C5FD).copy(alpha = 0.65f),
                edgeWidth = 1.2f,
                alpha = baseAlpha,
                specularHighlight = Color(0xFFFEF3C7).copy(alpha = 0.28f)
            )
        }
        PolyShaderPreset.HORROR_FOG -> {
            val cold = (0.28f + 0.42f * shadeFactor).coerceIn(0.25f, 0.72f)
            ShaderStyleVisual(
                faceColor = Color(baseTint.red * cold * 0.8f, baseTint.green * cold * 0.75f, baseTint.blue * cold * 0.85f, baseAlpha),
                edgeColor = Color(0xFFEF4444).copy(alpha = 0.55f),
                edgeWidth = 1.3f,
                alpha = baseAlpha,
                specularHighlight = Color(0xFFFCA5A5).copy(alpha = 0.12f)
            )
        }
        PolyShaderPreset.GOLD_CHROME -> {
            val band = if ((shadeFactor * 6).toInt() % 2 == 0) 0.95f else 0.58f
            ShaderStyleVisual(
                faceColor = Color(0.96f * band, 0.76f * band, 0.24f * band, baseAlpha),
                edgeColor = Color(0xFFFEF08A).copy(alpha = 0.85f),
                edgeWidth = 1.4f,
                alpha = baseAlpha,
                specularHighlight = Color.White.copy(alpha = 0.4f)
            )
        }
        PolyShaderPreset.WATERCOLOR_INK -> {
            val soft = (0.62f + 0.35f * shadeFactor).coerceIn(0.55f, 1f)
            ShaderStyleVisual(
                faceColor = Color(
                    (baseTint.red * soft + 0.18f).coerceAtMost(1f),
                    (baseTint.green * soft + 0.20f).coerceAtMost(1f),
                    (baseTint.blue * soft + 0.18f).coerceAtMost(1f),
                    (baseAlpha * 0.82f)
                ),
                edgeColor = Color(0xFF334155).copy(alpha = 0.85f),
                edgeWidth = 1.6f,
                alpha = 0.82f,
                specularHighlight = null
            )
        }
        PolyShaderPreset.THERMAL_WEIGHT -> {
            val heatColor = Color(
                red = (shadeFactor * 1.15f).coerceIn(0.15f, 1f),
                green = (1f - kotlin.math.abs(shadeFactor - 0.5f) * 1.8f).coerceIn(0.1f, 0.95f),
                blue = (1f - shadeFactor).coerceIn(0.1f, 1f),
                alpha = baseAlpha
            )
            ShaderStyleVisual(
                faceColor = heatColor,
                edgeColor = Color.White.copy(alpha = 0.45f),
                edgeWidth = 1.0f,
                alpha = baseAlpha,
                specularHighlight = null
            )
        }
    }
}

fun DrawScope.drawShaderBackground(shader: PolyShaderPreset, width: Float, height: Float) {
    when (shader) {
        PolyShaderPreset.LUMEN_RTGI -> drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF233247), Color(0xFF141820), VoidBlack),
                center = Offset(width * 0.5f, height * 0.32f),
                radius = maxOf(width, height) * 0.82f
            )
        )
        PolyShaderPreset.HORROR_FOG -> drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF1C1317), Color(0xFF0E0E12), VoidBlack),
                center = Offset(width * 0.5f, height * 0.45f),
                radius = maxOf(width, height) * 0.72f
            )
        )
        PolyShaderPreset.HOLOGRAM_CYBER, PolyShaderPreset.NEON_BLOOM -> drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF091318), Color(0xFF0E161B), Color(0xFF070B0E))
            )
        )
        PolyShaderPreset.WATERCOLOR_INK -> drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF1D2024), Color(0xFF181A1E), Color(0xFF121316))
            )
        )
        else -> drawRect(color = Color(0xFF131416))
    }
}

fun DrawScope.drawShaderPostOverlay(shader: PolyShaderPreset, width: Float, height: Float, fxTimeSec: Float) {
    when (shader) {
        PolyShaderPreset.HOLOGRAM_CYBER -> {
            val step = 14f
            var y = (fxTimeSec * 24f) % step
            while (y < height) {
                drawLine(Color(0xFF22D3EE).copy(alpha = 0.08f), Offset(0f, y), Offset(width, y), strokeWidth = 1f)
                y += step
            }
        }
        PolyShaderPreset.VHS_RETRO -> {
            val step = 9f
            var y = 0f
            while (y < height) {
                drawLine(Color.Black.copy(alpha = 0.22f), Offset(0f, y), Offset(width, y), strokeWidth = 1.2f)
                y += step
            }
            drawRect(
                color = Color(0xFFF43F5E).copy(alpha = 0.05f),
                topLeft = Offset(3f, 0f),
                size = Size(width, height)
            )
        }
        PolyShaderPreset.HORROR_FOG -> {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, Color(0xFF070709).copy(alpha = 0.65f)),
                    center = Offset(width * 0.5f, height * 0.5f),
                    radius = maxOf(width, height) * 0.62f
                )
            )
        }
        PolyShaderPreset.NEON_BLOOM -> {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF34D399).copy(alpha = 0.14f), Color.Transparent),
                    center = Offset(width * 0.5f, height * 0.52f),
                    radius = minOf(width, height) * 0.42f
                ),
                center = Offset(width * 0.5f, height * 0.52f),
                radius = minOf(width, height) * 0.42f
            )
        }
        else -> Unit
    }
}

fun DrawScope.drawAdaptiveParticles(
    shader: PolyShaderPreset,
    config: ParticleConfig,
    width: Float,
    height: Float,
    fxTimeSec: Float
) {
    if (!config.enabled) return
    val activePreset = resolveEffectiveParticlePreset(shader, config)
    val count = config.density.coerceIn(8, 120)
    val t = fxTimeSec * config.speed
    val turb = config.turbulence
    val pSize = config.size

    val palette = particlePaletteFor(activePreset, shader)

    for (i in 0 until count) {
        val seedA = ((i * 73 + 19) % 1000) / 1000f
        val seedB = ((i * 197 + 43) % 1000) / 1000f
        val seedC = ((i * 313 + 71) % 1000) / 1000f
        val col = palette[i % palette.size]

        when (activePreset) {
            ParticlePreset.RAIN -> {
                val progress = (seedB + t * (0.85f + seedC * 0.4f)) % 1f
                val x = (seedA * width + progress * 48f * turb) % width
                val y = progress * height
                drawLine(col.copy(alpha = 0.55f), Offset(x, y), Offset(x - 5f, y + 18f * pSize), strokeWidth = 1.6f * pSize, cap = StrokeCap.Round)
            }
            ParticlePreset.SNOW -> {
                val progress = (seedB + t * (0.16f + seedC * 0.12f)) % 1f
                val x = (seedA * width + sin(t * 1.8f + seedC * 12f) * 26f * turb + width) % width
                val y = progress * height
                drawCircle(col.copy(alpha = 0.78f), radius = (2.2f + seedC * 2.4f) * pSize, center = Offset(x, y))
            }
            ParticlePreset.FIRE_EMBERS, ParticlePreset.SPARKS -> {
                val progress = (seedB + t * (0.35f + seedC * 0.35f)) % 1f
                val x = (seedA * width + sin(t * 3.5f + i) * 32f * turb + width) % width
                val y = height * (1f - progress)
                val alpha = (1f - progress).coerceIn(0.15f, 0.9f)
                if (activePreset == ParticlePreset.SPARKS) {
                    drawLine(col.copy(alpha = alpha), Offset(x, y), Offset(x + cos(seedC * 6f) * 9f * pSize, y + 9f * pSize), strokeWidth = 2f * pSize)
                } else {
                    drawCircle(col.copy(alpha = alpha), radius = (2.6f + seedC * 2.5f) * pSize, center = Offset(x, y))
                }
            }
            ParticlePreset.SMOKE, ParticlePreset.HORROR_ASH, ParticlePreset.SCULPT_DUST -> {
                val progress = (seedB + t * (0.12f + seedC * 0.1f)) % 1f
                val x = (seedA * width + cos(t * 1.2f + i) * 38f * turb + width) % width
                val y = if (activePreset == ParticlePreset.HORROR_ASH) progress * height else height * (1f - progress * 0.85f)
                val r = if (activePreset == ParticlePreset.SMOKE) (8f + seedC * 12f) * pSize else (2.5f + seedC * 3f) * pSize
                drawCircle(col.copy(alpha = if (activePreset == ParticlePreset.SMOKE) 0.14f else 0.52f), radius = r, center = Offset(x, y))
            }
            ParticlePreset.DATA_STREAM, ParticlePreset.GLITCH_PIXELS -> {
                val progress = (seedB + t * (0.45f + seedC * 0.35f)) % 1f
                val x = ((seedA * 24).toInt() / 24f) * width
                val y = progress * height
                val len = (8f + seedC * 12f) * pSize
                if (activePreset == ParticlePreset.DATA_STREAM) {
                    drawLine(col.copy(alpha = 0.65f), Offset(x, y), Offset(x, y + len), strokeWidth = 2.2f * pSize)
                } else {
                    drawRect(col.copy(alpha = 0.55f), topLeft = Offset(x, y), size = Size(12f * pSize, 4f * pSize))
                }
            }
            ParticlePreset.SHOCKWAVE -> {
                if (i < 5) {
                    val wave = ((t * 0.38f + i * 0.2f) % 1f)
                    val radius = wave * minOf(width, height) * 0.48f
                    drawCircle(
                        color = col.copy(alpha = (1f - wave) * 0.55f),
                        radius = radius,
                        center = Offset(width * 0.5f, height * 0.54f),
                        style = Stroke(width = (2.5f * pSize).coerceAtLeast(1f))
                    )
                }
            }
            ParticlePreset.COMIC_SPEEDLINES -> {
                if (i % 2 == 0) {
                    val angle = seedA * 2f * PI.toFloat() + (t * 0.15f * turb)
                    val c = Offset(width * 0.5f, height * 0.54f)
                    val rStart = minOf(width, height) * (0.34f + ((t + seedB) % 0.15f))
                    val rEnd = rStart + (24f + seedC * 28f) * pSize
                    drawLine(
                        color = col.copy(alpha = 0.48f),
                        start = Offset(c.x + cos(angle) * rStart, c.y + sin(angle) * rStart),
                        end = Offset(c.x + cos(angle) * rEnd, c.y + sin(angle) * rEnd),
                        strokeWidth = 2f * pSize,
                        cap = StrokeCap.Round
                    )
                }
            }
            ParticlePreset.PRISM_SHARDS, ParticlePreset.PETALS -> {
                val progress = (seedB + t * (0.22f + seedC * 0.2f)) % 1f
                val x = (seedA * width + sin(t * 2f + i) * 30f * turb + width) % width
                val y = progress * height
                val s = (4.5f + seedC * 4f) * pSize
                val path = Path().apply {
                    moveTo(x, y - s)
                    lineTo(x + s * 0.8f, y + s * 0.4f)
                    lineTo(x - s * 0.7f, y + s * 0.8f)
                    close()
                }
                drawPath(path, col.copy(alpha = 0.65f))
            }
            ParticlePreset.BUBBLES -> {
                val progress = (seedB + t * (0.2f + seedC * 0.18f)) % 1f
                val x = (seedA * width + sin(t * 2.5f + i) * 16f * turb + width) % width
                val y = height * (1f - progress)
                drawCircle(col.copy(alpha = 0.55f), radius = (3.5f + seedC * 4.5f) * pSize, center = Offset(x, y), style = Stroke(1.4f))
            }
            ParticlePreset.FIREFLIES, ParticlePreset.STARFIELD, ParticlePreset.AUTO_SHADER -> {
                val pulse = 0.35f + 0.65f * ((sin(t * 3f + i * 1.4f) + 1f) * 0.5f)
                val x = (seedA * width + cos(t * 0.8f + i) * 22f * turb + width) % width
                val y = (seedB * height + sin(t * 0.9f + i) * 20f * turb + height) % height
                drawCircle(col.copy(alpha = pulse * 0.85f), radius = (2.2f + seedC * 2.8f) * pSize, center = Offset(x, y))
            }
        }
    }
}

private fun particlePaletteFor(preset: ParticlePreset, shader: PolyShaderPreset): List<Color> {
    return when (preset) {
        ParticlePreset.FIRE_EMBERS -> listOf(Color(0xFFF59E0B), Color(0xFFEF4444), Color(0xFFFDE047))
        ParticlePreset.RAIN -> listOf(Color(0xFF60A5FA), Color(0xFF93C5FD))
        ParticlePreset.SNOW -> listOf(Color.White, Color(0xFFBAE6FD))
        ParticlePreset.DATA_STREAM -> listOf(Color(0xFF22D3EE), Color(0xFF34D399))
        ParticlePreset.GLITCH_PIXELS -> listOf(Color(0xFFF43F5E), Color(0xFF22D3EE), Color(0xFFFACC15))
        ParticlePreset.HORROR_ASH -> listOf(Color(0xFFEF4444), Color(0xFF94A3B8), Color(0xFF64748B))
        ParticlePreset.PETALS -> listOf(Color(0xFF34D399), Color(0xFFA7F3D0), Color(0xFFFDE68A))
        ParticlePreset.PRISM_SHARDS -> listOf(Color(0xFF38BDF8), Color(0xFFE0F2FE), Color(0xFFA5F3FC))
        ParticlePreset.COMIC_SPEEDLINES -> listOf(Color(0xFFF59E0B), Color.White, Color(0xFF4A9EFF))
        ParticlePreset.SCULPT_DUST -> listOf(Color(0xFFFDBA74), Color(0xFFD6D3D1))
        else -> listOf(Color(shader.accentHex), SlateSilver, SteelBlue)
    }
}
