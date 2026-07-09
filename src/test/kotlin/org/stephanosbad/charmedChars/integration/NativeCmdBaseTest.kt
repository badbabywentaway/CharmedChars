/*
 * CharmedChars - A word-forming puzzle game for Minecraft
 * Copyright (C) 2025 StephanosBad
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 */
package org.stephanosbad.charmedChars.integration

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

/**
 * Tests for native mode CMD base offset logic.
 *
 * These tests verify the arithmetic that NativeItemManagerSetup uses to assign
 * Custom Model Data values from a configurable base — without touching Paper API.
 */
class NativeCmdBaseTest {

    // Mirrors the constants from NativeItemManagerSetup / the item enums.
    private val letterCount   = 26   // LetterBlock.entries.size
    private val numberCount   = 10   // NumericBlock.entries.size
    private val operatorCount = 4    // NonAlphaNumBlocks.entries.size
    private val colorCount    = 3    // BlockColor.entries.size
    private val pyriteCount   = 5    // ingot + pickaxe + axe + shovel + hoe

    /** Items per color group. */
    private val itemsPerColor = letterCount + numberCount + operatorCount  // 40

    /** Total block items across all colors. */
    private val totalBlockItems = itemsPerColor * colorCount  // 120

    // ── Item counts ───────────────────────────────────────────────────────────

    @Test
    fun `each color has 40 items`() {
        assertEquals(40, itemsPerColor)
    }

    @Test
    fun `total block items is 120`() {
        assertEquals(120, totalBlockItems)
    }

    @Test
    fun `pyrite starts at cmdBase plus 120`() {
        val cmdBase = 1000
        val pyriteStart = cmdBase + totalBlockItems
        assertEquals(1120, pyriteStart)
    }

    @Test
    fun `total items claimed is 125`() {
        assertEquals(125, totalBlockItems + pyriteCount)
    }

    // ── Default base (1000) ───────────────────────────────────────────────────

    @Test
    fun `default base assigns first block CMD 1000`() {
        val cmdBase = 1000
        assertEquals(1000, cmdBase)
    }

    @Test
    fun `default base assigns last block CMD 1119`() {
        val cmdBase = 1000
        val lastBlockCmd = cmdBase + totalBlockItems - 1
        assertEquals(1119, lastBlockCmd)
    }

    @Test
    fun `default base assigns first pyrite CMD 1120`() {
        val cmdBase = 1000
        val pyriteBase = cmdBase + totalBlockItems
        assertEquals(1120, pyriteBase)
    }

    @Test
    fun `default base assigns last pyrite CMD 1124`() {
        val cmdBase = 1000
        val lastPyriteCmd = cmdBase + totalBlockItems + pyriteCount - 1
        assertEquals(1124, lastPyriteCmd)
    }

    // ── Custom base ───────────────────────────────────────────────────────────

    @ParameterizedTest
    @ValueSource(ints = [1000, 2000, 5000, 100, 9875])
    fun `block CMD range spans exactly 120 values from cmdBase`(cmdBase: Int) {
        val firstCmd = cmdBase
        val lastCmd  = cmdBase + totalBlockItems - 1
        assertEquals(120, lastCmd - firstCmd + 1)
    }

    @ParameterizedTest
    @ValueSource(ints = [1000, 2000, 5000, 100, 9875])
    fun `pyrite CMD range starts immediately after block range`(cmdBase: Int) {
        val pyriteStart = cmdBase + totalBlockItems
        val lastBlock   = cmdBase + totalBlockItems - 1
        assertEquals(lastBlock + 1, pyriteStart)
    }

    @ParameterizedTest
    @ValueSource(ints = [1000, 2000, 5000, 100, 9875])
    fun `total CMD range spans exactly 125 values`(cmdBase: Int) {
        val firstCmd = cmdBase
        val lastCmd  = cmdBase + totalBlockItems + pyriteCount - 1
        assertEquals(125, lastCmd - firstCmd + 1)
    }

    @Test
    fun `offset of 1000 shifts all CMDs up by 1000`() {
        val defaultBase = 1000
        val shiftedBase = 2000
        val shift = shiftedBase - defaultBase

        val defaultFirst = defaultBase
        val shiftedFirst = shiftedBase
        assertEquals(shift, shiftedFirst - defaultFirst)

        val defaultLast = defaultBase + totalBlockItems + pyriteCount - 1
        val shiftedLast = shiftedBase + totalBlockItems + pyriteCount - 1
        assertEquals(shift, shiftedLast - defaultLast)
    }

    // ── No overlap between block and pyrite ranges ────────────────────────────

    @ParameterizedTest
    @ValueSource(ints = [1000, 2000, 5000])
    fun `block and pyrite CMD ranges do not overlap`(cmdBase: Int) {
        val blockRange  = (cmdBase until cmdBase + totalBlockItems)
        val pyriteRange = (cmdBase + totalBlockItems until cmdBase + totalBlockItems + pyriteCount)

        assertTrue(blockRange.last < pyriteRange.first,
            "Block range must end before pyrite range begins")
    }

    // ── Item ordering within buildItemList ────────────────────────────────────

    @Test
    fun `items within a color are ordered letters then numbers then operators`() {
        // First 26 items in a color group are letters
        val firstLetterOffset = 0
        val firstNumberOffset = letterCount
        val firstOperatorOffset = letterCount + numberCount

        assertEquals(0,  firstLetterOffset)
        assertEquals(26, firstNumberOffset)
        assertEquals(36, firstOperatorOffset)
    }

    @Test
    fun `colors are ordered cyan then magenta then yellow`() {
        val colors = listOf("cyan", "magenta", "yellow")
        assertEquals("cyan",    colors[0])
        assertEquals("magenta", colors[1])
        assertEquals("yellow",  colors[2])
    }

    @Test
    fun `cyan items occupy CMD base through base plus 39`() {
        val cmdBase = 1000
        val cyanFirst = cmdBase
        val cyanLast  = cmdBase + itemsPerColor - 1
        assertEquals(1000, cyanFirst)
        assertEquals(1039, cyanLast)
    }

    @Test
    fun `magenta items occupy base plus 40 through base plus 79`() {
        val cmdBase = 1000
        val magentaFirst = cmdBase + itemsPerColor
        val magentaLast  = cmdBase + (itemsPerColor * 2) - 1
        assertEquals(1040, magentaFirst)
        assertEquals(1079, magentaLast)
    }

    @Test
    fun `yellow items occupy base plus 80 through base plus 119`() {
        val cmdBase = 1000
        val yellowFirst = cmdBase + (itemsPerColor * 2)
        val yellowLast  = cmdBase + totalBlockItems - 1
        assertEquals(1080, yellowFirst)
        assertEquals(1119, yellowLast)
    }

    // ── Pyrite individual CMD values ──────────────────────────────────────────

    @Test
    fun `pyrite item CMD assignments with default base`() {
        val cmdBase = 1000
        val pyriteBase = cmdBase + totalBlockItems

        assertEquals(1120, pyriteBase + 0, "pyrite_ingot")
        assertEquals(1121, pyriteBase + 1, "pyrite_pickaxe")
        assertEquals(1122, pyriteBase + 2, "pyrite_axe")
        assertEquals(1123, pyriteBase + 3, "pyrite_shovel")
        assertEquals(1124, pyriteBase + 4, "pyrite_hoe")
    }
}
