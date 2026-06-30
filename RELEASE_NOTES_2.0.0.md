# CharmedChars v2.0.0 — MC 26.2 Compatibility

## Release Date
2026-06-30

## Overview

v2.0.0 brings full Minecraft 26.2 compatibility, verified across all four modes: Native, Oraxen, Nexo, and ItemsAdder (beta build). The headline gameplay addition is the Potent Sulfur alternate crafting path for Pyrite Ingot, introduced by MC 26.2's new sulfur material. Server operators gain a configurable CMD base offset to avoid conflicts with other native-mode plugins. Several Oraxen-specific issues are also resolved: cyan block display names now render correctly, and recipe keys are namespaced to prevent conflicts with other Oraxen plugins sharing the same recipe files.

---

## Verified Platforms

| Mode | Status |
|------|--------|
| Native | ✅ Verified on MC 26.2 |
| Oraxen 1.216.0+ | ✅ Verified on MC 26.2 |
| Nexo 1.24.0+ | ✅ Verified on MC 26.2 |
| ItemsAdder 4.0.2-beta-release-11+ | ✅ Verified on MC 26.2 (beta build) |

---

## New Features

### Potent Sulfur Alt Recipe for Pyrite Ingot (MC 26.2+)
- Iron Ingot + Potent Sulfur → Pyrite Ingot (shapeless, all four modes)
- **Native**: registered via `Material.matchMaterial("POTENT_SULFUR")` — activates automatically on MC 26.2+, silently skipped on older servers. Recipe key: `charmedchars:pyrite_ingot_sulfur`
- **Oraxen**: generated at `/oraxensetup` time with the same material guard; key `charmedchars_pyrite_ingot_sulfur` in `shapeless.yml`
- **Nexo**: generated at `/nexosetup` time with the same material guard; key `pyrite_ingot_sulfur` in `shapeless/charmedchars.yml`
- **ItemsAdder**: static entry `pyrite_ingot_sulfur_recipe` in `pyrite.yml` (ItemsAdder gracefully skips unknown materials on pre-26.2 servers)

### Configurable Native CMD Base Offset (`NativeItemManagerSetup.kt`, `ConfigManager.kt`)
- New config key `native-items.cmd-base` (default: `1000`) in `config.yml`
- CharmedChars claims `cmd-base` through `cmd-base + 124` (120 block items + 5 pyrite items)
- Change this value if another plugin occupies the same CMD range on the same base materials, then run `/nativesetup force` to regenerate the resource pack
- ⚠️ Changing on a live server with placed blocks will break existing block visual identities — setup-time config only

---

## Fixes

### Oraxen Cyan Block Display Names (`OraxenSetup.kt`)
- `<cyan>` is not a valid MiniMessage color tag; Oraxen rendered it literally as `<Cyan>` preceding the block name
- Fix: replaced with `<aqua>` — the correct MiniMessage named tag for that color
- Requires `/oraxensetup force` then `/oraxen reload all` to apply to existing servers

### Oraxen Recipe Namespace Conflicts (`OraxenSetup.kt`)
- Oraxen appends all plugin recipes to shared `shaped.yml` and `shapeless.yml`; bare keys like `pyrite_ingot:` could collide with other plugins using the same key names
- Fix: all CharmedChars recipe keys prefixed with `charmedchars_` (`charmedchars_pyrite_ingot`, `charmedchars_pyrite_pickaxe`, etc.)
- `removeCharmedCharsRecipes()` and `hasCharmedCharsRecipes()` updated to match the new key names
- Requires `/oraxensetup force` then `/oraxen reload all` to apply to existing servers

---

## Dependency Updates

| Library | Previous | New | Notes |
|---------|----------|-----|-------|
| ItemsAdder API | `com.github.LoneDev6:API-ItemsAdder:3.6.3-beta-14` (JitPack) | `dev.lone:api-itemsadder:4.0.2-beta-release-11` (maven.devs.beer) | Group ID and repository changed for 4.x |
| Oraxen | `1.212.0` | `1.216.0` | |
| Nexo | `1.0.0` | `1.24.0` | |

**Build fixes for Nexo 1.24.0:**
- Excluded transitive `com.google.code.gson:gson` from Nexo to resolve strict-version conflict with WorldGuard's `gson:2.11.0` constraint
- Added `-Xskip-prerelease-check` to Kotlin compiler options — Nexo 1.24.0 was compiled against a Kotlin pre-release build

---

## Documentation

- Logo block clarified as displaying the signature of **Gaia Temperini**, artist of the marble-style block font textures, across all documentation (README, HANGAR_SHOWCASE, config.yml, release notes)
- README version requirements updated to reflect current minimum versions
- Nexo stale "untested" labels removed (should have been cleared in v1.5.0)

---

## Upgrade Instructions

**From v1.5.0 / v1.5.1:**
1. Stop the server
2. Replace the existing CharmedChars JAR with `CharmedChars-2.0.0.jar`
3. Start the server
4. **Oraxen servers**: run `/oraxensetup force` then `/oraxen reload all` — required to apply the cyan display name fix and namespaced recipe keys
5. **Nexo servers**: run `/nexosetup force` then `/nexo reload` — required to pick up the sulfur recipe if running MC 26.2+
6. **Native servers**: run `/nativesetup force` — required only if you changed `native-items.cmd-base`; otherwise the existing pack remains valid
7. **ItemsAdder servers**: run `/iasetup force` then `/iazip` to pick up the new sulfur recipe entry

**No database or world migration needed** — drop-in replacement.

---

## Compatibility

- ✅ Minecraft 26.2 / PaperMC (experimental build)
- ✅ Java 21+
- ✅ ItemsAdder 4.0.2-beta-release-11+ — verified on MC 26.2 (beta build)
- ✅ Oraxen 1.216.0+ — verified on MC 26.2
- ✅ Nexo 1.24.0+ — verified on MC 26.2
- ✅ Native mode — verified on MC 26.2
- ✅ 100% backward compatible — no config or database changes required (existing `cmd-base` defaults to 1000, preserving prior behavior)
