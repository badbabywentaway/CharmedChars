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

/**
 * Automatic setup utility for Oraxen integration
 *
 * Handles creating CharmedChars custom block configurations and textures for
 * Oraxen plugin directory. Automates the manual setup process by:
 * - Creating the required directory structure
 * - Generating items configuration files (123 blocks + 5 pyrite items)
 * - Generating recipe configuration
 * - Copying all texture files (128 total)
 * - Creating a README with setup instructions
 *
 * @property plugin The CharmedChars plugin instance
 */
class OraxenSetup(private val plugin: CharmedChars) {

    /**
     * Oraxen plugin root folder
     */
    private val oraxenFolder = File(plugin.server.pluginManager.getPlugin("Oraxen")?.dataFolder?.parentFile, "Oraxen")

    /**
     * Oraxen items configuration folder
     */
    private val oraxenItemsFolder = File(oraxenFolder, "items")

    /**
     * Oraxen pack root folder
     */
    private val oraxenPackFolder = File(oraxenFolder, "pack")

    /**
     * Oraxen pack assets folder (for custom namespace)
     */
    private val oraxenAssetsFolder = File(oraxenPackFolder, "assets/charmedchars")

    /**
     * Oraxen pack textures folder
     */
    private val oraxenTexturesFolder = File(oraxenAssetsFolder, "textures")

    /**
     * Oraxen recipes folder
     */
    private val oraxenRecipesFolder = File(oraxenFolder, "recipes")

    /**
     * CharmedChars items config file
     */
    private val charmedCharsItemsFile = File(oraxenItemsFolder, "charmedchars_blocks.yml")

    /**
     * Oraxen shaped recipes file
     */
    private val shapedRecipesFile = File(oraxenRecipesFolder, "shaped.yml")

    /**
     * Oraxen shapeless recipes file
     */
    private val shapelessRecipesFile = File(oraxenRecipesFolder, "shapeless.yml")

    /**
     * Checks if Oraxen is installed and loaded
     *
     * @return true if Oraxen plugin is available
     */
    fun isOraxenAvailable(): Boolean {
        return plugin.server.pluginManager.getPlugin("Oraxen") != null
    }

    /**
     * Checks if CharmedChars configuration already exists in Oraxen
     *
     * @return true if the configuration files exist
     */
    fun isAlreadySetup(): Boolean {
        return charmedCharsItemsFile.exists() && hasCharmedCharsRecipes()
    }

    /**
     * Checks if CharmedChars recipes already exist in Oraxen recipe files
     */
    private fun hasCharmedCharsRecipes(): Boolean {
        return shapelessRecipesFile.exists() &&
               shapelessRecipesFile.readText().contains("pyrite_ingot:")
    }

    /**
     * Performs automatic setup of Oraxen configuration
     *
     * Executes the complete setup process:
     * 1. Verifies Oraxen is installed
     * 2. Checks if already setup (deletes if force is true)
     * 3. Creates directory structure
     * 4. Generates items configuration (blocks and pyrite tools)
     * 5. Generates recipes configuration
     * 6. Copies all texture files (128 textures)
     * 7. Creates README with instructions
     *
     * @param force If true, deletes existing configuration and regenerates
     * @return SetupResult containing success status, messages, and whether it was already setup
     */
    fun autoSetup(force: Boolean = false): SetupResult {
        val messages = mutableListOf<String>()

        // Check if Oraxen is available
        if (!isOraxenAvailable()) {
            return SetupResult(
                success = false,
                alreadySetup = false,
                messages = listOf("Oraxen plugin not found! Please install Oraxen first.")
            )
        }

        // Check if already setup
        if (isAlreadySetup()) {
            if (force) {
                messages.add("Force mode enabled - deleting existing configuration...")
                try {
                    charmedCharsItemsFile.delete()
                    removeCharmedCharsRecipes()
                    val texturesDir = File(oraxenPackFolder, "charmedchars")
                    texturesDir.deleteRecursively()
                    messages.add("  ✓ Old configuration deleted")
                } catch (e: Exception) {
                    messages.add("  ✗ Failed to delete old configuration: ${e.message}")
                    return SetupResult(false, false, messages)
                }
            } else {
                return SetupResult(
                    success = true,
                    alreadySetup = true,
                    messages = listOf(
                        "CharmedChars Oraxen configuration already exists.",
                        "Files are located in: ${oraxenItemsFolder.absolutePath}",
                        "To regenerate, run /oraxensetup force"
                    )
                )
            }
        }

        try {
            // Create directory structure
            messages.add("Creating directory structure...")
            oraxenItemsFolder.mkdirs()
            oraxenRecipesFolder.mkdirs()
            oraxenTexturesFolder.mkdirs()

            // Generate items configuration
            messages.add("Generating items configuration...")
            if (generateItemsConfig()) {
                messages.add("  ✓ Items configuration created (128 items)")
            } else {
                messages.add("  ✗ Failed to create items configuration")
                return SetupResult(false, false, messages)
            }

            // Generate recipes configuration
            messages.add("Generating recipes configuration...")
            if (generateRecipesConfig()) {
                messages.add("  ✓ Recipes configuration created (5 recipes)")
            } else {
                messages.add("  ✗ Failed to create recipes configuration")
                return SetupResult(false, false, messages)
            }

            // Generate model JSON files in minecraft namespace (Oraxen compatibility)
            messages.add("Generating model files...")
            var modelCount = 0
            val minecraftAssetsDir = File(oraxenPackFolder, "assets/minecraft")
            val blockModelsDir = File(minecraftAssetsDir, "models/block")
            val itemModelsDir = File(minecraftAssetsDir, "models/item")
            blockModelsDir.mkdirs()
            itemModelsDir.mkdirs()

            val colors = listOf("cyan", "magenta", "yellow")

            // Generate letter models
            for (color in colors) {
                for (letter in 'a'..'z') {
                    val texturePath = "charmedchars:block/$color/$letter"
                    if (generateBlockModel("${color}_${letter}", texturePath, blockModelsDir)) {
                        modelCount++
                    }
                    if (generateItemModel("${color}_${letter}", "minecraft:block/${color}_${letter}", itemModelsDir)) {
                        modelCount++
                    }
                }

                // Generate number models
                for (number in 0..9) {
                    val texturePath = "charmedchars:block/$color/$number"
                    if (generateBlockModel("${color}_${number}", texturePath, blockModelsDir)) {
                        modelCount++
                    }
                    if (generateItemModel("${color}_${number}", "minecraft:block/${color}_${number}", itemModelsDir)) {
                        modelCount++
                    }
                }

                // Generate operator models
                val operators = listOf("plus", "minus", "multiply", "division")
                for (op in operators) {
                    val texturePath = "charmedchars:block/$color/$op"
                    if (generateBlockModel("${color}_${op}", texturePath, blockModelsDir)) {
                        modelCount++
                    }
                    if (generateItemModel("${color}_${op}", "minecraft:block/${color}_${op}", itemModelsDir)) {
                        modelCount++
                    }
                }
            }
            messages.add("  ✓ Generated $modelCount model files (blocks + items)")

            // Copy textures
            messages.add("Copying texture files...")
            var textureCount = 0

            for (color in colors) {
                val colorDir = File(oraxenTexturesFolder, "block/$color")
                colorDir.mkdirs()

                // Copy letter textures (a-z) - using 256x256 downscaled versions for Oraxen
                for (letter in 'a'..'z') {
                    if (copyResourceToFile(
                            "pack-oraxen/assets/minecraft/textures/block/$color/$letter.png",
                            File(colorDir, "$letter.png")
                        )) {
                        textureCount++
                    }
                }

                // Copy number textures (0-9) - using 256x256 downscaled versions for Oraxen
                for (number in 0..9) {
                    if (copyResourceToFile(
                            "pack-oraxen/assets/minecraft/textures/block/$color/$number.png",
                            File(colorDir, "$number.png")
                        )) {
                        textureCount++
                    }
                }

                // Copy operator textures - using 256x256 downscaled versions for Oraxen
                val operators = listOf("plus", "minus", "multiply", "division")
                for (op in operators) {
                    if (copyResourceToFile(
                            "pack-oraxen/assets/minecraft/textures/block/$color/$op.png",
                            File(colorDir, "$op.png")
                        )) {
                        textureCount++
                    }
                }

                // Copy logo block - using 256x256 downscaled versions for Oraxen
                if (copyResourceToFile(
                        "pack-oraxen/assets/minecraft/textures/block/$color/logo_block.png",
                        File(colorDir, "logo_block.png")
                    )) {
                    textureCount++
                }
            }

            messages.add("  ✓ Copied $textureCount block texture files")

            // Copy pyrite item textures (16x16, copied as-is)
            messages.add("Copying pyrite item textures...")
            var pyriteTextureCount = 0
            val pyriteItemTexturesDir = File(oraxenTexturesFolder, "item/pyrite")
            pyriteItemTexturesDir.mkdirs()

            val pyriteItems = listOf("ingot", "pickaxe", "axe", "shovel", "hoe")
            for (item in pyriteItems) {
                if (copyResourceToFile(
                        "pack-oraxen/assets/minecraft/textures/item/pyrite/$item.png",
                        File(pyriteItemTexturesDir, "$item.png")
                    )) {
                    pyriteTextureCount++
                }
            }
            messages.add("  ✓ Copied $pyriteTextureCount pyrite texture files")

            // Create README
            messages.add("Creating README...")
            createReadme()
            messages.add("  ✓ README.txt created")

            messages.add("")
            messages.add("=== Setup Complete! ===")
            messages.add("Files copied to: ${oraxenItemsFolder.absolutePath}")
            messages.add("")
            messages.add("NEXT STEPS:")
            messages.add("1. Run /oraxen reload all")
            messages.add("2. Restart the server (recommended)")
            messages.add("3. Test with /charblock <player> cyan hello")
            messages.add("")

            return SetupResult(
                success = true,
                alreadySetup = false,
                messages = messages
            )

        } catch (e: Exception) {
            messages.add("ERROR: ${e.message}")
            plugin.logger.severe("Failed to auto-setup Oraxen configuration: ${e.message}")
            e.printStackTrace()
            return SetupResult(
                success = false,
                alreadySetup = false,
                messages = messages
            )
        }
    }

    /**
     * Generates the items configuration file for Oraxen
     *
     * Creates a YAML file with all 123 letter/number/operator blocks and 5 pyrite items
     */
    private fun generateItemsConfig(): Boolean {
        try {
            val yaml = StringBuilder()

            // Header
            yaml.appendLine("# CharmedChars - Oraxen Items Configuration")
            yaml.appendLine("# Auto-generated by CharmedChars plugin")
            yaml.appendLine("# Total: 128 items (123 blocks + 5 pyrite items)")
            yaml.appendLine("")

            val colors = listOf("cyan", "magenta", "yellow")
            val colorDisplayNames = mapOf(
                "cyan" to "<cyan>",
                "magenta" to "<light_purple>",
                "yellow" to "<yellow>"
            )

            var customVariation = 1

            // Generate letter blocks
            for (color in colors) {
                val displayColor = colorDisplayNames[color]
                yaml.appendLine("# ==================== ${color.uppercase()} LETTER BLOCKS ====================")

                for (letter in 'a'..'z') {
                    yaml.appendLine("${color}_${letter}:")
                    yaml.appendLine("  displayname: \"$displayColor${letter.uppercaseChar()} Block\"")
                    yaml.appendLine("  material: PAPER")
                    yaml.appendLine("  Pack:")
                    yaml.appendLine("    generate_model: false")
                    yaml.appendLine("    model: item/${color}_${letter}")
                    yaml.appendLine("  Mechanics:")
                    yaml.appendLine("    noteblock:")
                    yaml.appendLine("      custom_variation: ${customVariation++}")
                    yaml.appendLine("      directional: false")
                    yaml.appendLine("      hardness: 0.8")
                    yaml.appendLine("      block_model: block/${color}_${letter}")
                    yaml.appendLine("      drop:")
                    yaml.appendLine("        silktouch: false")
                    yaml.appendLine("        minimal_type:")
                    yaml.appendLine("          - WOODEN_PICKAXE")
                    yaml.appendLine("          - GOLDEN_PICKAXE")
                    yaml.appendLine("        loots:")
                    yaml.appendLine("          - { oraxen_item: ${color}_${letter}, probability: 1.0 }")
                    yaml.appendLine("")
                }
            }

            // Generate number blocks
            for (color in colors) {
                val displayColor = colorDisplayNames[color]
                yaml.appendLine("# ==================== ${color.uppercase()} NUMBER BLOCKS ====================")

                for (number in 0..9) {
                    yaml.appendLine("${color}_${number}:")
                    yaml.appendLine("  displayname: \"$displayColor$number Block\"")
                    yaml.appendLine("  material: PAPER")
                    yaml.appendLine("  Pack:")
                    yaml.appendLine("    generate_model: false")
                    yaml.appendLine("    model: item/${color}_${number}")
                    yaml.appendLine("  Mechanics:")
                    yaml.appendLine("    noteblock:")
                    yaml.appendLine("      custom_variation: ${customVariation++}")
                    yaml.appendLine("      directional: false")
                    yaml.appendLine("      hardness: 0.8")
                    yaml.appendLine("      block_model: block/${color}_${number}")
                    yaml.appendLine("      drop:")
                    yaml.appendLine("        silktouch: false")
                    yaml.appendLine("        minimal_type:")
                    yaml.appendLine("          - WOODEN_PICKAXE")
                    yaml.appendLine("          - GOLDEN_PICKAXE")
                    yaml.appendLine("        loots:")
                    yaml.appendLine("          - { oraxen_item: ${color}_${number}, probability: 1.0 }")
                    yaml.appendLine("")
                }
            }

            // Generate operator blocks
            val operators = listOf(
                "plus" to "+",
                "minus" to "-",
                "multiply" to "×",
                "division" to "÷"
            )

            for (color in colors) {
                val displayColor = colorDisplayNames[color]
                yaml.appendLine("# ==================== ${color.uppercase()} OPERATOR BLOCKS ====================")

                for ((opName, opSymbol) in operators) {
                    yaml.appendLine("${color}_${opName}:")
                    yaml.appendLine("  displayname: \"$displayColor$opSymbol Block\"")
                    yaml.appendLine("  material: PAPER")
                    yaml.appendLine("  Pack:")
                    yaml.appendLine("    generate_model: false")
                    yaml.appendLine("    model: item/${color}_${opName}")
                    yaml.appendLine("  Mechanics:")
                    yaml.appendLine("    noteblock:")
                    yaml.appendLine("      custom_variation: ${customVariation++}")
                    yaml.appendLine("      directional: false")
                    yaml.appendLine("      hardness: 0.8")
                    yaml.appendLine("      block_model: block/${color}_${opName}")
                    yaml.appendLine("      drop:")
                    yaml.appendLine("        silktouch: false")
                    yaml.appendLine("        minimal_type:")
                    yaml.appendLine("          - WOODEN_PICKAXE")
                    yaml.appendLine("          - GOLDEN_PICKAXE")
                    yaml.appendLine("        loots:")
                    yaml.appendLine("          - { oraxen_item: ${color}_${opName}, probability: 1.0 }")
                    yaml.appendLine("")
                }
            }

            // Generate pyrite items
            yaml.appendLine("# ==================== PYRITE (FOOL'S GOLD) ITEMS ====================")

            yaml.appendLine("pyrite_ingot:")
            yaml.appendLine("  displayname: \"<gold>Pyrite Ingot\"")
            yaml.appendLine("  material: PAPER")
            yaml.appendLine("  Pack:")
            yaml.appendLine("    generate_model: true")
            yaml.appendLine("    parent_model: \"item/generated\"")
            yaml.appendLine("    textures:")
            yaml.appendLine("      layer0: charmedchars:item/pyrite/ingot")
            yaml.appendLine("")

            yaml.appendLine("pyrite_pickaxe:")
            yaml.appendLine("  displayname: \"<gold>Pyrite Pickaxe\"")
            yaml.appendLine("  material: GOLDEN_PICKAXE")
            yaml.appendLine("  excludeFromInventory: false")
            yaml.appendLine("  lore:")
            yaml.appendLine("    - \"<gray>A fool's gold pickaxe\"")
            yaml.appendLine("    - \"<gray>Works like gold for scoring words\"")
            yaml.appendLine("    - \"<gray>Iron-tier durability and stats\"")
            yaml.appendLine("  Pack:")
            yaml.appendLine("    generate_model: true")
            yaml.appendLine("    parent_model: \"item/handheld\"")
            yaml.appendLine("    textures:")
            yaml.appendLine("      layer0: charmedchars:item/pyrite/pickaxe")
            yaml.appendLine("  Mechanics:")
            yaml.appendLine("    durability:")
            yaml.appendLine("      value: 250")
            yaml.appendLine("  AttributeModifiers:")
            yaml.appendLine("    GENERIC_ATTACK_DAMAGE:")
            yaml.appendLine("      amount: 4")
            yaml.appendLine("      slot: HAND")
            yaml.appendLine("    GENERIC_ATTACK_SPEED:")
            yaml.appendLine("      amount: -2.8")
            yaml.appendLine("      slot: HAND")
            yaml.appendLine("")

            yaml.appendLine("pyrite_axe:")
            yaml.appendLine("  displayname: \"<gold>Pyrite Axe\"")
            yaml.appendLine("  material: GOLDEN_AXE")
            yaml.appendLine("  excludeFromInventory: false")
            yaml.appendLine("  lore:")
            yaml.appendLine("    - \"<gray>A fool's gold axe\"")
            yaml.appendLine("    - \"<gray>Works like gold for scoring words\"")
            yaml.appendLine("    - \"<gray>Iron-tier durability and stats\"")
            yaml.appendLine("  Pack:")
            yaml.appendLine("    generate_model: true")
            yaml.appendLine("    parent_model: \"item/handheld\"")
            yaml.appendLine("    textures:")
            yaml.appendLine("      layer0: charmedchars:item/pyrite/axe")
            yaml.appendLine("  Mechanics:")
            yaml.appendLine("    durability:")
            yaml.appendLine("      value: 250")
            yaml.appendLine("  AttributeModifiers:")
            yaml.appendLine("    GENERIC_ATTACK_DAMAGE:")
            yaml.appendLine("      amount: 9")
            yaml.appendLine("      slot: HAND")
            yaml.appendLine("    GENERIC_ATTACK_SPEED:")
            yaml.appendLine("      amount: -3.1")
            yaml.appendLine("      slot: HAND")
            yaml.appendLine("")

            yaml.appendLine("pyrite_shovel:")
            yaml.appendLine("  displayname: \"<gold>Pyrite Shovel\"")
            yaml.appendLine("  material: GOLDEN_SHOVEL")
            yaml.appendLine("  excludeFromInventory: false")
            yaml.appendLine("  lore:")
            yaml.appendLine("    - \"<gray>A fool's gold shovel\"")
            yaml.appendLine("    - \"<gray>Works like gold for scoring words\"")
            yaml.appendLine("    - \"<gray>Iron-tier durability and stats\"")
            yaml.appendLine("  Pack:")
            yaml.appendLine("    generate_model: true")
            yaml.appendLine("    parent_model: \"item/handheld\"")
            yaml.appendLine("    textures:")
            yaml.appendLine("      layer0: charmedchars:item/pyrite/shovel")
            yaml.appendLine("  Mechanics:")
            yaml.appendLine("    durability:")
            yaml.appendLine("      value: 250")
            yaml.appendLine("  AttributeModifiers:")
            yaml.appendLine("    GENERIC_ATTACK_DAMAGE:")
            yaml.appendLine("      amount: 4.5")
            yaml.appendLine("      slot: HAND")
            yaml.appendLine("    GENERIC_ATTACK_SPEED:")
            yaml.appendLine("      amount: -3.0")
            yaml.appendLine("      slot: HAND")
            yaml.appendLine("")

            yaml.appendLine("pyrite_hoe:")
            yaml.appendLine("  displayname: \"<gold>Pyrite Hoe\"")
            yaml.appendLine("  material: GOLDEN_HOE")
            yaml.appendLine("  excludeFromInventory: false")
            yaml.appendLine("  lore:")
            yaml.appendLine("    - \"<gray>A fool's gold hoe\"")
            yaml.appendLine("    - \"<gray>Works like gold for scoring words\"")
            yaml.appendLine("    - \"<gray>Iron-tier durability and stats\"")
            yaml.appendLine("  Pack:")
            yaml.appendLine("    generate_model: true")
            yaml.appendLine("    parent_model: \"item/handheld\"")
            yaml.appendLine("    textures:")
            yaml.appendLine("      layer0: charmedchars:item/pyrite/hoe")
            yaml.appendLine("  Mechanics:")
            yaml.appendLine("    durability:")
            yaml.appendLine("      value: 250")
            yaml.appendLine("  AttributeModifiers:")
            yaml.appendLine("    GENERIC_ATTACK_DAMAGE:")
            yaml.appendLine("      amount: 1")
            yaml.appendLine("      slot: HAND")
            yaml.appendLine("    GENERIC_ATTACK_SPEED:")
            yaml.appendLine("      amount: -1.0")
            yaml.appendLine("      slot: HAND")
            yaml.appendLine("")

            charmedCharsItemsFile.writeText(yaml.toString())
            return true

        } catch (e: Exception) {
            plugin.logger.severe("Failed to generate items config: ${e.message}")
            e.printStackTrace()
            return false
        }
    }

    /**
     * Removes CharmedChars recipes from Oraxen recipe files
     */
    private fun removeCharmedCharsRecipes() {
        try {
            // Remove from shapeless.yml
            if (shapelessRecipesFile.exists()) {
                val content = shapelessRecipesFile.readText()
                val updated = content.replace(Regex("pyrite_ingot:.*?(?=\\n\\w+:|$)", RegexOption.DOT_MATCHES_ALL), "").trim()
                shapelessRecipesFile.writeText(updated)
            }

            // Remove from shaped.yml
            if (shapedRecipesFile.exists()) {
                var content = shapedRecipesFile.readText()
                content = content.replace(Regex("pyrite_pickaxe:.*?(?=\\n\\w+:|$)", RegexOption.DOT_MATCHES_ALL), "")
                content = content.replace(Regex("pyrite_axe:.*?(?=\\n\\w+:|$)", RegexOption.DOT_MATCHES_ALL), "")
                content = content.replace(Regex("pyrite_shovel:.*?(?=\\n\\w+:|$)", RegexOption.DOT_MATCHES_ALL), "")
                content = content.replace(Regex("pyrite_hoe:.*?(?=\\n\\w+:|$)", RegexOption.DOT_MATCHES_ALL), "")
                shapedRecipesFile.writeText(content.trim())
            }
        } catch (e: Exception) {
            plugin.logger.warning("Failed to remove old recipes: ${e.message}")
        }
    }

    /**
     * Generates the recipes configuration for Oraxen
     *
     * Appends pyrite recipes to Oraxen's shaped.yml and shapeless.yml files
     */
    private fun generateRecipesConfig(): Boolean {
        try {
            // Generate shapeless recipe (pyrite_ingot)
            val shapeless = StringBuilder()
            shapeless.appendLine("")
            shapeless.appendLine("# CharmedChars - Pyrite Ingot Recipe")
            shapeless.appendLine("pyrite_ingot:")
            shapeless.appendLine("  result:")
            shapeless.appendLine("    oraxen_item: pyrite_ingot")
            shapeless.appendLine("  ingredients:")
            shapeless.appendLine("    A:")
            shapeless.appendLine("      amount: 1")
            shapeless.appendLine("      minecraft_type: IRON_INGOT")
            shapeless.appendLine("    B:")
            shapeless.appendLine("      amount: 1")
            shapeless.appendLine("      minecraft_type: REDSTONE")

            // Append to shapeless.yml
            shapelessRecipesFile.appendText(shapeless.toString())

            // Generate shaped recipes (pyrite tools)
            val shaped = StringBuilder()

            shaped.appendLine("")
            shaped.appendLine("# CharmedChars - Pyrite Tool Recipes")
            shaped.appendLine("pyrite_pickaxe:")
            shaped.appendLine("  shape:")
            shaped.appendLine("    - PPP")
            shaped.appendLine("    - _S_")
            shaped.appendLine("    - _S_")
            shaped.appendLine("  result:")
            shaped.appendLine("    oraxen_item: pyrite_pickaxe")
            shaped.appendLine("  ingredients:")
            shaped.appendLine("    P:")
            shaped.appendLine("      oraxen_item: pyrite_ingot")
            shaped.appendLine("    S:")
            shaped.appendLine("      minecraft_type: STICK")
            shaped.appendLine("")

            shaped.appendLine("pyrite_axe:")
            shaped.appendLine("  shape:")
            shaped.appendLine("    - PP_")
            shaped.appendLine("    - PS_")
            shaped.appendLine("    - _S_")
            shaped.appendLine("  result:")
            shaped.appendLine("    oraxen_item: pyrite_axe")
            shaped.appendLine("  ingredients:")
            shaped.appendLine("    P:")
            shaped.appendLine("      oraxen_item: pyrite_ingot")
            shaped.appendLine("    S:")
            shaped.appendLine("      minecraft_type: STICK")
            shaped.appendLine("")

            shaped.appendLine("pyrite_shovel:")
            shaped.appendLine("  shape:")
            shaped.appendLine("    - _P_")
            shaped.appendLine("    - _S_")
            shaped.appendLine("    - _S_")
            shaped.appendLine("  result:")
            shaped.appendLine("    oraxen_item: pyrite_shovel")
            shaped.appendLine("  ingredients:")
            shaped.appendLine("    P:")
            shaped.appendLine("      oraxen_item: pyrite_ingot")
            shaped.appendLine("    S:")
            shaped.appendLine("      minecraft_type: STICK")
            shaped.appendLine("")

            shaped.appendLine("pyrite_hoe:")
            shaped.appendLine("  shape:")
            shaped.appendLine("    - PP_")
            shaped.appendLine("    - _S_")
            shaped.appendLine("    - _S_")
            shaped.appendLine("  result:")
            shaped.appendLine("    oraxen_item: pyrite_hoe")
            shaped.appendLine("  ingredients:")
            shaped.appendLine("    P:")
            shaped.appendLine("      oraxen_item: pyrite_ingot")
            shaped.appendLine("    S:")
            shaped.appendLine("      minecraft_type: STICK")

            // Append to shaped.yml
            shapedRecipesFile.appendText(shaped.toString())

            return true

        } catch (e: Exception) {
            plugin.logger.severe("Failed to generate recipes config: ${e.message}")
            e.printStackTrace()
            return false
        }
    }

    /**
     * Generates a block model JSON file for Oraxen
     *
     * Creates a cube_all model JSON that references the texture
     *
     * @param modelName The name of the model (e.g., "cyan_a")
     * @param texturePath The texture path (e.g., "charmedchars:block/cyan/a")
     * @param modelsDir The directory to save the model file in
     * @return true if the model was created successfully
     */
    private fun generateBlockModel(modelName: String, texturePath: String, modelsDir: File): Boolean {
        try {
            val modelFile = File(modelsDir, "$modelName.json")
            val modelJson = """
{
  "parent": "minecraft:block/cube_all",
  "textures": {
    "all": "$texturePath"
  }
}
            """.trimIndent()

            modelFile.writeText(modelJson)
            return true
        } catch (e: Exception) {
            plugin.logger.warning("Failed to generate block model $modelName: ${e.message}")
            return false
        }
    }

    /**
     * Generates an item model JSON file for Oraxen
     *
     * Creates an item model that parents to the block model
     *
     * @param modelName The name of the model (e.g., "cyan_a")
     * @param blockModelPath The block model path (e.g., "charmedchars:block/cyan_a")
     * @param modelsDir The directory to save the model file in
     * @return true if the model was created successfully
     */
    private fun generateItemModel(modelName: String, blockModelPath: String, modelsDir: File): Boolean {
        try {
            val modelFile = File(modelsDir, "$modelName.json")
            val modelJson = """
{
  "parent": "$blockModelPath"
}
            """.trimIndent()

            modelFile.writeText(modelJson)
            return true
        } catch (e: Exception) {
            plugin.logger.warning("Failed to generate item model $modelName: ${e.message}")
            return false
        }
    }

    /**
     * Copies a resource from the plugin JAR to a file on disk
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
     * Creates a README.txt file with setup instructions
     */
    private fun createReadme() {
        val readmeFile = File(oraxenItemsFolder, "CHARMEDCHARS_README.txt")
        readmeFile.writeText("""
CharmedChars - Oraxen Integration
======================================

This folder contains CharmedChars custom items for Oraxen.

AUTO-GENERATED by CharmedChars plugin via /oraxensetup command.

Contents:
---------
- charmedchars_blocks.yml  - Item definitions (128 items total)
- ../recipes/charmedchars.yml - Pyrite tool recipes (5 recipes)
- ../pack/textures/charmedchars/ - All texture files

Setup:
------
1. This configuration was automatically generated by CharmedChars
2. Run /oraxen reload all to load the new items
3. Restart your server (recommended)
4. Test with /charblock <player> cyan hello

Items Included:
----------------
- 78 Letter blocks (A-Z × 3 colors: cyan, magenta, yellow)
- 30 Number blocks (0-9 × 3 colors)
- 12 Operator blocks (+, -, ×, ÷ × 3 colors)
- 3 Logo blocks (1 × 3 colors)
- 5 Pyrite items (ingot, pickaxe, axe, shovel, hoe)

Total: 128 items

Item ID Format:
----------------
<color>_<character>

Examples:
- cyan_a
- magenta_5
- yellow_plus
- pyrite_pickaxe

Commands:
---------
/charblock <player> cyan hello  - Give blocks to player
/oraxen reload all              - Reload Oraxen configurations
/oraxensetup force              - Regenerate this configuration

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
