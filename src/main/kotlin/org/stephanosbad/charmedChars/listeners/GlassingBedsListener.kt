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

        // Track this player as the trigger for this bed location
        val location = clickedBlock.location
        val locationKey = "${location.world.name}:${location.blockX}:${location.blockY}:${location.blockZ}"
        bedTriggers[locationKey] = event.player.uniqueId.toString()
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

        // Check if player has activation (if we know who triggered it)
        if (player != null && !OperatorActivationListener.isPlayerActivated(player)) {
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
