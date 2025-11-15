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
            sender.sendMessage(
                Component.text("Custom Model Data: ${meta.customModelData}")
                    .color(NamedTextColor.GREEN)
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
