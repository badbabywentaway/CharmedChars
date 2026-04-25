# CharmedChars - Word Puzzle Adventure for Minecraft

> **🕊️ Fully playable in Peaceful Mode** — every feature, from letter block collection to the Ender Dragon finale, works without hostile mobs.

**Transform your Minecraft world into a word-forming puzzle game!** Collect magical letter blocks from logs, spell words for rewards, conquer the Nether's number guessing challenge, and claim a legendary logo block from the Ender Dragon.

---

## What is CharmedChars?

CharmedChars is a multi-layered puzzle adventure that weaves custom block gameplay through every dimension of Minecraft.

**In the Overworld**, mine logs with gold or pyrite tools to collect colorful letter blocks. Arrange them into words and score rewards based on your vocabulary skills and color matching strategy.

**In the Nether**, collect number blocks from warped and crimson stems. Each Fortress and Bastion holds a secret 3-digit code — use binary search to crack it and claim a jackpot of blaze rods or ender pearls.

**In The End**, the Ender Dragon drops a randomly-colored logo block above the exit portal on death. Place it in The End and strike it with a gold or pyrite tool to transform it into a matching colored shulker box filled with ghast tears — your ultimate reward for completing the adventure. The logo on the block is that of **Gaia Temperini**, the artist who created the original marble-style block textures used throughout CharmedChars.

Perfect for:
- **Educational servers** teaching spelling, vocabulary, and number strategy
- **Survival servers** adding unique progression mechanics across all three dimensions
- **Adventure maps** with puzzle elements and a satisfying finale
- **Players** who love word games, Scrabble-style scoring, and number puzzles

---

## Core Features

### Letter Block Collection System
- Mine any wood logs with **gold or pyrite tools** to collect random letter blocks
- **Three vibrant colors**: Cyan, Magenta, Yellow
- **Drop rates**: 6% base, up to 20% with Looting III enchantment
- **Frequency-based distribution**: Common letters (E, A, R) drop more often than rare ones (Q, X, Z)
- **Custom 512x512 textures** with ItemsAdder, Oraxen, or Nexo integration

### Word Formation & Scoring
- Place blocks in **straight lines** (horizontal or vertical)
- Break any letter with a gold/pyrite tool to score
- **Dictionary validation**: ~173,500 English words (ENABLE word list, public domain)
- **Smart length requirements**:
  - Same-color words: 3 letters minimum
  - Multi-color words: 4 letters minimum
- **Color bonus**: All same-color blocks = **3x score multiplier!**

### Pyrite (Fool's Gold) System
- Craftable alternative to expensive gold tools
- **Recipe**: Iron Ingot + Redstone = Pyrite Ingot
- **250 durability** vs gold's measly 32 uses
- **Iron-tier speed** - faster than gold!
- **Identical CharmedChars functionality** - works everywhere gold does

### Nether Structure Number Guessing Game
- Each **Fortress** and **Bastion** gets a secret 3-digit code (100-999)
- Collect number blocks from Warped/Crimson Stems
- Form 3-digit sequences and guess the structure's number
- **Correct guess**: Jackpot rewards! (12 Blaze Rods or 16 Ender Pearls)
- **Wrong guess (too high)**: Bed-like explosion - dangerous!
- **Wrong guess (too low)**: Blocks drop safely - try again!
- Use **binary search strategy** to find any number in ~10 guesses

### Ender Dragon Logo Block (v1.5.0) 🐉
- When the Ender Dragon dies, a **random-color logo block** drops on the end stone just outside the exit portal fountain — safe from the portal's item teleport and the dragon egg's non-full hitbox
- Drop is **world-based** — no killing blow required, works even with `/kill`
- **Three colors**: Cyan, Magenta, Yellow — chosen randomly each kill
- **Place it in The End** and hit it with a gold or pyrite tool to transform it into a matching **colored shulker box** filled with configurable loot (default: 4 Ghast Tears)
- Loot contents fully configurable via `logo-block.shulker-contents` in `config.yml`

### Glassing Beds (v1.4.0) 🛏️🔥
- **NEW**: Optional feature converts lava to glass when beds explode in Nether/End
- **Colored Glass (v1.4.1)**: Glass color matches bed color! 🎨
  - White beds → Clear glass
  - Red/Blue/Yellow/etc beds → Matching stained glass
  - All 16 Minecraft bed colors supported
  - Create decorative colored glass patterns in lava lakes
- **Activation System**: Hit 4 different operator blocks (+−×÷) with gold/pyrite tool each Nether visit
- **Requirements**: All same color, any order, must be in Nether dimension
- **Y-Level Restriction**: Only converts lava at Y≤28 (prevents ocean surface abuse)
- **5-Block Radius**: Converts lava in 11x11x11 cube around explosion
- **Session Reset**: Activation resets when entering Nether (portal/teleport/death)
- **Feature Control**: Disabled by default, enable with `/glassingbeds enable`
- **Strategic Gameplay**: Adds resource cost (operator blocks) for lava removal
- **Safety Warnings**: Not all lava flows guaranteed to stop; overlapping explosions can break glass

### Configurable Reward System
- Earn items based on word scores
- **Multiple reward tiers** with customizable formulas
- **Default rewards**: Iron Ingots (100+ score), Gold Nuggets (200+ score)
- **Admins**: Fully customize materials, quantities, and thresholds
- **Nether rewards**: Customize fortress/bastion prizes

---

## Quick Examples

### Example 1: Basic Word Scoring
```
1. Mine oak logs with gold pickaxe
2. Collect: [C-cyan] [A-cyan] [T-cyan]
3. Place in a line: [C][A][T]
4. Break any letter with gold tool
5. Result: "Hit: 395.61" (3x color bonus!)
6. Rewards: 3 Iron Ingots drop
```

### Example 2: High-Score Strategy
```
Word: "QUARTZ" (all magenta)
- Base score: ~250
- Color bonus: 3x
- Final score: ~750
- Rewards: 7 Iron Ingots + 5 Gold Nuggets
```

### Example 3: Nether Challenge
```
1. Enter Bastion Remnant
2. Message: "New Structure Discovered: BASTION"
3. Farm Warped Stems for number blocks
4. Guess 500 → "Too high!"
5. Guess 250 → "Too low!"
6. Guess 375 → "CORRECT! +16 Ender Pearls"
```

### Example 4: Colored Glass (v1.4.1)
```
1. Admin runs /glassingbeds enable
2. Player enters Nether via portal
3. Place 4 operators: cyan + cyan − cyan × cyan ÷
4. Hit sequence with gold pickaxe
5. Message: "✦ Glassing Beds ACTIVATED! ✦"
6. Place RED bed near lava → explosion creates RED stained glass
7. Place BLUE bed nearby → explosion creates BLUE stained glass
8. Place WHITE bed → explosion creates clear glass
9. Result: Decorative multicolored glass path through Nether!
```

---

## Installation

### Requirements
- **Minecraft**: 1.21.11+
- **Server**: Paper or Paper-based (Purpur, Pufferfish, etc.)
- **Java**: 21+
- **Custom Item Plugin** (choose ONE):
  - **ItemsAdder** 3.6.3-beta-14+ (premium - sold separately) - **Recommended**
  - **Oraxen** 1.212.0+ (premium with public source - sold separately) - Alternative option
  - **Nexo** 1.0.0+ (premium - sold separately) - Alternative option

### Optional Dependencies
- WorldGuard 7.0.14+ (region protection)
- GriefPrevention 16.15.0+ (claim protection)
- ProtocolLib 5.4.0+ (advanced features)

### Setup Steps (ItemsAdder)
1. Purchase and install **ItemsAdder** (proprietary plugin - not included)
2. Download CharmedChars JAR from [GitHub Releases](https://github.com/badbabywentaway/CharmedChars/releases)
3. Place both plugins in `plugins/` folder
4. Start server to generate configs
5. Run `/iasetup` to auto-configure ItemsAdder
6. Run `/iazip` to generate resource pack
7. Restart server completely
8. Players automatically receive resource pack on join

### Setup Steps (Oraxen - Alternative Option)
1. Purchase and install **Oraxen** (premium plugin with public source code)
2. Download CharmedChars JAR from [GitHub Releases](https://github.com/badbabywentaway/CharmedChars/releases)
3. Place both plugins in `plugins/` folder
4. Start server to generate configs
5. Run `/oraxensetup` to auto-configure Oraxen
6. Run `/oraxen reload all` to load items
7. Restart server (recommended)
8. Players automatically receive resource pack on join

### Setup Steps (Nexo - Alternative Option)
1. Purchase and install **Nexo** (premium plugin - requires license)
2. Download CharmedChars JAR from [GitHub Releases](https://github.com/badbabywentaway/CharmedChars/releases)
3. Place both plugins in `plugins/` folder
4. Start server to generate configs
5. Run `/nexosetup` to auto-configure Nexo
6. Run `/nexo reload all` to load items
7. Restart server (recommended)
8. Players automatically receive resource pack on join

**Detailed guides**:
- [QUICK_SETUP.md](https://github.com/badbabywentaway/CharmedChars/blob/master/QUICK_SETUP.md) (ItemsAdder)
- [ORAXEN_SETUP.md](https://github.com/badbabywentaway/CharmedChars/blob/master/ORAXEN_SETUP.md) (Oraxen)
- [NEXO_SETUP.md](https://github.com/badbabywentaway/CharmedChars/blob/master/NEXO_SETUP.md) (Nexo)

---

## Commands & Permissions

| Command | Description | Permission |
|---------|-------------|------------|
| `/charblock <player> <color> <text>` | Give letter blocks to a player | `charmedchars.blocks` |
| `/version` | Display plugin version | None |
| `/structurecode` | View structure's secret number | `charmedchars.blocks` |
| `/structuredb list [world]` | List all tracked structures | `charmedchars.blocks` |
| `/structuredb purge <type>` | Reset structure entries | `charmedchars.blocks` |
| `/glassingbeds <enable\|disable\|status>` | Control Glassing Beds feature (v1.4.0) | `charmedchars.admin` |
| `/iastatus` | Check ItemsAdder integration status | `charmedchars.admin` |
| `/iasetup [force]` | Auto-setup ItemsAdder configuration | `charmedchars.admin` |
| `/oraxensetup [force]` | Auto-setup Oraxen configuration | `charmedchars.admin` |
| `/nexosetup [force]` | Auto-setup Nexo configuration | `charmedchars.admin` |
| `/reload` | Reload plugin configuration | `charmedchars.admin` |

---

## Configuration Highlights

### Drop Rates (config.yml)
```yaml
letter-blocks:
  drop-chance: 0.06  # 6% base chance
  looting-multipliers:
    1: 1.67  # Looting I: 10%
    2: 2.67  # Looting II: 16%
    3: 3.33  # Looting III: 20%
```

### Reward Tiers (config.yml)
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
```

### Nether Rewards (config.yml)
```yaml
fortress-reward:
  material: BLAZE_ROD
  quantity: 12

bastion-reward:
  material: ENDER_PEARL
  quantity: 16
```

**Full configuration guide**: [REWARD_CONFIG.md](https://github.com/badbabywentaway/CharmedChars/blob/master/REWARD_CONFIG.md)

---

## Documentation

### For Players
- **[How to Play](https://github.com/badbabywentaway/CharmedChars/blob/master/PLAY_INSTRUCTIONS.md)** - Complete gameplay guide
  - Letter collection strategies
  - Word formation rules
  - Scoring mechanics and color bonuses
  - Pyrite crafting recipes
  - Nether number guessing game
  - Tips for maximizing rewards

### For Server Admins
- **[Quick Setup Guide - ItemsAdder](https://github.com/badbabywentaway/CharmedChars/blob/master/QUICK_SETUP.md)** - ItemsAdder installation
- **[Oraxen Setup Guide](https://github.com/badbabywentaway/CharmedChars/blob/master/ORAXEN_SETUP.md)** - Oraxen installation (alternative option)
- **[Nexo Setup Guide](https://github.com/badbabywentaway/CharmedChars/blob/master/NEXO_SETUP.md)** - Nexo installation (alternative option)
- **[Reward Configuration](https://github.com/badbabywentaway/CharmedChars/blob/master/REWARD_CONFIG.md)** - Customizing rewards
- **[Troubleshooting](https://github.com/badbabywentaway/CharmedChars/blob/master/TROUBLESHOOTING.md)** - Common issues

### For Developers
- **[Build Instructions](https://github.com/badbabywentaway/CharmedChars/blob/master/BUILD.md)** - Building from source
- **[Version History](https://github.com/badbabywentaway/CharmedChars/blob/master/VERSION.md)** - Complete changelog

---

## Technical Details

### Custom Blocks & Items
- **126 custom blocks**: 26 letters × 3 colors + 10 numbers × 3 colors + 4 operators × 3 colors + 3 logo blocks (cyan/magenta/yellow)
- **5 pyrite items**: Ingot, Pickaxe, Axe, Shovel, Hoe (craftable iron-tier tools)
- **Total**: 131 custom items with ItemsAdder, Oraxen, or Nexo integration
- **512x512 custom textures** by Gaia Temperini
- **Auto-setup commands** for ItemsAdder (`/iasetup`), Oraxen (`/oraxensetup`), and Nexo (`/nexosetup`)
- **Abstraction layer** supports multiple custom item providers (exactly one required)

### Database System
- **SQLite database** with Exposed ORM
- **Persistent structure tracking** for Nether number game
- **One-time reward enforcement** per structure
- **Multi-chunk structure support** using origin coordinates

### Protection Integration
- **Optional WorldGuard support** (soft dependency)
- **Optional GriefPrevention support** (soft dependency)
- **Configurable toggles** in config.yml
- **Works perfectly without protection plugins**

---

## Version Information

**Current Version**: 1.5.0

**Latest Updates**:
- **v1.5.0**: Ender Dragon Logo Block — Dragon finale and The End reward system
  - **NEW**: Ender Dragon death drops a random-color logo block on the end stone beside the exit portal fountain
  - **NEW**: Logo block placed in The End + gold/pyrite tool → colored shulker box with configurable loot
  - **NEW**: Logo block definitions added for all three providers (ItemsAdder, Oraxen, Nexo)
  - **FIXED**: Nexo integration fully tested and confirmed working at runtime
  - **FIXED**: Shadow JAR build — coroutines and other dependencies now correctly bundled
- **v1.4.2**: Dependency Updates for Minecraft 1.21.11 - Maintenance release with updated dependencies
  - **UPDATED**: Minecraft 1.21.10 → 1.21.11, PaperMC Build #130
  - **UPDATED**: Kotlin 2.2.21 → 2.3.20, Coroutines 1.9.0 → 1.10.2
  - **UPDATED**: ProtocolLib 5.3.0 → 5.4.0 (new Maven coordinates)
  - **UPDATED**: Oraxen 1.181.0 → 1.212.0, Nexo 0.1.0 → 1.0.0
  - **KEPT**: ItemsAdder 3.6.3-beta-14 (4.0.16 has stability issues)
  - **KEPT**: Exposed ORM 0.48.0 (1.0 migration deferred)
  - 100% backward compatible - no config/database changes
- **v1.4.1**: Colored Glass & Safety Enhancements - Glass color matches bed color
  - **NEW**: Colored glass feature - bed color determines glass color (16 colors)
  - White beds → Clear glass, other colors → Matching stained glass
  - Create decorative colored glass patterns in lava lakes
  - Comprehensive safety warnings for overlapping explosions
  - New FAQ entries about lava flow limitations and debris mining safety
  - 100% backward compatible with v1.4.0
- **v1.4.0**: Glassing Beds Feature - Major feature release with Nether lava management
  - **NEW**: Glassing Beds system - bed explosions convert lava to glass
  - **NEW**: Operator Activation - hit 4 operator blocks (+−×÷) to activate each Nether visit
  - Y-level restriction (Y≤28) prevents lava ocean surface abuse
  - 5-block cubic radius conversion (11x11x11 cube)
  - Session-based activation resets on dimension change
  - Admin command: `/glassingbeds enable/disable/status`
  - Optional feature - disabled by default
- **v1.3.0**: Nexo Integration & Fortune Enchantment - Complete three-provider support plus gameplay improvements
  - Full Nexo integration as third custom item provider option
  - Fortune enchantment now correctly boosts letter block drop rates
  - Copper tool support for Oraxen (Minecraft 1.21.9+)
  - Universal 256x256 textures for all providers
- **v1.2.0**: Oraxen compatibility - Full support for Oraxen as alternative to ItemsAdder
- **v1.1.x**: Pyrite system, bug fixes, and enhancements
- **v1.0.0**: Initial release with word scoring and Nether number game

**[Full Changelog](https://github.com/badbabywentaway/CharmedChars/blob/master/VERSION.md)**

---

## License & Dependencies

### CharmedChars
- **License**: GNU LGPL v3
- **Source Code**: Open source on GitHub
- **Commercial Use**: Permitted under LGPL v3 terms

### Required Dependency - Custom Item Plugin (Choose ONE)

**CRITICAL NOTICE**: CharmedChars requires **exactly ONE** of: ItemsAdder, Oraxen, or Nexo (not multiple, not none).

#### ItemsAdder (Recommended)
- **Premium plugin** - purchase required for server use
- **NOT included** with CharmedChars
- Available from [SpigotMC](https://www.spigotmc.org/resources/itemsadder.73355/)
- CharmedChars only uses ItemsAdder's public API (compileOnly dependency)
- Auto-setup with `/iasetup` command

#### Oraxen (Alternative Option)
- **Premium plugin with public source code** - purchase required for server use
- **NOT included** with CharmedChars
- Available from [SpigotMC](https://www.spigotmc.org/resources/oraxen.72448/)
- Source code available on [GitHub](https://github.com/oraxen/oraxen) but license requires purchase
- CharmedChars only uses Oraxen's public API (compileOnly dependency)
- Auto-setup with `/oraxensetup` command

#### Nexo (Alternative Option)
- **Premium plugin** - purchase required for server use
- **NOT included** with CharmedChars
- Available from [Polymart](https://polymart.org/resource/nexo.6901)
- CharmedChars only uses Nexo's public API (compileOnly dependency)
- Auto-setup with `/nexosetup` command

**Important**: Install **exactly ONE** of these plugins. CharmedChars will automatically detect which one you have and refuse to load if multiple are installed or none are installed.

---

## Credits

**Author**: StephanosBad

**Built With**:
- Kotlin 2.3.20 & Paper API 1.21.11
- ItemsAdder API 3.6.3-beta-14 (optional - one of three custom item providers - fully tested)
- Oraxen API 1.212.0 (optional - one of three custom item providers - fully tested)
- Nexo API 1.0.0 (optional - one of three custom item providers - fully tested)
- Exposed ORM 0.48.0 (SQLite)
- ProtocolLib 5.4.0 (optional)

**IMPORTANT**: ItemsAdder, Oraxen, and Nexo are all premium plugins sold separately and require purchase for server use.

**Artwork**:
- Block textures by Gaia Temperini
- Letter frequencies based on Oxford Concise Dictionary (9th edition, 1995)

**Development**:
- AI-assisted development by Claude (Anthropic)
- Features include: Nether number game, pyrite system, reward formulas, Oraxen compatibility, Nexo compatibility, custom item provider abstraction, comprehensive documentation
- All AI contributions include Co-Authored-By attribution in git commits

---

## Links

- **GitHub Repository**: https://github.com/badbabywentaway/CharmedChars
- **Download (Releases)**: https://github.com/badbabywentaway/CharmedChars/releases
- **How to Play**: https://github.com/badbabywentaway/CharmedChars/blob/master/PLAY_INSTRUCTIONS.md
- **Setup Guide (ItemsAdder)**: https://github.com/badbabywentaway/CharmedChars/blob/master/QUICK_SETUP.md
- **Setup Guide (Oraxen)**: https://github.com/badbabywentaway/CharmedChars/blob/master/ORAXEN_SETUP.md
- **Setup Guide (Nexo)**: https://github.com/badbabywentaway/CharmedChars/blob/master/NEXO_SETUP.md
- **Issue Tracker**: https://github.com/badbabywentaway/CharmedChars/issues

---

## Support

For help, feature requests, or bug reports:
1. Check the **[Troubleshooting Guide](https://github.com/badbabywentaway/CharmedChars/blob/master/TROUBLESHOOTING.md)**
2. Review **[Play Instructions](https://github.com/badbabywentaway/CharmedChars/blob/master/PLAY_INSTRUCTIONS.md)** and other documentation
3. File an issue on **[GitHub Issues](https://github.com/badbabywentaway/CharmedChars/issues)**

---

**Ready to add word-forming adventure to your Minecraft server? Download CharmedChars today!**
