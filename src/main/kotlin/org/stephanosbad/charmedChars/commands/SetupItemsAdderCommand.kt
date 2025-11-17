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
import org.stephanosbad.charmedChars.integration.ItemsAdderSetup

class SetupItemsAdderCommand(private val plugin: CharmedChars) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        // Check for force flag
        val force = args.isNotEmpty() && args[0].equals("force", ignoreCase = true)

        sender.sendMessage(
            Component.text("=== CharmedChars ItemsAdder Auto-Setup ===")
                .color(NamedTextColor.GOLD)
        )
        sender.sendMessage(Component.text(""))

        val setup = ItemsAdderSetup(plugin)

        // Run setup
        val result = setup.autoSetup()

        // Display messages with colors
        for (message in result.messages) {
            val color = when {
                message.startsWith("✓") || message.contains("SUCCESS") -> NamedTextColor.GREEN
                message.startsWith("✗") || message.startsWith("ERROR") -> NamedTextColor.RED
                message.startsWith("WARNING") || message.startsWith("⚠") -> NamedTextColor.YELLOW
                message.startsWith("===") -> NamedTextColor.GOLD
                message.startsWith("NEXT STEPS") -> NamedTextColor.AQUA
                message.matches(Regex("^\\d+\\..*")) -> NamedTextColor.YELLOW
                else -> NamedTextColor.GRAY
            }

            sender.sendMessage(
                Component.text(message).color(color)
            )
        }

        sender.sendMessage(Component.text(""))

        if (result.success) {
            if (result.alreadySetup) {
                sender.sendMessage(
                    Component.text("Configuration already exists. Use '/iasetup force' to regenerate.")
                        .color(NamedTextColor.YELLOW)
                )
            } else {
                sender.sendMessage(
                    Component.text("✓ Auto-setup completed successfully!")
                        .color(NamedTextColor.GREEN)
                )
                sender.sendMessage(
                    Component.text("Don't forget to run /iazip and restart the server!")
                        .color(NamedTextColor.GOLD)
                )
            }
        } else {
            sender.sendMessage(
                Component.text("✗ Auto-setup failed. See messages above for details.")
                    .color(NamedTextColor.RED)
            )
        }

        return true
    }
}
