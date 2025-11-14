package org.stephanosbad.charmedChars.items


import org.bukkit.inventory.ItemStack
import org.stephanosbad.charmedChars.block.CustomBlockEngine

enum class NonAlphaNumBlocks(val charVal: Char, blockName: String) {
    PLUS('+', "plus_block"),
    MINUS('-', "minus_block"),
    MULTIPLY('*', "multiply_block"),
    DIVISION('/', "divide_block");

    private val _itemStacks: MutableMap<BlockColor, ItemStack?> by lazy {
        mutableMapOf<BlockColor, ItemStack?>().apply {
            for (color in BlockColor.entries) {
                this[color] = CustomBlockEngine.getInstance(color, this@NonAlphaNumBlocks)?.itemStack
            }
        }
    }

    val itemStacks: MutableMap<BlockColor, ItemStack?>
        get() = _itemStacks

    val nonAlphaNumBlockName = blockName
}