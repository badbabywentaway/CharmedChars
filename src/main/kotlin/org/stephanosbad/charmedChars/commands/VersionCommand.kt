package org.stephanosbad.charmedChars.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.stephanosbad.charmedChars.CharmedChars

class VersionCommand(private val plugin: CharmedChars) : CommandExecutor {

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
