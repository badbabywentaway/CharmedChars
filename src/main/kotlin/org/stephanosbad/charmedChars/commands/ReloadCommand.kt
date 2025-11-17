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
package org.stephanosbad.charmedChars.commands

import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.stephanosbad.charmedChars.CharmedChars

class ReloadCommand(private val plugin: CharmedChars) : CommandExecutor {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (!sender.hasPermission("charmedChars.admin")) {
            sender.sendMessage(
                Component.text("You don't have permission to use this command!")
                    .color(NamedTextColor.RED)
            )
            return true
        }

        sender.sendMessage(
            Component.text("Reloading plugin configuration...")
                .color(NamedTextColor.YELLOW)
        )

        // Use coroutines for async reload
        plugin.launch {
            try {
                plugin.reload()
                sender.sendMessage(
                    Component.text("Plugin configuration reloaded successfully!")
                        .color(NamedTextColor.GREEN)
                )
            } catch (e: Exception) {
                sender.sendMessage(
                    Component.text("Failed to reload configuration: ${e.message}")
                        .color(NamedTextColor.RED)
                )
                plugin.logger.severe("Failed to reload configuration: ${e.message}")
            }
        }

        return true
    }
}