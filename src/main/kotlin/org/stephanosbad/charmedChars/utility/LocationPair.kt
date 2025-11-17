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

class LocationPair
/**
 * @param first
 * @param second
 */
    (first: Location, second: Location) : SimplerTuple<Location>(first, second) {
    val isValid: Boolean
        /**
         * @return
         */
        get() = first.world == second.world

    /**
     * @param location
     * @return
     */
    fun check(location: Location): Boolean {
        return location.world == first.world &&
                inMcRange(location.x, first.x, second.x) &&
                inMcRange(location.z, first.z, second.z)
    }

    /**
     * @param testValue
     * @param x1
     * @param x2
     * @return
     */
    private fun inMcRange(testValue: Double, x1: Double, x2: Double): Boolean {
        if (x1 > x2) {
            return testValue <= x1 && testValue >= x2
        }
        return testValue >= x1 && testValue <= x2
    }
}
