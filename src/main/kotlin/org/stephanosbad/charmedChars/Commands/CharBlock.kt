package org.stephanosbad.charmedChars.Commands

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.inventory.ItemStack
import org.bukkit.util.StringUtil
import org.stephanosbad.charmedChars.Items.LetterBlock
import org.stephanosbad.charmedChars.Items.NonAlphaNumBlocks
import org.stephanosbad.charmedChars.Items.NumericBlock
import java.lang.String
import java.util.*
import kotlin.Array
import kotlin.Boolean

class CharBlock : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: kotlin.String, args: Array<out kotlin.String>): Boolean {
        if (sender !== sender.server.consoleSender) return true

        if (args.size < 2) {
            return true
        }

        val givePlayerName = args[0]
        val givePlayer = Bukkit.getPlayerExact(givePlayerName)

        if (givePlayer == null || !givePlayer.isOnline) {
            return true
        }

        val characterString = args[1].lowercase()

        for (c in characterString.toCharArray()) {
            var dropStack: ItemStack? = null
            for (test in NonAlphaNumBlocks.entries) {
                if (test.charVal == c) {
                    dropStack = test.itemStack
                }
            }
            if (dropStack == null) {
                val isThere =
                    Arrays.stream(LetterBlock.entries.toTypedArray()).filter({ it -> it.character == c }).findFirst()
                if (!isThere.isEmpty) {
                    dropStack = isThere.get().itemStacks
                } else {
                    val isThereNum =
                        Arrays.stream(NumericBlock.entries.toTypedArray()).filter({ it -> it.c == c }).findFirst()
                    if (!isThereNum.isEmpty) dropStack = isThereNum.get().itemStack
                }
            }
            if (givePlayer.location.world != null) {
                givePlayer.location.world.dropItemNaturally(givePlayer.location, dropStack!!)
            }
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
            mainArg = args[0].lowercase(Locale.getDefault())
            val onlinePlayers: MutableList<kotlin.String?> = ArrayList<kotlin.String?>()
            for (p in Bukkit.getOnlinePlayers()) {
                onlinePlayers.add(p.name)
            }
            StringUtil.copyPartialMatches<MutableList<kotlin.String?>?>(mainArg, onlinePlayers, completions)
        }

        if (args.size == 2) {
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
