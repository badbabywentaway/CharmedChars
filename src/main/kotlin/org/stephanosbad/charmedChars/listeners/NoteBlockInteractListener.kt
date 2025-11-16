package org.stephanosbad.charmedChars.listeners

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.NotePlayEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.stephanosbad.charmedChars.CharmedChars
import org.stephanosbad.charmedChars.block.CustomBlockEngine

/**
 * Prevents custom note blocks from being interacted with (prevents note cycling)
 * Custom letter/number blocks should not change their instrument/note when clicked
 */
class NoteBlockInteractListener(private val plugin: CharmedChars) : Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        // Handle both left and right clicks on blocks
        if (event.action != Action.RIGHT_CLICK_BLOCK && event.action != Action.LEFT_CLICK_BLOCK) return

        val clickedBlock = event.clickedBlock ?: return

        // Only care about note blocks
        if (clickedBlock.type != Material.NOTE_BLOCK) return

        // Check if this is a custom block using the CustomBlockEngine
        val customBlock = CustomBlockEngine.byAlreadyPlaced(clickedBlock)
        if (customBlock != null) {
            // This is a custom block - cancel the interaction to prevent note cycling
            event.isCancelled = true
            val blockChar = customBlock.id?.character ?: customBlock.nonId?.nonAlphaNumBlockName ?: customBlock.numberId?.c?.toString() ?: "unknown"
            plugin.logger.fine("Blocked interaction with custom note block '$blockChar' at ${clickedBlock.location}")
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onNotePlay(event: NotePlayEvent) {
        // Prevent custom note blocks from playing sounds and changing notes
        val block = event.block

        if (block.type != Material.NOTE_BLOCK) return

        // Check if this is a custom block using the CustomBlockEngine
        val customBlock = CustomBlockEngine.byAlreadyPlaced(block)
        if (customBlock != null) {
            // This is a custom block - cancel the note play event
            event.isCancelled = true
            val blockChar = customBlock.id?.character ?: customBlock.nonId?.nonAlphaNumBlockName ?: customBlock.numberId?.c?.toString() ?: "unknown"
            plugin.logger.fine("Blocked note play for custom note block '$blockChar' at ${block.location}")
        }
    }
}
