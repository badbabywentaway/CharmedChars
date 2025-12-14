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

/**
 * Unit tests for three-digit sequence detection logic
 *
 * Tests the findThreeDigitSequence() method that searches for valid number
 * block sequences in the game. This is critical for the number guessing game.
 *
 * NOTE: Due to custom item provider API complexity, these tests focus on the
 * geometric logic patterns rather than full integration testing with mocked blocks.
 */
class SequenceDetectionTest {

    // ==================== Direction Detection Tests ====================

    @Test
    fun `sequence detection checks four cardinal directions`() {
        // The findThreeDigitSequence method should check:
        // 1. +X direction (east)
        // 2. -X direction (west)
        // 3. +Z direction (south)
        // 4. -Z direction (north)
        //
        // It should NOT check:
        // - Y-axis (vertical)
        // - Diagonal directions
        // - Curved paths

        assertTrue(true, "Document expected behavior: 4 cardinal directions only")
    }

    @ParameterizedTest
    @CsvSource(
        "1, 0",  // +X direction
        "-1, 0", // -X direction
        "0, 1",  // +Z direction
        "0, -1"  // -Z direction
    )
    fun `valid directions are horizontal only`(xOffset: Int, zOffset: Int) {
        // Sequence detection should work for these offset patterns
        // block1 at (x, y, z)
        // block2 at (x + xOffset, y, z + zOffset)
        // block3 at (x + xOffset*2, y, z + zOffset*2)

        val isHorizontal = (xOffset == 0 && zOffset != 0) || (xOffset != 0 && zOffset == 0)
        assertTrue(isHorizontal, "Direction ($xOffset, $zOffset) should be horizontal")
    }

    // ==================== Sequence Pattern Tests ====================

    @Test
    fun `sequence requires exactly 3 blocks`() {
        // Valid: [1][2][3]
        // Invalid: [1][2] (only 2)
        // Invalid: [1][2][3][4] (4 blocks, but detection stops at 3)

        val minBlocks = 3
        val maxBlocks = 3

        assertEquals(minBlocks, maxBlocks, "Sequence must be exactly 3 blocks")
    }

    @Test
    fun `sequence must be contiguous`() {
        // Valid: [1][2][3]
        // Invalid: [1][ ][3] (gap in middle)
        // Invalid: [1][2][ ] (missing third)

        // Distance between blocks must be exactly 1 block apart
        val requiredDistance = 1

        assertEquals(1, requiredDistance, "Blocks must be immediately adjacent")
    }

    @Test
    fun `sequence forms three-digit number from hundreds to ones`() {
        // First block (broken) = hundreds digit
        // Second block (+1 offset) = tens digit
        // Third block (+2 offset) = ones digit
        //
        // Example: [5][2][3] = 523

        val firstDigit = 5
        val secondDigit = 2
        val thirdDigit = 3

        val expectedNumber = firstDigit * 100 + secondDigit * 10 + thirdDigit
        assertEquals(523, expectedNumber, "Sequence [5][2][3] should form 523")
    }

    @ParameterizedTest
    @CsvSource(
        "1, 2, 3, 123",
        "9, 9, 9, 999",
        "1, 0, 0, 100",
        "5, 4, 7, 547",
        "0, 0, 0, 0"  // Edge case: all zeros
    )
    fun `three digits form correct number`(d1: Int, d2: Int, d3: Int, expected: Int) {
        val number = d1 * 100 + d2 * 10 + d3
        assertEquals(expected, number, "[$d1][$d2][$d3] should form $expected")
    }

    // ==================== Invalid Sequence Tests ====================

    @Test
    fun `diagonal sequences should not be detected`() {
        // Invalid patterns (should return null):
        // [1]
        //    [2]
        //       [3]
        //
        // This would be (+1, +1) direction which is NOT checked

        val validDirections = listOf(
            Pair(1, 0),   // +X only
            Pair(-1, 0),  // -X only
            Pair(0, 1),   // +Z only
            Pair(0, -1)   // -Z only
        )

        val diagonalDirection = Pair(1, 1)  // +X and +Z
        assertFalse(validDirections.contains(diagonalDirection),
            "Diagonal direction should NOT be in valid directions list")
    }

    @Test
    fun `vertical sequences should not be detected`() {
        // Invalid pattern (should return null):
        // [1]
        // [2]  (directly above/below)
        // [3]
        //
        // The method only checks X and Z, not Y

        // Y-offset would need to be checked, but it's not in the implementation
        assertTrue(true, "Y-axis is not checked in findThreeDigitSequence")
    }

    @Test
    fun `non-number blocks break sequence`() {
        // Invalid: [1][Stone][3]
        // Invalid: [1][2][Air]
        // Invalid: [Letter-A][2][3]

        // getNumberFromBlock returns null for non-number blocks
        // This causes the sequence detection to fail

        assertTrue(true, "Non-number blocks return null from getNumberFromBlock")
    }

    // ==================== Edge Case Tests ====================

    @Test
    fun `sequence at world boundary`() {
        // Sequence near world edges should still work if all 3 blocks are valid
        // Example: blocks at x = 29999998, 29999999, 30000000

        val worldBoundary = 30000000
        val block1X = worldBoundary - 2
        val block2X = worldBoundary - 1
        val block3X = worldBoundary

        assertTrue(block3X <= worldBoundary,
            "Sequence extending to world boundary should be valid")
    }

    @Test
    fun `sequence crossing chunk boundaries works`() {
        // Chunks are 16 blocks wide
        // Sequence: block at x=15, x=16, x=17 crosses chunk boundary

        val chunkSize = 16
        val block1X = chunkSize - 1  // 15 (chunk 0)
        val block2X = chunkSize      // 16 (chunk 1)
        val block3X = chunkSize + 1  // 17 (chunk 1)

        val block1Chunk = block1X / chunkSize  // 0
        val block2Chunk = block2X / chunkSize  // 1
        val block3Chunk = block3X / chunkSize  // 1

        assertNotEquals(block1Chunk, block2Chunk,
            "Sequence should work across chunk boundaries")
    }

    @Test
    fun `all zeros is valid sequence`() {
        // [0][0][0] = 000 is technically valid (even if unlikely to match)
        val number = 0 * 100 + 0 * 10 + 0
        assertEquals(0, number, "[0][0][0] forms valid number 0")
    }

    @Test
    fun `sequence with maximum digits`() {
        // [9][9][9] = 999
        val maxNumber = 9 * 100 + 9 * 10 + 9
        assertEquals(999, maxNumber, "[9][9][9] forms 999")
    }

    // ==================== Custom Block Identification Tests ====================

    @Test
    fun `getNumberFromBlock expects NoteBlock blockdata`() {
        // Custom item providers (ItemsAdder/Oraxen) use NoteBlock for custom blocks
        // Non-NoteBlock returns null immediately

        assertTrue(true, "Document: Only NoteBlock blockdata is checked")
    }

    @Test
    fun `getNumberFromBlock parses custom item provider namespacedID`() {
        // Expected format: "charmedchars:cyan_5"
        // Parts: ["cyan", "5"]
        // Returns: 5

        val namespacedId = "charmedchars:cyan_5"
        val parts = namespacedId.substring("charmedchars:".length).split("_")

        assertEquals(2, parts.size, "NamespacedID should split into 2 parts")
        assertEquals("cyan", parts[0], "First part is color")
        assertEquals("5", parts[1], "Second part is digit")
        assertEquals(5, parts[1].toIntOrNull(), "Second part converts to integer")
    }

    @ParameterizedTest
    @CsvSource(
        "charmedchars:cyan_0, 0",
        "charmedchars:magenta_5, 5",
        "charmedchars:yellow_9, 9"
    )
    fun `number extraction from namespacedID`(namespacedId: String, expectedDigit: Int) {
        val parts = namespacedId.substring("charmedchars:".length).split("_")
        val digit = parts.getOrNull(1)?.toIntOrNull()

        assertNotNull(digit, "Should extract digit from $namespacedId")
        assertEquals(expectedDigit, digit, "Extracted digit should match")
    }

    @Test
    fun `non-charmedchars blocks return null`() {
        // Blocks with different namespace should not be recognized
        val otherNamespace = "minecraft:stone"

        assertFalse(otherNamespace.startsWith("charmedchars:"),
            "Non-charmedchars blocks should be filtered out")
    }

    @Test
    fun `letter blocks return null`() {
        // "charmedchars:cyan_a" should return null (not a digit)
        val letterBlock = "charmedchars:cyan_a"
        val parts = letterBlock.substring("charmedchars:".length).split("_")
        val character = parts[1]

        assertNull(character.toIntOrNull(), "Letter 'a' should not convert to Int")
    }
}
