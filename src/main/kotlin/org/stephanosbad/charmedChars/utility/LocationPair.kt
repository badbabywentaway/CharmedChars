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
package org.stephanosbad.charmedChars.utility

import org.bukkit.Location

/**
 * Represents a cuboid region defined by two corner locations
 *
 * Used to define include/exclude zones in config.yml where letter block
 * mechanics should be enabled or disabled. The two locations define opposite
 * corners of a rectangular region in the X-Z plane.
 *
 * @param first The first corner location
 * @param second The second corner location (opposite corner)
 */
class LocationPair(first: Location, second: Location) : SimplerTuple<Location>(first, second) {
    /**
     * Checks if this location pair is valid
     *
     * A valid location pair requires both locations to be in the same world.
     *
     * @return true if both locations are in the same world
     */
    val isValid: Boolean
        get() = first.world == second.world

    /**
     * Checks if a location is within this cuboid region
     *
     * Tests whether the given location is inside the rectangular area defined
     * by the two corner locations. Only checks X and Z coordinates (horizontal plane).
     *
     * @param location The location to test
     * @return true if the location is within the region
     */
    fun check(location: Location): Boolean {
        return location.world == first.world &&
                inMcRange(location.x, first.x, second.x) &&
                inMcRange(location.z, first.z, second.z)
    }

    /**
     * Checks if a value is between two bounds (inclusive, order-independent)
     *
     * Works correctly regardless of which bound is larger, making it suitable
     * for Minecraft coordinates where players can define corners in any order.
     *
     * @param testValue The value to test
     * @param x1 First bound
     * @param x2 Second bound
     * @return true if testValue is between x1 and x2 (inclusive)
     */
    private fun inMcRange(testValue: Double, x1: Double, x2: Double): Boolean {
        if (x1 > x2) {
            return testValue <= x1 && testValue >= x2
        }
        return testValue >= x1 && testValue <= x2
    }
}
