# CharmedChars Version History

## Version 1.1.1 - Critical Coordinate Bug Fix & Test Coverage

### Release Date
2025-11-19

### Overview
Critical bug fix release addressing a serious coordinate calculation bug affecting structures at negative coordinates in the Nether. Includes comprehensive unit test coverage to prevent future regressions.

### Bug Fixes

#### **Fixed Critical Negative Coordinate Calculation Bug** ⭐
- **Problem**: Java/Kotlin integer division truncates toward ZERO, not floor
  - Before: `-1 / 16 = 0` (INCORRECT!)
  - After: `Math.floorDiv(-1, 16) = -1` (CORRECT!)
- **Impact**: Structures at negative coordinates near zero (very common in Nether) were incorrectly identified
- **Fix**: Replaced `(boundingBox.minX / 16).toInt()` with `Math.floorDiv(boundingBox.minX.toInt(), 16)` in all listeners and commands
- **Example**: A bastion at block coordinates (-50, -600) was mapped to wrong chunk origin
  - Before: Chunk (0, 0) - WRONG!
  - After: Chunk (-4, -38) - CORRECT!

#### Files Fixed
- `FortressNumberGameListener.kt` - Origin calculation (lines 136-137)
- `BastionNumberGameListener.kt` - Origin calculation (lines 114-115)
- `StructureListener.kt` - Origin calculation (lines 117-118)
- `StructureCodeCommand.kt` - Origin calculation (2 locations: lines 106-107, 162-163)

### Testing & Quality Assurance

#### **Comprehensive Unit Test Suite Added**
- **SequenceDetectionTest** (279 lines) - Documents 3-digit sequence detection logic
  - Tests valid directions (4 cardinal, horizontal only)
  - Tests invalid sequences (diagonal, vertical, gaps)
  - Tests number formation (hundreds-tens-ones)
  - Tests ItemsAdder block identification

- **ListenerConflictTest** (307 lines) - Verifies listener priority fixes from v1.1.0
  - Documents fortress listener checking for bastion first
  - Documents bastion listener early return behavior
  - Verifies only fortress listener handles "not in structure" case

- **DiscoveryMessageTest** (337 lines) - Verifies discovery notification fixes from v1.1.0
  - Documents that discovery messages do not reveal assigned numbers
  - Tests structure tracking key consistency across chunks
  - Tests prevention of message spam when moving within same structure

- **CoordinateCalculationTest** - Updated with Math.floorDiv expectations
  - All 12+ test cases updated to reflect proper floor division
  - Comprehensive documentation of negative coordinate handling
  - Tests edge cases including Int.MIN_VALUE and Int.MAX_VALUE

- **StructureDatabaseTest** - 26 existing tests (created in v1.1.0-dev)
  - Database CRUD operations
  - Unique number generation
  - Multi-chunk structure handling

#### **Test Coverage Infrastructure**
- Added JaCoCo 0.8.12 for test coverage reporting
- Configured 80% minimum coverage threshold
- Fixed Java 23 compatibility with updated JaCoCo version
- All 41+ tests passing

### Technical Details

**Coordinate Conversion Formula (OLD - INCORRECT):**
```kotlin
val originChunkX = (boundingBox.minX / 16).toInt()
val originChunkZ = (boundingBox.minZ / 16).toInt()
```

**Coordinate Conversion Formula (NEW - CORRECT):**
```kotlin
val originChunkX = Math.floorDiv(boundingBox.minX.toInt(), 16)
val originChunkZ = Math.floorDiv(boundingBox.minZ.toInt(), 16)
```

**Why This Matters:**
Minecraft's Nether commonly generates structures at negative coordinates. The old truncating division would incorrectly map:
- Blocks -1 to -15 → Chunk 0 (should be Chunk -1)
- Blocks -16 to -31 → Chunk -1 (should be Chunk -2)
- And so on...

This caused different structures to share the same database entry or the same structure to have multiple entries.

### Development Notes
- Bug discovered through comprehensive unit testing
- All fixes developed with assistance from Claude (Anthropic AI)
- Git commits include Co-Authored-By attribution for AI contributions
- Test-driven approach prevents future regressions

### Upgrade Notes
- **Highly Recommended**: This fix corrects structure identification in the Nether
- Existing databases may contain incorrect entries for structures at negative coordinates
- Consider purging and rediscovering Nether structures: `/structuredb purge <world>`
- No breaking changes to API or configuration

---

## Version 1.1.0 - Structure Number Game Bug Fixes

### Release Date
2025-11-19

### Overview
Critical bug fix release addressing multiple issues with the Nether structure number guessing game introduced in 1.0.0. Fixes problems with multi-chunk structure detection, listener conflicts, and message spam.

### Bug Fixes

#### **Fixed Structure Discovery Message Spam**
- Discovery announcements no longer repeat when moving between chunks within the same structure
- Changed player tracking from chunk-specific to structure-type-only
- Players now see "New Structure Discovered" only once per structure instance

#### **Fixed Listener Conflict Causing Wrong Messages**
- Resolved issue where FortressNumberGameListener would fire in bastions
- Players in bastions no longer see "you're not in a fortress!" message
- Each listener now checks for the OTHER structure type first and returns early
- Only fortress listener handles "not in any structure" message

#### **Fixed Multi-Chunk Structures Getting Separate Numbers** ⭐
- **Critical Fix**: Structures spanning multiple chunks now share ONE database entry
- Uses structure's bounding box origin coordinates instead of player's current chunk
- All chunks within the same fortress/bastion now use the same 3-digit number
- Prevents database pollution with duplicate entries for the same physical structure
- Implementation: Convert block coordinates to chunk via `(boundingBox.minX / 16).toInt()`

#### **Fixed /structurecode Command Not Finding Structures**
- Command now uses structure origin coordinates matching database storage
- Works correctly from any chunk within a fortress or bastion
- Updated display to show "Origin:" instead of "Location:" for clarity

### Technical Details

All fixes use structure bounding box origin as the unique identifier:
```kotlin
val boundingBox = structure.boundingBox
val originChunkX = (boundingBox.minX / 16).toInt()
val originChunkZ = (boundingBox.minZ / 16).toInt()
```

### Files Modified
- `BastionNumberGameListener.kt` - Origin-based lookup, listener priority
- `FortressNumberGameListener.kt` - Origin-based lookup, listener priority
- `StructureListener.kt` - Origin-based tracking and database queries
- `StructureCodeCommand.kt` - Origin-based database queries

### Database Compatibility
- Existing structure databases will work but may contain duplicate entries
- Consider purging old entries: `/structuredb purge <all|fortress|bastion>`
- New structures will be tracked correctly using origin coordinates

### Commands
| Command | Description | Permission |
|---------|-------------|------------|
| `/structurecode` | View structure's 3-digit code | `charmedchars.blocks` |
| `/structuredb list [world]` | List all tracked structures | `charmedchars.blocks` |
| `/structuredb purge <all\|world\|fortress\|bastion>` | Remove structure entries | `charmedchars.blocks` |

### Development Notes
- All bug fixes developed with assistance from Claude (Anthropic AI)
- Git commits include Co-Authored-By attribution for AI contributions
- Comprehensive testing of multi-chunk structure behavior

---

## Version 1.0.0 - Initial Release

### Release Date
2025-11-17

### Overview
Initial release of CharmedChars - A word-forming puzzle game for Minecraft where players collect letter blocks from logs, arrange them into words, and earn rewards based on word scores.

---

## Recent Changes

### Latest (Development Build)

#### New Features
- **Nether Structure Number Guessing Game** - Added mini-game for Bastion Remnants and Nether Fortresses
  - Each structure assigned unique 3-digit number (100-999)
  - Players break number block sequences to guess the structure's code
  - Correct guess: Configurable rewards (default: 12 blaze rods for fortress, 16 ender pearls for bastion)
  - Wrong guess (too high): Bed-like explosion (power 5.0)
  - Wrong guess (too low): Blocks drop as items (recoverable)
  - Sequence outside structure: Blocks drop as items with warning
  - One-time rewards per structure with database tracking
  - SQLite database with Exposed ORM for persistent tracking
  - Structure discovery notifications when entering for first time

- **Git Version Tagging Scripts** - Cross-platform scripts for semantic versioning
  - Shell script (tag-version.sh) for Linux/Mac
  - Batch script (tag-version.bat) for Windows
  - Auto-increment version support (--major, --minor, --patch)
  - Integration with gradle.properties
  - Annotated and lightweight tag support

- **Configurable Structure Rewards** - Server admins can customize number game rewards
  - Configure material type and quantity in config.yml
  - Supports any valid Minecraft material
  - Separate configs for fortress and bastion rewards

#### Bug Fixes
- **Fixed letter block item drop after scoring** - Cancelled BlockBreakEvent to prevent the first letter from dropping as an item when a word is scored
- **Fixed color randomization** - Replaced Math.random() with Kotlin's .random() to ensure all three colors (cyan, magenta, yellow) drop with equal probability
- **Fixed hardcoded drop rates** - Updated ItemManager to read drop rates from config instead of using hardcoded values

#### Code Quality
- **Comprehensive KDoc documentation** - Added detailed documentation to all classes
- **LGPL v3 license compliance** - Added proper license headers to all source files
- **Removed diagnostic logging** - Cleaned up verbose console logging during gameplay to reduce spam
- **Deleted diagnostic files** - Removed 11 obsolete diagnostic/troubleshooting files and scripts
- **Better randomization** - Using Kotlin's idiomatic random functions instead of Java's Math.random()

#### Development Notes
- Number guessing game system developed with assistance from Claude (Anthropic AI)
- Database architecture, game logic, and explosion mechanics implemented using AI-assisted development
- Cross-platform version tagging scripts created with AI assistance
- All git commits include Co-Authored-By attribution for AI contributions

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

**⚠️ IMPORTANT - ItemsAdder Licensing Notice:**

ItemsAdder is a **proprietary/commercial plugin** and is **NOT included** with CharmedChars. You must:
- Purchase and download ItemsAdder separately from [SpigotMC](https://www.spigotmc.org/resources/itemsadder.73355/) or the official source
- Comply with ItemsAdder's own license terms
- ItemsAdder is required for CharmedChars to function - it is a runtime dependency only

CharmedChars itself is open source, but it requires ItemsAdder (a proprietary plugin) to operate. CharmedChars only interfaces with ItemsAdder's public API and does not bundle or redistribute any ItemsAdder code.

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
**Artwork**: Block textures by Gaia Temperini
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

*Last Updated: 2025-11-19*
