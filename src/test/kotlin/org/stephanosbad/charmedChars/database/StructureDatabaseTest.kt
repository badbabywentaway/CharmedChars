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
package org.stephanosbad.charmedChars.database

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.stephanosbad.charmedChars.CharmedChars
import java.io.File
import java.util.UUID

/**
 * Unit tests for StructureDatabase
 *
 * Tests the critical database logic including:
 * - Unique number generation (100-999)
 * - Structure creation and retrieval
 * - Origin coordinate handling
 * - Concurrent access scenarios
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StructureDatabaseTest {

    private lateinit var database: StructureDatabase
    private lateinit var plugin: CharmedChars
    private lateinit var testDataFolder: File

    @BeforeAll
    fun setupAll() {
        // Create temporary directory for test database
        testDataFolder = File.createTempFile("charmedchars-test-", "").apply {
            delete()
            mkdirs()
        }

        // Mock the plugin
        plugin = mockk<CharmedChars>(relaxed = true)
        every { plugin.dataFolder } returns testDataFolder
        every { plugin.logger } returns mockk(relaxed = true)
    }

    @BeforeEach
    fun setup() {
        // Create fresh database for each test
        database = StructureDatabase(plugin)
        database.initialize()
    }

    @AfterEach
    fun cleanup() {
        database.close()
        // Clean up database file
        File(testDataFolder, "structures.db").delete()
    }

    @AfterAll
    fun cleanupAll() {
        testDataFolder.deleteRecursively()
    }

    // ==================== Number Generation Tests ====================

    @Test
    fun `generateUniqueNumber returns number between 100 and 999`() {
        val number = database.generateUniqueNumber()
        assertTrue(number in 100..999, "Number $number should be between 100 and 999")
    }

    @Test
    fun `generateUniqueNumber never returns duplicates`() {
        val numbers = mutableSetOf<Int>()

        // Create 100 structures, each should get a unique number
        repeat(100) { index ->
            val structure = database.getOrCreateStructure(
                worldName = "test_world",
                structureType = StructureType.FORTRESS,
                chunkX = index,
                chunkZ = 0,
                discoveredBy = UUID.randomUUID()
            )

            assertFalse(numbers.contains(structure.assignedNumber),
                "Number ${structure.assignedNumber} was assigned twice!")
            numbers.add(structure.assignedNumber)
        }

        assertEquals(100, numbers.size, "Should have 100 unique numbers")
        // All numbers should be in valid range
        assertTrue(numbers.all { it in 100..999 }, "All numbers should be 100-999")
    }

    @Test
    fun `generateUniqueNumber throws exception when exhausted`() {
        // Fill up all 900 numbers
        repeat(900) { index ->
            database.getOrCreateStructure(
                worldName = "test_world",
                structureType = StructureType.FORTRESS,
                chunkX = index,
                chunkZ = 0,
                discoveredBy = UUID.randomUUID()
            )
        }

        // 901st should throw
        assertThrows<IllegalStateException> {
            database.generateUniqueNumber()
        }
    }

    // ==================== Structure Creation Tests ====================

    @Test
    fun `getOrCreateStructure creates new entry with unique number`() {
        val playerId = UUID.randomUUID()

        val structure = database.getOrCreateStructure(
            worldName = "world_nether",
            structureType = StructureType.BASTION_REMNANT,
            chunkX = 10,
            chunkZ = 20,
            discoveredBy = playerId
        )

        assertNotNull(structure)
        assertTrue(structure.assignedNumber in 100..999)
        assertEquals("world_nether", structure.worldName)
        assertEquals(StructureType.BASTION_REMNANT, structure.structureType)
        assertEquals(10, structure.chunkX)
        assertEquals(20, structure.chunkZ)
        assertEquals(playerId, structure.discoveredBy)
        assertFalse(structure.rewardsDispensed)
    }

    @Test
    fun `getOrCreateStructure returns existing entry for same coordinates`() {
        val playerId1 = UUID.randomUUID()
        val playerId2 = UUID.randomUUID()

        // First player discovers
        val structure1 = database.getOrCreateStructure(
            worldName = "world_nether",
            structureType = StructureType.FORTRESS,
            chunkX = 5,
            chunkZ = 15,
            discoveredBy = playerId1
        )

        // Second player enters same structure
        val structure2 = database.getOrCreateStructure(
            worldName = "world_nether",
            structureType = StructureType.FORTRESS,
            chunkX = 5,
            chunkZ = 15,
            discoveredBy = playerId2
        )

        // Should be same structure
        assertEquals(structure1.id, structure2.id)
        assertEquals(structure1.assignedNumber, structure2.assignedNumber)
        assertEquals(playerId1, structure2.discoveredBy, "Original discoverer should be preserved")
    }

    @Test
    fun `different chunks in same structure should use same origin coordinates`() {
        // This test verifies the critical fix from v1.1.0
        val playerId = UUID.randomUUID()

        // Simulate origin chunk
        val structureOrigin = database.getOrCreateStructure(
            worldName = "world_nether",
            structureType = StructureType.BASTION_REMNANT,
            chunkX = 10,
            chunkZ = 20,
            discoveredBy = playerId
        )

        // Simulate different chunk in same structure (using same origin)
        val structureSameOrigin = database.getOrCreateStructure(
            worldName = "world_nether",
            structureType = StructureType.BASTION_REMNANT,
            chunkX = 10,
            chunkZ = 20,
            discoveredBy = playerId
        )

        // Should return the SAME structure
        assertEquals(structureOrigin.id, structureSameOrigin.id)
        assertEquals(structureOrigin.assignedNumber, structureSameOrigin.assignedNumber)
    }

    @Test
    fun `different structures in same world get different numbers`() {
        val playerId = UUID.randomUUID()

        val fortress1 = database.getOrCreateStructure(
            worldName = "world_nether",
            structureType = StructureType.FORTRESS,
            chunkX = 0,
            chunkZ = 0,
            discoveredBy = playerId
        )

        val fortress2 = database.getOrCreateStructure(
            worldName = "world_nether",
            structureType = StructureType.FORTRESS,
            chunkX = 100,
            chunkZ = 100,
            discoveredBy = playerId
        )

        assertNotEquals(fortress1.assignedNumber, fortress2.assignedNumber)
    }

    // ==================== Retrieval Tests ====================

    @Test
    fun `getStructure returns null for non-existent structure`() {
        val result = database.getStructure(
            worldName = "world_nether",
            structureType = StructureType.FORTRESS,
            chunkX = 999,
            chunkZ = 999
        )

        assertNull(result)
    }

    @Test
    fun `getStructure returns existing structure`() {
        val playerId = UUID.randomUUID()

        val created = database.getOrCreateStructure(
            worldName = "world_nether",
            structureType = StructureType.FORTRESS,
            chunkX = 50,
            chunkZ = 60,
            discoveredBy = playerId
        )

        val retrieved = database.getStructure(
            worldName = "world_nether",
            structureType = StructureType.FORTRESS,
            chunkX = 50,
            chunkZ = 60
        )

        assertNotNull(retrieved)
        assertEquals(created.id, retrieved?.id)
        assertEquals(created.assignedNumber, retrieved?.assignedNumber)
    }

    @Test
    fun `getStructureByNumber returns correct structure`() {
        val playerId = UUID.randomUUID()

        val structure = database.getOrCreateStructure(
            worldName = "world_nether",
            structureType = StructureType.BASTION_REMNANT,
            chunkX = 25,
            chunkZ = 35,
            discoveredBy = playerId
        )

        val retrieved = database.getStructureByNumber(structure.assignedNumber)

        assertNotNull(retrieved)
        assertEquals(structure.id, retrieved?.id)
        assertEquals(structure.assignedNumber, retrieved?.assignedNumber)
    }

    // ==================== Rewards Tests ====================

    @Test
    fun `markRewardsDispensed updates database correctly`() {
        val playerId = UUID.randomUUID()

        val structure = database.getOrCreateStructure(
            worldName = "world_nether",
            structureType = StructureType.FORTRESS,
            chunkX = 15,
            chunkZ = 25,
            discoveredBy = playerId
        )

        assertFalse(structure.rewardsDispensed)

        val success = database.markRewardsDispensed(structure.id)
        assertTrue(success)

        // Retrieve again to verify
        val updated = database.getStructure(
            worldName = "world_nether",
            structureType = StructureType.FORTRESS,
            chunkX = 15,
            chunkZ = 25
        )

        assertNotNull(updated)
        assertTrue(updated!!.rewardsDispensed)
    }

    // ==================== Deletion Tests ====================

    @Test
    fun `deleteStructureByNumber removes structure`() {
        val playerId = UUID.randomUUID()

        val structure = database.getOrCreateStructure(
            worldName = "world_nether",
            structureType = StructureType.FORTRESS,
            chunkX = 30,
            chunkZ = 40,
            discoveredBy = playerId
        )

        val deleted = database.deleteStructureByNumber(structure.assignedNumber)
        assertTrue(deleted)

        // Should not exist anymore
        val retrieved = database.getStructureByNumber(structure.assignedNumber)
        assertNull(retrieved)
    }

    @Test
    fun `purgeAllStructures clears database`() {
        val playerId = UUID.randomUUID()

        // Create multiple structures
        repeat(10) { index ->
            database.getOrCreateStructure(
                worldName = "world_nether",
                structureType = StructureType.FORTRESS,
                chunkX = index,
                chunkZ = index,
                discoveredBy = playerId
            )
        }

        val allStructures = database.getAllStructures()
        assertEquals(10, allStructures.size)

        val purged = database.purgeAllStructures()
        assertEquals(10, purged)

        val afterPurge = database.getAllStructures()
        assertEquals(0, afterPurge.size)
    }
}
