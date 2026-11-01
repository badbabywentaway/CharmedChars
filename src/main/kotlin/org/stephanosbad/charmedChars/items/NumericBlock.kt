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

import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import org.stephanosbad.charmedChars.CharmedChars

/**
 * Enum of numeric blocks (0-9)
 *
 * Number blocks can be obtained from nether wood (warped/crimson stems) when
 * nether drops are enabled in config.yml. Each number is available in three
 * colors (cyan, magenta, yellow).
 *
 * @property c The numeric character this block represents
 */
enum class NumericBlock(val c: Char) {
    BLOCK_0('0'),
    BLOCK_1('1'),
    BLOCK_2('2'),
    BLOCK_3('3'),
    BLOCK_4('4'),
    BLOCK_5('5'),
    BLOCK_6('6'),
    BLOCK_7('7'),
    BLOCK_8('8'),
    BLOCK_9('9'),
    ;

    /**
     * ItemStack for each block color (cyan, magenta, yellow), looked up fresh from
     * [CharmedChars.catalogItems] each access (which does its own caching with proper
     * invalidation after setup reruns - unlike a `by lazy` here, which would cache a
     * null forever if the provider wasn't ready yet the first time this was read).
     */
    val itemStacks: Map<BlockColor, ItemStack?>
        get() {
            val plugin = Bukkit.getPluginManager().getPlugin("CharmedChars") as? CharmedChars
            return BlockColor.entries.associateWith { color ->
                plugin?.catalogItems?.get("${color.directoryName}_$c")
            }
        }
}