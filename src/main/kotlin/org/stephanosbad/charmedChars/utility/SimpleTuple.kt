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

/**
 * Generic tuple class for holding two values of potentially different types
 *
 * A simple data structure for pairing two related values. Used throughout the
 * plugin for returning multiple values from functions (e.g., letter character
 * and frequency score).
 *
 * @param T The type of the first value
 * @param U The type of the second value
 * @property first The first value in the tuple
 * @property second The second value in the tuple
 */
open class SimpleTuple<T, U>(var first: T, var second: U)
