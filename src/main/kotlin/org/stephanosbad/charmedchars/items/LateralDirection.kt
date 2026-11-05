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
package org.stephanosbad.charmedchars.items

/**
 * Represents a lateral direction in the X-Z plane
 *
 * Used to track the direction when scanning letter blocks to form words.
 * Words must be formed in a single cardinal direction (±X or ±Z), not diagonally.
 *
 * @property xOffset The offset in the X direction (-1, 0, or 1)
 * @property zOffset The offset in the Z direction (-1, 0, or 1)
 */
class LateralDirection(var xOffset: Int, var zOffset: Int)