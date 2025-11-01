package org.stephanosbad.charmedChars.Items

import org.bukkit.inventory.ItemStack

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

    val itemStack: ItemStack = CustomBlock.getInstance(c.toString() + "_block")!!.itemStack!!
}