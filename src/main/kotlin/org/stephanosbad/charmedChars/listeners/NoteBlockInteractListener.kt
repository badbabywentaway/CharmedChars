package org.stephanosbad.charmedChars.listeners

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.data.type.NoteBlock
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    fun onBlockPhysicsMonitor(event: BlockPhysicsEvent) {
        // MONITOR priority runs LAST - restore block data if it was changed
        val block = event.block

        if (block.type != Material.NOTE_BLOCK) return

        val currentData = block.blockData as? NoteBlock ?: return
        val currentNote = currentData.note.id
        val currentInstrument = currentData.instrument

        plugin.logger.info("[NoteBlock Debug] BlockPhysicsEvent MONITOR for note block at ${block.location}, note=$currentNote, instrument=$currentInstrument")

        // Check if this is a custom block using the CustomBlockEngine
        val customBlock = CustomBlockEngine.byAlreadyPlaced(block)

        if (customBlock != null) {
            val blockChar = customBlock.id?.character ?: customBlock.nonId?.nonAlphaNumBlockName ?: customBlock.numberId?.c?.toString() ?: "unknown"
            plugin.logger.info("[NoteBlock Debug] Custom block '$blockChar' detected in MONITOR")

            // Calculate what the note/instrument SHOULD be from the custom model data
            val itemStack = customBlock.itemStack
            if (itemStack != null && itemStack.hasItemMeta()) {
                val meta = itemStack.itemMeta
                if (meta.hasCustomModelData()) {
                    val customModelData = meta.customModelData
                    val relativeValue = customModelData - 1100
                    val expectedNote = relativeValue % 25
                    val expectedInstrumentIndex = (relativeValue / 25) % org.bukkit.Instrument.values().size
                    val expectedInstrument = org.bukkit.Instrument.values()[expectedInstrumentIndex]

                    plugin.logger.info("[NoteBlock Debug] Expected: note=$expectedNote, instrument=$expectedInstrument | Current: note=$currentNote, instrument=$currentInstrument")

                    if (currentNote != expectedNote || currentInstrument != expectedInstrument) {
                        plugin.logger.warning("[NoteBlock] Custom block data was modified! Current: note=$currentNote, instrument=$currentInstrument, Expected: note=$expectedNote, instrument=$expectedInstrument. Scheduling restore...")

                        // Create the correct block data
                        val correctData = block.blockData.clone() as NoteBlock
                        correctData.note = org.bukkit.Note(expectedNote)
                        correctData.instrument = expectedInstrument

                        // Restore on next tick to ensure it takes effect after all event processing
                        Bukkit.getScheduler().runTask(plugin, Runnable {
                            if (block.type == Material.NOTE_BLOCK) {
                                block.blockData = correctData
                                plugin.logger.info("[NoteBlock] Restored block data for '$blockChar' to note=$expectedNote, instrument=$expectedInstrument at ${block.location}")
                            }
                        })
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    fun onBlockBreak(event: BlockBreakEvent) {
        // Ensure custom note blocks drop the correct item with custom model data
        val block = event.block

        if (block.type != Material.NOTE_BLOCK) return

        plugin.logger.info("[NoteBlock Debug] BlockBreakEvent for note block at ${block.location}, cancelled=${event.isCancelled}")

        // Check if this is a custom block using the CustomBlockEngine
        val customBlock = CustomBlockEngine.byAlreadyPlaced(block)
        plugin.logger.info("[NoteBlock Debug] CustomBlock detection in BlockBreak: ${if (customBlock != null) "FOUND" else "NULL"}")

        if (customBlock != null) {
            val blockChar = customBlock.id?.character ?: customBlock.nonId?.nonAlphaNumBlockName ?: customBlock.numberId?.c?.toString() ?: "unknown"
            plugin.logger.info("[NoteBlock] Processing break for custom block '$blockChar'")

            // This is a custom block - prevent default behavior and handle it ourselves
            event.isCancelled = true
            event.isDropItems = false

            plugin.logger.info("[NoteBlock Debug] Event cancelled, dropping item and removing block")

            // Drop the custom item
            val itemStack = customBlock.itemStack
            if (itemStack != null) {
                block.world.dropItemNaturally(block.location, itemStack)
                plugin.logger.info("[NoteBlock] Dropped custom item for '$blockChar'")
            } else {
                plugin.logger.warning("[NoteBlock] CustomBlock itemStack is null")
            }

            // Manually remove the block on next tick to ensure event cancellation takes effect
            Bukkit.getScheduler().runTask(plugin, Runnable {
                if (block.type == Material.NOTE_BLOCK) {
                    block.type = Material.AIR
                    plugin.logger.info("[NoteBlock] Removed block '$blockChar' at ${block.location}")
                }
            })
        }
    }
}
