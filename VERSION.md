# CharmedChars Version History

## Version 1.0.0 - Initial Release

### Release Date
TBD

### Overview
Initial release of CharmedChars - A word-forming puzzle game for Minecraft where players collect letter blocks from logs, arrange them into words, and earn rewards based on word scores.

---

## Recent Changes

### Latest (Development Build)

#### Bug Fixes
- **Fixed letter block item drop after scoring** - Cancelled BlockBreakEvent to prevent the first letter from dropping as an item when a word is scored
- **Fixed color randomization** - Replaced Math.random() with Kotlin's .random() to ensure all three colors (cyan, magenta, yellow) drop with equal probability
- **Fixed hardcoded drop rates** - Updated ItemManager to read drop rates from config instead of using hardcoded values

#### Features
- **Doubled drop rates** - Increased base drop rate from 3% to 6%, with Looting levels now giving 10%, 16%, and 20% drop chances
- **Comprehensive documentation** - Added PLAY_INSTRUCTIONS.md and REWARD_CONFIG.md with detailed gameplay and configuration guides
- **Config-driven drop rates** - Drop chances now configurable via config.yml with looting multipliers

#### Code Quality
- **Removed diagnostic logging** - Cleaned up verbose console logging during gameplay to reduce spam
- **Deleted diagnostic files** - Removed 11 obsolete diagnostic/troubleshooting files and scripts
- **Better randomization** - Using Kotlin's idiomatic random functions instead of Java's Math.random()

---

## Version Numbering Scheme

CharmedChars follows [Semantic Versioning](https://semver.org/):

- **MAJOR.MINOR.PATCH** format (e.g., 1.0.0)
  - **MAJOR**: Incompatible API changes or major feature overhauls
  - **MINOR**: New features added in a backward-compatible manner
  - **PATCH**: Backward-compatible bug fixes

### Development Builds
- Development builds may include `-SNAPSHOT` suffix (e.g., 1.0.0-SNAPSHOT)
- Pre-release versions may use `-beta`, `-alpha`, or `-rc` suffixes

---

## System Requirements

### Minecraft Server
- **Minecraft Version**: 1.21.10+
- **Server Type**: Paper or Paper-based (Purpur, Pufferfish, etc.)
- **Java Version**: Java 21+

### Required Dependencies
- **ItemsAdder**: 3.6.3-beta-14 or higher

### Optional Dependencies
**The plugin works perfectly without these! They are soft dependencies only.**

- **WorldGuard**: 7.0.14+ (for region protection - can be disabled in config)
- **GriefPrevention**: 16.15.0+ (for claim protection - can be disabled in config)
- **ProtocolLib**: 5.3.0+ (for advanced features)

Note: Protection plugins are completely optional. If not installed, the plugin functions normally. If installed, they can be enabled/disabled via the `protection` section in config.yml.

---

## Key Features

### Gameplay
- **Letter Block Collection**: Mine wood logs with gold tools to get random letter blocks
- **Word Formation**: Place blocks in straight lines to form words
- **Dictionary Validation**: ~100,000+ English words from Oxford Dictionary
- **Scoring System**: Frequency-based scoring with color bonuses (3x multiplier for same-color words)
- **Configurable Rewards**: Drop items based on word scores with customizable thresholds

### Technical
- **ItemsAdder Integration**: Custom blocks with 512x512 textures
- **Three Block Colors**: Cyan, Magenta, Yellow
- **26 Letters + Numbers + Operators**: Full alphabet plus 0-9 and +, -, *, /
- **Optional Protection Support**: Optional integration with WorldGuard and GriefPrevention (soft dependencies)
- **Configurable Drop Rates**: Adjustable via config with Looting enchantment support

### Administration
- **Easy Setup**: Auto-configuration commands for ItemsAdder
- **Flexible Rewards**: Multiple reward tiers with configurable formulas
- **Resource Pack Hosting**: Built-in HTTP server for resource pack delivery
- **Comprehensive Config**: All gameplay values configurable

---

## Installation

1. Download CharmedChars-1.0.0.jar
2. Place in `plugins/` folder
3. Install ItemsAdder (required dependency)
4. **(Optional)** Install WorldGuard and/or GriefPrevention for protection support
5. Start server to generate config
6. Run `/iasetup` to auto-configure ItemsAdder
7. Run `/iazip` to generate resource pack
8. Restart server
9. Players automatically receive resource pack on join

**Note**: The plugin works without WorldGuard or GriefPrevention. Protection integrations can be enabled/disabled in config.yml under the `protection` section.

See `QUICK_SETUP.md` for detailed installation instructions.

---

## Configuration

See `REWARD_CONFIG.md` for complete configuration documentation.

### Quick Config Examples

**Drop Rates** (config.yml):
```yaml
letter-blocks:
  drop-chance: 0.06  # 6% base
  looting-multipliers:
    1: 1.67  # 10% with Looting I
    2: 2.67  # 16% with Looting II
    3: 3.33  # 20% with Looting III
```

**Rewards** (config.yml):
```yaml
Drop:
  - materialName: "IRON_INGOT"
    minimumRewardCount: 1.0
    multiplier: 0.01
    minimumThreshold: 100.0
    maximumRewardCap: 20.0
```

---

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/charblock <player> <color> <text>` | Give letter blocks to a player | `charmedchars.blocks` |
| `/reload` | Reload plugin configuration | `charmedchars.admin` |
| `/iastatus` | Check ItemsAdder integration status | `charmedchars.admin` |
| `/iasetup [force]` | Auto-setup ItemsAdder configuration | `charmedchars.admin` |

---

## Known Issues

None currently reported.

---

## Support & Documentation

- **Play Instructions**: See `PLAY_INSTRUCTIONS.md`
- **Reward Configuration**: See `REWARD_CONFIG.md`
- **Setup Guide**: See `QUICK_SETUP.md`
- **Troubleshooting**: See `TROUBLESHOOTING.md`
- **Build Instructions**: See `BUILD.md`

---

## Credits

**Author**: StephanosBad
**Built with**: Kotlin, Paper API, ItemsAdder
**Letter Frequencies**: Based on Oxford Concise Dictionary (9th edition, 1995)

---

## License

Proprietary - All rights reserved

---

## Changelog Format

Each version entry includes:
- **Release Date**: When the version was published
- **Bug Fixes**: Issues resolved in this version
- **Features**: New functionality added
- **Breaking Changes**: Incompatible changes requiring migration
- **Deprecations**: Features marked for removal in future versions
- **Internal**: Code quality improvements not affecting gameplay

---

*Last Updated: 2025-11-17*
