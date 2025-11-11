package org.stephanosbad.charmedChars.Block

import org.bukkit.inventory.ItemStack
import org.stephanosbad.charmedChars.Items.LetterBlock
import org.stephanosbad.charmedChars.Items.NonAlphaNumBlocks
import org.stephanosbad.charmedChars.Items.NumericBlock

data class CustomBlock(
    val id: LetterBlock?,
    val itemStack: ItemStack? = null,
    val nonId: NonAlphaNumBlocks? = null,
    val numberId : NumericBlock? = null){
    constructor( nonId: NonAlphaNumBlocks?, itemStack: ItemStack?) : this(null, itemStack, nonId)
    constructor( numberId: NumericBlock?, itemStack: ItemStack?) : this(null, itemStack, null, numberId)
}
