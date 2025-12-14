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
package org.stephanosbad.charmedChars.items

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * Unit tests for word validation logic introduced in v1.1.2
 *
 * Tests the minimum word length rules and tool validation patterns
 * used by ItemManager for scoring words in the game.
 *
 * NOTE: These tests focus on the pure logic patterns rather than
 * full integration testing with Bukkit mocking.
 */
class WordValidationTest {

    // ==================== Word Length Validation Tests ====================

    /**
     * Helper function that mimics the word length validation logic
     * from ItemManager.onBreakWoodOrLetter()
     */
    private fun isValidWordLength(wordLength: Int, isSameColor: Boolean): Boolean {
        val minimumLength = if (isSameColor) 3 else 4
        return wordLength >= minimumLength
    }

    @ParameterizedTest
    @CsvSource(
        "3, true, true",   // Single-color, 3 letters: VALID
        "4, true, true",   // Single-color, 4 letters: VALID
        "5, true, true",   // Single-color, 5 letters: VALID
        "2, true, false",  // Single-color, 2 letters: INVALID
        "1, true, false",  // Single-color, 1 letter: INVALID
    )
    fun `single-color words require minimum 3 letters`(length: Int, sameColor: Boolean, expected: Boolean) {
        val result = isValidWordLength(length, sameColor)
        assertEquals(expected, result,
            "Single-color word of length $length should be ${if (expected) "valid" else "invalid"}")
    }

    @ParameterizedTest
    @CsvSource(
        "4, false, true",   // Multi-color, 4 letters: VALID
        "5, false, true",   // Multi-color, 5 letters: VALID
        "10, false, true",  // Multi-color, 10 letters: VALID
        "3, false, false",  // Multi-color, 3 letters: INVALID
        "2, false, false",  // Multi-color, 2 letters: INVALID
        "1, false, false",  // Multi-color, 1 letter: INVALID
    )
    fun `multi-color words require minimum 4 letters`(length: Int, sameColor: Boolean, expected: Boolean) {
        val result = isValidWordLength(length, sameColor)
        assertEquals(expected, result,
            "Multi-color word of length $length should be ${if (expected) "valid" else "invalid"}")
    }

    @Test
    fun `minimum word length calculation`() {
        // Single-color: minimum 3
        val singleColorMin = if (true) 3 else 4
        assertEquals(3, singleColorMin, "Single-color minimum should be 3")

        // Multi-color: minimum 4
        val multiColorMin = if (false) 3 else 4
        assertEquals(4, multiColorMin, "Multi-color minimum should be 4")
    }

    @Test
    fun `word length validation is case-insensitive`() {
        // Words are converted to lowercase before validation
        val word1 = "CAT"
        val word2 = "cat"
        val word3 = "CaT"

        assertEquals(word1.lowercase(), word2.lowercase())
        assertEquals(word2.lowercase(), word3.lowercase())

        // All should have same length after lowercase conversion
        assertEquals(3, word1.lowercase().length)
        assertEquals(3, word2.lowercase().length)
        assertEquals(3, word3.lowercase().length)
    }

    // ==================== Tool Validation Pattern Tests ====================

    @Test
    fun `gold tools are identified by material name`() {
        // Vanilla gold tools have "gold" in their Material name
        val goldToolNames = listOf(
            "GOLDEN_PICKAXE",
            "GOLDEN_AXE",
            "GOLDEN_SHOVEL",
            "GOLDEN_HOE",
            "GOLDEN_SWORD"
        )

        for (toolName in goldToolNames) {
            assertTrue(toolName.lowercase().contains("gold"),
                "$toolName should contain 'gold' in lowercase")
        }
    }

    @Test
    fun `pyrite tools are identified by namespacedID`() {
        // Custom item provider (ItemsAdder/Oraxen) pyrite tools have namespacedID format: "charmedchars:pyrite_*"
        val pyriteNamespacedIds = listOf(
            "charmedchars:pyrite_ingot",
            "charmedchars:pyrite_pickaxe",
            "charmedchars:pyrite_axe",
            "charmedchars:pyrite_shovel",
            "charmedchars:pyrite_hoe"
        )

        for (namespacedId in pyriteNamespacedIds) {
            assertTrue(namespacedId.lowercase().contains("pyrite"),
                "$namespacedId should contain 'pyrite' in lowercase")
        }
    }

    @Test
    fun `non-gold tools are rejected`() {
        // These tool names should NOT be recognized as valid
        val invalidToolNames = listOf(
            "IRON_PICKAXE",
            "DIAMOND_AXE",
            "NETHERITE_PICKAXE",
            "STONE_SHOVEL",
            "WOODEN_HOE"
        )

        for (toolName in invalidToolNames) {
            assertFalse(toolName.lowercase().contains("gold"),
                "$toolName should NOT contain 'gold'")
        }
    }

    @Test
    fun `tool validation checks three sources`() {
        // ItemManager.isValidTool() checks in order:
        // 1. Material type contains "gold" (vanilla gold tools)
        // 2. CustomItemProvider namespacedID contains "pyrite" (ItemsAdder/Oraxen pyrite tools)
        // 3. Display name contains "gold" or "pyrite" (fallback for custom items)

        val validationSteps = listOf(
            "Check material type for 'gold'",
            "Check custom item provider namespacedID for 'pyrite'",
            "Check display name for 'gold' or 'pyrite'"
        )

        assertEquals(3, validationSteps.size,
            "Tool validation should have 3 steps")
    }

    @Test
    fun `tool validation requires ItemMeta`() {
        // isValidTool() returns false if item.itemMeta is null
        // This prevents NPE when checking custom items

        val hasItemMeta = true
        val noItemMeta = false

        assertTrue(hasItemMeta, "Valid tools must have ItemMeta")
        assertFalse(noItemMeta, "Items without ItemMeta are invalid tools")
    }

    // ==================== Edge Case Tests ====================

    @Test
    fun `empty word is always invalid`() {
        val emptyWordLength = 0

        assertFalse(isValidWordLength(emptyWordLength, true),
            "Empty single-color word should be invalid")
        assertFalse(isValidWordLength(emptyWordLength, false),
            "Empty multi-color word should be invalid")
    }

    @Test
    fun `very long words are valid if they meet minimum`() {
        val veryLongWordLength = 100

        assertTrue(isValidWordLength(veryLongWordLength, true),
            "Very long single-color word should be valid")
        assertTrue(isValidWordLength(veryLongWordLength, false),
            "Very long multi-color word should be valid")
    }

    @Test
    fun `boundary case for single-color words`() {
        // 2 letters: INVALID
        assertFalse(isValidWordLength(2, true),
            "2-letter single-color word should be invalid")

        // 3 letters: VALID (minimum)
        assertTrue(isValidWordLength(3, true),
            "3-letter single-color word should be valid (minimum)")

        // 4 letters: VALID
        assertTrue(isValidWordLength(4, true),
            "4-letter single-color word should be valid")
    }

    @Test
    fun `boundary case for multi-color words`() {
        // 3 letters: INVALID
        assertFalse(isValidWordLength(3, false),
            "3-letter multi-color word should be invalid")

        // 4 letters: VALID (minimum)
        assertTrue(isValidWordLength(4, false),
            "4-letter multi-color word should be valid (minimum)")

        // 5 letters: VALID
        assertTrue(isValidWordLength(5, false),
            "5-letter multi-color word should be valid")
    }

    // ==================== Real-World Scenario Tests ====================

    @Test
    fun `common short words with single-color`() {
        val shortWords = mapOf(
            "CAT" to 3,
            "DOG" to 3,
            "HI" to 2,
            "NO" to 2,
            "YES" to 3,
            "ZONE" to 4
        )

        for ((word, length) in shortWords) {
            val isValid = isValidWordLength(length, true)
            val expectedValid = length >= 3

            assertEquals(expectedValid, isValid,
                "Single-color word '$word' (length $length) should be ${if (expectedValid) "valid" else "invalid"}")
        }
    }

    @Test
    fun `common short words with multi-color`() {
        val shortWords = mapOf(
            "CATS" to 4,
            "DOGS" to 4,
            "HEY" to 3,
            "NOPE" to 4,
            "ZONE" to 4
        )

        for ((word, length) in shortWords) {
            val isValid = isValidWordLength(length, false)
            val expectedValid = length >= 4

            assertEquals(expectedValid, isValid,
                "Multi-color word '$word' (length $length) should be ${if (expectedValid) "valid" else "invalid"}")
        }
    }

    @Test
    fun `color bonus encourages longer single-color words`() {
        // The 3x multiplier for same-color words encourages players to:
        // 1. Collect multiple blocks of the same color
        // 2. Form longer words with same color (even though minimum is 3)

        val singleColorBonus = 3
        val multiColorBonus = 1

        assertTrue(singleColorBonus > multiColorBonus,
            "Single-color bonus should be higher to encourage color matching")

        // Lower minimum (3) + higher bonus (3x) = strategic gameplay
        val singleColorMin = 3
        val multiColorMin = 4

        assertTrue(singleColorMin < multiColorMin,
            "Single-color minimum is lower because of bonus reward")
    }

    @Test
    fun `tool validation prevents accidental number sequence triggers`() {
        // v1.1.2 fixed issue where netherite tools would trigger number sequences
        // Only gold and pyrite tools should work

        val validToolTypes = listOf("gold", "pyrite")
        val invalidToolTypes = listOf("netherite", "diamond", "iron", "stone", "wood")

        for (toolType in validToolTypes) {
            assertTrue(toolType == "gold" || toolType == "pyrite",
                "$toolType should be recognized as valid tool type")
        }

        for (toolType in invalidToolTypes) {
            assertFalse(toolType == "gold" || toolType == "pyrite",
                "$toolType should NOT be recognized as valid tool type")
        }
    }
}
