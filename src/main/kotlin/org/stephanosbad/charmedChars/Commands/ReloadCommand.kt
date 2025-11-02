package org.stephanosbad.charmedChars.Commands

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
        if (!sender.hasPermission("myplugin.admin")) {
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