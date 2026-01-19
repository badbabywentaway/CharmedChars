# CharmedChars v1.4.1 - Colored Glass & Safety Enhancements

**Release Date**: January 19, 2026

## 🎨 What's New

### Colored Glass Feature
Transform lava into **colored glass** that matches your bed color! This enhancement makes glassing beds even more useful for decorative builds.

**How It Works**:
- 🤍 **White beds** → Clear glass (unstained)
- 🔴 **Red beds** → Red stained glass
- 🔵 **Blue beds** → Blue stained glass
- 🟡 **Yellow beds** → Yellow stained glass
- 🟢 **Lime beds** → Lime stained glass
- ...and all 16 Minecraft bed colors!

**Use Cases**:
- Create multicolored glass paths through the Nether
- "Paint" lava lakes with decorative glass patterns
- Build colored glass structures and art installations
- Mix colors in the same lava lake for unique designs

### Safety Documentation
Added comprehensive safety warnings to help players understand the limitations of glassing beds:

**⚠️ Important Safety Limitations**:
- **Overlapping Explosions**: Multiple bed explosions can destroy previously-created glass
- **Lava Flow Behavior**: Not all lava flows are guaranteed to stop
- **Underground Lava**: Use the same caution as if you didn't have glassing beds
- **Ancient Debris Mining**: Consider mining in different, safer areas

**New FAQ Entries**:
- Why does lava keep flowing even after I use glassing beds?
- Is it safe to do ancient debris mining with glassing beds?
- What colors of glass can I create?
- Can I get clear glass instead of colored?
- Can I mix colors in one lava lake?

## 📦 Download

**JAR File**: `CharmedChars-1.4.1.jar` (attached below)

**Requirements**:
- Minecraft 1.21.10+
- Paper or Paper-based server
- Java 21+
- **One of**: ItemsAdder 3.6.3-beta-14+ OR Oraxen 1.181.0+ OR Nexo 0.1.0+

## 🚀 Installation

### Upgrading from v1.4.0
1. Stop server
2. Replace `CharmedChars-1.4.0.jar` with `CharmedChars-1.4.1.jar`
3. Start server
4. **No configuration changes needed** - colored glass works automatically!

### Upgrading from v1.3.2 or Earlier
1. Follow the [v1.4.0 upgrade instructions](https://github.com/badbabywentaway/CharmedChars/releases/tag/v1.4.0) first
2. Then upgrade to v1.4.1 as above

### New Installations
1. Download CharmedChars-1.4.1.jar
2. Install one custom item provider (ItemsAdder/Oraxen/Nexo)
3. Place both JARs in `plugins/` folder
4. Start server
5. Run setup command (`/iasetup`, `/oraxensetup`, or `/nexosetup`)
6. Restart server
7. Optionally enable Glassing Beds: `/glassingbeds enable`

## 🎮 Gameplay Examples

### Example 1: Colored Glass Art
```
1. Activate glassing beds (hit 4 operator sequence)
2. Find a lava lake in the Nether
3. Place RED bed near one section → right-click
4. Red stained glass appears! 🔴
5. Place BLUE bed near another section → right-click
6. Blue stained glass appears! 🔵
7. Place WHITE bed for clear glass in the middle
8. Result: Beautiful multicolored glass path!
```

### Example 2: Decorative Build
```
Player wants to create a rainbow glass floor:
- Use 7 different colored beds
- Create 7 sections of colored glass
- Result: Rainbow glass platform over former lava lake!
```

### Example 3: Safety Awareness
```
1. Player uses 3 beds in overlapping explosions
2. Second explosion destroys some glass from first explosion
3. Lava flows through the gap
4. Player realizes: "I need to plan bed placement better!"
5. Lesson: Space out bed explosions to avoid overlap
```

## ⚙️ Configuration

**No configuration changes needed!** The colored glass feature works automatically.

The glassing beds feature itself is still controlled by the same config:
```yaml
glassing-beds:
  enabled: false  # Set to true or use /glassingbeds enable
  max-y: 28
```

## 🔧 Technical Details

### Code Changes
**Modified File**: `GlassingBedsListener.kt`

**Bed Color Detection**:
```kotlin
val bedType = explodedBlock.type
val glassType = if (bedType == Material.WHITE_BED) {
    Material.GLASS  // White beds create clear glass
} else {
    // Extract color from bed type name (e.g., "RED_BED" → "RED")
    val bedColor = bedType.name.removeSuffix("_BED")
    // Map to stained glass (e.g., "RED" → "RED_STAINED_GLASS")
    Material.getMaterial("${bedColor}_STAINED_GLASS") ?: Material.GLASS
}
```

**All 16 Bed Colors Supported**:
WHITE, ORANGE, MAGENTA, LIGHT_BLUE, YELLOW, LIME, PINK, GRAY, LIGHT_GRAY, CYAN, PURPLE, BLUE, BROWN, GREEN, RED, BLACK

### Compatibility
- ✅ **100% Backward Compatible** - No breaking changes
- ✅ **Feature Automatic** - Works immediately after upgrade
- ✅ **No Config Changes** - Existing configs work unchanged
- ✅ **All Providers** - Works with ItemsAdder, Oraxen, Nexo

### Performance
- **Zero Performance Impact** - Single Material lookup per explosion
- **Efficient String Manipulation** - Trivial suffix removal + concatenation
- **Same Conversion Logic** - Only the glass type changes, not the algorithm

## 📚 Documentation

### Updated Files
- ✅ **PLAY_INSTRUCTIONS.md** - Added colored glass examples and safety warnings
- ✅ **VERSION.md** - Complete v1.4.1 changelog
- ✅ **config.yml** - Added colored glass feature comment
- ✅ **RELEASE_NOTES_1.4.1.md** - This file

### Player Guide
See **[PLAY_INSTRUCTIONS.md](https://github.com/badbabywentaway/CharmedChars/blob/master/PLAY_INSTRUCTIONS.md)** Section 10 for:
- Complete glassing beds guide
- Colored glass examples
- Safety warnings and limitations
- FAQ with 13+ questions

### Configuration Guide
See **[REWARD_CONFIG.md](https://github.com/badbabywentaway/CharmedChars/blob/master/REWARD_CONFIG.md)** for admin configuration help.

## 🐛 Known Issues

None currently reported.

## 💡 Design Philosophy

**Why Colored Glass?**
- **Decorative Utility**: Glassing beds becomes more than just safety - it's a building tool
- **Player Expression**: Choose colors that match your build aesthetic
- **No Complexity**: No new mechanics to learn - just works automatically
- **Historical Accuracy**: White beds create clear glass (matches Minecraft's glass crafting)

**Why Safety Warnings?**
- **Realistic Expectations**: Players need to know limitations upfront
- **Prevent Frustration**: Clear documentation prevents "why didn't it work?" confusion
- **Encourage Safe Play**: Reinforces that glassing beds is a helper, not a replacement for caution
- **Community Feedback**: Based on initial testing and player questions

## 🙏 Credits

**Development**: StephanosBad
**AI-Assisted Development**: Claude Sonnet 4.5 (Anthropic)
- Colored glass feature design and implementation
- Bed color to stained glass mapping logic
- Safety documentation enhancements
- FAQ additions and example updates

All AI contributions include Co-Authored-By attribution in git commits.

## 📝 Full Changelog

See **[VERSION.md](https://github.com/badbabywentaway/CharmedChars/blob/master/VERSION.md)** for complete changelog including:
- Detailed feature descriptions
- Technical implementation notes
- Code snippets and line numbers
- Upgrade instructions
- Testing verification
- Development notes

## 🔗 Links

- **GitHub Repository**: https://github.com/badbabywentaway/CharmedChars
- **Issues**: https://github.com/badbabywentaway/CharmedChars/issues
- **Documentation**: https://github.com/badbabywentaway/CharmedChars/tree/master#readme
- **Previous Release**: [v1.4.0](https://github.com/badbabywentaway/CharmedChars/releases/tag/v1.4.0)

---

**Enjoy creating colorful glass art in your Nether! Report bugs on GitHub Issues.**
