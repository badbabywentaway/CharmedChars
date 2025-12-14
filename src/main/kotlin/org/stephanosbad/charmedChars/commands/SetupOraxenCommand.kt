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

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.stephanosbad.charmedChars.CharmedChars
import org.stephanosbad.charmedChars.integration.OraxenSetup

/**
 * Command handler for /oraxensetup
 *
 * Automatically configures Oraxen with CharmedChars custom items and recipes.
 * Creates all necessary configuration files and copies textures to Oraxen directories.
 *
 * Usage:
 * - /oraxensetup - Auto-setup Oraxen configuration
 * - /oraxensetup force - Force regenerate even if already setup
 */
class SetupOraxenCommand(private val plugin: CharmedChars) : CommandExecutor {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        // Check permission
        if (!sender.hasPermission("charmedchars.admin")) {
            sender.sendMessage(
                Component.text("You don't have permission to use this command.")
                    .color(NamedTextColor.RED)
            )
            return true
        }

        // Parse force flag
        val force = args.isNotEmpty() && args[0].equals("force", ignoreCase = true)

        if (force) {
            sender.sendMessage(
                Component.text("Force mode enabled - will regenerate configuration if it exists")
                    .color(NamedTextColor.YELLOW)
            )
        }

        sender.sendMessage(
            Component.text("Starting Oraxen auto-setup...")
                .color(NamedTextColor.AQUA)
        )

        // Run setup
        val setup = OraxenSetup(plugin)
        val result = setup.autoSetup(force)

        // Display results
        if (result.success) {
            if (result.alreadySetup) {
                sender.sendMessage(Component.text(""))
                for (message in result.messages) {
                    sender.sendMessage(
                        Component.text(message)
                            .color(NamedTextColor.YELLOW)
                    )
                }
            } else {
                sender.sendMessage(Component.text(""))
                for (message in result.messages) {
                    val color = when {
                        message.startsWith("✓") || message.contains("Complete") -> NamedTextColor.GREEN
                        message.startsWith("✗") || message.startsWith("ERROR") -> NamedTextColor.RED
                        message.startsWith("NEXT STEPS") || message.contains("===") -> NamedTextColor.GOLD
                        else -> NamedTextColor.GRAY
                    }
                    sender.sendMessage(
                        Component.text(message)
                            .color(color)
                    )
                }
            }
        } else {
            sender.sendMessage(
                Component.text("Setup failed!")
                    .color(NamedTextColor.RED)
            )
            sender.sendMessage(Component.text(""))
            for (message in result.messages) {
                sender.sendMessage(
                    Component.text(message)
                        .color(NamedTextColor.RED)
                )
            }
        }

        return true
    }
}
