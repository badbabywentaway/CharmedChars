package org.stephanosbad.charmedChars.Items


import org.bukkit.inventory.ItemStack

enum class NonAlphaNumBlocks(val charVal: Char, blockName: String) {
    PLUS('+', "plus_block"),
    MINUS('-', "minus_block"),
    MULTIPLY('*', "multiply_block"),
    DIVISION('/', "divide_block");

    val oraxenBlockName = blockName
    var itemStack: ItemStack = CustomBlock.getInstance(oraxenBlockName)!!.itemStack!!
}