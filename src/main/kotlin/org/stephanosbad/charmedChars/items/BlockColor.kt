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
package org.stephanosbad.charmedChars.items

import kotlin.random.Random

/**
 * Enum representing the three available block colors
 *
 * Letter blocks come in three colors (cyan, magenta, yellow). When all letters
 * in a word are the same color, the player receives a 3x score multiplier bonus.
 *
 * @property directoryName The directory name used in ItemsAdder custom block IDs
 */
enum class BlockColor(val directoryName: String)
{
    /** Cyan colored blocks */
    CYAN("cyan"),
    /** Magenta colored blocks */
    MAGENTA("magenta"),
    /** Yellow colored blocks */
    YELLOW("yellow");

    companion object {
        /**
         * Returns a random block color
         *
         * @return Randomly selected color (CYAN, MAGENTA, or YELLOW)
         */
        fun getRand(): BlockColor {
            return entries.random()
        }
    }
}