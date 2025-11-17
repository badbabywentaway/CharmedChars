package org.stephanosbad.charmedChars.listeners

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.BlockPosition
import com.comphenix.protocol.wrappers.WrappedBlockData
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.persistence.PersistentDataType
import org.stephanosbad.charmedChars.CharmedChars

/**
 * Uses ProtocolLib to send fake block packets to clients
 * Custom noteblocks appear as barrier blocks to prevent interaction
 */
class FakeBlockListener(private val plugin: CharmedChars) : Listener {

    private val protocolManager = ProtocolLibrary.getProtocolManager()
    private val trackedChunks = mutableSetOf<Chunk>()

    /**
     * Get the custom model data stored in PDC for a block
     */
    private fun getCustomModelData(block: Block): Int? {
        val chunk = block.chunk
        val key = NamespacedKey(plugin, "noteblock_${block.x}_${block.y}_${block.z}")
        return chunk.persistentDataContainer.get(key, PersistentDataType.INTEGER)
    }

    init {
        // Register packet listener to intercept block change packets
        protocolManager.addPacketListener(object : PacketAdapter(
            plugin,
            ListenerPriority.NORMAL,
            PacketType.Play.Server.BLOCK_CHANGE,
            PacketType.Play.Server.MULTI_BLOCK_CHANGE
        ) {
            override fun onPacketSending(event: PacketEvent) {
                // Don't modify packets we're sending ourselves
                if (event.isPlayerTemporary) return

                try {
                    when (event.packetType) {
                        PacketType.Play.Server.BLOCK_CHANGE -> handleBlockChange(event)
                        PacketType.Play.Server.MULTI_BLOCK_CHANGE -> handleMultiBlockChange(event)
                    }
                } catch (e: Exception) {
                    plugin.logger.warning("[FakeBlock] Error handling packet: ${e.message}")
                    e.printStackTrace()
                }
            }
        })

        plugin.logger.info("[FakeBlock] ProtocolLib integration initialized")
    }

    /**
     * Handle single block change packets
     */
    private fun handleBlockChange(event: PacketEvent) {
        val packet = event.packet
        val blockPosition = packet.blockPositionModifier.read(0)
        val world = event.player.world

        val block = world.getBlockAt(blockPosition.x, blockPosition.y, blockPosition.z)
        if (block.type != Material.NOTE_BLOCK) return

        // Check if this is a custom block
        if (getCustomModelData(block) != null) {
            // Modify the packet to show a barrier block instead
            packet.blockData.write(0, WrappedBlockData.createData(Material.BARRIER))
            plugin.logger.fine("[FakeBlock] Modified BLOCK_CHANGE packet for custom noteblock at ${blockPosition}")
        }
    }

    /**
     * Handle multi-block change packets (chunk updates)
     */
    private fun handleMultiBlockChange(event: PacketEvent) {
        // This is more complex - would need to iterate through all blocks in the chunk section
        // For now, we'll rely on individual block updates and chunk resends
    }

    /**
     * Send fake blocks for all custom noteblocks in a chunk to a player
     */
    fun sendFakeBlocksForChunk(player: Player, chunk: Chunk) {
        val world = chunk.world
        val chunkX = chunk.x * 16
        val chunkZ = chunk.z * 16

        // Scan the chunk for custom noteblocks
        for (x in 0..15) {
            for (z in 0..15) {
                for (y in world.minHeight..world.maxHeight) {
                    val block = chunk.getBlock(x, y, z)
                    if (block.type == Material.NOTE_BLOCK) {
                        if (getCustomModelData(block) != null) {
                            sendFakeBlock(player, block, Material.BARRIER)
                        }
                    }
                }
            }
        }
    }

    /**
     * Send a fake block packet to a player
     */
    private fun sendFakeBlock(player: Player, block: Block, fakeType: Material) {
        try {
            val packet = PacketContainer(PacketType.Play.Server.BLOCK_CHANGE)
            packet.blockPositionModifier.write(0, BlockPosition(block.x, block.y, block.z))
            packet.blockData.write(0, WrappedBlockData.createData(fakeType))

            protocolManager.sendServerPacket(player, packet)
            plugin.logger.fine("[FakeBlock] Sent fake block (${fakeType}) to ${player.name} at ${block.location}")
        } catch (e: Exception) {
            plugin.logger.warning("[FakeBlock] Failed to send fake block: ${e.message}")
        }
    }

    /**
     * Restore real blocks for a chunk (show noteblocks again)
     */
    fun restoreRealBlocksForChunk(player: Player, chunk: Chunk) {
        val world = chunk.world
        for (x in 0..15) {
            for (z in 0..15) {
                for (y in world.minHeight..world.maxHeight) {
                    val block = chunk.getBlock(x, y, z)
                    if (block.type == Material.NOTE_BLOCK) {
                        if (getCustomModelData(block) != null) {
                            // Send the real block data
                            val packet = PacketContainer(PacketType.Play.Server.BLOCK_CHANGE)
                            packet.blockPositionModifier.write(0, BlockPosition(block.x, block.y, block.z))
                            packet.blockData.write(0, WrappedBlockData.createData(block.blockData))
                            protocolManager.sendServerPacket(player, packet)
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onChunkLoad(event: ChunkLoadEvent) {
        // When a chunk loads, send fake blocks to all online players
        val chunk = event.chunk
        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            Bukkit.getOnlinePlayers().forEach { player ->
                if (player.world == chunk.world) {
                    sendFakeBlocksForChunk(player, chunk)
                }
            }
        }, 1L) // Delay 1 tick to ensure chunk is fully loaded
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        // Send fake blocks for all loaded chunks when player joins
        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            player.world.loadedChunks.forEach { chunk ->
                sendFakeBlocksForChunk(player, chunk)
            }
        }, 20L) // Delay 1 second to ensure player is fully loaded
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerMove(event: PlayerMoveEvent) {
        // Only check when player moves to a new chunk
        val from = event.from
        val to = event.to ?: return

        if (from.chunk != to.chunk) {
            // Player entered a new chunk - send fake blocks
            sendFakeBlocksForChunk(event.player, to.chunk)
        }
    }

    /**
     * Send a fake block for a specific location to all nearby players
     */
    fun sendFakeBlockToNearbyPlayers(block: Block, fakeType: Material) {
        val location = block.location
        location.world.players.forEach { player ->
            if (player.location.distance(location) < 128) { // Render distance check
                sendFakeBlock(player, block, fakeType)
            }
        }
    }
}
