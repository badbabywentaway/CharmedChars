# CharmedChars Version History

## Version 1.3.2 - Oraxen Compatibility & Behavior Consistency

### Release Date
2026-01-13

### Overview
Compatibility release adding Oraxen support for number scoring and ensuring consistent block behavior across all three providers. Number blocks now work identically to letter blocks, with proper tool checking and item dropping in all dimensions.

### Changes

#### **Critical Fixes** 🐛

**Fixed Number Scoring on Oraxen (PlayerInteractEvent Support)**
- **Problem**: Number scoring only used BlockDamageEvent, but Oraxen works better with PlayerInteractEvent for custom NoteBlocks
- **Impact**: Number blocks didn't score on Oraxen servers while letter blocks worked fine
- **Root Cause**: Letter spelling used BOTH PlayerInteractEvent and BlockDamageEvent, but number scoring only used BlockDamageEvent
- **Fix**: Added PlayerInteractEvent handler to both number game listeners matching letter system
- **Changes**:
  - Added `onInteractNumberBlock()` method handling LEFT_CLICK_BLOCK (primary, Oraxen-compatible)
  - Kept `onNumberBlockDamage()` method for Nexo compatibility (Paper block updates disabled)
  - Extracted shared logic into `processNumberSequenceScoring()` method
- **Files**: `FortressNumberGameListener.kt`, `BastionNumberGameListener.kt`
- **Result**: Number scoring now works on all three providers (ItemsAdder, Oraxen, Nexo)

**Number Blocks Drop in Overworld/End**
- **Problem**: Number blocks only functioned in the Nether, causing confusion in other dimensions
- **Impact**: Players hitting 3-digit sequences in Overworld/End would see no feedback
- **Fix**: Added dimension check AFTER sequence detection to drop blocks as items with feedback message
- **Behavior**:
  - Overworld/End: "Number blocks can only be used in the Nether! Blocks dropped."
  - Nether (outside fortress/bastion): "Number sequence detected, but you're not in a fortress or bastion! Blocks dropped."
  - Nether (in fortress/bastion): Normal game mechanics (rewards/explosion/feedback)
- **Files**: `FortressNumberGameListener.kt:130-156`
- **Result**: Number blocks now recoverable like letter blocks when used outside intended location

**Consistent Block Breaking Behavior Across Providers**
- **Problem**: Number blocks always cancelled BlockBreakEvent, making them unbreakable with non-gold tools
- **Impact**: Players couldn't break misplaced number blocks with correct tools (pickaxe/axe) like they could with letter blocks
- **Fix**: Removed `event.isCancelled = true` to match letter block behavior
- **Behavior by Provider**:
  - **ItemsAdder**: Purple warning, prevents breaking with wrong tools (protected)
  - **Oraxen**: Block breaks according to tool whitelist (drops if correct tool, vanishes if wrong)
  - **Nexo**: Block breaks according to tool whitelist (drops if correct tool, vanishes if wrong)
- **Files**: `FortressNumberGameListener.kt:333-350`, `BastionNumberGameListener.kt:290-307`
- **Result**: Number blocks behave identically to letter blocks across all providers

#### **Code Quality** 📝

**Event Handler Architecture Matches Letter System**
- Both number listeners now use dual event handlers like letter spelling
- Shared scoring logic in `processNumberSequenceScoring()` method
- Consistent debug logging across all event handlers
- Provider abstraction layer used consistently

**Comprehensive Debug Logging**
- Added event fire detection logs to diagnose provider-specific issues
- Logs show which event triggered (PlayerInteractEvent vs BlockDamageEvent)
- Tool and block type logged at event entry points
- Sequence detection progress logged

### Impact

**Compatibility**
- **100% Backward Compatible**: Existing servers work without changes
- **No Database Changes**: Structure database format unchanged
- **No Config Changes**: Existing config.yml files remain valid
- **No Breaking Changes**: All commands and features unchanged

**Gameplay Improvements**
- Number scoring now works on Oraxen servers (previously broken)
- Number blocks recoverable in Overworld/End (no permanent loss)
- Consistent breaking behavior with correct tools (pickaxe/axe)
- Identical user experience between letter and number systems

### Files Modified

**Number Game Listeners**:
- `FortressNumberGameListener.kt` - PlayerInteractEvent support, dimension handling, break behavior
- `BastionNumberGameListener.kt` - PlayerInteractEvent support, dimension handling, break behavior

**Version & Deployment**:
- `gradle.properties` - Version bump to 1.3.2
- `DEPLOY_CLEAN_JAR.bat` - Updated version and release notes
- `README.md` - Updated version reference
- `VERSION.md` - This file

### Upgrade Notes

**From v1.3.1**:
1. Download CharmedChars-1.3.2.jar
2. Replace old JAR in `plugins/` folder
3. Restart server
4. Number scoring will now work on Oraxen
5. Number blocks can be broken with correct tools

**Oraxen Users**:
- Critical fix - number scoring now works identically to letter spelling
- Use PlayerInteractEvent (left-click) for immediate scoring
- Blocks drop as items in Overworld/End for recovery

**All Providers**:
- Number blocks can now be broken with correct tools per provider config
- Matches letter block behavior exactly

### Development Notes

**AI-Assisted Development**:
- All compatibility fixes developed with assistance from Claude (Anthropic)
- Event handler architecture improvements guided by AI analysis
- All AI contributions include Co-Authored-By attribution in git commits

---

## Version 1.3.1 - Critical Number Scoring Fix

### Release Date
2026-01-12

### Overview
Critical bug fix release resolving number game scoring issues with Nexo provider. Changes number scoring to execute on hit (BlockDamageEvent) instead of break, matching the letter spelling system. Fixes hardcoded namespace bug that prevented number recognition with non-charmedchars namespaces.

### Changes

#### **Bug Fixes** 🐛

**Fixed NoClassDefFoundError with Nexo Provider**
- **Problem**: Number game listeners directly called ItemsAdder API even when Nexo was installed
- **Impact**: Game crashed when hitting/breaking number blocks with Nexo: `java.lang.NoClassDefFoundError: dev/lone/itemsadder/api/CustomStack`
- **Fix**: Updated both listeners to use `CustomItemProviderManager` abstraction layer
- **Files**: `FortressNumberGameListener.kt`, `BastionNumberGameListener.kt`
- **Result**: Number games now work correctly with all three providers (ItemsAdder, Oraxen, Nexo)

**Fixed Hardcoded Namespace Bug in Number Recognition**
- **Problem**: Number block recognition hardcoded "charmedchars:" namespace check
- **Impact**: Number blocks wouldn't work with Oraxen (uses "oraxen:" namespace) or ItemsAdder (might use "itemsadder:")
- **Fix**: Changed to namespace-agnostic parsing like letter block system
- **Before**: `if (!namespacedId.startsWith("charmedchars:")) return null`
- **After**: `val parts = namespacedId.split(":")` (works with any namespace)
- **Files**: `FortressNumberGameListener.kt:456-475`, `BastionNumberGameListener.kt:320-345`
- **Result**: Number blocks now work identically to letter blocks across all providers

**CRITICAL: Number Scoring Now Happens on Hit (Not Break)**
- **Problem**: Number scoring used BlockBreakEvent (break) while letter scoring used BlockDamageEvent (hit)
- **Impact**: Scoring didn't trigger on hit, inconsistent behavior between systems
- **Fix**: Moved ALL scoring logic from BlockBreakEvent to BlockDamageEvent in both listeners
- **Changes**:
  - `BlockDamageEvent` (on hit): Now executes full scoring logic (rewards, explosions, feedback)
  - `BlockBreakEvent` (on break): Now only prevents breaking to avoid item duplication
- **Files**: `FortressNumberGameListener.kt`, `BastionNumberGameListener.kt`
- **Result**: Number scoring now works identically to letter spelling - immediate execution on hit

**Prevents Item Duplication from Breaking Number Blocks**
- **Problem**: Players could break number blocks to get items instead of scoring
- **Fix**: BlockBreakEvent now cancels if block is a number block
- **Result**: Number blocks can only be removed via proper scoring with valid tools

#### **Code Quality** 📝

**Consistent Behavior Between Letter and Number Systems**
- Both systems now use BlockDamageEvent for scoring
- Both systems use BlockBreakEvent to prevent normal breaking
- Both systems use provider abstraction layer consistently
- Namespace-agnostic block recognition in both systems

### Impact

**Compatibility**
- **100% Backward Compatible**: Existing servers work without changes
- **No Database Changes**: Structure database format unchanged
- **No Config Changes**: Existing config.yml files remain valid
- **No Breaking Changes**: All commands and features unchanged

**Gameplay Improvements**
- Number scoring now responds immediately on hit (like letters)
- Consistent experience between letter spelling and number guessing
- Works correctly with all three providers (ItemsAdder, Oraxen, Nexo)
- No more crashes with Nexo provider

### Files Modified

**Number Game Listeners**:
- `FortressNumberGameListener.kt` - Provider abstraction, namespace fix, scoring on hit
- `BastionNumberGameListener.kt` - Provider abstraction, namespace fix, scoring on hit

**Version & Deployment**:
- `gradle.properties` - Version bump to 1.3.1
- `DEPLOY_CLEAN_JAR.bat` - Updated version and release notes
- `README.md` - Added Nexo to all documentation, updated version
- `VERSION.md` - This file

### Upgrade Notes

**From v1.3.0**:
1. Download CharmedChars-1.3.1.jar
2. Replace old JAR in `plugins/` folder
3. Restart server
4. Number scoring will now work on hit (not break)
5. All three providers now fully supported

**Nexo Users**:
- Critical fix - this version resolves all Nexo compatibility issues
- Number games now work correctly without crashes
- No manual configuration changes needed

### Development Notes

**AI-Assisted Development**:
- All bug fixes developed with assistance from Claude (Anthropic)
- Provider abstraction consistency improvements guided by AI
- All AI contributions include Co-Authored-By attribution in git commits

---

## Version 1.3.0 - Nexo Integration & Fortune Enchantment Support

### Release Date
2026-01-01

### Overview
Major feature release adding full Nexo integration as a third custom item provider option, Fortune enchantment support for drop rate bonuses, copper tool compatibility for Oraxen, and universal 256x256 texture support for all providers.

### Changes

#### **New Features** ⭐

**Nexo Support - Third Custom Item Provider Option**
- **Full Nexo Integration**: Complete support for Nexo as an alternative to ItemsAdder or Oraxen
- **Paper Configuration Requirements**: Automatically generates Paper-compatible configuration files
- **Auto-Setup Command**: `/nexosetup` automatically configures all 128 custom items
- **Block Model Generation**: Generates proper Nexo block model configurations
- **Texture Management**: Automatically copies all 128 texture files to Nexo's resource pack folder
- **Recipe Generation**: Creates all pyrite crafting recipes in Nexo format
- **Force Flag**: `/nexosetup force` overwrites existing configurations
- **File**: `src/main/kotlin/org/stephanosbad/charmedChars/integration/NexoSetup.kt`
- **Command**: `src/main/kotlin/org/stephanosbad/charmedChars/commands/SetupNexoCommand.kt`
- **Provider**: `src/main/kotlin/org/stephanosbad/charmedChars/integration/NexoProvider.kt`

**Fortune Enchantment Support** 🎯
- **Critical Fix**: Fortune enchantment now correctly increases letter block drop rates
- **Drop Rate Scaling**: Fortune I/II/III now properly applies multipliers to drop chances
- **Compatibility**: Works alongside existing Looting enchantment bonuses
- **File**: `src/main/kotlin/org/stephanosbad/charmedChars/items/ItemManager.kt`

**Copper Tool Support for Oraxen** 🔧
- **Minecraft 1.21.9+ Support**: Added copper tools to Oraxen break whitelist
- **Tool Types**: All copper pickaxes and axes now work with letter blocks
- **Documentation**: Updated README and setup guides with copper tool information
- **Provider Behavior Differences**: Comprehensive documentation of ItemsAdder vs Oraxen vs Nexo behavior

**Universal 256x256 Texture System** 🎨
- **Cross-Provider Compatibility**: All textures downscaled to 256x256 for universal support
- **Oraxen 1.203+ Compatibility**: Meets Oraxen's texture resolution requirements
- **Consistent Experience**: Same visual quality across all three providers
- **Python Script**: `downscale_textures_for_oraxen.py` for automated texture conversion
- **Dual Texture Sets**: Maintains both 512x512 (ItemsAdder/Nexo) and 256x256 (Oraxen) versions

#### **Bug Fixes** 🐛

**Fixed Fortune Enchantment Detection**
- **Problem**: Fortune enchantment on tools was not being detected, so drop rates weren't increased
- **Impact**: Players using Fortune tools received standard drop rates instead of boosted rates
- **Fix**: Added proper Fortune enchantment detection in drop calculation logic
- **File**: `src/main/kotlin/org/stephanosbad/charmedChars/items/ItemManager.kt`
- **Result**: Fortune I/II/III now correctly apply their respective multipliers

**Fixed Oraxen Invisible Block Issue**
- **Problem**: Breaking Oraxen letter blocks could sometimes result in invisible blocks
- **Impact**: Players experienced visual glitches after breaking blocks
- **Fix**: Improved block break event handling for Oraxen provider
- **Note**: This commit was later reverted and fixed with a different approach

**Fixed Config Generation**
- **Problem**: Plugin wasn't generating complete default config on first run
- **Impact**: New installations had incomplete configuration files
- **Fix**: ConfigManager now properly copies full default config from resources
- **File**: `src/main/kotlin/org/stephanosbad/charmedChars/utility/ConfigManager.kt`
- **Result**: First-time setup now generates complete config.yml with all default values

#### **Documentation Updates** 📝

**Comprehensive Multi-Provider Documentation**
- **NEXO_SETUP.md**: NEW - Complete Nexo setup guide with installation instructions
- **README.md**: Updated with Nexo references and copper tool information
- **HANGAR_SHOWCASE.md**: Updated with three-provider support details
- **ORAXEN_SETUP.md**: Added copper tool support information
- **PLAY_INSTRUCTIONS.md**: Updated with Fortune enchantment information

**New Documentation Files**:
- `NEXO_SETUP.md` - Complete Nexo setup guide
- `DEPLOY_AND_TEST.md` - Development deployment workflow

### Impact

**Compatibility**
- **100% Backward Compatible**: Existing ItemsAdder and Oraxen servers work without changes
- **No Breaking Changes**: All existing commands, features, and APIs unchanged
- **No Database Changes**: Structure database format unchanged
- **No Config Changes**: Existing config.yml files remain valid

**Server Owner Benefits**
- **Three Provider Options**: Choose between ItemsAdder, Oraxen, or Nexo
- **Fortune Enchantment**: Players can now use Fortune tools for better drop rates
- **Copper Tools**: Minecraft 1.21.9+ servers can use new copper tools (Oraxen)
- **Consistent Textures**: All providers now use compatible texture resolutions

### Files Modified

**Core Integration Layer**:
- `NexoProvider.kt` - NEW: Nexo implementation
- `NexoSetup.kt` - NEW: Nexo setup command
- `SetupNexoCommand.kt` - NEW: Command handler
- `CustomItemProviderManager.kt` - Updated for Nexo
- `OraxenSetup.kt` - Copper tool support

**Gameplay Systems**:
- `ItemManager.kt` - Fortune enchantment support
- `BastionNumberGameListener.kt` - Enhanced validation
- `FortressNumberGameListener.kt` - Enhanced validation

**Configuration**:
- `ConfigManager.kt` - Improved config generation
- `ConfigDataHandler.kt` - Enhanced data handling
- `config.yml` - Updated default configuration

**Documentation**:
- `NEXO_SETUP.md` - NEW: Nexo setup guide
- `DEPLOY_AND_TEST.md` - NEW: Development workflow
- `VERSION.md` - This file

### Upgrade Notes

**From v1.2.0**:
1. Download CharmedChars-1.3.0.jar
2. Replace old JAR in `plugins/` folder
3. Restart server
4. Fortune enchantment will now work on tools

**Switching to Nexo**:
1. Remove existing provider plugin
2. Install Nexo plugin
3. Install CharmedChars-1.3.0.jar
4. Run `/nexosetup`
5. Run `/nexo reload`
6. Restart server

### Development Notes

**AI-Assisted Development**:
- Nexo integration developed with assistance from Claude (Anthropic)
- Fortune enchantment fix implemented with AI guidance
- All AI contributions include Co-Authored-By attribution in git commits

---

## Version 1.2.0 - Oraxen Compatibility

### Release Date
2025-12-14

### Overview
Major feature release adding full support for Oraxen as an alternative to ItemsAdder. Introduces a custom item provider abstraction layer that allows server owners to choose between two premium custom item providers: ItemsAdder or Oraxen (both require purchase, though Oraxen has public source code).

### Changes

#### **New Features** ⭐

**Oraxen Support - Alternative to ItemsAdder**
- **IMPORTANT**: Both ItemsAdder and Oraxen are premium plugins requiring purchase for server use
- **Custom Item Provider Abstraction Layer**: Complete abstraction separating core plugin logic from custom item provider implementation
- **Dual Provider Support**: Works with either ItemsAdder OR Oraxen (not both, not neither)
- **Automatic Provider Detection**: Plugin automatically detects which custom item provider is installed at runtime
- **Safety Checks**: Refuses to load if both providers are installed or neither is installed, with clear error messages
- **File**: `src/main/kotlin/org/stephanosbad/charmedChars/integration/CustomItemProvider.kt`

**New Command: /oraxensetup**
- **Automatic Oraxen Configuration**: One-command setup for Oraxen integration
- **Generates 128 Items**: Automatically creates all 123 letter/number blocks + 5 pyrite items
- **Block Model JSON Generation**: Generates proper Oraxen block model JSONs for all items
- **Texture Copying**: Automatically copies all 128 texture files to Oraxen's pack folder
- **Recipe Generation**: Creates all pyrite crafting recipes in Oraxen format
- **Force Flag**: `/oraxensetup force` overwrites existing configurations
- **File**: `src/main/kotlin/org/stephanosbad/charmedChars/integration/OraxenSetup.kt`

**Abstraction Layer Architecture**
- **CustomItemProvider Interface**: Unified interface for all custom item operations
  - `getCustomItem()` - Retrieve custom item by namespaced ID
  - `getNamespacedId()` - Get custom item ID from ItemStack
  - `getProviderName()` - Get provider name ("ItemsAdder" or "Oraxen")
- **CustomItemProviderManager**: Singleton managing provider lifecycle
  - Auto-detects installed provider during plugin initialization
  - Validates only one provider is present
  - Provides global access to provider instance
- **ItemsAdderProvider**: Implementation for ItemsAdder API
- **OraxenProvider**: Implementation for Oraxen API

**Technical Implementation Details**

Block Model Generation (Oraxen):
```kotlin
// Oraxen requires explicit block model JSONs
// CharmedChars auto-generates these during /oraxensetup
{
  "parent": "block/cube_all",
  "textures": {
    "all": "charmedchars:block/cyan/a"
  }
}
```

Provider Detection Logic:
```kotlin
// Checks for both providers at startup
val hasItemsAdder = Bukkit.getPluginManager().getPlugin("ItemsAdder") != null
val hasOraxen = Bukkit.getPluginManager().getPlugin("Oraxen") != null

// Validates exactly one provider
if (hasItemsAdder && hasOraxen) {
    error("Both ItemsAdder and Oraxen detected - install only ONE")
}
if (!hasItemsAdder && !hasOraxen) {
    error("No custom item provider found - install ItemsAdder OR Oraxen")
}
```

#### **Documentation Updates** 📝

**Comprehensive Documentation Overhaul**
- **ORAXEN_SETUP.md**: New complete setup guide for Oraxen users
  - Step-by-step installation instructions
  - Command reference and troubleshooting
  - Configuration examples
  - Comparison with ItemsAdder workflow

- **HANGAR_SHOWCASE.md**: Updated showcase documentation
  - Dual provider setup instructions
  - Clear choice between two premium providers (ItemsAdder and Oraxen)
  - Updated commands table with /oraxensetup
  - Abstraction layer technical details
  - Updated version history with v1.2.0 features

- **README.md**: Updated main documentation
  - Provider selection guidance
  - Installation steps for both providers
  - Updated technical details section

- **Test Documentation**: Updated test comments
  - WordValidationTest: Updated to reflect custom item provider abstraction
  - SequenceDetectionTest: Updated block identification documentation

**New Documentation Files**:
- `ORAXEN_SETUP.md` - Complete Oraxen setup guide
- Integration test suite documentation

### Impact

**Compatibility**
- **100% Backward Compatible**: Existing ItemsAdder servers work without changes
- **No Breaking Changes**: All existing commands, features, and APIs unchanged
- **No Database Changes**: Structure database format unchanged
- **No Config Changes**: config.yml format unchanged

**Server Owner Benefits**
- **Choice**: Server owners can choose between two premium custom item providers
- **Flexibility**: Both providers are premium but offer different features and pricing
- **Migration Path**: Can switch between providers by changing plugin + running setup command
- **Same Features**: All CharmedChars features work identically with both providers
- **Public Source**: Oraxen has public source code (though still requires purchase for use)

**Technical Quality**
- **Clean Abstraction**: Provider-specific code isolated in dedicated classes
- **Easy Maintenance**: Future provider support can be added without core changes
- **Comprehensive Tests**: New integration test suite for both providers
- **Robust Error Handling**: Clear error messages for misconfiguration

### Files Modified

**Core Integration Layer**:
- `src/main/kotlin/org/stephanosbad/charmedChars/integration/CustomItemProvider.kt` - NEW: Provider interface
- `src/main/kotlin/org/stephanosbad/charmedChars/integration/CustomItemProviderManager.kt` - NEW: Provider manager
- `src/main/kotlin/org/stephanosbad/charmedChars/integration/ItemsAdderProvider.kt` - NEW: ItemsAdder implementation
- `src/main/kotlin/org/stephanosbad/charmedChars/integration/OraxenProvider.kt` - NEW: Oraxen implementation
- `src/main/kotlin/org/stephanosbad/charmedChars/integration/OraxenSetup.kt` - NEW: Oraxen setup command
- `src/main/kotlin/org/stephanosbad/charmedChars/commands/OraxenSetupCommand.kt` - NEW: Command handler

**Plugin Initialization**:
- `src/main/kotlin/org/stephanosbad/charmedChars/CharmedChars.kt` - Provider detection and initialization

**Documentation**:
- `ORAXEN_SETUP.md` - NEW: Oraxen setup guide
- `HANGAR_SHOWCASE.md` - Updated with dual provider support
- `README.md` - Updated installation instructions
- `VERSION.md` - This file (added v1.2.0 release notes)
- `gradle.properties` - Version bump to 1.2.0
- `BUILD.md` - Updated JAR filename references
- `TROUBLESHOOTING.md` - Updated JAR filename references
- `DEPLOY_CLEAN_JAR.bat` - Updated deployment script
- `verify_jar.bat` - Updated JAR filename
- `verify_jar_correct.bat` - Updated JAR filename
- `cleanup_uppercase.bat` - Updated JAR filename

**Test Suite**:
- `src/test/kotlin/org/stephanosbad/charmedChars/integration/CustomItemProviderTest.kt` - NEW: Provider interface tests
- `src/test/kotlin/org/stephanosbad/charmedChars/integration/CustomItemProviderManagerTest.kt` - NEW: Manager tests
- `src/test/kotlin/org/stephanosbad/charmedChars/integration/OraxenSetupTest.kt` - NEW: Oraxen setup tests
- `src/test/kotlin/org/stephanosbad/charmedChars/items/WordValidationTest.kt` - Updated comments
- `src/test/kotlin/org/stephanosbad/charmedChars/listeners/SequenceDetectionTest.kt` - Updated comments

### Upgrade Notes

**From v1.1.5 (ItemsAdder Users)**:
1. **No Changes Required**: Drop-in replacement
2. Download CharmedChars-1.2.0.jar
3. Replace old JAR in `plugins/` folder
4. Restart server
5. Plugin will auto-detect ItemsAdder and work normally
6. All existing data, configs, and resource packs unchanged

**From v1.1.5 (Switching to Oraxen)**:
1. Remove ItemsAdder from `plugins/` folder
2. Install Oraxen plugin
3. Install CharmedChars-1.2.0.jar
4. Start server
5. Run `/oraxensetup` to configure Oraxen
6. Run `/oraxen reload all` to load items
7. Restart server for full effect
8. Players will receive new resource pack

**New Installations**:
1. Choose **ONE** custom item provider:
   - **ItemsAdder** (premium plugin): Purchase from SpigotMC
   - **Oraxen** (premium plugin with public source): Purchase from SpigotMC
   - **IMPORTANT**: Both are premium plugins requiring purchase for server use
2. Install chosen provider + CharmedChars
3. Run setup command:
   - ItemsAdder: `/iasetup` then `/iazip`
   - Oraxen: `/oraxensetup` then `/oraxen reload all`
4. Restart server

**Important Notes**:
- **Do NOT install both** ItemsAdder and Oraxen - plugin will refuse to load
- **Must install one** - plugin requires a custom item provider
- **Resource pack regeneration**: Switching providers requires new resource pack generation
- **No data loss**: Structure database and player data preserved during provider switch

### Known Issues

None currently reported.

### Development Notes

**Architecture Design**:
- Provider abstraction pattern separates concerns cleanly
- Each provider implementation encapsulates provider-specific API calls
- Manager pattern provides global access point while controlling lifecycle
- Interface-based design allows easy addition of future providers

**Testing Strategy**:
- Integration tests mock provider behavior
- Test both ItemsAdder and Oraxen code paths
- Verify provider detection logic
- Validate configuration generation

**AI-Assisted Development**:
- Oraxen compatibility feature developed with assistance from Claude (Anthropic)
- Custom item provider abstraction architecture designed with AI guidance
- All AI contributions include Co-Authored-By attribution in git commits
- Comprehensive documentation generated with AI assistance

---

## Version 1.1.5 - Bug Fix Release

### Release Date
2025-12-07

### Overview
Bug fix release addressing ItemsAdder setup failure and comprehensive documentation updates.

### Changes

#### **Bug Fixes** 🐛

**Fixed: ItemsAdder Data folder creation failure**
- **Problem**: `/iasetup` command would fail if ItemsAdder's `data` folder didn't exist
- **Impact**: Users had to manually create the folder before running setup
- **Fix**: Added automatic `data` folder creation in `ItemsAdderSetup.enableNamespace()`
- **File**: `src/main/kotlin/org/stephanosbad/charmedChars/integration/ItemsAdderSetup.kt:290-293`
- **Result**: Setup now works on first run without manual intervention

**Technical Details:**
```kotlin
// Added before writing items_packs.yml:
if (!itemsAdderDataFolder.exists()) {
    itemsAdderDataFolder.mkdirs()
}
```

#### **Documentation Updates** 📝

**Comprehensive documentation review and updates to accurately reflect all gameplay features:**

- **README.md**:
  - Added pyrite tools to "How to Play" section
  - Added minimum word lengths (3 same-color, 4 multi-color)
  - Added Nether Challenge section
  - Added missing commands (`/structurecode`, `/structuredb list`, `/structuredb purge`)
  - Updated Technical section with accurate counts (128 items: 123 blocks + 5 pyrite items)
  - Added SQLite database mention

- **HANGAR_SHOWCASE.md**:
  - Updated "Custom Blocks" to "Custom Blocks & Items"
  - Added 5 pyrite items to technical details
  - Clarified total: 128 custom items

- **QUICK_SETUP.md**:
  - Updated `/iasetup` description to mention pyrite.yml
  - Corrected texture count: 128 files (123 blocks + 5 pyrite items)
  - Added "Enables charmedchars namespace" step
  - Updated `/iazip` description to mention item models

- **PLAY_INSTRUCTIONS.md**:
  - Added comprehensive "Nether Structure Number Guessing Game" section (232 lines)
  - Covers gameplay mechanics, strategy tips, commands, rewards, FAQ
  - Updated "Getting Started Checklist" with correct minimum word sizes

### Impact
- **Setup Process**: Now fully automated - no manual folder creation needed
- **Documentation**: All files now accurately represent complete feature set
- **Compatibility**: 100% backward compatible with v1.1.4
- **No Database Changes**: All player data preserved
- **No Config Changes**: All settings remain the same

### Files Modified
- `ItemsAdderSetup.kt` - Bug fix (4 lines added)
- `VERSION.md` - This file
- `README.md` - Complete feature documentation
- `HANGAR_SHOWCASE.md` - Accurate item counts and features
- `QUICK_SETUP.md` - Correct setup step descriptions
- `PLAY_INSTRUCTIONS.md` - Added Nether game section, updated checklist

### Upgrade Notes
- **From v1.1.4**: Drop-in replacement, no changes needed
- **Setup Improvement**: `/iasetup` now creates necessary folders automatically
- **Documentation**: All gameplay features now properly documented
- **No Breaking Changes**: Full backward compatibility

---

## Version 1.1.4 - Code Cleanup & Refactoring

### Release Date
2025-12-01

### Overview
Internal code cleanup release removing dead code and improving code clarity. No functional changes or new features.

### Changes

#### **Dead Code Removal** 🧹
Removed unused functions, imports, and empty structures to improve maintainability.

**Removed Functions (6 total):**
- `ItemManager.getNoteblockNumber()` - Legacy note block method (replaced by getCustomVariation)
- `ItemManager.checkLateralBlocks()` - Refactored inline into letterBlockBreak method
- `StructureDatabase.deleteStructureById()` - Unused public API (deleteStructureByNumber used instead)
- `StructureData.getLocationKey()` - Unused utility function
- `StructureType.fromString()` - Kotlin stdlib valueOf() used instead
- `ItemManager` empty companion object - Leftover from refactoring

**Removed Imports:**
- `BlockColor.kt` - Unused `kotlin.random.Random` import

#### **Code Clarity Improvements** 📝
Renamed 23 unused interface parameters with underscore prefix for clarity.

**Affected Commands:**
- CharBlock, ReloadCommand, VersionCommand
- ItemsAdderStatusCommand, SetupItemsAdderCommand
- StructureCodeCommand, StructureDatabaseCommand

**Example:**
```kotlin
// Before:
override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>)

// After:
override fun onCommand(sender: CommandSender, _command: Command, _label: String, _args: Array<out String>)
```

### Impact
- **Lines Removed:** 113 lines
- **Lines Added:** 18 lines
- **Net Reduction:** 95 lines of code
- **Files Modified:** 12 files
- **Tests:** All 131 tests passing ✅
- **Build:** Successful ✅

### Upgrade Notes
- **From v1.1.3:** Drop-in replacement, no changes needed
- **Compatibility:** 100% backward compatible
- **No Database Changes:** All player data preserved
- **No Config Changes:** All settings remain the same

---

## Version 1.1.3 - Brass-Colored Pyrite Textures

### Release Date
2025-11-21

### Overview
Minor visual update improving the pyrite textures to better distinguish them from vanilla gold items.

### Changes

#### **Updated Pyrite Textures** 🎨
Pyrite items now feature a brass-like metal color instead of bright gold.

- **Color:** Warmer, coppery-golden brass tone (hue shifted to 88%)
- **Saturation:** Slightly reduced (95%) for a more subdued appearance
- **Visual Identity:** Better represents "fool's gold" (pyrite) vs real gold
- **Affected Items:**
  - pyrite_ingot.png - Brass-colored ingot
  - pyrite_pickaxe.png - Warmer metal head
  - pyrite_axe.png - Coppery-golden blade
  - pyrite_shovel.png - Brass-colored spade
  - pyrite_hoe.png - Warmer brass tone

### Technical Details
- Applied ImageMagick color transformation: `-modulate 100,95,88`
- Textures auto-copied by `/iasetup` command
- Resource pack regeneration required with `/iazip`

### Upgrade Notes
- **From v1.1.2:** Drop-in replacement, no config changes needed
- **Resource Pack:** Regenerate with `/iazip` after update to see new textures
- **Existing Items:** Already placed pyrite tools will show new textures automatically

---

## Version 1.1.2 - Pyrite (Fool's Gold) System & Gameplay Improvements

### Release Date
2025-11-20

### Overview
Major feature release introducing the Pyrite material system - an iron-tier alternative to gold tools with identical CharmedChars functionality. Includes word length validation rules to improve gameplay balance.

### New Features

#### **Pyrite (Fool's Gold) Material System** ⭐
A craftable alternative to gold tools with better durability but same CharmedChars functionality.

**Crafting:**
- Pyrite Ingot: 1 Iron Ingot + 1 Redstone (shapeless)
- Tools: Standard recipes using Pyrite Ingots + Sticks (same patterns as vanilla tools)

**Items:**
- `charmedchars:pyrite_ingot` - Crafting material
- `charmedchars:pyrite_pickaxe` - 250 durability, iron-tier stats
- `charmedchars:pyrite_axe` - 250 durability, iron-tier stats
- `charmedchars:pyrite_shovel` - 250 durability, iron-tier stats
- `charmedchars:pyrite_hoe` - 250 durability, iron-tier stats

**Functionality:**
- Works like gold for mining logs → letter block drops
- Works like gold for breaking letter blocks → word scoring
- Works like gold for breaking number sequences → fortress/bastion rewards
- Iron-tier durability (250 uses vs gold's 32)
- Iron-tier mining speed and enchantability

#### **Minimum Word Length Rules** ⭐
Prevents scoring of very short words to improve game balance.

- **Single-color words:** Minimum 3 letters (e.g., "CAT" in all cyan)
- **Multi-color words:** Minimum 4 letters (e.g., "CATS" in cyan+magenta)
- Clear feedback messages when words are too short
- Example: Breaking "IN" (2 letters) shows "Miss: single-color words must be at least 3 letters long"

#### **Tool Validation for Number Sequences** ⭐
Number sequences now require gold or pyrite tools to prevent unintended triggers.

- Only gold or pyrite tools can break number sequences in fortresses/bastions
- Prevents netherite, diamond, and other tools from accidentally triggering games
- Consistent with letter block and word scoring tool requirements

### Bug Fixes

#### **Fixed Pyrite Tool Detection**
- Changed from `displayName()` to ItemsAdder's `CustomStack.byItemStack()` API
- Pyrite tools now properly recognized for all CharmedChars features
- Added fallback to plain text display name serialization for compatibility

#### **Fixed ItemsAdder Block Breaking Warnings**
- Added pyrite tools to `break_tools_whitelist` for all 123 character blocks
- Eliminated false "cannot use tool to break item" messages
- Updated blocks.yml with `charmedchars:pyrite_axe` and `charmedchars:pyrite_pickaxe`

#### **Fixed Pyrite Recipe Format**
- Moved recipes from item-nested structure to global `recipes:` section
- Added `crafting_table:` parent key as required by ItemsAdder
- Fixed shapeless recipe format (changed from list to letter mapping)
- Removed durability from pyrite_ingot to prevent spurious repair recipes

### Configuration Changes

#### **ItemsAdder Integration**
- New file: `src/main/resources/itemsadder/pyrite.yml`
- Auto-copied by `/iasetup` command
- 5 items + 5 crafting recipes
- Texture files included in resource pack

#### **Build System**
- Added `processResources` task to substitute version in plugin.yml
- Version now correctly shows "1.1.2" instead of "${version}"

### Technical Details

**Tool Validation Logic (ItemManager.kt:223-252):**
```kotlin
private fun isValidTool(item: ItemStack): Boolean {
    // 1. Check vanilla gold tools
    if (item.type.name.lowercase().contains("gold")) return true

    // 2. Check pyrite tools via ItemsAdder API
    val customStack = CustomStack.byItemStack(item)
    if (customStack?.namespacedID?.lowercase()?.contains("pyrite") == true) {
        return true
    }

    // 3. Fallback to display name check
    // ...
}
```

**Word Length Validation (ItemManager.kt:477-486):**
```kotlin
val minimumLength = if (isSameColor) 3 else 4
if (wordLength < minimumLength) {
    e.player.sendMessage("Miss: $colorType words must be at least $minimumLength letters long")
    e.isCancelled = true
    return
}
```

**Pyrite Recipe Structure:**
```yaml
recipes:
  crafting_table:
    pyrite_ingot_recipe:
      enabled: true
      shapeless: true
      ingredients:
        I: IRON_INGOT
        R: REDSTONE
      result:
        item: charmedchars:pyrite_ingot
        amount: 1
```

### Files Changed
- `gradle.properties` - Version bump to 1.1.2
- `src/main/resources/itemsadder/pyrite.yml` - New pyrite item definitions and recipes
- `src/main/resources/itemsadder/blocks.yml` - Added pyrite tools to break_tools_whitelist (123 blocks)
- `src/main/kotlin/org/stephanosbad/charmedChars/items/ItemManager.kt` - Tool validation + word length rules
- `src/main/kotlin/org/stephanosbad/charmedChars/listeners/FortressNumberGameListener.kt` - Tool validation
- `src/main/kotlin/org/stephanosbad/charmedChars/listeners/BastionNumberGameListener.kt` - Tool validation
- `src/main/kotlin/org/stephanosbad/charmedChars/integration/ItemsAdderSetup.kt` - Pyrite texture copying
- `build.gradle.kts` - Added processResources for version substitution
- `DEPLOY_CLEAN_JAR.bat` - Version update
- `VERSION.md` - This file
- `PLAY_INSTRUCTIONS.md` - Added pyrite system documentation

### Upgrade Notes

**For Server Admins:**
1. Replace JAR with CharmedChars-1.1.2.jar
2. Run `/iasetup force` to regenerate ItemsAdder configs
3. Run `/iazip` to rebuild resource pack
4. Restart server completely (important for recipe cache)
5. Pyrite recipes will be available in crafting tables

**For Players:**
- New pyrite tools available via crafting (cheaper than gold!)
- Words shorter than 3-4 letters no longer score
- Number sequences require gold/pyrite tools (prevents accidents)

### Known Issues
None

---

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
- **Dictionary Validation**: ~173,500 English words from ENABLE word list (public domain)
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
