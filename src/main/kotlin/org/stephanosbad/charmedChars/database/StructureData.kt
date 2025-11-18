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

import java.util.UUID

/**
 * Data class representing a tracked Nether structure with its assigned number
 *
 * @property id Unique identifier in the database
 * @property worldName Name of the world where the structure exists
 * @property structureType Type of structure (Bastion Remnant or Fortress)
 * @property chunkX X-coordinate of the structure's chunk
 * @property chunkZ Z-coordinate of the structure's chunk
 * @property assignedNumber Three-digit number (100-999) assigned for guessing
 * @property discoveredBy UUID of the player who first discovered this structure
 * @property discoveredAt Timestamp (milliseconds since epoch) of discovery
 * @property rewardsDispensed Whether rewards have been dispensed for this structure
 */
data class StructureData(
    val id: Int,
    val worldName: String,
    val structureType: StructureType,
    val chunkX: Int,
    val chunkZ: Int,
    val assignedNumber: Int,
    val discoveredBy: UUID,
    val discoveredAt: Long,
    val rewardsDispensed: Boolean = false
) {
    /**
     * Returns a unique key for this structure based on its location
     */
    fun getLocationKey(): String {
        return "$worldName:$chunkX:$chunkZ:${structureType.name}"
    }
}
