package org.stephanosbad.charmedChars.Items


import org.bukkit.inventory.ItemStack
import org.stephanosbad.charmedChars.Block.BlockColor
import org.stephanosbad.charmedChars.Block.BlockLetter
import org.stephanosbad.charmedChars.Block.CustomBlockEngine

enum class NonAlphaNumBlocks(val charVal: Char, blockName: String) {
    PLUS('+', "plus_block"),
    MINUS('-', "minus_block"),
    MULTIPLY('*', "multiply_block"),
    DIVISION('/', "divide_block");

    val itemStacks: MutableMap<BlockColor, ItemStack?> = mutableMapOf()

    val nonAlphaNumBlockName = blockName
    val nonBlockId  = BlockLetter.entries.firstOrNull{ it.filenameBase == nonAlphaNumBlockName}
    init {
        nonBlockId?.let {
            for (color in BlockColor.entries) {
                this.itemStacks[color] = CustomBlockEngine.getInstance(color, this)!!.itemStack!!
            }
        }
    }
}