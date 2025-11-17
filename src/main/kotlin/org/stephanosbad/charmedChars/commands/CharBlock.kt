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
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.StringUtil
import org.stephanosbad.charmedChars.items.BlockColor
import org.stephanosbad.charmedChars.items.LetterBlock
import org.stephanosbad.charmedChars.items.NonAlphaNumBlocks
import org.stephanosbad.charmedChars.items.NumericBlock
import java.util.*

/**
 * Command handler for giving character blocks to players
 *
 * Allows administrators to give letter blocks, number blocks, and operator blocks
 * to players with a specified color. Validates player names, colors, and characters,
 * and provides helpful error messages and setup instructions if ItemsAdder is not configured.
 */
class CharBlock : CommandExecutor, TabCompleter {

    /**
     * Executes the /charblock command
     *
     * @param sender The command sender (player or console)
     * @param command The command instance
     * @param label The command label used
     * @param args Command arguments: [player] [color] [text]
     * @return true if the command was handled successfully
     */
    override fun onCommand(sender: CommandSender, command: Command, label: kotlin.String, args: Array<out kotlin.String>): Boolean {
        // Check permission
        if (!sender.hasPermission("charmedChars.blocks")) {
            sender.sendMessage(
                Component.text("You don't have permission to use this command.")
                    .color(NamedTextColor.RED)
            )
            return true
        }

        if (args.size < 3) {
            sender.sendMessage(
                Component.text("Usage: /charblock <player> <color> <text>")
                    .color(NamedTextColor.RED)
            )
            sender.sendMessage(
                Component.text("Colors: cyan, magenta, yellow")
                    .color(NamedTextColor.GRAY)
            )
            return true
        }

        val givePlayerName = args[0]
        val givePlayer = Bukkit.getPlayerExact(givePlayerName)

        if (givePlayer == null || !givePlayer.isOnline) {
            sender.sendMessage(
                Component.text("Player '$givePlayerName' not found or offline.")
                    .color(NamedTextColor.RED)
            )
            return true
        }

        val colorName = args[1]
        val blockColor =  BlockColor.entries.firstOrNull { colorName.lowercase() ==  it.name.lowercase()}
        if(blockColor == null)
        {
            sender.sendMessage(
                Component.text("Invalid color '$colorName'. Use: cyan, magenta, or yellow")
                    .color(NamedTextColor.RED)
            )
            return true
        }

        val characterString = args[2].lowercase()
        var blocksGiven = 0

        for (c in characterString.toCharArray()) {
            var itemStack: ItemStack? = null

            // Try non-alphanumeric blocks first
            for (test in NonAlphaNumBlocks.entries) {
                if (test.charVal == c) {
                    itemStack = test.itemStacks[blockColor]?.clone()
                }
            }

            if (itemStack == null) {
                // Try letter blocks (uppercase)
                val isThere =
                    Arrays.stream(LetterBlock.entries.toTypedArray()).filter { it -> it.character == c.uppercaseChar() }.findFirst()
                if (!isThere.isEmpty) {
                    itemStack = isThere.get().itemStacks[blockColor]?.clone()
                } else {
                    // Try number blocks
                    val isThereNum =
                        Arrays.stream(NumericBlock.entries.toTypedArray()).filter { it -> it.c == c }.findFirst()
                    if (!isThereNum.isEmpty) {
                        itemStack = isThereNum.get().itemStacks[blockColor]?.clone()
                    }
                }
            }

            if (itemStack != null) {
                // Give directly to inventory, or drop if full
                val remaining = givePlayer.inventory.addItem(itemStack)
                if (remaining.isNotEmpty()) {
                    // Inventory full, drop the item
                    givePlayer.location.world?.dropItemNaturally(givePlayer.location, itemStack)
                }
                blocksGiven++
            }
        }

        if (blocksGiven > 0) {
            sender.sendMessage(
                Component.text("Gave $blocksGiven ${blockColor.name.lowercase()} blocks to ${givePlayer.name}")
                    .color(NamedTextColor.GREEN)
            )
            givePlayer.sendMessage(
                Component.text("You received $blocksGiven ${blockColor.name.lowercase()} character blocks")
                    .color(NamedTextColor.GREEN)
            )
        } else {
            sender.sendMessage(
                Component.text("No valid characters found in '$characterString'")
                    .color(NamedTextColor.RED)
            )
            sender.sendMessage(
                Component.text("ItemsAdder hasn't been configured with CharmedChars blocks yet.")
                    .color(NamedTextColor.YELLOW)
            )
            sender.sendMessage(Component.text(""))
            sender.sendMessage(
                Component.text("Quick Setup (3 steps):")
                    .color(NamedTextColor.GOLD)
            )
            sender.sendMessage(
                Component.text("  1. Run /iasetup (auto-copies configs & textures)")
                    .color(NamedTextColor.GRAY)
            )
            sender.sendMessage(
                Component.text("  2. Run /iazip (generates resource pack)")
                    .color(NamedTextColor.GRAY)
            )
            sender.sendMessage(
                Component.text("  3. Restart server")
                    .color(NamedTextColor.GRAY)
            )
            sender.sendMessage(Component.text(""))
            sender.sendMessage(
                Component.text("Check status: /iastatus")
                    .color(NamedTextColor.AQUA)
            )
        }

        return true
    }

    /**
     * Provides tab completion for the /charblock command
     *
     * @param sender The command sender
     * @param cmd The command instance
     * @param label The command label
     * @param args Current command arguments
     * @return List of completion suggestions
     */
    override fun onTabComplete(
        sender: CommandSender,
        cmd: Command,
        label: kotlin.String,
        args: Array<out kotlin.String>
    ): List<kotlin.String?>? {
        var completions: MutableList<kotlin.String?> = ArrayList<kotlin.String?>()
        val mainArg: kotlin.String?

        if (args.isEmpty()) return null


        if (args.size == 1) {
            mainArg = args[0].lowercase()
            val onlinePlayers: MutableList<kotlin.String?> = ArrayList<kotlin.String?>()
            for (p in Bukkit.getOnlinePlayers()) {
                onlinePlayers.add(p.name)
            }
            StringUtil.copyPartialMatches<MutableList<kotlin.String?>?>(mainArg, onlinePlayers, completions)
        }

        if (args.size == 2) {
            val player = args[1].lowercase()
            val colorMatches: MutableList<kotlin.String?> = ArrayList<kotlin.String?>()
            for (c in BlockColor.entries)
            {
                colorMatches.add(c.name.lowercase())
            }
            StringUtil.copyPartialMatches<MutableList<kotlin.String?>?>(player, colorMatches, completions)
        }

        if (args.size == 3) {
            val characterMatches: MutableList<kotlin.String?> = ArrayList<kotlin.String?>()
            for (letter in LetterBlock.entries) {
                characterMatches.add(String.valueOf(letter.character))
            }
            for (number in NumericBlock.entries) {
                characterMatches.add(String.valueOf(number.c))
            }
            for (non in NonAlphaNumBlocks.entries) {
                characterMatches.add(non.nonAlphaNumBlockName.lowercase().split("_")[0])
            }
            completions = characterMatches
        }


        return completions
    }





    companion object {
        /**
         * The command name used to register this command executor
         */
        var CommandName: kotlin.String = "charblock"
    }
}
