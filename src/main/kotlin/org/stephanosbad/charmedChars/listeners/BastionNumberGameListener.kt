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
package org.stephanosbad.charmedChars.listeners

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.block.Block
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.inventory.ItemStack
import org.stephanosbad.charmedChars.CharmedChars
import org.stephanosbad.charmedChars.database.StructureDatabase
import org.stephanosbad.charmedChars.database.StructureType

/**
 * Listener for the Bastion Remnant number guessing game
 *
 * When a player breaks a sequence of 3 number blocks in a Bastion Remnant
 * and the numbers match the bastion's assigned three-digit number,
 * the player receives 16 ender pearls as a reward.
 *
 * @property plugin Reference to the main plugin instance
 * @property database Database manager for structure tracking
 */
class BastionNumberGameListener(
    private val plugin: CharmedChars,
    private val database: StructureDatabase
) : Listener {

    /**
     * Handles block damage (hits) to execute number sequence scoring
     *
     * When a player hits a number block with a valid tool in a bastion remnant,
     * this executes the full scoring logic including rewards, explosions, or feedback.
     * This matches the letter spelling system where scoring happens on hit, not break.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    fun onNumberBlockDamage(event: BlockDamageEvent) {
        val player = event.player
        val block = event.block
        val location = block.location

        // Check if player is in the Nether
        if (!location.world.environment.name.equals("NETHER", ignoreCase = true)) {
            return
        }

        // Check if player is using a gold or pyrite tool
        val hand = player.inventory.itemInMainHand
        if (!isValidTool(hand)) {
            return
        }

        // Check if the hit block is a number block
        val firstDigit = getNumberFromBlock(block) ?: return

        // Check for 3-digit sequence first (before checking structure)
        val sequence = findThreeDigitSequence(block, firstDigit)

        // If no sequence found, let the event proceed normally
        if (sequence == null) {
            return
        }

        // Check structure types
        val chunk = location.chunk
        val registry = Registry.STRUCTURE
        val bastionRemnant = registry.get(NamespacedKey.minecraft("bastion_remnant"))
        val fortress = registry.get(NamespacedKey.minecraft("fortress"))

        // If player is in a fortress, let the fortress listener handle it
        if (fortress != null && chunk.getStructures(fortress).isNotEmpty()) {
            return
        }

        // If player is not in a bastion remnant, ignore (fortress listener handles "not in any structure")
        if (bastionRemnant == null || chunk.getStructures(bastionRemnant).isEmpty()) {
            return
        }

        // Get the structure's origin coordinates (using bounding box)
        // This ensures all chunks of the same bastion use the same database entry
        val structures = chunk.getStructures(bastionRemnant)
        val structure = structures.firstOrNull()

        if (structure == null) {
            return
        }

        val boundingBox = structure.boundingBox
        // Use Math.floorDiv for proper handling of negative coordinates
        val originChunkX = Math.floorDiv(boundingBox.minX.toInt(), 16)
        val originChunkZ = Math.floorDiv(boundingBox.minZ.toInt(), 16)

        // Get bastion data using the structure's origin chunk
        val bastionData = database.getOrCreateStructure(
            worldName = location.world.name,
            structureType = StructureType.BASTION_REMNANT,
            chunkX = originChunkX,
            chunkZ = originChunkZ,
            discoveredBy = player.uniqueId
        )

        // Check if rewards were already dispensed
        if (bastionData.rewardsDispensed) {
            // Drop all blocks as items
            val provider = plugin.customItemProviderManager.getProvider()
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

            player.sendMessage(
                Component.text("This bastion's treasure has already been claimed!")
                    .color(NamedTextColor.RED)
            )
            player.sendMessage(
                Component.text("Number blocks dropped as items.")
                    .color(NamedTextColor.YELLOW)
            )
            return
        }

        // Compare with bastion number (sequence already found above)
        if (sequence.number == bastionData.assignedNumber) {
            // SUCCESS! Give rewards
            // Remove all three blocks
            val provider = plugin.customItemProviderManager.getProvider()
            for (block in sequence.blocks) {
                if (provider != null) {
                    if (!provider.removeCustomBlock(block)) {
                        block.type = Material.AIR
                    }
                } else {
                    block.type = Material.AIR
                }
            }

            // Get reward configuration and apply score-based rewards
            val rewards = plugin.configManager.getBastionNumberScoreRewards()
            val score = sequence.number.toDouble()

            // Apply all configured rewards
            for (reward in rewards) {
                reward.applyReward(player, location, score)
            }

            // Mark rewards as dispensed
            database.markRewardsDispensed(bastionData.id)

            // Celebrate!
            player.sendMessage(
                Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    .color(NamedTextColor.LIGHT_PURPLE)
            )
            player.sendMessage(
                Component.text("  ✦ JACKPOT! ✦")
                    .color(NamedTextColor.LIGHT_PURPLE)
                    .decorate(TextDecoration.BOLD)
            )
            player.sendMessage(
                Component.text("  You cracked the bastion code: ${sequence.number}!")
                    .color(NamedTextColor.DARK_PURPLE)
            )
            player.sendMessage(
                Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    .color(NamedTextColor.LIGHT_PURPLE)
            )

            plugin.logger.info("Player ${player.name} solved bastion remnant #${bastionData.assignedNumber} (${sequence.number})")
        } else if (sequence.number > bastionData.assignedNumber) {
            // Guessed too high - EXPLODE! (like sleeping in Nether)
            // Remove blocks before explosion
            val provider = plugin.customItemProviderManager.getProvider()
            for (block in sequence.blocks) {
                if (provider != null) {
                    if (!provider.removeCustomBlock(block)) {
                        block.type = Material.AIR
                    }
                } else {
                    block.type = Material.AIR
                }
            }

            // Create bed-like explosion (power 5.0, sets fire, breaks blocks)
            val explosionLocation = block.location.add(0.5, 0.5, 0.5)
            location.world.createExplosion(
                explosionLocation,
                5.0f,      // Power (same as bed explosion)
                true,      // Set fire
                true       // Break blocks
            )

            player.sendMessage(
                Component.text("TOO HIGH! The number is lower than ${sequence.number}!")
                    .color(NamedTextColor.DARK_RED)
                    .decorate(TextDecoration.BOLD)
            )

            plugin.logger.info("Player ${player.name} guessed too high for bastion remnant #${bastionData.assignedNumber} (guessed ${sequence.number}) - EXPLOSION!")
        } else {
            // Guessed too low - drop blocks as items
            val provider = plugin.customItemProviderManager.getProvider()
            for (block in sequence.blocks) {
                if (provider != null) {
                    val customBlockInfo = provider.getCustomBlock(block)
                    if (customBlockInfo != null) {
                        // Drop the custom block as an item
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

            player.sendMessage(
                Component.text("Too low! The number is higher than ${sequence.number}.")
                    .color(NamedTextColor.RED)
            )
            player.sendMessage(
                Component.text("Number blocks dropped as items.")
                    .color(NamedTextColor.YELLOW)
            )

            plugin.logger.info("Player ${player.name} guessed too low for bastion remnant #${bastionData.assignedNumber} (guessed ${sequence.number})")
        }
    }

    /**
     * Prevents number blocks from being broken
     *
     * Number blocks should only be removed by the scoring system when hit with a valid tool.
     * This handler prevents them from being broken normally to avoid item duplication or
     * breaking them without valid tools.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onNumberBlockBreak(event: BlockBreakEvent) {
        val block = event.block

        // Check if this is a number block
        val digit = getNumberFromBlock(block)

        // If it's a number block, cancel the break event
        // Scoring is handled by onNumberBlockDamage
        if (digit != null) {
            event.isCancelled = true
        }
    }

    /**
     * Data class representing a found three-digit sequence
     */
    private data class NumberSequence(
        val number: Int,
        val blocks: List<Block>
    )

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
    private fun findThreeDigitSequence(startBlock: Block, firstDigit: Int): NumberSequence? {
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
    private fun getNumberFromBlock(block: Block): Int? {
        if (block.state.blockData !is NoteBlock) {
            return null
        }

        val provider = plugin.customItemProviderManager.getProvider() ?: return null
        val customBlockInfo = provider.getCustomBlock(block) ?: return null
        val namespacedId = customBlockInfo.namespacedId

        // Parse the namespaced ID (e.g., "charmedchars:cyan_5" or "oraxen:cyan_5")
        val parts = namespacedId.split(":")
        if (parts.size != 2) return null

        val blockName = parts[1]  // e.g., "cyan_5"
        val nameParts = blockName.split("_")
        if (nameParts.size != 2) return null

        val character = nameParts[1]  // e.g., "5"

        // Check if it's a digit
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
    private fun isValidTool(item: ItemStack): Boolean {
        if (item.itemMeta == null) {
            return false
        }

        // Check if it's a gold tool (vanilla)
        if (item.type.name.lowercase().contains("gold")) {
            return true
        }

        // Check if it's a pyrite tool using the custom item provider
        val provider = plugin.customItemProviderManager.getProvider()
        if (provider != null) {
            val customItem = provider.getCustomItem(item)
            if (customItem != null) {
                val namespacedId = customItem.namespacedId.lowercase()
                if (namespacedId.contains("pyrite")) {
                    return true
                }
            }
        }

        return false
    }
}
