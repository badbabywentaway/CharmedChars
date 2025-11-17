# CharmedChars - How to Play

Welcome to CharmedChars! This is a word-forming puzzle game in Minecraft where you collect letter blocks, arrange them into words, and score points for rewards.

> **Note**: CharmedChars requires ItemsAdder (a proprietary plugin, purchased separately). See `README.md` or `QUICK_SETUP.md` for installation details.

## Quick Start

1. **Mine logs with gold tools** to collect letter blocks
2. **Place letter blocks** in straight lines to form words
3. **Break any letter** in your word with a gold tool to score
4. **Earn rewards** based on your word's score!

---

## 1. Collecting Letter Blocks

### How to Get Letter Blocks

**Mine any wood logs with GOLD TOOLS ONLY:**
- Oak, Spruce, Birch, Jungle, Acacia, Dark Oak, Mangrove, Cherry, Pale Oak
- Warped Stems and Crimson Stems (Nether wood)
- Bamboo blocks

**Drop Chances:**
- Base chance: **6%** per log broken
- With Looting I: **10%**
- With Looting II: **16%**
- With Looting III: **20%**

**Special Nether Wood Bonus:**
- Warped and Crimson Stems can also drop **number blocks (0-9)** and **operator blocks (+, -, *, /)**

### What You'll Get

When a letter block drops, you'll receive:
- One random letter block (A-Z)
- Random color: Cyan, Magenta, or Yellow
- Letters are weighted by frequency (E, A, R, I, O are more common than X, Z, Q, J)

---

## 2. Placing Letter Blocks

### Placement Rules

- Place blocks **horizontally or vertically** in straight lines
- Blocks must be **adjacent** (touching) to each other
- Must be on the **same Y level** (same height)
- Direction must be **all X-axis OR all Z-axis** (not diagonal)

### Example Valid Placements

```
Horizontal word:
[C] [A] [T]  ✓ Valid

Vertical word:
[D]
[O]
[G]  ✓ Valid

Invalid (diagonal):
[C]
    [A]
        [T]  ✗ Invalid
```

### Color Mixing

- You can mix colors in a word
- However, using **all the same color gives a 3x score bonus!**

---

## 3. Scoring Words

### How to Score

1. Form your word by placing letter blocks
2. **Break any letter block** in the word using a **gold tool**
3. The game will:
   - Detect the entire word automatically
   - Check if it's a valid English word
   - Calculate your score
   - Remove all blocks if valid
   - Give you rewards based on score

### Valid Words

- Minimum **2 letters** (default setting)
- Must be in the built-in English dictionary (~100,000+ words)
- Must be a straight line (not diagonal)

### If Your Word is Valid

- **"Hit: [score]"** message appears
- All letter blocks are removed
- Score is calculated
- Rewards drop at your location

### If Your Word is Invalid

- **"Miss"** message appears
- Blocks remain in place (don't break)
- No score, no rewards

---

## 4. Score Calculation

### Basic Scoring Formula

Each letter has a **frequency factor** based on how common it is in English:

```
Letter Score = Frequency Factor + 10
```

**Word Score = Sum of all letter scores**

### Letter Values (Examples)

| Letter | Frequency Factor | Score Value |
|--------|-----------------|-------------|
| E | 56.88 | 66.88 |
| A | 43.31 | 53.31 |
| R | 38.64 | 48.64 |
| I | 38.45 | 48.45 |
| O | 36.51 | 46.51 |
| T | 35.43 | 45.43 |
| Z | 1.39 | 11.39 |
| Q | 1.00 | 11.00 |
| X | 1.48 | 11.48 |

**Tip:** Rare letters like Z, Q, X are worth LESS because they're harder to use in words!

### Example: Word "HI"

```
H: 15.31 + 10 = 25.31
I: 38.45 + 10 = 48.45
----------------------------
Total Score: 73.76
```

### Color Bonus - Triple Score!

If **ALL blocks in your word are the same color**, you get a **3x multiplier**!

```
Example: "CAT" with all Cyan blocks
C: 23.13 + 10 = 33.13
A: 43.31 + 10 = 53.31
T: 35.43 + 10 = 45.43
----------------------------
Base Score: 131.87
× 3 (all cyan)
----------------------------
FINAL SCORE: 395.61
```

You'll see: **"Triple Score! All Blocks Are CYAN!"**

---

## 5. Earning Rewards

### How Rewards Work

When you score a word, you receive item drops based on your score. The default rewards are:

### Default Reward Tiers

**Iron Ingots** (Lower tier)
- Minimum score needed: **100**
- Base reward: 1 ingot
- Extra: +0.01 ingots per point above 100
- Maximum: 20 ingots

**Gold Nuggets** (Higher tier)
- Minimum score needed: **200**
- Base reward: 0 nuggets
- Extra: +0.01 nuggets per point above 200
- Maximum: 50 nuggets

### Reward Examples

**Score: 150**
- Iron: (150 - 100) × 0.01 + 1 = 1.5 → **2 Iron Ingots**
- Gold: Score too low → **0 Gold Nuggets**

**Score: 300**
- Iron: (300 - 100) × 0.01 + 1 = 3 → **3 Iron Ingots**
- Gold: (300 - 200) × 0.01 + 0 = 1 → **1 Gold Nugget**

**Score: 500 (with color bonus!)**
- Iron: (500 - 100) × 0.01 + 1 = 5 → **5 Iron Ingots**
- Gold: (500 - 200) × 0.01 + 0 = 3 → **3 Gold Nuggets**

---

## 6. Tips and Strategies

### Getting More Letter Blocks

- Use **gold axes** with **Looting III** for 20% drop chance
- Mine **Nether wood** to also collect number and operator blocks
- Farm wood in bulk - the more logs, the more letters!

### Maximizing Your Score

- **Aim for same-color words** to get the 3x multiplier
- **Longer words = higher scores** (more letters = more points)
- **Common letters (E, A, R, I, O, T, N, S) have higher values**
- Save your best letters for long same-color words

### Best Word Strategies

1. **Collect letters first** before building words
2. **Organize by color** in your inventory
3. **Plan long words** with same color for maximum bonus
4. **Keep a word list** handy for hard letters (Q, Z, X)

### Example High-Scoring Words

| Word | Colors | Base Score | With 3x Bonus |
|------|--------|------------|---------------|
| "ZONE" | Mixed | ~200 | 200 |
| "ZONE" | All Cyan | ~200 | **600** |
| "QUARTZ" | Mixed | ~250 | 250 |
| "QUARTZ" | All Magenta | ~250 | **750** |
| "AMAZING" | All Yellow | ~350 | **1050+** |

---

## 7. Commands

### Player Commands

**Give letter blocks to a player:**
```
/charblock <player> <color> <text>
```

Examples:
```
/charblock Steve cyan HELLO
/charblock Alex magenta QUARTZ
/charblock Notch yellow ABC123
```

Parameters:
- `<player>`: Player name
- `<color>`: cyan, magenta, or yellow
- `<text>`: Any combination of A-Z, 0-9, +, -, *, /

Permission: `charmedchars.blocks`

---

## 8. Frequently Asked Questions

### Q: My word didn't score. Why?

**Possible reasons:**
- Word is not in the dictionary (try a different word)
- Blocks aren't in a straight line (check alignment)
- Blocks are diagonal (must be horizontal or vertical)
- Word has less than 2 letters (default minimum)

### Q: Can I use numbers and operators in words?

**Currently:** The scoring system only recognizes A-Z letter words. Numbers and operators are collectible but can't be scored as "words" yet.

### Q: What happens if I break a block that's not part of a word?

**Answer:** The block will just break normally with no scoring. You need to form a valid word first.

### Q: Can I place blocks vertically?

**Yes!** Words can be horizontal OR vertical, as long as they're in a straight line.

### Q: Do I need to break the first or last letter?

**No!** You can break ANY letter in the word, and the system will detect the entire word automatically in all 4 cardinal directions.

### Q: What is the longest word I can make?

**Technically unlimited**, but you're limited by:
- How many blocks you can collect
- Available space to place them
- Finding valid dictionary words

---

## 9. Troubleshooting

### Letter blocks won't drop from logs

- Make sure you're using a **GOLD tool** (axe, pickaxe, shovel)
- Check that you're mining **wood logs** (not planks)
- Drop chance is 6% - keep mining!

### Word says "Miss" but should be valid

- Double-check spelling
- Try common words first to test
- Check that blocks are truly in a straight line (same Y level)

### Blocks won't place

- Make sure you have the custom resource pack loaded
- Run `/iastatus` to check ItemsAdder integration
- Contact server admin if textures aren't loading

---

## Getting Started Checklist

- [ ] Craft or obtain a gold axe
- [ ] Add Looting enchantment (optional, but recommended)
- [ ] Mine 20-30 logs to get your first letter blocks
- [ ] Practice making simple 2-3 letter words
- [ ] Try to make a word with all same color
- [ ] Break the word and collect your rewards!

---

**Good luck, and have fun spelling!**

For technical setup and admin configuration, see:
- `QUICK_SETUP.md` - ItemsAdder setup
- `REWARD_CONFIG.md` - Reward configuration guide
- `config.yml` - Full configuration options
