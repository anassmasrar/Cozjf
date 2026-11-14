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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BlockCategory
import com.example.model.CodeViewMode
import com.example.model.LogicBlock
import com.example.ui.theme.*

@Composable
fun TrinityCodePanel(
    blockCatalog: List<LogicBlock>,
    activeCategory: BlockCategory,
    assembledBlocks: List<LogicBlock>,
    codeViewMode: CodeViewMode,
    isWidePane: Boolean,
    onSelectCategory: (BlockCategory) -> Unit,
    onAddBlock: (LogicBlock) -> Unit,
    onRemoveBlock: (String) -> Unit,
    onSelectCodeView: (CodeViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PanelSurface)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TRINITÉ BLOCS / NODES / CODE",
                style = MaterialTheme.typography.labelLarge.copy(color = TextPrimary)
            )
            Text(
                text = "${assembledBlocks.size} BLOCS",
                style = MaterialTheme.typography.labelSmall.copy(color = SteelBlue)
            )
        }

        if (isWidePane) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BlocksWorkspaceSection(
                    blockCatalog = blockCatalog,
                    activeCategory = activeCategory,
                    assembledBlocks = assembledBlocks,
                    onSelectCategory = onSelectCategory,
                    onAddBlock = onAddBlock,
                    onRemoveBlock = onRemoveBlock,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                LiveTranslationSection(
                    assembledBlocks = assembledBlocks,
                    codeViewMode = codeViewMode,
                    onSelectCodeView = onSelectCodeView,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BlocksWorkspaceSection(
                    blockCatalog = blockCatalog,
                    activeCategory = activeCategory,
                    assembledBlocks = assembledBlocks,
                    onSelectCategory = onSelectCategory,
                    onAddBlock = onAddBlock,
                    onRemoveBlock = onRemoveBlock,
                    modifier = Modifier.fillMaxWidth()
                )
                LiveTranslationSection(
                    assembledBlocks = assembledBlocks,
                    codeViewMode = codeViewMode,
                    onSelectCodeView = onSelectCodeView,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp)
                )
            }
        }
    }
}

@Composable
private fun BlocksWorkspaceSection(
    blockCatalog: List<LogicBlock>,
    activeCategory: BlockCategory,
    assembledBlocks: List<LogicBlock>,
    onSelectCategory: (BlockCategory) -> Unit,
    onAddBlock: (LogicBlock) -> Unit,
    onRemoveBlock: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(ElevatedSurface, RoundedCornerShape(4.dp))
            .border(1.dp, BorderHairline, RoundedCornerShape(4.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            BlockCategory.entries.forEach { cat ->
                val catColor = categoryColor(cat)
                val selected = cat == activeCategory
                Surface(
                    modifier = Modifier
                        .testTag("block_cat_${cat.name}")
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onSelectCategory(cat) },
                    color = if (selected) catColor.copy(alpha = 0.18f) else VoidBlack,
                    border = BorderStroke(1.dp, if (selected) catColor else BorderHairline),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = cat.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (selected) catColor else TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            blockCatalog.filter { it.category == activeCategory }.forEach { block ->
                val catColor = categoryColor(block.category)
                Surface(
                    modifier = Modifier
                        .testTag("add_block_${block.id}")
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onAddBlock(block) },
                    color = CardSurface,
                    border = BorderStroke(1.dp, catColor.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = catColor, modifier = Modifier.size(13.dp))
                        Text(
                            text = block.title,
                            style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary)
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = BorderHairline)

        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            assembledBlocks.forEachIndexed { index, block ->
                val accent = categoryColor(block.category)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardSurface, RoundedCornerShape(4.dp))
                        .border(1.dp, BorderHairline, RoundedCornerShape(4.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.width(4.dp).height(34.dp).background(accent))
                    Text(
                        text = "0${index + 1} ${block.title}",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary),
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    )
                    IconButton(onClick = { onRemoveBlock(block.id) }, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Retirer", tint = TextSecondary, modifier = Modifier.size(13.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveTranslationSection(
    assembledBlocks: List<LogicBlock>,
    codeViewMode: CodeViewMode,
    onSelectCodeView: (CodeViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(VoidBlack, RoundedCornerShape(4.dp))
            .border(1.dp, BorderHairline, RoundedCornerShape(4.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (codeViewMode == CodeViewMode.GEOMETRY_NODES) Icons.Outlined.AccountTree else Icons.Outlined.Code,
                contentDescription = null,
                tint = SteelBlue,
                modifier = Modifier.size(14.dp)
            )
            CodeViewMode.entries.forEach { mode ->
                val selected = mode == codeViewMode
                Surface(
                    modifier = Modifier
                        .testTag("code_mode_${mode.name}")
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onSelectCodeView(mode) },
                    color = if (selected) SteelBlueDim else ElevatedSurface,
                    border = BorderStroke(1.dp, if (selected) SteelBlue else BorderHairline),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = mode.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (selected) TextPrimary else TextSecondary
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (codeViewMode == CodeViewMode.GEOMETRY_NODES) {
            GeometryNodeGraphCanvas(
                blocks = assembledBlocks,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp)
                    .background(Color(0xFF101214), RoundedCornerShape(4.dp))
                    .border(1.dp, BorderHairline, RoundedCornerShape(4.dp))
            )
        } else {
            val codeText = buildTranslatedCode(assembledBlocks, codeViewMode)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 135.dp)
                    .background(Color(0xFF0E1012), RoundedCornerShape(4.dp))
                    .border(1.dp, BorderHairline, RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = codeText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = when (codeViewMode) {
                            CodeViewMode.JAVA -> PrecisionAmber
                            CodeViewMode.PYTHON -> SteelBlue
                            CodeViewMode.GLSL -> TerminalEmerald
                            else -> TextPrimary
                        },
                        lineHeight = 14.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun GeometryNodeGraphCanvas(
    blocks: List<LogicBlock>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 18.dp.toPx()
            var x = 0f
            while (x < size.width) {
                var y = 0f
                while (y < size.height) {
                    drawCircle(Color(0xFF22252A), radius = 1.1f, center = Offset(x, y))
                    y += step
                }
                x += step
            }

            if (blocks.isEmpty()) return@Canvas

            val nodeCount = blocks.size.coerceAtMost(4)
            val nodeW = (size.width - 30.dp.toPx()) / nodeCount.coerceAtLeast(1)
            val centerY = size.height * 0.52f

            for (i in 0 until nodeCount - 1) {
                val startX = 15.dp.toPx() + i * nodeW + (nodeW - 12.dp.toPx())
                val endX = 15.dp.toPx() + (i + 1) * nodeW
                val start = Offset(startX, centerY)
                val end = Offset(endX, centerY)
                val path = Path().apply {
                    moveTo(start.x, start.y)
                    cubicTo(start.x + 16.dp.toPx(), start.y, end.x - 16.dp.toPx(), end.y, end.x, end.y)
                }
                drawPath(path, color = SteelBlue, style = Stroke(width = 2.2f))
                drawCircle(color = SlateSilver, radius = 3.5f, center = start)
                drawCircle(color = SteelBlue, radius = 3.5f, center = end)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            blocks.take(4).forEach { block ->
                val accent = categoryColor(block.category)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(ElevatedSurface.copy(alpha = 0.94f), RoundedCornerShape(4.dp))
                        .border(1.dp, accent.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(accent.copy(alpha = 0.22f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = block.category.label,
                            style = MaterialTheme.typography.labelSmall.copy(color = accent, fontSize = 8.sp),
                            maxLines = 1
                        )
                    }
                    Text(
                        text = block.nodeTitle,
                        style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary, fontSize = 9.sp),
                        modifier = Modifier.padding(5.dp),
                        maxLines = 2
                    )
                }
            }
        }
    }
}

private fun buildTranslatedCode(blocks: List<LogicBlock>, mode: CodeViewMode): String {
    if (blocks.isEmpty()) return "// Aucun bloc actif"
    return when (mode) {
        CodeViewMode.JAVA -> buildString {
            blocks.forEach { appendLine(it.javaSnippet) }
            if (blocks.any { it.category == BlockCategory.CONDITIONS }) appendLine("  }")
            if (blocks.any { it.category == BlockCategory.EVENEMENTS }) appendLine("}")
        }
        CodeViewMode.PYTHON -> buildString {
            blocks.forEach { appendLine(it.pythonSnippet) }
        }
        CodeViewMode.GLSL -> buildString {
            appendLine("void mainImage(out vec4 fragColor, in vec2 vUv) {")
            blocks.forEach { appendLine(it.glslSnippet) }
            appendLine("}")
        }
        CodeViewMode.GEOMETRY_NODES -> ""
    }
}

private fun categoryColor(category: BlockCategory): Color = when (category) {
    BlockCategory.EVENEMENTS -> PrecisionAmber
    BlockCategory.CONDITIONS -> SteelBlue
    BlockCategory.ACTIONS -> TerminalEmerald
    BlockCategory.FX -> SlateSilver
}
