package org.stephanosbad.charmedChars.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.stephanosbad.charmedChars.CharmedChars

/**
 * Commands for managing custom textures and resource packs
 */
class TextureCommand(private val plugin: CharmedChars) : CommandExecutor, TabCompleter {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (!sender.hasPermission("charmedChars.textures")) {
            sender.sendMessage(
                Component.text("You don't have permission to use texture commands!")
                    .color(NamedTextColor.RED)
            )
            return true
        }

        when (args.getOrNull(0)?.lowercase()) {
            "download", "get" -> {
                if (sender !is Player) {
                    sender.sendMessage(
                        Component.text("This command can only be used by players!")
                            .color(NamedTextColor.RED)
                    )
                    return true
                }

                plugin.textureManager.sendResourcePackToPlayer(sender)
                return true
            }

            "regenerate", "regen" -> {
                if (!sender.hasPermission("charmedChars.textures.admin")) {
                    sender.sendMessage(
                        Component.text("You don't have permission to regenerate resource packs!")
                            .color(NamedTextColor.RED)
                    )
                    return true
                }

                sender.sendMessage(
                    Component.text("Regenerating resource pack...")
                        .color(NamedTextColor.YELLOW)
                )

                plugin.textureManager.regenerateResourcePack()

                sender.sendMessage(
                    Component.text("Resource pack regenerated! Players should re-download it.")
                        .color(NamedTextColor.GREEN)
                )
                return true
            }

            "status", "info" -> {
                val isReady = plugin.textureManager.isTextureSystemReady()
                val statusColor = if (isReady) NamedTextColor.GREEN else NamedTextColor.RED
                val statusText = if (isReady) "Ready" else "Not Ready"

                sender.sendMessage(
                    Component.text("Custom Textures Status: $statusText")
                        .color(statusColor)
                )

                sender.sendMessage(
                    Component.text("Enabled: ${plugin.configManager.customTexturesEnabled}")
                        .color(NamedTextColor.GRAY)
                )

                if (!isReady) {
                    sender.sendMessage(
                        Component.text("Run '/textures regenerate' to create the resource pack")
                            .color(NamedTextColor.YELLOW)
                    )
                }

                return true
            }

            "help", null -> {
                sender.sendMessage(
                    Component.text("Custom Textures Commands:")
                        .color(NamedTextColor.GOLD)
                )

                sender.sendMessage(
                    Component.text("  /textures download - Get the resource pack")
                        .color(NamedTextColor.YELLOW)
                )

                sender.sendMessage(
                    Component.text("  /textures status - Check system status")
                        .color(NamedTextColor.YELLOW)
                )

                if (sender.hasPermission("charmedChars.textures.admin")) {
                    sender.sendMessage(
                        Component.text("  /textures regenerate - Rebuild resource pack")
                            .color(NamedTextColor.AQUA)
                    )
                }

                return true
            }

            else -> {
                sender.sendMessage(
                    Component.text("Unknown subcommand: ${args[0]}")
                        .color(NamedTextColor.RED)
                )

                sender.sendMessage(
                    Component.text("Use '/textures help' for available commands")
                        .color(NamedTextColor.GRAY)
                )

                return true
            }
        }
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): List<String> {
        return when (args.size) {
            1 -> {
                val commands = mutableListOf("download", "status", "help")
                if (sender.hasPermission("charmedChars.textures.admin")) {
                    commands.add("regenerate")
                }
                commands.filter { it.startsWith(args[0], ignoreCase = true) }
            }
            else -> emptyList()
        }
    }
}