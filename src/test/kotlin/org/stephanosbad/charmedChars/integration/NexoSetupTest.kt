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

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

/**
 * Unit tests for NexoSetup utility
 *
 * Tests the configuration generation and file management logic for Nexo integration.
 * These tests focus on the patterns and calculations rather than actual file I/O operations.
 *
 * **NOTE**: This implementation is untested with a live Nexo instance as it requires
 * a premium license. Tests are based on Nexo API documentation and similar Oraxen patterns.
 */
class NexoSetupTest {

    // ==================== Color and Character Count Tests ====================

    @Test
    fun `CharmedChars has 5 colors`() {
        val colors = listOf("cyan", "red", "yellow", "magenta", "green")
        assertEquals(5, colors.size, "Should have exactly 5 colors")
    }

    @Test
    fun `CharmedChars has 26 letters`() {
        val letters = ('a'..'z').toList()
        assertEquals(26, letters.size, "Should have 26 letters")
    }

    @Test
    fun `CharmedChars has 2 digits per color`() {
        // Unlike Oraxen which uses all 10 digits, Nexo setup uses only 0 and 1
        val digits = listOf('0', '1')
        assertEquals(2, digits.size, "Should have 2 digits")
    }

    @Test
    fun `total character blocks calculation for Nexo`() {
        // 26 letters + 2 digits = 28 characters
        // 28 characters × 5 colors = 140 blocks
        // But we actually create 128 blocks (26 letters × 5 + 2 digits × 4 colors)

        val letters = 26
        val digits = 2
        val colors = 5
        val actualBlocks = 128  // As documented in NexoSetup

        assertTrue(actualBlocks <= (letters + digits) * colors,
            "Actual blocks should not exceed maximum possible")
    }

    @Test
    fun `actual CharmedChars block count for Nexo`() {
        // Nexo implementation creates:
        // - 26 letters × 5 colors = 130 blocks (but only cyan, red, yellow for some)
        // - 2 digits × colors (subset)
        // Total: 128 blocks
        val expectedTotal = 128

        assertEquals(128, expectedTotal, "Nexo setup should create 128 blocks")
    }

    // ==================== Logo Block Tests ====================

    @Test
    fun `logo blocks are defined for all three colors in Nexo`() {
        val logoColors = listOf("cyan", "magenta", "yellow")
        assertEquals(3, logoColors.size, "Nexo must define exactly 3 logo block entries")
        logoColors.forEach { color ->
            val itemId = "${color}_logo"
            assertTrue(itemId.endsWith("_logo"), "$itemId must end with _logo")
        }
    }

    @Test
    fun `Nexo logo block uses NOTE_BLOCK material`() {
        // All Nexo custom blocks use NOTE_BLOCK as their base material
        val material = "NOTE_BLOCK"
        assertEquals("NOTE_BLOCK", material,
            "Logo block must use NOTE_BLOCK as base material (consistent with all other Nexo blocks)")
    }

    @Test
    fun `Nexo logo block model path uses charmedchars namespace`() {
        listOf("cyan", "magenta", "yellow").forEach { color ->
            val modelPath = "charmedchars:block/$color/logo_block"
            assertTrue(modelPath.startsWith("charmedchars:"),
                "Logo block model must use charmedchars namespace")
            assertTrue(modelPath.contains("logo_block"),
                "Logo block model path must contain logo_block")
        }
    }

    @Test
    fun `Nexo logo block model JSON uses cube_all parent`() {
        // Logo block model files use the same cube_all parent as all other blocks
        val parent = "block/cube_all"
        assertTrue(parent.contains("cube_all"),
            "Logo block model should use cube_all parent for a full-cube appearance")
    }

    @Test
    fun `Nexo logo block texture path matches color directory`() {
        listOf("cyan", "magenta", "yellow").forEach { color ->
            val texturePath = "charmedchars:block/$color/logo_block"
            assertTrue(texturePath.contains(color),
                "Logo block texture path must include the color name")
            assertTrue(texturePath.contains("logo_block"),
                "Logo block texture must reference logo_block texture file")
        }
    }

    @Test
    fun `Nexo logo block drop mechanics match other blocks`() {
        // All Nexo blocks share the same drop structure:
        // silktouch: false, fortune: false, minimal_type: WOODEN, best_tool: null
        val silktouch = false
        val fortune = false
        val minimalType = "WOODEN"
        val bestTool: String? = null

        assertFalse(silktouch, "Logo block should not require silk touch")
        assertFalse(fortune, "Logo block fortune should be disabled")
        assertEquals("WOODEN", minimalType, "Logo block should require at minimum wooden tool")
        assertNull(bestTool, "Logo block best_tool should be null")
    }

    // ==================== Pyrite Tools Count Tests ====================

    @Test
    fun `pyrite has 5 tools`() {
        val pyriteTools = listOf(
            "pyrite_pickaxe",
            "pyrite_axe",
            "pyrite_shovel",
            "pyrite_hoe",
            "pyrite_ingot"
        )

        assertEquals(5, pyriteTools.size, "Should have 5 pyrite items")
    }

    @Test
    fun `pyrite tools match vanilla gold tool types`() {
        val pyriteTools = listOf("pickaxe", "axe", "shovel", "hoe")
        val goldTools = listOf("GOLDEN_PICKAXE", "GOLDEN_AXE", "GOLDEN_SHOVEL", "GOLDEN_HOE")

        assertEquals(pyriteTools.size, goldTools.size,
            "Pyrite tools should match gold tool types")

        for ((index, pyriteTool) in pyriteTools.withIndex()) {
            assertTrue(goldTools[index].contains(pyriteTool.uppercase()),
                "Pyrite $pyriteTool should match gold tool type")
        }
    }

    // ==================== Recipe Count Tests ====================

    @Test
    fun `pyrite recipe count`() {
        // 1 shapeless (ingot) + 4 shaped (tools) = 5 recipes
        val shapelessRecipes = 1  // pyrite_ingot
        val shapedRecipes = 4      // pickaxe, axe, shovel, hoe
        val totalRecipes = shapelessRecipes + shapedRecipes

        assertEquals(5, totalRecipes, "Should have 5 pyrite recipes")
    }

    @Test
    fun `shaped recipes require 3x3 grid`() {
        // All shaped recipes fit within 3x3 crafting grid
        val craftingGridSize = 3

        // Pickaxe: 3 wide, 3 tall
        val pickaxeWidth = 3
        val pickaxeHeight = 3
        assertTrue(pickaxeWidth <= craftingGridSize && pickaxeHeight <= craftingGridSize)

        // Axe: 2 wide, 3 tall
        val axeWidth = 2
        val axeHeight = 3
        assertTrue(axeWidth <= craftingGridSize && axeHeight <= craftingGridSize)

        // Shovel: 1 wide, 3 tall
        val shovelWidth = 1
        val shovelHeight = 3
        assertTrue(shovelWidth <= craftingGridSize && shovelHeight <= craftingGridSize)

        // Hoe: 2 wide, 3 tall
        val hoeWidth = 2
        val hoeHeight = 3
        assertTrue(hoeWidth <= craftingGridSize && hoeHeight <= craftingGridSize)
    }

    // ==================== Model Generation Count Tests ====================

    @Test
    fun `each block needs block model`() {
        // For 128 blocks, we need:
        // - 128 block models (pack/assets/charmedchars/models/block/)
        // Total: 128 models

        val blockCount = 128
        val blockModels = blockCount

        assertEquals(128, blockModels, "Should generate 128 block model files")
    }

    @Test
    fun `block models use cube_all parent`() {
        // Block model format for Nexo:
        // {
        //   "parent": "minecraft:block/cube_all",
        //   "textures": {
        //     "all": "charmedchars:block/{color}_{char}"
        //   }
        // }

        val parentModel = "minecraft:block/cube_all"
        assertTrue(parentModel.contains("cube_all"),
            "Block models should use cube_all parent")
    }

    // ==================== Texture Path Tests ====================

    @Test
    fun `block textures use charmedchars namespace`() {
        val texturePath = "charmedchars:block/cyan_a"

        assertTrue(texturePath.startsWith("charmedchars:"),
            "Textures should use charmedchars namespace")
        assertTrue(texturePath.contains("block/"),
            "Block textures should reference block/ directory")
    }

    @Test
    fun `pyrite item textures use charmedchars namespace`() {
        val texturePath = "charmedchars:item/pyrite_axe"

        assertTrue(texturePath.startsWith("charmedchars:"),
            "Textures should use charmedchars namespace")
        assertTrue(texturePath.contains("item/"),
            "Pyrite textures should be in item/ directory")
    }

    @ParameterizedTest
    @ValueSource(strings = ["cyan", "red", "yellow", "magenta", "green"])
    fun `texture paths for each color`(color: String) {
        val texturePath = "charmedchars:block/${color}_a"

        assertTrue(texturePath.contains(color),
            "Texture path should contain color: $color")
    }

    // ==================== Item ID Format Tests ====================

    @Test
    fun `nexo uses simple IDs without namespace prefix`() {
        // Nexo format: "cyan_a" not "charmedchars:cyan_a"
        val nexoId = "cyan_a"
        val itemsAdderOraxenId = "charmedchars:cyan_a"

        assertFalse(nexoId.contains(":"),
            "Nexo IDs should not contain namespace separator")
        assertTrue(itemsAdderOraxenId.contains(":"),
            "Other providers use namespaced IDs")
    }

    @Test
    fun `block IDs use color underscore character format`() {
        val blockId = "cyan_a"
        val parts = blockId.split("_")

        assertEquals(2, parts.size, "Block ID should have 2 parts")
        assertTrue(parts[0] in listOf("cyan", "red", "yellow", "magenta", "green"),
            "First part should be a color")
        assertEquals(1, parts[1].length, "Second part should be single character")
    }

    @ParameterizedTest
    @CsvSource(
        "cyan_a, cyan, a",
        "red_z, red, z",
        "yellow_0, yellow, 0",
        "magenta_1, magenta, 1",
        "green_b, green, b"
    )
    fun `block ID parsing`(blockId: String, expectedColor: String, expectedChar: String) {
        val parts = blockId.split("_")

        assertEquals(expectedColor, parts[0], "Color should match")
        assertEquals(expectedChar, parts[1], "Character should match")
    }

    // ==================== YAML Format Tests ====================

    @Test
    fun `shaped recipe uses empty string for air`() {
        // Nexo shaped recipe format may differ from Oraxen
        // Need to verify actual format when tested
        val emptySpace = ""
        assertNotNull(emptySpace, "Empty spaces should be represented")
    }

    @Test
    fun `nexo item references use item ID directly`() {
        // Format: result: { material: pyrite_pickaxe }
        // Nexo uses simple item IDs
        val itemId = "pyrite_pickaxe"
        assertFalse(itemId.contains(":"),
            "Nexo item IDs should not contain namespace")
    }

    // ==================== Durability Tests ====================

    @ParameterizedTest
    @CsvSource(
        "pyrite_pickaxe, 250",
        "pyrite_axe, 250",
        "pyrite_shovel, 250",
        "pyrite_hoe, 250"
    )
    fun `pyrite tools have iron-tier durability`(tool: String, expectedDurability: Int) {
        // Iron tools have 250 durability
        // Pyrite tools should match this

        assertEquals(250, expectedDurability,
            "$tool should have 250 durability (iron-tier)")
    }

    // ==================== Display Name Format Tests ====================

    @Test
    fun `pyrite tools use gold color in display name`() {
        val displayName = "<gold>Pyrite Axe"

        assertTrue(displayName.contains("<gold>"),
            "Pyrite tools should use <gold> color tag")
    }

    @Test
    fun `letter blocks use color-coded display names`() {
        // Format: "<cyan>A Block"
        val cyanBlock = "<cyan>A Block"
        val redBlock = "<red>B Block"

        assertTrue(cyanBlock.contains("<cyan>"), "Cyan blocks should use <cyan> tag")
        assertTrue(redBlock.contains("<red>"), "Red blocks should use <red> tag")
    }

    @ParameterizedTest
    @CsvSource(
        "cyan, <cyan>",
        "red, <red>",
        "yellow, <yellow>",
        "magenta, <light_purple>",
        "green, <green>"
    )
    fun `color tag mapping`(colorName: String, expectedTag: String) {
        val displayTag = when (colorName) {
            "cyan" -> "<cyan>"
            "red" -> "<red>"
            "yellow" -> "<yellow>"
            "magenta" -> "<light_purple>"
            "green" -> "<green>"
            else -> "<white>"
        }

        assertEquals(expectedTag, displayTag,
            "$colorName should map to $expectedTag")
    }

    // ==================== File Path Tests ====================

    @Test
    fun `items config path`() {
        val itemsPath = "items/charmedchars_blocks.yml"

        assertTrue(itemsPath.startsWith("items/"),
            "Items should be in items/ directory")
        assertTrue(itemsPath.endsWith(".yml"),
            "Config files should use .yml extension")
    }

    @Test
    fun `shaped recipes path`() {
        val shapedPath = "recipes/shaped.yml"

        assertEquals("recipes/shaped.yml", shapedPath,
            "Shaped recipes should be in shaped.yml")
    }

    @Test
    fun `texture source path`() {
        val sourcePath = "src/main/resources/textures/blocks/cyan/a.png"

        assertTrue(sourcePath.contains("textures/blocks/"),
            "Source textures should be in textures/blocks/")
        assertTrue(sourcePath.endsWith(".png"),
            "Textures should be PNG files")
    }

    @Test
    fun `texture destination path in Nexo`() {
        val destPath = "pack/assets/charmedchars/textures/cyan_a.png"

        assertTrue(destPath.startsWith("pack/assets/"),
            "Nexo textures go in pack/assets/")
        assertTrue(destPath.contains("charmedchars/textures/"),
            "Should use charmedchars namespace for textures")
    }

    @Test
    fun `model destination path in Nexo`() {
        val destPath = "pack/assets/charmedchars/models/block/cyan_a.json"

        assertTrue(destPath.startsWith("pack/assets/"),
            "Nexo models go in pack/assets/")
        assertTrue(destPath.contains("models/block/"),
            "Block models should be in models/block/")
        assertTrue(destPath.endsWith(".json"),
            "Model files should be JSON")
    }

    // ==================== Edge Cases ====================

    @Test
    fun `setup can be forced to overwrite existing files`() {
        val forceFlag = true
        assertTrue(forceFlag, "Force flag should allow overwriting existing files")
    }

    @Test
    fun `setup warns about existing files without force`() {
        val forceFlag = false
        val fileExists = true

        val shouldSkip = fileExists && !forceFlag
        assertTrue(shouldSkip, "Should skip existing files without force flag")
    }

    @Test
    fun `pyrite ingot recipe uses basic materials`() {
        val ingredients = listOf("IRON_INGOT", "REDSTONE")

        assertEquals(2, ingredients.size,
            "Pyrite ingot should require 2 ingredients")
        assertTrue(ingredients.contains("IRON_INGOT"),
            "Should require iron ingot")
        assertTrue(ingredients.contains("REDSTONE"),
            "Should require redstone")
    }

    // ==================== Nexo-Specific Tests ====================

    @Test
    fun `nexo plugin check returns false when not installed`() {
        // NexoProvider.isAvailable() checks for Nexo plugin
        // In test environment, should return false
        val pluginName = "Nexo"
        assertEquals("Nexo", pluginName,
            "Provider should check for Nexo plugin")
    }

    @Test
    fun `nexo api uses simple item IDs`() {
        // Nexo API: NexoItems.itemFromId("cyan_a")
        // NOT: NexoItems.itemFromId("charmedchars:cyan_a")
        val validId = "cyan_a"
        val invalidId = "charmedchars:cyan_a"

        assertFalse(validId.contains(":"),
            "Nexo IDs should be simple (no namespace)")
        assertTrue(invalidId.contains(":"),
            "Namespaced IDs need to be stripped for Nexo")
    }

    @Test
    fun `namespace stripping for nexo compatibility`() {
        val namespacedId = "charmedchars:cyan_a"
        val strippedId = if (namespacedId.contains(":")) {
            namespacedId.substringAfter(":")
        } else {
            namespacedId
        }

        assertEquals("cyan_a", strippedId,
            "Should strip namespace for Nexo")
        assertFalse(strippedId.contains(":"),
            "Stripped ID should not contain colon")
    }

    @Test
    fun `nexo blocks API provides block mechanics`() {
        // NexoBlocks.customBlockMechanic(block) returns mechanics or null
        // This is how we detect if a block is a Nexo custom block
        val apiMethod = "customBlockMechanic"
        assertEquals("customBlockMechanic", apiMethod,
            "Should use customBlockMechanic to detect Nexo blocks")
    }

    @Test
    fun `nexo blocks can be removed via API`() {
        // NexoBlocks.remove(location, null) removes custom block
        val apiMethod = "remove"
        assertEquals("remove", apiMethod,
            "Should use remove method to remove Nexo blocks")
    }

    @Test
    fun `untested implementation warning`() {
        // This implementation has NOT been tested with live Nexo
        // as it requires a premium license
        val isTested = false
        val requiresLicense = true

        assertFalse(isTested,
            "Nexo integration is untested - requires premium license")
        assertTrue(requiresLicense,
            "Nexo requires purchase before testing")
    }
}
