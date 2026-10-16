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
package org.stephanosbad.charmedChars.integration

import org.bukkit.Material
import org.bukkit.plugin.Plugin
import org.stephanosbad.charmedChars.items.BlockColor
import org.stephanosbad.charmedChars.items.LetterBlock
import org.stephanosbad.charmedChars.items.NonAlphaNumBlocks
import org.stephanosbad.charmedChars.items.NumericBlock
import org.stephanosbad.integrare.ProviderKind
import org.stephanosbad.integrare.catalog.CatalogItem
import org.stephanosbad.integrare.catalog.CatalogRecipe
import org.stephanosbad.integrare.catalog.Ingredient
import org.stephanosbad.integrare.catalog.ItemCatalog
import org.stephanosbad.integrare.catalog.TextureSource
import org.stephanosbad.integrare.catalog.ToolTier

/**
 * Every CharmedChars custom item and recipe, for Integrare to install into the
 * active provider.
 *
 * Display names differ per provider because each provider's existing items carry
 * the names they were created with; changing a native name would stop new items
 * stacking with old ones.
 */
object CharmedCharsCatalog {

    const val NAMESPACE = "charmedchars"

    // Must never change: existing native items in player inventories are tagged with it
    const val NATIVE_NBT_KEY = "charmedchars_item_id"

    private val operatorSymbols = mapOf(
        NonAlphaNumBlocks.PLUS to "+",
        NonAlphaNumBlocks.MINUS to "-",
        NonAlphaNumBlocks.MULTIPLY to "×",
        NonAlphaNumBlocks.DIVISION to "÷"
    )

    private val BlockColor.miniMessageTag
        get() = when (this) {
            BlockColor.CYAN -> "<aqua>"
            BlockColor.MAGENTA -> "<light_purple>"
            BlockColor.YELLOW -> "<yellow>"
        }

    private val BlockColor.nexoGradient
        get() = when (this) {
            BlockColor.CYAN -> "#00FFFF:#00CED1"
            BlockColor.MAGENTA -> "#FF00FF:#FF1493"
            BlockColor.YELLOW -> "#FFFF00:#FFD700"
        }

    private val BlockColor.title get() = directoryName.replaceFirstChar { it.uppercase() }

    /**
     * @param glyph Character shown in Oraxen/ItemsAdder names, e.g. "A", "5", "×", "Logo"
     * @param nexoGlyph Character shown in Nexo names
     * @param suffix Id and texture suffix, e.g. "a", "5", "plus", "logo"
     */
    private fun block(color: BlockColor, suffix: String, texture: String, glyph: String, nexoGlyph: String) =
        CatalogItem.Block(
            id = "${color.directoryName}_$suffix",
            displayName = "${color.miniMessageTag}$glyph Block",
            texture = "block/${color.directoryName}/$texture",
            permission = "$NAMESPACE.block",
            overrides = mapOf(
                ProviderKind.ITEMSADDER to CatalogItem.Overrides(displayName = "${color.title} $glyph Block"),
                ProviderKind.NEXO to CatalogItem.Overrides(
                    displayName = "<gradient:${color.nexoGradient}>$nexoGlyph</gradient>"
                ),
                ProviderKind.NATIVE to CatalogItem.Overrides(displayName = "${color.miniMessageTag}${color.title} $suffix")
            )
        )

    private fun letter(color: BlockColor, letter: LetterBlock) =
        block(color, letter.character.lowercase(), letter.character.lowercase(),
            letter.character.toString(), letter.character.toString())

    private fun number(color: BlockColor, number: NumericBlock) =
        block(color, number.c.toString(), number.c.toString(), number.c.toString(), number.c.toString())

    private fun operator(color: BlockColor, op: NonAlphaNumBlocks) =
        block(color, op.nonAlphaNumBlockName, op.nonAlphaNumBlockName, operatorSymbols.getValue(op), op.charVal.toString())

    private fun logo(color: BlockColor) = block(color, "logo", "logo_block", "Logo", "Logo").let {
        // Oraxen's logo name has no glyph before " Logo Block"
        it.copy(displayName = "${color.miniMessageTag} Logo Block")
    }

    private val pyriteLore = listOf(
        "<gray>A fool's gold %s",
        "<gray>Works like gold for scoring words",
        "<gray>Iron-tier durability and stats"
    )

    private fun pyriteName(name: String, base: String = "<gold>$name") = mapOf(
        ProviderKind.ITEMSADDER to CatalogItem.Overrides(displayName = name),
        ProviderKind.NEXO to CatalogItem.Overrides(displayName = "<gradient:#FFD700:#B8860B>$name</gradient>")
    ).let { base to it }

    /** Pyrite tools: gold base items with vanilla iron durability, mining tier and attack stats. */
    private fun pyriteTool(tool: String, base: Material, damage: Double, speed: Double): CatalogItem.Item {
        val name = "Pyrite ${tool.replaceFirstChar { it.uppercase() }}"
        val (displayName, overrides) = pyriteName(name)
        return CatalogItem.Item(
            id = "pyrite_$tool",
            displayName = displayName,
            texture = "item/pyrite/$tool",
            baseMaterial = base,
            handheld = true,
            durability = 250,
            miningTier = ToolTier.IRON,
            attackDamage = damage,
            attackSpeed = speed,
            lore = pyriteLore.map { it.format(tool) },
            permission = "$NAMESPACE.pyrite",
            overrides = overrides
        )
    }

    private val pyriteIngot = pyriteName("Pyrite Ingot").let { (displayName, overrides) ->
        CatalogItem.Item(
            id = "pyrite_ingot",
            displayName = displayName,
            texture = "item/pyrite/ingot",
            baseMaterial = Material.GOLD_INGOT,
            permission = "$NAMESPACE.pyrite",
            // Ingots already on Oraxen/Nexo servers use these bases
            overrides = overrides + mapOf(
                ProviderKind.ORAXEN to CatalogItem.Overrides(baseMaterial = Material.PAPER),
                ProviderKind.NEXO to overrides.getValue(ProviderKind.NEXO).copy(baseMaterial = Material.BRICK)
            )
        )
    }

    private val pyriteTools = listOf(
        pyriteTool("pickaxe", Material.GOLDEN_PICKAXE, 4.0, -2.8),
        pyriteTool("axe", Material.GOLDEN_AXE, 9.0, -3.1),
        pyriteTool("shovel", Material.GOLDEN_SHOVEL, 4.5, -3.0),
        pyriteTool("hoe", Material.GOLDEN_HOE, 1.0, -1.0)
    )

    private val recipes: List<CatalogRecipe> = listOf(
        CatalogRecipe.Shapeless("pyrite_ingot", "pyrite_ingot",
            listOf(Ingredient.Vanilla("IRON_INGOT"), Ingredient.Vanilla("REDSTONE"))),
        CatalogRecipe.Shapeless("pyrite_ingot_sulfur", "pyrite_ingot",
            listOf(Ingredient.Vanilla("IRON_INGOT"), Ingredient.Vanilla("POTENT_SULFUR"))),
        pyriteToolRecipe("pickaxe", "PPP", " S ", " S "),
        pyriteToolRecipe("axe", "PP ", "PS ", " S "),
        pyriteToolRecipe("shovel", " P ", " S ", " S "),
        pyriteToolRecipe("hoe", "PP ", " S ", " S ")
    )

    private fun pyriteToolRecipe(tool: String, vararg shape: String) = CatalogRecipe.Shaped(
        "pyrite_$tool", "pyrite_$tool", shape.toList(),
        mapOf('P' to Ingredient.Custom("pyrite_ingot"), 'S' to Ingredient.Vanilla("STICK"))
    )

    fun create(plugin: Plugin): ItemCatalog {
        val colors = BlockColor.entries
        val alphabetical = LetterBlock.entries.sortedBy { it.character }

        // Listing order of the provider configs; also Oraxen's custom_variation order
        val items = colors.flatMap { c -> alphabetical.map { letter(c, it) } } +
            colors.flatMap { c -> NumericBlock.entries.map { number(c, it) } } +
            colors.flatMap { c -> NonAlphaNumBlocks.entries.map { operator(c, it) } } +
            colors.map { logo(it) } +
            pyriteIngot + pyriteTools

        // Native custom model data and note block states. Append only: the first
        // 125 slots are what existing native servers have placed and stored.
        val nativeOrder = colors.flatMap { c ->
            LetterBlock.entries.map { letter(c, it).id } +
                NumericBlock.entries.map { number(c, it).id } +
                NonAlphaNumBlocks.entries.map { operator(c, it).id }
        } + pyriteIngot.id + pyriteTools.map { it.id } + colors.map { logo(it).id }

        return ItemCatalog(
            namespace = NAMESPACE,
            items = items,
            recipes = recipes,
            textures = TextureSource.fromPluginJar(
                plugin,
                fullRoot = "pack/assets/minecraft/textures",
                reducedRoot = "pack-oraxen/assets/minecraft/textures"
            ),
            slotOrder = mapOf(ProviderKind.NATIVE to nativeOrder)
        )
    }
}
