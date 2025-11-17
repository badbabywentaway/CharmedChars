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
package org.stephanosbad.charmedChars.items

import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import dev.lone.itemsadder.api.CustomBlock
import dev.lone.itemsadder.api.CustomStack
import me.ryanhamshire.GriefPrevention.GriefPrevention
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.stephanosbad.charmedChars.CharmedChars
import org.stephanosbad.charmedChars.rewards.DropReward
import org.stephanosbad.charmedChars.rewards.Reward
import org.stephanosbad.charmedChars.rewards.RewardType
import org.stephanosbad.charmedChars.utility.LocationPair
import org.stephanosbad.charmedChars.utility.SimpleTuple
import org.stephanosbad.charmedChars.utility.WordDict
import java.util.*
import java.util.concurrent.atomic.AtomicReference

class ItemManager @JvmOverloads constructor(localPlugin: CharmedChars? = null) :
    Listener {
    /**
     * Wood material list.
     */
    private val list: HashMap<Material?, Material?> = object : HashMap<Material?, Material?>() {
        init {
            put(Material.ACACIA_LOG, Material.STRIPPED_ACACIA_LOG)
            put(Material.SPRUCE_LOG, Material.STRIPPED_SPRUCE_LOG)
            put(Material.OAK_LOG, Material.STRIPPED_OAK_LOG)
            put(Material.DARK_OAK_LOG, Material.STRIPPED_DARK_OAK_LOG)
            put(Material.JUNGLE_LOG, Material.STRIPPED_JUNGLE_LOG)
            put(Material.BIRCH_LOG, Material.STRIPPED_BIRCH_LOG)
            put(Material.MANGROVE_LOG, Material.STRIPPED_MANGROVE_LOG)
            put(Material.CHERRY_LOG, Material.STRIPPED_CHERRY_LOG)
            put(Material.PALE_OAK_LOG, Material.STRIPPED_PALE_OAK_LOG)
            put(Material.WARPED_STEM, Material.STRIPPED_WARPED_STEM)
            put(Material.CRIMSON_STEM, Material.STRIPPED_CRIMSON_STEM)
            put(Material.BAMBOO_BLOCK, Material.STRIPPED_BAMBOO_BLOCK)
        }
    }


    private val listForNumberDrops: HashMap<Material?, Material?> = object : HashMap<Material?, Material?>() {
        init {
            put(Material.WARPED_STEM, Material.STRIPPED_WARPED_STEM)
            put(Material.CRIMSON_STEM, Material.STRIPPED_CRIMSON_STEM)
        }
    }

    private var characterBlocksAvailableInNether: MutableList<ItemStack?>? = null

    /**
     * Exclusion zone for use of this plugin.
     */
    private var exclude: LocationPair? = null

    /**
     * Inclusion zone for use of this plugin. If defined, acts as an exclusive include.
     */
    private var include: LocationPair? = null

    /**
     * World Guard anti griefing tool. Accessor.
     */
    var worldGuard: WorldGuard? = null

    /**
     * World Guard anti griefing tool. Plugin accessor.
     */
    var worldGuardPlugin: WorldGuardPlugin? = null

    /**
     * Grief Prevention anti griefing tool. Accessor
     */
    var griefPrevention: GriefPrevention? = null

    /**
     * Reward implementations
     */
    var rewards: MutableList<Reward> = ArrayList<Reward>()

    var plugin = localPlugin!!

    /**
     * Constructor
     * @param localPlugin - Master plugin
     */
    init {
        // WorldGuard integration (optional soft dependency)
        try {
            worldGuardPlugin = WorldGuardPlugin.inst()
            worldGuard = WorldGuard.getInstance()
            if (worldGuardPlugin != null && worldGuard != null) {
                val status = if (plugin.configManager.worldGuardIntegration) "enabled" else "disabled in config"
                Bukkit.getLogger().info("WorldGuard found - integration $status")
            } else {
                throw NullPointerException("Class variable did not instantiate")
            }
        } catch (e: Exception) {
            Bukkit.getLogger().info("WorldGuard not available (optional)")
        } catch (e: Error) {
            Bukkit.getLogger().info("WorldGuard not available (optional)")
        }

        // GriefPrevention integration (optional soft dependency)
        try {
            griefPrevention = GriefPrevention.instance
            if (griefPrevention != null) {
                val status = if (plugin.configManager.griefPreventionIntegration) "enabled" else "disabled in config"
                Bukkit.getLogger().info("GriefPrevention found - integration $status")
            } else {
                throw NullPointerException("Class variable did not instantiate")
            }
        } catch (e: Exception) {
            Bukkit.getLogger().info("GriefPrevention not available (optional)")
        } catch (e: Error) {
            Bukkit.getLogger().info("GriefPrevention not available (optional)")
        }

        try {
            setRewards()
        } catch (e: Exception) {
            Bukkit.getLogger().info("Rewards not available.")
        } catch (e: Error) {
            Bukkit.getLogger().info("Rewards not available.")
        }
    }

    /**
     * combined action for wood block or letter block rewards
     *
     * @param e - block break event
     */
    @EventHandler
    fun onBreakWoodOrLetter(e: BlockBreakEvent) {
        val player = e.player
        val hand = player.inventory.itemInMainHand
        val material = e.getBlock().blockData.material

        if (!(hand.containsEnchantment(Enchantment.SILK_TOUCH))) {
            //If there is no silk touch on it

            if (list.containsKey(material)) {
                //Must be gold item in hand
                if (hand.itemMeta == null) {
                    return
                }
                if (!hand.type.name.lowercase().contains("gold") &&
                    true != hand.itemMeta.displayName()?.examinableName()?.lowercase()?.contains("gold")
                ) {
                    return
                }

                var chance = plugin.configManager.letterBlockDropChance
                if (hand.containsEnchantment(Enchantment.LOOTING)) {
                    val baseChance = plugin.configManager.letterBlockDropChance
                    when (hand.enchantments[Enchantment.LOOTING]) {
                        1 -> chance = baseChance * plugin.configManager.lootingMultiplier1
                        2 -> chance = baseChance * plugin.configManager.lootingMultiplier2
                        3 -> chance = baseChance * plugin.configManager.lootingMultiplier3
                        else -> {}
                    }
                }

                if (Math.random() < chance) {
                    //check wood
                    woodBlockBreak(e, list.get(material)!!, material)
                }
            } else {
                //check letter
                letterBlockBreak(e)
            }
        }
    }

    /**
     * Check if it was a wood block that was broken.
     * @param e - break event.
     * @param material - Material to replace block
     * @param oldMaterial - Old material of block
     */
    private fun woodBlockBreak(e: BlockBreakEvent, material: Material, oldMaterial: Material?) {
        val block = LetterBlock.randomPickBlock()
        val player = e.player

        if (block == null) {
            plugin.logger.warning("Failed to generate letter block - randomPickBlock() returned null")
            return
        }

        if (protectedSpot(player, e.getBlock().location, e.getBlock())) {
            player.sendMessage("Protected.")
            return
        }

        e.isCancelled = true
        e.getBlock().type = Material.AIR

        if (listForNumberDrops.containsKey(oldMaterial)) {
            val numChar = randomNumAndCharacter()
            player.world.dropItemNaturally(e.getBlock().location, numChar!!)
        }

        player.world.dropItemNaturally(e.getBlock().location, block)
        player.world.dropItemNaturally(e.getBlock().location, ItemStack(material, 1))
    }

    private fun randomNumAndCharacter(): ItemStack? {
        if (characterBlocksAvailableInNether == null) {
            characterBlocksAvailableInNether = ArrayList<ItemStack?>()
            for (c in BlockColor.entries) {
                for (x in NumericBlock.entries) {
                    characterBlocksAvailableInNether!!.add(x.itemStacks[c])
                }
                for (x in NonAlphaNumBlocks.entries) {
                    characterBlocksAvailableInNether!!.add(x.itemStacks[c])
                }
            }
        }

        // Clone ItemStack to avoid reference sharing bug
        val selectedItem = characterBlocksAvailableInNether!![(Math.random() * characterBlocksAvailableInNether!!.size).toInt()]
        return selectedItem?.clone()
    }

    /**
     * Check if it was a letter block that was broken.
     * @param e - break event.
     */
    fun letterBlockBreak(e: BlockBreakEvent) {
        val hand = e.player.inventory.itemInMainHand

        if (protectedSpot(e.player, e.getBlock().location, e.getBlock())) {
            e.player.sendMessage("Protected block: " + e.getBlock().location)
            return
        }
        if (hand.itemMeta == null) {
            return
        }
        if (!hand.type.name.lowercase().contains("gold")) {
            return
        }

        val brokenBlock = e.getBlock()
        var c: SimpleTuple<Char, Double> = testForLetter(e.player, brokenBlock)
        if (c.first == '\u0000') {
            return
        }

        // Determine the axis by checking adjacent blocks
        val world = brokenBlock.world
        val x = brokenBlock.x
        val y = brokenBlock.y
        val z = brokenBlock.z

        val hasXAdjacent = testForLetter(e.player, world.getBlockAt(x + 1, y, z)).first != '\u0000' ||
                          testForLetter(e.player, world.getBlockAt(x - 1, y, z)).first != '\u0000'
        val hasZAdjacent = testForLetter(e.player, world.getBlockAt(x, y, z + 1)).first != '\u0000' ||
                          testForLetter(e.player, world.getBlockAt(x, y, z - 1)).first != '\u0000'

        // Check all 4 directions to find which one has letter blocks
        val hasXPlus = testForLetter(e.player, world.getBlockAt(x + 1, y, z)).first != '\u0000'
        val hasXMinus = testForLetter(e.player, world.getBlockAt(x - 1, y, z)).first != '\u0000'
        val hasZPlus = testForLetter(e.player, world.getBlockAt(x, y, z + 1)).first != '\u0000'
        val hasZMinus = testForLetter(e.player, world.getBlockAt(x, y, z - 1)).first != '\u0000'

        // Determine scan direction - pick one of the 4 cardinal directions that has letters
        val lateralDirection: LateralDirection
        if (hasXPlus && !hasXMinus && !hasZPlus && !hasZMinus) {
            // Only +X has letters
            lateralDirection = LateralDirection(1, 0)
        } else if (hasXMinus && !hasXPlus && !hasZPlus && !hasZMinus) {
            // Only -X has letters
            lateralDirection = LateralDirection(-1, 0)
        } else if (hasZPlus && !hasXPlus && !hasXMinus && !hasZMinus) {
            // Only +Z has letters
            lateralDirection = LateralDirection(0, 1)
        } else if (hasZMinus && !hasXPlus && !hasXMinus && !hasZPlus) {
            // Only -Z has letters
            lateralDirection = LateralDirection(0, -1)
        } else if (!hasXAdjacent && !hasZAdjacent) {
            // Single letter word
            lateralDirection = LateralDirection(1, 0)
        } else {
            // Multiple directions have letters (invalid pattern)
            e.player.sendMessage("Miss")
            return
        }

        // Build word starting from the broken block (treat it as first letter)
        var testBlock = brokenBlock
        var score = 0.0
        val outString = StringBuilder()
        val blockArray: MutableList<Location> = ArrayList<Location>(mutableListOf<Location?>())
        var isSameColor = true
        var colorTest: BlockColor? = null

        c = testForLetter(e.player, testBlock)
        while (c.first != '\u0000') {
            val letterScore = c.second + 10
            score += letterScore
            blockArray.add(testBlock.location)
            outString.append(c.first)

            if(isSameColor)
            {
                var getColor = getBlockColor(testBlock)
                if(colorTest == null)
                {
                     colorTest = getColor
                }
                else if (colorTest != getColor)
                {
                    isSameColor = false
                }
            }

            testBlock = offsetBlock(testBlock, lateralDirection)
            c = testForLetter(e.player, testBlock)
        }

        if(isSameColor && colorTest != null)
        {
            score *= 3
            e.player.sendMessage("Triple Score! All Blocks Are ${colorTest.name}!")
        }

        val wordLowercase = outString.toString().lowercase()
        val isInDictionary = WordDict.singleton!!.words.contains(wordLowercase)

        if (isInDictionary) {
            e.player.sendMessage("Hit: $score")

            // Remove all blocks in the word using ItemsAdder API
            for (locationOfBlock in blockArray) {
                val block = locationOfBlock.world.getBlockAt(locationOfBlock)
                // Use ItemsAdder API to properly remove custom blocks
                val customBlock = CustomBlock.byAlreadyPlaced(block)
                if (customBlock != null) {
                    customBlock.remove()
                } else {
                    // Fallback to vanilla removal if not a custom block
                    block.type = Material.AIR
                }
            }

            // Cancel the event to prevent the broken block from dropping as an item
            e.isCancelled = true

            applyScore(e.player, score)
        } else {
            e.player.sendMessage("Miss")
            // Cancel the event so the block doesn't break on a miss
            e.isCancelled = true
        }
    }

    /**
     * Apply the score to the player. Drops or cash.
     * @param player - Player to apply score
     * @param score - score
     */
    private fun applyScore(player: Player, score: Double) {
        for (reward in rewards) {
            if (reward is DropReward) {
                reward.applyReward(player, player.location, score)
            }
        }
    }

    /**
     * Find block adjacent to another
     * @param testBlock - block from which to find the adjacent
     * @param lateralDirection - Direction in which to test
     * @return adjacent block
     */
    private fun offsetBlock(testBlock: Block, lateralDirection: LateralDirection): Block {
        val x =
            testBlock.x + lateralDirection.xOffset
        val y = testBlock.y
        val z =
            testBlock.z + lateralDirection.zOffset
        return testBlock.world.getBlockAt(x, y, z)
    }

    /**
     * Check the block for the next lateral block.
     * @param player - player (used in grief protecting)
     * @param testBlock - block under test
     * @return Direction of block.
     */
    private fun checkLateralBlocks(player: Player?, testBlock: Block): LateralDirection {
        val retValue = LateralDirection(0, 0)
        val world = testBlock.world
        val x = testBlock.x
        val y = testBlock.y
        val z = testBlock.z

        val xUp = testForLetter(player, world.getBlockAt(x + 1, y, z)).first != '\u0000'
        val xDown = testForLetter(player, world.getBlockAt(x - 1, y, z)).first != '\u0000'
        val zUp = testForLetter(player, world.getBlockAt(x, y, z + 1)).first != '\u0000'
        val zDown = testForLetter(player, world.getBlockAt(x, y, z - 1)).first != '\u0000'

        if (xUp && !xDown && !zUp && !zDown) {
            retValue.xOffset = 1
        } else if (!xUp && xDown && !zUp && !zDown) {
            retValue.xOffset = -1
        } else if (!xUp && !xDown && zUp && !zDown) {
            retValue.zOffset = 1
        } else if (!xUp && !xDown && !zUp && zDown) {
            retValue.zOffset = -1
        }

        return retValue
    }

    /**
     * Test to see if this block is a letter block
     * @param player - player who hit it. Used to null the result if letter block is grief protected.
     * @param testBlock - block to test.
     * @return - character of block and rarity score
     */
    fun testForLetter(player: Player?, testBlock: Block): SimpleTuple<Char, Double> {
        if (protectedSpot(player, testBlock.location, testBlock)) {
            return SimpleTuple('\u0000', 0.0)
        }
        if (testBlock.state.blockData !is NoteBlock) {
            return SimpleTuple('\u0000', 0.0)
        }
        val match: AtomicReference<SimpleTuple<Char, Double>> = AtomicReference(SimpleTuple('\u0000', 0.0))
        val variation = getCustomVariation(testBlock)
        if (Arrays.stream(LetterBlock.entries.toTypedArray()).anyMatch { v ->
                val found = variation == v
                if (found) {
                    match.set(SimpleTuple(v.character, v.frequencyFactor))
                }
                found
            }) {
            return match.get()
        }
        return SimpleTuple('\u0000', 0.0)
    }

    fun getNoteblockNumber(testBlock: Block) : Int?
    {
        return testBlock.drops.firstOrNull()?.itemMeta?.customModelData

    }

    /**
     * Get custom block variation using ItemsAdder API
     * @param block - block to check
     * @return - LetterBlock if it's a letter block, null otherwise
     */
    fun getCustomVariation(block: Block?): LetterBlock? {
        if (block == null) return null

        val customBlock = CustomBlock.byAlreadyPlaced(block) ?: return null
        val namespacedId = customBlock.namespacedID

        // Parse the ItemsAdder ID (e.g., "charmedchars:cyan_a")
        if (!namespacedId.startsWith("charmedchars:")) return null

        val parts = namespacedId.substring("charmedchars:".length).split("_")
        if (parts.size != 2) return null

        val letter = parts[1].uppercase()

        // Find the matching LetterBlock
        return LetterBlock.entries.firstOrNull { it.name == letter }
    }

    /**
     * Get the color of a custom block using ItemsAdder API
     * @param block - block to check
     * @return - BlockColor if it's a custom block, null otherwise
     */
    fun getBlockColor(block: Block?): BlockColor? {
        if (block == null) return null

        val customBlock = CustomBlock.byAlreadyPlaced(block) ?: return null
        val namespacedId = customBlock.namespacedID

        // Parse the ItemsAdder ID (e.g., "charmedchars:cyan_a")
        if (!namespacedId.startsWith("charmedchars:")) return null

        val parts = namespacedId.substring("charmedchars:".length).split("_")
        if (parts.isEmpty()) return null

        val colorName = parts[0].uppercase()

        // Find the matching BlockColor
        return BlockColor.entries.firstOrNull { it.name == colorName }
    }

    /**
     * Determine if this location is protected from this player
     * @param player - MC Player
     * @param location - location to examine
     * @param block - block to examine (some grief plugins require this)
     * @return - verification that location is being protected
     */
    fun protectedSpot(player: Player?, location: Location, block: Block?): Boolean {
        // Check GriefPrevention if enabled in config
        if (plugin.configManager.griefPreventionIntegration) {
            var griefPrevention = this.griefPrevention
            if (griefPrevention != null && griefPrevention.allowBreak(player, block, location) != null) {
                return true
            }
        }

        // Check WorldGuard if enabled in config
        if (plugin.configManager.worldGuardIntegration) {
            var worldGuardPlugin = this.worldGuardPlugin
            if (worldGuardPlugin != null &&
                !worldGuardPlugin.createProtectionQuery().testBlockBreak(player, block)
            ) {
                return true
            }
        }

        return ourConfigProtects(location)
    }

    /**
     * Determine if our config protects this location
     * @param location - location to examine
     * @return - verification that location is being protected
     */
    private fun ourConfigProtects(location: Location): Boolean {
        var exclude = this.exclude
        var include = this.include
        var configuration = plugin.configDataHandler!!.configuration!!

        if (exclude == null) {
            val excludeFrom = configuration.getLocation("exclude.from", null)
            val excludeTo = configuration.getLocation("exclude.to", null)
            if (excludeFrom != null && excludeTo != null) {
                exclude = LocationPair(excludeFrom, excludeTo)
                this.exclude = exclude
            }
        }

        if (include == null) {
            val includeFrom = configuration.getLocation("include.from", null)
            val includeTo = configuration.getLocation("include.to", null)
            if (includeFrom != null && includeTo != null) {
                include = LocationPair(includeFrom, includeTo)
                this.include = include
            }
        }

        if (exclude != null && exclude.isValid && exclude.check(location)) {
            return true
        }

        return include != null && include.isValid && !include.check(location)
    }

    /**
     * Setup rewards from config file
     */
    private fun setRewards() {
        for (t in RewardType.entries) {
            val configuration =
                plugin.configDataHandler!!.configuration!!
            when (t) {
                RewardType.Drop -> {
                    val listOfDropConfigs =
                        checkNotNull(configuration.getList("Drop"))

                    for (drop in listOfDropConfigs) {
                        try {
                            if (drop !is MutableMap<*, *>) {
                                continue
                            }
                            val dropParams = drop as MutableMap<*, *>?
                            val materialName = dropParams!!["materialName"] as String?
                            val minimumRewardCount = dropParams["minimumRewardCount"] as Double
                            val multiplier = dropParams["multiplier"] as Double
                            val minimumThreshold = dropParams["minimumThreshold"] as Double
                            val maximumRewardCap = dropParams["maximumRewardCap"] as Double
                            rewards.add(
                                DropReward(
                                    materialName,
                                    minimumRewardCount,
                                    multiplier,
                                    minimumThreshold,
                                    maximumRewardCap
                                )
                            )
                        } catch (e: Exception) {
                            Bukkit.getLogger().info(e.toString())
                        } catch (e: Error) {
                            Bukkit.getLogger().info(e.toString())
                        }
                    }
                }
            }
        }
    }

    companion object
}
