package org.stephanosbad.charmedChars.integration

import org.bukkit.Bukkit
import org.stephanosbad.charmedChars.CharmedChars
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files

class ItemsAdderSetup(private val plugin: CharmedChars) {

    private val itemsAdderFolder = File(plugin.server.pluginManager.getPlugin("ItemsAdder")?.dataFolder?.parentFile, "ItemsAdder")
    private val itemsAdderDataFolder = File(itemsAdderFolder, "data")
    private val itemsAdderContentsFolder = File(itemsAdderFolder, "contents")
    private val charmedCharsIAFolder = File(itemsAdderContentsFolder, "charmedchars")

    /**
     * Check if ItemsAdder is installed and accessible
     */
    fun isItemsAdderAvailable(): Boolean {
        return plugin.server.pluginManager.getPlugin("ItemsAdder") != null
    }

    /**
     * Check if CharmedChars configuration already exists in ItemsAdder
     */
    fun isAlreadySetup(): Boolean {
        val configFile = File(charmedCharsIAFolder, "configs/blocks.yml")
        return configFile.exists()
    }

    /**
     * Automatically setup ItemsAdder configuration by copying from plugin resources
     * @return SetupResult with status and messages
     */
    fun autoSetup(): SetupResult {
        val messages = mutableListOf<String>()

        // Check if ItemsAdder is available
        if (!isItemsAdderAvailable()) {
            return SetupResult(
                success = false,
                alreadySetup = false,
                messages = listOf("ItemsAdder plugin not found! Please install ItemsAdder first.")
            )
        }

        // Check if already setup
        if (isAlreadySetup()) {
            return SetupResult(
                success = true,
                alreadySetup = true,
                messages = listOf(
                    "CharmedChars ItemsAdder configuration already exists.",
                    "Files are located in: ${charmedCharsIAFolder.absolutePath}",
                    "To regenerate, delete the folder and run this command again."
                )
            )
        }

        try {
            // Create directory structure
            messages.add("Creating directory structure...")
            val configsDir = File(charmedCharsIAFolder, "configs")
            val texturesDir = File(charmedCharsIAFolder, "resourcepack/assets/charmedchars/textures/block")

            configsDir.mkdirs()
            texturesDir.mkdirs()

            // Enable charmedchars namespace in items_packs.yml
            messages.add("Enabling charmedchars namespace...")
            if (enableNamespace()) {
                messages.add("  ✓ items_packs.yml updated")
            } else {
                messages.add("  ✗ Failed to update items_packs.yml")
                return SetupResult(false, false, messages)
            }

            // Copy blocks.yml configuration
            messages.add("Copying blocks.yml configuration...")
            val blocksYml = copyResourceToFile(
                "itemsadder/blocks.yml",
                File(configsDir, "blocks.yml")
            )
            if (blocksYml) {
                messages.add("  ✓ blocks.yml copied successfully")
            } else {
                messages.add("  ✗ Failed to copy blocks.yml")
                return SetupResult(false, false, messages)
            }

            // Copy textures
            messages.add("Copying texture files...")
            var textureCount = 0
            val colors = listOf("cyan", "magenta", "yellow")

            for (color in colors) {
                val colorDir = File(texturesDir, color)
                colorDir.mkdirs()

                // Copy letter textures (a-z)
                for (letter in 'a'..'z') {
                    if (copyResourceToFile(
                        "pack/assets/minecraft/textures/block/$color/$letter.png",
                        File(colorDir, "$letter.png")
                    )) {
                        textureCount++
                    }
                }

                // Copy number textures (0-9)
                for (number in 0..9) {
                    if (copyResourceToFile(
                        "pack/assets/minecraft/textures/block/$color/$number.png",
                        File(colorDir, "$number.png")
                    )) {
                        textureCount++
                    }
                }

                // Copy operator textures
                val operators = listOf("plus", "minus", "multiply", "division")
                for (op in operators) {
                    if (copyResourceToFile(
                        "pack/assets/minecraft/textures/block/$color/$op.png",
                        File(colorDir, "$op.png")
                    )) {
                        textureCount++
                    }
                }

                // Copy logo block
                if (copyResourceToFile(
                    "pack/assets/minecraft/textures/block/$color/logo_block.png",
                    File(colorDir, "logo_block.png")
                )) {
                    textureCount++
                }
            }

            messages.add("  ✓ Copied $textureCount texture files")

            // Create README
            messages.add("Creating README...")
            createReadme()
            messages.add("  ✓ README.txt created")

            messages.add("")
            messages.add("=== Setup Complete! ===")
            messages.add("Files copied to: ${charmedCharsIAFolder.absolutePath}")
            messages.add("")
            messages.add("NEXT STEPS:")
            messages.add("1. Run /iazip to generate the resource pack")
            messages.add("2. Restart the server")
            messages.add("3. Test with /iastatus to verify blocks loaded")
            messages.add("")

            return SetupResult(
                success = true,
                alreadySetup = false,
                messages = messages
            )

        } catch (e: Exception) {
            messages.add("ERROR: ${e.message}")
            plugin.logger.severe("Failed to auto-setup ItemsAdder configuration: ${e.message}")
            e.printStackTrace()
            return SetupResult(
                success = false,
                alreadySetup = false,
                messages = messages
            )
        }
    }

    /**
     * Enable the charmedchars namespace in items_packs.yml
     */
    private fun enableNamespace(): Boolean {
        try {
            val itemsPacksFile = File(itemsAdderDataFolder, "items_packs.yml")

            val content = if (itemsPacksFile.exists()) {
                // File exists, check if charmedchars is already enabled
                val existingContent = itemsPacksFile.readText()

                if (existingContent.contains("charmedchars:")) {
                    // Already enabled, no need to modify
                    return true
                }

                // Add charmedchars to existing file
                val lines = existingContent.lines().toMutableList()
                val packsIndex = lines.indexOfFirst { it.trim().startsWith("packs:") }

                if (packsIndex != -1) {
                    // Insert after packs: line
                    lines.add(packsIndex + 1, "  charmedchars:")
                    lines.add(packsIndex + 2, "    enabled: true")
                    lines.joinToString("\n")
                } else {
                    // Malformed file, recreate it
                    createItemsPacksYml()
                }
            } else {
                // File doesn't exist, create it
                createItemsPacksYml()
            }

            itemsPacksFile.writeText(content)
            return true

        } catch (e: Exception) {
            plugin.logger.severe("Failed to enable namespace in items_packs.yml: ${e.message}")
            e.printStackTrace()
            return false
        }
    }

    /**
     * Create a new items_packs.yml file with charmedchars enabled
     */
    private fun createItemsPacksYml(): String {
        return """
# ItemsAdder items packs configuration
# This file controls which custom item namespaces are loaded

packs:
  charmedchars:
    enabled: true
        """.trimIndent()
    }

    /**
     * Copy a resource from the plugin JAR to a file on disk
     */
    private fun copyResourceToFile(resourcePath: String, targetFile: File): Boolean {
        try {
            val inputStream = plugin.getResource(resourcePath) ?: run {
                plugin.logger.warning("Resource not found: $resourcePath")
                return false
            }

            inputStream.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            return true
        } catch (e: Exception) {
            plugin.logger.warning("Failed to copy resource $resourcePath: ${e.message}")
            return false
        }
    }

    /**
     * Create a README file with setup instructions
     */
    private fun createReadme() {
        val readmeFile = File(charmedCharsIAFolder, "README.txt")
        readmeFile.writeText("""
CharmedChars - ItemsAdder Integration
======================================

This folder contains CharmedChars custom blocks for ItemsAdder.

AUTO-GENERATED by CharmedChars plugin.

Contents:
---------
- configs/blocks.yml       - Block definitions (123 blocks total)
- resourcepack/...         - Block textures (cyan, magenta, yellow)

Setup:
------
1. This configuration was automatically copied by CharmedChars
2. Run /iazip to generate the resource pack
3. Restart your server
4. Run /iastatus to verify blocks are loaded

Blocks Included:
----------------
- 78 Letter blocks (A-Z × 3 colors)
- 30 Number blocks (0-9 × 3 colors)
- 12 Operator blocks (+, -, ×, ÷ × 3 colors)
- 3 Logo blocks (1 × 3 colors)

Total: 123 blocks

Block ID Format:
----------------
charmedchars:<color>_<character>

Examples:
- charmedchars:cyan_a
- charmedchars:magenta_5
- charmedchars:yellow_plus

Commands:
---------
/iaget charmedchars:cyan_a     - Get a block
/charblock <player> cyan hello - Give blocks to player
/iastatus                      - Check setup status

For more information, see the CharmedChars documentation.
        """.trimIndent())
    }

    /**
     * Result of the setup operation
     */
    data class SetupResult(
        val success: Boolean,
        val alreadySetup: Boolean,
        val messages: List<String>
    )
}
