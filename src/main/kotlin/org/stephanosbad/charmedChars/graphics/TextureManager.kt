package org.stephanosbad.charmedChars.graphics

import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.stephanosbad.charmedChars.CharmedChars
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class TextureManager(private val plugin: CharmedChars) {

    private val resourcePackDir = File(plugin.dataFolder, "resourcepack")
    private val texturesDir = File(resourcePackDir, "assets/minecraft/textures/block")
    private val modelsDir = File(resourcePackDir, "assets/minecraft/models/block")
    private val blockstatesDir = File(resourcePackDir, "assets/minecraft/blockstates")

    companion object {
        // Custom model data values for each block type
        const val MAGIC_STONE_MODEL = 1001
        const val ENCHANTED_LOG_MODEL = 1002
        const val CRYSTAL_BLOCK_MODEL = 1003

        // Texture file names
        const val MAGIC_STONE_TEXTURE = "magic_stone"
        const val ENCHANTED_LOG_TEXTURE = "enchanted_log"
        const val CRYSTAL_BLOCK_TEXTURE = "crystal_block"
    }

    fun initialize() {
        createDirectories()
        generateResourcePack()
        plugin.logger.info("Custom textures system initialized!")
    }

    private fun createDirectories() {
        listOf(resourcePackDir, texturesDir, modelsDir, blockstatesDir).forEach { dir ->
            if (!dir.exists()) {
                dir.mkdirs()
            }
        }
    }

    /**
     * Generate the complete resource pack with custom textures
     */
    fun generateResourcePack() {
        try {
            generatePackMcmeta()
            generateBlockModels()
            generateBlockStates()
            generateTextureFiles()
            createResourcePackZip()

            plugin.logger.info("Resource pack generated successfully!")
        } catch (e: Exception) {
            plugin.logger.severe("Failed to generate resource pack: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun generatePackMcmeta() {
        val packMcmeta = File(resourcePackDir, "pack.mcmeta")
        val content = """
            {
                "pack": {
                    "pack_format": 18,
                    "description": "CharmedChars Custom Blocks"
                }
            }
        """.trimIndent()

        packMcmeta.writeText(content)
    }

    /**
     * Generate custom block models
     */
    private fun generateBlockModels() {
        // Magic Stone model
        generateBlockModel(
            "magic_stone",
            MAGIC_STONE_TEXTURE,
            """
            {
                "parent": "block/cube_all",
                "textures": {
                    "all": "block/$MAGIC_STONE_TEXTURE"
                }
            }
            """.trimIndent()
        )

        // Enchanted Log model
        generateBlockModel(
            "enchanted_log",
            ENCHANTED_LOG_TEXTURE,
            """
            {
                "parent": "block/cube_column",
                "textures": {
                    "end": "block/${ENCHANTED_LOG_TEXTURE}_top",
                    "side": "block/$ENCHANTED_LOG_TEXTURE"
                }
            }
            """.trimIndent()
        )

        // Crystal Block model
        generateBlockModel(
            "crystal_block",
            CRYSTAL_BLOCK_TEXTURE,
            """
            {
                "parent": "block/cube_all",
                "textures": {
                    "all": "block/$CRYSTAL_BLOCK_TEXTURE"
                },
                "elements": [
                    {
                        "from": [0, 0, 0],
                        "to": [16, 16, 16],
                        "faces": {
                            "down": {"texture": "#all", "cullface": "down"},
                            "up": {"texture": "#all", "cullface": "up"},
                            "north": {"texture": "#all", "cullface": "north"},
                            "south": {"texture": "#all", "cullface": "south"},
                            "west": {"texture": "#all", "cullface": "west"},
                            "east": {"texture": "#all", "cullface": "east"}
                        }
                    }
                ]
            }
            """.trimIndent()
        )
    }

    private fun generateBlockModel(name: String, texture: String, modelJson: String) {
        val modelFile = File(modelsDir, "$name.json")
        modelFile.writeText(modelJson)
    }

    /**
     * Generate blockstate files for custom blocks
     */
    private fun generateBlockStates() {
        // Note: Since we're using custom model data on items rather than actual blocks,
        // we override the base block models through item models
        generateItemModels()
    }

    private fun generateItemModels() {
        val itemModelsDir = File(resourcePackDir, "assets/minecraft/models/item")
        itemModelsDir.mkdirs()

        // Generate item models that override base materials with custom model data
        generateItemModelOverride("smooth_stone", MAGIC_STONE_MODEL, "block/magic_stone")
        generateItemModelOverride("dark_oak_log", ENCHANTED_LOG_MODEL, "block/enchanted_log")
        generateItemModelOverride("amethyst_block", CRYSTAL_BLOCK_MODEL, "block/crystal_block")
    }

    private fun generateItemModelOverride(baseMaterial: String, customModelData: Int, model: String) {
        val itemModelsDir = File(resourcePackDir, "assets/minecraft/models/item")
        val itemFile = File(itemModelsDir, "$baseMaterial.json")

        val content = """
            {
                "parent": "item/generated",
                "textures": {
                    "layer0": "block/$baseMaterial"
                },
                "overrides": [
                    {
                        "predicate": {
                            "custom_model_data": $customModelData
                        },
                        "model": "$model"
                    }
                ]
            }
        """.trimIndent()

        itemFile.writeText(content)
    }

    /**
     * Generate placeholder texture files with descriptions
     */
    private fun generateTextureFiles() {
        // Create texture placeholder files with instructions
        val textureInfo = File(texturesDir, "TEXTURE_INFO.txt")
        val instructions = """
            Custom Block Textures for MyMinecraftPlugin
            ==========================================
            
            To add custom textures:
            1. Place your 16x16 PNG texture files in this directory
            2. Name them exactly as follows:
               - magic_stone.png (for Magic Stone block)
               - enchanted_log.png (for Enchanted Log side)
               - enchanted_log_top.png (for Enchanted Log top/bottom)
               - crystal_block.png (for Crystal Block)
            
            3. Restart the server to regenerate the resource pack
            4. Players can download the pack with /blocks resourcepack
            
            Texture Requirements:
            - 16x16 pixels (standard Minecraft block size)
            - PNG format
            - Avoid transparency for solid blocks
            - Use vibrant colors for magical blocks
            
            Example texture descriptions:
            - Magic Stone: Purple/pink stone with glowing runes
            - Enchanted Log: Dark wood with green magical veins
            - Crystal Block: Translucent crystal with rainbow reflections
            
        """.trimIndent()

        textureInfo.writeText(instructions)

        // Generate sample texture URLs/references
        generateSampleTextureReferences()
    }

    private fun generateSampleTextureReferences() {
        val samplesDir = File(texturesDir, "samples")
        samplesDir.mkdirs()

        val sampleInfo = File(samplesDir, "sample_textures.json")
        val samples = """
            {
                "magic_stone": {
                    "description": "Mystical purple stone with glowing runes",
                    "base_color": "#8A2BE2",
                    "glow_effect": true,
                    "pattern": "runic_symbols"
                },
                "enchanted_log": {
                    "description": "Dark oak with green magical veins",
                    "base_color": "#3C2415", 
                    "accent_color": "#00FF7F",
                    "pattern": "magical_veins"
                },
                "crystal_block": {
                    "description": "Translucent crystal with rainbow prismatic effect",
                    "base_color": "#E6E6FA",
                    "effect": "prismatic",
                    "transparency": 0.8
                }
            }
        """.trimIndent()

        sampleInfo.writeText(samples)
    }

    /**
     * Create the final resource pack ZIP file
     */
    private fun createResourcePackZip() {
        val zipFile = File(plugin.dataFolder, "MyMinecraftPlugin-ResourcePack.zip")

        try {
            ZipOutputStream(FileOutputStream(zipFile)).use { zip ->
                addDirectoryToZip(zip, resourcePackDir, "")
            }

            plugin.logger.info("Resource pack ZIP created: ${zipFile.absolutePath}")
        } catch (e: IOException) {
            plugin.logger.severe("Failed to create resource pack ZIP: ${e.message}")
        }
    }

    private fun addDirectoryToZip(zip: ZipOutputStream, directory: File, basePath: String) {
        directory.listFiles()?.forEach { file ->
            val entryPath = if (basePath.isEmpty()) file.name else "$basePath/${file.name}"

            if (file.isDirectory) {
                addDirectoryToZip(zip, file, entryPath)
            } else {
                zip.putNextEntry(ZipEntry(entryPath))
                file.inputStream().use { input ->
                    input.copyTo(zip)
                }
                zip.closeEntry()
            }
        }
    }

    /**
     * Apply custom model data to an item for custom textures
     */
    fun applyCustomTexture(item: ItemStack, blockType: String): ItemStack {
        val meta = item.itemMeta ?: return item

        val customModelData = when (blockType) {
            "magic_stone" -> MAGIC_STONE_MODEL
            "enchanted_log" -> ENCHANTED_LOG_MODEL
            "crystal_block" -> CRYSTAL_BLOCK_MODEL
            else -> return item
        }

        meta.setCustomModelData(customModelData)
        item.itemMeta = meta

        return item
    }

    /**
     * Send resource pack to player
     */
    fun sendResourcePackToPlayer(player: Player) {
        if (!plugin.configManager.customTexturesEnabled) {
            player.sendMessage(
                Component.text("Custom textures are disabled on this server.")
                    .color(NamedTextColor.RED)
            )
            return
        }

        val resourcePackFile = File(plugin.dataFolder, "MyMinecraftPlugin-ResourcePack.zip")
        if (!resourcePackFile.exists()) {
            player.sendMessage(
                Component.text("Resource pack not found! Contact an administrator.")
                    .color(NamedTextColor.RED)
            )
            return
        }

        // In a real implementation, you'd host this on a web server
        // For now, provide instructions to manually install
        player.sendMessage(
            Component.text("Custom Textures Available!")
                .color(NamedTextColor.GOLD)
        )

        player.sendMessage(
            Component.text("To see custom block textures:")
                .color(NamedTextColor.YELLOW)
        )

        player.sendMessage(
            Component.text("1. Download the resource pack from the server files")
                .color(NamedTextColor.GRAY)
        )

        player.sendMessage(
            Component.text("2. Place it in your resourcepacks folder")
                .color(NamedTextColor.GRAY)
        )

        player.sendMessage(
            Component.text("3. Enable it in Options > Resource Packs")
                .color(NamedTextColor.GRAY)
        )

        player.sendMessage(
            Component.text("File location: ${resourcePackFile.absolutePath}")
                .color(NamedTextColor.AQUA)
        )
    }

    /**
     * Check if custom textures are enabled and resource pack exists
     */
    fun isTextureSystemReady(): Boolean {
        return plugin.configManager.customTexturesEnabled &&
                File(plugin.dataFolder, "MyMinecraftPlugin-ResourcePack.zip").exists()
    }

    /**
     * Regenerate resource pack (useful for adding new textures)
     */
    fun regenerateResourcePack() {
        plugin.launch {
            try {
                generateResourcePack()
                plugin.logger.info("Resource pack regenerated successfully!")
            } catch (e: Exception) {
                plugin.logger.severe("Failed to regenerate resource pack: ${e.message}")
            }
        }
    }
}