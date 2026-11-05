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
package org.stephanosbad.charmedchars.commands

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Chunk
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.generator.structure.Structure
import org.stephanosbad.charmedchars.CharmedChars
import org.stephanosbad.charmedchars.database.StructureType

/**
 * Command handler to view structure three-digit codes
 *
 * Allows players with permission to view the assigned three-digit number
 * for the fortress or bastion remnant they are currently standing in.
 * This is useful for debugging or assisting other players.
 *
 * Requires charmedchars.blocks permission.
 *
 * @property plugin Reference to the main plugin instance
 */
class StructureCodeCommand(private val plugin: CharmedChars) : CommandExecutor {

    /**
     * Executes the /structurecode command
     *
     * Checks if the sender is a player with the required permission, then
     * detects if they are in a fortress or bastion remnant and displays
     * the assigned three-digit code.
     *
     * @param sender The command sender
     * @param command The command instance
     * @param label The command label used
     * @param args Command arguments (unused)
     * @return true if the command was handled successfully
     */
    override fun onCommand(sender: CommandSender, _command: Command, _label: String, _args: Array<out String>): Boolean {
        // Check if sender is a player
        if (sender !is Player) {
            sender.sendMessage(
                Component.text("This command can only be used by players.")
                    .color(NamedTextColor.RED)
            )
            return true
        }

        // Check permission
        if (!sender.hasPermission("charmedchars.blocks")) {
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

        val registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.STRUCTURE)

        val fortress = registry.get(NamespacedKey.minecraft("fortress"))
        if (announceStructureCode(
                sender, chunk, fortress, StructureType.FORTRESS, location.world.name,
                headerText = "NETHER FORTRESS CODE", borderColor = NamedTextColor.GOLD, codeColor = NamedTextColor.YELLOW
            )
        ) return true

        val bastionRemnant = registry.get(NamespacedKey.minecraft("bastion_remnant"))
        if (announceStructureCode(
                sender, chunk, bastionRemnant, StructureType.BASTION_REMNANT, location.world.name,
                headerText = "BASTION REMNANT CODE", borderColor = NamedTextColor.LIGHT_PURPLE, codeColor = NamedTextColor.DARK_PURPLE
            )
        ) return true

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

    /**
     * Looks up [structureKey] in [chunk] and, if found and recorded in the database, sends
     * [sender] its three-digit code. Returns true if the caller should stop checking further
     * structure types - either because the code was shown, or because structure data in the
     * chunk couldn't be read. Returns false both when [structureKey] isn't present in the chunk
     * and when it is present but not yet in the database, so the caller can fall through to the
     * next structure type (matching the original un-refactored behavior).
     */
    private fun announceStructureCode(
        sender: Player,
        chunk: Chunk,
        structureKey: Structure?,
        databaseType: StructureType,
        worldName: String,
        headerText: String,
        borderColor: NamedTextColor,
        codeColor: NamedTextColor
    ): Boolean {
        if (structureKey == null || chunk.getStructures(structureKey).isEmpty()) return false

        val structure = chunk.getStructures(structureKey).firstOrNull()
        if (structure == null) {
            sender.sendMessage(
                Component.text("Error: Could not get structure data.")
                    .color(NamedTextColor.RED)
            )
            return true
        }

        val boundingBox = structure.boundingBox
        // Use Math.floorDiv for proper handling of negative coordinates
        val originChunkX = Math.floorDiv(boundingBox.minX.toInt(), 16)
        val originChunkZ = Math.floorDiv(boundingBox.minZ.toInt(), 16)

        val data = plugin.structureDatabase.getStructure(
            worldName = worldName,
            structureType = databaseType,
            chunkX = originChunkX,
            chunkZ = originChunkZ
        ) ?: return false

        sender.sendMessage(
            Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .color(borderColor)
        )
        sender.sendMessage(
            Component.text("  $headerText")
                .color(borderColor)
                .decorate(TextDecoration.BOLD)
        )
        sender.sendMessage(
            Component.text("  Code: ${data.assignedNumber}")
                .color(codeColor)
                .decorate(TextDecoration.BOLD)
        )
        sender.sendMessage(
            Component.text("  Rewards dispensed: ${if (data.rewardsDispensed) "Yes" else "No"}")
                .color(if (data.rewardsDispensed) NamedTextColor.RED else NamedTextColor.GREEN)
        )
        sender.sendMessage(
            Component.text("  Origin: ($originChunkX, $originChunkZ)")
                .color(NamedTextColor.GRAY)
        )
        sender.sendMessage(
            Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .color(borderColor)
        )
        return true
    }
}
