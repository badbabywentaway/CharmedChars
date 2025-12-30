# CharmedChars

A word-forming puzzle game for Minecraft where players collect letter blocks from logs, arrange them into words, and earn rewards based on word scores.

## ⚠️ Important Licensing Information

### CharmedChars License
CharmedChars is **open source** software licensed under **GNU LGPL v3**.

### Required Custom Item Plugin - ItemsAdder OR Oraxen
**CRITICAL NOTICE**: CharmedChars requires **either ItemsAdder OR Oraxen** (not both).

#### ItemsAdder (Recommended)
- **Proprietary/commercial plugin** - purchase separately
- **NOT included** with CharmedChars
- **NOT open source** - paid premium plugin
- Available from: [SpigotMC](https://www.spigotmc.org/resources/itemsadder.73355/)
- **Auto-setup available** with `/iasetup` command
- You **MUST comply** with ItemsAdder's license terms

#### Oraxen (Alternative)
- Open-source custom items plugin
- Available from: [SpigotMC](https://www.spigotmc.org/resources/oraxen.72448/) or [GitHub](https://github.com/oraxen/oraxen)
- **Auto-setup available** with `/oraxensetup` command (see `ORAXEN_SETUP.md`)
- Free alternative to ItemsAdder

**Important**: Install **exactly ONE** of these plugins. CharmedChars will automatically detect which one you have and refuse to load if both are installed or neither is installed.

---

## Quick Start

### Requirements

**Server Software:**
- Minecraft 1.21.10+
- Paper or Paper-based server (Purpur, Pufferfish, etc.)
- Java 21+

**Required Plugin (choose ONE):**
- ItemsAdder 3.6.3-beta-14+ (**proprietary - purchase separately**) - Recommended
- Oraxen 1.181.0+ (open-source, free) - Free alternative

Both plugins now have auto-setup commands!

**Optional Plugins:**
- WorldGuard 7.0.14+ (for region protection)
- GriefPrevention 16.15.0+ (for claim protection)
- ProtocolLib 5.3.0+ (for advanced features)

### Installation

#### With ItemsAdder (Recommended)
1. **Purchase and install ItemsAdder** (required proprietary dependency)
2. Download CharmedChars jar
3. Place both plugins in `plugins/` folder
4. Start server to generate configs
5. Run `/iasetup` to auto-configure ItemsAdder
6. Run `/iazip` to generate resource pack
7. Restart server
8. Players automatically receive resource pack on join

#### With Oraxen (Alternative)
1. **Install Oraxen** (free open-source alternative)
2. Download CharmedChars jar
3. Place both plugins in `plugins/` folder
4. Start server to generate configs
5. Run `/oraxensetup` to auto-configure Oraxen
6. Run `/oraxen reload all` to load items
7. Restart server (recommended)
8. Players automatically receive resource pack on join

See `QUICK_SETUP.md` (ItemsAdder) or `ORAXEN_SETUP.md` (Oraxen) for detailed instructions.

---

## ItemsAdder vs Oraxen: Key Differences

Both custom item providers work with CharmedChars, but they have different behaviors:

| Feature | ItemsAdder | Oraxen |
|---------|------------|--------|
| **License** | Proprietary (paid) | Open-source (free) |
| **Texture Resolution** | 512x512 | 256x256 (1.203+) |
| **Block Breaking Without Correct Tool** | **Shows purple warning, prevents breaking** | **Block disappears without dropping** |
| **Player Feedback** | Clear visual warning message | No warning (silent loss) |
| **Tool Whitelist** | `break_tools_whitelist` with wildcards (`_AXE`, `_PICKAXE`) | `minimal_type` with explicit material list |
| **Auto-Setup Command** | `/iasetup` | `/oraxensetup` |

**Important Note on Block Breaking:**
- **ItemsAdder**: Players cannot accidentally lose blocks - non-whitelisted tools show a purple warning and prevent breaking
- **Oraxen**: Players CAN lose blocks permanently if using wrong tools (bare hands, shovels, hoes) - blocks break and disappear without dropping items
- This difference is **by design** - Oraxen maintainers intentionally do not prevent breaking ([GitHub Issue #36](https://github.com/oraxen/Oraxen/issues/36))

**Current Tool Configuration:**
Both providers allow these tools to break blocks and receive drops:
- All pickaxes (wooden, stone, copper, iron, golden, diamond, netherite)
- All axes (wooden, stone, copper, iron, golden, diamond, netherite)
- Pyrite pickaxe and pyrite axe (custom items)

> **Note:** Copper tools require Minecraft 1.21.9+ ("The Copper Age" update)

**Recommendation:**
- Choose **ItemsAdder** if you want protected blocks with clear player feedback (requires purchase)
- Choose **Oraxen** if you prefer free/open-source and accept vanilla-like block loss behavior

---

## How to Play

### Collecting Letter Blocks
- Mine any wood logs with **gold or pyrite tools**
- Drop chance: 6% (10%/16%/20% with Looting I/II/III)
- Receive random colored letter blocks (Cyan, Magenta, Yellow)
- Pyrite tools: Craft with Iron + Redstone for 250 durability (vs gold's 32)

### Forming Words
- Place letter blocks in **straight lines** (horizontal or vertical)
- Break any letter in the word with a gold or pyrite tool
- Word is validated against ~173,500 English word dictionary (ENABLE word list)
- Minimum lengths: 3 letters (same-color) or 4 letters (multi-color)

### Scoring
- Each letter has a frequency-based score
- **Color bonus**: All same-color blocks = **3x multiplier**
- Earn configurable rewards based on score

### Nether Challenge
- Each fortress/bastion has a secret 3-digit code (100-999)
- Guess correctly for rewards (12 Blaze Rods or 16 Ender Pearls)
- Wrong guesses: Explosion if too high, blocks drop if too low

See `PLAY_INSTRUCTIONS.md` for complete gameplay guide.

---

## Features

### Gameplay
- Letter block collection from wood logs with gold or pyrite tools
- Word formation with straight-line placement rules
- Dictionary validation (~173,500 words - ENABLE word list, public domain)
- Minimum word lengths (3 letters same-color, 4 letters multi-color)
- Frequency-based scoring system
- Color bonus multiplier (3x for same-color words)
- Configurable item rewards
- Pyrite (Fool's Gold) system - craftable iron-tier alternative to gold tools
- Nether Structure Number Guessing Game - guess 3-digit codes in fortresses and bastions for rewards

### Technical
- ItemsAdder or Oraxen integration for custom blocks and items (plugin auto-detects which is installed)
- 123 custom blocks (26 letters × 3 colors + 10 numbers × 3 colors + 4 operators × 3 colors)
- 5 pyrite items (ingot, pickaxe, axe, shovel, hoe)
- Total: 128 custom items with 512x512 custom textures
- Optional WorldGuard/GriefPrevention protection support
- Configurable drop rates and Looting enchantment scaling
- SQLite database for Nether structure tracking

### Administration
- Auto-setup commands for ItemsAdder configuration
- Flexible reward system with configurable formulas
- Built-in HTTP server for resource pack delivery
- Comprehensive config for all gameplay values
- Config toggles for protection plugin integrations

---

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/charblock <player> <color> <text>` | Give letter blocks to a player | `charmedchars.blocks` |
| `/version` | Display plugin version | None |
| `/structurecode` | View structure's secret number | `charmedchars.blocks` |
| `/structuredb list [world]` | List all tracked structures | `charmedchars.blocks` |
| `/structuredb purge <type>` | Reset structure entries | `charmedchars.blocks` |
| `/iastatus` | Check ItemsAdder integration status | `charmedchars.admin` |
| `/iasetup [force]` | Auto-setup ItemsAdder configuration | `charmedchars.admin` |
| `/reload` | Reload plugin configuration | `charmedchars.admin` |

---

## Configuration

### Drop Rates (config.yml)
```yaml
letter-blocks:
  drop-chance: 0.06  # 6% base chance
  looting-multipliers:
    1: 1.67  # Looting I: 10%
    2: 2.67  # Looting II: 16%
    3: 3.33  # Looting III: 20%
```

### Rewards (config.yml)
```yaml
Drop:
  - materialName: "IRON_INGOT"
    minimumRewardCount: 1.0
    multiplier: 0.01
    minimumThreshold: 100.0
    maximumRewardCap: 20.0
```

### Protection Integration (config.yml)
```yaml
protection:
  worldguard-integration: true      # Optional - disable if needed
  griefprevention-integration: true # Optional - disable if needed
```

See `REWARD_CONFIG.md` for complete configuration documentation.

---

## Documentation

- **QUICK_SETUP.md** - Installation and setup guide
- **PLAY_INSTRUCTIONS.md** - Complete player guide
- **REWARD_CONFIG.md** - Reward configuration reference
- **VERSION.md** - Version history and changelog
- **TROUBLESHOOTING.md** - Common issues and solutions
- **BUILD.md** - Building from source

---

## Building from Source

```bash
git clone <repository-url>
cd CharmedChars
./gradlew buildWithVersion
```

Output: `build/libs/CharmedChars-1.2.0.jar`

**Note**: You still need to obtain ItemsAdder separately - it cannot be built from source as it is proprietary.

---

## Version

Current Version: **1.2.0**

Run `/version` in-game or `./gradlew version` to display version information.

See `VERSION.md` for complete changelog and version history.

---

## Dependencies

### Build Dependencies
- Kotlin 2.2.21
- Gradle 8.x
- Paper API 1.21.10
- ItemsAdder API 3.6.3-beta-14 (compile-only)
- WorldGuard 7.0.14 (compile-only, optional)
- GriefPrevention 16.15.0 (bundled, optional)
- ProtocolLib 5.3.0 (compile-only, optional)

### Runtime Dependencies
- **ItemsAdder 3.6.3-beta-14+** (**REQUIRED** - proprietary plugin, obtain separately)
- WorldGuard 7.0.14+ (optional soft dependency)
- GriefPrevention 16.15.0+ (optional soft dependency)
- ProtocolLib 5.3.0+ (optional soft dependency)

---

## License

CharmedChars: **GNU LGPL v3** (see LICENSE.txt)

**Dependency Licenses:**
- **ItemsAdder**: Proprietary/Commercial (NOT open source) - **required dependency**
- Paper API: GPL v3
- WorldGuard: LGPL v3 (optional)
- GriefPrevention: GPL (optional)
- ProtocolLib: GPL v2+ (optional)
- Kotlin stdlib: Apache 2.0 (bundled)
- Kotlinx coroutines: Apache 2.0 (bundled)

**License Compliance Notice:**

CharmedChars itself is open source under LGPL v3, but it has a hard runtime dependency on ItemsAdder, which is proprietary software. This means:

1. CharmedChars source code is freely available
2. You can modify and redistribute CharmedChars under LGPL v3 terms
3. **However**, to actually run CharmedChars, you need ItemsAdder
4. ItemsAdder must be obtained separately and is subject to its own proprietary license
5. CharmedChars only uses ItemsAdder's public API (compileOnly dependency)
6. No ItemsAdder code is bundled or redistributed with CharmedChars

This arrangement is permitted under LGPL v3 as ItemsAdder is treated as a "System Library" or runtime-only dependency.

---

## Credits

**Author**: StephanosBad

**Built with:**
- Kotlin & Paper API
- ItemsAdder (proprietary plugin - required)
- Letter frequency data from Oxford Concise Dictionary (9th edition, 1995)

**Artwork:**
- Block textures by Gaia Temperini

**Development Assistance:**
- AI-assisted development by Claude (Anthropic)
- Features developed with AI assistance:
  - Nether structure number guessing game (database, game logic, explosions)
  - Git version tagging scripts (cross-platform automation)
  - Configurable reward system
  - LGPL v3 compliance implementation
  - Comprehensive documentation and KDoc comments
  - Code organization and architecture improvements
- All AI-contributed code includes Co-Authored-By attribution in git commits

---

## Support

For issues, feature requests, or questions:
- Check `TROUBLESHOOTING.md` for common issues
- Review documentation files for detailed guides
- File issues on the project repository

---

**⚠️ REMINDER**: This plugin requires ItemsAdder (proprietary/commercial) to function. CharmedChars is open source, but ItemsAdder must be purchased/downloaded separately from official sources.
