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
import org.stephanosbad.integrare.ProviderKind
import org.stephanosbad.integrare.provider.NativeItemProvider

/**
 * /iasetup, /oraxensetup, /nexosetup and /nativesetup [force]
 *
 * Installs the CharmedChars item catalog into the active custom item provider.
 * Each command name is bound to the provider it sets up, and refuses to run
 * when a different provider is active.
 *
 * @param expected The provider this command name sets up
 */
class ProviderSetupCommand(
    private val plugin: CharmedChars,
    private val expected: ProviderKind
) : CommandExecutor {

    companion object {
        fun commandFor(kind: ProviderKind) = when (kind) {
            ProviderKind.ITEMSADDER -> "iasetup"
            ProviderKind.ORAXEN -> "oraxensetup"
            ProviderKind.NEXO -> "nexosetup"
            ProviderKind.NATIVE -> "nativesetup"
        }
    }

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

        val providerName = plugin.customItemProviderManager.getProviderName()
        val setup = plugin.providerSetup()
        if (setup == null || setup.kind != expected) {
            sender.sendMessage(
                Component.text("/$label is not for this server: it uses $providerName.").color(NamedTextColor.RED)
            )
            setup?.let {
                sender.sendMessage(Component.text("Use /${commandFor(it.kind)} instead.").color(NamedTextColor.YELLOW))
            }
            return true
        }

        val force = args.isNotEmpty() && args[0].equals("force", ignoreCase = true)
        if (force) {
            sender.sendMessage(
                Component.text("Force mode enabled - will regenerate configuration if it exists").color(NamedTextColor.YELLOW)
            )
        }

        if (setup.serverRequirements.isNotEmpty()) {
            sender.sendMessage(Component.text("⚠ Required server settings:").color(NamedTextColor.GOLD))
            setup.serverRequirements.forEach { sender.sendMessage(Component.text(it).color(NamedTextColor.GRAY)) }
            sender.sendMessage(Component.text(""))
        }

        sender.sendMessage(Component.text("Starting $providerName setup...").color(NamedTextColor.AQUA))
        val result = setup.install(force)

        if (!result.success) {
            sender.sendMessage(Component.text("Setup failed!").color(NamedTextColor.RED))
        }
        for (message in result.messages) {
            val color = when {
                !result.success || message.startsWith("ERROR") || message.trim().startsWith("✗") -> NamedTextColor.RED
                result.alreadySetup -> NamedTextColor.YELLOW
                message.trim().startsWith("✓") || message.trim().startsWith("Done") ||
                    message.contains("Complete") || message.contains("Ready") -> NamedTextColor.GREEN
                message.startsWith("NEXT STEPS") || message.contains("===") -> NamedTextColor.GOLD
                else -> NamedTextColor.GRAY
            }
            sender.sendMessage(Component.text(message).color(color))
        }
        if (result.alreadySetup) {
            sender.sendMessage(Component.text("Use '/$label force' to regenerate.").color(NamedTextColor.YELLOW))
        }

        // The native pack is served by CharmedChars itself; start hosting the new zip
        val nativeProvider = plugin.customItemProviderManager.getProvider() as? NativeItemProvider
        val zipFile = result.zipFile
        val hash = result.packHash
        if (nativeProvider != null && result.success && !result.alreadySetup && zipFile != null && hash != null) {
            nativeProvider.packHash = hash
            plugin.startNativePackHosting(nativeProvider, zipFile)
            sender.sendMessage(Component.text("Pack server started. Players will receive the pack on join.").color(NamedTextColor.GREEN))
            sender.sendMessage(Component.text("URL: ${nativeProvider.packUrl}").color(NamedTextColor.AQUA))
        }

        return true
    }
}
