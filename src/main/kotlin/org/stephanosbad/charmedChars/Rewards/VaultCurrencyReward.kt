package org.stephanosbad.charmedChars.Rewards

import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.entity.Player
import org.stephanosbad.charmedChars.CharmedChars
import org.stephanosbad.charmedChars.Utility.ColorPrint

/**
 * Currency reward for Vault plugin
 */
class VaultCurrencyReward(
    plugin: CharmedChars,
    minimumRewardCount: Double,
    multiplier: Double,
    minimumThreshold: Double,
    maximumRewardCap: Double
) : CurrencyReward(minimumRewardCount, multiplier, minimumThreshold, maximumRewardCap) {
    /**
     * Reference to root plugin
     */
    private var plugin: CharmedChars

    /**
     * Constructor
     * @param plugin - root plugin.
     * @param minimumRewardCount - Minimum number of rewards to drop.
     * @param multiplier - Multiply factor (by score)
     * @param minimumThreshold - Minimum score to apply reward
     * @param maximumRewardCap - Maximum number of rewards of this type.
     */
    init {
        this.plugin = plugin
    }

    /**
     * Need to manually set root plugin, needed by this reward type, to maintain serialization.
     * @param plugin - root plugin
     */
    fun setPlugin(plugin: CharmedChars) {
        this.plugin = plugin
    }

    /**
     * Apply the vault currency.
     * @param player - Player to apply
     * @param score - Score to apply.
     */
    override fun applyReward(player: Player, score: Double) {
        ColorPrint.sendPlayer(player, "Currency for score $score")

        var netAmount = if (score >= minimumThreshold)
            (score - minimumThreshold) * multiplier + minimumRewardCount
        else
            minimumRewardCount
        if (netAmount > maximumRewardCap) {
            netAmount = maximumRewardCap
        }
        val count = Math.round(netAmount).toDouble()
        ColorPrint.sendPlayer(player, count.toString())
        if (plugin.vaultEconomyEnabled) {
            val r: EconomyResponse? = plugin.econ?.depositPlayer(player, count)
            if (r?.transactionSuccess() != true) {
                println(r?.errorMessage)
            }
        } else {
            ColorPrint.sendPlayer(player, "NOT!!!")
        }
    }
}
