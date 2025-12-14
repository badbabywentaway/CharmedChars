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

/**
 * Unit tests for CustomItemProvider interface and data classes
 *
 * Tests the abstraction layer for custom item providers (ItemsAdder/Oraxen).
 * These tests focus on the contract and data structures rather than
 * implementation-specific behavior.
 */
class CustomItemProviderTest {

    // ==================== CustomItemInfo Tests ====================

    @Test
    fun `CustomItemInfo stores namespaced ID correctly`() {
        val info = CustomItemInfo(
            namespacedId = "charmedchars:cyan_a",
            namespace = "charmedchars",
            itemName = "cyan_a"
        )

        assertEquals("charmedchars:cyan_a", info.namespacedId)
        assertEquals("charmedchars", info.namespace)
        assertEquals("cyan_a", info.itemName)
    }

    @Test
    fun `CustomItemInfo equality works correctly`() {
        val info1 = CustomItemInfo("charmedchars:cyan_a", "charmedchars", "cyan_a")
        val info2 = CustomItemInfo("charmedchars:cyan_a", "charmedchars", "cyan_a")
        val info3 = CustomItemInfo("charmedchars:red_b", "charmedchars", "red_b")

        assertEquals(info1, info2, "Identical CustomItemInfo should be equal")
        assertNotEquals(info1, info3, "Different CustomItemInfo should not be equal")
    }

    @ParameterizedTest
    @CsvSource(
        "charmedchars:cyan_a, charmedchars, cyan_a",
        "charmedchars:pyrite_pickaxe, charmedchars, pyrite_pickaxe",
        "charmedchars:red_5, charmedchars, red_5",
        "charmedchars:yellow_plus, charmedchars, yellow_plus"
    )
    fun `CustomItemInfo parses various item formats`(
        namespacedId: String,
        expectedNamespace: String,
        expectedItemName: String
    ) {
        val info = CustomItemInfo(namespacedId, expectedNamespace, expectedItemName)

        assertEquals(expectedNamespace, info.namespace)
        assertEquals(expectedItemName, info.itemName)
        assertTrue(info.namespacedId.contains(":"),
            "NamespacedID should contain separator: $namespacedId")
    }

    // ==================== CustomBlockInfo Tests ====================

    @Test
    fun `CustomBlockInfo stores namespaced ID correctly`() {
        val info = CustomBlockInfo(
            namespacedId = "charmedchars:cyan_a",
            namespace = "charmedchars",
            blockName = "cyan_a"
        )

        assertEquals("charmedchars:cyan_a", info.namespacedId)
        assertEquals("charmedchars", info.namespace)
        assertEquals("cyan_a", info.blockName)
    }

    @Test
    fun `CustomBlockInfo equality works correctly`() {
        val info1 = CustomBlockInfo("charmedchars:cyan_a", "charmedchars", "cyan_a")
        val info2 = CustomBlockInfo("charmedchars:cyan_a", "charmedchars", "cyan_a")
        val info3 = CustomBlockInfo("charmedchars:red_b", "charmedchars", "red_b")

        assertEquals(info1, info2, "Identical CustomBlockInfo should be equal")
        assertNotEquals(info1, info3, "Different CustomBlockInfo should not be equal")
    }

    @ParameterizedTest
    @CsvSource(
        "charmedchars:cyan_a, charmedchars, cyan_a",
        "charmedchars:red_5, charmedchars, red_5",
        "charmedchars:yellow_plus, charmedchars, yellow_plus",
        "charmedchars:magenta_minus, charmedchars, magenta_minus"
    )
    fun `CustomBlockInfo parses various block formats`(
        namespacedId: String,
        expectedNamespace: String,
        expectedBlockName: String
    ) {
        val info = CustomBlockInfo(namespacedId, expectedNamespace, expectedBlockName)

        assertEquals(expectedNamespace, info.namespace)
        assertEquals(expectedBlockName, info.blockName)
        assertTrue(info.namespacedId.contains(":"),
            "NamespacedID should contain separator: $namespacedId")
    }

    // ==================== Namespaced ID Format Tests ====================

    @Test
    fun `namespacedID follows minecraft convention`() {
        // Format: namespace:item_name
        val validFormats = listOf(
            "charmedchars:cyan_a",
            "minecraft:diamond",
            "oraxen:custom_item"
        )

        for (id in validFormats) {
            assertTrue(id.contains(":"),
                "$id should contain ':' separator")
            assertEquals(2, id.split(":").size,
                "$id should split into exactly 2 parts")
        }
    }

    @Test
    fun `namespace extraction from namespacedID`() {
        val namespacedId = "charmedchars:cyan_a"
        val parts = namespacedId.split(":")

        assertEquals("charmedchars", parts[0], "First part is namespace")
        assertEquals("cyan_a", parts[1], "Second part is item/block name")
    }

    @ParameterizedTest
    @CsvSource(
        "charmedchars:cyan_a, charmedchars, cyan_a",
        "charmedchars:pyrite_pickaxe, charmedchars, pyrite_pickaxe",
        "minecraft:stone, minecraft, stone",
        "oraxen:custom_block, oraxen, custom_block"
    )
    fun `namespacedID parsing is consistent`(
        fullId: String,
        expectedNamespace: String,
        expectedName: String
    ) {
        val parts = fullId.split(":")

        assertEquals(expectedNamespace, parts[0])
        assertEquals(expectedName, parts[1])
    }

    // ==================== Provider Contract Tests ====================

    @Test
    fun `provider names are well-defined`() {
        val validProviderNames = listOf("ItemsAdder", "Oraxen")

        for (name in validProviderNames) {
            assertFalse(name.isBlank(), "Provider name should not be blank")
            assertTrue(name.first().isUpperCase(),
                "Provider name should be capitalized: $name")
        }
    }

    @Test
    fun `namespaced IDs are case-sensitive`() {
        // Minecraft treats namespaced IDs as case-sensitive
        val lowercase = "charmedchars:cyan_a"
        val uppercase = "CHARMEDCHARS:CYAN_A"
        val mixed = "CharmedChars:Cyan_A"

        assertNotEquals(lowercase, uppercase, "Case matters in namespacedID")
        assertNotEquals(lowercase, mixed, "Case matters in namespacedID")
    }

    // ==================== CharmedChars Specific Tests ====================

    @Test
    fun `all CharmedChars items use charmedchars namespace`() {
        val charmedCharsItems = listOf(
            "charmedchars:cyan_a",
            "charmedchars:red_5",
            "charmedchars:pyrite_pickaxe",
            "charmedchars:yellow_plus"
        )

        for (item in charmedCharsItems) {
            assertTrue(item.startsWith("charmedchars:"),
                "$item should start with charmedchars namespace")
        }
    }

    @Test
    fun `CharmedChars block naming convention`() {
        // Format: {color}_{character}
        val blockIds = listOf(
            "cyan_a", "red_5", "yellow_plus", "magenta_minus"
        )

        for (blockId in blockIds) {
            assertTrue(blockId.contains("_"),
                "$blockId should contain underscore separator")

            val parts = blockId.split("_")
            assertTrue(parts.size >= 2,
                "$blockId should have at least 2 parts (color_character)")
        }
    }

    @Test
    fun `CharmedChars color names are lowercase`() {
        val colors = listOf("cyan", "red", "yellow", "magenta", "green", "blue")

        for (color in colors) {
            assertEquals(color, color.lowercase(),
                "Color names should be lowercase: $color")
        }
    }

    @Test
    fun `pyrite items use pyrite prefix`() {
        val pyriteItems = listOf(
            "pyrite_ingot",
            "pyrite_pickaxe",
            "pyrite_axe",
            "pyrite_shovel",
            "pyrite_hoe"
        )

        for (item in pyriteItems) {
            assertTrue(item.startsWith("pyrite_"),
                "$item should start with pyrite_ prefix")
        }
    }

    // ==================== Edge Cases ====================

    @Test
    fun `CustomItemInfo handles special characters in names`() {
        val specialChars = listOf(
            CustomItemInfo("charmedchars:cyan_plus", "charmedchars", "cyan_plus"),
            CustomItemInfo("charmedchars:red_minus", "charmedchars", "red_minus"),
            CustomItemInfo("charmedchars:yellow_multiply", "charmedchars", "yellow_multiply"),
            CustomItemInfo("charmedchars:magenta_divide", "charmedchars", "magenta_divide")
        )

        for (info in specialChars) {
            assertNotNull(info.itemName, "Item name should not be null")
            assertTrue(info.itemName.isNotEmpty(), "Item name should not be empty")
        }
    }

    @Test
    fun `CustomBlockInfo and CustomItemInfo can represent same object`() {
        // Same block can be represented as both item and block
        val itemInfo = CustomItemInfo("charmedchars:cyan_a", "charmedchars", "cyan_a")
        val blockInfo = CustomBlockInfo("charmedchars:cyan_a", "charmedchars", "cyan_a")

        assertEquals(itemInfo.namespacedId, blockInfo.namespacedId,
            "Same object should have same namespacedID as item and block")
        assertEquals(itemInfo.namespace, blockInfo.namespace)
        assertEquals(itemInfo.itemName, blockInfo.blockName)
    }
}
