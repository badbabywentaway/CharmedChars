package org.stephanosbad.charmedChars.items

import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
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
import org.stephanosbad.charmedChars.block.CustomBlockEngine
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
        try {
            worldGuardPlugin = WorldGuardPlugin.inst()
            worldGuard = WorldGuard.getInstance()
            if (worldGuardPlugin != null && worldGuard != null) {
                Bukkit.getLogger().info("WorldGuard found.")
            } else {
                throw NullPointerException("Class variable did not instantiate")
            }
        } catch (e: Exception) {
            Bukkit.getLogger().info("WorldGuard not available.")
        } catch (e: Error) {
            Bukkit.getLogger().info("WorldGuard not available.")
        }

        try {
            griefPrevention = GriefPrevention.instance
            if (griefPrevention != null) {
                Bukkit.getLogger().info("GriefPrevention found.")
            } else {
                throw NullPointerException("Class variable did not instantiate")
            }
        } catch (e: Exception) {
            Bukkit.getLogger().info("GriefPrevention not available.")
        } catch (e: Error) {
            Bukkit.getLogger().info("GriefPrevention not available.")
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
        player.inventory.itemInMainHand
        player.inventory.itemInMainHand.enchantments

        if (!(player.inventory.itemInMainHand.containsEnchantment(Enchantment.SILK_TOUCH))) {
            //If there is no silk touch on it

            val material = e.getBlock().blockData.material
            if (list.containsKey(material)) {
                val hand = e.player.inventory.itemInMainHand


                //Must be gold item in hand
                if (hand.itemMeta == null) {
                    return
                }
                if (!hand.type.name.lowercase().contains("gold") &&
                    true != hand.itemMeta.displayName()?.examinableName()?.lowercase()?.contains("gold")
                ) {
                    return
                }

                var chance = .03
                if (hand.containsEnchantment(Enchantment.LOOTING)) {
                    when (hand.enchantments[Enchantment.LOOTING]) {
                        1 -> chance = .05
                        2 -> chance = .08
                        3 -> chance = .1
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
        val block =
            LetterBlock.randomPickBlock()
        val player = e.player

        if (block == null) {
            plugin.logger.warning("Failed to generate letter block - randomPickBlock() returned null")
            plugin.logger.warning("This likely means CustomBlockEngine.getInstance() is returning null")
            return
        }

        if (protectedSpot(player, e.getBlock().location, e.getBlock())) {
            player.sendMessage("Protected.")
            return
        }

        e.isCancelled = true
        e.getBlock().type = Material.AIR
        if (listForNumberDrops.containsKey(oldMaterial)) {
            player.world.dropItemNaturally(e.getBlock().location, randomNumAndCharacter()!!)
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

        return characterBlocksAvailableInNether!![(Math.random() * characterBlocksAvailableInNether!!.size).toInt()]
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

        var testBlock = e.getBlock()
        var score = 0.0
        var c: SimpleTuple<Char, Double> = testForLetter(e.player, testBlock)
        if (c.first == '\u0000') {
            return
        }
        val lateralDirection: LateralDirection = checkLateralBlocks(e.player, testBlock)

        val outString = StringBuilder()
        val blockArray: MutableList<Location> = ArrayList<Location>(mutableListOf<Location?>())
        var isSameColor = true
        var colorTest: BlockColor? = null


        if (lateralDirection.isValid) {


            while (c.first != '\u0000') {
                score += c.second + 10
                blockArray.add(testBlock.location)
                outString.append(c.first)
                testBlock = offsetBlock(testBlock, lateralDirection)
                c = testForLetter(e.player, testBlock)
                if(isSameColor)
                {
                    var getColor = plugin.customBlockEngine.letterBlockKeys.entries.firstOrNull{ it.value.second == getNoteblockNumber(testBlock) }?.key?.first
                    if(colorTest == null)
                    {
                         colorTest = getColor
                    }
                    else if (colorTest != getColor)
                    {
                        isSameColor = false
                    }
                }
            }
        }
        if(isSameColor && colorTest != null)
        {
            score *= 3
            e.player.sendMessage("Triple Score! All Blocks Are ${colorTest.name}!")
        }
        if (WordDict.singleton!!.words.contains(outString.toString().lowercase())) {
            e.isCancelled = true
            e.player.sendMessage("Hit: $score")

            for (locationOfBlock in blockArray) {
                e.getBlock().world.getBlockAt(locationOfBlock).type = Material.AIR
            }
            applyScore(e.player, score)
        } else {
            e.player.sendMessage("Miss")
        }
    }

    /**
     * Apply the score to the player. Drops or cash.
     * @param player - Player to apply score
     * @param score - score
     */
    private fun applyScore(player: Player, score: Double) {
        for (reward in rewards) {
            /*if (reward is VaultCurrencyReward) {
                reward.applyReward(player, score)
            } else */if (reward is DropReward) {
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
            Bukkit.getLogger().info("Part of word is protected: " + testBlock.location)
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
     * Get "noteblock" variation code. When Oraxen obsoletes this, this will change.
     * @param block - noteblock block
     * @return - Oraxen's noteblock variation code
     */
    fun getCustomVariation(block: Block?): LetterBlock? {
        var retValue = CustomBlockEngine.byAlreadyPlaced(block)?.id
        return retValue
    }

    /**
     * Determine if this location is protected from this player
     * @param player - MC Player
     * @param location - location to examine
     * @param block - block to examine (some grief plugins require this)
     * @return - verification that location is being protected
     */
    fun protectedSpot(player: Player?, location: Location, block: Block?): Boolean {
        var griefPrevention = this.griefPrevention
        if (griefPrevention != null && griefPrevention.allowBreak(player, block, location) != null) {
            return true
        }
        var worldGuardPlugin = this.worldGuardPlugin

        if (worldGuardPlugin != null &&
            !worldGuardPlugin.createProtectionQuery().testBlockBreak(player, block)
        ) {
            return true
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
