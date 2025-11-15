package org.stephanosbad.charmedChars.listeners

import org.bukkit.Material
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.stephanosbad.charmedChars.CharmedChars

/**
 * Handles placing custom character blocks
 * Sets the note block's instrument and note based on custom model data
 */
class BlockPlaceListener(private val plugin: CharmedChars) : Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    fun onBlockPlace(event: BlockPlaceEvent) {
        val itemInHand = event.itemInHand

        // Only process note blocks with custom model data
        if (itemInHand.type != Material.NOTE_BLOCK) return
        if (!itemInHand.hasItemMeta()) return

        val meta = itemInHand.itemMeta
        if (!meta.hasCustomModelData()) return

        val customModelData = meta.customModelData
        val placedBlock = event.blockPlaced

        // Set the note block's instrument and note based on custom model data
        // This allows the resource pack to show different textures via blockstates
        if (placedBlock.blockData is NoteBlock) {
            val noteBlockData = placedBlock.blockData as NoteBlock

            // Map custom model data (1100-1399+) to instrument/note combinations
            // Note blocks have 25 notes (0-24) and multiple instruments
            // We'll use the custom model data to determine which combination to use

            val relativeValue = customModelData - 1100  // Offset to 0-based
            val note = relativeValue % 25  // 25 possible notes
            val instrumentIndex = (relativeValue / 25) % NoteBlock.Instrument.values().size

            noteBlockData.note = org.bukkit.Note(note)
            noteBlockData.instrument = NoteBlock.Instrument.values()[instrumentIndex]

            placedBlock.blockData = noteBlockData

            plugin.logger.fine("Placed custom block with CMD=$customModelData -> instrument=${noteBlockData.instrument}, note=${noteBlockData.note.id}")
        }
    }
}
