/*
 * CharmedChars - A word-forming puzzle game for Minecraft
 * Copyright (C) 2025 StephanosBad
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. If not, see <https://www.gnu.org/licenses/>.
 */
package org.stephanosbad.charmedChars.listeners

import io.papermc.paper.event.player.AsyncChatEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.stephanosbad.charmedChars.CharmedChars

class ExampleListener(private val plugin: CharmedChars) : Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val welcomeMessage = plugin.configManager.welcomeMessage

        // Async welcome message with delay
        plugin.launch {
            delay(2000) // Wait 2 seconds

            player.sendMessage(
                Component.text()
                    .append(Component.text("🎉 Welcome, ").color(NamedTextColor.YELLOW))
                    .append(Component.text(player.name).color(NamedTextColor.GOLD))
                    .append(Component.text("! 🎉").color(NamedTextColor.YELLOW))
                    .appendNewline()
                    .append(Component.text(welcomeMessage).color(NamedTextColor.GREEN))
                    .appendNewline()
                    .append(Component.text("This server runs on Paper with Kotlin + Gradle!").color(NamedTextColor.AQUA))
                    .build()
            )
        }

        // Custom join message for all players
        event.joinMessage(
            Component.text()
                .append(Component.text("[+] ").color(NamedTextColor.GREEN))
                .append(Component.text(player.name).color(NamedTextColor.WHITE))
                .append(Component.text(" joined the server").color(NamedTextColor.GRAY))
                .build()
        )

        plugin.logger.info("Player ${player.name} joined the server")
    }

    @EventHandler(priority = EventPriority.NORMAL)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player

        // Custom quit message
        event.quitMessage(
            Component.text()
                .append(Component.text("[-] ").color(NamedTextColor.RED))
                .append(Component.text(player.name).color(NamedTextColor.WHITE))
                .append(Component.text(" left the server").color(NamedTextColor.GRAY))
                .build()
        )

        plugin.logger.info("Player ${player.name} left the server")
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerChat(event: AsyncChatEvent) {
        val player = event.player
        val message = PlainTextComponentSerializer.plainText().serialize(event.message())

        // Async chat processing
        plugin.runAsync {
            // Example: Log chat messages
            if (plugin.configManager.debugMode) {
                plugin.logger.info("${player.name}: $message")
            }

            // Example: Process blocked words asynchronously
            val blockedWords = plugin.configManager.blockedWords
            if (blockedWords.any { message.contains(it, ignoreCase = true) }) {
                // Cancel the event on the main thread
                plugin.server.scheduler.runTask(plugin) { _ ->
                    if (!event.isCancelled) {
                        event.isCancelled = true
                        player.sendMessage(
                            Component.text("Your message contains blocked content!")
                                .color(NamedTextColor.RED)
                        )
                    }
                }
            }
        }
    }
}