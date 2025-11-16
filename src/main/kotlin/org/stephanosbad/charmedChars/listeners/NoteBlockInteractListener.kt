package org.stephanosbad.charmedChars.listeners

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.block.NotePlayEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.stephanosbad.charmedChars.CharmedChars
import org.stephanosbad.charmedChars.block.CustomBlockEngine

/**
 * Prevents custom note blocks from being interacted with (prevents note cycling)
 * Custom letter/number blocks should not change their instrument/note when clicked
 */
class NoteBlockInteractListener(private val plugin: CharmedChars) : Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        // Handle both left and right clicks on blocks
        if (event.action != Action.RIGHT_CLICK_BLOCK && event.action != Action.LEFT_CLICK_BLOCK) return

        val clickedBlock = event.clickedBlock ?: return

        // Only care about note blocks
        if (clickedBlock.type != Material.NOTE_BLOCK) return

        plugin.logger.info("[NoteBlock Debug] Player interacted with note block at ${clickedBlock.location}")

        // Check if this is a custom block using the CustomBlockEngine
        val customBlock = CustomBlockEngine.byAlreadyPlaced(clickedBlock)
        plugin.logger.info("[NoteBlock Debug] CustomBlock detection result: ${if (customBlock != null) "FOUND" else "NULL"}")

        if (customBlock != null) {
            // This is a custom block - cancel the interaction to prevent note cycling
            event.isCancelled = true
            val blockChar = customBlock.id?.character ?: customBlock.nonId?.nonAlphaNumBlockName ?: customBlock.numberId?.c?.toString() ?: "unknown"
            plugin.logger.info("[NoteBlock] Blocked interaction with custom note block '$blockChar' at ${clickedBlock.location}")
        } else {
            plugin.logger.info("[NoteBlock Debug] Not a custom block, allowing interaction")
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    fun onNotePlay(event: NotePlayEvent) {
        // Prevent custom note blocks from playing sounds and changing notes
        val block = event.block

        if (block.type != Material.NOTE_BLOCK) return

        plugin.logger.info("[NoteBlock Debug] NotePlayEvent triggered at ${block.location}")

        // Check if this is a custom block using the CustomBlockEngine
        val customBlock = CustomBlockEngine.byAlreadyPlaced(block)
        plugin.logger.info("[NoteBlock Debug] CustomBlock detection in NotePlay: ${if (customBlock != null) "FOUND" else "NULL"}")

        if (customBlock != null) {
            // This is a custom block - cancel the note play event
            event.isCancelled = true
            val blockChar = customBlock.id?.character ?: customBlock.nonId?.nonAlphaNumBlockName ?: customBlock.numberId?.c?.toString() ?: "unknown"
            plugin.logger.info("[NoteBlock] Blocked note play for custom note block '$blockChar' at ${block.location}")
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    fun onBlockPhysics(event: BlockPhysicsEvent) {
        // Prevent custom note blocks from changing when blocks are placed next to them
        // LOWEST priority = runs FIRST, before Minecraft processes the physics update
        val block = event.block

        if (block.type != Material.NOTE_BLOCK) return

        plugin.logger.info("[NoteBlock Debug] BlockPhysicsEvent for note block at ${block.location}")

        // Check if this is a custom block using the CustomBlockEngine
        val customBlock = CustomBlockEngine.byAlreadyPlaced(block)
        plugin.logger.info("[NoteBlock Debug] CustomBlock detection in BlockPhysics: ${if (customBlock != null) "FOUND" else "NULL"}")

        if (customBlock != null) {
            // This is a custom block - cancel the physics event to prevent state changes
            event.isCancelled = true
            val blockChar = customBlock.id?.character ?: customBlock.nonId?.nonAlphaNumBlockName ?: customBlock.numberId?.c?.toString() ?: "unknown"
            plugin.logger.info("[NoteBlock] Blocked physics update for custom note block '$blockChar' at ${block.location}")
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    fun onBlockBreak(event: BlockBreakEvent) {
        // Ensure custom note blocks drop the correct item with custom model data
        val block = event.block

        if (block.type != Material.NOTE_BLOCK) return

        plugin.logger.info("[NoteBlock Debug] BlockBreakEvent for note block at ${block.location}")

        // Check if this is a custom block using the CustomBlockEngine
        val customBlock = CustomBlockEngine.byAlreadyPlaced(block)
        plugin.logger.info("[NoteBlock Debug] CustomBlock detection in BlockBreak: ${if (customBlock != null) "FOUND" else "NULL"}")

        if (customBlock != null) {
            // This is a custom block - cancel default drops and drop the custom item
            event.isDropItems = false

            val itemStack = customBlock.itemStack
            if (itemStack != null) {
                block.world.dropItemNaturally(block.location, itemStack)
                val blockChar = customBlock.id?.character ?: customBlock.nonId?.nonAlphaNumBlockName ?: customBlock.numberId?.c?.toString() ?: "unknown"
                plugin.logger.info("[NoteBlock] Dropped custom item for note block '$blockChar' at ${block.location}")
            } else {
                plugin.logger.warning("[NoteBlock] CustomBlock itemStack is null for block at ${block.location}")
            }
        }
    }
}
