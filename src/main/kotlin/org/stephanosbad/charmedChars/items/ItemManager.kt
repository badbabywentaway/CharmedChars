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

/**
 * Main event handler for letter block drops and word validation
 *
 * This class manages the core gameplay loop:
 * - Dropping letter blocks when players mine logs with gold tools
 * - Detecting when players break letter blocks to form words
 * - Validating words against the dictionary
 * - Calculating scores based on letter frequencies and color matching
 * - Integrating with protection plugins (WorldGuard, GriefPrevention)
 * - Distributing rewards for valid words
 *
 * @property plugin The CharmedChars plugin instance
 */
class ItemManager @JvmOverloads constructor(localPlugin: CharmedChars? = null) :
    Listener {
    /**
     * Wood material list mapping regular logs to stripped logs
     *
     * When players mine these logs with gold tools (without Silk Touch), they have a chance
     * to receive letter blocks and the log is converted to its stripped variant.
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

    /**
     * Nether wood materials that can drop number and operator blocks
     *
     * When players mine warped or crimson stems with gold tools, they receive
     * number blocks (0-9) and operator blocks (+, -, *, /) in addition to letter blocks.
     */
    private val listForNumberDrops: HashMap<Material?, Material?> = object : HashMap<Material?, Material?>() {
        init {
            put(Material.WARPED_STEM, Material.STRIPPED_WARPED_STEM)
            put(Material.CRIMSON_STEM, Material.STRIPPED_CRIMSON_STEM)
        }
    }

    /**
     * Cached list of all numeric and operator block ItemStacks
     *
     * Lazy-initialized when first number/operator block needs to drop from nether wood.
     * Includes all numeric blocks (0-9) and operators (+, -, *, /) in all three colors.
     */
    private var characterBlocksAvailableInNether: MutableList<ItemStack?>? = null

    /**
     * Exclusion zone where letter block mechanics are disabled
     *
     * Defined in config.yml as a cuboid region (from/to coordinates).
     * Players cannot obtain or use letter blocks in this zone.
     */
    private var exclude: LocationPair? = null

    /**
     * Inclusion zone where letter block mechanics are exclusively allowed
     *
     * If defined in config.yml, letter blocks work ONLY within this zone.
     * All areas outside the inclusion zone are effectively excluded.
     */
    private var include: LocationPair? = null

    /**
     * WorldGuard integration instance (optional soft dependency)
     *
     * Used to check build permissions before allowing letter block placement/breaking.
     * Null if WorldGuard is not installed.
     *
     * Note: Stored as Any? to avoid class loading errors when WorldGuard is not present.
     */
    var worldGuard: Any? = null

    /**
     * WorldGuard plugin instance (optional soft dependency)
     *
     * Provides access to WorldGuard's protection query API.
     * Null if WorldGuard is not installed.
     *
     * Note: Stored as Any? to avoid class loading errors when WorldGuard is not present.
     */
    var worldGuardPlugin: Any? = null

    /**
     * GriefPrevention integration instance (optional soft dependency)
     *
     * Used to check claim permissions before allowing letter block placement/breaking.
     * Null if GriefPrevention is not installed.
     */
    var griefPrevention: GriefPrevention? = null

    /**
     * List of configured rewards for valid words
     *
     * Populated from config.yml. Currently supports drop-based rewards
     * (spawning items based on word score).
     */
    var rewards: MutableList<Reward> = ArrayList<Reward>()

    /**
     * Reference to the main CharmedChars plugin instance
     */
    var plugin = localPlugin!!

    /**
     * Initializes the ItemManager and sets up integrations
     *
     * - Attempts to load WorldGuard integration (optional)
     * - Attempts to load GriefPrevention integration (optional)
     * - Loads reward configurations from config.yml
     */
    init {
        // WorldGuard integration (optional soft dependency)
        try {
            val worldGuardPluginClass = Class.forName("com.sk89q.worldguard.bukkit.WorldGuardPlugin")
            val worldGuardClass = Class.forName("com.sk89q.worldguard.WorldGuard")

            val instMethod = worldGuardPluginClass.getMethod("inst")
            val getInstanceMethod = worldGuardClass.getMethod("getInstance")

            worldGuardPlugin = instMethod.invoke(null)
            worldGuard = getInstanceMethod.invoke(null)

            if (worldGuardPlugin != null && worldGuard != null) {
                val status = if (plugin.configManager.worldGuardIntegration) "enabled" else "disabled in config"
                Bukkit.getLogger().info("WorldGuard found - integration $status")
            }
        } catch (e: ClassNotFoundException) {
            Bukkit.getLogger().info("WorldGuard not available (optional)")
        } catch (e: Exception) {
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
     * Main event handler for block breaking
     *
     * Handles two scenarios:
     * 1. Breaking logs with gold tools (without Silk Touch) - drops letter blocks
     * 2. Breaking letter blocks - validates words and awards scores
     *
     * @param e The block break event from Bukkit/Spigot
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
     * Handles breaking wood blocks to drop letter blocks
     *
     * Called when a player breaks a log with a gold tool. Cancels the event,
     * converts the log to its stripped variant, and drops a random letter block.
     * If the broken wood is nether wood (warped/crimson stem), also drops a
     * random number or operator block.
     *
     * @param e The block break event
     * @param material The stripped wood material to drop
     * @param oldMaterial The original wood material that was broken
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

    /**
     * Selects a random numeric or operator block
     *
     * Lazy-initializes the pool of all number (0-9) and operator (+, -, *, /) blocks
     * in all three colors, then randomly selects one.
     *
     * @return A cloned ItemStack of a random number or operator block, or null if initialization fails
     */
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
     * Handles breaking letter blocks to form and validate words
     *
     * This is the core word validation logic:
     * 1. Detects the direction of adjacent letter blocks (X or Z axis)
     * 2. Scans in that direction to build the complete word
     * 3. Calculates score based on letter frequencies
     * 4. Applies 3x multiplier if all letters are the same color
     * 5. Validates against the dictionary
     * 6. Removes all blocks and awards score if valid, or cancels event if invalid
     *
     * @param e The block break event
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
     * Applies rewards based on the word score
     *
     * Iterates through all configured rewards (from config.yml) and applies them.
     * Currently supports drop rewards (spawning items based on score).
     *
     * @param player The player who formed the valid word
     * @param score The calculated word score (sum of letter frequencies + color bonus)
     */
    private fun applyScore(player: Player, score: Double) {
        for (reward in rewards) {
            if (reward is DropReward) {
                reward.applyReward(player, player.location, score)
            }
        }
    }

    /**
     * Gets the block adjacent to the given block in the specified direction
     *
     * @param testBlock The starting block
     * @param lateralDirection The direction (X or Z offset) to move
     * @return The adjacent block in the specified direction
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
     * Determines which lateral direction has letter blocks
     *
     * Checks all four cardinal directions (±X, ±Z) to find which one contains
     * letter blocks adjacent to the test block.
     *
     * @param player The player (used for protection checks)
     * @param testBlock The block to check around
     * @return A LateralDirection indicating which direction has letters, or (0,0) if none/multiple
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
     * Tests if a block is a letter block and returns its character and score
     *
     * Uses ItemsAdder API to identify custom letter blocks. Returns null character ('\u0000')
     * if the block is not a letter block, is protected, or is not a NoteBlock.
     *
     * @param player The player breaking the block (used for protection checks)
     * @param testBlock The block to test
     * @return A tuple containing the letter character and its frequency factor, or ('\u0000', 0.0) if not a letter
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

    /**
     * Gets the custom model data number from a note block's drops
     *
     * This is a legacy method for compatibility with older note block-based implementations.
     *
     * @param testBlock The note block to check
     * @return The custom model data value, or null if not found
     */
    fun getNoteblockNumber(testBlock: Block) : Int?
    {
        return testBlock.drops.firstOrNull()?.itemMeta?.customModelData

    }

    /**
     * Identifies which letter a custom block represents
     *
     * Parses the ItemsAdder namespaced ID (e.g., "charmedchars:cyan_a") to extract
     * the letter component and match it to a LetterBlock enum value.
     *
     * @param block The block to check
     * @return The corresponding LetterBlock enum, or null if not a letter block
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
     * Identifies the color of a custom block
     *
     * Parses the ItemsAdder namespaced ID (e.g., "charmedchars:cyan_a") to extract
     * the color component and match it to a BlockColor enum value.
     *
     * @param block The block to check
     * @return The corresponding BlockColor enum, or null if not a custom block
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
     * Checks if a location is protected from the player
     *
     * Checks multiple protection sources in order:
     * 1. GriefPrevention claims (if enabled in config)
     * 2. WorldGuard regions (if enabled in config)
     * 3. Config-defined include/exclude zones
     *
     * @param player The player attempting to interact with the block
     * @param location The location to check
     * @param block The block to check (required by some protection plugins)
     * @return true if the location is protected and the player cannot interact
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
        if (plugin.configManager.worldGuardIntegration && worldGuardPlugin != null) {
            try {
                val createQueryMethod = worldGuardPlugin!!.javaClass.getMethod("createProtectionQuery")
                val query = createQueryMethod.invoke(worldGuardPlugin)
                val testBreakMethod = query.javaClass.getMethod("testBlockBreak", Player::class.java, Block::class.java)
                val canBreak = testBreakMethod.invoke(query, player, block) as Boolean

                if (!canBreak) {
                    return true
                }
            } catch (e: Exception) {
                // If WorldGuard check fails, log and continue
                plugin.logger.warning("WorldGuard protection check failed: ${e.message}")
            }
        }

        return ourConfigProtects(location)
    }

    /**
     * Checks if config-defined zones protect a location
     *
     * Evaluates include/exclude zones defined in config.yml:
     * - If location is in an exclusion zone, returns true (protected)
     * - If an inclusion zone is defined and location is outside it, returns true (protected)
     * - Otherwise returns false (not protected)
     *
     * @param location The location to check
     * @return true if the location is protected by config zones
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
     * Loads reward configurations from config.yml
     *
     * Parses the reward configurations and creates appropriate reward instances.
     * Currently supports drop-based rewards with configurable materials, multipliers,
     * thresholds, and caps.
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
