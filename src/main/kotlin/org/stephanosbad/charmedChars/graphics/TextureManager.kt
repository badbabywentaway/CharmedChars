package org.stephanosbad.charmedChars.graphics

import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.stephanosbad.charmedChars.CharmedChars
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class TextureManager(private val plugin: CharmedChars) {

    private val resourcePackDir = File(plugin.dataFolder, "resourcepack")
    private val texturesDir = File(resourcePackDir, "assets/minecraft/textures")
    private val blockModelsDir = File(resourcePackDir, "assets/minecraft/models/block")
    private val itemModelsDir = File(resourcePackDir, "assets/minecraft/models/item")
    private val blockstatesDir = File(resourcePackDir, "assets/minecraft/blockstates")

    // Extracted resource directories (copied from JAR on first run)
    private val extractedPackDir = File(plugin.dataFolder, "extracted_pack")
    private val sourceTexturesDir = File(extractedPackDir, "assets/minecraft/textures")
    private val sourceBlockModelsDir = File(extractedPackDir, "models/block")
    private val sourceItemModelsDir = File(extractedPackDir, "models/item")

    // Resource pack SHA-1 hash for verification
    private var resourcePackHash: String? = null


    /**
     * Initialize the texture manager and generate the resource pack
     *
     * DEPENDENCY REQUIREMENT: CustomBlockEngine must be initialized before calling this method.
     * The resource pack generation reads custom model data IDs from CustomBlockEngine.
     */
    fun initialize() {
        createDirectories()
        extractResourcePackAssets()
        generateResourcePack()
        plugin.logger.info("Custom textures system initialized!")
    }

    /**
     * Extract resource pack assets from the plugin JAR to the filesystem
     * This is necessary because resources are packaged inside the JAR in production
     */
    private fun extractResourcePackAssets() {
        if (extractedPackDir.exists()) {
            plugin.logger.info("Resource pack assets already extracted, skipping...")
            return
        }

        plugin.logger.info("Extracting resource pack assets...")
        extractedPackDir.mkdirs()

        // Define resource paths to extract
        val resourcePaths = listOf(
            "pack/assets/minecraft/textures/cyan",
            "pack/assets/minecraft/textures/magenta",
            "pack/assets/minecraft/textures/yellow",
            "pack/models/block/cyan",
            "pack/models/block/magenta",
            "pack/models/block/yellow",
            "pack/models/item/cyan",
            "pack/models/item/magenta",
            "pack/models/item/yellow"
        )

        var extractedAny = false
        resourcePaths.forEach { resourcePath ->
            try {
                if (extractResourceDirectory(resourcePath)) {
                    extractedAny = true
                }
            } catch (e: Exception) {
                plugin.logger.warning("Failed to extract resource directory: $resourcePath - ${e.message}")
            }
        }

        // If we didn't extract from JAR, try to copy from classpath resources
        if (!extractedAny) {
            plugin.logger.info("Not running from JAR, extracting from classpath resources...")
            extractFromClasspath()
        }

        plugin.logger.info("Resource pack assets extracted successfully!")
    }

    /**
     * Extract resources from classpath (for development mode)
     */
    private fun extractFromClasspath() {
        val colors = listOf("cyan", "magenta", "yellow")

        colors.forEach { color ->
            // Extract textures
            extractClasspathDirectory("pack/assets/minecraft/textures/$color",
                File(extractedPackDir, "assets/minecraft/textures/$color"))

            // Extract block models
            extractClasspathDirectory("pack/models/block/$color",
                File(extractedPackDir, "models/block/$color"))

            // Extract item models
            extractClasspathDirectory("pack/models/item/$color",
                File(extractedPackDir, "models/item/$color"))
        }
    }

    /**
     * Extract a directory from classpath to filesystem
     */
    private fun extractClasspathDirectory(resourcePath: String, destDir: File) {
        destDir.mkdirs()

        // Try to get resource as URL
        val resourceUrl = plugin::class.java.classLoader.getResource(resourcePath) ?: run {
            plugin.logger.warning("Resource not found: $resourcePath")
            return
        }

        val uri = try {
            resourceUrl.toURI()
        } catch (e: Exception) {
            plugin.logger.warning("Failed to convert resource URL to URI: $resourcePath")
            return
        }

        val resourceFile = File(uri)
        if (resourceFile.exists() && resourceFile.isDirectory) {
            resourceFile.listFiles()?.forEach { file ->
                if (file.isFile) {
                    file.copyTo(File(destDir, file.name), overwrite = true)
                }
            }
            plugin.logger.info("Copied ${resourceFile.listFiles()?.size ?: 0} files from $resourcePath")
        }
    }

    /**
     * Extract a directory from plugin resources to the filesystem
     * Returns true if successfully extracted from JAR, false otherwise
     */
    private fun extractResourceDirectory(resourcePath: String): Boolean {
        // Get the JAR file
        val jarFile = try {
            File(plugin::class.java.protectionDomain.codeSource.location.toURI())
        } catch (e: Exception) {
            plugin.logger.warning("Failed to locate plugin JAR: ${e.message}")
            return false
        }

        if (!jarFile.exists() || !jarFile.name.endsWith(".jar")) {
            // Development mode - resources might be on filesystem
            return false
        }

        java.util.jar.JarFile(jarFile).use { jar ->
            val entries = jar.entries()
            var extractedCount = 0

            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                val entryName = entry.name

                if (entryName.startsWith(resourcePath) && !entry.isDirectory) {
                    // Calculate destination path (remove "pack/" prefix)
                    val relativePath = entryName.removePrefix("pack/")
                    val destFile = File(extractedPackDir, relativePath)

                    // Create parent directories
                    destFile.parentFile?.mkdirs()

                    // Extract file
                    jar.getInputStream(entry).use { input ->
                        destFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    extractedCount++
                }
            }

            if (extractedCount > 0) {
                plugin.logger.info("Extracted $extractedCount files from $resourcePath")
            }

            return extractedCount > 0
        }
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
        "description": "CharmedChars Letter, Number & Symbol Blocks",
        "min_format": [69, 0],
        "max_format": [69, 0]
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

    /**
     * Generate blockstate files for custom blocks
     */
    private fun generateBlockStates() {
        // Generate note_block blockstate file that maps instrument+note to models
        generateNoteBlockBlockstate()

        // Generate item models with custom model data
        generateItemModels()
    }

    /**
     * Generate note_block.json blockstate file
     * Maps instrument + note combinations to block models based on custom model data
     */
    private fun generateNoteBlockBlockstate() {
        val blockstateFile = File(blockstatesDir, "note_block.json")
        val variants = mutableListOf<String>()

        if (!plugin.isCustomBlockEngineInitialized) {
            plugin.logger.warning("CustomBlockEngine not initialized! Cannot generate note_block blockstate")
            return
        }

        val customBlockEngine = plugin.customBlockEngine

        // Map each custom model data value to an instrument+note combination
        val allBlocks = mutableListOf<Pair<Int, Pair<String, String>>>()  // (customModelData, (color, name))

        // Add letter blocks
        customBlockEngine.letterBlockKeys.forEach { (colorLetterPair, keyDataPair) ->
            val (color, letter) = colorLetterPair
            val (_, customModelData) = keyDataPair
            allBlocks.add(customModelData to (color.directoryName to letter.character.toString()))
        }

        // Add number blocks
        customBlockEngine.numberBlockKeys.forEach { (colorNumberPair, keyDataPair) ->
            val (color, number) = colorNumberPair
            val (_, customModelData) = keyDataPair
            allBlocks.add(customModelData to (color.directoryName to number.c.toString()))
        }

        // Add character blocks
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
            allBlocks.add(customModelData to (color.directoryName to modelName))
        }

        // Sort by custom model data to ensure consistent ordering
        allBlocks.sortBy { it.first }

        // Get note block instruments
        val instruments = listOf("harp", "basedrum", "snare", "hat", "bass", "flute", "bell",
                                 "guitar", "chime", "xylophone", "iron_xylophone", "cow_bell",
                                 "didgeridoo", "bit", "banjo", "pling")

        // Map custom model data to instrument+note combinations
        allBlocks.forEachIndexed { index, (customModelData, colorAndName) ->
            val (color, name) = colorAndName
            val relativeValue = customModelData - 1100
            val note = relativeValue % 25
            val instrumentIndex = (relativeValue / 25) % instruments.size
            val instrument = instruments[instrumentIndex]

            variants.add(""""instrument=$instrument,note=$note":{"model":"block/$color/$name"}""")
        }

        // Add default variant for regular note blocks (no custom data)
        variants.add(0, """"instrument=harp,note=0":{"model":"block/note_block"}""")

        val blockstateJson = """{
    "variants":{
        ${variants.joinToString(",\n        ")}
    }
}"""

        blockstateFile.writeText(blockstateJson)
        plugin.logger.info("Generated note_block blockstate with ${allBlocks.size} variants")
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
     *
     * IMPORTANT: This method requires CustomBlockEngine to be initialized first.
     * CustomBlockEngine must be initialized before calling textureManager.initialize()
     */
    private fun generateNoteBlockItemModel() {
        val noteBlockFile = File(itemModelsDir, "note_block.json")
        val overrides = mutableListOf<String>()

        // Safety check: Ensure CustomBlockEngine is initialized
        if (!plugin.isCustomBlockEngineInitialized) {
            plugin.logger.severe("CustomBlockEngine not initialized! Cannot generate note_block.json")
            plugin.logger.severe("Make sure CustomBlockEngine is created before calling textureManager.initialize()")
            return
        }

        val customBlockEngine = plugin.customBlockEngine
        var modelDataCounter = 0

        // Add letter blocks
        customBlockEngine.letterBlockKeys.forEach { (colorLetterPair, keyDataPair) ->
            val (color, letter) = colorLetterPair
            val (_, customModelData) = keyDataPair
            overrides.add("""{
    "predicate": {
        "custom_model_data": $customModelData
    },
    "model": "item/${color.directoryName}/${letter.character}"
}""")
            modelDataCounter++
        }

        // Add number blocks
        customBlockEngine.numberBlockKeys.forEach { (colorNumberPair, keyDataPair) ->
            val (color, number) = colorNumberPair
            val (_, customModelData) = keyDataPair
            overrides.add("""{
    "predicate": {
        "custom_model_data": $customModelData
    },
    "model": "item/${color.directoryName}/${number.c}"
}""")
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
            overrides.add("""{
    "predicate": {
        "custom_model_data": $customModelData
    },
    "model": "item/${color.directoryName}/$modelName"
}""")
            modelDataCounter++
        }

        val noteBlockJson = """{
    "parent": "minecraft:item/generated",
    "textures": {
        "layer0": "minecraft:block/note_block"
    },
    "overrides": [
        ${overrides.joinToString(",\n        ")}
    ]
}"""

        noteBlockFile.writeText(noteBlockJson)
        plugin.logger.info("Generated note_block.json with $modelDataCounter custom model data overrides")

        // Log first few overrides for debugging
        if (overrides.isNotEmpty()) {
            plugin.logger.info("Sample overrides (first 3):")
            overrides.take(3).forEach { plugin.logger.info(it) }
        }
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

            // Generate SHA-1 hash for resource pack verification
            resourcePackHash = generateSHA1(zipFile)
            plugin.logger.info("Resource pack SHA-1: $resourcePackHash")
        } catch (e: IOException) {
            plugin.logger.severe("Failed to create resource pack ZIP: ${e.message}")
        }
    }

    /**
     * Generate SHA-1 hash of a file
     */
    private fun generateSHA1(file: File): String {
        val digest = MessageDigest.getInstance("SHA-1")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
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
     * Send resource pack to player automatically
     */
    fun sendResourcePackToPlayer(player: Player, sendManualInstructions: Boolean = false) {
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

        // Get resource pack URL - prefer self-hosted if available
        val resourcePackUrl = if (plugin.configManager.selfHostEnabled &&
                                   plugin.isResourcePackServerInitialized &&
                                   plugin.resourcePackServer.isRunning()) {
            plugin.resourcePackServer.getResourcePackUrl()
        } else {
            plugin.configManager.resourcePackUrl
        }

        // Store hash in local val to enable smart cast
        val hash = resourcePackHash
        if (resourcePackUrl.isNotBlank() && hash != null) {
            // Send resource pack automatically using Paper API
            try {
                player.setResourcePack(
                    resourcePackUrl,
                    hash,
                    plugin.configManager.resourcePackRequired,
                    Component.text("CharmedChars requires a custom resource pack to display letter blocks correctly.")
                        .color(NamedTextColor.YELLOW)
                )

                player.sendMessage(
                    Component.text("Sending resource pack... Please accept the download prompt!")
                        .color(NamedTextColor.GREEN)
                )
            } catch (e: Exception) {
                plugin.logger.warning("Failed to send resource pack to ${player.name}: ${e.message}")
                sendManualInstructionsToPlayer(player, resourcePackFile)
            }
        } else if (sendManualInstructions) {
            // Fall back to manual instructions
            sendManualInstructionsToPlayer(player, resourcePackFile)
        } else {
            player.sendMessage(
                Component.text("Resource pack URL not configured! Use '/textures download' for manual installation.")
                    .color(NamedTextColor.YELLOW)
            )
        }
    }

    /**
     * Send manual installation instructions to player
     */
    private fun sendManualInstructionsToPlayer(player: Player, resourcePackFile: File) {
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
     * Get the resource pack SHA-1 hash
     */
    fun getResourcePackHash(): String? = resourcePackHash

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