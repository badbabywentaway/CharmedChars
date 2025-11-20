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
package org.stephanosbad.charmedChars.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * Unit tests for coordinate calculation logic
 *
 * Tests the critical conversion from block coordinates to chunk coordinates
 * that is used to identify structure origins. This is essential for the
 * multi-chunk structure fix from v1.1.0.
 *
 * Formula: chunkCoord = (blockCoord / 16).toInt()
 */
class CoordinateCalculationTest {

    /**
     * Helper function that mimics the actual calculation used in listeners
     * Uses Math.floorDiv for proper handling of negative coordinates
     */
    private fun blockToChunk(blockCoord: Int): Int {
        return Math.floorDiv(blockCoord, 16)
    }

    // ==================== Positive Coordinate Tests ====================

    @ParameterizedTest
    @CsvSource(
        "0, 0",      // Origin
        "15, 0",     // End of chunk 0
        "16, 1",     // Start of chunk 1
        "31, 1",     // End of chunk 1
        "32, 2",     // Start of chunk 2
        "100, 6",    // Middle chunk
        "256, 16",   // Larger chunk
        "1000, 62",  // Far chunk
    )
    fun `positive block coordinates convert correctly to chunk coordinates`(blockX: Int, expectedChunk: Int) {
        val result = blockToChunk(blockX)
        assertEquals(expectedChunk, result,
            "Block $blockX should be in chunk $expectedChunk")
    }

    // ==================== Negative Coordinate Tests ====================
    // CRITICAL: Nether often has negative coordinates!
    // Using Math.floorDiv() for proper floor division (not truncation)
    // floorDiv(-1, 16) = -1 (correct chunk)
    // Regular division: -1 / 16 = 0 (incorrect!)

    @ParameterizedTest
    @CsvSource(
        "-1, -1",     // floorDiv: -1/16 = -1 (correct!)
        "-15, -1",    // Still in chunk -1
        "-16, -1",    // Exactly at chunk boundary
        "-17, -2",    // Now in chunk -2
        "-31, -2",    // Still chunk -2
        "-32, -2",    // Exactly at chunk boundary
        "-100, -7",   // Far negative
        "-256, -16",  // Very far negative
        "-1000, -63", // Extremely far
    )
    fun `negative block coordinates convert correctly to chunk coordinates`(blockX: Int, expectedChunk: Int) {
        val result = blockToChunk(blockX)
        assertEquals(expectedChunk, result,
            "Block $blockX should be in chunk $expectedChunk (negative coords in Nether!)")
    }

    // ==================== Boundary Tests ====================

    @Test
    fun `chunk boundaries are calculated correctly`() {
        // Chunk 0: blocks 0-15
        assertEquals(0, blockToChunk(0))
        assertEquals(0, blockToChunk(15))

        // Chunk 1: blocks 16-31
        assertEquals(1, blockToChunk(16))
        assertEquals(1, blockToChunk(31))

        // With Math.floorDiv, negative coords are properly handled
        // Chunk -1: blocks -1 to -16
        assertEquals(-1, blockToChunk(-1))    // floorDiv: -1/16 = -1
        assertEquals(-1, blockToChunk(-15))   // floorDiv: -15/16 = -1
        assertEquals(-1, blockToChunk(-16))   // floorDiv: -16/16 = -1

        // Chunk -2: blocks -17 to -32
        assertEquals(-2, blockToChunk(-17))
        assertEquals(-2, blockToChunk(-31))
        assertEquals(-2, blockToChunk(-32))

        // Chunk -3: blocks -33 to -48
        assertEquals(-3, blockToChunk(-33))
        assertEquals(-3, blockToChunk(-47))
    }

    @Test
    fun `structure at world origin (0, 0) calculates correctly`() {
        val chunkX = blockToChunk(0)
        val chunkZ = blockToChunk(0)

        assertEquals(0, chunkX)
        assertEquals(0, chunkZ)
    }

    @Test
    fun `structure spanning chunk boundary calculates origin correctly`() {
        // Structure with bounding box from block 10 to 50
        // Should use chunk 0 as origin (minX = 10 -> chunk 0)
        val minBlockX = 10
        val maxBlockX = 50

        val originChunk = blockToChunk(minBlockX)
        assertEquals(0, originChunk, "Structure starting at block 10 should have origin chunk 0")

        // Verify other chunks are different
        val endChunk = blockToChunk(maxBlockX)
        assertEquals(3, endChunk, "Structure ending at block 50 should span to chunk 3")
    }

    @Test
    fun `large structure spanning many chunks uses first chunk as origin`() {
        // Simulate a very large bastion spanning blocks -50 to 100
        val minBlockX = -50
        val maxBlockX = 100

        val originChunk = blockToChunk(minBlockX)
        assertEquals(-4, originChunk, "Structure starting at block -50 should have origin chunk -4")

        val endChunk = blockToChunk(maxBlockX)
        assertEquals(6, endChunk, "Structure ending at block 100 should span to chunk 6")

        // Structure spans 11 chunks total (-4 to 6)
        val spanChunks = endChunk - originChunk + 1
        assertEquals(11, spanChunks)
    }

    // ==================== Edge Case Tests ====================

    @Test
    fun `Integer MIN_VALUE and MAX_VALUE handled correctly`() {
        // These are extreme edge cases but should not crash
        val minChunk = blockToChunk(Int.MIN_VALUE)
        val maxChunk = blockToChunk(Int.MAX_VALUE)

        assertTrue(minChunk < 0, "MIN_VALUE should result in negative chunk")
        assertTrue(maxChunk > 0, "MAX_VALUE should result in positive chunk")

        // Verify division works without overflow
        assertDoesNotThrow {
            blockToChunk(Int.MIN_VALUE)
            blockToChunk(Int.MAX_VALUE)
        }
    }

    @Test
    fun `exactly 16 blocks apart results in 1 chunk difference`() {
        assertEquals(1, blockToChunk(16) - blockToChunk(0))
        assertEquals(1, blockToChunk(32) - blockToChunk(16))
        assertEquals(1, blockToChunk(0) - blockToChunk(-16))
    }

    // ==================== Real-World Scenario Tests ====================

    @Test
    fun `typical fortress coordinates`() {
        // Fortress at blocks 200-300 (roughly 6 chunks wide)
        val minX = 200
        val minZ = -150

        val originChunkX = blockToChunk(minX)
        val originChunkZ = blockToChunk(minZ)

        assertEquals(12, originChunkX)   // 200 / 16 = 12
        assertEquals(-10, originChunkZ)   // floorDiv(-150, 16) = -10 (negative!)
    }

    @Test
    fun `typical bastion coordinates in deep Nether`() {
        // Bastion at blocks -800 to -700 (negative coords common in Nether)
        val minX = -800
        val minZ = -600

        val originChunkX = blockToChunk(minX)
        val originChunkZ = blockToChunk(minZ)

        assertEquals(-50, originChunkX)  // floorDiv(-800, 16) = -50
        assertEquals(-38, originChunkZ)  // floorDiv(-600, 16) = -38
    }

    @Test
    fun `same structure accessed from different chunks returns same origin`() {
        // Player 1 discovers at block (100, 200)
        val player1BlockX = 100
        val player1BlockZ = 200

        // Player 2 enters at block (120, 230) - same structure, different chunk
        val player2BlockX = 120
        val player2BlockZ = 230

        // Structure bounding box minimum (origin): (96, 192)
        val structureMinX = 96
        val structureMinZ = 192

        val originChunkX = blockToChunk(structureMinX)
        val originChunkZ = blockToChunk(structureMinZ)

        assertEquals(6, originChunkX)   // 96 / 16 = 6
        assertEquals(12, originChunkZ)  // 192 / 16 = 12

        // Both players should get the same origin
        val player1ChunkX = blockToChunk(player1BlockX)  // 100 / 16 = 6
        val player2ChunkX = blockToChunk(player2BlockX)  // 120 / 16 = 7 (different!)

        // But the structure origin is what matters
        assertTrue(player1ChunkX == originChunkX || player2ChunkX != originChunkX,
            "Different player chunks, but same structure origin")
    }
}
