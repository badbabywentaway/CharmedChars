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
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.generator.structure.Structure
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.stephanosbad.charmedChars.CharmedChars
import org.stephanosbad.charmedChars.database.StructureDatabase
import org.stephanosbad.charmedChars.database.StructureType

/**
 * Listener for detecting when players enter Bastion Remnants or Nether Fortresses
 *
 * This listener tracks player movement and detects when they enter tracked structures.
 * When a player enters a new structure for the first time, a unique three-digit number
 * is assigned to that structure and stored in the database.
 *
 * @property plugin Reference to the main plugin instance
 * @property database Database manager for structure tracking
 */
class StructureListener(
    private val plugin: CharmedChars,
    private val database: StructureDatabase
) : Listener {

    // Cache to track which structure type each player is currently in
    // Key: Player UUID, Value: Structure key (world:structureType)
    private val playerCurrentStructure = mutableMapOf<String, String>()

    /**
     * Handles player movement to detect structure entry
     *
     * This method is called on MONITOR priority (after other plugins) to ensure
     * we don't interfere with other movement-related plugins.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerMove(event: PlayerMoveEvent) {
        // Only check if player moved to a new block
        val from = event.from
        val to = event.to ?: return

        if (from.blockX == to.blockX && from.blockY == to.blockY && from.blockZ == to.blockZ) {
            return
        }

        val player = event.player
        val location = to

        // Only check in the Nether
        if (!location.world.environment.name.equals("NETHER", ignoreCase = true)) {
            // If player left the Nether, clear their current structure
            playerCurrentStructure.remove(player.uniqueId.toString())
            return
        }

        // Check for structures at player's location using registry
        val chunk = location.chunk
        val registry = Registry.STRUCTURE

        // Get all structure types we care about
        val bastionRemnant = registry.get(NamespacedKey.minecraft("bastion_remnant"))
        val fortress = registry.get(NamespacedKey.minecraft("fortress"))

        // Check if player is in either structure
        var structureType: StructureType? = null

        if (bastionRemnant != null && chunk.getStructures(bastionRemnant).isNotEmpty()) {
            structureType = StructureType.BASTION_REMNANT
        } else if (fortress != null && chunk.getStructures(fortress).isNotEmpty()) {
            structureType = StructureType.FORTRESS
        }

        if (structureType == null) {
            // Player is not in any tracked structure
            val previousStructure = playerCurrentStructure.remove(player.uniqueId.toString())
            if (previousStructure != null) {
                player.sendMessage(
                    Component.text("You have left the structure.")
                        .color(NamedTextColor.GRAY)
                )
            }
            return
        }

        val chunkX = chunk.x
        val chunkZ = chunk.z
        val worldName = location.world.name

        // Create a key for tracking which structure type the player is in
        // This prevents spam when moving between chunks within the same structure
        val structureKey = "$worldName:${structureType.name}"

        // Check if player is still in the same structure type (ignoring chunk changes)
        val previousStructure = playerCurrentStructure[player.uniqueId.toString()]
        if (previousStructure == structureKey) {
            // Player is still in the same structure, no need to show messages
            return
        }

        // Player entered a new structure type
        playerCurrentStructure[player.uniqueId.toString()] = structureKey

        // Get or create structure record
        val structureData = database.getOrCreateStructure(
            worldName = worldName,
            structureType = structureType,
            chunkX = chunkX,
            chunkZ = chunkZ,
            discoveredBy = player.uniqueId
        )

        // Notify player
        val wasNewlyDiscovered = structureData.discoveredBy == player.uniqueId &&
                (System.currentTimeMillis() - structureData.discoveredAt) < 1000

        if (wasNewlyDiscovered) {
            player.sendMessage(
                Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    .color(NamedTextColor.GOLD)
            )
            player.sendMessage(
                Component.text("  ✦ New Structure Discovered! ✦")
                    .color(NamedTextColor.GOLD)
                    .decorate(TextDecoration.BOLD)
            )
            player.sendMessage(
                Component.text("  Type: ${structureType.displayName}")
                    .color(NamedTextColor.YELLOW)
            )
            player.sendMessage(
                Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    .color(NamedTextColor.GOLD)
            )
        } else {
            player.sendMessage(
                Component.text("Entered ${structureType.displayName}")
                    .color(NamedTextColor.YELLOW)
            )
        }
    }
}
