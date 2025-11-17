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

import dev.lone.itemsadder.api.CustomStack
import org.bukkit.inventory.ItemStack
import java.util.*

/**
 * Enum of letter blocks with frequency-based scoring
 *
 * Each letter has a frequency percentage and scoring factor based on real-world
 * English letter frequencies from the Oxford Concise Dictionary (9th edition, 1995).
 * Letters are weighted for random selection and assigned ItemsAdder custom blocks
 * in three colors (cyan, magenta, yellow).
 *
 * @property frequencyPercent The frequency percentage of this letter in English text
 * @property frequencyFactor The scoring factor for this letter (higher = rarer)
 * @property customVariation Legacy note block variation ID (kept for compatibility)
 */
enum class LetterBlock(
    /**
     * Frequency of letter found in real life via info received by Oxford publication
     */
    private val frequencyPercent: Double,
    /**
     * Score factor of letter (used for word scoring calculations)
     */
    val frequencyFactor: Double,

    /**
     * Noteblock variation (legacy - kept for compatibility)
     */
    val customVariation: Int
) {
    //Concise Oxford Dictionary (9th edition, 1995)
    E(11.1607, 56.88, 9),
    M(3.0129, 15.36, 17),
    A(8.4966, 43.31, 5),
    H(3.0034, 15.31, 12),
    R(7.5809, 38.64, 22),
    G(2.4705, 12.59, 11),
    I(7.5448, 38.45, 13),
    B(2.0720, 10.56, 6),
    O(7.1635, 36.51, 19),
    F(1.8121, 9.24, 10),
    T(6.9509, 35.43, 24),
    Y(1.7779, 9.06, 29),
    N(6.6544, 33.92, 18),
    W(1.2899, 6.57, 27),
    S(5.7351, 29.23, 23),
    K(1.1016, 5.61, 15),
    L(5.4893, 27.98, 16),
    V(1.0074, 5.13, 26),
    C(4.5388, 23.13, 7),
    X(0.2902, 1.48, 28),
    U(3.6308, 18.51, 25),
    Z(0.2722, 1.39, 30),
    D(3.3844, 17.25, 8),
    J(0.1965, 1.00, 14),
    P(3.1671, 16.14, 20),
    Q(0.1962, 1.0, 21);


    /**
     * The letter character this block represents (first character of enum name)
     */
    val character: Char = this.name[0]

    /**
     * Lazy-initialized map of ItemStacks for each block color
     */
    private val _itemStacks: MutableMap<BlockColor, ItemStack?> by lazy {
        mutableMapOf<BlockColor, ItemStack?>().apply {
            for (color in BlockColor.entries) {
                val itemId = "charmedchars:${color.directoryName}_${this@LetterBlock.character.lowercase()}"
                val customStack = CustomStack.getInstance(itemId)
                this[color] = customStack?.itemStack
            }
        }
    }

    /**
     * Map of ItemStacks for each block color (cyan, magenta, yellow)
     */
    val itemStacks: MutableMap<BlockColor, ItemStack?>
        get() = _itemStacks

    /**
     * Lower bound for random selection range
     */
    private var hitLow = 0.0

    /**
     * Upper bound for random selection range
     */
    private var hitHigh = 0.0

    /**
     * Determines if a random value falls within this letter's selection range
     *
     * @param testValue The random value to test
     * @return true if the value is within [hitLow, hitHigh)
     */
    private fun isHit(testValue: Double): Boolean {
        return (testValue >= hitLow) && (testValue < hitHigh)
    }

    companion object {
        /**
         * Maximum hit value for random selection (sum of all letter frequencies)
         */
        private val hitMax = sumAll()

        /**
         * Calculates the total frequency and initializes selection ranges
         *
         * Iterates through all letters and assigns each a range [hitLow, hitHigh)
         * proportional to its frequency percentage. This enables frequency-weighted
         * random letter selection.
         *
         * @return The total sum of all letter frequencies
         */
        private fun sumAll(): Double {
            var ret = 0.0
            for (ch in entries) {
                ch.hitLow = ret
                ret += ch.frequencyPercent
                ch.hitHigh = ret
            }
            return ret
        }

        /**
         * Selects a random letter weighted by rarity
         *
         * Common letters (E, A, etc.) are more likely to be picked than rare
         * letters (Z, Q, etc.) based on their frequency percentages.
         *
         * @return The randomly selected letter, or null if no match found
         */
        fun randomPick(): LetterBlock? {
            val randVal = Math.random() * hitMax
            return Arrays.stream<LetterBlock?>(entries.toTypedArray())
                .filter { it: LetterBlock? -> it!!.isHit(randVal) }.findFirst().orElse(null)
        }

        /**
         * Selects and creates a random letter block ItemStack
         *
         * Picks a random letter weighted by frequency and assigns it a random color.
         * Returns a cloned ItemStack to prevent reference sharing bugs.
         *
         * @return The letter block ItemStack, or null if ItemsAdder blocks aren't loaded
         */
        fun randomPickBlock(): ItemStack? {
            val pickedLetter = randomPick() ?: return null
            val pickedColor = BlockColor.getRand()
            val itemStack = pickedLetter.itemStacks[pickedColor]

            // Clone ItemStack to avoid reference sharing bug
            return itemStack?.clone()
        }
    }
}
