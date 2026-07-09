/*
 * CharmedChars - A word-forming puzzle game for Minecraft
 * Copyright (C) 2025 StephanosBad
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 */
package org.stephanosbad.charmedChars.rewards

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.math.roundToInt

/**
 * Tests for the reward amount formula used by DropReward / Reward.
 *
 * Formula (from Reward.kt):
 *   if score >= minimumThreshold:
 *     amount = (score - threshold) * multiplier + minimumRewardCount
 *   else:
 *     amount = minimumRewardCount
 *   amount = min(amount, maximumRewardCap)
 *   count  = amount.roundToInt()
 */
class RewardFormulaTest {

    /** Pure implementation of the formula — no Bukkit dependency. */
    private fun calcReward(
        score: Double,
        minimumRewardCount: Double,
        multiplier: Double,
        minimumThreshold: Double,
        maximumRewardCap: Double
    ): Int {
        var net = if (score >= minimumThreshold)
            (score - minimumThreshold) * multiplier + minimumRewardCount
        else
            minimumRewardCount
        if (net > maximumRewardCap) net = maximumRewardCap
        return net.roundToInt()
    }

    // ── Default iron ingot config (from config.yml) ───────────────────────────
    // minimumRewardCount: 1.0, multiplier: 0.01, threshold: 100.0, cap: 20.0

    @Test
    fun `score below threshold returns minimum reward count`() {
        val count = calcReward(50.0, 1.0, 0.01, 100.0, 20.0)
        assertEquals(1, count)
    }

    @Test
    fun `score at threshold returns minimum reward count`() {
        val count = calcReward(100.0, 1.0, 0.01, 100.0, 20.0)
        assertEquals(1, count)
    }

    @Test
    fun `score above threshold scales correctly`() {
        // (200 - 100) * 0.01 + 1.0 = 2.0
        val count = calcReward(200.0, 1.0, 0.01, 100.0, 20.0)
        assertEquals(2, count)
    }

    @Test
    fun `score well above threshold is capped at maximumRewardCap`() {
        // (2000 - 100) * 0.01 + 1.0 = 20.0 — exactly at cap
        val count = calcReward(2000.0, 1.0, 0.01, 100.0, 20.0)
        assertEquals(20, count)
    }

    @Test
    fun `score far above threshold is capped`() {
        val count = calcReward(9999.0, 1.0, 0.01, 100.0, 20.0)
        assertEquals(20, count)
    }

    // ── Default gold nugget config ────────────────────────────────────────────
    // minimumRewardCount: 0.0, multiplier: 0.01, threshold: 200.0, cap: 50.0

    @Test
    fun `zero minimum reward count returns zero below threshold`() {
        val count = calcReward(150.0, 0.0, 0.01, 200.0, 50.0)
        assertEquals(0, count)
    }

    @Test
    fun `zero minimum reward count at threshold returns zero`() {
        val count = calcReward(200.0, 0.0, 0.01, 200.0, 50.0)
        assertEquals(0, count)
    }

    @Test
    fun `zero minimum reward count scales above threshold`() {
        // (300 - 200) * 0.01 + 0.0 = 1.0
        val count = calcReward(300.0, 0.0, 0.01, 200.0, 50.0)
        assertEquals(1, count)
    }

    // ── Parameterised formula checks ──────────────────────────────────────────

    @ParameterizedTest
    @CsvSource(
        // score,  minCount, mult,  threshold, cap,  expected
        "100.0,    1.0,      0.01,  100.0,     20.0, 1",
        "200.0,    1.0,      0.01,  100.0,     20.0, 2",
        "600.0,    1.0,      0.01,  100.0,     20.0, 6",
        "2000.0,   1.0,      0.01,  100.0,     20.0, 20",
        "0.0,      1.0,      0.01,  100.0,     20.0, 1",
        "300.0,    0.0,      0.01,  200.0,     50.0, 1",
        "5200.0,   0.0,      0.01,  200.0,     50.0, 50"
    )
    fun `formula produces expected count`(
        score: Double,
        minCount: Double,
        mult: Double,
        threshold: Double,
        cap: Double,
        expected: Int
    ) {
        val count = calcReward(score, minCount, mult, threshold, cap)
        assertEquals(expected, count,
            "score=$score minCount=$minCount mult=$mult threshold=$threshold cap=$cap")
    }

    // ── Edge cases ────────────────────────────────────────────────────────────

    @Test
    fun `zero multiplier always returns minimum reward count below cap`() {
        val count = calcReward(9999.0, 1.0, 0.0, 100.0, 20.0)
        assertEquals(1, count)
    }

    @Test
    fun `minimum reward count of zero with zero multiplier always gives zero`() {
        val count = calcReward(9999.0, 0.0, 0.0, 100.0, 20.0)
        assertEquals(0, count)
    }

    @Test
    fun `rounding half up at 0_5 fractional amount`() {
        // (150 - 100) * 0.01 + 1.0 = 1.5 → rounds to 2
        val count = calcReward(150.0, 1.0, 0.01, 100.0, 20.0)
        assertEquals(2, count)
    }

    @Test
    fun `cap is inclusive`() {
        // result exactly equals cap
        val count = calcReward(2000.0, 1.0, 0.01, 100.0, 20.0)
        assertEquals(20, count)
    }

    @Test
    fun `minimum reward count cannot exceed cap`() {
        // minimumRewardCount > cap — cap wins
        val count = calcReward(50.0, 100.0, 0.0, 200.0, 5.0)
        assertEquals(5, count)
    }

    // ── Nether structure reward configs ──────────────────────────────────────
    // Fortress: minCount=24, mult=0.02, threshold=100, cap=30
    // Bastion:  minCount=16, mult=0.025, threshold=100, cap=40

    @Test
    fun `fortress reward at minimum score gives 24`() {
        val count = calcReward(0.0, 24.0, 0.02, 100.0, 30.0)
        assertEquals(24, count)
    }

    @Test
    fun `fortress reward above threshold scales`() {
        // (350 - 100) * 0.02 + 24 = 29.0
        val count = calcReward(350.0, 24.0, 0.02, 100.0, 30.0)
        assertEquals(29, count)
    }

    @Test
    fun `fortress reward is capped at 30`() {
        val count = calcReward(999.0, 24.0, 0.02, 100.0, 30.0)
        assertEquals(30, count)
    }

    @Test
    fun `bastion reward at minimum score gives 16`() {
        val count = calcReward(0.0, 16.0, 0.025, 100.0, 40.0)
        assertEquals(16, count)
    }

    @Test
    fun `bastion reward above threshold scales`() {
        // (500 - 100) * 0.025 + 16 = 26.0
        val count = calcReward(500.0, 16.0, 0.025, 100.0, 40.0)
        assertEquals(26, count)
    }

    @Test
    fun `bastion reward is capped at 40`() {
        // (1060 - 100) * 0.025 + 16 = 40.0 — exactly at cap; higher scores also return 40
        val count = calcReward(1060.0, 16.0, 0.025, 100.0, 40.0)
        assertEquals(40, count)
        val countAboveCap = calcReward(9999.0, 16.0, 0.025, 100.0, 40.0)
        assertEquals(40, countAboveCap)
    }
}
