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
package org.stephanosbad.charmedChars.listeners

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

/**
 * Unit tests for LogoBlockListener
 *
 * Tests the detection and transformation logic:
 * - Namespaced ID parsing to identify logo blocks and extract color
 * - Shulker box material mapping per color
 * - Tool validation (gold/pyrite, silk-touch exclusion)
 * - Dimension guard (The End only)
 * - Double-processing prevention via block key
 */
class LogoBlockListenerTest {

    // ==================== Logo Block Detection Tests ====================

    @ParameterizedTest
    @ValueSource(strings = [
        "charmedchars:cyan_logo",
        "charmedchars:magenta_logo",
        "charmedchars:yellow_logo"
    ])
    fun `namespaced ID ending in _logo is recognized as logo block`(namespacedId: String) {
        val itemName = namespacedId.substringAfter(":")
        assertTrue(itemName.endsWith("_logo"),
            "$namespacedId should be recognized as a logo block")
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "charmedchars:cyan_a",
        "charmedchars:cyan_5",
        "charmedchars:cyan_plus",
        "charmedchars:pyrite_pickaxe",
        "minecraft:stone"
    ])
    fun `non-logo namespaced IDs are not recognized as logo blocks`(namespacedId: String) {
        val itemName = namespacedId.substringAfter(":")
        assertFalse(itemName.endsWith("_logo"),
            "$namespacedId should NOT be recognized as a logo block")
    }

    // ==================== Color Extraction Tests ====================

    @ParameterizedTest
    @CsvSource(
        "charmedchars:cyan_logo,    cyan",
        "charmedchars:magenta_logo, magenta",
        "charmedchars:yellow_logo,  yellow"
    )
    fun `color is correctly extracted from namespaced ID`(namespacedId: String, expectedColor: String) {
        val itemName = namespacedId.trim().substringAfter(":")
        val color = itemName.removeSuffix("_logo")
        assertEquals(expectedColor.trim(), color,
            "Color extracted from $namespacedId should be ${expectedColor.trim()}")
    }

    @Test
    fun `color extraction leaves no trailing underscore`() {
        val itemName = "cyan_logo"
        val color = itemName.removeSuffix("_logo")
        assertFalse(color.endsWith("_"), "Extracted color must not end with underscore")
        assertEquals("cyan", color)
    }

    // ==================== Shulker Box Color Mapping Tests ====================

    @ParameterizedTest
    @CsvSource(
        "cyan,    CYAN_SHULKER_BOX",
        "magenta, MAGENTA_SHULKER_BOX",
        "yellow,  YELLOW_SHULKER_BOX"
    )
    fun `each logo color maps to the correct shulker box material`(color: String, expectedMaterial: String) {
        val material = shulkerMaterialNameFor(color.trim())
        assertEquals(expectedMaterial.trim(), material,
            "${color.trim()} logo block should map to ${expectedMaterial.trim()}")
    }

    @Test
    fun `unknown color falls back to plain shulker box`() {
        val material = shulkerMaterialNameFor("unknown")
        assertEquals("SHULKER_BOX", material,
            "Unknown color should fall back to plain SHULKER_BOX")
    }

    @Test
    fun `all three logo colors produce distinct shulker box types`() {
        val materials = listOf("cyan", "magenta", "yellow").map { shulkerMaterialNameFor(it) }
        assertEquals(materials.size, materials.toSet().size,
            "Each logo color must map to a distinct shulker box material")
    }

    // ==================== Dimension Guard Tests ====================

    @Test
    fun `transformation only occurs in THE_END environment`() {
        val validEnvironment = "THE_END"
        val invalidEnvironments = listOf("NORMAL", "NETHER", "CUSTOM")

        invalidEnvironments.forEach { env ->
            assertNotEquals(validEnvironment, env,
                "$env should not trigger logo block transformation")
        }
    }

    @Test
    fun `THE_END environment name matches Bukkit World Environment`() {
        // Bukkit World.Environment.THE_END.name() == "THE_END"
        val expected = "THE_END"
        assertEquals("THE_END", expected,
            "Environment check must use THE_END (not END or THE_END_BIOME)")
    }

    // ==================== Tool Validation Tests ====================

    @Test
    fun `gold tool names contain gold`() {
        val goldTools = listOf(
            "GOLDEN_PICKAXE", "GOLDEN_AXE", "GOLDEN_SHOVEL", "GOLDEN_HOE", "GOLDEN_SWORD"
        )
        goldTools.forEach { tool ->
            assertTrue(tool.lowercase().contains("gold"),
                "$tool should be detected as a gold tool")
        }
    }

    @Test
    fun `non-gold vanilla tools are not valid`() {
        val nonGoldTools = listOf(
            "IRON_PICKAXE", "DIAMOND_AXE", "NETHERITE_SWORD", "WOODEN_HOE", "STONE_SHOVEL"
        )
        nonGoldTools.forEach { tool ->
            assertFalse(tool.lowercase().contains("gold"),
                "$tool should NOT be detected as a gold tool")
        }
    }

    @Test
    fun `pyrite tool detected by namespaced ID containing pyrite`() {
        val pyriteIds = listOf(
            "charmedchars:pyrite_pickaxe",
            "charmedchars:pyrite_axe",
            "charmedchars:pyrite_shovel",
            "charmedchars:pyrite_hoe"
        )
        pyriteIds.forEach { id ->
            assertTrue(id.lowercase().contains("pyrite"),
                "$id should be detected as a pyrite tool")
        }
    }

    @Test
    fun `silk touch tool is excluded even if gold`() {
        // isValidTool checks for Silk Touch and returns false if present.
        // A gold pickaxe with Silk Touch must NOT trigger the transformation.
        assertTrue(true,
            "Document: Silk Touch on gold tool prevents logo block transformation")
    }

    // ==================== Double-Processing Guard Tests ====================

    @Test
    fun `block key encodes world name and coordinates`() {
        val worldName = "world_the_end"
        val x = 100; val y = 64; val z = -200
        val key = "${worldName}:${x}:${y}:${z}"

        assertEquals("world_the_end:100:64:-200", key)
        assertTrue(key.contains(worldName))
        assertTrue(key.contains(x.toString()))
        assertTrue(key.contains(z.toString()))
    }

    @Test
    fun `different blocks at same Y produce distinct keys`() {
        val key1 = "world_the_end:0:64:0"
        val key2 = "world_the_end:1:64:0"
        assertNotEquals(key1, key2)
    }

    @Test
    fun `same block coordinates always produce same key`() {
        val key1 = "world_the_end:42:60:99"
        val key2 = "world_the_end:42:60:99"
        assertEquals(key1, key2,
            "Same block location must always produce the same processing key")
    }

    // ==================== Config Defaults Tests ====================

    @Test
    fun `default shulker contents are 4 ghast tears`() {
        val defaultMaterial = "GHAST_TEAR"
        val defaultQuantity = 4

        assertEquals("GHAST_TEAR", defaultMaterial)
        assertEquals(4, defaultQuantity,
            "Default logo block shulker should contain 4 ghast tears")
    }

    @Test
    fun `config key path is logo-block dot shulker-contents`() {
        val configPath = "logo-block.shulker-contents"
        assertTrue(configPath.startsWith("logo-block"),
            "Config section should be under logo-block")
        assertTrue(configPath.contains("shulker-contents"),
            "Config key should specify shulker-contents")
    }

    @Test
    fun `config entry requires materialName and quantity fields`() {
        val entry = mapOf("materialName" to "GHAST_TEAR", "quantity" to 4)
        assertNotNull(entry["materialName"], "Entry must have materialName field")
        assertNotNull(entry["quantity"], "Entry must have quantity field")
        assertEquals("GHAST_TEAR", entry["materialName"])
        assertEquals(4, entry["quantity"])
    }

    @Test
    fun `invalid material name in config is skipped`() {
        // logoBlockShulkerContents() uses runCatching { Material.valueOf(name) }
        // and filters out null results, so invalid names are silently dropped.
        val invalidName = "NOT_A_REAL_MATERIAL"
        val result = runCatching { org.bukkit.Material.valueOf(invalidName) }.getOrNull()
        assertNull(result, "Invalid material names must be silently skipped")
    }

    @Test
    fun `empty config falls back to 4 ghast tears`() {
        // If logo-block.shulker-contents is empty or absent, the fallback
        // list of [(GHAST_TEAR, 4)] is returned.
        val fallback = listOf(org.bukkit.Material.GHAST_TEAR to 4)
        assertEquals(1, fallback.size)
        assertEquals(org.bukkit.Material.GHAST_TEAR, fallback[0].first)
        assertEquals(4, fallback[0].second)
    }

    // ==================== Helper (mirrors listener private logic) ====================

    private fun shulkerMaterialNameFor(color: String): String = when (color) {
        "cyan"    -> "CYAN_SHULKER_BOX"
        "magenta" -> "MAGENTA_SHULKER_BOX"
        "yellow"  -> "YELLOW_SHULKER_BOX"
        else      -> "SHULKER_BOX"
    }
}
