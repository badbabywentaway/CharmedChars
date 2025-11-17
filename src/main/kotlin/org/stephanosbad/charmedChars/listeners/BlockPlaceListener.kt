package org.stephanosbad.charmedChars.listeners

import org.bukkit.Bukkit
import org.bukkit.Instrument
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Note
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.persistence.PersistentDataType
import org.stephanosbad.charmedChars.CharmedChars

/**
 * Handles placing custom character blocks
 * Sets the note block's instrument and note based on custom model data
 */
class BlockPlaceListener(private val plugin: CharmedChars) : Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
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

            // Store custom model data in PDC for this block
            val chunk = placedBlock.chunk
            val key = NamespacedKey(plugin, "noteblock_${placedBlock.x}_${placedBlock.y}_${placedBlock.z}")
            chunk.persistentDataContainer.set(key, PersistentDataType.INTEGER, customModelData)
            plugin.logger.info("[NoteBlock PDC] Stored CMD=$customModelData for block at ${placedBlock.location}")

            // Schedule a task to re-apply the block data on the next tick
            // This ensures our changes stick even if Minecraft modifies it based on the block below
            Bukkit.getScheduler().runTask(plugin, Runnable {
                if (placedBlock.type == Material.NOTE_BLOCK) {
                    val finalData = placedBlock.blockData as NoteBlock
                    finalData.note = Note(note)
                    finalData.instrument = Instrument.values()[instrumentIndex]
                    placedBlock.blockData = finalData
                    plugin.logger.info("[BlockPlace] Re-applied block data on next tick: instrument=${finalData.instrument}, note=${finalData.note.id}")
                }
            })
        } else {
            plugin.logger.warning("[BlockPlace] Block placed is not a NoteBlock! Type: ${placedBlock.type}, BlockData: ${placedBlock.blockData}")
        }
    }
}
