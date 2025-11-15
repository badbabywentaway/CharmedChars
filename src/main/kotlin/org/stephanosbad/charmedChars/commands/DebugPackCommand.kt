package org.stephanosbad.charmedChars.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.stephanosbad.charmedChars.CharmedChars
import java.io.File

class DebugPackCommand(private val plugin: CharmedChars) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        sender.sendMessage(
            Component.text("=== Resource Pack Debug Info ===")
                .color(NamedTextColor.GOLD)
        )

        // Check if resource pack exists
        val resourcePackFile = File(plugin.dataFolder, "CharmedChars-ResourcePack.zip")
        sender.sendMessage(
            Component.text("Resource Pack Exists: ${resourcePackFile.exists()}")
                .color(if (resourcePackFile.exists()) NamedTextColor.GREEN else NamedTextColor.RED)
        )

        if (resourcePackFile.exists()) {
            val sizeInKB = resourcePackFile.length() / 1024
            sender.sendMessage(
                Component.text("Resource Pack Size: $sizeInKB KB")
                    .color(NamedTextColor.YELLOW)
            )
            sender.sendMessage(
                Component.text("Last Modified: ${java.util.Date(resourcePackFile.lastModified())}")
                    .color(NamedTextColor.YELLOW)
            )
        }

        // Check pack.mcmeta
        val resourcePackDir = File(plugin.dataFolder, "resourcepack")
        val packMcmeta = File(resourcePackDir, "pack.mcmeta")
        sender.sendMessage(
            Component.text("pack.mcmeta Exists: ${packMcmeta.exists()}")
                .color(if (packMcmeta.exists()) NamedTextColor.GREEN else NamedTextColor.RED)
        )

        if (packMcmeta.exists()) {
            try {
                val content = packMcmeta.readText()
                sender.sendMessage(
                    Component.text("pack.mcmeta content:")
                        .color(NamedTextColor.YELLOW)
                )
                content.lines().forEach { line ->
                    sender.sendMessage(
                        Component.text("  $line")
                            .color(NamedTextColor.GRAY)
                    )
                }
            } catch (e: Exception) {
                sender.sendMessage(
                    Component.text("Error reading pack.mcmeta: ${e.message}")
                        .color(NamedTextColor.RED)
                )
            }
        }

        // Check note_block.json
        val noteBlockJson = File(resourcePackDir, "assets/minecraft/models/item/note_block.json")
        sender.sendMessage(
            Component.text("note_block.json Exists: ${noteBlockJson.exists()}")
                .color(if (noteBlockJson.exists()) NamedTextColor.GREEN else NamedTextColor.RED)
        )

        if (noteBlockJson.exists()) {
            try {
                val content = noteBlockJson.readText()
                val lines = content.lines()
                sender.sendMessage(
                    Component.text("note_block.json (first 10 lines):")
                        .color(NamedTextColor.YELLOW)
                )
                lines.take(10).forEach { line ->
                    sender.sendMessage(
                        Component.text("  $line")
                            .color(NamedTextColor.GRAY)
                    )
                }
                if (lines.size > 10) {
                    sender.sendMessage(
                        Component.text("  ... (${lines.size - 10} more lines)")
                            .color(NamedTextColor.GRAY)
                    )
                }
            } catch (e: Exception) {
                sender.sendMessage(
                    Component.text("Error reading note_block.json: ${e.message}")
                        .color(NamedTextColor.RED)
                )
            }
        }

        // Check custom textures config
        sender.sendMessage(
            Component.text("Custom Textures Enabled: ${plugin.configManager.customTexturesEnabled}")
                .color(if (plugin.configManager.customTexturesEnabled) NamedTextColor.GREEN else NamedTextColor.RED)
        )

        // Resource pack server status
        if (plugin.isResourcePackServerInitialized) {
            sender.sendMessage(
                Component.text("Resource Pack Server Running: ${plugin.resourcePackServer.isRunning()}")
                    .color(if (plugin.resourcePackServer.isRunning()) NamedTextColor.GREEN else NamedTextColor.RED)
            )
            if (plugin.resourcePackServer.isRunning()) {
                sender.sendMessage(
                    Component.text("Resource Pack URL: ${plugin.resourcePackServer.getResourcePackUrl()}")
                        .color(NamedTextColor.AQUA)
                )
            }
        }

        return true
    }
}
