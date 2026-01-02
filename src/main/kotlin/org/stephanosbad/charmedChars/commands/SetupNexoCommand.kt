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
import org.stephanosbad.charmedChars.integration.NexoSetup

/**
 * Command handler for /nexosetup
 *
 * Automatically configures Nexo with CharmedChars custom items and recipes.
 * Creates all necessary configuration files and copies textures to Nexo directories.
 *
 * **IMPORTANT**: Nexo integration requires Paper block update optimizations to be enabled.
 * See NEXO_SETUP.md for required paper-global.yml configuration.
 *
 * Usage:
 * - /nexosetup - Auto-setup Nexo configuration
 * - /nexosetup force - Force regenerate even if already setup
 */
class SetupNexoCommand(private val plugin: CharmedChars) : CommandExecutor {

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
            Component.text("═══════════════════════════════════════")
                .color(NamedTextColor.GOLD)
        )
        sender.sendMessage(
            Component.text("  CharmedChars Nexo Auto-Setup")
                .color(NamedTextColor.AQUA)
        )
        sender.sendMessage(
            Component.text("═══════════════════════════════════════")
                .color(NamedTextColor.GOLD)
        )
        sender.sendMessage(Component.text(""))
        sender.sendMessage(
            Component.text("⚠ IMPORTANT: Paper Configuration Required")
                .color(NamedTextColor.GOLD)
        )
        sender.sendMessage(
            Component.text("Nexo requires Paper block update optimizations!")
                .color(NamedTextColor.YELLOW)
        )
        sender.sendMessage(
            Component.text("Edit config/paper-global.yml and set:")
                .color(NamedTextColor.GRAY)
        )
        sender.sendMessage(
            Component.text("  block-updates:")
                .color(NamedTextColor.GRAY)
        )
        sender.sendMessage(
            Component.text("    disable-noteblock-updates: true")
                .color(NamedTextColor.GRAY)
        )
        sender.sendMessage(
            Component.text("    disable-tripwire-updates: true")
                .color(NamedTextColor.GRAY)
        )
        sender.sendMessage(
            Component.text("    disable-chorus-plant-updates: true")
                .color(NamedTextColor.GRAY)
        )
        sender.sendMessage(Component.text(""))

        // Run setup
        val setup = NexoSetup(plugin)
        val result = setup.setup(force)

        sender.sendMessage(Component.text(""))

        // Display results based on SetupResult type
        when (result) {
            is NexoSetup.SetupResult.Success -> {
                sender.sendMessage(
                    Component.text("✓ Setup Complete!")
                        .color(NamedTextColor.GREEN)
                )
                sender.sendMessage(Component.text(""))
                sender.sendMessage(
                    Component.text("Generated files:")
                        .color(NamedTextColor.GRAY)
                )
                sender.sendMessage(
                    Component.text("  • items/charmedchars_blocks.yml (128 items)")
                        .color(NamedTextColor.GRAY)
                )
                sender.sendMessage(
                    Component.text("  • pack/assets/charmedchars/textures/ (128 textures)")
                        .color(NamedTextColor.GRAY)
                )
                sender.sendMessage(
                    Component.text("  • pack/assets/charmedchars/models/ (block models)")
                        .color(NamedTextColor.GRAY)
                )
                sender.sendMessage(
                    Component.text("  • recipes/shaped.yml (updated with pyrite recipes)")
                        .color(NamedTextColor.GRAY)
                )
                sender.sendMessage(Component.text(""))
                sender.sendMessage(
                    Component.text("═══════════════════════════════════════")
                        .color(NamedTextColor.GOLD)
                )
                sender.sendMessage(
                    Component.text("  NEXT STEPS:")
                        .color(NamedTextColor.GOLD)
                )
                sender.sendMessage(
                    Component.text("═══════════════════════════════════════")
                        .color(NamedTextColor.GOLD)
                )
                sender.sendMessage(Component.text(""))
                sender.sendMessage(
                    Component.text("1. Run: /nexo reload all")
                        .color(NamedTextColor.AQUA)
                )
                sender.sendMessage(
                    Component.text("2. Restart your server (recommended)")
                        .color(NamedTextColor.AQUA)
                )
                sender.sendMessage(
                    Component.text("3. Players will receive the updated pack")
                        .color(NamedTextColor.AQUA)
                )
                sender.sendMessage(Component.text(""))
                sender.sendMessage(
                    Component.text("Test with: /charblock <player> cyan a")
                        .color(NamedTextColor.GRAY)
                )
                sender.sendMessage(Component.text(""))
            }

            is NexoSetup.SetupResult.NexoNotInstalled -> {
                sender.sendMessage(
                    Component.text("✗ Nexo Not Installed")
                        .color(NamedTextColor.RED)
                )
                sender.sendMessage(Component.text(""))
                sender.sendMessage(
                    Component.text("Nexo plugin is not installed on this server.")
                        .color(NamedTextColor.YELLOW)
                )
                sender.sendMessage(
                    Component.text("Please install Nexo before running this command.")
                        .color(NamedTextColor.YELLOW)
                )
                sender.sendMessage(Component.text(""))
                sender.sendMessage(
                    Component.text("Download: https://polymart.org/resource/nexo.6901")
                        .color(NamedTextColor.GRAY)
                )
                sender.sendMessage(Component.text(""))
            }

            is NexoSetup.SetupResult.AlreadySetup -> {
                sender.sendMessage(
                    Component.text("⚠ Already Setup")
                        .color(NamedTextColor.YELLOW)
                )
                sender.sendMessage(Component.text(""))
                sender.sendMessage(
                    Component.text("CharmedChars configuration already exists in Nexo.")
                        .color(NamedTextColor.GRAY)
                )
                sender.sendMessage(
                    Component.text("Use '/nexosetup force' to regenerate.")
                        .color(NamedTextColor.GRAY)
                )
                sender.sendMessage(Component.text(""))
            }

            is NexoSetup.SetupResult.Error -> {
                sender.sendMessage(
                    Component.text("✗ Setup Failed")
                        .color(NamedTextColor.RED)
                )
                sender.sendMessage(Component.text(""))
                sender.sendMessage(
                    Component.text("Error: ${result.message}")
                        .color(NamedTextColor.RED)
                )
                sender.sendMessage(Component.text(""))
                sender.sendMessage(
                    Component.text("Please check console logs for details.")
                        .color(NamedTextColor.YELLOW)
                )
                sender.sendMessage(
                    Component.text("Report this error on GitHub if it persists.")
                        .color(NamedTextColor.YELLOW)
                )
                sender.sendMessage(Component.text(""))
            }
        }

        return true
    }
}
