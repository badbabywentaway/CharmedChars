package org.stephanosbad.charmedChars.listeners

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType
import org.stephanosbad.charmedChars.CharmedChars

/**
 * Handles custom note block protection, state management and breaking
 * Uses PersistentDataContainer to store custom model data in chunks
 *
 * Protection approach:
 * 1. PlayerInteractEvent (LOWEST):
 *    - Denies empty-handed interaction (prevents note cycling)
 *    - Allows interaction when holding blocks (enables placement)
 *
 * 2. BlockPlaceListener (HIGHEST):
 *    - Sets correct note/instrument on placement
 *    - Re-applies state on next tick to fix any Minecraft changes
 *
 * 3. BlockDamageEvent/BlockBreakEvent (HIGHEST):
 *    - Handles breaking with correct custom drops
 *
 * Note: NotePlayEvent and BlockPhysicsEvent are NOT cancelled.
 * This allows block placement to work naturally. Any temporary state changes
 * are corrected by BlockPlaceListener's next-tick re-application.
 */
class NoteBlockInteractListener(private val plugin: CharmedChars) : Listener {

    /**
     * Get the custom model data stored in PDC for a block
     */
    private fun getCustomModelData(block: Block): Int? {
        val chunk = block.chunk
        val key = NamespacedKey(plugin, "noteblock_${block.x}_${block.y}_${block.z}")
        return chunk.persistentDataContainer.get(key, PersistentDataType.INTEGER)
    }

    /**
     * Store custom model data in PDC for a block
     */
    private fun setCustomModelData(block: Block, customModelData: Int) {
        val chunk = block.chunk
        val key = NamespacedKey(plugin, "noteblock_${block.x}_${block.y}_${block.z}")
        chunk.persistentDataContainer.set(key, PersistentDataType.INTEGER, customModelData)
        plugin.logger.info("[NoteBlock PDC] Stored CMD=$customModelData for block at ${block.location}")
    }

    /**
     * Remove custom model data from PDC for a block
     */
    private fun removeCustomModelData(block: Block) {
        val chunk = block.chunk
        val key = NamespacedKey(plugin, "noteblock_${block.x}_${block.y}_${block.z}")
        chunk.persistentDataContainer.remove(key)
        plugin.logger.info("[NoteBlock PDC] Removed data for block at ${block.location}")
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    fun onBlockDamage(event: BlockDamageEvent) {
        // Debug handler to see if blocks are being damaged at all
        val block = event.block
        plugin.logger.info("[NoteBlock Debug] BlockDamageEvent for block type=${block.type} at ${block.location}, instaBreak=${event.instaBreak}, player=${event.player.name}, gameMode=${event.player.gameMode}, cancelled=${event.isCancelled}")

        if (block.type == Material.NOTE_BLOCK) {
            val customModelData = getCustomModelData(block)
            plugin.logger.info("[NoteBlock Debug] BlockDamage on noteblock, CMD=$customModelData")

            // If it's an instant break in creative mode, manually trigger break logic
            if (event.instaBreak && customModelData != null) {
                plugin.logger.info("[NoteBlock Debug] Instant break detected for custom block, manually handling...")

                // Cancel the default damage behavior
                event.isCancelled = true

                // Schedule break handling on next tick
                Bukkit.getScheduler().runTask(plugin, Runnable {
                    handleCustomBlockBreak(block, customModelData, event.player)
                })
            }
        }
    }

    /**
     * Manually handle breaking a custom noteblock
     */
    private fun handleCustomBlockBreak(block: org.bukkit.block.Block, customModelData: Int, player: org.bukkit.entity.Player) {
        if (block.type != Material.NOTE_BLOCK) return

        plugin.logger.info("[NoteBlock] Manually breaking custom block CMD=$customModelData")

        // Create the item with the original custom model data
        val itemStack = org.bukkit.inventory.ItemStack(Material.NOTE_BLOCK)
        val meta = itemStack.itemMeta
        meta.setCustomModelData(customModelData)
        itemStack.itemMeta = meta

        // Drop the item at block location
        block.world.dropItemNaturally(block.location, itemStack)
        plugin.logger.info("[NoteBlock] Dropped custom item CMD=$customModelData")

        // Remove from PDC
        removeCustomModelData(block)

        // Remove the block
        block.type = Material.AIR
        plugin.logger.info("[NoteBlock] Removed block at ${block.location}")
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        // Only handle RIGHT clicks (note cycling)
        // Left clicks are handled by BlockDamageEvent/BlockBreakEvent
        if (event.action != Action.RIGHT_CLICK_BLOCK) return

        val clickedBlock = event.clickedBlock ?: return

        // Only care about note blocks
        if (clickedBlock.type != Material.NOTE_BLOCK) return

        // Check if this block has custom model data stored in PDC
        val customModelData = getCustomModelData(clickedBlock)
        if (customModelData != null) {
            val itemInHand = event.item

            // If player is holding a placeable block, allow event to pass through
            // This allows block placement to work naturally
            // Any state changes to existing blocks will be fixed by BlockPlaceListener re-application
            if (itemInHand != null && itemInHand.type.isBlock) {
                plugin.logger.info("[NoteBlock] Player holding placeable block, allowing event for placement")
                return
            }

            // Player is empty-handed or holding non-block item - deny interaction
            event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY)
            plugin.logger.info("[NoteBlock] Denied interaction with custom block CMD=$customModelData")
        }
    }

    // Note: NotePlayEvent is NOT cancelled to allow block placement to work
    // When you right-click a noteblock with a block in hand:
    // 1. NotePlayEvent fires (we let it pass)
    // 2. Note might change temporarily
    // 3. BlockPlaceEvent fires and places new block
    // 4. BlockPlaceListener re-applies correct state on next tick for ALL affected blocks
    //
    // Empty-handed interaction is prevented by PlayerInteractEvent denial

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    fun onBlockBreak(event: BlockBreakEvent) {
        // Ensure custom note blocks drop the correct item with custom model data
        val block = event.block

        plugin.logger.info("[NoteBlock Debug] BlockBreakEvent HIGHEST for block type=${block.type} at ${block.location}, cancelled=${event.isCancelled}")

        if (block.type != Material.NOTE_BLOCK) return

        // Check if this block has custom model data stored in PDC
        val customModelData = getCustomModelData(block)
        plugin.logger.info("[NoteBlock Debug] Checked PDC, customModelData=$customModelData")

        if (customModelData != null) {
            plugin.logger.info("[NoteBlock] Processing break for custom block CMD=$customModelData")

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

            // Remove from PDC
            removeCustomModelData(block)

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
        // Clean up PDC even if the event was cancelled or handled by someone else
        if (event.block.type == Material.NOTE_BLOCK) {
            removeCustomModelData(event.block)
        }
    }
}
