package org.stephanosbad.charmedChars.Items

import org.bukkit.inventory.ItemStack
import org.stephanosbad.charmedChars.Block.BlockColor
import org.stephanosbad.charmedChars.Block.BlockLetter
import org.stephanosbad.charmedChars.Block.CustomBlockEngine

enum class NumericBlock(val c: Char) {
    BLOCK_0('0'),
    BLOCK_1('1'),
    BLOCK_2('2'),
    BLOCK_3('3'),
    BLOCK_4('4'),
    BLOCK_5('5'),
    BLOCK_6('6'),
    BLOCK_7('7'),
    BLOCK_8('8'),
    BLOCK_9('9'),
    ;

    val itemStacks: MutableMap<BlockColor, ItemStack?> = mutableMapOf()

    val nonBlockId  = BlockLetter.entries.firstOrNull{ it.filenameBase == c.toString()}

    init {
        nonBlockId?.let {
            for (color in BlockColor.entries) {
                this.itemStacks[color] = CustomBlockEngine.getInstance(color, this)!!.itemStack!!
            }
        }
    }
}