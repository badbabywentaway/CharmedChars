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

/**
 * Enum representing the types of Nether structures tracked by the plugin
 *
 * @property displayName Human-readable name of the structure type
 */
enum class StructureType(val displayName: String) {
    /**
     * Bastion Remnant structures found in the Nether
     */
    BASTION_REMNANT("Bastion Remnant"),

    /**
     * Nether Fortress structures found in the Nether
     */
    FORTRESS("Nether Fortress")
}
