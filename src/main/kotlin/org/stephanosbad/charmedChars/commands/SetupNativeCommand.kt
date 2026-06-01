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
import org.stephanosbad.charmedChars.integration.NativeItemManagerSetup
import org.stephanosbad.charmedChars.integration.NativeItemProvider

/**
 * /nativesetup [force]
 *
 * Generates the CharmedChars resource pack for the built-in NativeItemManager.
 * Only available when the NativeItems provider is active (no ItemsAdder/Oraxen/Nexo).
 *
 * The pack is written to <dataFolder>/native-pack/ and must be served to clients
 * separately (server resource pack, client pack folder, etc.).
 */
class SetupNativeCommand(private val plugin: CharmedChars) : CommandExecutor {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (!sender.hasPermission("charmedchars.admin")) {
            sender.sendMessage(Component.text("You don't have permission to use this command.").color(NamedTextColor.RED))
            return true
        }

        val nativeProvider = plugin.customItemProviderManager.getProvider() as? NativeItemProvider
        if (nativeProvider == null) {
            sender.sendMessage(
                Component.text("NativeItemManager is not active. This server uses ${plugin.customItemProviderManager.getProviderName()}.")
                    .color(NamedTextColor.RED)
            )
            return true
        }

        val force = args.isNotEmpty() && args[0].equals("force", ignoreCase = true)
        if (force) {
            sender.sendMessage(Component.text("Force mode — pack will be regenerated.").color(NamedTextColor.YELLOW))
        }

        sender.sendMessage(Component.text("Generating native resource pack...").color(NamedTextColor.AQUA))

        val result = NativeItemManagerSetup(plugin, nativeProvider).autoSetup(force)

        for (message in result.messages) {
            val color = when {
                message.startsWith("  Done") || message.contains("Ready") -> NamedTextColor.GREEN
                message.startsWith("ERROR") -> NamedTextColor.RED
                message.contains("===") -> NamedTextColor.GOLD
                else -> NamedTextColor.GRAY
            }
            sender.sendMessage(Component.text(message).color(color))
        }

        if (result.success && !result.alreadySetup) {
            val zipFile = result.zipFile
            val hash = result.packHash
            if (zipFile != null && hash != null) {
                nativeProvider.packHash = hash
                plugin.startNativePackHosting(nativeProvider, zipFile)
                sender.sendMessage(Component.text("Pack server started. Players will receive the pack on join.").color(NamedTextColor.GREEN))
                sender.sendMessage(Component.text("URL: ${nativeProvider.packUrl}").color(NamedTextColor.AQUA))
            }
        }

        return true
    }
}
