package org.stephanosbad.charmedChars.listeners

import org.bukkit.Material
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockExplodeEvent
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

        // Get explosion center and max Y-level
        val explosionCenter = event.block.location
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
