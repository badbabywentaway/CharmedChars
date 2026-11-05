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
package org.stephanosbad.charmedchars.listeners

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.inventory.ItemStack
import org.stephanosbad.charmedchars.CharmedChars
import org.stephanosbad.integrare.matchesTool
import org.stephanosbad.integrare.splitName

/**
 * [NumberSequence] detection and block-handling helpers shared by [StructureNumberGame].
 * Namespaced here (rather than left as bare top-level functions) so call sites make
 * clear where this gameplay-specific logic comes from.
 */
object NumberSequenceGame {

    /**
     * Finds a valid three-digit sequence starting from the broken block
     *
     * Checks all four cardinal directions (±X, ±Z) to find a sequence of exactly
     * three number blocks in a straight line.
     *
     * @param startBlock The block that was broken
     * @param firstDigit The digit value of the broken block
     * @return NumberSequence if found, null otherwise
     */
    fun CharmedChars.findThreeDigitSequence(startBlock: Block, firstDigit: Int): NumberSequence? {
        val world = startBlock.world
        val x = startBlock.x
        val y = startBlock.y
        val z = startBlock.z

        // Check all 4 directions
        val directions = listOf(
            Triple(1, 0, "X+"),   // +X direction
            Triple(-1, 0, "X-"),  // -X direction
            Triple(0, 1, "Z+"),   // +Z direction
            Triple(0, -1, "Z-")   // -Z direction
        )

        for ((xOffset, zOffset, _) in directions) {
            val block2 = world.getBlockAt(x + xOffset, y, z + zOffset)
            val block3 = world.getBlockAt(x + xOffset * 2, y, z + zOffset * 2)

            val digit2 = getNumberFromBlock(block2)
            val digit3 = getNumberFromBlock(block3)

            if (digit2 != null && digit3 != null) {
                // Found a valid 3-digit sequence!
                val number = firstDigit * 100 + digit2 * 10 + digit3
                return NumberSequence(
                    number = number,
                    blocks = listOf(startBlock, block2, block3)
                )
            }
        }

        return null // No valid sequence found
    }

    /**
     * Extracts the digit value from a number block
     *
     * Uses the custom item provider to identify numeric custom blocks and parse their digit.
     * Works with any namespace (charmedchars, oraxen, nexo, itemsadder).
     *
     * @param block The block to check
     * @return The digit (0-9) if it's a number block, null otherwise
     */
    fun CharmedChars.getNumberFromBlock(block: Block): Int? {
        if (block.state.blockData !is NoteBlock) {
            return null
        }

        val provider = customItemProviderManager.getProvider() ?: return null
        val customBlockInfo = provider.getCustomBlock(block) ?: return null
        val (_, character) = customBlockInfo.splitName() ?: return null

        return character.toIntOrNull()
    }

    /**
     * Checks if an item is a valid tool for number sequence gameplay
     *
     * Valid tools include:
     * - Vanilla gold tools
     * - Pyrite tools (custom tools from any provider)
     *
     * @param item The item to check
     * @return true if the item is a valid gold or pyrite tool
     */
    fun CharmedChars.isValidTool(item: ItemStack): Boolean =
        customItemProviderManager.getProvider().matchesTool(item, "gold", "pyrite")

    /**
     * Drops [sequence]'s blocks as items at [location] rather than awarding them -
     * used for every path where the player doesn't earn the sequence's reward.
     */
    fun CharmedChars.dropBlocksAsItems(sequence: NumberSequence, location: Location) {
        val provider = customItemProviderManager.getProvider()
        for (block in sequence.blocks) {
            if (provider != null) {
                val customBlockInfo = provider.getCustomBlock(block)
                if (customBlockInfo != null) {
                    val itemStack = provider.getItemStack(customBlockInfo.namespacedId)
                    if (itemStack != null) {
                        location.world.dropItemNaturally(block.location, itemStack)
                    }
                    provider.removeCustomBlock(block)
                } else {
                    block.type = Material.AIR
                }
            } else {
                block.type = Material.AIR
            }
        }
    }

    /**
     * Removes [sequence]'s blocks without dropping them - used when the blocks are
     * consumed for a reward or destroyed by the too-high explosion.
     */
    fun CharmedChars.removeBlocks(sequence: NumberSequence) {
        val provider = customItemProviderManager.getProvider()
        for (block in sequence.blocks) {
            if (provider != null) {
                if (!provider.removeCustomBlock(block)) {
                    block.type = Material.AIR
                }
            } else {
                block.type = Material.AIR
            }
        }
    }
}
