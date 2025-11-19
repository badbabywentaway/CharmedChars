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

import dev.lone.itemsadder.api.CustomBlock
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
import org.bukkit.inventory.ItemStack
import org.stephanosbad.charmedChars.CharmedChars
import org.stephanosbad.charmedChars.database.StructureDatabase
import org.stephanosbad.charmedChars.database.StructureType
import org.stephanosbad.charmedChars.items.NumericBlock

/**
 * Listener for the Fortress number guessing game
 *
 * When a player breaks a sequence of 3 number blocks in a Nether Fortress
 * and the numbers match the fortress's assigned three-digit number,
 * the player receives 12 blaze rods as a reward.
 *
 * @property plugin Reference to the main plugin instance
 * @property database Database manager for structure tracking
 */
class FortressNumberGameListener(
    private val plugin: CharmedChars,
    private val database: StructureDatabase
) : Listener {

    /**
     * Handles block breaking to detect number sequences in fortresses
     *
     * This method checks if:
     * 1. The broken block is a number block
     * 2. The player is in a Nether Fortress
     * 3. There are 2 more number blocks adjacent (forming a 3-digit sequence)
     * 4. The sequence matches the fortress's assigned number
     * 5. Rewards haven't been dispensed yet
     *
     * If all conditions are met, gives 12 blaze rods and marks rewards as dispensed.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onNumberBlockBreak(event: BlockBreakEvent) {
        val player = event.player
        val brokenBlock = event.block
        val location = brokenBlock.location

        // Check if player is in the Nether
        if (!location.world.environment.name.equals("NETHER", ignoreCase = true)) {
            return
        }

        // Check if the broken block is a number block
        val firstDigit = getNumberFromBlock(brokenBlock) ?: return

        // Check for 3-digit sequence first (before checking structure)
        val sequence = findThreeDigitSequence(brokenBlock, firstDigit)

        // If no sequence found, let the event proceed normally
        if (sequence == null) {
            return
        }

        // Check structure types
        val chunk = location.chunk
        val registry = Registry.STRUCTURE
        val fortress = registry.get(NamespacedKey.minecraft("fortress"))
        val bastionRemnant = registry.get(NamespacedKey.minecraft("bastion_remnant"))

        // If player is in a bastion, let the bastion listener handle it
        if (bastionRemnant != null && chunk.getStructures(bastionRemnant).isNotEmpty()) {
            return
        }

        if (fortress == null || chunk.getStructures(fortress).isEmpty()) {
            // Player has a 3-digit sequence but NOT in a fortress (and not in bastion)
            // Drop the blocks as items instead of removing them
            event.isCancelled = true

            for (block in sequence.blocks) {
                val customBlock = CustomBlock.byAlreadyPlaced(block)
                if (customBlock != null) {
                    // Drop the custom block as an item
                    val itemStack = customBlock.itemStack
                    if (itemStack != null) {
                        location.world.dropItemNaturally(block.location, itemStack)
                    }
                    customBlock.remove()
                } else {
                    block.type = Material.AIR
                }
            }

            player.sendMessage(
                Component.text("Number sequence detected, but you're not in a fortress or bastion! Blocks dropped.")
                    .color(NamedTextColor.YELLOW)
            )
            return
        }

        // Get fortress data
        val fortressData = database.getOrCreateStructure(
            worldName = location.world.name,
            structureType = StructureType.FORTRESS,
            chunkX = chunk.x,
            chunkZ = chunk.z,
            discoveredBy = player.uniqueId
        )

        // Check if rewards were already dispensed
        if (fortressData.rewardsDispensed) {
            event.isCancelled = true

            // Drop all blocks as items
            for (block in sequence.blocks) {
                val customBlock = CustomBlock.byAlreadyPlaced(block)
                if (customBlock != null) {
                    val itemStack = customBlock.itemStack
                    if (itemStack != null) {
                        location.world.dropItemNaturally(block.location, itemStack)
                    }
                    customBlock.remove()
                } else {
                    block.type = Material.AIR
                }
            }

            player.sendMessage(
                Component.text("This fortress's treasure has already been claimed!")
                    .color(NamedTextColor.RED)
            )
            player.sendMessage(
                Component.text("Number blocks dropped as items.")
                    .color(NamedTextColor.YELLOW)
            )
            return
        }

        // Compare with fortress number (sequence already found above)
        if (sequence.number == fortressData.assignedNumber) {
            // SUCCESS! Give rewards
            event.isCancelled = true // Cancel the break event

            // Remove all three blocks
            for (block in sequence.blocks) {
                val customBlock = CustomBlock.byAlreadyPlaced(block)
                if (customBlock != null) {
                    customBlock.remove()
                } else {
                    block.type = Material.AIR
                }
            }

            // Get reward configuration
            val rewardMaterialName = plugin.configManager.fortressRewardMaterial
            val rewardAmount = plugin.configManager.fortressRewardAmount

            // Parse material and give reward
            val rewardMaterial = Material.getMaterial(rewardMaterialName)
            if (rewardMaterial != null) {
                val rewardItem = ItemStack(rewardMaterial, rewardAmount)
                player.inventory.addItem(rewardItem)
            } else {
                plugin.logger.warning("Invalid fortress reward material: $rewardMaterialName. Using BLAZE_ROD as fallback.")
                val rewardItem = ItemStack(Material.BLAZE_ROD, rewardAmount)
                player.inventory.addItem(rewardItem)
            }

            // Mark rewards as dispensed
            database.markRewardsDispensed(fortressData.id)

            // Celebrate!
            player.sendMessage(
                Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    .color(NamedTextColor.GOLD)
            )
            player.sendMessage(
                Component.text("  ✦ JACKPOT! ✦")
                    .color(NamedTextColor.GOLD)
                    .decorate(TextDecoration.BOLD)
            )
            player.sendMessage(
                Component.text("  You cracked the fortress code: ${sequence.number}!")
                    .color(NamedTextColor.YELLOW)
            )
            player.sendMessage(
                Component.text("  Reward: $rewardAmount ${rewardMaterial?.name?.replace("_", " ") ?: rewardMaterialName}")
                    .color(NamedTextColor.GREEN)
                    .decorate(TextDecoration.BOLD)
            )
            player.sendMessage(
                Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    .color(NamedTextColor.GOLD)
            )

            plugin.logger.info("Player ${player.name} solved fortress #${fortressData.assignedNumber} (${sequence.number})")
        } else if (sequence.number > fortressData.assignedNumber) {
            // Guessed too high - EXPLODE! (like sleeping in Nether)
            event.isCancelled = true

            // Remove blocks before explosion
            for (block in sequence.blocks) {
                val customBlock = CustomBlock.byAlreadyPlaced(block)
                if (customBlock != null) {
                    customBlock.remove()
                } else {
                    block.type = Material.AIR
                }
            }

            // Create bed-like explosion (power 5.0, sets fire, breaks blocks)
            val explosionLocation = brokenBlock.location.add(0.5, 0.5, 0.5)
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

            plugin.logger.info("Player ${player.name} guessed too high for fortress #${fortressData.assignedNumber} (guessed ${sequence.number}) - EXPLOSION!")
        } else {
            // Guessed too low - drop blocks as items
            event.isCancelled = true

            for (block in sequence.blocks) {
                val customBlock = CustomBlock.byAlreadyPlaced(block)
                if (customBlock != null) {
                    // Drop the custom block as an item
                    val itemStack = customBlock.itemStack
                    if (itemStack != null) {
                        location.world.dropItemNaturally(block.location, itemStack)
                    }
                    customBlock.remove()
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

            plugin.logger.info("Player ${player.name} guessed too low for fortress #${fortressData.assignedNumber} (guessed ${sequence.number})")
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
     * Uses ItemsAdder API to identify numeric custom blocks and parse their digit.
     *
     * @param block The block to check
     * @return The digit (0-9) if it's a number block, null otherwise
     */
    private fun getNumberFromBlock(block: Block): Int? {
        if (block.state.blockData !is NoteBlock) {
            return null
        }

        val customBlock = CustomBlock.byAlreadyPlaced(block) ?: return null
        val namespacedId = customBlock.namespacedID

        // Parse the ItemsAdder ID (e.g., "charmedchars:cyan_5")
        if (!namespacedId.startsWith("charmedchars:")) return null

        val parts = namespacedId.substring("charmedchars:".length).split("_")
        if (parts.size != 2) return null

        val character = parts[1]

        // Check if it's a digit
        return character.toIntOrNull()
    }
}
