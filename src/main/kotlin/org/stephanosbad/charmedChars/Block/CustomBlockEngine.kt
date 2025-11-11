package org.stephanosbad.charmedChars.Block

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import org.stephanosbad.charmedChars.CharmedChars
import org.stephanosbad.charmedChars.Items.LetterBlock
import org.stephanosbad.charmedChars.Items.NonAlphaNumBlocks
import org.stephanosbad.charmedChars.Items.NumericBlock


class CustomBlockEngine(private val plugin: CharmedChars, var initialBlockCode: Int) {

    init{
        globalPlugin = plugin
    }
    val letterBlockKeys = buildMap<Pair<BlockColor, BlockLetter>, Pair<NamespacedKey, Int>> {

        BlockColor.entries.forEach {
            var color = it
            BlockLetter.entries.forEach {
                put(Pair(color, it), Pair(NamespacedKey(plugin, "${color.directoryName}_${it.filenameBase}"), initialBlockCode++))
            }
        }
    }

    val numberBlockKeys = buildMap<Pair<BlockColor, BlockNumber>, Pair<NamespacedKey, Int>> {

        BlockColor.entries.forEach {
            var color = it
            BlockNumber.entries.forEach {
                put(Pair(color, it), Pair(NamespacedKey(plugin, "${color.directoryName}_${it.filenameBase}"), initialBlockCode++))
            }
        }
    }

    val characterBlockKeys = buildMap<Pair<BlockColor, BlockCharacter>, Pair<NamespacedKey, Int>> {

        BlockColor.entries.forEach {
            var color = it
            BlockCharacter.entries.forEach {
                put(Pair(color, it), Pair(NamespacedKey(plugin, "${color.directoryName}_${it.filenameBase}"), initialBlockCode++))
            }
        }
    }

    companion object {
        internal var globalPlugin : CharmedChars? = null
        fun byAlreadyPlaced(block: Block?): CustomBlock? {

            var meta = block?.drops?.firstOrNull()?.itemMeta
            var localBlockEngine = globalPlugin?.customBlockEngine

            if(meta != null && localBlockEngine != null)
            {
                var letterBlockKey = LetterBlock.entries.firstOrNull { it.customVariation == meta.customModelData }
                return letterBlockKey?.let { CustomBlock(it) }
            }
            return null
        }

        fun getInstance(color: BlockColor, letterBlock: LetterBlock): CustomBlock? {


            var blockLetter = BlockLetter.entries.firstOrNull { it.filenameBase == letterBlock.character.toString() }
            var letterBlockKey = globalPlugin?.customBlockEngine?.letterBlockKeys[Pair(color, blockLetter )]
            letterBlockKey?.let{
                val item = ItemStack(Material.NOTE_BLOCK)
                val meta = item.itemMeta

                meta.displayName(
                    Component.text("${color.name} ${letterBlock.character} Block")
                        .color(when(color){
                            BlockColor.CYAN -> NamedTextColor.AQUA
                            BlockColor.YELLOW -> NamedTextColor.YELLOW
                            BlockColor.MAGENTA -> NamedTextColor.LIGHT_PURPLE
                        })
                )
                meta.setCustomModelData(letterBlockKey.second)
                item.itemMeta = meta

                return CustomBlock(letterBlock, item)
            }


            return null

        }

        fun getInstance(color: BlockColor, nonAlphaNumeric: NonAlphaNumBlocks): CustomBlock?{
            var blockLetter = BlockCharacter.entries.firstOrNull { it.filenameBase == nonAlphaNumeric.nonAlphaNumBlockName }
            var letterBlockKey = globalPlugin?.customBlockEngine?.characterBlockKeys[Pair(color, blockLetter )]
            letterBlockKey?.let {
                val item = ItemStack(Material.NOTE_BLOCK)
                val meta = item.itemMeta

                meta.displayName(
                    Component.text("${color.name} ${nonAlphaNumeric.nonAlphaNumBlockName} Block")
                        .color(
                            when (color) {
                                BlockColor.CYAN -> NamedTextColor.AQUA
                                BlockColor.YELLOW -> NamedTextColor.YELLOW
                                BlockColor.MAGENTA -> NamedTextColor.LIGHT_PURPLE
                            }
                        )
                )
                meta.setCustomModelData(letterBlockKey.second)
                item.itemMeta = meta

                return CustomBlock(nonAlphaNumeric, item)
            }
            return null
        }

        fun getInstance(color: BlockColor, numericBlock: NumericBlock): CustomBlock?{
            var blockNumber = BlockNumber.entries.firstOrNull { it.filenameBase == numericBlock.name }
            var numberBlockKey = globalPlugin?.customBlockEngine?.numberBlockKeys[Pair(color, blockNumber )]
            numberBlockKey?.let {
                val item = ItemStack(Material.NOTE_BLOCK)
                val meta = item.itemMeta

                meta.displayName(
                    Component.text("${color.name} ${numericBlock.c} Block")
                        .color(
                            when (color) {
                                BlockColor.CYAN -> NamedTextColor.AQUA
                                BlockColor.YELLOW -> NamedTextColor.YELLOW
                                BlockColor.MAGENTA -> NamedTextColor.LIGHT_PURPLE
                            }
                        )
                )
                meta.setCustomModelData(numberBlockKey.second)
                item.itemMeta = meta

                return CustomBlock(numericBlock, item)
            }
            return null
        }


    }

}

