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

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.stephanosbad.charmedchars.CharmedChars
import org.stephanosbad.charmedchars.database.StructureDatabase
import org.stephanosbad.charmedchars.database.StructureType
import org.stephanosbad.charmedchars.rewards.DropReward
import org.stephanosbad.integrare.CustomBlockHitListener

/**
 * Per-structure-type display/reward configuration, selected by [StructureNumberGame]
 * once it has determined which structure (if any) the player is standing in.
 */
private data class StructureGameConfig(
    val databaseType: StructureType,
    val celebrationLabel: String,
    val logLabel: String,
    val possessiveLabel: String,
    val borderColor: NamedTextColor,
    val codeColor: NamedTextColor,
    val rewardsSupplier: () -> List<DropReward>
)

/**
 * The Fortress and Bastion Remnant number guessing games, as a single listener.
 *
 * When a player breaks a sequence of 3 number blocks in a Nether Fortress or
 * Bastion Remnant and the numbers match that structure's assigned three-digit
 * number, the player receives a reward (12 blaze rods for a fortress, 16 ender
 * pearls for a bastion).
 *
 * These were originally two separate, independently-registered listeners, but
 * every hit needs the same detection work (tool check, digit check, sequence scan,
 * structure lookup) regardless of which structure the player is actually in -
 * running that twice, once per listener, was pure waste. Detection now happens
 * once per hit, then branches into a fortress- or bastion-flavored
 * [StructureGameConfig] for the remaining success/too-high/too-low handling. It
 * also means the "wrong dimension" / "in neither structure" messages are sent
 * exactly once, unconditionally - with only one listener there's no second one
 * racing to also send them, so no silent/messenger role distinction is needed.
 *
 * @property plugin Reference to the main plugin instance
 * @property database Database manager for structure tracking
 */
class StructureNumberGame(
    private val plugin: CharmedChars,
    private val database: StructureDatabase
) {

    companion object {
        /**
         * Registers the combined Fortress/Bastion number guessing game as a [Listener].
         *
         * Event-order handling (providers fire Interact/Damage/Break in different
         * orders) is delegated to [CustomBlockHitListener] rather than
         * reimplemented here.
         */
        fun listener(plugin: CharmedChars, database: StructureDatabase): Listener {
            val game = StructureNumberGame(plugin, database)
            val provider = plugin.customItemProviderManager.getProvider()
                ?: error("StructureNumberGame.listener requires an initialized custom item provider")
            return CustomBlockHitListener(
                provider = provider,
                matches = { it.blockName.substringAfterLast("_").toIntOrNull() != null },
                onHit = game::onNumberBlockHit
            )
        }
    }

    private val fortressConfig = StructureGameConfig(
        databaseType = StructureType.FORTRESS,
        celebrationLabel = "fortress",
        logLabel = "fortress",
        possessiveLabel = "fortress's",
        borderColor = NamedTextColor.GOLD,
        codeColor = NamedTextColor.YELLOW,
        rewardsSupplier = { plugin.configManager.getFortressNumberScoreRewards() }
    )

    private val bastionConfig = StructureGameConfig(
        databaseType = StructureType.BASTION_REMNANT,
        celebrationLabel = "bastion",
        logLabel = "bastion remnant",
        possessiveLabel = "bastion's",
        borderColor = NamedTextColor.LIGHT_PURPLE,
        codeColor = NamedTextColor.DARK_PURPLE,
        rewardsSupplier = { plugin.configManager.getBastionNumberScoreRewards() }
    )

    /**
     * Core number sequence detection and scoring logic, called via [listener] for a
     * left-click/damage/break hit on a number block:
     * 1. Validates the player is using a gold or pyrite tool
     * 2. Scans for a 3-digit sequence in all four cardinal directions
     * 3. Determines structure type (fortress or bastion), if either
     * 4. Compares sequence against structure's secret number
     * 5. Awards rewards, triggers explosions, or provides feedback
     *
     * @return true if a sequence was found and its blocks removed (dropped or
     *   consumed for reward), so the triggering event is cancelled; false lets the
     *   block break normally - no sequence was completed by this hit.
     */
    fun onNumberBlockHit(block: Block, player: Player): Boolean {
        val location = block.location

        // Check if player is using a gold or pyrite tool
        val hand = player.inventory.itemInMainHand
        if (!plugin.isValidTool(hand)) return false

        // Check if the hit block is a number block
        val firstDigit = plugin.getNumberFromBlock(block) ?: return false

        // Check for 3-digit sequence first (before checking structure)
        val sequence = plugin.findThreeDigitSequence(block, firstDigit) ?: return false

        // Check if player is in the Nether - if not, just drop blocks
        if (!location.world.environment.name.equals("NETHER", ignoreCase = true)) {
            return dropAndMessage(
                sequence, location, player,
                "Number blocks can only be used in the Nether! Blocks dropped."
            )
        }

        // Determine which structure (if any) the player is in
        val chunk = location.chunk
        val registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.STRUCTURE)
        val fortress = registry.get(NamespacedKey.minecraft("fortress"))
        val bastionRemnant = registry.get(NamespacedKey.minecraft("bastion_remnant"))

        // If a chunk ever contained both structures (not a case vanilla worldgen produces,
        // but not impossible with /structure or a custom world), fortress wins - this
        // differs from the old two-listener design, where each listener deferred to the
        // other on seeing it present, so neither would fire at all.
        val (structureKind, config) = when {
            fortress != null && chunk.getStructures(fortress).isNotEmpty() -> fortress to fortressConfig
            bastionRemnant != null && chunk.getStructures(bastionRemnant).isNotEmpty() -> bastionRemnant to bastionConfig
            else -> return dropAndMessage(
                sequence, location, player,
                "Number sequence detected, but you're not in a fortress or bastion! Blocks dropped."
            )
        }

        // Get the structure's origin coordinates (using bounding box)
        // This ensures all chunks of the same structure use the same database entry
        val structure = chunk.getStructures(structureKind).firstOrNull() ?: return false

        val boundingBox = structure.boundingBox
        // Use Math.floorDiv for proper handling of negative coordinates
        val originChunkX = Math.floorDiv(boundingBox.minX.toInt(), 16)
        val originChunkZ = Math.floorDiv(boundingBox.minZ.toInt(), 16)

        // Get structure data using the structure's origin chunk
        val structureData = database.getOrCreateStructure(
            worldName = location.world.name,
            structureType = config.databaseType,
            chunkX = originChunkX,
            chunkZ = originChunkZ,
            discoveredBy = player.uniqueId
        )

        // Check if rewards were already dispensed
        if (structureData.rewardsDispensed) {
            plugin.dropBlocksAsItems(sequence, location)

            player.sendMessage(
                Component.text("This ${config.possessiveLabel} treasure has already been claimed!")
                    .color(NamedTextColor.RED)
            )
            player.sendMessage(
                Component.text("Number blocks dropped as items.")
                    .color(NamedTextColor.YELLOW)
            )
            return true
        }

        // Compare with structure number (sequence already found above)
        if (sequence.number == structureData.assignedNumber) {
            // SUCCESS! Give rewards
            plugin.removeBlocks(sequence)

            // Get reward configuration and apply score-based rewards
            val rewards = config.rewardsSupplier()
            val score = sequence.number.toDouble()

            // Apply all configured rewards
            for (reward in rewards) {
                reward.applyReward(player, location, score)
            }

            // Mark rewards as dispensed
            database.markRewardsDispensed(structureData.id)

            // Celebrate!
            player.sendMessage(
                Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    .color(config.borderColor)
            )
            player.sendMessage(
                Component.text("  ✦ JACKPOT! ✦")
                    .color(config.borderColor)
                    .decorate(TextDecoration.BOLD)
            )
            player.sendMessage(
                Component.text("  You cracked the ${config.celebrationLabel} code: ${sequence.number}!")
                    .color(config.codeColor)
            )
            player.sendMessage(
                Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    .color(config.borderColor)
            )

            plugin.logger.info("Player ${player.name} solved ${config.logLabel} #${structureData.assignedNumber} (${sequence.number})")
        } else if (sequence.number > structureData.assignedNumber) {
            // Guessed too high - EXPLODE! (like sleeping in Nether)
            plugin.removeBlocks(sequence)

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

            plugin.logger.info("Player ${player.name} guessed too high for ${config.logLabel} #${structureData.assignedNumber} (guessed ${sequence.number}) - EXPLOSION!")
        } else {
            // Guessed too low - drop blocks as items
            plugin.dropBlocksAsItems(sequence, location)

            player.sendMessage(
                Component.text("Too low! The number is higher than ${sequence.number}.")
                    .color(NamedTextColor.RED)
            )
            player.sendMessage(
                Component.text("Number blocks dropped as items.")
                    .color(NamedTextColor.YELLOW)
            )

            plugin.logger.info("Player ${player.name} guessed too low for ${config.logLabel} #${structureData.assignedNumber} (guessed ${sequence.number})")
        }

        return true
    }

    /** Drops [sequence]'s blocks as items, sends [message], and returns true to cancel the event. */
    private fun dropAndMessage(sequence: NumberSequence, location: Location, player: Player, message: String): Boolean {
        plugin.dropBlocksAsItems(sequence, location)
        player.sendMessage(Component.text(message).color(NamedTextColor.YELLOW))
        return true
    }
}
