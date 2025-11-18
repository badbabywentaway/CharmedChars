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
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.stephanosbad.charmedChars.CharmedChars
import org.stephanosbad.charmedChars.database.StructureType

/**
 * Command handler for administrators to view structure three-digit codes
 *
 * Allows server administrators to view the assigned three-digit number
 * for the fortress or bastion remnant they are currently standing in.
 * This is useful for debugging or assisting players.
 *
 * Requires charmedchars.admin permission.
 *
 * @property plugin Reference to the main plugin instance
 */
class StructureCodeCommand(private val plugin: CharmedChars) : CommandExecutor {

    /**
     * Executes the /structurecode command
     *
     * Checks if the sender is a player with admin permissions, then
     * detects if they are in a fortress or bastion remnant and displays
     * the assigned three-digit code.
     *
     * @param sender The command sender
     * @param command The command instance
     * @param label The command label used
     * @param args Command arguments (unused)
     * @return true if the command was handled successfully
     */
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        // Check if sender is a player
        if (sender !is Player) {
            sender.sendMessage(
                Component.text("This command can only be used by players.")
                    .color(NamedTextColor.RED)
            )
            return true
        }

        // Check permission
        if (!sender.hasPermission("charmedchars.admin")) {
            sender.sendMessage(
                Component.text("You don't have permission to use this command.")
                    .color(NamedTextColor.RED)
            )
            return true
        }

        val location = sender.location
        val chunk = location.chunk

        // Check if player is in the Nether
        if (!location.world.environment.name.equals("NETHER", ignoreCase = true)) {
            sender.sendMessage(
                Component.text("You must be in the Nether to use this command.")
                    .color(NamedTextColor.RED)
            )
            return true
        }

        val registry = Registry.STRUCTURE

        // Check for Nether Fortress
        val fortress = registry.get(NamespacedKey.minecraft("fortress"))
        if (fortress != null && chunk.getStructures(fortress).isNotEmpty()) {
            val fortressData = plugin.structureDatabase.getStructure(
                worldName = location.world.name,
                structureType = StructureType.FORTRESS,
                chunkX = chunk.x,
                chunkZ = chunk.z
            )

            if (fortressData != null) {
                sender.sendMessage(
                    Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                        .color(NamedTextColor.GOLD)
                )
                sender.sendMessage(
                    Component.text("  NETHER FORTRESS CODE")
                        .color(NamedTextColor.GOLD)
                        .decorate(TextDecoration.BOLD)
                )
                sender.sendMessage(
                    Component.text("  Code: ${fortressData.assignedNumber}")
                        .color(NamedTextColor.YELLOW)
                        .decorate(TextDecoration.BOLD)
                )
                sender.sendMessage(
                    Component.text("  Rewards dispensed: ${if (fortressData.rewardsDispensed) "Yes" else "No"}")
                        .color(if (fortressData.rewardsDispensed) NamedTextColor.RED else NamedTextColor.GREEN)
                )
                sender.sendMessage(
                    Component.text("  Location: (${chunk.x}, ${chunk.z})")
                        .color(NamedTextColor.GRAY)
                )
                sender.sendMessage(
                    Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                        .color(NamedTextColor.GOLD)
                )
                return true
            }
        }

        // Check for Bastion Remnant
        val bastionRemnant = registry.get(NamespacedKey.minecraft("bastion_remnant"))
        if (bastionRemnant != null && chunk.getStructures(bastionRemnant).isNotEmpty()) {
            val bastionData = plugin.structureDatabase.getStructure(
                worldName = location.world.name,
                structureType = StructureType.BASTION_REMNANT,
                chunkX = chunk.x,
                chunkZ = chunk.z
            )

            if (bastionData != null) {
                sender.sendMessage(
                    Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                        .color(NamedTextColor.LIGHT_PURPLE)
                )
                sender.sendMessage(
                    Component.text("  BASTION REMNANT CODE")
                        .color(NamedTextColor.LIGHT_PURPLE)
                        .decorate(TextDecoration.BOLD)
                )
                sender.sendMessage(
                    Component.text("  Code: ${bastionData.assignedNumber}")
                        .color(NamedTextColor.DARK_PURPLE)
                        .decorate(TextDecoration.BOLD)
                )
                sender.sendMessage(
                    Component.text("  Rewards dispensed: ${if (bastionData.rewardsDispensed) "Yes" else "No"}")
                        .color(if (bastionData.rewardsDispensed) NamedTextColor.RED else NamedTextColor.GREEN)
                )
                sender.sendMessage(
                    Component.text("  Location: (${chunk.x}, ${chunk.z})")
                        .color(NamedTextColor.GRAY)
                )
                sender.sendMessage(
                    Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                        .color(NamedTextColor.LIGHT_PURPLE)
                )
                return true
            }
        }

        // Not in any structure
        sender.sendMessage(
            Component.text("You are not currently in a Nether Fortress or Bastion Remnant.")
                .color(NamedTextColor.YELLOW)
        )
        sender.sendMessage(
            Component.text("Stand inside a structure to view its code.")
                .color(NamedTextColor.GRAY)
        )

        return true
    }
}
