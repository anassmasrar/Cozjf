package com.example.ui.panels

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.GamesModsSubTab
import com.example.ui.theme.*
import com.example.viewmodel.OmniStudioUiState
import com.example.viewmodel.OmniStudioViewModel

@Composable
fun GamesAndModsHubPanel(
    uiState: OmniStudioUiState,
    isWidePane: Boolean,
    viewModel: OmniStudioViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VoidBlack)
    ) {
        // 3 clear sub-sections inside Jeux & Mods
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PanelSurface)
                .border(1.dp, BorderHairline)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            GamesModsSubTab.entries.forEach { subTab ->
                val selected = subTab == uiState.gamesModsSubTab
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("subtab_${subTab.name}")
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { viewModel.selectGamesModsSubTab(subTab) },
                    color = if (selected) SteelBlueDim else ElevatedSurface,
                    border = BorderStroke(1.dp, if (selected) SteelBlue else BorderHairline),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = subTab.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (selected) TextPrimary else TextSecondary,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        )
                    }
                }
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (uiState.gamesModsSubTab) {
                GamesModsSubTab.BLOCS_CODE -> TrinityCodePanel(
                    blockCatalog = uiState.blockCatalog,
                    activeCategory = uiState.activeBlockCategory,
                    assembledBlocks = uiState.assembledBlocks,
                    codeViewMode = uiState.codeViewMode,
                    isWidePane = isWidePane,
                    onSelectCategory = viewModel::selectBlockCategory,
                    onAddBlock = viewModel::addBlockToStack,
                    onRemoveBlock = viewModel::removeBlockFromStack,
                    onSelectCodeView = viewModel::setCodeViewMode
                )
                GamesModsSubTab.MINECRAFT_JAR -> ModAtelierPanel(
                    modState = uiState.modBuild,
                    onSelectFormat = viewModel::selectModExportFormat,
                    onSelectAssetEngine = viewModel::selectModAssetEngine,
                    onSelectTargetType = viewModel::selectModTargetType,
                    onModAiPromptChange = viewModel::updateModAiPrompt,
                    onGenerateModWithClaude = viewModel::generateModWithClaude,
                    onUpdateConfig = viewModel::updateModConfig,
                    onUpdateHorror = viewModel::updateHorrorConfig,
                    onCompileJar = viewModel::compileMinecraftJar,
                    onProvideArchiveBytes = viewModel::getOrCreateModArchiveBytes,
                    onExportFeedback = viewModel::showStatusBanner
                )
                GamesModsSubTab.BUILD_NATIF -> BuildNatifPanel(
                    buildState = uiState.nativeBuild,
                    onToggleOptions = viewModel::toggleNativeBuildOption,
                    onUpdatePlayConfig = viewModel::updatePlayStoreConfig,
                    onCompilePlayAab = viewModel::compilePlayStoreAab,
                    onProvideAabBytes = viewModel::getOrCreatePlayStoreAabBytes,
                    onExportFeedback = viewModel::showStatusBanner,
                    onCompileExe = viewModel::compileWindowsExe,
                    onCompileApk = viewModel::compileAndroidApk
                )
            }
        }
    }
}
