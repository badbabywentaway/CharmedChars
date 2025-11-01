package org.stephanosbad.charmedChars.Utility

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
