# CharmedChars

A word-forming puzzle game for Minecraft where players collect letter blocks from logs, arrange them into words, and earn rewards based on word scores.

## ⚠️ Important Licensing Information

### CharmedChars License
CharmedChars is **open source** software licensed under **GNU LGPL v3**.

### Required Dependency - ItemsAdder
**CRITICAL NOTICE**: CharmedChars requires **ItemsAdder**, which is a **proprietary/commercial plugin**.

- **ItemsAdder is NOT included** with CharmedChars
- **ItemsAdder is NOT open source** - it is a paid premium plugin
- You **MUST purchase/download ItemsAdder separately** from:
  - [SpigotMC](https://www.spigotmc.org/resources/itemsadder.73355/)
  - Or other official sources
- You **MUST comply** with ItemsAdder's own license terms
- ItemsAdder is a **runtime dependency only** - CharmedChars interfaces with its public API but does not bundle or redistribute any ItemsAdder code

**Without ItemsAdder, this plugin will not function.**

---

## Quick Start

### Requirements

**Server Software:**
- Minecraft 1.21.10+
- Paper or Paper-based server (Purpur, Pufferfish, etc.)
- Java 21+

**Required Plugin:**
- ItemsAdder 3.6.3-beta-14 or higher (**proprietary - purchase separately**)

**Optional Plugins:**
- WorldGuard 7.0.14+ (for region protection)
- GriefPrevention 16.15.0+ (for claim protection)
- ProtocolLib 5.3.0+ (for advanced features)

### Installation

1. **Purchase and install ItemsAdder** (required proprietary dependency)
2. Download CharmedChars-1.0.0.jar
3. Place both plugins in `plugins/` folder
4. Start server to generate configs
5. Run `/iasetup` to auto-configure ItemsAdder
6. Run `/iazip` to generate resource pack
7. Restart server
8. Players automatically receive resource pack on join

See `QUICK_SETUP.md` for detailed installation instructions.

---

## How to Play

### Collecting Letter Blocks
- Mine any wood logs with **gold tools**
- Drop chance: 6% (10%/16%/20% with Looting I/II/III)
- Receive random colored letter blocks (Cyan, Magenta, Yellow)

### Forming Words
- Place letter blocks in **straight lines** (horizontal or vertical)
- Break any letter in the word with a gold tool
- Word is validated against ~100,000+ English word dictionary

### Scoring
- Each letter has a frequency-based score
- **Color bonus**: All same-color blocks = **3x multiplier**
- Earn configurable rewards based on score

See `PLAY_INSTRUCTIONS.md` for complete gameplay guide.

---

## Features

### Gameplay
- Letter block collection from wood logs with gold tools
- Word formation with straight-line placement rules
- Dictionary validation (~100,000+ words)
- Frequency-based scoring system
- Color bonus multiplier (3x for same-color words)
- Configurable item rewards

### Technical
- ItemsAdder integration for custom blocks
- 123 custom blocks (26 letters × 3 colors + numbers + operators)
- 512x512 custom textures
- Optional WorldGuard/GriefPrevention protection support
- Configurable drop rates and Looting enchantment scaling

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

Output: `build/libs/CharmedChars-1.0.0.jar`

**Note**: You still need to obtain ItemsAdder separately - it cannot be built from source as it is proprietary.

---

## Version

Current Version: **1.0.0**

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
