/*
 * CharmedChars - A word-forming puzzle game for Minecraft
 * Copyright (C) 2025 StephanosBad
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. If not, see <https://www.gnu.org/licenses/>.
 */
package org.stephanosbad.charmedChars.integration

import org.bukkit.Bukkit
import org.stephanosbad.charmedChars.CharmedChars
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files

/**
 * Automatic setup utility for ItemsAdder integration
 *
 * Handles copying CharmedChars custom block configurations and textures to the
 * ItemsAdder plugin directory. Automates the manual setup process by:
 * - Creating the required directory structure
 * - Copying blocks.yml configuration (123 blocks total)
 * - Copying all texture files (123 PNG files in 3 colors)
 * - Enabling the charmedchars namespace in items_packs.yml
 * - Creating a README with setup instructions
 *
 * @property plugin The CharmedChars plugin instance
 */
class ItemsAdderSetup(private val plugin: CharmedChars) {

    /**
     * ItemsAdder plugin root folder
     */
    private val itemsAdderFolder = File(plugin.server.pluginManager.getPlugin("ItemsAdder")?.dataFolder?.parentFile, "ItemsAdder")

    /**
     * ItemsAdder data folder (for items_packs.yml)
     */
    private val itemsAdderDataFolder = File(itemsAdderFolder, "data")

    /**
     * ItemsAdder contents folder (for custom namespaces)
     */
    private val itemsAdderContentsFolder = File(itemsAdderFolder, "contents")

    /**
     * CharmedChars namespace folder within ItemsAdder
     */
    private val charmedCharsIAFolder = File(itemsAdderContentsFolder, "charmedchars")

    /**
     * Checks if ItemsAdder is installed and loaded
     *
     * @return true if ItemsAdder plugin is available
     */
    fun isItemsAdderAvailable(): Boolean {
        return plugin.server.pluginManager.getPlugin("ItemsAdder") != null
    }

    /**
     * Checks if CharmedChars configuration already exists in ItemsAdder
     *
     * Verifies whether blocks.yml has already been copied to the ItemsAdder directory.
     *
     * @return true if the configuration file exists
     */
    fun isAlreadySetup(): Boolean {
        val configFile = File(charmedCharsIAFolder, "configs/blocks.yml")
        return configFile.exists()
    }

    /**
     * Performs automatic setup of ItemsAdder configuration
     *
     * Executes the complete setup process:
     * 1. Verifies ItemsAdder is installed
     * 2. Checks if already setup (returns early if so)
     * 3. Creates directory structure
     * 4. Enables charmedchars namespace in items_packs.yml
     * 5. Copies blocks.yml configuration
     * 6. Copies all texture files (123 textures across 3 colors)
     * 7. Creates README with instructions
     *
     * @return SetupResult containing success status, messages, and whether it was already setup
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
     * Enables the charmedchars namespace in ItemsAdder's items_packs.yml
     *
     * Modifies items_packs.yml to include the charmedchars namespace with enabled: true.
     * If the file doesn't exist or is malformed, creates a new one with charmedchars enabled.
     *
     * @return true if the namespace was enabled successfully
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
     * Creates a new items_packs.yml configuration with charmedchars enabled
     *
     * @return The YAML content as a string with charmedchars namespace enabled
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
     * Copies a resource from the plugin JAR to a file on disk
     *
     * Extracts a resource from the plugin's JAR file and writes it to the specified
     * destination file. Used for copying configuration files and textures.
     *
     * @param resourcePath The path to the resource within the JAR
     * @param targetFile The destination file on disk
     * @return true if the copy succeeded, false if the resource was not found or copy failed
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
     * Creates a README.txt file with setup instructions and block information
     *
     * Generates a comprehensive README documenting:
     * - What the auto-setup created
     * - Next steps for completing setup (run /iazip, restart server)
     * - Block naming conventions
     * - Usage examples
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
     * Result of an automatic setup operation
     *
     * @property success Whether the setup completed successfully
     * @property alreadySetup Whether the configuration already existed (setup was skipped)
     * @property messages List of informational messages about the setup process
     */
    data class SetupResult(
        val success: Boolean,
        val alreadySetup: Boolean,
        val messages: List<String>
    )
}
