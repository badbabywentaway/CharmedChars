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
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.stephanosbad.charmedChars.CharmedChars
import org.stephanosbad.charmedChars.database.StructureType
import java.text.SimpleDateFormat
import java.util.*

/**
 * Command handler for viewing and managing structure database information
 *
 * Provides subcommands to query, inspect, and manage the structure database:
 * - list [world] - List all structures, optionally filtered by world
 * - player <playerName> - View structures discovered by a specific player
 * - lookup <number> - Look up a structure by its assigned three-digit number
 * - stats - Show database statistics
 * - remove <number> - Remove a structure by its assigned number
 * - purge [all|world|fortress|bastion] - Delete structures by criteria
 *
 * Requires charmedchars.blocks permission.
 *
 * @property plugin Reference to the main plugin instance
 */
class StructureDatabaseCommand(private val plugin: CharmedChars) : CommandExecutor, TabCompleter {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    /**
     * Executes the /structuredb command
     *
     * Routes to appropriate subcommand handler based on arguments.
     *
     * @param sender The command sender
     * @param command The command instance
     * @param label The command label used
     * @param args Command arguments
     * @return true if the command was handled successfully
     */
    override fun onCommand(sender: CommandSender, _command: Command, _label: String, args: Array<out String>): Boolean {
        // Check permission
        if (!sender.hasPermission("charmedchars.blocks")) {
            sender.sendMessage(
                Component.text("You don't have permission to use this command.")
                    .color(NamedTextColor.RED)
            )
            return true
        }

        if (args.isEmpty()) {
            showHelp(sender)
            return true
        }

        when (args[0].lowercase()) {
            "list" -> handleList(sender, args)
            "player" -> handlePlayer(sender, args)
            "lookup" -> handleLookup(sender, args)
            "stats" -> handleStats(sender)
            "remove" -> handleRemove(sender, args)
            "purge" -> handlePurge(sender, args)
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

    /**
     * Shows help message with available subcommands
     */
    private fun showHelp(sender: CommandSender) {
        sender.sendMessage(
            Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .color(NamedTextColor.GOLD)
        )
        sender.sendMessage(
            Component.text("  Structure Database Commands")
                .color(NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD)
        )
        sender.sendMessage(
            Component.text("  /structuredb list [world]")
                .color(NamedTextColor.YELLOW)
        )
        sender.sendMessage(
            Component.text("    - List all structures (optionally filtered by world)")
                .color(NamedTextColor.GRAY)
        )
        sender.sendMessage(
            Component.text("  /structuredb player <playerName>")
                .color(NamedTextColor.YELLOW)
        )
        sender.sendMessage(
            Component.text("    - View structures discovered by a player")
                .color(NamedTextColor.GRAY)
        )
        sender.sendMessage(
            Component.text("  /structuredb lookup <number>")
                .color(NamedTextColor.YELLOW)
        )
        sender.sendMessage(
            Component.text("    - Look up a structure by its assigned number")
                .color(NamedTextColor.GRAY)
        )
        sender.sendMessage(
            Component.text("  /structuredb stats")
                .color(NamedTextColor.YELLOW)
        )
        sender.sendMessage(
            Component.text("    - Show database statistics")
                .color(NamedTextColor.GRAY)
        )
        sender.sendMessage(
            Component.text("  /structuredb remove <number>")
                .color(NamedTextColor.RED)
        )
        sender.sendMessage(
            Component.text("    - Remove a structure by its assigned number")
                .color(NamedTextColor.GRAY)
        )
        sender.sendMessage(
            Component.text("  /structuredb purge <all|world|fortress|bastion> [worldName]")
                .color(NamedTextColor.DARK_RED)
        )
        sender.sendMessage(
            Component.text("    - Delete structures (all, by world, or by type)")
                .color(NamedTextColor.GRAY)
        )
        sender.sendMessage(
            Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .color(NamedTextColor.GOLD)
        )
    }

    /**
     * Handles the 'list' subcommand
     */
    private fun handleList(sender: CommandSender, args: Array<out String>) {
        val structures = if (args.size > 1) {
            val worldName = args[1]
            plugin.structureDatabase.getStructuresByWorld(worldName)
        } else {
            plugin.structureDatabase.getAllStructures()
        }

        if (structures.isEmpty()) {
            sender.sendMessage(
                Component.text("No structures found in the database.")
                    .color(NamedTextColor.YELLOW)
            )
            return
        }

        sender.sendMessage(
            Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .color(NamedTextColor.AQUA)
        )
        sender.sendMessage(
            Component.text("  Structure Database (${structures.size} total)")
                .color(NamedTextColor.AQUA)
                .decorate(TextDecoration.BOLD)
        )
        sender.sendMessage(
            Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .color(NamedTextColor.AQUA)
        )

        for (structure in structures) {
            val playerName = Bukkit.getOfflinePlayer(structure.discoveredBy).name ?: "Unknown"
            val rewardStatus = if (structure.rewardsDispensed) "✓ Claimed" else "✗ Unclaimed"
            val rewardColor = if (structure.rewardsDispensed) NamedTextColor.GREEN else NamedTextColor.RED

            sender.sendMessage(
                Component.text("${structure.structureType.displayName} #${structure.assignedNumber}")
                    .color(NamedTextColor.WHITE)
                    .decorate(TextDecoration.BOLD)
            )
            sender.sendMessage(
                Component.text("  World: ${structure.worldName} | Chunk: (${structure.chunkX}, ${structure.chunkZ})")
                    .color(NamedTextColor.GRAY)
            )
            sender.sendMessage(
                Component.text("  Discovered by: $playerName")
                    .color(NamedTextColor.GRAY)
            )
            sender.sendMessage(
                Component.text("  Rewards: $rewardStatus")
                    .color(rewardColor)
            )
            sender.sendMessage(Component.text(""))
        }
    }

    /**
     * Handles the 'player' subcommand
     */
    private fun handlePlayer(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2) {
            sender.sendMessage(
                Component.text("Usage: /structuredb player <playerName>")
                    .color(NamedTextColor.RED)
            )
            return
        }

        val playerName = args[1]
        val offlinePlayer = Bukkit.getOfflinePlayer(playerName)

        if (!offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline) {
            sender.sendMessage(
                Component.text("Player '$playerName' not found.")
                    .color(NamedTextColor.RED)
            )
            return
        }

        val structures = plugin.structureDatabase.getStructuresByPlayer(offlinePlayer.uniqueId)

        if (structures.isEmpty()) {
            sender.sendMessage(
                Component.text("Player '$playerName' has not discovered any structures.")
                    .color(NamedTextColor.YELLOW)
            )
            return
        }

        sender.sendMessage(
            Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .color(NamedTextColor.LIGHT_PURPLE)
        )
        sender.sendMessage(
            Component.text("  Structures discovered by $playerName")
                .color(NamedTextColor.LIGHT_PURPLE)
                .decorate(TextDecoration.BOLD)
        )
        sender.sendMessage(
            Component.text("  Total: ${structures.size}")
                .color(NamedTextColor.GRAY)
        )
        sender.sendMessage(
            Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .color(NamedTextColor.LIGHT_PURPLE)
        )

        for (structure in structures) {
            val rewardStatus = if (structure.rewardsDispensed) "✓ Claimed" else "✗ Unclaimed"
            val rewardColor = if (structure.rewardsDispensed) NamedTextColor.GREEN else NamedTextColor.RED
            val date = dateFormat.format(Date(structure.discoveredAt))

            sender.sendMessage(
                Component.text("${structure.structureType.displayName} #${structure.assignedNumber}")
                    .color(NamedTextColor.WHITE)
                    .decorate(TextDecoration.BOLD)
            )
            sender.sendMessage(
                Component.text("  World: ${structure.worldName} | Chunk: (${structure.chunkX}, ${structure.chunkZ})")
                    .color(NamedTextColor.GRAY)
            )
            sender.sendMessage(
                Component.text("  Discovered: $date")
                    .color(NamedTextColor.GRAY)
            )
            sender.sendMessage(
                Component.text("  Rewards: $rewardStatus")
                    .color(rewardColor)
            )
            sender.sendMessage(Component.text(""))
        }
    }

    /**
     * Handles the 'lookup' subcommand
     */
    private fun handleLookup(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2) {
            sender.sendMessage(
                Component.text("Usage: /structuredb lookup <number>")
                    .color(NamedTextColor.RED)
            )
            return
        }

        val number = args[1].toIntOrNull()
        if (number == null || number < 100 || number > 999) {
            sender.sendMessage(
                Component.text("Invalid number. Must be between 100 and 999.")
                    .color(NamedTextColor.RED)
            )
            return
        }

        val structure = plugin.structureDatabase.getStructureByNumber(number)
        if (structure == null) {
            sender.sendMessage(
                Component.text("No structure found with number $number.")
                    .color(NamedTextColor.YELLOW)
            )
            return
        }

        val playerName = Bukkit.getOfflinePlayer(structure.discoveredBy).name ?: "Unknown"
        val rewardStatus = if (structure.rewardsDispensed) "✓ Claimed" else "✗ Unclaimed"
        val rewardColor = if (structure.rewardsDispensed) NamedTextColor.GREEN else NamedTextColor.RED
        val date = dateFormat.format(Date(structure.discoveredAt))

        sender.sendMessage(
            Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .color(NamedTextColor.GOLD)
        )
        sender.sendMessage(
            Component.text("  ${structure.structureType.displayName} #${structure.assignedNumber}")
                .color(NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD)
        )
        sender.sendMessage(
            Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .color(NamedTextColor.GOLD)
        )
        sender.sendMessage(
            Component.text("World: ${structure.worldName}")
                .color(NamedTextColor.WHITE)
        )
        sender.sendMessage(
            Component.text("Location: Chunk (${structure.chunkX}, ${structure.chunkZ})")
                .color(NamedTextColor.WHITE)
        )
        sender.sendMessage(
            Component.text("Discovered by: $playerName")
                .color(NamedTextColor.WHITE)
        )
        sender.sendMessage(
            Component.text("Discovered at: $date")
                .color(NamedTextColor.WHITE)
        )
        sender.sendMessage(
            Component.text("Rewards: $rewardStatus")
                .color(rewardColor)
        )
        sender.sendMessage(
            Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .color(NamedTextColor.GOLD)
        )
    }

    /**
     * Handles the 'stats' subcommand
     */
    private fun handleStats(sender: CommandSender) {
        val allStructures = plugin.structureDatabase.getAllStructures()
        val totalStructures = allStructures.size
        val claimedStructures = allStructures.count { it.rewardsDispensed }
        val unclaimedStructures = totalStructures - claimedStructures
        val fortresses = allStructures.count { it.structureType == StructureType.FORTRESS }
        val bastions = allStructures.count { it.structureType == StructureType.BASTION_REMNANT }
        val usedNumbers = allStructures.size
        val availableNumbers = 900 - usedNumbers

        sender.sendMessage(
            Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .color(NamedTextColor.GREEN)
        )
        sender.sendMessage(
            Component.text("  Structure Database Statistics")
                .color(NamedTextColor.GREEN)
                .decorate(TextDecoration.BOLD)
        )
        sender.sendMessage(
            Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .color(NamedTextColor.GREEN)
        )
        sender.sendMessage(
            Component.text("Total structures: $totalStructures")
                .color(NamedTextColor.WHITE)
        )
        sender.sendMessage(
            Component.text("  Fortresses: $fortresses")
                .color(NamedTextColor.GOLD)
        )
        sender.sendMessage(
            Component.text("  Bastions: $bastions")
                .color(NamedTextColor.LIGHT_PURPLE)
        )
        sender.sendMessage(Component.text(""))
        sender.sendMessage(
            Component.text("Rewards claimed: $claimedStructures")
                .color(NamedTextColor.GREEN)
        )
        sender.sendMessage(
            Component.text("Rewards unclaimed: $unclaimedStructures")
                .color(NamedTextColor.RED)
        )
        sender.sendMessage(Component.text(""))
        sender.sendMessage(
            Component.text("Numbers used: $usedNumbers / 900")
                .color(NamedTextColor.YELLOW)
        )
        sender.sendMessage(
            Component.text("Numbers available: $availableNumbers")
                .color(NamedTextColor.AQUA)
        )
        sender.sendMessage(
            Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .color(NamedTextColor.GREEN)
        )
    }

    /**
     * Handles the 'remove' subcommand
     */
    private fun handleRemove(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2) {
            sender.sendMessage(
                Component.text("Usage: /structuredb remove <number>")
                    .color(NamedTextColor.RED)
            )
            return
        }

        val number = args[1].toIntOrNull()
        if (number == null || number < 100 || number > 999) {
            sender.sendMessage(
                Component.text("Invalid number. Must be between 100 and 999.")
                    .color(NamedTextColor.RED)
            )
            return
        }

        // Get structure info before deleting for confirmation message
        val structure = plugin.structureDatabase.getStructureByNumber(number)
        if (structure == null) {
            sender.sendMessage(
                Component.text("No structure found with number $number.")
                    .color(NamedTextColor.YELLOW)
            )
            return
        }

        val deleted = plugin.structureDatabase.deleteStructureByNumber(number)
        if (deleted) {
            sender.sendMessage(
                Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    .color(NamedTextColor.RED)
            )
            sender.sendMessage(
                Component.text("  Structure Removed")
                    .color(NamedTextColor.RED)
                    .decorate(TextDecoration.BOLD)
            )
            sender.sendMessage(
                Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    .color(NamedTextColor.RED)
            )
            sender.sendMessage(
                Component.text("${structure.structureType.displayName} #$number has been deleted.")
                    .color(NamedTextColor.WHITE)
            )
            sender.sendMessage(
                Component.text("World: ${structure.worldName}")
                    .color(NamedTextColor.GRAY)
            )
            sender.sendMessage(
                Component.text("Location: Chunk (${structure.chunkX}, ${structure.chunkZ})")
                    .color(NamedTextColor.GRAY)
            )
            plugin.logger.info("${sender.name} removed structure #$number (${structure.structureType.displayName})")
        } else {
            sender.sendMessage(
                Component.text("Failed to delete structure #$number.")
                    .color(NamedTextColor.RED)
            )
        }
    }

    /**
     * Handles the 'purge' subcommand
     */
    private fun handlePurge(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2) {
            sender.sendMessage(
                Component.text("Usage: /structuredb purge <all|world|fortress|bastion> [worldName]")
                    .color(NamedTextColor.RED)
            )
            return
        }

        val purgeType = args[1].lowercase()
        val deletedCount: Int

        when (purgeType) {
            "all" -> {
                deletedCount = plugin.structureDatabase.purgeAllStructures()
                sender.sendMessage(
                    Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                        .color(NamedTextColor.DARK_RED)
                )
                sender.sendMessage(
                    Component.text("  DATABASE PURGED")
                        .color(NamedTextColor.DARK_RED)
                        .decorate(TextDecoration.BOLD)
                )
                sender.sendMessage(
                    Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                        .color(NamedTextColor.DARK_RED)
                )
                sender.sendMessage(
                    Component.text("Deleted $deletedCount structures from the database.")
                        .color(NamedTextColor.WHITE)
                )
                plugin.logger.warning("${sender.name} purged ALL structures from the database ($deletedCount total)")
            }
            "world" -> {
                if (args.size < 3) {
                    sender.sendMessage(
                        Component.text("Usage: /structuredb purge world <worldName>")
                            .color(NamedTextColor.RED)
                    )
                    return
                }
                val worldName = args[2]
                deletedCount = plugin.structureDatabase.deleteStructuresByWorld(worldName)
                sender.sendMessage(
                    Component.text("Deleted $deletedCount structures from world '$worldName'.")
                        .color(NamedTextColor.YELLOW)
                )
                plugin.logger.info("${sender.name} deleted $deletedCount structures from world '$worldName'")
            }
            "fortress" -> {
                deletedCount = plugin.structureDatabase.deleteStructuresByType(StructureType.FORTRESS)
                sender.sendMessage(
                    Component.text("Deleted $deletedCount Nether Fortress structures.")
                        .color(NamedTextColor.GOLD)
                )
                plugin.logger.info("${sender.name} deleted $deletedCount fortress structures")
            }
            "bastion" -> {
                deletedCount = plugin.structureDatabase.deleteStructuresByType(StructureType.BASTION_REMNANT)
                sender.sendMessage(
                    Component.text("Deleted $deletedCount Bastion Remnant structures.")
                        .color(NamedTextColor.LIGHT_PURPLE)
                )
                plugin.logger.info("${sender.name} deleted $deletedCount bastion structures")
            }
            else -> {
                sender.sendMessage(
                    Component.text("Invalid purge type. Use: all, world, fortress, or bastion")
                        .color(NamedTextColor.RED)
                )
                return
            }
        }
    }

    /**
     * Provides tab completion for command arguments
     */
    override fun onTabComplete(
        sender: CommandSender,
        _command: Command,
        _alias: String,
        args: Array<out String>
    ): List<String>? {
        if (!sender.hasPermission("charmedchars.blocks")) {
            return emptyList()
        }

        return when (args.size) {
            1 -> listOf("list", "player", "lookup", "stats", "remove", "purge").filter { it.startsWith(args[0].lowercase()) }
            2 -> when (args[0].lowercase()) {
                "player" -> Bukkit.getOnlinePlayers().map { it.name }.filter { it.lowercase().startsWith(args[1].lowercase()) }
                "list" -> Bukkit.getWorlds().map { it.name }.filter { it.lowercase().startsWith(args[1].lowercase()) }
                "purge" -> listOf("all", "world", "fortress", "bastion").filter { it.startsWith(args[1].lowercase()) }
                else -> emptyList()
            }
            3 -> when (args[0].lowercase()) {
                "purge" -> if (args[1].lowercase() == "world") {
                    Bukkit.getWorlds().map { it.name }.filter { it.lowercase().startsWith(args[2].lowercase()) }
                } else {
                    emptyList()
                }
                else -> emptyList()
            }
            else -> emptyList()
        }
    }
}
