package org.stephanosbad.charmedChars.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.stephanosbad.charmedChars.CharmedChars

/**
 * Listener for automatically sending resource pack to players when they join
 */
class ResourcePackListener(private val plugin: CharmedChars) : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        // Check if auto-send is enabled and custom textures are enabled
        if (!plugin.configManager.customTexturesEnabled) {
            return
        }

        if (!plugin.configManager.resourcePackAutoSend) {
            return
        }

        // Wait a bit before sending to ensure player is fully loaded
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            try {
                plugin.textureManager.sendResourcePackToPlayer(player)
            } catch (e: Exception) {
                plugin.logger.warning("Failed to send resource pack to ${player.name}: ${e.message}")
            }
        }, 20L) // 1 second delay (20 ticks)
    }
}
