# CharmedChars - How to Play

Welcome to CharmedChars! This is a word-forming puzzle game in Minecraft where you collect letter blocks, arrange them into words, and score points for rewards.

> **Note**: CharmedChars requires **either ItemsAdder OR Oraxen** (choose one):
> - **ItemsAdder** (proprietary, paid) - Recommended for protected block breaking
> - **Oraxen** (open-source, free) - Free alternative with vanilla-like behavior
>
> See `README.md`, `QUICK_SETUP.md` (ItemsAdder), or `ORAXEN_SETUP.md` (Oraxen) for installation details.

## Quick Start

1. **Mine logs with gold or pyrite tools** to collect letter blocks
2. **Place letter blocks** in straight lines to form words
3. **Break any letter** in your word with a gold or pyrite tool to score
4. **Earn rewards** based on your word's score!

> **New in v1.1.2:** Pyrite tools work just like gold but with iron-tier durability! Craft pyrite ingots from iron + redstone.
>
> **New in v1.4.0:** Glassing Beds feature - Convert lava to glass with bed explosions in the Nether! Requires activation with operator blocks. See Section 10 for details.

---

## 1. Collecting Letter Blocks

### How to Get Letter Blocks

**Mine any wood logs with GOLD or PYRITE TOOLS:**
- Oak, Spruce, Birch, Jungle, Acacia, Dark Oak, Mangrove, Cherry, Pale Oak
- Warped Stems and Crimson Stems (Nether wood)
- Bamboo blocks
- **Tools:** Gold pickaxe, gold axe, pyrite pickaxe, or pyrite axe

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

### ⚠️ Important: Breaking Placed Letter Blocks

**Be careful which tools you use to break placed letter blocks!**

**Tools That Work (break and drop the block):**
- All pickaxes (wooden, stone, copper, iron, golden, diamond, netherite)
- All axes (wooden, stone, copper, iron, golden, diamond, netherite)
- Pyrite pickaxe and pyrite axe

> **Note:** Copper tools require Minecraft 1.21.9+ ("The Copper Age" update)

**Tools That DON'T Work:**
- **ItemsAdder servers**: Bare hands, shovels, hoes → Shows purple warning, prevents breaking
- **Oraxen servers**: Bare hands, shovels, hoes → **Block breaks and disappears permanently** (no warning!)

> **Oraxen Warning**: On Oraxen servers, if you break a letter block with bare hands, a shovel, or a hoe, the block will be **permanently lost** without any warning message. Always use pickaxes or axes to safely mine letter blocks!

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
2. **Break any letter block** in the word using a **gold or pyrite tool**
3. The game will:
   - Detect the entire word automatically
   - Check if it's a valid English word
   - Validate minimum length
   - Calculate your score
   - Remove all blocks if valid
   - Give you rewards based on score

### Valid Words

- **Minimum length:**
  - **Single-color words:** 3 letters minimum (e.g., "CAT" all cyan)
  - **Multi-color words:** 4 letters minimum (e.g., "CATS" cyan+magenta)
- Must be in the built-in English dictionary (~173,500 words from ENABLE word list)
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

## 8. Pyrite (Fool's Gold) System

**New in v1.1.2!** Pyrite tools are a craftable alternative to gold with better durability.

### Why Use Pyrite?

Gold tools have only **32 durability**, which breaks quickly. Pyrite tools have:
- **250 durability** (same as iron tools)
- **Iron-tier mining speed** (faster than gold!)
- **Same CharmedChars functionality** (letter drops, word scoring, number sequences)
- **Cheaper to obtain** (no gold mining needed!)

### Crafting Pyrite

#### Step 1: Craft Pyrite Ingots

**Recipe:** Shapeless (put anywhere in crafting table)
```
Iron Ingot + Redstone = Pyrite Ingot
```

**How to get:**
- `/iagive <player> charmedchars:pyrite_ingot` (admin command)
- Craft yourself: 1 Iron Ingot + 1 Redstone

#### Step 2: Craft Pyrite Tools

Use standard tool recipes with Pyrite Ingots instead of gold:

**Pyrite Pickaxe:**
```
[P] [P] [P]
    [S]
    [S]
```

**Pyrite Axe:**
```
[P] [P]
[P] [S]
    [S]
```

**Pyrite Shovel:**
```
    [P]
    [S]
    [S]
```

**Pyrite Hoe:**
```
[P] [P]
    [S]
    [S]
```

Where:
- `P` = Pyrite Ingot
- `S` = Stick

### Pyrite Tool Stats

All pyrite tools have:
- **Durability:** 250 uses (vs gold's 32)
- **Mining Speed:** Iron-tier (faster than gold)
- **Enchantability:** Same as iron tools
- **Attack Damage:** Iron-tier
- **CharmedChars Features:** Identical to gold tools

### Using Pyrite Tools

Pyrite tools work **exactly like gold** for:

✅ **Mining logs for letter blocks:**
- Pyrite pickaxe or axe on any wood log
- Same drop chances as gold tools
- Same Looting enchantment bonuses

✅ **Breaking letter blocks to score words:**
- Use any pyrite tool
- Same scoring system
- Same rewards

✅ **Breaking number sequences in Nether structures:**
- Fortresses: Break 3-digit sequence for blaze rods
- Bastions: Break 3-digit sequence for ender pearls

### Comparison: Gold vs Pyrite

| Feature | Gold Tools | Pyrite Tools |
|---------|-----------|--------------|
| **Durability** | 32 uses | 250 uses ✅ |
| **Mining Speed** | Fast | Faster ✅ |
| **Letter Drops** | Yes | Yes |
| **Word Scoring** | Yes | Yes |
| **Number Sequences** | Yes | Yes |
| **Enchantable** | Low | Higher ✅ |
| **Cost** | Gold ore/nuggets | Iron + Redstone ✅ |

**Bottom line:** Pyrite is better than gold in every way except lore!

---

## 9. Nether Structure Number Guessing Game

**Available in:** Nether Fortresses and Bastion Remnants

Each Nether fortress and bastion remnant has a secret **3-digit number** (100-999). Find it by breaking number block sequences, and win valuable rewards!

### How It Works

#### Structure Assignment

- Every fortress and bastion gets a **unique random 3-digit code** when first discovered
- The code is stored in a database and persists across server restarts
- Each structure can only be won **once** (one-time rewards)
- You'll see a discovery message the first time you enter a structure

#### Discovery Notifications

When you first enter a Nether structure, you'll see:
```
New Structure Discovered: FORTRESS
Try to guess its secret 3-digit number!
```

or

```
New Structure Discovered: BASTION
Try to guess its secret 3-digit number!
```

### How to Play

#### Step 1: Collect Number Blocks

- Mine **Warped Stems** or **Crimson Stems** with gold/pyrite tools
- Number blocks (0-9) drop the same way as letter blocks
- Same drop chances: 6% base, up to 20% with Looting III

#### Step 2: Form a 3-Digit Sequence

- Place **exactly 3 number blocks** in a straight line (horizontal only)
- Must be in a **cardinal direction** (North, South, East, or West)
- Blocks must be **adjacent** (touching)
- Must be on the **same Y level** (same height)

**Example Valid Sequences:**
```
[4] [2] [0]  ✓ Horizontal (East-West or North-South)
```

**Invalid:**
```
[4]
[2]
[0]  ✗ Vertical not allowed

[4]   [2]   [0]  ✗ Gaps not allowed
```

#### Step 3: Break the Sequence

- **Must be inside** a fortress or bastion
- Break **any block** in the 3-digit sequence with a **gold or pyrite tool**
- The game reads the sequence in **hundreds-tens-ones** order
- Direction matters: [4][2][0] could be read as 420 or 024 depending on orientation

### Outcomes

#### Correct Guess - Jackpot!

You guessed the structure's secret number!

**Rewards (Default):**
- **Fortress:** 12 Blaze Rods
- **Bastion:** 16 Ender Pearls

**What happens:**
- Blocks disappear
- Rewards drop at your location
- Success message appears
- Structure is marked as completed (can't win again)

#### Wrong Guess - Too High

Your guess is **higher** than the secret number.

**What happens:**
- **EXPLOSION!** (same power as a bed explosion in the Nether)
- Blocks are destroyed
- You take damage (wear armor!)
- No items recovered
- Message: "Wrong! Your guess is too high"

**Tip:** This is dangerous - be prepared!

#### Wrong Guess - Too Low

Your guess is **lower** than the secret number.

**What happens:**
- Blocks **drop as items** (recoverable!)
- No explosion
- No damage
- Pick up your number blocks and try again
- Message: "Wrong! Your guess is too low"

**Tip:** This is the safer outcome - you get your blocks back!

#### Outside Structure

You broke a 3-digit sequence but you're **not in a fortress or bastion**.

**What happens:**
- Blocks drop as items (recoverable)
- Warning message appears
- No explosion, no rewards

### Strategy Tips

#### Finding the Number

**Use Binary Search Strategy:**
1. Start with **500** (middle of 100-999 range)
2. If too high: Try **300** (middle of 100-500)
3. If too low: Try **700** (middle of 500-999)
4. Keep narrowing the range

**Example Binary Search:**
```
Guess 500 → Too high
Guess 300 → Too low
Guess 400 → Too high
Guess 350 → Too low
Guess 375 → Too low
Guess 387 → CORRECT!
```

This method finds any number in **~10 guesses or less**!

#### Safety Tips

- **Wear armor** when guessing (explosions hurt!)
- **Don't stand on the blocks** when breaking them
- **Bring extra number blocks** so you can keep guessing
- **Bring fire resistance potions** for fortress explosions (lava nearby!)
- **Save your progress**: Use `/structurecode` to check the number before leaving

#### Farming Number Blocks

- **Best source:** Warped and Crimson Stems in the Nether
- **Use Looting III** on gold/pyrite axe for 20% drop rate
- **Farm in bulk** before attempting guesses
- **Organize inventory** by number for quick sequence building

### Commands

#### View Structure's Code

```
/structurecode
```

- Must be **inside** a fortress or bastion
- Shows the structure's secret 3-digit number
- Permission: `charmedchars.blocks`
- Useful for checking your work or admin debugging

**Example output:**
```
Structure Type: FORTRESS
Secret Number: 387
Origin: Chunk (-4, -38)
```

#### Admin Commands

```
/structuredb list [world]
```
- List all tracked structures and their numbers
- Permission: `charmedchars.blocks`

```
/structuredb purge <all|world|fortress|bastion>
```
- Remove structure entries (resets the game)
- Permission: `charmedchars.blocks`

### Rewards Configuration

Server admins can customize rewards in `config.yml`:

**Default Fortress Rewards:**
```yaml
fortress-reward:
  material: BLAZE_ROD
  quantity: 12
```

**Default Bastion Rewards:**
```yaml
bastion-reward:
  material: ENDER_PEARL
  quantity: 16
```

Admins can change the material type and quantity to anything!

### Frequently Asked Questions

**Q: Can I win the same structure multiple times?**
No - each structure awards prizes only once. After winning, further guesses won't give rewards.

**Q: What if I leave and come back?**
The structure's number stays the same. You can leave and return anytime.

**Q: Can I see the number without guessing?**
Yes - use `/structurecode` (requires permission). But that takes the fun out of it!

**Q: Do sequences work vertically?**
No - only horizontal sequences (North-South or East-West) are valid.

**Q: What happens if the structure already has been won?**
You'll see a message that the structure has already been completed, and no explosion occurs.

**Q: Can I use this to farm blaze rods/ender pearls?**
No - it's one-time per structure. But finding multiple structures in the Nether will let you win multiple times!

---

## 10. Glassing Beds Feature (v1.4.0)

**New in v1.4.0!** An optional feature that lets you convert lava to glass using bed explosions in the Nether and End.

> **Note:** This feature is **disabled by default**. Server admins must enable it with `/glassingbeds enable`.

### What is Glassing Beds?

When enabled, beds that explode in the Nether or End will convert nearby lava blocks into glass blocks. This provides a unique way to clear lava lakes and create safe paths through dangerous terrain.

**Key Features:**
- 🛏️ Bed explosions convert lava to glass (5-block radius)
- 🔒 Requires activation each time you enter the Nether
- 📏 Y-level restriction prevents ocean surface abuse (Y≤28)
- ⚙️ Server-controlled feature (disabled by default)

### How to Use Glassing Beds

#### Step 1: Server Must Enable Feature

Your server admin must first enable the feature:
```
/glassingbeds enable
```

If not enabled, beds will explode normally without converting lava.

#### Step 2: Activate in the Nether

**Every time you enter the Nether**, you must activate glassing beds by hitting a special sequence:

1. **Place 4 operator blocks** in a straight line:
   - Must be all 4 different operators: +, -, ×, ÷
   - Must be all the same color (cyan, magenta, or yellow)
   - Any order works: "+−×÷" or "÷×−+" both valid
   - Must be horizontal (not diagonal or vertical with Y changes)

2. **Hit the sequence** with a gold or pyrite tool (pickaxe or axe)

3. **Success message appears:**
   ```
   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
   ✦ Glassing Beds ACTIVATED! ✦
   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
   You can now use beds to convert lava to glass!
   ```

4. **Blocks are consumed** - The 4 operator blocks disappear

#### Step 3: Use Beds to Convert Lava

Once activated:
1. Find a lava lake or lava source
2. Place a bed near the lava (choose your favorite color!)
3. Right-click the bed to explode it
4. **BOOM!** Lava within 5 blocks converts to glass

**Important:**
- Only converts lava at Y≤28 (underground lava)
- Does NOT convert lava ocean surface (Y=31)
- Bed explosion is still dangerous - wear armor!

**Colored Glass Feature (v1.4.1):**
The glass color matches your bed color!
- 🤍 **White beds** → Clear glass (unstained)
- 🔴 **Red beds** → Red stained glass
- 🔵 **Blue beds** → Blue stained glass
- 🟡 **Yellow beds** → Yellow stained glass
- ...and all 16 Minecraft bed colors!

This lets you "paint" lava lakes with colored glass for decorative builds!

### Activation Rules

#### Valid Activation
✅ **All these work:**
```
cyan + cyan − cyan × cyan ÷  (order doesn't matter)
magenta ÷ magenta × magenta − magenta +
yellow − yellow + yellow ÷ yellow ×
```

#### Invalid Activation
❌ **These DON'T work:**

**Mixed colors:**
```
cyan + magenta − cyan × cyan ÷
→ "Mixed colors! All operators must be the same color."
```

**Duplicate operators:**
```
cyan + cyan + cyan × cyan ÷
→ "All 4 operators must be different!"
```

**Wrong count:**
```
cyan + cyan − cyan ×  (only 3 operators)
→ "Need exactly 4 operator blocks (+−×÷)"
```

**Wrong dimension:**
```
Try to activate in Overworld
→ "Operator activation only works in the Nether!"
```

### Session Reset System

Your glassing beds activation **resets** when you:
- ❌ Exit the Nether through a portal
- ❌ Teleport out of the Nether
- ❌ Die and respawn outside the Nether
- ❌ Use `/kill` or similar commands

**This means:**
1. Activate glassing beds in the Nether
2. Use beds to convert lava successfully
3. Leave Nether and return
4. **Activation is gone** - must re-activate!

**Why?** This prevents permanent activation and makes operator blocks a valuable resource.

### Examples

#### Example 1: Basic Usage
```
1. Admin enabled feature: /glassingbeds enable
2. Enter Nether via portal
3. Mine Warped Stems for operator blocks
4. Collect: cyan+ cyan− cyan× cyan÷
5. Place in a line: [+][−][×][÷]
6. Hit with gold pickaxe
7. Message: "✦ Glassing Beds ACTIVATED! ✦"
8. Place bed near lava lake
9. Right-click bed → BOOM! Lava becomes glass
```

#### Example 2: Underground Lava
```
Y-Level: 20 (underground)
Place bed near lava
Explosion converts lava to glass ✅
Safe path created!
```

#### Example 3: Lava Ocean Surface (Prevented)
```
Y-Level: 31 (lava ocean surface)
Place bed near lava
Explosion happens
Lava does NOT convert ❌
Message: (Y-level too high)
```

#### Example 4: Colored Glass (v1.4.1)
```
1. Activate glassing beds
2. Place RED bed near lava
3. Right-click → explosion
4. Lava converts to RED STAINED GLASS ✅
5. Place BLUE bed near different lava
6. Right-click → explosion
7. Lava converts to BLUE STAINED GLASS ✅
8. Use WHITE bed for clear glass
9. Result: Decorative multicolored glass paths!
```

#### Example 5: Forgot to Activate
```
1. Enter Nether
2. Place bed near lava without activating
3. Right-click bed → explosion
4. Lava doesn't convert
5. Message: "Glassing beds not activated for this Nether visit!"
6. Hint: "Hit 4 different operator blocks (+−×÷) of the same color"
```

### Strategy Tips

#### Operator Block Farming
- **Source:** Warped and Crimson Stems (Nether wood)
- **Drop rate:** Same as letters/numbers (6% base, 20% with Looting III)
- **Farm in bulk:** You'll need 4 operators each Nether visit
- **Stock colors:** Keep sets of same-color operators ready

#### Cost-Benefit Analysis
- **Cost:** 4 operator blocks per Nether session
- **Benefit:** Convert unlimited lava during that session
- **Worth it?** Depends on how much lava you need to clear

#### Safe Usage
- **Wear armor:** Bed explosions deal massive damage
- **Don't stand on bed:** Move away after placing
- **Fire resistance:** Recommended near lava
- **Blast protection:** Helps reduce explosion damage

### Configuration (Admins)

#### Enable/Disable Feature
```bash
/glassingbeds enable   # Turn on feature
/glassingbeds disable  # Turn off feature
/glassingbeds status   # Check if enabled
```

#### Config File Settings
Located in `config.yml`:
```yaml
glassing-beds:
  # Enable/disable the feature
  enabled: false

  # Maximum Y-level for lava conversion
  # Only lava at or below this Y will convert
  max-y: 28
```

**Customization:**
- `enabled`: Set to `true` to enable by default
- `max-y`: Adjust Y-level restriction (default: 28)
  - Lower = more restrictive
  - Higher = less restrictive
  - 28 = allows underground lava but not ocean (Y=31)

### Frequently Asked Questions

**Q: Does the feature work without activation?**
No - you must activate by hitting the operator sequence each Nether visit.

**Q: Can I stay activated by staying in the Nether?**
Yes! As long as you don't leave, your activation persists.

**Q: What if I die in the Nether?**
If you respawn outside the Nether, activation resets. If you respawn in the Nether (bed respawn), it stays active.

**Q: Do I need to activate in the End?**
No - activation only works in the Nether, but once activated, it works in both Nether and End.

**Q: Can I activate with other tools?**
No - only gold or pyrite tools work for activation.

**Q: What if my server has the feature disabled?**
Beds will explode normally without converting lava. Ask your admin to enable it!

**Q: Can I reuse the same operators?**
No - successful activation consumes all 4 operator blocks. You'll need to get more.

**Q: Does it work on flowing lava?**
Yes - both source blocks and flowing lava convert to glass.

**Q: What's the radius?**
5 blocks in all directions (11×11×11 cube total).

**Q: What colors of glass can I create? (v1.4.1)**
All 16 Minecraft bed colors are supported:
- WHITE → Clear glass (unstained)
- RED, ORANGE, YELLOW, LIME, GREEN, CYAN, LIGHT_BLUE, BLUE → Matching stained glass
- PURPLE, MAGENTA, PINK, BROWN, BLACK, GRAY, LIGHT_GRAY → Matching stained glass

**Q: Can I get clear glass instead of colored?**
Yes! Use a WHITE bed to create regular clear glass instead of stained glass.

**Q: Can I mix colors in one lava lake?**
Yes! Use different colored beds in different spots to create multicolored glass patterns.

---

## 11. Frequently Asked Questions

### Q: My word didn't score. Why?

**Possible reasons:**
- Word is not in the dictionary (try a different word)
- Word is too short:
  - Single-color words need at least 3 letters
  - Multi-color words need at least 4 letters
- Blocks aren't in a straight line (check alignment)
- Blocks are diagonal (must be horizontal or vertical)
- Not using a gold or pyrite tool

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

## 12. Troubleshooting

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

**Basic Gameplay:**
- [ ] Craft or obtain a gold axe (or pyrite axe for better durability)
- [ ] Add Looting enchantment (optional, but recommended)
- [ ] Mine 20-30 logs to get your first letter blocks
- [ ] Practice making simple 3-letter words with all same color (e.g., "CAT", "DOG", "RUN")
- [ ] Try making a 4+ letter word with multiple colors (e.g., "CATS", "DOGS", "RUNS")
- [ ] Break any letter in the word with your gold/pyrite tool to score
- [ ] Collect your rewards and aim for longer same-color words for bigger bonuses!

**Advanced Features (Optional):**
- [ ] Try the Nether number guessing game in fortresses/bastions (see Section 9)
- [ ] If enabled by admin: Activate Glassing Beds and convert lava in the Nether (see Section 10)

---

**Good luck, and have fun spelling!**

For technical setup and admin configuration, see:
- `QUICK_SETUP.md` - ItemsAdder setup
- `REWARD_CONFIG.md` - Reward configuration guide
- `config.yml` - Full configuration options
