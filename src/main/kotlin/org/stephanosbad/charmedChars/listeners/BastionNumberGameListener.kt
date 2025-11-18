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
     * Handles block breaking to detect number sequences in bastion remnants
     *
     * This method checks if:
     * 1. The broken block is a number block
     * 2. The player is in a Bastion Remnant
     * 3. There are 2 more number blocks adjacent (forming a 3-digit sequence)
     * 4. The sequence matches the bastion's assigned number
     * 5. Rewards haven't been dispensed yet
     *
     * If all conditions are met, gives 16 ender pearls and marks rewards as dispensed.
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

        // Check if player is in a bastion remnant
        val chunk = location.chunk
        val registry = Registry.STRUCTURE
        val bastionRemnant = registry.get(NamespacedKey.minecraft("bastion_remnant"))

        if (bastionRemnant == null || chunk.getStructures(bastionRemnant).isEmpty()) {
            return // Not in a bastion remnant
        }

        // Get bastion data
        val bastionData = database.getOrCreateStructure(
            worldName = location.world.name,
            structureType = StructureType.BASTION_REMNANT,
            chunkX = chunk.x,
            chunkZ = chunk.z,
            discoveredBy = player.uniqueId
        )

        // Check if rewards were already dispensed
        if (bastionData.rewardsDispensed) {
            player.sendMessage(
                Component.text("This bastion's treasure has already been claimed!")
                    .color(NamedTextColor.RED)
            )
            return
        }

        // Check for 3-digit sequence in all directions
        val sequence = findThreeDigitSequence(brokenBlock, firstDigit)

        if (sequence == null) {
            return // No valid 3-digit sequence found
        }

        // Compare with bastion number
        if (sequence.number == bastionData.assignedNumber) {
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
            val rewardMaterialName = plugin.configManager.bastionRewardMaterial
            val rewardAmount = plugin.configManager.bastionRewardAmount

            // Parse material and give reward
            val rewardMaterial = Material.getMaterial(rewardMaterialName)
            if (rewardMaterial != null) {
                val rewardItem = ItemStack(rewardMaterial, rewardAmount)
                player.inventory.addItem(rewardItem)
            } else {
                plugin.logger.warning("Invalid bastion reward material: $rewardMaterialName. Using ENDER_PEARL as fallback.")
                val rewardItem = ItemStack(Material.ENDER_PEARL, rewardAmount)
                player.inventory.addItem(rewardItem)
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
                Component.text("  Reward: $rewardAmount ${rewardMaterial?.name?.replace("_", " ") ?: rewardMaterialName}")
                    .color(NamedTextColor.GREEN)
                    .decorate(TextDecoration.BOLD)
            )
            player.sendMessage(
                Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    .color(NamedTextColor.LIGHT_PURPLE)
            )

            plugin.logger.info("Player ${player.name} solved bastion remnant #${bastionData.assignedNumber} (${sequence.number})")
        } else {
            // Wrong number
            player.sendMessage(
                Component.text("Close, but not quite! This bastion's number is ${bastionData.assignedNumber}, not ${sequence.number}.")
                    .color(NamedTextColor.RED)
            )
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
