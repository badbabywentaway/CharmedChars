package org.stephanosbad.charmedChars.listeners

import org.bukkit.Bukkit
import org.bukkit.Instrument
import org.bukkit.Material
import org.bukkit.Note
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

    @EventHandler(priority = EventPriority.LOWEST)
    fun onBlockPlace(event: BlockPlaceEvent) {
        val itemInHand = event.itemInHand

        plugin.logger.info("[BlockPlace Debug] Block place event: ${event.block.type} at ${event.block.location}")

        // Only process note blocks with custom model data
        if (itemInHand.type != Material.NOTE_BLOCK) return
        if (!itemInHand.hasItemMeta()) {
            plugin.logger.info("[BlockPlace Debug] Note block has no item meta")
            return
        }

        val meta = itemInHand.itemMeta
        if (!meta.hasCustomModelData()) {
            plugin.logger.info("[BlockPlace Debug] Note block has no custom model data")
            return
        }

        val customModelData = meta.customModelData
        val placedBlock = event.blockPlaced

        plugin.logger.info("[BlockPlace Debug] Placing custom note block with CMD=$customModelData at ${placedBlock.location}")

        // Set the note block's instrument and note based on custom model data
        // This allows the resource pack to show different textures via blockstates
        if (placedBlock.blockData is NoteBlock) {
            val noteBlockData = placedBlock.blockData as NoteBlock

            // Map custom model data (1100-1399+) to instrument/note combinations
            // Note blocks have 25 notes (0-24) and multiple instruments
            // We'll use the custom model data to determine which combination to use

            val relativeValue = customModelData - 1100  // Offset to 0-based
            val note = relativeValue % 25  // 25 possible notes
            val instrumentIndex = (relativeValue / 25) % Instrument.values().size

            noteBlockData.note = Note(note)
            noteBlockData.instrument = Instrument.values()[instrumentIndex]

            placedBlock.blockData = noteBlockData

            plugin.logger.info("[BlockPlace] Placed custom block: CMD=$customModelData -> instrument=${noteBlockData.instrument}, note=${noteBlockData.note.id} (relativeValue=$relativeValue) at ${placedBlock.location}")

            // Verify the data was set correctly
            val verifyData = placedBlock.blockData as NoteBlock
            plugin.logger.info("[BlockPlace Debug] Verification - instrument=${verifyData.instrument}, note=${verifyData.note.id}")

            // Note: Fake blocks disabled - event cancellation handles interaction prevention
            // The resource pack shows the correct texture based on note/instrument data
        } else {
            plugin.logger.warning("[BlockPlace] Block placed is not a NoteBlock! Type: ${placedBlock.type}, BlockData: ${placedBlock.blockData}")
        }
    }
}
