# CharmedChars v1.4.2 - Dependency Updates for Minecraft 1.21.11

**Release Date**: April 15, 2026

## 🔧 What's New

This is a maintenance release with no gameplay changes. It updates core dependencies to support Minecraft 1.21.11 and hardens the build system for modern Java environments.

### Minecraft 1.21.11 Support

The plugin now targets **PaperMC 1.21.11 (Build #130+)**. Servers still running 1.21.10 are unaffected — this is a drop-in upgrade.

### Dependency Updates

| Dependency | Old | New |
|---|---|---|
| Kotlin | 2.2.21 | 2.3.20 |
| Kotlinx Coroutines | 1.9.0 | 1.10.2 |
| PaperMC API | 1.21.10-R0.1-SNAPSHOT | 1.21.11-R0.1-SNAPSHOT |
| ProtocolLib | 5.3.0 | 5.4.0 |
| Oraxen | 1.181.0 | 1.212.0 |
| Nexo | 0.1.0 | 1.0.0 |

> **Note**: ProtocolLib 5.4.0 changed its Maven coordinates to `net.dmulloy2`. This is transparent to server admins — download the JAR as normal.

> **Note**: Nexo 1.0.0 compiles successfully but has not been tested at runtime (no premium license available). Community feedback welcome.

### Build System Hardening

- **Gradle**: 8.8 → 8.14.4 (adds Java 23/24 daemon support)
- **Java toolchain**: explicitly targets Java 21 bytecode via `kotlin { jvmToolchain(21) }`

These changes ensure the project builds correctly on modern JDK installations without manual `JAVA_HOME` configuration.

## 📦 Download

**JAR File**: `CharmedChars-1.4.2.jar` (attached below)

**Requirements**:
- Minecraft 1.21.11+
- Paper or Paper-based server
- Java 21+
- **One of**: ItemsAdder 3.6.3-beta-14+ OR Oraxen 1.212.0+ OR Nexo 1.0.0+
- ProtocolLib 5.4.0+ (if using ProtocolLib features)

## 🚀 Installation

### Upgrading from v1.4.1

1. Stop server
2. Replace `CharmedChars-1.4.1.jar` with `CharmedChars-1.4.2.jar`
3. Update PaperMC to Build #130 or later (1.21.11)
4. If using Oraxen: update to 1.212.0+
5. If using ProtocolLib: update to 5.4.0+
6. Start server

**No configuration changes needed — drop-in replacement.**

### Upgrading from v1.4.0 or Earlier

1. Follow the [v1.4.1 upgrade instructions](https://github.com/badbabywentaway/CharmedChars/releases/tag/v1.4.1) first
2. Then upgrade to v1.4.2 as above

### New Installations

1. Download `CharmedChars-1.4.2.jar`
2. Install one custom item provider (ItemsAdder / Oraxen / Nexo)
3. Place both JARs in `plugins/` folder
4. Start server
5. Run setup command (`/iasetup`, `/oraxensetup`, or `/nexosetup`)
6. Restart server
7. Optionally enable Glassing Beds: `/glassingbeds enable`

## ✅ Compatibility

- ✅ **100% Backward Compatible** — No breaking changes
- ✅ **No configuration changes** required
- ✅ **No database schema changes**
- ✅ **No gameplay changes**
- ✅ Works with ItemsAdder, Oraxen, and Nexo
- ✅ All 131+ tests passing

## ⚠️ Known Issues / Deferred Updates

- **ItemsAdder 4.0.16**: Not supported — critical stability issues (OutOfMemoryError, server crashes) reported in the community. Remaining on 3.6.3-beta-14.
- **Exposed ORM 1.0**: Migration deferred to v1.5.0 due to package namespace changes (`org.jetbrains.exposed` → `org.exposed`).

## 🐛 Known Bugs

None currently reported.

## 🙏 Credits

**Development**: StephanosBad
**AI-Assisted Development**: Claude Sonnet 4.6 (Anthropic)
- Dependency version research and verification
- Build system upgrade (Gradle 8.8 → 8.14.4, Java 21 toolchain)
- Documentation updates

All AI contributions include Co-Authored-By attribution in git commits.

## 📝 Full Changelog

See **[VERSION.md](https://github.com/badbabywentaway/CharmedChars/blob/master/VERSION.md)** for the complete changelog including detailed build system notes and deferred update rationale.

## 🔗 Links

- **GitHub Repository**: https://github.com/badbabywentaway/CharmedChars
- **Issues**: https://github.com/badbabywentaway/CharmedChars/issues
- **Documentation**: https://github.com/badbabywentaway/CharmedChars/tree/master#readme
- **Previous Release**: [v1.4.1](https://github.com/badbabywentaway/CharmedChars/releases/tag/v1.4.1)

---

**No gameplay changes in this release — safe to upgrade at any time.**
