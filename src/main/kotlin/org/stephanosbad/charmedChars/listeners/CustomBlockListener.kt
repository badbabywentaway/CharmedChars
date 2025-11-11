package org.stephanosbad.charmedChars.listeners

import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.stephanosbad.charmedChars.block.CustomBlocks
import org.stephanosbad.charmedChars.CharmedChars

/**
 * Handles custom block interactions and behaviors
 */
class CustomBlockListener(private val plugin: CharmedChars) : Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    fun onBlockPlace(event: BlockPlaceEvent) {
        val item = event.itemInHand
        if (!plugin.customBlocks.isCustomBlock(item)) return

        val blockType = plugin.customBlocks.getCustomBlockType(item) ?: return
        val player = event.player
        val block = event.blockPlaced

        // Store custom block data in the world
        storeCustomBlockData(block, blockType)

        // Handle specific placement effects
        when (blockType) {
            CustomBlocks.Companion.MAGIC_STONE -> {
                // Particle effect on placement
                spawnParticleEffect(block.location, Particle.ENCHANT, 20)
                player.playSound(block.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.2f)

                player.sendMessage(
                    Component.text("You placed a Magic Stone! Right-click to activate.")
                        .color(NamedTextColor.LIGHT_PURPLE)
                )
            }

            CustomBlocks.Companion.ENCHANTED_LOG -> {
                spawnParticleEffect(block.location, Particle.HAPPY_VILLAGER, 15)
                player.playSound(block.location, Sound.BLOCK_WOOD_PLACE, 1.0f, 0.8f)

                player.sendMessage(
                    Component.text("Enchanted Log placed! It will grow faster than normal.")
                        .color(NamedTextColor.GREEN)
                )
            }

            CustomBlocks.Companion.CRYSTAL_BLOCK -> {
                spawnParticleEffect(block.location, Particle.END_ROD, 25)
                player.playSound(block.location, Sound.BLOCK_AMETHYST_BLOCK_PLACE, 1.0f, 1.0f)

                player.sendMessage(
                    Component.text("Crystal Block placed! It amplifies nearby enchantments.")
                        .color(NamedTextColor.AQUA)
                )
            }
        }

        plugin.logger.info("${player.name} placed custom block: $blockType at ${block.location}")
    }

    @EventHandler(priority = EventPriority.NORMAL)
    fun onBlockBreak(event: BlockBreakEvent) {
        val block = event.block
        val blockType = getCustomBlockData(block) ?: return

        val player = event.player

        // Cancel normal drops
        event.isDropItems = false

        // Handle custom block breaking
        when (blockType) {
            CustomBlocks.Companion.MAGIC_STONE -> {
                // Drop the custom item
                val item = plugin.customBlocks.createMagicStone()
                block.world.dropItemNaturally(block.location, item)

                // Special effects
                spawnParticleEffect(block.location, Particle.SOUL, 30)
                player.playSound(block.location, Sound.BLOCK_GLASS_BREAK, 1.0f, 0.7f)

                // Bonus XP
                player.giveExp(10)
            }

            CustomBlocks.Companion.ENCHANTED_LOG -> {
                val item = plugin.customBlocks.createEnchantedLog()
                block.world.dropItemNaturally(block.location, item)

                spawnParticleEffect(block.location, Particle.COMPOSTER, 20)
                player.playSound(block.location, Sound.BLOCK_WOOD_BREAK, 1.0f, 0.9f)

                // Bonus XP for enchanted logs
                player.giveExp(15)
            }

            CustomBlocks.Companion.CRYSTAL_BLOCK -> {
                val item = plugin.customBlocks.createCrystalBlock()
                block.world.dropItemNaturally(block.location, item)

                spawnParticleEffect(block.location, Particle.DRAGON_BREATH, 35)
                player.playSound(block.location, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 1.0f, 1.1f)

                // Extra bonus XP for rare blocks
                player.giveExp(25)
            }
        }

        // Clear custom block data
        clearCustomBlockData(block)

        plugin.logger.info("${player.name} broke custom block: $blockType at ${block.location}")
    }

    @EventHandler(priority = EventPriority.NORMAL)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        if (event.hand != EquipmentSlot.HAND) return

        val block = event.clickedBlock ?: return
        val blockType = getCustomBlockData(block) ?: return
        val player = event.player

        // Handle right-click interactions
        when (blockType) {
            CustomBlocks.Companion.MAGIC_STONE -> {
                event.isCancelled = true

                plugin.launch {
                    // Activate magic stone effect
                    player.sendMessage(
                        Component.text("Magic Stone activated! You feel energized...")
                            .color(NamedTextColor.LIGHT_PURPLE)
                    )

                    // Heal player
                    if (player.health < player.maxHealth) {
                        player.health = minOf(player.health + 4.0, player.maxHealth)
                    }

                    // Give temporary effects
                    player.addPotionEffect(
                        PotionEffect(
                            PotionEffectType.SPEED,
                            20 * 10, // 10 seconds
                            1
                        )
                    )

                    // Visual and audio effects
                    spawnParticleEffect(block.location.add(0.5, 1.0, 0.5), Particle.FIREWORK, 40)
                    player.playSound(block.location, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f)
                }
            }

            CustomBlocks.Companion.CRYSTAL_BLOCK -> {
                event.isCancelled = true

                player.sendMessage(
                    Component.text("The crystal resonates with mystical energy...")
                        .color(NamedTextColor.AQUA)
                )

                // Show nearby players
                val nearbyPlayers = block.location.getNearbyPlayers(10.0)
                if (nearbyPlayers.isNotEmpty()) {
                    player.sendMessage(
                        Component.text("${nearbyPlayers.size} players are nearby")
                            .color(NamedTextColor.YELLOW)
                    )
                }

                // Crystal effect
                spawnParticleEffect(block.location.add(0.5, 1.0, 0.5), Particle.END_ROD, 25)
                player.playSound(block.location, Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.2f)
            }
        }
    }

    // Utility methods

    private fun spawnParticleEffect(location: Location, particle: Particle, count: Int) {
        plugin.launch {
            location.world.spawnParticle(
                particle,
                location.add(0.5, 0.5, 0.5),
                count,
                0.3, 0.3, 0.3,
                0.1
            )
        }
    }

    private fun storeCustomBlockData(block: Block, blockType: String) {
        // In a real plugin, you'd want to store this in a database or file
        // For this skeleton, we'll use a simple in-memory storage
        // You should implement persistent storage for production use
        val key = "${block.world.name}_${block.x}_${block.y}_${block.z}"
        plugin.customBlockData[key] = blockType
    }

    private fun getCustomBlockData(block: Block): String? {
        val key = "${block.world.name}_${block.x}_${block.y}_${block.z}"
        return plugin.customBlockData[key]
    }

    private fun clearCustomBlockData(block: Block) {
        val key = "${block.world.name}_${block.x}_${block.y}_${block.z}"
        plugin.customBlockData.remove(key)
    }
}