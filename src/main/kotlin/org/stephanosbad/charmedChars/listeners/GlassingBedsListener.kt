package org.stephanosbad.charmedChars.listeners

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.stephanosbad.charmedChars.CharmedChars

/**
 * Listener for the Glassing Beds feature
 *
 * When a bed explodes in the Nether or End, this listener converts
 * lava blocks within a 5-block radius to glass blocks, but only if the
 * lava is at or below the configured max Y-level (default: 28).
 * This prevents using beds to glass lava ocean surfaces for easy travel.
 *
 * @property plugin Reference to the main plugin instance
 */
class GlassingBedsListener(
    private val plugin: CharmedChars
) : Listener {

    // Track which player triggered each bed explosion (for activation check)
    // Key: "world:x:y:z", Value: Player UUID
    private val bedTriggers = mutableMapOf<String, String>()

    /**
     * Track which player is triggering a bed explosion
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBedInteract(event: PlayerInteractEvent) {
        // Only check right-click on blocks
        if (event.action != Action.RIGHT_CLICK_BLOCK) return

        val clickedBlock = event.clickedBlock ?: return

        // Check if it's a bed
        if (!clickedBlock.type.name.endsWith("_BED")) return

        // Check if in Nether (where beds explode)
        if (clickedBlock.world.environment != World.Environment.NETHER) return

        val playerUuid = event.player.uniqueId.toString()

        // Track this player for the clicked block
        val location = clickedBlock.location
        val locationKey = "${location.world.name}:${location.blockX}:${location.blockY}:${location.blockZ}"
        bedTriggers[locationKey] = playerUuid

        // Beds are two blocks - track all adjacent blocks in case the other half explodes
        // Check all 6 cardinal directions for the other bed block
        val adjacentOffsets = listOf(
            Pair(1, 0), Pair(-1, 0),   // X axis
            Pair(0, 1), Pair(0, -1)    // Z axis (Y axis not needed for horizontal beds)
        )

        for ((xOffset, zOffset) in adjacentOffsets) {
            val adjacentBlock = clickedBlock.getRelative(xOffset, 0, zOffset)
            if (adjacentBlock.type.name.endsWith("_BED")) {
                val adjacentKey = "${location.world.name}:${location.blockX + xOffset}:${location.blockY}:${location.blockZ + zOffset}"
                bedTriggers[adjacentKey] = playerUuid
                break  // Found the other half, no need to check more
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun onBlockExplode(event: BlockExplodeEvent) {
        // Check if feature is enabled
        if (!plugin.configManager.glassingBedsEnabled) {
            return
        }

        // Get the exploding block state
        val explodedBlock = event.explodedBlockState

        // Check if it's a bed (all bed types end with "_BED")
        if (!explodedBlock.type.name.endsWith("_BED")) {
            return
        }

        // Check if we're in Nether or End
        val world = event.block.world
        val environment = world.environment
        if (environment != World.Environment.NETHER && environment != World.Environment.THE_END) {
            return
        }

        // Get explosion center
        val explosionCenter = event.block.location
        val locationKey = "${explosionCenter.world.name}:${explosionCenter.blockX}:${explosionCenter.blockY}:${explosionCenter.blockZ}"

        // Find the player who triggered this bed explosion
        val playerUuid = bedTriggers.remove(locationKey)
        val player = if (playerUuid != null) {
            plugin.server.getPlayer(java.util.UUID.fromString(playerUuid))
        } else {
            null
        }

        // If we can't identify the player, don't convert lava (safety measure)
        if (player == null) {
            return
        }

        // Check if player has activation
        if (!OperatorActivationListener.isPlayerActivated(player)) {
            // Player hasn't activated glassing beds - explosion happens but no lava conversion
            player.sendMessage(
                Component.text("Glassing beds not activated for this Nether visit!")
                    .color(NamedTextColor.RED)
            )
            player.sendMessage(
                Component.text("Hint: Hit 4 different operator blocks (+−×÷) of the same color")
                    .color(NamedTextColor.GRAY)
            )
            player.sendMessage(
                Component.text("with a gold/pyrite tool to activate!")
                    .color(NamedTextColor.GRAY)
            )
            return
        }

        // Player has activation or no player tracking - convert lava
        val maxY = plugin.configManager.glassingBedsMaxY

        // Scan 5-block cubic radius and convert lava to glass
        var convertedBlocks = 0
        for (x in -5..5) {
            for (y in -5..5) {
                for (z in -5..5) {
                    val block = explosionCenter.block.getRelative(x, y, z)
                    // Only convert lava at or below the configured max Y-level
                    if (block.type == Material.LAVA && block.y <= maxY) {
                        block.type = Material.GLASS
                        convertedBlocks++
                    }
                }
            }
        }

        // Log if any lava was converted
        if (convertedBlocks > 0) {
            plugin.logger.info(
                "Glassing Beds: Converted $convertedBlocks lava blocks to glass " +
                "at ${explosionCenter.blockX}, ${explosionCenter.blockY}, ${explosionCenter.blockZ}"
            )
        }
    }
}
