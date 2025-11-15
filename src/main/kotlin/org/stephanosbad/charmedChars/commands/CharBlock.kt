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
import java.lang.String
import java.util.*
import kotlin.Array
import kotlin.Boolean

class CharBlock : CommandExecutor, TabCompleter {
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
                    .color(NamedTextColor.YELLOW)
            )
        }

        return true
    }

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
         * String which to use for registering this command
         */
        var CommandName: kotlin.String = "charblock"
    }
}
