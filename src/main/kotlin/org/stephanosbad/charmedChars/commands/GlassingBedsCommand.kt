package org.stephanosbad.charmedChars.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.stephanosbad.charmedChars.CharmedChars

/**
 * Command handler for managing the Glassing Beds feature
 *
 * Provides subcommands to enable, disable, and check status of the feature.
 * Requires charmedchars.admin permission.
 *
 * @property plugin Reference to the main plugin instance
 */
class GlassingBedsCommand(private val plugin: CharmedChars) : CommandExecutor {

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

        // Handle subcommands
        if (args.isEmpty()) {
            showHelp(sender)
            return true
        }

        when (args[0].lowercase()) {
            "enable" -> handleEnable(sender)
            "disable" -> handleDisable(sender)
            "status" -> handleStatus(sender)
            else -> {
                sender.sendMessage(
                    Component.text("Unknown subcommand: ${args[0]}")
                        .color(NamedTextColor.RED)
                )
                showHelp(sender)
            }
        }

        return true
    }

    private fun showHelp(sender: CommandSender) {
        sender.sendMessage(
            Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .color(NamedTextColor.AQUA)
        )
        sender.sendMessage(
            Component.text("  Glassing Beds Commands")
                .color(NamedTextColor.AQUA)
                .decorate(TextDecoration.BOLD)
        )
        sender.sendMessage(
            Component.text("  /glassingbeds enable")
                .color(NamedTextColor.YELLOW)
        )
        sender.sendMessage(
            Component.text("    - Enable lava-to-glass conversion on bed explosions")
                .color(NamedTextColor.GRAY)
        )
        sender.sendMessage(
            Component.text("  /glassingbeds disable")
                .color(NamedTextColor.YELLOW)
        )
        sender.sendMessage(
            Component.text("    - Disable the glassing beds feature")
                .color(NamedTextColor.GRAY)
        )
        sender.sendMessage(
            Component.text("  /glassingbeds status")
                .color(NamedTextColor.YELLOW)
        )
        sender.sendMessage(
            Component.text("    - Check if the feature is enabled or disabled")
                .color(NamedTextColor.GRAY)
        )
        sender.sendMessage(
            Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .color(NamedTextColor.AQUA)
        )
    }

    private fun handleEnable(sender: CommandSender) {
        // Modify config
        plugin.config.set("glassing-beds.enabled", true)
        plugin.saveConfig()
        plugin.configManager.reloadConfig()

        sender.sendMessage(
            Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .color(NamedTextColor.GREEN)
        )
        sender.sendMessage(
            Component.text("  Glassing Beds ENABLED")
                .color(NamedTextColor.GREEN)
                .decorate(TextDecoration.BOLD)
        )
        sender.sendMessage(
            Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .color(NamedTextColor.GREEN)
        )
        sender.sendMessage(
            Component.text("Bed explosions in the Nether/End will now convert")
                .color(NamedTextColor.WHITE)
        )
        sender.sendMessage(
            Component.text("lava blocks within 5 blocks to glass.")
                .color(NamedTextColor.WHITE)
        )

        plugin.logger.info("${sender.name} enabled the Glassing Beds feature")
    }

    private fun handleDisable(sender: CommandSender) {
        // Modify config
        plugin.config.set("glassing-beds.enabled", false)
        plugin.saveConfig()
        plugin.configManager.reloadConfig()

        sender.sendMessage(
            Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .color(NamedTextColor.RED)
        )
        sender.sendMessage(
            Component.text("  Glassing Beds DISABLED")
                .color(NamedTextColor.RED)
                .decorate(TextDecoration.BOLD)
        )
        sender.sendMessage(
            Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .color(NamedTextColor.RED)
        )
        sender.sendMessage(
            Component.text("Bed explosions will no longer convert lava to glass.")
                .color(NamedTextColor.WHITE)
        )

        plugin.logger.info("${sender.name} disabled the Glassing Beds feature")
    }

    private fun handleStatus(sender: CommandSender) {
        val isEnabled = plugin.configManager.glassingBedsEnabled
        val statusText = if (isEnabled) "ENABLED" else "DISABLED"
        val statusColor = if (isEnabled) NamedTextColor.GREEN else NamedTextColor.RED

        sender.sendMessage(
            Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .color(NamedTextColor.AQUA)
        )
        sender.sendMessage(
            Component.text("  Glassing Beds Status")
                .color(NamedTextColor.AQUA)
                .decorate(TextDecoration.BOLD)
        )
        sender.sendMessage(
            Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .color(NamedTextColor.AQUA)
        )
        sender.sendMessage(
            Component.text("Feature: $statusText")
                .color(statusColor)
                .decorate(TextDecoration.BOLD)
        )

        if (isEnabled) {
            sender.sendMessage(Component.text(""))
            sender.sendMessage(
                Component.text("Bed explosions in the Nether/End will convert")
                    .color(NamedTextColor.GRAY)
            )
            sender.sendMessage(
                Component.text("lava blocks within 5 blocks to glass.")
                    .color(NamedTextColor.GRAY)
            )
        }
    }
}
