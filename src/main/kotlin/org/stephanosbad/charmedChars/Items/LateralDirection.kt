package org.stephanosbad.charmedChars.Items

class LateralDirection
/**
 * Constructor
 * @param xOffset x axis direction.
 * @param zOffset z axis direction
 */(var xOffset: Int, var zOffset: Int) {
    val isValid: Boolean
        /**
         * Check if this direction is orthogonal
         * @return Validity
         */
        get() = xOffset != 0 || zOffset != 0
}