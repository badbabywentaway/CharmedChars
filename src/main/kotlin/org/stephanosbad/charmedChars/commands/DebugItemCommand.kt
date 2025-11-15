package org.stephanosbad.charmedChars.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.stephanosbad.charmedChars.CharmedChars

class DebugItemCommand(private val plugin: CharmedChars) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("This command can only be used by players")
            return true
        }

        val item = sender.inventory.itemInMainHand
        if (item.type.isAir) {
            sender.sendMessage(
                Component.text("Hold an item in your main hand")
                    .color(NamedTextColor.RED)
            )
            return true
        }

        val meta = item.itemMeta
        if (meta == null) {
            sender.sendMessage(
                Component.text("Item has no metadata")
                    .color(NamedTextColor.RED)
            )
            return true
        }

        sender.sendMessage(
            Component.text("=== Item Debug Info ===")
                .color(NamedTextColor.GOLD)
        )
        sender.sendMessage(
            Component.text("Material: ${item.type}")
                .color(NamedTextColor.YELLOW)
        )
        sender.sendMessage(
            Component.text("Has Custom Model Data: ${meta.hasCustomModelData()}")
                .color(NamedTextColor.YELLOW)
        )
        if (meta.hasCustomModelData()) {
            val customModelData = meta.customModelData
            sender.sendMessage(
                Component.text("Custom Model Data: $customModelData")
                    .color(NamedTextColor.GREEN)
            )

            // Decode what this custom model data means
            val colorOffset = when {
                customModelData >= 1300 -> "YELLOW (offset 1300)"
                customModelData >= 1200 -> "MAGENTA (offset 1200)"
                customModelData >= 1100 -> "CYAN (offset 1100)"
                else -> "UNKNOWN"
            }
            sender.sendMessage(
                Component.text("Color: $colorOffset")
                    .color(NamedTextColor.AQUA)
            )

            val baseOffset = when {
                customModelData >= 1300 -> 1300
                customModelData >= 1200 -> 1200
                customModelData >= 1100 -> 1100
                else -> 0
            }
            val relativeValue = customModelData - baseOffset
            val note = relativeValue % 25
            val instrumentIndex = (relativeValue / 25) % 16

            sender.sendMessage(
                Component.text("When placed: instrument index=$instrumentIndex, note=$note")
                    .color(NamedTextColor.YELLOW)
            )
        }
        if (meta.hasDisplayName()) {
            sender.sendMessage(
                Component.text("Display Name: ")
                    .color(NamedTextColor.YELLOW)
                    .append(meta.displayName()!!)
            )
        }

        return true
    }
}
