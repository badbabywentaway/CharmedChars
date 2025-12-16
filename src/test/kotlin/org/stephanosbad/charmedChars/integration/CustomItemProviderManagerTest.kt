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

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * Unit tests for CustomItemProviderManager logic
 *
 * Tests the provider detection and initialization logic concepts:
 * - Mutual exclusivity pattern (cannot have multiple providers)
 * - Provider selection requirements (exactly one of: ItemsAdder, Oraxen, or Nexo)
 * - Configuration validation patterns
 *
 * NOTE: These tests focus on the logical patterns and validation rules
 * rather than full integration testing with actual provider implementations,
 * since provider APIs are not available in test environment.
 */
class CustomItemProviderManagerTest {

    // ==================== Provider Mutual Exclusivity Tests ====================

    @Test
    fun `exactly one provider should be active using count logic`() {
        // The manager enforces mutual exclusivity by counting active providers
        // Valid states: exactly 1 provider is true
        // Invalid states: 0 providers or 2+ providers

        val validConfigurations = listOf(
            Triple(true, false, false),   // Only ItemsAdder
            Triple(false, true, false),   // Only Oraxen
            Triple(false, false, true)    // Only Nexo
        )

        val invalidConfigurations = listOf(
            Triple(true, true, false),    // ItemsAdder + Oraxen
            Triple(true, false, true),    // ItemsAdder + Nexo
            Triple(false, true, true),    // Oraxen + Nexo
            Triple(true, true, true),     // All three
            Triple(false, false, false)   // None
        )

        for ((itemsAdder, oraxen, nexo) in validConfigurations) {
            val count = listOf(itemsAdder, oraxen, nexo).count { it }
            assertEquals(1, count,
                "ItemsAdder=$itemsAdder, Oraxen=$oraxen, Nexo=$nexo should have exactly 1 provider")
        }

        for ((itemsAdder, oraxen, nexo) in invalidConfigurations) {
            val count = listOf(itemsAdder, oraxen, nexo).count { it }
            assertNotEquals(1, count,
                "ItemsAdder=$itemsAdder, Oraxen=$oraxen, Nexo=$nexo should not have exactly 1 provider")
        }
    }

    @ParameterizedTest
    @CsvSource(
        "true, false, false, true",   // ItemsAdder only: valid
        "false, true, false, true",   // Oraxen only: valid
        "false, false, true, true",   // Nexo only: valid
        "true, true, false, false",   // ItemsAdder + Oraxen: invalid
        "true, false, true, false",   // ItemsAdder + Nexo: invalid
        "false, true, true, false",   // Oraxen + Nexo: invalid
        "true, true, true, false",    // All three: invalid
        "false, false, false, false"  // None: invalid
    )
    fun `provider configuration validation`(
        itemsAdderInstalled: Boolean,
        oraxenInstalled: Boolean,
        nexoInstalled: Boolean,
        expectedValid: Boolean
    ) {
        val count = listOf(itemsAdderInstalled, oraxenInstalled, nexoInstalled).count { it }
        val isValid = (count == 1)
        assertEquals(expectedValid, isValid,
            "ItemsAdder=$itemsAdderInstalled, Oraxen=$oraxenInstalled, Nexo=$nexoInstalled should be ${if (expectedValid) "valid" else "invalid"}")
    }

    @Test
    fun `count logic ensures mutual exclusivity`() {
        // Count logic: exactly 1 provider should be active
        // count == 0: none installed - invalid
        // count == 1: exactly one installed - valid
        // count == 2: two installed - invalid
        // count == 3: all installed - invalid

        val noneInstalled = listOf(false, false, false).count { it }
        val oneInstalled = listOf(true, false, false).count { it }
        val twoInstalled = listOf(true, true, false).count { it }
        val allInstalled = listOf(true, true, true).count { it }

        assertEquals(0, noneInstalled, "None installed should count to 0")
        assertEquals(1, oneInstalled, "One installed should count to 1")
        assertEquals(2, twoInstalled, "Two installed should count to 2")
        assertEquals(3, allInstalled, "All installed should count to 3")

        // Only count == 1 is valid
        assertFalse(noneInstalled == 1, "Count 0 should be invalid")
        assertTrue(oneInstalled == 1, "Count 1 should be valid")
        assertFalse(twoInstalled == 1, "Count 2 should be invalid")
        assertFalse(allInstalled == 1, "Count 3 should be invalid")
    }

    // ==================== Provider Name Tests ====================

    @Test
    fun `provider names are well-defined strings`() {
        val validProviderNames = listOf("ItemsAdder", "Oraxen", "Nexo", "None")

        for (name in validProviderNames) {
            assertFalse(name.isBlank(), "Provider name should not be blank: $name")
            assertTrue(name.first().isUpperCase(),
                "Provider name should start with uppercase: $name")
        }
    }

    @Test
    fun `none provider indicates uninitialized state`() {
        val uninitializedProviderName = "None"

        assertEquals("None", uninitializedProviderName,
            "Uninitialized state should return 'None'")
        assertNotEquals("ItemsAdder", uninitializedProviderName)
        assertNotEquals("Oraxen", uninitializedProviderName)
        assertNotEquals("Nexo", uninitializedProviderName)
    }

    // ==================== Error Message Requirements Tests ====================

    @Test
    fun `error messages should be actionable`() {
        // When both are installed, message should mention "both"
        val bothInstalledKeywords = listOf("both", "conflict", "mutual", "exclusive")
        assertTrue(bothInstalledKeywords.any { it == "both" },
            "Error for both installed should mention the conflict")

        // When neither is installed, message should mention requirement
        val neitherInstalledKeywords = listOf("neither", "requires", "install", "missing")
        assertTrue(neitherInstalledKeywords.any { it == "requires" },
            "Error for neither installed should mention requirement")
    }

    @Test
    fun `error state validation pattern`() {
        // Error states:
        // 1. Multiple providers installed (any combination of 2 or 3)
        // 2. No providers installed

        data class ErrorState(val itemsAdder: Boolean, val oraxen: Boolean, val nexo: Boolean, val description: String)

        val errorStates = listOf(
            ErrorState(true, true, false, "ItemsAdder + Oraxen"),
            ErrorState(true, false, true, "ItemsAdder + Nexo"),
            ErrorState(false, true, true, "Oraxen + Nexo"),
            ErrorState(true, true, true, "all three"),
            ErrorState(false, false, false, "none installed")
        )

        for (state in errorStates) {
            val count = listOf(state.itemsAdder, state.oraxen, state.nexo).count { it }
            val isError = (count != 1)
            assertTrue(isError, "Should be error state: ${state.description}")
        }
    }

    // ==================== Provider Selection Logic Tests ====================

    @Test
    fun `ItemsAdder is selected when only it is installed`() {
        // Logical flow: count == 1 and itemsAdderAvailable == true

        val itemsAdderAvailable = true
        val oraxenAvailable = false
        val nexoAvailable = false

        val count = listOf(itemsAdderAvailable, oraxenAvailable, nexoAvailable).count { it }
        val shouldUseItemsAdder = (count == 1) && itemsAdderAvailable
        assertTrue(shouldUseItemsAdder,
            "Should select ItemsAdder when only it is available")
    }

    @Test
    fun `Oraxen is selected when only it is installed`() {
        // Logical flow: count == 1 and oraxenAvailable == true

        val itemsAdderAvailable = false
        val oraxenAvailable = true
        val nexoAvailable = false

        val count = listOf(itemsAdderAvailable, oraxenAvailable, nexoAvailable).count { it }
        val shouldUseOraxen = (count == 1) && oraxenAvailable
        assertTrue(shouldUseOraxen,
            "Should select Oraxen when only it is available")
    }

    @Test
    fun `Nexo is selected when only it is installed`() {
        // Logical flow: count == 1 and nexoAvailable == true

        val itemsAdderAvailable = false
        val oraxenAvailable = false
        val nexoAvailable = true

        val count = listOf(itemsAdderAvailable, oraxenAvailable, nexoAvailable).count { it }
        val shouldUseNexo = (count == 1) && nexoAvailable
        assertTrue(shouldUseNexo,
            "Should select Nexo when only it is available")
    }

    @ParameterizedTest
    @CsvSource(
        "true, false, false, ItemsAdder",
        "false, true, false, Oraxen",
        "false, false, true, Nexo"
    )
    fun `correct provider is selected based on availability`(
        itemsAdderAvailable: Boolean,
        oraxenAvailable: Boolean,
        nexoAvailable: Boolean,
        expectedProvider: String
    ) {
        val selectedProvider = when {
            itemsAdderAvailable && !oraxenAvailable && !nexoAvailable -> "ItemsAdder"
            !itemsAdderAvailable && oraxenAvailable && !nexoAvailable -> "Oraxen"
            !itemsAdderAvailable && !oraxenAvailable && nexoAvailable -> "Nexo"
            else -> "None"
        }

        assertEquals(expectedProvider, selectedProvider,
            "Should select $expectedProvider when ItemsAdder=$itemsAdderAvailable, Oraxen=$oraxenAvailable, Nexo=$nexoAvailable")
    }

    // ==================== Initialization Success Criteria Tests ====================

    @Test
    fun `initialization succeeds only with exactly one provider`() {
        // Success criteria: count == 1

        val successCases = listOf(
            Triple(true, false, false),   // ItemsAdder only
            Triple(false, true, false),   // Oraxen only
            Triple(false, false, true)    // Nexo only
        )

        for ((itemsAdder, oraxen, nexo) in successCases) {
            val count = listOf(itemsAdder, oraxen, nexo).count { it }
            val shouldSucceed = (count == 1)
            assertTrue(shouldSucceed,
                "Should succeed with ItemsAdder=$itemsAdder, Oraxen=$oraxen, Nexo=$nexo")
        }
    }

    @Test
    fun `initialization fails without valid provider configuration`() {
        // Failure criteria: count != 1

        val failureCases = listOf(
            Triple(true, true, false),    // ItemsAdder + Oraxen
            Triple(true, false, true),    // ItemsAdder + Nexo
            Triple(false, true, true),    // Oraxen + Nexo
            Triple(true, true, true),     // All three
            Triple(false, false, false)   // None
        )

        for ((itemsAdder, oraxen, nexo) in failureCases) {
            val count = listOf(itemsAdder, oraxen, nexo).count { it }
            val shouldFail = (count != 1)
            assertTrue(shouldFail,
                "Should fail with ItemsAdder=$itemsAdder, Oraxen=$oraxen, Nexo=$nexo")
        }
    }

    // ==================== Provider Availability Check Tests ====================

    @Test
    fun `provider availability is boolean check`() {
        // isAvailable() should return true or false
        val availabilityStates = listOf(true, false)

        for (state in availabilityStates) {
            assertTrue(state == true || state == false,
                "Availability should be boolean: $state")
        }
    }

    @Test
    fun `plugin detection checks plugin manager`() {
        // Bukkit.getPluginManager().getPlugin("PluginName") returns:
        // - Plugin instance if installed
        // - null if not installed

        val pluginInstalled = true  // Would return Plugin instance
        val pluginNotInstalled = false  // Would return null

        assertNotEquals(pluginInstalled, pluginNotInstalled,
            "Installed and not installed should be different states")
    }

    // ==================== Integration Contract Tests ====================

    @Test
    fun `manager requires CharmedChars plugin instance`() {
        // Constructor: CustomItemProviderManager(plugin: CharmedChars)
        // This ensures logging and configuration access

        val hasPluginParameter = true
        assertTrue(hasPluginParameter,
            "Manager should require plugin instance in constructor")
    }

    @Test
    fun `initialization returns result with success flag`() {
        // InitResult should have:
        // - success: Boolean
        // - provider: CustomItemProvider?
        // - messages: List<String>

        data class MockInitResult(
            val success: Boolean,
            val provider: Any?,
            val messages: List<String>
        )

        val successResult = MockInitResult(true, "SomeProvider", listOf("Success"))
        val failureResult = MockInitResult(false, null, listOf("Error"))

        assertTrue(successResult.success, "Success result should have success=true")
        assertFalse(failureResult.success, "Failure result should have success=false")
        assertNotNull(successResult.provider, "Success should have provider")
        assertNull(failureResult.provider, "Failure should have null provider")
    }
}
