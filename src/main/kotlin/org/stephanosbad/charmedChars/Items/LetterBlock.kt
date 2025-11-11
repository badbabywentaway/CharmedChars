package org.stephanosbad.charmedChars.Items

import org.bukkit.inventory.ItemStack
import org.stephanosbad.charmedChars.Items.BlockColor
import org.stephanosbad.charmedChars.Block.CustomBlockEngine
import java.util.*

enum class LetterBlock(
    /**
     * Frequency of letter found in real life via info received by Oxford publication.
     */
    private val frequencyPercent: Double,
    /**
     * Score facter of letter
     */
    val frequencyFactor: Double,
    /**
     * Oraxen Noteblock variation
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
     * letter block character
     */
    val character: Char = this.name[0]

    val itemStacks: MutableMap<BlockColor, ItemStack?> = mutableMapOf()

    /**
     * Hit range low (randomizer)
     */
    private var hitLow = 0.0

    /**
     * Hit range high (randomizer)
     */
    private var hitHigh = 0.0

    /**
     * Oraxen ID
     */
    //val letterBlockId  = BlockLetter.entries.firstOrNull{ it.filenameBase == this.character.lowercaseChar().toString()}

    /**
     * Determine if letter is hit by randomizer
     * @param testValue - hit value
     * @return - Veracity of hit
     */
    private fun isHit(testValue: Double): Boolean {
        return (testValue >= hitLow) && (testValue < hitHigh)
    }

    /**
     * Constructor
     * @param frequencyPercent - Frequency of letter found in real life via info received by Oxford publication.
     * @param frequencyFactor - Score facter of letter
     * @param customVariation - Oraxen noteblock variation
     */
    init {
        //letterBlockId?.let {
            for (color in BlockColor.entries) {
                this.itemStacks[color] = CustomBlockEngine.getInstance(color, this)!!.itemStack!!
            }
        //}
    }

    companion object {
        /**
         * Maximum hit value.
         */
        private val hitMax = sumAll()

        /**
         * Maximum hit value.
         * @return - Maximum hit value.
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
         * rarity weighted random letter picker
         * @return - picked letter
         */
        fun randomPick(): LetterBlock? {
            val randVal = Math.random() * hitMax
            return Arrays.stream<LetterBlock?>(entries.toTypedArray())
                .filter { it: LetterBlock? -> it!!.isHit(randVal) }.findFirst().orElse(null)
        }

        /**
         * Letter block random picker. Weighted by rarity.
         * @return Item stack of single letter block
         */
        fun randomPickBlock(): ItemStack? {
            return randomPick()!!.itemStacks[BlockColor.getRand()]
        }
    }
}
