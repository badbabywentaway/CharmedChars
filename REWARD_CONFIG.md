# CharmedChars - Reward Configuration Guide

This guide explains how to configure the reward system in CharmedChars, allowing you to customize what items players receive when they score words.

---

## Table of Contents

1. [Overview](#overview)
2. [Configuration File Location](#configuration-file-location)
3. [Reward Formula Explained](#reward-formula-explained)
4. [Configuration Parameters](#configuration-parameters)
5. [Default Configuration](#default-configuration)
6. [Creating Custom Rewards](#creating-custom-rewards)
7. [Examples](#examples)
8. [Testing Your Configuration](#testing-your-configuration)
9. [Troubleshooting](#troubleshooting)

---

## Overview

The CharmedChars reward system converts word scores into item drops. When a player successfully scores a word:

1. The score is calculated based on letter frequency values
2. Each configured reward is evaluated
3. Items are dropped at the player's location
4. A message shows what rewards were earned

You can configure **multiple reward types** that trigger at different score thresholds.

---

## Configuration File Location

**Path:** `plugins/CharmedChars/config.yml`

On first run, the plugin creates this file with default settings. Edit this file to customize rewards.

**Important:** After editing, reload the server or restart the plugin for changes to take effect.

---

## Reward Formula Explained

Each reward uses this calculation:

```
IF score >= minimumThreshold:
    netAmount = (score - minimumThreshold) × multiplier + minimumRewardCount
ELSE:
    netAmount = minimumRewardCount

IF netAmount > maximumRewardCap:
    netAmount = maximumRewardCap

finalCount = round(netAmount)

IF finalCount > 0:
    Drop finalCount items
```

### What This Means

- **minimumThreshold**: Score needed to activate the bonus calculation
- **multiplier**: How much each point above threshold is worth
- **minimumRewardCount**: Base reward (even if threshold not met)
- **maximumRewardCap**: Maximum items that can drop

---

## Configuration Parameters

### Reward Structure

Each reward in the `Drop:` list needs these five parameters:

| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| `materialName` | String | Minecraft material name (must be valid Material enum) | `"IRON_INGOT"` |
| `minimumRewardCount` | Double | Base reward amount (given even below threshold) | `1.0` |
| `multiplier` | Double | Points-to-items conversion rate | `0.01` |
| `minimumThreshold` | Double | Minimum score to activate bonus calculation | `100.0` |
| `maximumRewardCap` | Double | Maximum items that can drop | `20.0` |

### Material Names

Use exact Minecraft/Spigot material enum names (case-sensitive):

**Common Items:**
- `IRON_INGOT`, `GOLD_INGOT`, `DIAMOND`, `EMERALD`
- `GOLD_NUGGET`, `IRON_NUGGET`
- `NETHERITE_INGOT`, `NETHERITE_SCRAP`
- `COAL`, `COPPER_INGOT`, `REDSTONE`
- `LAPIS_LAZULI`, `QUARTZ`
- `EXPERIENCE_BOTTLE` (XP bottles)

**Food:**
- `GOLDEN_APPLE`, `ENCHANTED_GOLDEN_APPLE`
- `COOKED_BEEF`, `BREAD`

**Other:**
- `ARROW`, `ENDER_PEARL`
- `TOTEM_OF_UNDYING`
- Any valid Spigot Material name

**Find all materials:** https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html

---

## Default Configuration

The plugin comes with two default reward tiers:

```yaml
Drop:
  # Tier 1: Iron Ingots (easier to get)
  - materialName: "IRON_INGOT"
    minimumRewardCount: 1.0
    multiplier: 0.01
    minimumThreshold: 100.0
    maximumRewardCap: 20.0

  # Tier 2: Gold Nuggets (higher scores)
  - materialName: "GOLD_NUGGET"
    minimumRewardCount: 0.0
    multiplier: 0.01
    minimumThreshold: 200.0
    maximumRewardCap: 50.0
```

### How Default Rewards Work

**Iron Ingots:**
- Always get at least 1 ingot (minimumRewardCount: 1.0)
- Score above 100: Get +0.01 ingots per point over 100
- Capped at 20 ingots maximum

**Gold Nuggets:**
- No base reward (minimumRewardCount: 0.0)
- Need score of 200+ to get any nuggets
- Get 0.01 nuggets per point over 200
- Capped at 50 nuggets maximum

---

## Creating Custom Rewards

### Example 1: Diamond Reward for High Scores

Add this to your `config.yml` under the `Drop:` section:

```yaml
Drop:
  - materialName: "IRON_INGOT"
    minimumRewardCount: 1.0
    multiplier: 0.01
    minimumThreshold: 100.0
    maximumRewardCap: 20.0

  - materialName: "GOLD_NUGGET"
    minimumRewardCount: 0.0
    multiplier: 0.01
    minimumThreshold: 200.0
    maximumRewardCap: 50.0

  # NEW: Diamonds for very high scores
  - materialName: "DIAMOND"
    minimumRewardCount: 0.0
    multiplier: 0.005
    minimumThreshold: 500.0
    maximumRewardCap: 10.0
```

**Result:** Players scoring 500+ get diamonds!
- Score 500: `(500-500) × 0.005 + 0 = 0` diamonds (need >500)
- Score 600: `(600-500) × 0.005 + 0 = 0.5 → 1` diamond
- Score 1000: `(1000-500) × 0.005 + 0 = 2.5 → 3` diamonds
- Score 3000: `(3000-500) × 0.005 + 0 = 12.5 → 10` diamonds (capped)

### Example 2: Emerald Reward with Guaranteed Base

```yaml
  - materialName: "EMERALD"
    minimumRewardCount: 1.0
    multiplier: 0.002
    minimumThreshold: 300.0
    maximumRewardCap: 5.0
```

**Result:**
- Any score: Always get 1 emerald minimum
- Score 300+: Get bonus emeralds
- Score 800: `(800-300) × 0.002 + 1 = 2` emeralds
- Score 2500: `(2500-300) × 0.002 + 1 = 5.4 → 5` emeralds (capped)

### Example 3: Experience Bottles

```yaml
  - materialName: "EXPERIENCE_BOTTLE"
    minimumRewardCount: 0.0
    multiplier: 0.05
    minimumThreshold: 150.0
    maximumRewardCap: 64.0
```

**Result:**
- Score 150+: Start getting XP bottles
- Score 200: `(200-150) × 0.05 = 2.5 → 3` bottles
- Score 500: `(500-150) × 0.05 = 17.5 → 18` bottles

### Example 4: Rare Item Jackpot

```yaml
  - materialName: "NETHERITE_INGOT"
    minimumRewardCount: 0.0
    multiplier: 0.001
    minimumThreshold: 1000.0
    maximumRewardCap: 3.0
```

**Result:** Only the highest scores get Netherite!
- Score 1000: `(1000-1000) × 0.001 = 0` netherite (need >1000)
- Score 1500: `(1500-1000) × 0.001 = 0.5 → 1` netherite
- Score 3000: `(3000-1000) × 0.001 = 2 → 2` netherite

---

## Examples

### Balanced Economy Configuration

```yaml
Drop:
  # Common reward - always get something
  - materialName: "COAL"
    minimumRewardCount: 2.0
    multiplier: 0.02
    minimumThreshold: 50.0
    maximumRewardCap: 32.0

  # Uncommon - medium scores
  - materialName: "IRON_INGOT"
    minimumRewardCount: 0.0
    multiplier: 0.01
    minimumThreshold: 150.0
    maximumRewardCap: 16.0

  # Rare - high scores
  - materialName: "GOLD_INGOT"
    minimumRewardCount: 0.0
    multiplier: 0.005
    minimumThreshold: 300.0
    maximumRewardCap: 8.0

  # Very rare - exceptional scores
  - materialName: "DIAMOND"
    minimumRewardCount: 0.0
    multiplier: 0.002
    minimumThreshold: 600.0
    maximumRewardCap: 4.0
```

### Generous Server Configuration

```yaml
Drop:
  # Everyone gets iron
  - materialName: "IRON_INGOT"
    minimumRewardCount: 3.0
    multiplier: 0.05
    minimumThreshold: 100.0
    maximumRewardCap: 64.0

  # Gold is easy to get
  - materialName: "GOLD_INGOT"
    minimumRewardCount: 1.0
    multiplier: 0.02
    minimumThreshold: 200.0
    maximumRewardCap: 32.0

  # Diamonds for decent scores
  - materialName: "DIAMOND"
    minimumRewardCount: 0.0
    multiplier: 0.01
    minimumThreshold: 400.0
    maximumRewardCap: 16.0
```

### Hardcore Server Configuration

```yaml
Drop:
  # Very low base rewards
  - materialName: "IRON_NUGGET"
    minimumRewardCount: 1.0
    multiplier: 0.005
    minimumThreshold: 200.0
    maximumRewardCap: 9.0

  # Iron ingots are valuable
  - materialName: "IRON_INGOT"
    minimumRewardCount: 0.0
    multiplier: 0.003
    minimumThreshold: 500.0
    maximumRewardCap: 5.0

  # Diamonds are jackpot only
  - materialName: "DIAMOND"
    minimumRewardCount: 0.0
    multiplier: 0.0005
    minimumThreshold: 1500.0
    maximumRewardCap: 2.0
```

### XP and Utility Focus

```yaml
Drop:
  # XP bottles
  - materialName: "EXPERIENCE_BOTTLE"
    minimumRewardCount: 1.0
    multiplier: 0.1
    minimumThreshold: 100.0
    maximumRewardCap: 64.0

  # Arrows for combat
  - materialName: "ARROW"
    minimumRewardCount: 5.0
    multiplier: 0.2
    minimumThreshold: 150.0
    maximumRewardCap: 64.0

  # Ender pearls for transport
  - materialName: "ENDER_PEARL"
    minimumRewardCount: 0.0
    multiplier: 0.01
    minimumThreshold: 300.0
    maximumRewardCap: 16.0
```

---

## Testing Your Configuration

### Step 1: Edit Configuration

1. Open `plugins/CharmedChars/config.yml`
2. Modify the `Drop:` section
3. Save the file

### Step 2: Reload

Restart your server or reload the plugin:
```
/reload confirm
```

Or restart the server for guaranteed config loading.

### Step 3: Test Scoring

1. Give yourself letter blocks:
   ```
   /charblock YourName cyan TESTWORD
   ```

2. Place the blocks to form a word
3. Break any letter with a gold tool
4. Check what items drop

### Step 4: Verify Calculations

Use this formula to verify expected rewards:

```
Expected = (score - threshold) × multiplier + minimumCount
(capped at maximumCap)
```

Check server logs for the actual score:
```
[PlayerName] ✓ HIT! Final score: 123.4
```

---

## Troubleshooting

### Issue: No items dropping

**Possible causes:**
- Score is below all reward thresholds
- minimumRewardCount is 0.0 for all rewards
- Configuration file has errors

**Solution:**
- Check server logs for errors
- Verify YAML syntax (indentation matters!)
- Test with low threshold reward first

### Issue: Wrong material name error

**Error in logs:**
```
Invalid material name: IRON_BAR
```

**Solution:**
- Use exact Spigot Material enum names
- Common mistake: `IRON_BAR` → should be `IRON_INGOT`
- Check: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html

### Issue: Too many/too few items

**Problem:** Calculation seems wrong

**Solution:**
1. Check your math:
   ```
   netAmount = (score - threshold) × multiplier + minimumCount
   ```
2. Remember: Result is ROUNDED to integer
3. Check maximumRewardCap - might be hitting cap
4. Verify multiplier (0.01 = 1% conversion, 0.001 = 0.1% conversion)

### Issue: Configuration not loading

**Symptoms:** Changes don't take effect

**Solution:**
- Restart server completely (not just reload)
- Check for YAML syntax errors (use YAML validator)
- Ensure file is saved in correct location: `plugins/CharmedChars/config.yml`
- Check file permissions (must be readable)

---

## Advanced Configuration Tips

### Setting Reward Tiers

Create progression by setting thresholds at different score ranges:

```yaml
Drop:
  - materialName: "COAL"
    minimumThreshold: 50.0     # Tier 1: 50-149

  - materialName: "IRON_INGOT"
    minimumThreshold: 150.0    # Tier 2: 150-299

  - materialName: "GOLD_INGOT"
    minimumThreshold: 300.0    # Tier 3: 300-599

  - materialName: "DIAMOND"
    minimumThreshold: 600.0    # Tier 4: 600+
```

### Avoiding Inflation

Keep multipliers low to prevent economy inflation:
- Conservative: 0.001 - 0.005
- Moderate: 0.01 - 0.02
- Generous: 0.05 - 0.1

### Encouraging High Scores

Use zero minimumRewardCount and high thresholds:

```yaml
  - materialName: "NETHERITE_INGOT"
    minimumRewardCount: 0.0    # No base reward
    minimumThreshold: 1000.0   # Need high score
```

### Guaranteed Rewards

Use non-zero minimumRewardCount with low threshold:

```yaml
  - materialName: "BREAD"
    minimumRewardCount: 3.0    # Always get 3
    minimumThreshold: 10.0     # Almost always met
```

---

## Configuration Examples by Score Range

### What Players Get at Different Scores

Using default configuration:

| Score | Iron Ingots | Gold Nuggets | Total Value |
|-------|-------------|--------------|-------------|
| 50 | 1 | 0 | Low |
| 100 | 1 | 0 | Low |
| 150 | 2 | 0 | Low-Medium |
| 200 | 2 | 0 | Medium |
| 250 | 3 | 1 | Medium |
| 300 | 3 | 1 | Medium-High |
| 400 | 4 | 2 | High |
| 500 | 5 | 3 | High |
| 1000 | 10 | 8 | Very High |
| 2000 | 20 (cap) | 18 | Maximum |

---

## Other Configuration Options

### Blocked Words

Prevent certain words from scoring:

```yaml
blocked-words:
  - "inappropriate1"
  - "inappropriate2"
```

### Minimum Word Length

Require longer words:

```yaml
scoring:
  min-word-length: 3  # Require 3+ letter words
```

### Drop Chances

Adjust letter block drop rates:

```yaml
letter-blocks:
  drop-chance: 0.05  # 5% base chance
  looting-multipliers:
    1: 1.5   # Looting I: 7.5%
    2: 2.0   # Looting II: 10%
    3: 2.5   # Looting III: 12.5%
```

---

## Summary Checklist

- [ ] Locate `plugins/CharmedChars/config.yml`
- [ ] Understand the reward formula
- [ ] Plan your reward tiers (thresholds and items)
- [ ] Edit the `Drop:` section with valid materials
- [ ] Set appropriate multipliers for your economy
- [ ] Configure maximumRewardCap to prevent exploits
- [ ] Test with various score values
- [ ] Reload/restart server
- [ ] Verify rewards drop correctly
- [ ] Adjust based on player feedback

---

**Need help with other configuration?**
- See `config.yml` for all available options
- See `PLAY_INSTRUCTIONS.md` for gameplay mechanics
- See `QUICK_SETUP.md` for ItemsAdder integration

Good luck configuring your rewards!
