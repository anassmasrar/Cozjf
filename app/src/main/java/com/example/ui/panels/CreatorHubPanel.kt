package com.example.ui.panels

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.ViewInAr
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.BotCheckResult
import com.example.model.CommunityAsset
import com.example.ui.theme.*

@Composable
fun CreatorHubPanel(
    searchQuery: String,
    selectedCategory: String,
    assets: List<CommunityAsset>,
    statusNotice: String?,
    onSearchChange: (String) -> Unit,
    onSelectCategory: (String) -> Unit,
    onImportAsset: (CommunityAsset) -> Unit,
    onOpenPublishModal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("Tous", "Meshes 3D", "Mods Horreur", "Shaders", "Rigs IK")
    val filtered = assets.filter { asset ->
        val matchesCategory = selectedCategory == "Tous" || asset.category == selectedCategory
        val matchesQuery = searchQuery.isBlank() ||
            asset.title.contains(searchQuery, ignoreCase = true) ||
            asset.author.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesQuery
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PanelSurface)
            .verticalScroll(rememberScrollState())
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CREATOR HUB & MODÉRATION PAR BOTS",
                style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary)
            )
            OutlinedButton(
                onClick = onOpenPublishModal,
                modifier = Modifier
                    .testTag("open_publish_hub_btn")
                    .heightIn(min = 38.dp),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, SteelBlue),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Filled.AddCircleOutline, contentDescription = null, tint = SteelBlue, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Publier", style = MaterialTheme.typography.labelMedium.copy(color = SteelBlue))
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            leadingIcon = {
                Icon(Icons.Filled.Search, contentDescription = "Rechercher", tint = TextSecondary, modifier = Modifier.size(18.dp))
            },
            placeholder = {
                Text("Rechercher assets, rigs, shaders, auteurs...", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted))
            },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall.copy(color = TextPrimary),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SteelBlue,
                unfocusedBorderColor = BorderHairline,
                focusedContainerColor = VoidBlack,
                unfocusedContainerColor = VoidBlack
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hub_search_input")
        )

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { cat ->
                val active = cat == selectedCategory
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onSelectCategory(cat) },
                    color = if (active) SteelBlueDim else ElevatedSurface,
                    border = BorderStroke(1.dp, if (active) SteelBlue else BorderHairline),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = cat,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (active) TextPrimary else TextSecondary
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        if (statusNotice != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TerminalEmeraldDim, RoundedCornerShape(4.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = TerminalEmerald, modifier = Modifier.size(15.dp))
                Text(statusNotice, style = MaterialTheme.typography.labelSmall.copy(color = TerminalEmerald))
            }
        }

        // Exploration Asset Cards Grid
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            filtered.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { asset ->
                        CommunityAssetCard(
                            asset = asset,
                            onImport = { onImportAsset(asset) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CommunityAssetCard(
    asset: CommunityAsset,
    onImport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = ElevatedSurface,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, BorderHairline)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp)
                    .background(VoidBlack),
                contentAlignment = Alignment.Center
            ) {
                if (asset.thumbnailRes != null) {
                    Image(
                        painter = painterResource(id = asset.thumbnailRes),
                        contentDescription = asset.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.ViewInAr,
                        contentDescription = asset.title,
                        tint = SteelBlue.copy(alpha = 0.7f),
                        modifier = Modifier.size(34.dp)
                    )
                }
                // Bot Verification Badge
                if (asset.verifiedByBots) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(5.dp)
                            .background(FrostedGlassBg, RoundedCornerShape(3.dp))
                            .border(1.dp, TerminalEmerald.copy(alpha = 0.6f), RoundedCornerShape(3.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(Icons.Filled.Shield, contentDescription = "Vérifié par bots", tint = TerminalEmerald, modifier = Modifier.size(10.dp))
                        Text("BOT OK", style = MaterialTheme.typography.labelSmall.copy(color = TerminalEmerald, fontSize = 8.sp))
                    }
                }
            }

            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = asset.title,
                    style = MaterialTheme.typography.labelLarge.copy(color = TextPrimary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Auteur: ${asset.author}",
                    style = MaterialTheme.typography.labelSmall.copy(color = PrecisionAmber, fontSize = 10.sp),
                    maxLines = 1
                )
                Text(
                    text = "${asset.formatTag} | ${asset.polyCount}",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp)
                )
                Text(
                    text = asset.creditLicense,
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 9.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Button(
                    onClick = onImport,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 38.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CardSurface,
                        contentColor = SteelBlue
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Importer", style = MaterialTheme.typography.labelSmall.copy(color = SteelBlue, fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Composable
fun PublishAssetModal(
    title: String,
    creditAuthor: String,
    attributionConfirmed: Boolean,
    botChecks: List<BotCheckResult>,
    onFormChange: (title: String, creditAuthor: String, confirmed: Boolean) -> Unit,
    onRunModerationAndPublish: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = PanelSurface,
            border = BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.Shield, contentDescription = null, tint = TerminalEmerald)
                    Text(
                        text = "PUBLICATION SÉCURISÉE & MODÉRATION BOTS",
                        style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary)
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { onFormChange(it, creditAuthor, attributionConfirmed) },
                    label = { Text("Nom de l'Asset / Mod", style = MaterialTheme.typography.labelSmall) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = creditAuthor,
                    onValueChange = { onFormChange(title, it, attributionConfirmed) },
                    label = { Text("Crédits d'origine obligatoires (Auteur / Source)", style = MaterialTheme.typography.labelSmall) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onFormChange(title, creditAuthor, !attributionConfirmed) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = attributionConfirmed,
                        onCheckedChange = { onFormChange(title, creditAuthor, it) },
                        colors = CheckboxDefaults.colors(checkedColor = TerminalEmerald)
                    )
                    Text(
                        text = "Je certifie l'attribution exacte des crédits d'origine et la licence ouverte.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }

                if (botChecks.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(VoidBlack, RoundedCornerShape(4.dp))
                            .border(1.dp, BorderHairline, RoundedCornerShape(4.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        botChecks.forEach { check ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = if (check.passed) TerminalEmerald else PrecisionAmber,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "${check.botName}: ${check.status}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (check.passed) TerminalEmerald else PrecisionAmber,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRunModerationAndPublish,
                        enabled = title.isNotBlank() && creditAuthor.isNotBlank() && attributionConfirmed,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TerminalEmerald, contentColor = VoidBlack)
                    ) {
                        Text("Vérifier & Publier", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Text("Fermer", color = TextPrimary)
                    }
                }
            }
        }
    }
}
