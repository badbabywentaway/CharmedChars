package org.stephanosbad.charmedChars.commands


import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.stephanosbad.charmedChars.block.CustomBlocks
import org.stephanosbad.charmedChars.CharmedChars

/**
 * Command to give custom blocks to players
 */
class BlocksCommand(private val plugin: CharmedChars) : CommandExecutor, TabCompleter {

    private val blockTypes = listOf(
        CustomBlocks.MAGIC_STONE,
        CustomBlocks.ENCHANTED_LOG,
        CustomBlocks.CRYSTAL_BLOCK
    )

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (!sender.hasPermission("charmedChars.blocks")) {
            sender.sendMessage(
                Component.text("You don't have permission to use this command!")
                    .color(NamedTextColor.RED)
            )
            return true
        }

        when (args.size) {
            0 -> {
                // List available blocks
                sender.sendMessage(
                    Component.text("Available Custom Blocks:")
                        .color(NamedTextColor.GOLD)
                )

                blockTypes.forEach { blockType ->
                    sender.sendMessage(
                        Component.text("  • $blockType")
                            .color(NamedTextColor.YELLOW)
                    )
                }

                sender.sendMessage(
                    Component.text("Usage: /blocks give <type> [player] [amount]")
                        .color(NamedTextColor.GRAY)
                )

                return true
            }

            1 -> {
                if (args[0].equals("list", ignoreCase = true)) {
                    return onCommand(sender, command, label, emptyArray())
                }

                sender.sendMessage(
                    Component.text("Usage: /blocks give <type> [player] [amount]")
                        .color(NamedTextColor.YELLOW)
                )
                return true
            }

            2, 3, 4 -> {
                if (!args[0].equals("give", ignoreCase = true)) {
                    sender.sendMessage(
                        Component.text("Unknown subcommand: ${args[0]}")
                            .color(NamedTextColor.RED)
                    )
                    return true
                }

                val blockType = args[1].lowercase()
                if (blockType !in blockTypes) {
                    sender.sendMessage(
                        Component.text("Invalid block type: $blockType")
                            .color(NamedTextColor.RED)
                    )
                    return true
                }

                // Determine target player
                val targetPlayer = when {
                    args.size >= 3 -> {
                        plugin.server.getPlayer(args[2]) ?: run {
                            sender.sendMessage(
                                Component.text("Player '${args[2]}' not found!")
                                    .color(NamedTextColor.RED)
                            )
                            return true
                        }
                    }
                    sender is Player -> sender
                    else -> {
                        sender.sendMessage(
                            Component.text("You must specify a player when using from console!")
                                .color(NamedTextColor.RED)
                        )
                        return true
                    }
                }

                // Determine amount
                val amount = if (args.size >= 4) {
                    args[3].toIntOrNull() ?: run {
                        sender.sendMessage(
                            Component.text("Invalid amount: ${args[3]}")
                                .color(NamedTextColor.RED)
                        )
                        return true
                    }
                } else {
                    1
                }

                if (amount <= 0 || amount > 64) {
                    sender.sendMessage(
                        Component.text("Amount must be between 1 and 64!")
                            .color(NamedTextColor.RED)
                    )
                    return true
                }

                // Give the custom block
                val customBlock = plugin.customBlocks.getCustomBlockItem(blockType)
                if (customBlock == null) {
                    sender.sendMessage(
                        Component.text("Failed to create custom block: $blockType")
                            .color(NamedTextColor.RED)
                    )
                    return true
                }

                customBlock.amount = amount

                val leftOver = targetPlayer.inventory.addItem(customBlock)
                if (leftOver.isNotEmpty()) {
                    // Drop items that don't fit
                    leftOver.values.forEach { item ->
                        targetPlayer.world.dropItemNaturally(targetPlayer.location, item)
                    }
                }

                // Send success messages
                sender.sendMessage(
                    Component.text("Gave $amount × $blockType to ${targetPlayer.name}")
                        .color(NamedTextColor.GREEN)
                )

                if (sender != targetPlayer) {
                    targetPlayer.sendMessage(
                        Component.text("You received $amount × $blockType from ${sender.name}")
                            .color(NamedTextColor.GREEN)
                    )
                }

                return true
            }

            else -> {
                sender.sendMessage(
                    Component.text("Usage: /blocks give <type> [player] [amount]")
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
                listOf("give", "list").filter { it.startsWith(args[0], ignoreCase = true) }
            }

            2 -> {
                if (args[0].equals("give", ignoreCase = true)) {
                    blockTypes.filter { it.startsWith(args[1], ignoreCase = true) }
                } else {
                    emptyList()
                }
            }

            3 -> {
                if (args[0].equals("give", ignoreCase = true)) {
                    plugin.server.onlinePlayers
                        .map { it.name }
                        .filter { it.startsWith(args[2], ignoreCase = true) }
                        .sorted()
                } else {
                    emptyList()
                }
            }

            4 -> {
                if (args[0].equals("give", ignoreCase = true)) {
                    listOf("1", "8", "16", "32", "64").filter { it.startsWith(args[3]) }
                } else {
                    emptyList()
                }
            }

            else -> emptyList()
        }
    }
}

