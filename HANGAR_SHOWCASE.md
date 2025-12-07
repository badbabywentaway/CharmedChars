# CharmedChars - Word Puzzle Adventure for Minecraft

**Transform your Minecraft world into a word-forming puzzle game!** Collect magical letter blocks from logs, spell words for rewards, and conquer the Nether's number guessing challenge.

---

## What is CharmedChars?

CharmedChars is a unique word-puzzle plugin that adds a whole new dimension to Minecraft gameplay. Mine logs with gold or pyrite tools to collect colorful letter blocks, arrange them into words, and earn valuable rewards based on your vocabulary skills and strategic color matching.

Perfect for:
- **Educational servers** teaching spelling and vocabulary
- **Survival servers** adding unique progression mechanics
- **Adventure maps** with puzzle elements
- **Players** who love word games and Scrabble-style scoring

---

## Core Features

### Letter Block Collection System
- Mine any wood logs with **gold or pyrite tools** to collect random letter blocks
- **Three vibrant colors**: Cyan, Magenta, Yellow
- **Drop rates**: 6% base, up to 20% with Looting III enchantment
- **Frequency-based distribution**: Common letters (E, A, R) drop more often than rare ones (Q, X, Z)
- **Custom 512x512 textures** with ItemsAdder integration

### Word Formation & Scoring
- Place blocks in **straight lines** (horizontal or vertical)
- Break any letter with a gold/pyrite tool to score
- **Dictionary validation**: ~100,000+ English words
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

---

## Installation

### Requirements
- **Minecraft**: 1.21.10+
- **Server**: Paper or Paper-based (Purpur, Pufferfish, etc.)
- **Java**: 21+
- **ItemsAdder**: 3.6.3-beta-14+ (**required dependency** - sold separately)

### Optional Dependencies
- WorldGuard 7.0.14+ (region protection)
- GriefPrevention 16.15.0+ (claim protection)
- ProtocolLib 5.3.0+ (advanced features)

### Setup Steps
1. Purchase and install **ItemsAdder** (proprietary plugin - not included)
2. Download CharmedChars JAR from [GitHub Releases](https://github.com/badbabywentaway/CharmedChars/releases)
3. Place both plugins in `plugins/` folder
4. Start server to generate configs
5. Run `/iasetup` to auto-configure ItemsAdder
6. Run `/iazip` to generate resource pack
7. Restart server completely
8. Players automatically receive resource pack on join

**Detailed guide**: [QUICK_SETUP.md](https://github.com/badbabywentaway/CharmedChars/blob/master/QUICK_SETUP.md)

---

## Commands & Permissions

| Command | Description | Permission |
|---------|-------------|------------|
| `/charblock <player> <color> <text>` | Give letter blocks to a player | `charmedchars.blocks` |
| `/version` | Display plugin version | None |
| `/structurecode` | View structure's secret number | `charmedchars.blocks` |
| `/structuredb list [world]` | List all tracked structures | `charmedchars.blocks` |
| `/structuredb purge <type>` | Reset structure entries | `charmedchars.blocks` |
| `/iastatus` | Check ItemsAdder integration | `charmedchars.admin` |
| `/iasetup [force]` | Auto-setup ItemsAdder config | `charmedchars.admin` |
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
- **[Quick Setup Guide](https://github.com/badbabywentaway/CharmedChars/blob/master/QUICK_SETUP.md)** - Installation walkthrough
- **[Reward Configuration](https://github.com/badbabywentaway/CharmedChars/blob/master/REWARD_CONFIG.md)** - Customizing rewards
- **[Troubleshooting](https://github.com/badbabywentaway/CharmedChars/blob/master/TROUBLESHOOTING.md)** - Common issues

### For Developers
- **[Build Instructions](https://github.com/badbabywentaway/CharmedChars/blob/master/BUILD.md)** - Building from source
- **[Version History](https://github.com/badbabywentaway/CharmedChars/blob/master/VERSION.md)** - Complete changelog

---

## Technical Details

### Custom Blocks & Items
- **123 custom blocks**: 26 letters × 3 colors + 10 numbers × 3 colors + 4 operators × 3 colors
- **5 pyrite items**: Ingot, Pickaxe, Axe, Shovel, Hoe (craftable iron-tier tools)
- **Total**: 128 custom items with ItemsAdder integration
- **512x512 custom textures** by Gaia Temperini
- **Auto-setup commands** for easy ItemsAdder configuration

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

**Current Version**: 1.1.5

**Latest Updates**:
- **v1.1.5**: Bug fix - automatic Data folder creation, comprehensive documentation updates
- **v1.1.4**: Code cleanup and refactoring (95 lines removed)
- **v1.1.3**: Brass-colored pyrite textures
- **v1.1.2**: Pyrite system, word length validation, tool validation for number sequences
- **v1.1.1**: Critical coordinate bug fixes, comprehensive test coverage
- **v1.1.0**: Multi-chunk structure fixes, listener conflict resolution
- **v1.0.0**: Initial release with word scoring and Nether number game

**[Full Changelog](https://github.com/badbabywentaway/CharmedChars/blob/master/VERSION.md)**

---

## License & Dependencies

### CharmedChars
- **License**: GNU LGPL v3
- **Source Code**: Open source on GitHub
- **Commercial Use**: Permitted under LGPL v3 terms

### Required Dependency - ItemsAdder
**CRITICAL NOTICE**: CharmedChars requires **ItemsAdder**, which is a **proprietary/commercial plugin**.

- ItemsAdder is **NOT included** with CharmedChars
- ItemsAdder is **NOT open source** - it is a paid premium plugin
- You **MUST purchase ItemsAdder separately** from [SpigotMC](https://www.spigotmc.org/resources/itemsadder.73355/)
- CharmedChars only uses ItemsAdder's public API (compileOnly dependency)
- No ItemsAdder code is bundled or redistributed

**Without ItemsAdder, this plugin will not function.**

---

## Credits

**Author**: StephanosBad

**Built With**:
- Kotlin 2.2.21 & Paper API 1.21.10
- ItemsAdder API 3.6.3-beta-14
- Exposed ORM (SQLite)

**Artwork**:
- Block textures by Gaia Temperini
- Letter frequencies based on Oxford Concise Dictionary (9th edition, 1995)

**Development**:
- AI-assisted development by Claude (Anthropic)
- Features include: Nether number game, pyrite system, reward formulas, comprehensive documentation
- All AI contributions include Co-Authored-By attribution in git commits

---

## Links

- **GitHub Repository**: https://github.com/badbabywentaway/CharmedChars
- **Download (Releases)**: https://github.com/badbabywentaway/CharmedChars/releases
- **How to Play**: https://github.com/badbabywentaway/CharmedChars/blob/master/PLAY_INSTRUCTIONS.md
- **Setup Guide**: https://github.com/badbabywentaway/CharmedChars/blob/master/QUICK_SETUP.md
- **Issue Tracker**: https://github.com/badbabywentaway/CharmedChars/issues

---

## Support

For help, feature requests, or bug reports:
1. Check the **[Troubleshooting Guide](https://github.com/badbabywentaway/CharmedChars/blob/master/TROUBLESHOOTING.md)**
2. Review **[Play Instructions](https://github.com/badbabywentaway/CharmedChars/blob/master/PLAY_INSTRUCTIONS.md)** and other documentation
3. File an issue on **[GitHub Issues](https://github.com/badbabywentaway/CharmedChars/issues)**

---

**Ready to add word-forming adventure to your Minecraft server? Download CharmedChars today!**
