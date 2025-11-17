package org.stephanosbad.charmedChars.listeners

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.EnumWrappers
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.persistence.PersistentDataType
import org.stephanosbad.charmedChars.CharmedChars

/**
 * Uses ProtocolLib to intercept player interaction packets
 * Prevents custom noteblocks from being clicked/interacted with at the packet level
 */
class ProtocolLibInteractionListener(private val plugin: CharmedChars) {

    private val protocolManager = ProtocolLibrary.getProtocolManager()

    /**
     * Get the custom model data stored in PDC for a block
     */
    private fun getCustomModelData(block: Block): Int? {
        val chunk = block.chunk
        val key = NamespacedKey(plugin, "noteblock_${block.x}_${block.y}_${block.z}")
        return chunk.persistentDataContainer.get(key, PersistentDataType.INTEGER)
    }

    init {
        // Intercept client-side block interaction packets to prevent note cycling
        protocolManager.addPacketListener(object : PacketAdapter(
            plugin,
            ListenerPriority.HIGHEST,
            PacketType.Play.Client.USE_ITEM,
            PacketType.Play.Client.BLOCK_PLACE
        ) {
            override fun onPacketReceiving(event: PacketEvent) {
                try {
                    when (event.packetType) {
                        PacketType.Play.Client.BLOCK_PLACE -> handleBlockPlace(event)
                        PacketType.Play.Client.USE_ITEM -> handleUseItem(event)
                    }
                } catch (e: Exception) {
                    plugin.logger.warning("[ProtocolLib] Error handling interaction packet: ${e.message}")
                    e.printStackTrace()
                }
            }
        })

        plugin.logger.info("[ProtocolLib] Packet-level interaction prevention initialized")
    }

    /**
     * Handle BLOCK_PLACE packets (includes right-clicking blocks)
     */
    private fun handleBlockPlace(event: PacketEvent) {
        val packet = event.packet
        val player = event.player

        try {
            // Get the block position from the packet
            val blockPosition = packet.blockPositionModifier.readSafely(0) ?: return
            val world = player.world
            val block = world.getBlockAt(blockPosition.x, blockPosition.y, blockPosition.z)

            // Check if this is a custom noteblock
            if (block.type == Material.NOTE_BLOCK && getCustomModelData(block) != null) {
                val customModelData = getCustomModelData(block)
                event.isCancelled = true
                plugin.logger.info("[ProtocolLib] Blocked BLOCK_PLACE packet for custom noteblock CMD=$customModelData at ${block.location}")
            }
        } catch (e: Exception) {
            // Silently fail - packet structure might not match expected format
            plugin.logger.fine("[ProtocolLib] Could not process BLOCK_PLACE packet: ${e.message}")
        }
    }

    /**
     * Handle USE_ITEM packets (right-click with item in hand)
     */
    private fun handleUseItem(event: PacketEvent) {
        // This packet doesn't have direct block position info
        // We rely on BLOCK_PLACE for block interactions
        // This is here as a secondary check if needed in the future
    }
}
