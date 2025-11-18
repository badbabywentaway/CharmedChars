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

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.stephanosbad.charmedChars.CharmedChars
import java.io.File
import java.util.UUID
import kotlin.random.Random

/**
 * Database manager for tracking Nether structures and their assigned numbers
 *
 * This class handles all database operations for the structure tracking system,
 * including initialization, number generation, and CRUD operations.
 *
 * @property plugin Reference to the main plugin instance
 */
class StructureDatabase(private val plugin: CharmedChars) {

    private var database: Database? = null
    private val usedNumbers = mutableSetOf<Int>()

    /**
     * Initializes the database connection and creates tables if needed
     *
     * Creates a SQLite database file in the plugin's data folder and
     * sets up the structure tracking table.
     */
    fun initialize() {
        val dbFile = File(plugin.dataFolder, "structures.db")

        // Ensure the data folder exists
        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdirs()
        }

        // Connect to SQLite database
        database = Database.connect(
            url = "jdbc:sqlite:${dbFile.absolutePath}",
            driver = "org.sqlite.JDBC"
        )

        // Create tables
        transaction(database) {
            SchemaUtils.create(StructureTable)
        }

        // Load all used numbers into memory for quick lookups
        loadUsedNumbers()

        plugin.logger.info("Structure database initialized at ${dbFile.absolutePath}")
    }

    /**
     * Loads all assigned numbers from the database into memory
     */
    private fun loadUsedNumbers() {
        transaction(database) {
            StructureTable.selectAll().forEach { row ->
                usedNumbers.add(row[StructureTable.assignedNumber])
            }
        }
    }

    /**
     * Generates a unique three-digit number (100-999)
     *
     * @return A random number between 100 and 999 that hasn't been assigned yet
     * @throws IllegalStateException if all 900 possible numbers are exhausted
     */
    fun generateUniqueNumber(): Int {
        if (usedNumbers.size >= 900) {
            throw IllegalStateException("All three-digit numbers have been assigned!")
        }

        var number: Int
        do {
            number = Random.nextInt(100, 1000)
        } while (usedNumbers.contains(number))

        return number
    }

    /**
     * Gets or creates a structure record for the given location
     *
     * If a structure already exists at the given coordinates, returns it.
     * Otherwise, creates a new structure with a unique assigned number.
     *
     * @param worldName Name of the world
     * @param structureType Type of structure
     * @param chunkX Chunk X-coordinate
     * @param chunkZ Chunk Z-coordinate
     * @param discoveredBy UUID of the discovering player
     * @return StructureData for this location
     */
    fun getOrCreateStructure(
        worldName: String,
        structureType: StructureType,
        chunkX: Int,
        chunkZ: Int,
        discoveredBy: UUID
    ): StructureData {
        return transaction(database) {
            // Try to find existing structure
            val existing = StructureTable.select {
                (StructureTable.worldName eq worldName) and
                (StructureTable.structureType eq structureType.name) and
                (StructureTable.chunkX eq chunkX) and
                (StructureTable.chunkZ eq chunkZ)
            }.firstOrNull()

            if (existing != null) {
                // Return existing structure
                rowToStructureData(existing)
            } else {
                // Create new structure with unique number
                val assignedNumber = generateUniqueNumber()
                usedNumbers.add(assignedNumber)

                val id = StructureTable.insert {
                    it[StructureTable.worldName] = worldName
                    it[StructureTable.structureType] = structureType.name
                    it[StructureTable.chunkX] = chunkX
                    it[StructureTable.chunkZ] = chunkZ
                    it[StructureTable.assignedNumber] = assignedNumber
                    it[StructureTable.discoveredBy] = discoveredBy.toString()
                    it[StructureTable.discoveredAt] = System.currentTimeMillis()
                    it[StructureTable.rewardsDispensed] = false
                } get StructureTable.id

                StructureData(
                    id = id,
                    worldName = worldName,
                    structureType = structureType,
                    chunkX = chunkX,
                    chunkZ = chunkZ,
                    assignedNumber = assignedNumber,
                    discoveredBy = discoveredBy,
                    discoveredAt = System.currentTimeMillis(),
                    rewardsDispensed = false
                )
            }
        }
    }

    /**
     * Gets a structure by its assigned number
     *
     * @param number The three-digit number to search for
     * @return StructureData if found, null otherwise
     */
    fun getStructureByNumber(number: Int): StructureData? {
        return transaction(database) {
            StructureTable.select {
                StructureTable.assignedNumber eq number
            }.firstOrNull()?.let { rowToStructureData(it) }
        }
    }

    /**
     * Gets all structures discovered by a specific player
     *
     * @param playerUuid UUID of the player
     * @return List of structures discovered by this player
     */
    fun getStructuresByPlayer(playerUuid: UUID): List<StructureData> {
        return transaction(database) {
            StructureTable.select {
                StructureTable.discoveredBy eq playerUuid.toString()
            }.map { rowToStructureData(it) }
        }
    }

    /**
     * Gets all structures in a specific world
     *
     * @param worldName Name of the world
     * @return List of structures in this world
     */
    fun getStructuresByWorld(worldName: String): List<StructureData> {
        return transaction(database) {
            StructureTable.select {
                StructureTable.worldName eq worldName
            }.map { rowToStructureData(it) }
        }
    }

    /**
     * Marks rewards as dispensed for a specific structure
     *
     * @param structureId The database ID of the structure
     * @return true if the update was successful, false otherwise
     */
    fun markRewardsDispensed(structureId: Int): Boolean {
        return transaction(database) {
            val updated = StructureTable.update({ StructureTable.id eq structureId }) {
                it[rewardsDispensed] = true
            }
            updated > 0
        }
    }

    /**
     * Converts a database row to a StructureData object
     */
    private fun rowToStructureData(row: ResultRow): StructureData {
        return StructureData(
            id = row[StructureTable.id],
            worldName = row[StructureTable.worldName],
            structureType = StructureType.valueOf(row[StructureTable.structureType]),
            chunkX = row[StructureTable.chunkX],
            chunkZ = row[StructureTable.chunkZ],
            assignedNumber = row[StructureTable.assignedNumber],
            discoveredBy = UUID.fromString(row[StructureTable.discoveredBy]),
            discoveredAt = row[StructureTable.discoveredAt],
            rewardsDispensed = row[StructureTable.rewardsDispensed]
        )
    }

    /**
     * Closes the database connection
     */
    fun close() {
        // Exposed doesn't require explicit closing, but we can clear our cache
        usedNumbers.clear()
        plugin.logger.info("Structure database closed")
    }
}
