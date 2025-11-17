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
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.stephanosbad.charmedChars.CharmedChars

/**
 * Command handler for displaying plugin version information
 *
 * Shows the plugin version, API version, author, recent changes, and
 * a link to the full changelog in VERSION.md.
 *
 * @property plugin The CharmedChars plugin instance
 */
class VersionCommand(private val plugin: CharmedChars) : CommandExecutor {

    /**
     * Executes the /version command
     *
     * @param sender The command sender
     * @param command The command instance
     * @param label The command label used
     * @param args Command arguments (unused)
     * @return true if the command was handled successfully
     */
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        val version = plugin.description.version
        val apiVersion = plugin.description.apiVersion

        sender.sendMessage(
            Component.text()
                .append(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━").color(NamedTextColor.DARK_GRAY))
                .build()
        )

        sender.sendMessage(
            Component.text()
                .append(Component.text("  CharmedChars").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD))
                .append(Component.text(" - Word Building Game").color(NamedTextColor.YELLOW))
                .build()
        )

        sender.sendMessage(Component.text(""))

        sender.sendMessage(
            Component.text()
                .append(Component.text("  Version: ").color(NamedTextColor.GRAY))
                .append(Component.text(version).color(NamedTextColor.GREEN))
                .build()
        )

        sender.sendMessage(
            Component.text()
                .append(Component.text("  API Version: ").color(NamedTextColor.GRAY))
                .append(Component.text(apiVersion ?: "Unknown").color(NamedTextColor.GREEN))
                .build()
        )

        sender.sendMessage(
            Component.text()
                .append(Component.text("  Author: ").color(NamedTextColor.GRAY))
                .append(Component.text("StephanosBad").color(NamedTextColor.AQUA))
                .build()
        )

        sender.sendMessage(Component.text(""))

        sender.sendMessage(
            Component.text()
                .append(Component.text("  Latest Changes:").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD))
                .build()
        )

        sender.sendMessage(
            Component.text("    • Fixed letter drop after scoring").color(NamedTextColor.WHITE)
        )

        sender.sendMessage(
            Component.text("    • Fixed color randomization").color(NamedTextColor.WHITE)
        )

        sender.sendMessage(
            Component.text("    • Doubled drop rates (6% base)").color(NamedTextColor.WHITE)
        )

        sender.sendMessage(
            Component.text("    • Added comprehensive docs").color(NamedTextColor.WHITE)
        )

        sender.sendMessage(Component.text(""))

        sender.sendMessage(
            Component.text()
                .append(Component.text("  Documentation: ").color(NamedTextColor.GRAY))
                .append(Component.text("See VERSION.md for full changelog").color(NamedTextColor.AQUA))
                .build()
        )

        sender.sendMessage(
            Component.text()
                .append(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━").color(NamedTextColor.DARK_GRAY))
                .build()
        )

        return true
    }
}
