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
    private val texturesDir = File(resourcePackDir, "assets/minecraft/textures")
    private val blockModelsDir = File(resourcePackDir, "assets/minecraft/models/block")
    private val itemModelsDir = File(resourcePackDir, "assets/minecraft/models/item")
    private val blockstatesDir = File(resourcePackDir, "assets/minecraft/blockstates")

    // Source directories from our created assets
    private val sourceTexturesDir = File(plugin.dataFolder.parentFile.parentFile.parentFile, "src/main/resources/pack/assets/minecraft/textures")
    private val sourceBlockModelsDir = File(plugin.dataFolder.parentFile.parentFile.parentFile, "src/main/resources/pack/models/block")
    private val sourceItemModelsDir = File(plugin.dataFolder.parentFile.parentFile.parentFile, "src/main/resources/pack/models/item")

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
        listOf(resourcePackDir, texturesDir, blockModelsDir, itemModelsDir, blockstatesDir).forEach { dir ->
            if (!dir.exists()) {
                dir.mkdirs()
            }
        }
        // Create color subdirectories
        listOf("cyan", "magenta", "yellow").forEach { color ->
            File(texturesDir, color).mkdirs()
            File(blockModelsDir, color).mkdirs()
            File(itemModelsDir, color).mkdirs()
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
        "description": "CharmedChars Letter, Number & Symbol Blocks"
    }
}
        """.trimIndent()

        packMcmeta.writeText(content)
    }

    /**
     * Generate custom block models by copying from source directory
     */
    private fun generateBlockModels() {
        plugin.logger.info("Copying block models...")

        if (!sourceBlockModelsDir.exists()) {
            plugin.logger.warning("Source block models directory not found: ${sourceBlockModelsDir.absolutePath}")
            return
        }

        listOf("cyan", "magenta", "yellow").forEach { color ->
            val sourceColorDir = File(sourceBlockModelsDir, color)
            val destColorDir = File(blockModelsDir, color)

            if (sourceColorDir.exists() && sourceColorDir.isDirectory) {
                sourceColorDir.listFiles()?.filter { it.extension == "json" }?.forEach { jsonFile ->
                    val destFile = File(destColorDir, jsonFile.name)
                    jsonFile.copyTo(destFile, overwrite = true)
                }
                plugin.logger.info("Copied ${sourceColorDir.listFiles()?.size ?: 0} block models for $color")
            }
        }
    }

    private fun generateBlockModel(name: String, texture: String, modelJson: String) {
        val modelFile = File(blockModelsDir, "$name.json")
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
        plugin.logger.info("Generating item models...")

        // First, copy our color-organized item models
        if (sourceItemModelsDir.exists()) {
            listOf("cyan", "magenta", "yellow").forEach { color ->
                val sourceColorDir = File(sourceItemModelsDir, color)
                val destColorDir = File(itemModelsDir, color)

                if (sourceColorDir.exists() && sourceColorDir.isDirectory) {
                    sourceColorDir.listFiles()?.filter { it.extension == "json" }?.forEach { jsonFile ->
                        val destFile = File(destColorDir, jsonFile.name)
                        jsonFile.copyTo(destFile, overwrite = true)
                    }
                    plugin.logger.info("Copied ${sourceColorDir.listFiles()?.size ?: 0} item models for $color")
                }
            }
        }

        // Generate note_block.json with all custom model data overrides
        generateNoteBlockItemModel()
    }

    /**
     * Generate note_block.json with custom model data overrides for all our character blocks
     */
    private fun generateNoteBlockItemModel() {
        val noteBlockFile = File(itemModelsDir, "note_block.json")
        val overrides = mutableListOf<String>()

        val customBlockEngine = plugin.customBlockEngine
        var modelDataCounter = 0

        // Add letter blocks
        customBlockEngine.letterBlockKeys.forEach { (colorLetterPair, keyDataPair) ->
            val (color, letter) = colorLetterPair
            val (_, customModelData) = keyDataPair
            overrides.add("""
                {
                    "predicate": {
                        "custom_model_data": $customModelData
                    },
                    "model": "item/${color.directoryName}/${letter.character}"
                }""".trimIndent())
            modelDataCounter++
        }

        // Add number blocks
        customBlockEngine.numberBlockKeys.forEach { (colorNumberPair, keyDataPair) ->
            val (color, number) = colorNumberPair
            val (_, customModelData) = keyDataPair
            overrides.add("""
                {
                    "predicate": {
                        "custom_model_data": $customModelData
                    },
                    "model": "item/${color.directoryName}/${number.c}"
                }""".trimIndent())
            modelDataCounter++
        }

        // Add character blocks (operators)
        customBlockEngine.characterBlockKeys.forEach { (colorCharPair, keyDataPair) ->
            val (color, char) = colorCharPair
            val (_, customModelData) = keyDataPair
            val modelName = when(char.charVal) {
                '+' -> "plus"
                '-' -> "minus"
                '*' -> "multiply"
                '/' -> "division"
                else -> char.nonAlphaNumBlockName
            }
            overrides.add("""
                {
                    "predicate": {
                        "custom_model_data": $customModelData
                    },
                    "model": "item/${color.directoryName}/$modelName"
                }""".trimIndent())
            modelDataCounter++
        }

        val noteBlockJson = """
{
    "parent": "block/note_block",
    "overrides": [
        ${overrides.joinToString(",\n        ")}
    ]
}
        """.trimIndent()

        noteBlockFile.writeText(noteBlockJson)
        plugin.logger.info("Generated note_block.json with $modelDataCounter custom model data overrides")
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
     * Copy texture PNG files from source directory
     */
    private fun generateTextureFiles() {
        plugin.logger.info("Copying texture files...")

        if (!sourceTexturesDir.exists()) {
            plugin.logger.warning("Source textures directory not found: ${sourceTexturesDir.absolutePath}")
            return
        }

        listOf("cyan", "magenta", "yellow").forEach { color ->
            val sourceColorDir = File(sourceTexturesDir, color)
            val destColorDir = File(texturesDir, color)

            if (sourceColorDir.exists() && sourceColorDir.isDirectory) {
                sourceColorDir.listFiles()?.filter { it.extension == "png" }?.forEach { pngFile ->
                    val destFile = File(destColorDir, pngFile.name)
                    pngFile.copyTo(destFile, overwrite = true)
                }
                plugin.logger.info("Copied ${sourceColorDir.listFiles()?.filter { it.extension == "png" }?.size ?: 0} textures for $color")
            }
        }
    }


    /**
     * Create the final resource pack ZIP file
     */
    private fun createResourcePackZip() {
        val zipFile = File(plugin.dataFolder, "CharmedChars-ResourcePack.zip")

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

        val resourcePackFile = File(plugin.dataFolder, "CharmedChars-ResourcePack.zip")
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
                File(plugin.dataFolder, "CharmedChars-ResourcePack.zip").exists()
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