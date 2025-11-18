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

import org.jetbrains.exposed.sql.Table

/**
 * Database table for tracking Nether structures (Bastion Remnants and Fortresses)
 *
 * This table stores unique identifiers for discovered structures along with
 * their randomly assigned three-digit numbers for number guessing gameplay.
 *
 * @property id Auto-incrementing primary key
 * @property worldName Name of the world where the structure exists
 * @property structureType Type of structure (BASTION_REMNANT or FORTRESS)
 * @property chunkX X-coordinate of the structure's chunk
 * @property chunkZ Z-coordinate of the structure's chunk
 * @property assignedNumber Three-digit number (100-999) assigned to this structure
 * @property discoveredBy UUID of the first player who discovered this structure
 * @property discoveredAt Timestamp when the structure was first discovered
 * @property rewardsDispensed Whether rewards have been dispensed for this structure
 */
object StructureTable : Table("nether_structures") {
    val id = integer("id").autoIncrement()
    val worldName = varchar("world_name", 255)
    val structureType = varchar("structure_type", 50)
    val chunkX = integer("chunk_x")
    val chunkZ = integer("chunk_z")
    val assignedNumber = integer("assigned_number")
    val discoveredBy = varchar("discovered_by", 36)
    val discoveredAt = long("discovered_at")
    val rewardsDispensed = bool("rewards_dispensed").default(false)

    override val primaryKey = PrimaryKey(id)

    /**
     * Creates a unique index on world, structure type, and chunk coordinates
     * to prevent duplicate structure entries
     */
    init {
        index(isUnique = true, worldName, structureType, chunkX, chunkZ)
    }
}
