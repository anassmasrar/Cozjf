package com.example

import com.example.data.repository.ModPackageExporter
import com.example.data.repository.OmniAiEngine
import com.example.model.AnimationStyle
import com.example.model.BlockbenchTargetType
import com.example.model.ModBuildState
import com.example.model.ModExportFormat
import com.example.model.ModelingAiEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

class ExampleUnitTest {

    private val aiEngine = OmniAiEngine()

    @Test
    fun `addition_isCorrect`() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun `meshy tripo and blockbench generate articulated meshes without credits`() {
        val dragon = aiEngine.generateModel3D("Dragon ailé", ModelingAiEngine.MESHY_3D)
        assertTrue(dragon.node.parts.size >= 4)

        val bbBlock = aiEngine.generateModel3D("Autel Rubis", ModelingAiEngine.BLOCKBENCH_IA, BlockbenchTargetType.BLOCK)
        assertTrue(bbBlock.node.parts.isNotEmpty())

        val car = aiEngine.generateModel3D("Voiture Sport", ModelingAiEngine.TRIPO_3D)
        assertTrue(car.node.parts.size >= 3)
    }

    @Test
    fun `claude plans live rig and multi style cartoon animation`() {
        val model = aiEngine.generateModel3D("Créature Dragon", ModelingAiEngine.MESHY_3D)
        val rig = aiEngine.planClaudeRig(model.node)
        assertTrue(rig.bones.size >= 6)

        val cartoon = aiEngine.generateClaudeAnimation("Rebond Cartoon", AnimationStyle.CARTOON_TEMMER)
        assertEquals(AnimationStyle.CARTOON_TEMMER, cartoon.clip.style)
        assertTrue(cartoon.poses.values.any { it.scaleY != 1f })
    }

    @Test
    fun `mod exporter packages valid mcaddon jar and play store aab zip archives`() {
        val model = aiEngine.generateModel3D("Stalker", ModelingAiEngine.BLOCKBENCH_IA, BlockbenchTargetType.ENTITY)
        val mcaddonBundle = aiEngine.generateClaudeModProject(
            prompt = "Boss Stalker",
            modState = ModBuildState(exportFormat = ModExportFormat.MCADDON),
            activeNode = model.node
        )
        val zipBytes = ModPackageExporter.buildZipBytes(mcaddonBundle.files)
        val entries = mutableListOf<String>()
        ZipInputStream(ByteArrayInputStream(zipBytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                entries.add(entry.name)
                entry = zis.nextEntry
            }
        }
        assertTrue(entries.any { it.endsWith("manifest.json") })

        val aabBytes = ModPackageExporter.buildPlayStoreBundleBytes("com.test.game", "Test Game", "1.0.0")
        assertTrue(aabBytes.size > 100)
    }
}
