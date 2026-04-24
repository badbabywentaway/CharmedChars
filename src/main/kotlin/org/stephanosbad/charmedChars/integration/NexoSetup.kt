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

import org.stephanosbad.charmedChars.CharmedChars
import org.stephanosbad.charmedChars.items.LetterBlock
import org.stephanosbad.charmedChars.items.NonAlphaNumBlocks
import org.stephanosbad.charmedChars.items.NumericBlock
import java.io.File
import java.io.FileOutputStream

/**
 * Automatic setup utility for Nexo integration
 *
 * Handles creating CharmedChars custom block configurations and textures for
 * Nexo plugin directory. Automates the manual setup process by:
 * - Creating the required directory structure
 * - Generating items configuration files (123 blocks + 5 pyrite items)
 * - Generating recipe configuration
 * - Copying all texture files (128 total)
 * - Creating a README with setup instructions
 *
 * **IMPORTANT NOTE**: This implementation has not been tested with a live Nexo
 * instance as it requires a premium license. The implementation is based on
 * Nexo's public documentation and API structure.
 *
 * **API Reference**: https://jd.nexomc.com/
 * **Documentation**: https://docs.nexomc.com/
 *
 * @property plugin The CharmedChars plugin instance
 */
class NexoSetup(private val plugin: CharmedChars) {

    /**
     * Nexo plugin root folder
     */
    private val nexoFolder = File(plugin.server.pluginManager.getPlugin("Nexo")?.dataFolder?.parentFile, "Nexo")

    /**
     * Nexo items configuration folder
     */
    private val nexoItemsFolder = File(nexoFolder, "items")

    /**
     * Nexo pack root folder
     */
    private val nexoPackFolder = File(nexoFolder, "pack")

    /**
     * Nexo pack assets folder (for custom namespace)
     */
    private val nexoAssetsFolder = File(nexoPackFolder, "assets/charmedchars")

    /**
     * Nexo pack textures folder
     */
    private val nexoTexturesFolder = File(nexoAssetsFolder, "textures")

    /**
     * Nexo pack models folder for block models
     */
    private val nexoModelsFolder = File(nexoAssetsFolder, "models/block")

    /**
     * Nexo recipes folder
     */
    private val recipesFolder = File(nexoFolder, "recipes")

    /**
     * CharmedChars items config file
     */
    private val charmedCharsItemsFile = File(nexoItemsFolder, "charmedchars_blocks.yml")

    /**
     * Checks if Nexo is installed and loaded
     *
     * @return true if Nexo plugin is available
     */
    fun isNexoAvailable(): Boolean {
        return plugin.server.pluginManager.getPlugin("Nexo") != null
    }

    /**
     * Checks if CharmedChars configuration already exists in Nexo
     *
     * @return true if the configuration files exist
     */
    fun isAlreadySetup(): Boolean {
        return charmedCharsItemsFile.exists() && hasCharmedCharsRecipes()
    }

    /**
     * Checks if CharmedChars recipes exist in Nexo configuration
     *
     * @return true if recipes are found
     */
    private fun hasCharmedCharsRecipes(): Boolean {
        val shapedFile = File(recipesFolder, "shaped/charmedchars.yml")
        val shapelessFile = File(recipesFolder, "shapeless/charmedchars.yml")

        return shapedFile.exists() || shapelessFile.exists()
    }

    /**
     * Performs the full Nexo setup process
     *
     * @param force If true, overwrites existing configuration
     * @return Result of the setup operation
     */
    fun setup(force: Boolean = false): SetupResult {
        if (!isNexoAvailable()) {
            return SetupResult.NexoNotInstalled
        }

        if (isAlreadySetup() && !force) {
            return SetupResult.AlreadySetup
        }

        try {
            // Create directory structure
            createDirectories()

            // Generate and write items configuration
            generateItemsConfig()

            // Copy texture files
            copyTextures()

            // Generate block models (Nexo may require these like Oraxen does)
            generateBlockModels()

            // Generate recipes
            generateRecipes()

            // Create README
            createReadme()

            return SetupResult.Success
        } catch (e: Exception) {
            plugin.logger.severe("Failed to setup Nexo integration: ${e.message}")
            e.printStackTrace()
            return SetupResult.Error(e.message ?: "Unknown error")
        }
    }

    /**
     * Creates required directory structure
     */
    private fun createDirectories() {
        nexoItemsFolder.mkdirs()
        nexoTexturesFolder.mkdirs()
        nexoModelsFolder.mkdirs()
        recipesFolder.mkdirs()

        // Create block texture folders (for letter/number/operator blocks)
        val blockTexturesFolder = File(nexoTexturesFolder, "block")
        listOf("cyan", "magenta", "yellow").forEach { color ->
            File(blockTexturesFolder, color).mkdirs()
        }

        // Create item texture folder (for pyrite items)
        File(nexoTexturesFolder, "item/pyrite").mkdirs()
    }

    /**
     * Generates the items configuration YAML file for all CharmedChars items
     */
    private fun generateItemsConfig() {
        val config = StringBuilder()

        config.appendLine("# CharmedChars Items Configuration for Nexo")
        config.appendLine("# Auto-generated by CharmedChars plugin")
        config.appendLine("# DO NOT EDIT MANUALLY - Use /nexosetup force to regenerate")
        config.appendLine()

        // Generate letter blocks (26 letters × 3 colors = 78 items)
        listOf("cyan", "magenta", "yellow").forEach { color ->
            LetterBlock.entries.forEach { letter ->
                val itemId = "${color}_${letter.name.lowercase()}"
                config.appendLine("$itemId:")
                config.appendLine("  itemname: \"<gradient:${getGradientColors(color)}>${letter.name}</gradient>\"")
                config.appendLine("  material: NOTE_BLOCK")
                config.appendLine("  Pack:")
                config.appendLine("    model: charmedchars:block/$color/${letter.name.lowercase()}")
                config.appendLine("  Mechanics:")
                config.appendLine("    custom_block:")
                config.appendLine("      type: NOTEBLOCK")
                config.appendLine("      drop:")
                config.appendLine("        silktouch: false")
                config.appendLine("        fortune: false")
                config.appendLine("        minimal_type: WOODEN")
                config.appendLine("        best_tool: null")
                config.appendLine("        loots:")
                config.appendLine("          - nexo_item: $itemId")
                config.appendLine()
            }
        }

        // Generate number blocks (10 numbers × 3 colors = 30 items)
        listOf("cyan", "magenta", "yellow").forEach { color ->
            NumericBlock.entries.forEach { number ->
                val itemId = "${color}_${number.c}"
                config.appendLine("$itemId:")
                config.appendLine("  itemname: \"<gradient:${getGradientColors(color)}>${number.c}</gradient>\"")
                config.appendLine("  material: NOTE_BLOCK")
                config.appendLine("  Pack:")
                config.appendLine("    model: charmedchars:block/$color/${number.c}")
                config.appendLine("  Mechanics:")
                config.appendLine("    custom_block:")
                config.appendLine("      type: NOTEBLOCK")
                config.appendLine("      drop:")
                config.appendLine("        silktouch: false")
                config.appendLine("        fortune: false")
                config.appendLine("        minimal_type: WOODEN")
                config.appendLine("        best_tool: null")
                config.appendLine("        loots:")
                config.appendLine("          - nexo_item: $itemId")
                config.appendLine()
            }
        }

        // Generate operator blocks (4 operators × 3 colors = 12 items)
        listOf("cyan", "magenta", "yellow").forEach { color ->
            NonAlphaNumBlocks.entries.forEach { operator ->
                val itemId = "${color}_${operator.name.lowercase()}"
                config.appendLine("$itemId:")
                config.appendLine("  itemname: \"<gradient:${getGradientColors(color)}>${operator.charVal}</gradient>\"")
                config.appendLine("  material: NOTE_BLOCK")
                config.appendLine("  Pack:")
                config.appendLine("    model: charmedchars:block/$color/${operator.name.lowercase()}")
                config.appendLine("  Mechanics:")
                config.appendLine("    custom_block:")
                config.appendLine("      type: NOTEBLOCK")
                config.appendLine("      drop:")
                config.appendLine("        silktouch: false")
                config.appendLine("        fortune: false")
                config.appendLine("        minimal_type: WOODEN")
                config.appendLine("        best_tool: null")
                config.appendLine("        loots:")
                config.appendLine("          - nexo_item: $itemId")
                config.appendLine()
            }
        }

        // Generate logo blocks (3 colors = 3 items)
        listOf("cyan", "magenta", "yellow").forEach { color ->
            val itemId = "${color}_logo"
            config.appendLine("$itemId:")
            config.appendLine("  itemname: \"<gradient:${getGradientColors(color)}>Logo</gradient>\"")
            config.appendLine("  material: NOTE_BLOCK")
            config.appendLine("  Pack:")
            config.appendLine("    model: charmedchars:block/$color/logo_block")
            config.appendLine("  Mechanics:")
            config.appendLine("    custom_block:")
            config.appendLine("      type: NOTEBLOCK")
            config.appendLine("      drop:")
            config.appendLine("        silktouch: false")
            config.appendLine("        fortune: false")
            config.appendLine("        minimal_type: WOODEN")
            config.appendLine("        best_tool: null")
            config.appendLine("        loots:")
            config.appendLine("          - nexo_item: $itemId")
            config.appendLine()
        }

        // Generate pyrite items (5 items)
        val pyriteItems = listOf(
            "pyrite_ingot" to "Pyrite Ingot",
            "pyrite_pickaxe" to "Pyrite Pickaxe",
            "pyrite_axe" to "Pyrite Axe",
            "pyrite_shovel" to "Pyrite Shovel",
            "pyrite_hoe" to "Pyrite Hoe"
        )

        pyriteItems.forEach { (id, name) ->
            config.appendLine("$id:")
            config.appendLine("  itemname: \"<gradient:#FFD700:#B8860B>$name</gradient>\"")
            config.appendLine("  material: ${getMaterialForPyrite(id)}")
            config.appendLine("  Pack:")
            config.appendLine("    generate_model: true")
            config.appendLine("    parent_model: \"item/handheld\"")
            config.appendLine("    textures:")
            config.appendLine("      layer0: charmedchars:item/pyrite/$id")

            // Add tool properties for pyrite tools
            if (id != "pyrite_ingot") {
                config.appendLine("  Mechanics:")
                config.appendLine("    durability:")
                config.appendLine("      value: 250")
                config.appendLine("    tool:")
                config.appendLine("      tier: IRON")
            }
            config.appendLine()
        }

        // Write config to file
        charmedCharsItemsFile.writeText(config.toString())
        plugin.logger.info("Generated items configuration: ${charmedCharsItemsFile.name}")
    }

    /**
     * Gets gradient color codes for each color
     */
    private fun getGradientColors(color: String): String {
        return when (color) {
            "cyan" -> "#00FFFF:#00CED1"
            "magenta" -> "#FF00FF:#FF1493"
            "yellow" -> "#FFFF00:#FFD700"
            else -> "#FFFFFF:#CCCCCC"
        }
    }

    /**
     * Gets the base material for pyrite items
     */
    private fun getMaterialForPyrite(itemId: String): String {
        return when (itemId) {
            "pyrite_ingot" -> "BRICK"
            "pyrite_pickaxe" -> "IRON_PICKAXE"
            "pyrite_axe" -> "IRON_AXE"
            "pyrite_shovel" -> "IRON_SHOVEL"
            "pyrite_hoe" -> "IRON_HOE"
            else -> "PAPER"
        }
    }

    /**
     * Copies texture files from plugin resources to Nexo pack folder
     */
    private fun copyTextures() {
        var copiedCount = 0

        // Copy letter textures to block subfolder (use 256×256 from pack-oraxen to stay within Nexo pack size limits)
        listOf("cyan", "magenta", "yellow").forEach { color ->
            LetterBlock.entries.forEach { letter ->
                val resourcePath = "pack-oraxen/assets/minecraft/textures/block/$color/${letter.name.lowercase()}.png"
                val destFile = File(nexoTexturesFolder, "block/$color/${letter.name.lowercase()}.png")
                if (copyResource(resourcePath, destFile)) {
                    copiedCount++
                }
            }
        }

        // Copy number textures to block subfolder
        listOf("cyan", "magenta", "yellow").forEach { color ->
            NumericBlock.entries.forEach { number ->
                val resourcePath = "pack-oraxen/assets/minecraft/textures/block/$color/${number.c}.png"
                val destFile = File(nexoTexturesFolder, "block/$color/${number.c}.png")

                if (copyResource(resourcePath, destFile)) {
                    copiedCount++
                }
            }
        }

        // Copy operator textures to block subfolder
        listOf("cyan", "magenta", "yellow").forEach { color ->
            NonAlphaNumBlocks.entries.forEach { operator ->
                val resourcePath = "pack-oraxen/assets/minecraft/textures/block/$color/${operator.name.lowercase()}.png"
                val destFile = File(nexoTexturesFolder, "block/$color/${operator.name.lowercase()}.png")

                if (copyResource(resourcePath, destFile)) {
                    copiedCount++
                }
            }
        }

        // Copy logo block textures to block subfolder
        listOf("cyan", "magenta", "yellow").forEach { color ->
            val resourcePath = "pack-oraxen/assets/minecraft/textures/block/$color/logo_block.png"
            val destFile = File(nexoTexturesFolder, "block/$color/logo_block.png")
            if (copyResource(resourcePath, destFile)) {
                copiedCount++
            }
        }

        // Copy pyrite item textures to item/pyrite subfolder
        listOf("ingot", "pickaxe", "axe", "shovel", "hoe").forEach { item ->
            val resourcePath = "pack/assets/minecraft/textures/item/pyrite/$item.png"
            val destFile = File(nexoTexturesFolder, "item/pyrite/pyrite_$item.png")

            if (copyResource(resourcePath, destFile)) {
                copiedCount++
            }
        }

        plugin.logger.info("Copied $copiedCount texture files to Nexo pack folder")
    }

    /**
     * Generates block model JSON files for custom blocks
     * Similar to Oraxen, Nexo may benefit from explicit model definitions
     */
    private fun generateBlockModels() {
        var modelCount = 0

        listOf("cyan", "magenta", "yellow").forEach { color ->
            val colorFolder = File(nexoModelsFolder, color)

            // Delete old model files before generating new ones
            if (colorFolder.exists()) {
                colorFolder.listFiles()?.forEach { it.delete() }
            }

            colorFolder.mkdirs()

            // Letter blocks
            LetterBlock.entries.forEach { letter ->
                val model = """
                {
                  "parent": "block/cube_all",
                  "textures": {
                    "all": "charmedchars:block/$color/${letter.name.lowercase()}"
                  }
                }
                """.trimIndent()

                File(colorFolder, "${letter.name.lowercase()}.json").writeText(model)
                modelCount++
            }

            // Number blocks
            NumericBlock.entries.forEach { number ->
                val model = """
                {
                  "parent": "block/cube_all",
                  "textures": {
                    "all": "charmedchars:block/$color/${number.c}"
                  }
                }
                """.trimIndent()

                File(colorFolder, "${number.c}.json").writeText(model)
                modelCount++
            }

            // Operator blocks
            NonAlphaNumBlocks.entries.forEach { operator ->
                val model = """
                {
                  "parent": "block/cube_all",
                  "textures": {
                    "all": "charmedchars:block/$color/${operator.name.lowercase()}"
                  }
                }
                """.trimIndent()

                File(colorFolder, "${operator.name.lowercase()}.json").writeText(model)
                modelCount++
            }

            // Logo block
            val logoModel = """
            {
              "parent": "block/cube_all",
              "textures": {
                "all": "charmedchars:block/$color/logo_block"
              }
            }
            """.trimIndent()

            File(colorFolder, "logo_block.json").writeText(logoModel)
            modelCount++
        }

        plugin.logger.info("Generated $modelCount block model JSON files")
    }

    /**
     * Generates recipe configuration for pyrite items
     *
     * Uses Nexo's official recipe format:
     * - Shapeless recipes use ingredient list format
     * - Shaped recipes use shape + ingredients with letter mapping
     */
    private fun generateRecipes() {
        // Generate shapeless recipes (pyrite_ingot)
        generateShapelessRecipes()

        // Generate shaped recipes (tools)
        generateShapedRecipes()

        plugin.logger.info("Generated recipe configuration")
    }

    /**
     * Generates shapeless recipes (pyrite ingot from iron + redstone)
     */
    private fun generateShapelessRecipes() {
        val shapelessFolder = File(recipesFolder, "shapeless")
        shapelessFolder.mkdirs()

        val shapelessRecipes = StringBuilder()
        val shapelessFile = File(shapelessFolder, "charmedchars.yml")

        // Don't append to existing file - overwrite it
        shapelessRecipes.appendLine("# CharmedChars Shapeless Recipes")
        shapelessRecipes.appendLine("# Auto-generated by CharmedChars plugin")
        shapelessRecipes.appendLine()

        // Pyrite ingot recipe (shapeless: Iron + Redstone)
        shapelessRecipes.appendLine("pyrite_ingot:")
        shapelessRecipes.appendLine("  result:")
        shapelessRecipes.appendLine("    nexo_item: pyrite_ingot")
        shapelessRecipes.appendLine("    amount: 1")
        shapelessRecipes.appendLine("  ingredients:")
        shapelessRecipes.appendLine("    A:")
        shapelessRecipes.appendLine("      amount: 1")
        shapelessRecipes.appendLine("      minecraft_type: IRON_INGOT")
        shapelessRecipes.appendLine("    B:")
        shapelessRecipes.appendLine("      amount: 1")
        shapelessRecipes.appendLine("      minecraft_type: REDSTONE")
        shapelessRecipes.appendLine()

        shapelessFile.writeText(shapelessRecipes.toString())
    }

    /**
     * Generates shaped recipes (pyrite tools)
     * Uses Nexo's shape format with letter-based ingredient mapping
     */
    private fun generateShapedRecipes() {
        val shapedFolder = File(recipesFolder, "shaped")
        shapedFolder.mkdirs()

        val shapedRecipes = StringBuilder()
        val shapedFile = File(shapedFolder, "charmedchars.yml")

        // Don't append to existing file - overwrite it
        shapedRecipes.appendLine("# CharmedChars Shaped Recipes")
        shapedRecipes.appendLine("# Auto-generated by CharmedChars plugin")
        shapedRecipes.appendLine()

        // Pyrite pickaxe
        shapedRecipes.appendLine("pyrite_pickaxe:")
        shapedRecipes.appendLine("  result:")
        shapedRecipes.appendLine("    nexo_item: pyrite_pickaxe")
        shapedRecipes.appendLine("    amount: 1")
        shapedRecipes.appendLine("  ingredients:")
        shapedRecipes.appendLine("    P:")
        shapedRecipes.appendLine("      amount: 3")
        shapedRecipes.appendLine("      nexo_item: pyrite_ingot")
        shapedRecipes.appendLine("    S:")
        shapedRecipes.appendLine("      amount: 2")
        shapedRecipes.appendLine("      minecraft_type: STICK")
        shapedRecipes.appendLine("  shape:")
        shapedRecipes.appendLine("    - PPP")
        shapedRecipes.appendLine("    - ' S '")
        shapedRecipes.appendLine("    - ' S '")
        shapedRecipes.appendLine()

        // Pyrite axe
        shapedRecipes.appendLine("pyrite_axe:")
        shapedRecipes.appendLine("  result:")
        shapedRecipes.appendLine("    nexo_item: pyrite_axe")
        shapedRecipes.appendLine("    amount: 1")
        shapedRecipes.appendLine("  ingredients:")
        shapedRecipes.appendLine("    P:")
        shapedRecipes.appendLine("      amount: 3")
        shapedRecipes.appendLine("      nexo_item: pyrite_ingot")
        shapedRecipes.appendLine("    S:")
        shapedRecipes.appendLine("      amount: 2")
        shapedRecipes.appendLine("      minecraft_type: STICK")
        shapedRecipes.appendLine("  shape:")
        shapedRecipes.appendLine("    - 'PP '")
        shapedRecipes.appendLine("    - 'PS '")
        shapedRecipes.appendLine("    - ' S '")
        shapedRecipes.appendLine()

        // Pyrite shovel
        shapedRecipes.appendLine("pyrite_shovel:")
        shapedRecipes.appendLine("  result:")
        shapedRecipes.appendLine("    nexo_item: pyrite_shovel")
        shapedRecipes.appendLine("    amount: 1")
        shapedRecipes.appendLine("  ingredients:")
        shapedRecipes.appendLine("    P:")
        shapedRecipes.appendLine("      amount: 1")
        shapedRecipes.appendLine("      nexo_item: pyrite_ingot")
        shapedRecipes.appendLine("    S:")
        shapedRecipes.appendLine("      amount: 2")
        shapedRecipes.appendLine("      minecraft_type: STICK")
        shapedRecipes.appendLine("  shape:")
        shapedRecipes.appendLine("    - ' P '")
        shapedRecipes.appendLine("    - ' S '")
        shapedRecipes.appendLine("    - ' S '")
        shapedRecipes.appendLine()

        // Pyrite hoe
        shapedRecipes.appendLine("pyrite_hoe:")
        shapedRecipes.appendLine("  result:")
        shapedRecipes.appendLine("    nexo_item: pyrite_hoe")
        shapedRecipes.appendLine("    amount: 1")
        shapedRecipes.appendLine("  ingredients:")
        shapedRecipes.appendLine("    P:")
        shapedRecipes.appendLine("      amount: 2")
        shapedRecipes.appendLine("      nexo_item: pyrite_ingot")
        shapedRecipes.appendLine("    S:")
        shapedRecipes.appendLine("      amount: 2")
        shapedRecipes.appendLine("      minecraft_type: STICK")
        shapedRecipes.appendLine("  shape:")
        shapedRecipes.appendLine("    - 'PP '")
        shapedRecipes.appendLine("    - ' S '")
        shapedRecipes.appendLine("    - ' S '")
        shapedRecipes.appendLine()

        shapedFile.writeText(shapedRecipes.toString())
    }

    /**
     * Creates a README file with setup instructions
     */
    private fun createReadme() {
        val readme = """
# CharmedChars - Nexo Integration

This folder contains auto-generated configuration for CharmedChars custom items and blocks.

## What was generated:

- **items/charmedchars_blocks.yml**: 128 custom items (123 blocks + 5 pyrite items)
- **pack/assets/charmedchars/textures/**: 128 texture files in cyan/magenta/yellow colors
- **pack/assets/charmedchars/models/block/**: Block model JSON files
- **recipes/shaped.yml**: Pyrite crafting recipes (updated)

## Next Steps:

1. Run `/nexo reload all` to load the new items
2. Restart your server (recommended)
3. Players will automatically receive the updated resource pack

## Testing CharmedChars:

- Use `/charblock <player> <color> <letter>` to give letter blocks
- Place letter blocks in horizontal or vertical lines
- Break any letter with a gold or pyrite tool to score words
- See PLAY_INSTRUCTIONS.md for complete gameplay guide

## Regenerating Configuration:

If you need to regenerate this configuration:
```
/nexosetup force
```

## Support:

- GitHub: https://github.com/badbabywentaway/CharmedChars
- Issues: https://github.com/badbabywentaway/CharmedChars/issues

---
Generated by CharmedChars v${plugin.description.version}
**IMPORTANT**: Nexo integration requires Paper block update optimizations to be enabled.
See NEXO_SETUP.md in the CharmedChars GitHub repository for configuration instructions.
Report any issues on GitHub!
        """.trimIndent()

        File(nexoFolder, "CharmedChars_README.txt").writeText(readme)
    }

    /**
     * Copies a resource from plugin JAR to destination file
     *
     * @param resourcePath Path to resource in JAR
     * @param destFile Destination file
     * @return true if successful
     */
    private fun copyResource(resourcePath: String, destFile: File): Boolean {
        return try {
            plugin.getResource(resourcePath)?.use { input ->
                destFile.parentFile.mkdirs()
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            plugin.logger.warning("Failed to copy resource $resourcePath: ${e.message}")
            false
        }
    }

    /**
     * Represents the result of a setup operation
     */
    sealed class SetupResult {
        object Success : SetupResult()
        object NexoNotInstalled : SetupResult()
        object AlreadySetup : SetupResult()
        data class Error(val message: String) : SetupResult()
    }
}
