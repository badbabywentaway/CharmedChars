package org.stephanosbad.charmedChars.Commands

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.stephanosbad.charmedChars.CharmedChars

class ExampleCommand(private val plugin: CharmedChars) : CommandExecutor, TabCompleter {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        when (args.size) {
            0 -> {
                if (sender !is Player) {
                    sender.sendMessage(
                        Component.text("This command can only be used by players!")
                            .color(NamedTextColor.RED)
                    )
                    return true
                }

                if (!sender.hasPermission("myplugin.example")) {
                    sender.sendMessage(
                        Component.text("You don't have permission to use this command!")
                            .color(NamedTextColor.RED)
                    )
                    return true
                }

                // Async greeting with delay
                plugin.launch {
                    sender.sendMessage(
                        Component.text("Preparing your greeting...")
                            .color(NamedTextColor.YELLOW)
                    )

                    delay(1500) // Simulate some work

                    sender.sendMessage(
                        Component.text()
                            .append(Component.text("Hello from ").color(NamedTextColor.GREEN))
                            .append(
                                Component.text("MyMinecraftPlugin")
                                    .color(NamedTextColor.GOLD)
                                    .decorate(TextDecoration.BOLD)
                            )
                            .append(Component.text("! (Built with Gradle & Kotlin)").color(NamedTextColor.GREEN))
                            .build()
                    )
                }

                return true
            }

            1 -> {
                if (!sender.hasPermission("myplugin.admin")) {
                    sender.sendMessage(
                        Component.text("You need admin permission to target other players!")
                            .color(NamedTextColor.RED)
                    )
                    return true
                }

                val targetPlayer = Bukkit.getPlayer(args[0])
                if (targetPlayer == null) {
                    sender.sendMessage(
                        Component.text("Player '${args[0]}' not found!")
                            .color(NamedTextColor.RED)
                    )
                    return true
                }

                // Async operation
                plugin.launch {
                    targetPlayer.sendMessage(
                        Component.text("You've been greeted by ${sender.name}! ✨")
                            .color(NamedTextColor.AQUA)
                    )

                    delay(500)

                    sender.sendMessage(
                        Component.text("Greeting sent to ${targetPlayer.name}!")
                            .color(NamedTextColor.GREEN)
                    )
                }

                return true
            }

            else -> {
                sender.sendMessage(
                    Component.text("Usage: /example [player]")
                        .color(NamedTextColor.YELLOW)
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
                if (sender.hasPermission("myplugin.admin")) {
                    Bukkit.getOnlinePlayers()
                        .map { it.name }
                        .filter { it.startsWith(args[0], ignoreCase = true) }
                        .sorted()
                } else {
                    emptyList()
                }
            }
            else -> emptyList()
        }
    }
}