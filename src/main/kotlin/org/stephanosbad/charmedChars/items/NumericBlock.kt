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
     * Lazy-initialized map of ItemStacks for each block color
     */
    private val _itemStacks: MutableMap<BlockColor, ItemStack?> by lazy {
        mutableMapOf<BlockColor, ItemStack?>().apply {
            val plugin = Bukkit.getPluginManager().getPlugin("CharmedChars") as? CharmedChars
            val provider = plugin?.customItemProviderManager?.getProvider()

            if (provider == null) {
                System.err.println("WARNING: CustomItemProvider not available when initializing NumericBlock")
            }

            for (color in BlockColor.entries) {
                val itemId = "charmedchars:${color.directoryName}_${this@NumericBlock.c}"
                val itemStack = provider?.getItemStack(itemId)
                if (itemStack == null) {
                    System.err.println("WARNING: CustomItemProvider returned null for $itemId")
                }
                this[color] = itemStack
            }
        }
    }

    /**
     * Map of ItemStacks for each block color (cyan, magenta, yellow)
     */
    val itemStacks: MutableMap<BlockColor, ItemStack?>
        get() = _itemStacks
}