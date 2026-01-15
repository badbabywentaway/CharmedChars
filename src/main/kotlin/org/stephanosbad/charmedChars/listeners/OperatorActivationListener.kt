package org.stephanosbad.charmedChars.listeners

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.inventory.ItemStack
import org.stephanosbad.charmedChars.CharmedChars
import org.stephanosbad.charmedChars.items.BlockColor
import org.stephanosbad.charmedChars.items.LateralDirection
import org.stephanosbad.charmedChars.items.NonAlphaNumBlocks

/**
 * Listener for the Glassing Beds operator activation system
 *
 * Players must hit a sequence of 4 different operator blocks (+, -, ×, ÷) of the same color
 * in the Nether to activate the glassing beds feature for their current Nether session.
 * The activation persists until they leave the Nether through any means.
 *
 * @property plugin Reference to the main plugin instance
 */
class OperatorActivationListener(
    private val plugin: CharmedChars
) : Listener {

    companion object {
        // Track which players have activated glassing beds in current Nether session
        // Key: Player UUID string, Value: true if activated
        private val playerGlassingBedsActive = mutableMapOf<String, Boolean>()

        /**
         * Check if a player has activated glassing beds in their current Nether session
         */
        fun isPlayerActivated(player: Player): Boolean {
            return playerGlassingBedsActive[player.uniqueId.toString()] == true
        }
    }

    /**
     * Primary event handler for operator sequence detection (Oraxen compatibility)
     *
     * Fires immediately on LEFT_CLICK_BLOCK action.
     * Uses LOWEST priority to run before other plugins.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action != Action.LEFT_CLICK_BLOCK) return
        if (!isValidTool(event.item)) return

        val clickedBlock = event.clickedBlock ?: return
        processOperatorSequence(event.player, clickedBlock)
    }

    /**
     * Fallback event handler for operator sequence detection (Nexo compatibility)
     *
     * Fires when player starts breaking a block (left-click and hold).
     * Works better for Nexo when Paper disables noteblock updates.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    fun onBlockDamage(event: BlockDamageEvent) {
        if (!isValidTool(event.itemInHand)) return
        processOperatorSequence(event.player, event.block)
    }

    /**
     * Reset activation when player enters Nether
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerMove(event: PlayerMoveEvent) {
        val from = event.from
        val to = event.to ?: return

        // Check if player entered Nether
        if (from.world.environment != World.Environment.NETHER &&
            to.world.environment == World.Environment.NETHER) {

            val uuid = event.player.uniqueId.toString()
            // Reset activation for this new Nether visit
            playerGlassingBedsActive.remove(uuid)
        }
    }

    /**
     * Clean up on player disconnect to prevent memory leaks
     */
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        playerGlassingBedsActive.remove(event.player.uniqueId.toString())
    }

    /**
     * Reset activation on respawn (player died, starts new Nether session)
     */
    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        // Always reset on death/respawn - new session
        playerGlassingBedsActive.remove(event.player.uniqueId.toString())
    }

    /**
     * Process operator sequence detection and validation
     */
    private fun processOperatorSequence(player: Player, clickedBlock: Block) {
        // Check if clicked block is an operator
        val (clickedOperator, clickedColor) = getOperatorFromBlock(clickedBlock) ?: return

        // Scan all 4 cardinal directions for adjacent operator blocks
        val directions = listOf(
            LateralDirection(1, 0),   // +X
            LateralDirection(-1, 0),  // -X
            LateralDirection(0, 1),   // +Z
            LateralDirection(0, -1)   // -Z
        )

        var scanDirection: LateralDirection? = null
        for (direction in directions) {
            val testLocation = clickedBlock.location.clone()
                .add(direction.xOffset.toDouble(), 0.0, direction.zOffset.toDouble())
            val adjacentBlock = testLocation.block

            if (getOperatorFromBlock(adjacentBlock) != null) {
                scanDirection = direction
                break
            }
        }

        // Collect operators in the detected direction (or single block if no adjacent)
        val operators = mutableListOf<NonAlphaNumBlocks>()
        val colors = mutableListOf<BlockColor>()
        val blockLocations = mutableListOf<Block>()

        // Start with clicked block
        operators.add(clickedOperator)
        colors.add(clickedColor)
        blockLocations.add(clickedBlock)

        // Scan in both directions if adjacent blocks found
        if (scanDirection != null) {
            // Scan forward
            var currentLocation = clickedBlock.location.clone()
            while (true) {
                currentLocation.add(scanDirection.xOffset.toDouble(), 0.0, scanDirection.zOffset.toDouble())
                val block = currentLocation.block
                val (operator, color) = getOperatorFromBlock(block) ?: break

                operators.add(operator)
                colors.add(color)
                blockLocations.add(block)
            }

            // Scan backward
            currentLocation = clickedBlock.location.clone()
            val reverseDirection = LateralDirection(-scanDirection.xOffset, -scanDirection.zOffset)

            while (true) {
                currentLocation.add(reverseDirection.xOffset.toDouble(), 0.0, reverseDirection.zOffset.toDouble())
                val block = currentLocation.block
                val (operator, color) = getOperatorFromBlock(block) ?: break

                operators.add(0, operator)  // Add to beginning to maintain order
                colors.add(0, color)
                blockLocations.add(0, block)
            }
        }

        // Validate sequence
        if (!validateOperatorSequence(operators, colors, player)) {
            sendFailureMessage(player, operators, colors)
            return
        }

        // Success! Activate glassing beds and consume blocks
        val provider = plugin.customItemProviderManager.getProvider()
        for (block in blockLocations) {
            if (provider != null) {
                provider.removeCustomBlock(block)
            } else {
                block.type = Material.AIR
            }
        }

        // Mark player as activated
        playerGlassingBedsActive[player.uniqueId.toString()] = true

        // Send success message
        player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            .color(NamedTextColor.GREEN))
        player.sendMessage(Component.text("✦ Glassing Beds ACTIVATED! ✦")
            .color(NamedTextColor.GREEN)
            .decorate(TextDecoration.BOLD))
        player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            .color(NamedTextColor.GREEN))
        player.sendMessage(Component.text("You can now use beds to convert lava to glass!")
            .color(NamedTextColor.GRAY))

        plugin.logger.info("${player.name} activated glassing beds in Nether")
    }

    /**
     * Check if item is a valid gold or pyrite tool
     */
    private fun isValidTool(item: ItemStack?): Boolean {
        if (item == null || item.itemMeta == null) {
            return false
        }

        // Check if it's a gold tool (vanilla)
        if (item.type.name.lowercase().contains("gold")) {
            return true
        }

        // Check if it's a pyrite tool using the custom item provider
        val provider = plugin.customItemProviderManager.getProvider()
        if (provider != null) {
            val customItem = provider.getCustomItem(item)
            if (customItem != null && customItem.namespacedId.lowercase().contains("pyrite")) {
                return true
            }
        }

        // Fallback: check if display name contains "gold" or "pyrite"
        val displayName = item.itemMeta.displayName()
        if (displayName != null) {
            val plainText = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                .serialize(displayName).lowercase()
            if (plainText.contains("gold") || plainText.contains("pyrite")) {
                return true
            }
        }

        return false
    }

    /**
     * Get operator and color from a block
     * Returns null if block is not an operator block
     */
    private fun getOperatorFromBlock(block: Block): Pair<NonAlphaNumBlocks, BlockColor>? {
        // Check if NoteBlock
        if (block.state.blockData !is NoteBlock) return null

        // Get custom block info from provider
        val provider = plugin.customItemProviderManager.getProvider()
        val blockInfo = provider?.getCustomBlock(block) ?: return null

        // Parse namespaced ID: "charmedchars:cyan_plus" → "cyan" + "plus"
        val parts = blockInfo.blockName.split("_")
        if (parts.size != 2) return null

        val colorName = parts[0]
        val operatorName = parts[1]

        // Match color
        val color = BlockColor.entries.find {
            it.directoryName.equals(colorName, ignoreCase = true)
        } ?: return null

        // Match operator
        val operator = NonAlphaNumBlocks.entries.find {
            it.nonAlphaNumBlockName.equals(operatorName, ignoreCase = true)
        } ?: return null

        return Pair(operator, color)
    }

    /**
     * Validate operator sequence
     */
    private fun validateOperatorSequence(
        operators: List<NonAlphaNumBlocks>,
        colors: List<BlockColor>,
        player: Player
    ): Boolean {
        // Must be exactly 4 operators
        if (operators.size != 4) return false

        // All must be different (convert to Set removes duplicates)
        if (operators.toSet().size != 4) return false

        // All must be same color
        if (colors.toSet().size != 1) return false

        // Player must be in Nether
        if (player.world.environment != World.Environment.NETHER) return false

        return true
    }

    /**
     * Send appropriate failure message based on validation failure
     */
    private fun sendFailureMessage(
        player: Player,
        operators: List<NonAlphaNumBlocks>,
        colors: List<BlockColor>
    ) {
        when {
            player.world.environment != World.Environment.NETHER -> {
                player.sendMessage(
                    Component.text("Operator activation only works in the Nether!")
                        .color(NamedTextColor.RED)
                )
            }
            operators.size != 4 -> {
                player.sendMessage(
                    Component.text("Need exactly 4 operator blocks (+−×÷)")
                        .color(NamedTextColor.RED)
                )
            }
            operators.toSet().size != 4 -> {
                player.sendMessage(
                    Component.text("All 4 operators must be different!")
                        .color(NamedTextColor.RED)
                )
            }
            colors.toSet().size != 1 -> {
                player.sendMessage(
                    Component.text("Mixed colors! All operators must be the same color.")
                        .color(NamedTextColor.RED)
                )
            }
        }
    }
}
