# CharmedChars v1.4.0 - Glassing Beds Feature

**Release Date**: January 16, 2026

## 🎉 What's New

### Glassing Beds System
Transform lava into glass using bed explosions in the Nether and End! This major new feature adds strategic resource gathering and dimension control to enhance Nether gameplay.

**Key Features**:
- ✅ Bed explosions convert lava to glass within 5-block radius
- ✅ Y-level restriction (max Y=28) prevents lava ocean abuse
- ✅ Optional feature - disabled by default for server choice
- ✅ Admin control via `/glassingbeds enable/disable/status` command

### Operator Activation System
Players must "unlock" glassing beds each time they enter the Nether by hitting a sequence of 4 operator blocks.

**How it Works**:
1. Enter Nether
2. Place 4 operator blocks (+, -, ×, ÷) in a straight line
3. All must be same color (cyan, magenta, or yellow)
4. Any order works: "+−×÷" or "÷×−+" both valid
5. Hit the sequence with gold or pyrite tool
6. Message: "✦ Glassing Beds ACTIVATED! ✦"
7. Now beds will convert lava to glass!
8. Activation resets when you leave and re-enter Nether

**Visual Feedback**:
- ✅ Success: Green bordered activation message
- ❌ Failure: Red error messages explaining why
- 🔄 Session reset: Activates automatically on Nether re-entry

## 📦 Download

**JAR File**: `CharmedChars-1.4.0.jar` (attached below)

**Requirements**:
- Minecraft 1.21.10+
- Paper or Paper-based server
- Java 21+
- **One of**: ItemsAdder 3.6.3-beta-14+ OR Oraxen 1.181.0+ OR Nexo 0.1.0+

## 🚀 Installation

### New Installations
1. Download CharmedChars-1.4.0.jar
2. Install one custom item provider (ItemsAdder/Oraxen/Nexo)
3. Place both JARs in `plugins/` folder
4. Start server
5. Run setup command (`/iasetup`, `/oraxensetup`, or `/nexosetup`)
6. Restart server
7. Optionally enable Glassing Beds: `/glassingbeds enable`

### Upgrading from v1.3.2
1. Stop server
2. Replace old JAR with CharmedChars-1.4.0.jar
3. Start server
4. **No config changes needed** - feature is disabled by default
5. Optionally enable: `/glassingbeds enable`

**Note**: Glassing Beds is **disabled by default**. Your existing gameplay is unchanged unless you explicitly enable it.

## ⚙️ Configuration

### Enable/Disable Feature
```bash
# Enable feature
/glassingbeds enable

# Disable feature
/glassingbeds disable

# Check status
/glassingbeds status
```

### Config File (config.yml)
```yaml
# Glassing Beds Feature
glassing-beds:
  # Enable/disable the glassing beds feature
  enabled: false

  # Maximum Y-level for lava-to-glass conversion
  # Only lava at or below this Y-level will be converted
  # Recommended: 28 (allows underground lava but not ocean surface)
  max-y: 28
```

## 🎮 Gameplay Examples

### Example 1: Basic Usage
```
1. Admin runs: /glassingbeds enable
2. Player enters Nether via portal
3. Place operator blocks: cyan+ cyan− cyan× cyan÷
4. Hit sequence with gold pickaxe
5. Message: "✦ Glassing Beds ACTIVATED! ✦"
6. Place bed near lava lake
7. Right-click bed → BOOM! Lava becomes glass
```

### Example 2: Session Reset
```
1. Activate glassing beds (as above)
2. Use beds to convert lava successfully
3. Exit Nether through portal
4. Re-enter Nether
5. Try to use bed → lava doesn't convert
6. Message: "Glassing beds not activated for this Nether visit!"
7. Re-do operator sequence to re-activate
```

### Example 3: Validation Failures
```
# Wrong count
Place only 3 operators
→ "Need exactly 4 operator blocks (+−×÷)"

# Duplicate operators
Place: + + × ÷
→ "All 4 operators must be different!"

# Mixed colors
Place: cyan+ magenta− cyan× cyan÷
→ "Mixed colors! All operators must be the same color."

# Wrong dimension
Try to activate in Overworld
→ "Operator activation only works in the Nether!"
```

## 📋 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/glassingbeds enable` | Enable lava-to-glass conversion | `charmedchars.admin` |
| `/glassingbeds disable` | Disable the glassing beds feature | `charmedchars.admin` |
| `/glassingbeds status` | Check if feature is enabled | `charmedchars.admin` |

**Tab Completion**: Press TAB after typing `/glassingbeds` to see available subcommands.

## 🔧 Technical Details

### New Files
- `GlassingBedsListener.kt` (156 lines) - Main feature logic
- `OperatorActivationListener.kt` (355 lines) - Activation system
- `GlassingBedsCommand.kt` (214 lines) - Admin command

### Architecture
- **Dual Event Handlers**: PlayerInteractEvent + BlockDamageEvent for max compatibility
- **Two-Block Bed Tracking**: Handles bed structure correctly
- **Session State Management**: Per-player activation tracking with automatic cleanup
- **Provider Agnostic**: Works with ItemsAdder, Oraxen, and Nexo

### Compatibility
- ✅ **100% Backward Compatible** - No breaking changes
- ✅ **Feature Opt-In** - Disabled by default
- ✅ **No Database Changes** - Structure database unchanged
- ✅ **All Providers** - Works with ItemsAdder, Oraxen, Nexo

## 📚 Documentation

### Updated Documentation
- ✅ README.md - Added Glassing Beds section
- ✅ VERSION.md - Complete v1.4.0 changelog
- ✅ HANGAR_SHOWCASE.md - Feature highlights and examples
- ✅ config.yml - New glassing-beds configuration section

### Gameplay Guide
See **[PLAY_INSTRUCTIONS.md](https://github.com/badbabywentaway/CharmedChars/blob/master/PLAY_INSTRUCTIONS.md)** for player-facing instructions.

### Configuration Guide
See **[REWARD_CONFIG.md](https://github.com/badbabywentaway/CharmedChars/blob/master/REWARD_CONFIG.md)** for admin configuration help.

## 🐛 Known Issues

None currently reported.

## 💡 Design Philosophy

**Why Activation Requirement?**
- Prevents accidental lava removal (expensive beds!)
- Adds resource cost (operator blocks) for powerful feature
- Creates strategic decision: is it worth the activation cost?
- Session-based design encourages careful planning

**Why Y-Level Restriction?**
- Prevents trivial ocean surface glassing at Y=31
- Allows underground lava removal where it's most useful
- Configurable to match your server's world generation
- Balances power of the feature

**Why Disabled by Default?**
- Server owners choose if feature fits their gameplay
- No surprise mechanics for existing servers
- Allows gradual rollout and testing
- Respects different server philosophies

## 🙏 Credits

**Development**: StephanosBad
**AI-Assisted Development**: Claude (Anthropic)
- Feature design and architecture
- Operator activation system
- Bed tracking logic and event handling
- Documentation and release notes

All AI contributions include Co-Authored-By attribution in git commits.

## 📝 Full Changelog

See **[VERSION.md](https://github.com/badbabywentaway/CharmedChars/blob/master/VERSION.md)** for complete changelog including:
- Detailed feature descriptions
- Technical implementation notes
- Configuration examples
- Upgrade instructions
- Development notes

## 🔗 Links

- **GitHub Repository**: https://github.com/badbabywentaway/CharmedChars
- **Issues**: https://github.com/badbabywentaway/CharmedChars/issues
- **Documentation**: https://github.com/badbabywentaway/CharmedChars/tree/master#readme
- **Previous Release**: [v1.3.2](https://github.com/badbabywentaway/CharmedChars/releases/tag/v1.3.2)

---

**Enjoy the new Glassing Beds feature! Report bugs on GitHub Issues.**
