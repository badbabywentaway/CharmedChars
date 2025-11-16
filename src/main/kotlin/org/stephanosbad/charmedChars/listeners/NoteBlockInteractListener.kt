package org.stephanosbad.charmedChars.listeners

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.stephanosbad.charmedChars.CharmedChars

/**
 * Prevents custom note blocks from being interacted with (prevents note cycling)
 * Custom letter/number blocks should not change their instrument/note when clicked
 */
class NoteBlockInteractListener(private val plugin: CharmedChars) : Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        // Only care about right-clicking blocks
        if (event.action != Action.RIGHT_CLICK_BLOCK) return

        val clickedBlock = event.clickedBlock ?: return

        // Only care about note blocks
        if (clickedBlock.type != Material.NOTE_BLOCK) return

        // Check if this is a custom block by checking if it drops an item with custom model data
        val drops = clickedBlock.drops
        if (drops.isNotEmpty()) {
            val drop = drops.first()
            if (drop.hasItemMeta() && drop.itemMeta.hasCustomModelData()) {
                // This is a custom block - cancel the interaction to prevent note cycling
                event.isCancelled = true
                plugin.logger.fine("Blocked interaction with custom note block at ${clickedBlock.location}")
            }
        }
    }
}
