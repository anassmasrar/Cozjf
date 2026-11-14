package com.example.ui.panels

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SdkDocEntry
import com.example.ui.theme.*

@Composable
fun SystemSdkPanel(
    sdkDocs: List<SdkDocEntry>,
    selectedSdkId: String,
    onSelectSdkModule: (String) -> Unit,
    modifier: Modifier = Modifier,
    scrollable: Boolean = true
) {
    val activeDoc = sdkDocs.find { it.id == selectedSdkId } ?: sdkDocs.first()

    Column(
        modifier = modifier
            .then(
                if (scrollable) {
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                } else {
                    Modifier.fillMaxWidth()
                }
            )
            .background(PanelSurface)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "ARCHITECTURE LIBRE & API OUVERTE (SDK)",
            style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary)
        )

        // Zero-Barrier Open Charter Guarantee Grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElevatedSurface, RoundedCornerShape(6.dp))
                .border(1.dp, TerminalEmerald.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "GARANTIE ARCHITECTURE OUVERTE — ZÉRO BARRIÈRE ÉCONOMIQUE",
                style = MaterialTheme.typography.labelSmall.copy(color = TerminalEmerald)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OpenGuaranteeChip("0 Crédit Requis", Modifier.weight(1f))
                OpenGuaranteeChip("0 Abonnement", Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OpenGuaranteeChip("Export Local Illimité", Modifier.weight(1f))
                OpenGuaranteeChip("SDK Apache-2.0 / MIT", Modifier.weight(1f))
            }
        }

        // Open API Documentation Viewer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElevatedSurface, RoundedCornerShape(6.dp))
                .border(1.dp, BorderHairline, RoundedCornerShape(6.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Outlined.MenuBook, contentDescription = null, tint = SteelBlue, modifier = Modifier.size(16.dp))
                Text(
                    text = "VISIONNEUSE DE DOCUMENTATION API OUVERTE",
                    style = MaterialTheme.typography.labelSmall.copy(color = SteelBlue)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                sdkDocs.forEach { doc ->
                    val selected = doc.id == activeDoc.id
                    Surface(
                        modifier = Modifier
                            .testTag("sdk_tab_${doc.id}")
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onSelectSdkModule(doc.id) },
                        color = if (selected) SteelBlueDim else VoidBlack,
                        border = BorderStroke(1.dp, if (selected) SteelBlue else BorderHairline),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = doc.moduleName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (selected) TextPrimary else TextSecondary
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VoidBlack, RoundedCornerShape(4.dp))
                    .border(1.dp, BorderHairline, RoundedCornerShape(4.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = activeDoc.moduleName,
                    style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = activeDoc.summary,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
                Text(
                    text = "SIGNATURE: ${activeDoc.endpointSignature}",
                    style = MaterialTheme.typography.labelSmall.copy(color = PrecisionAmber)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ColorCodeSurface, RoundedCornerShape(4.dp))
                        .border(1.dp, BorderHairline, RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = activeDoc.exampleCode,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TerminalEmerald,
                            lineHeight = 15.sp
                        )
                    )
                }
            }
        }
    }
}

private val ColorCodeSurface = androidx.compose.ui.graphics.Color(0xFF0E1013)

@Composable
private fun OpenGuaranteeChip(label: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(VoidBlack, RoundedCornerShape(4.dp))
            .border(1.dp, BorderHairline, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = TerminalEmerald, modifier = Modifier.size(13.dp))
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary, fontSize = 10.sp))
    }
}
