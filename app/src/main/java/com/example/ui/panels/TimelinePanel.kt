package com.example.ui.panels

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TimelineState
import com.example.ui.theme.*

@Composable
fun TimelinePanel(
    timeline: TimelineState,
    compactMode: Boolean,
    onTogglePlay: () -> Unit,
    onScrubFrame: (Int) -> Unit,
    onToggleKeyframe: () -> Unit,
    onSelectClip: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isKeyframeAtCurrent = timeline.currentFrame in timeline.keyframes

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PanelSurface)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Blender-style Transport & Frame Range Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                IconButton(
                    onClick = { onScrubFrame(timeline.startFrame) },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(Icons.Filled.SkipPrevious, contentDescription = "Début", tint = TextSecondary, modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (timeline.isPlaying) SteelBlueDim else ElevatedSurface)
                        .border(1.dp, if (timeline.isPlaying) SteelBlue else BorderHairline, RoundedCornerShape(4.dp))
                        .testTag("timeline_play_btn")
                ) {
                    Icon(
                        imageVector = if (timeline.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Lecture / Pause",
                        tint = if (timeline.isPlaying) SteelBlue else TextPrimary,
                        modifier = Modifier.size(17.dp)
                    )
                }
                IconButton(
                    onClick = { onScrubFrame(timeline.endFrame) },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(Icons.Filled.SkipNext, contentDescription = "Fin", tint = TextSecondary, modifier = Modifier.size(16.dp))
                }

                // Bouton Keyframe explicite et bien lisible
                Surface(
                    modifier = Modifier
                        .testTag("timeline_keyframe_btn")
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(onClick = onToggleKeyframe),
                    color = if (isKeyframeAtCurrent) PrecisionAmberDim else ElevatedSurface,
                    border = BorderStroke(1.dp, if (isKeyframeAtCurrent) PrecisionAmber else BorderHairline),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FiberManualRecord,
                            contentDescription = "Ajouter ou retirer Keyframe",
                            tint = PrecisionAmber,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = if (isKeyframeAtCurrent) "Suppr. Key" else "+ Keyframe",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isKeyframeAtCurrent) PrecisionAmber else TextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            // Frame Readouts
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(SteelBlueDim, RoundedCornerShape(3.dp))
                        .border(1.dp, SteelBlue, RoundedCornerShape(3.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "F${timeline.currentFrame}",
                        style = MaterialTheme.typography.labelMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                    )
                }
                Text(
                    text = "${timeline.keyframes.size} keys",
                    style = MaterialTheme.typography.labelSmall.copy(color = PrecisionAmber, fontSize = 10.sp)
                )
            }
        }

        // Interactive Frame Scrubber + Keyframe Diamonds
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .background(VoidBlack, RoundedCornerShape(4.dp))
                .border(1.dp, BorderHairline, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val span = (timeline.endFrame - timeline.startFrame).coerceAtLeast(1)

                for (tick in 0..20) {
                    val x = (tick / 20f) * w
                    drawLine(
                        color = BorderHairline,
                        start = Offset(x, 0f),
                        end = Offset(x, if (tick % 5 == 0) h * 0.45f else h * 0.24f),
                        strokeWidth = 1f
                    )
                }

                timeline.keyframes.forEach { kf ->
                    val ratio = ((kf - timeline.startFrame).toFloat() / span).coerceIn(0f, 1f)
                    val cx = ratio * w
                    val cy = h * 0.62f
                    val d = 4.5.dp.toPx()
                    val diamond = Path().apply {
                        moveTo(cx, cy - d)
                        lineTo(cx + d, cy)
                        lineTo(cx, cy + d)
                        lineTo(cx - d, cy)
                        close()
                    }
                    drawPath(diamond, color = PrecisionAmber)
                }

                val playRatio = ((timeline.currentFrame - timeline.startFrame).toFloat() / span).coerceIn(0f, 1f)
                val px = playRatio * w
                drawLine(color = SteelBlue, start = Offset(px, 0f), end = Offset(px, h), strokeWidth = 2.dp.toPx())
            }

            Slider(
                value = timeline.currentFrame.toFloat(),
                onValueChange = { onScrubFrame(it.toInt()) },
                valueRange = timeline.startFrame.toFloat()..timeline.endFrame.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = SteelBlue.copy(alpha = 0.0f),
                    activeTrackColor = VoidBlack.copy(alpha = 0f),
                    inactiveTrackColor = VoidBlack.copy(alpha = 0f)
                ),
                modifier = Modifier.fillMaxSize()
            )
        }

        // Animation Clips Layer Row (visible en mode non-compact)
        if (!compactMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                timeline.clips.forEach { clip ->
                    val selected = clip.id == timeline.selectedClipId
                    Surface(
                        modifier = Modifier
                            .testTag("anim_clip_${clip.id}")
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onSelectClip(clip.id) },
                        color = if (selected) SteelBlueDim else ElevatedSurface,
                        border = BorderStroke(1.dp, if (selected) SteelBlue else BorderHairline),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DirectionsRun,
                                contentDescription = clip.name,
                                tint = if (selected) SteelBlue else TextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "${clip.name} (${clip.frames}f)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (selected) TextPrimary else TextSecondary,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
