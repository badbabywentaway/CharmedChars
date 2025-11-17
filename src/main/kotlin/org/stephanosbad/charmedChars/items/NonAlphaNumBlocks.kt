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


import dev.lone.itemsadder.api.CustomStack
import org.bukkit.inventory.ItemStack

/**
 * Enum of operator/symbol blocks (+, -, *, /)
 *
 * These special character blocks can be obtained from nether wood when operator
 * drops are enabled in config.yml. Each operator is available in three colors.
 *
 * @property charVal The character symbol this block represents
 * @property nonAlphaNumBlockName The ItemsAdder block name for this operator
 */
enum class NonAlphaNumBlocks(val charVal: Char, blockName: String) {
    PLUS('+', "plus"),
    MINUS('-', "minus"),
    MULTIPLY('*', "multiply"),
    DIVISION('/', "division");

    /**
     * Lazy-initialized map of ItemStacks for each block color
     */
    private val _itemStacks: MutableMap<BlockColor, ItemStack?> by lazy {
        mutableMapOf<BlockColor, ItemStack?>().apply {
            for (color in BlockColor.entries) {
                val itemId = "charmedchars:${color.directoryName}_${this@NonAlphaNumBlocks.nonAlphaNumBlockName}"
                val customStack = CustomStack.getInstance(itemId)
                if (customStack == null) {
                    System.err.println("WARNING: ItemsAdder CustomStack.getInstance returned null for $itemId")
                }
                this[color] = customStack?.itemStack
            }
        }
    }

    /**
     * Map of ItemStacks for each block color (cyan, magenta, yellow)
     */
    val itemStacks: MutableMap<BlockColor, ItemStack?>
        get() = _itemStacks

    /**
     * The ItemsAdder block name for this operator
     */
    val nonAlphaNumBlockName = blockName
}