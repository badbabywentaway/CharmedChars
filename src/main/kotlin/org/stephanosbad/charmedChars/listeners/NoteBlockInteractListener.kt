package org.stephanosbad.charmedChars.listeners

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.block.NotePlayEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.stephanosbad.charmedChars.CharmedChars
import org.stephanosbad.charmedChars.block.CustomBlockEngine
import java.util.concurrent.ConcurrentHashMap

/**
 * Prevents custom note blocks from being interacted with (prevents note cycling)
 * Custom letter/number blocks should not change their instrument/note when clicked
 */
class NoteBlockInteractListener(private val plugin: CharmedChars) : Listener {

    // Track custom noteblocks by location -> customModelData
    private val customBlocks = ConcurrentHashMap<Location, Int>()

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBlockPlaceMonitor(event: BlockPlaceEvent) {
        // Track placed custom blocks
        val itemInHand = event.itemInHand
        if (itemInHand.type != Material.NOTE_BLOCK) return
        if (!itemInHand.hasItemMeta()) return

        val meta = itemInHand.itemMeta
        if (!meta.hasCustomModelData()) return

        val customModelData = meta.customModelData
        val location = event.blockPlaced.location

        customBlocks[location] = customModelData
        plugin.logger.info("[NoteBlock] Tracked custom block CMD=$customModelData at $location")
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        // Handle both left and right clicks on blocks
        if (event.action != Action.RIGHT_CLICK_BLOCK && event.action != Action.LEFT_CLICK_BLOCK) return

        val clickedBlock = event.clickedBlock ?: return

        // Only care about note blocks
        if (clickedBlock.type != Material.NOTE_BLOCK) return

        // Check if this location is tracked as a custom block
        val customModelData = customBlocks[clickedBlock.location]
        if (customModelData != null) {
            // This is a custom block - cancel the interaction to prevent note cycling
            event.isCancelled = true
            plugin.logger.info("[NoteBlock] Blocked interaction with tracked custom block CMD=$customModelData at ${clickedBlock.location}")
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    fun onNotePlay(event: NotePlayEvent) {
        // Prevent custom note blocks from playing sounds and changing notes
        val block = event.block

        if (block.type != Material.NOTE_BLOCK) return

        // Check if this location is tracked as a custom block
        val customModelData = customBlocks[block.location]
        if (customModelData != null) {
            // This is a custom block - cancel the note play event
            event.isCancelled = true
            plugin.logger.info("[NoteBlock] Blocked note play for tracked custom block CMD=$customModelData at ${block.location}")
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    fun onBlockPhysicsMonitor(event: BlockPhysicsEvent) {
        // MONITOR priority runs LAST - restore block data if it was changed
        val block = event.block

        if (block.type != Material.NOTE_BLOCK) return

        // Check if this location is tracked as a custom block
        val originalCMD = customBlocks[block.location] ?: return

        val currentData = block.blockData as? NoteBlock ?: return
        val currentNote = currentData.note.id
        val currentInstrument = currentData.instrument

        plugin.logger.info("[NoteBlock Debug] BlockPhysicsEvent MONITOR for tracked block CMD=$originalCMD at ${block.location}, current: note=$currentNote, instrument=$currentInstrument")

        // Calculate what the note/instrument SHOULD be from the ORIGINAL custom model data
        val relativeValue = originalCMD - 1100
        val expectedNote = (relativeValue % 25).toByte()
        val expectedInstrumentIndex = (relativeValue / 25) % org.bukkit.Instrument.values().size
        val expectedInstrument = org.bukkit.Instrument.values()[expectedInstrumentIndex]

        plugin.logger.info("[NoteBlock Debug] Expected: note=$expectedNote, instrument=$expectedInstrument | Current: note=$currentNote, instrument=$currentInstrument")

        if (currentNote != expectedNote || currentInstrument != expectedInstrument) {
            plugin.logger.warning("[NoteBlock] Custom block data was modified! Current: note=$currentNote, instrument=$currentInstrument, Expected: note=$expectedNote, instrument=$expectedInstrument. Scheduling restore...")

            // Create the correct block data
            val correctData = block.blockData.clone() as NoteBlock
            correctData.note = org.bukkit.Note(expectedNote.toInt())
            correctData.instrument = expectedInstrument

            // Restore on next tick to ensure it takes effect after all event processing
            Bukkit.getScheduler().runTask(plugin, Runnable {
                if (block.type == Material.NOTE_BLOCK) {
                    block.blockData = correctData
                    plugin.logger.info("[NoteBlock] Restored block data to note=$expectedNote, instrument=$expectedInstrument at ${block.location}")
                }
            })
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    fun onBlockBreak(event: BlockBreakEvent) {
        // Ensure custom note blocks drop the correct item with custom model data
        val block = event.block

        if (block.type != Material.NOTE_BLOCK) return

        plugin.logger.info("[NoteBlock Debug] BlockBreakEvent HIGHEST for note block at ${block.location}, cancelled=${event.isCancelled}")

        // Check if this location is tracked as a custom block
        val customModelData = customBlocks[block.location]
        if (customModelData != null) {
            plugin.logger.info("[NoteBlock] Processing break for tracked custom block CMD=$customModelData")

            // This is a custom block - prevent default behavior and handle it ourselves
            event.isCancelled = true
            event.isDropItems = false

            plugin.logger.info("[NoteBlock Debug] Event cancelled, creating and dropping item")

            // Create the item with the original custom model data
            val itemStack = org.bukkit.inventory.ItemStack(Material.NOTE_BLOCK)
            val meta = itemStack.itemMeta
            meta.setCustomModelData(customModelData)
            itemStack.itemMeta = meta

            // Drop the item
            block.world.dropItemNaturally(block.location, itemStack)
            plugin.logger.info("[NoteBlock] Dropped custom item CMD=$customModelData")

            // Remove from tracking
            customBlocks.remove(block.location)

            // Manually remove the block on next tick to ensure event cancellation takes effect
            Bukkit.getScheduler().runTask(plugin, Runnable {
                if (block.type == Material.NOTE_BLOCK) {
                    block.type = Material.AIR
                    plugin.logger.info("[NoteBlock] Removed block at ${block.location}")
                }
            })
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onBlockBreakMonitor(event: BlockBreakEvent) {
        // Clean up tracking even if the event was cancelled or handled by someone else
        if (event.block.type == Material.NOTE_BLOCK) {
            customBlocks.remove(event.block.location)
        }
    }
}
