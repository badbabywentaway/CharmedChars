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
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
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
     * Handles left-clicking number blocks for sequence scoring (Oraxen compatibility)
     *
     * PlayerInteractEvent fires immediately when a player left-clicks a block,
     * making it more reliable than BlockDamageEvent for Oraxen custom blocks.
     * This matches the letter spelling system's primary scoring trigger.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    fun onInteractNumberBlock(event: PlayerInteractEvent) {
        // Only care about left-clicking blocks
        if (event.action != Action.LEFT_CLICK_BLOCK) {
            return
        }

        val block = event.clickedBlock ?: return
        plugin.logger.info("[DEBUG] PlayerInteractEvent (LEFT_CLICK) fired! Block: ${block.type.name}, Tool: ${event.player.inventory.itemInMainHand.type.name}")

        processNumberSequenceScoring(event.player, block)
    }

    /**
     * Handles block damage (hits) to execute number sequence scoring (Nexo compatibility)
     *
     * BlockDamageEvent fires when a player starts breaking a block (left-click and hold).
     * This is more reliable than PlayerInteractEvent when noteblock updates are disabled.
     * Works better with Nexo when Paper's block-updates.disable-noteblock-updates is enabled.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    fun onNumberBlockDamage(event: BlockDamageEvent) {
        plugin.logger.info("[DEBUG] BlockDamageEvent fired! Block: ${event.block.type.name}, Tool: ${event.player.inventory.itemInMainHand.type.name}, Cancelled: ${event.isCancelled}")

        processNumberSequenceScoring(event.player, event.block)
    }

    /**
     * Core number sequence detection and scoring logic
     *
     * This is the main scoring logic used by both PlayerInteractEvent and BlockDamageEvent:
     * 1. Checks if player is in the Nether
     * 2. Validates the player is using a gold or pyrite tool
     * 3. Checks if the hit block is a number block
     * 4. Scans for a 3-digit sequence in all four cardinal directions
     * 5. Determines structure type (fortress or bastion)
     * 6. Compares sequence against structure's secret number
     * 7. Awards rewards, triggers explosions, or provides feedback
     *
     * @param player The player who hit the block
     * @param block The block that was hit
     */
    private fun processNumberSequenceScoring(player: org.bukkit.entity.Player, block: Block) {
        val location = block.location

        // Check if player is using a gold or pyrite tool
        val hand = player.inventory.itemInMainHand
        if (!isValidTool(hand)) {
            plugin.logger.info("[DEBUG] Not valid tool: ${hand.type.name}")
            return
        }

        // Check if the hit block is a number block
        plugin.logger.info("[DEBUG] Checking block: ${block.type.name} at ${block.location}")
        val firstDigit = getNumberFromBlock(block)
        if (firstDigit == null) {
            plugin.logger.info("[DEBUG] Not a number block or getNumberFromBlock returned null")
            return
        }
        plugin.logger.info("[DEBUG] Found number block: $firstDigit")

        // Check for 3-digit sequence first (before checking structure)
        plugin.logger.info("[DEBUG] Looking for 3-digit sequence starting with $firstDigit")
        val sequence = findThreeDigitSequence(block, firstDigit)

        // If no sequence found, let the event proceed normally
        if (sequence == null) {
            plugin.logger.info("[DEBUG] No 3-digit sequence found - need 3 adjacent number blocks in a straight line")
            return
        }
        plugin.logger.info("[DEBUG] Found sequence: ${sequence.number}")

        // Check if player is in the Nether - if not, just drop blocks
        if (!location.world.environment.name.equals("NETHER", ignoreCase = true)) {
            plugin.logger.info("[DEBUG] Not in Nether (world: ${location.world.environment.name}), dropping blocks as items")

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
                Component.text("Number blocks can only be used in the Nether! Blocks dropped.")
                    .color(NamedTextColor.YELLOW)
            )
            return
        }

        // Now we know we're in the Nether - check structure types
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
                Component.text("Number sequence detected, but you're not in a fortress or bastion! Blocks dropped.")
                    .color(NamedTextColor.YELLOW)
            )
            return
        }

        // Get the structure's origin coordinates (using bounding box)
        // This ensures all chunks of the same fortress use the same database entry
        val structures = chunk.getStructures(fortress)
        val structure = structures.firstOrNull()

        if (structure == null) {
            return
        }

        val boundingBox = structure.boundingBox
        // Use Math.floorDiv for proper handling of negative coordinates
        val originChunkX = Math.floorDiv(boundingBox.minX.toInt(), 16)
        val originChunkZ = Math.floorDiv(boundingBox.minZ.toInt(), 16)

        // Get fortress data using the structure's origin chunk
        val fortressData = database.getOrCreateStructure(
            worldName = location.world.name,
            structureType = StructureType.FORTRESS,
            chunkX = originChunkX,
            chunkZ = originChunkZ,
            discoveredBy = player.uniqueId
        )

        // Check if rewards were already dispensed
        if (fortressData.rewardsDispensed) {
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
            val rewards = plugin.configManager.getFortressNumberScoreRewards()
            val score = sequence.number.toDouble()

            // Apply all configured rewards
            for (reward in rewards) {
                reward.applyReward(player, location, score)
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
                Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    .color(NamedTextColor.GOLD)
            )

            plugin.logger.info("Player ${player.name} solved fortress #${fortressData.assignedNumber} (${sequence.number})")
        } else if (sequence.number > fortressData.assignedNumber) {
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

            plugin.logger.info("Player ${player.name} guessed too high for fortress #${fortressData.assignedNumber} (guessed ${sequence.number}) - EXPLOSION!")
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

            plugin.logger.info("Player ${player.name} guessed too low for fortress #${fortressData.assignedNumber} (guessed ${sequence.number})")
        }
    }

    /**
     * Handles number block breaking
     *
     * Number blocks are primarily scored via PlayerInteractEvent/BlockDamageEvent.
     * This handler does NOT cancel the break event, allowing ItemsAdder/Oraxen/Nexo
     * to handle tool checking and dropping for non-gold/non-pyrite tools.
     *
     * If scoring already removed the blocks, this will harmlessly try to break AIR.
     * If using wrong tool, provider handles dropping based on tool whitelist.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onNumberBlockBreak(event: BlockBreakEvent) {
        plugin.logger.info("[DEBUG] BlockBreakEvent fired! Block: ${event.block.type.name}, Tool: ${event.player.inventory.itemInMainHand.type.name}")

        val block = event.block

        // Check if this is a number block
        val digit = getNumberFromBlock(block)

        if (digit != null) {
            plugin.logger.info("[DEBUG] Number block detected in BlockBreakEvent (digit: $digit) - allowing break to proceed so provider can handle tool checking")
            // Scoring was already handled by PlayerInteractEvent/BlockDamageEvent
            // Allow the break to proceed (blocks already removed by processNumberSequenceScoring if valid sequence)
            // If not valid tool or no sequence, let ItemsAdder/Oraxen/Nexo handle breaking/dropping
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
            plugin.logger.info("[DEBUG] Block is not a NoteBlock: ${block.state.blockData.javaClass.simpleName}")
            return null
        }

        val provider = plugin.customItemProviderManager.getProvider()
        if (provider == null) {
            plugin.logger.info("[DEBUG] No custom item provider available")
            return null
        }
        plugin.logger.info("[DEBUG] Using provider: ${provider.getProviderName()}")

        val customBlockInfo = provider.getCustomBlock(block)
        if (customBlockInfo == null) {
            plugin.logger.info("[DEBUG] Provider returned null for block")
            return null
        }
        val namespacedId = customBlockInfo.namespacedId
        plugin.logger.info("[DEBUG] Got namespacedId: $namespacedId")

        // Parse the namespaced ID (e.g., "charmedchars:cyan_5" or "oraxen:cyan_5")
        val parts = namespacedId.split(":")
        if (parts.size != 2) {
            plugin.logger.info("[DEBUG] Invalid namespace format: $namespacedId")
            return null
        }

        val blockName = parts[1]  // e.g., "cyan_5"
        val nameParts = blockName.split("_")
        if (nameParts.size != 2) {
            plugin.logger.info("[DEBUG] Invalid block name format: $blockName")
            return null
        }

        val character = nameParts[1]  // e.g., "5"

        // Check if it's a digit
        val digit = character.toIntOrNull()
        plugin.logger.info("[DEBUG] Parsed digit: $digit from character: $character")
        return digit
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
