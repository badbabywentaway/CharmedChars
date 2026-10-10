# CharmedChars v2.1.0 — MC 26.3 Compatibility

## Release Date
2026-10-10

## Overview

v2.1.0 brings Minecraft 26.3 compatibility, verified across all four modes with live in-game scoring tests: Native, Oraxen (beta), Nexo, and ItemsAdder (beta). The headline fix resolves a case where word scoring could silently never trigger at all — caused by ItemsAdder's 4.1.0 nativepackets beta firing `BlockBreakEvent` before the interact/damage events CharmedChars relied on for scoring. This release also fixes a Native-mode startup crash, adds Poplar wood to the letter-block drop table, and updates Paper, WorldGuard, and NBT-API to versions that actually work on MC 26.3.

---

## Verified Platforms

| Mode | Status |
|------|--------|
| Native | ✅ Verified live on MC 26.3 (placement + scoring tested) |
| Oraxen 1.220.0 (Discord beta) | ✅ Verified live on MC 26.3 (placement + scoring tested) |
| Nexo 1.29.1 | ✅ Verified live on MC 26.3 (placement + scoring tested) |
| ItemsAdder 4.1.0-beta-nativepackets-3 (Discord beta) | ✅ Verified live on MC 26.3 (placement + scoring tested) |

---

## Fixes

### Word Scoring Never Triggering on Reversed Event Order (`ItemManager.kt`)
- **Root cause**: ItemsAdder's `4.1.0-beta-nativepackets` line (plus its `NativePackets` companion plugin) fires `BlockBreakEvent` *before* `PlayerInteractEvent`/`BlockDamageEvent` — the reverse of normal Minecraft event order
- CharmedChars only triggered scoring from the interact/damage handlers. By the time they fired, the targeted block had already been removed by the break event, so `testForLetter` silently rejected it as "not a letter block"
- **Symptom**: breaking a letter block looked completely normal (correct texture, normal break animation) but never produced a "Hit" or "Miss" message — regardless of whether the placed blocks formed a valid word
- **Fix**: `letterBlockBreak` (the `BlockBreakEvent` handler) now also calls `processWordScoring` directly, using the still-intact block reference from the break event itself — proven correct by logging the exact detected letter at that point in the event chain
- On providers with normal event order (Oraxen, Nexo, Native), this is a safe no-op: the block is already gone by the time the break handler re-checks it, since scoring already ran and removed it via the interact/damage path
- Verified end-to-end with real placement + breaking on all four modes

### CharmedChars Crashing on Enable in Native Mode (`CharmedChars.kt`)
- `onEnable()` called `NativeItemManagerSetup.setup()` before `configManager` was initialized; `setup()` reads `plugin.configManager` internally, throwing `UninitializedPropertyAccessException` on every single boot
- This also triggered whenever ItemsAdder, Oraxen, or Nexo failed to enable (e.g. an incompatible version on a new MC release) and CharmedChars fell back to Native mode — taking CharmedChars down right along with the failed provider instead of gracefully falling back
- Fix: `configManager` now initializes before the native setup() call

---

## New Features

### Poplar Wood Letter Block Drops (`ItemManager.kt`)
- Poplar log → stripped Poplar log is now registered in the gold/pyrite-tool drop table, same as every other wood type
- Resolved via `Material.matchMaterial("POPLAR_LOG")` rather than a direct enum reference, since Poplar isn't present in every running Paper build (e.g. 26.2 stable) — degrades gracefully to a no-op on servers without it, no crash
- Verified live: breaking a Poplar log with a gold/pyrite tool drops letter blocks correctly on MC 26.3

---

## Dependency Updates

| Library | Previous | New | Notes |
|---------|----------|-----|-------|
| Paper API | `1.21.11-R0.1-SNAPSHOT` | `26.3.build.169-beta` | |
| JVM toolchain | `21` | `25` | Required by the Paper 26.3 beta API jar |
| JaCoCo | `0.8.12` | `0.8.15` | 0.8.12 can't instrument JVM 25 bytecode (class file major version 69) |
| WorldGuard | `7.0.14` | `7.0.19` | 7.0.14 strictly pins Guava/Gson/Fastutil versions that conflict with what Paper 26.3 bundles |
| NBT-API | `2.15.7` | `2.16.1` | 2.15.7 has no NMS mapping for MC 26.3 — Native mode hard-crashed (`Method not loaded! 'ITEMSTACK_BUKKITMIRROR'`) the moment it tried to tag an item |
| ItemsAdder API | `dev.lone:api-itemsadder:4.0.2-beta-release-11` (`maven.devs.beer`) | `beer.devs:itemsadder-api:4.0.18-beta-10` (Maven Central) | `maven.devs.beer` is dead (confirmed 404). Java package names (`dev.lone.itemsadder.api.*`) are unchanged, so no source changes were needed |

**Runtime-only — Discord betas, not yet on public release channels (no Maven artifact to pin):**
- **ItemsAdder `4.1.0-beta-nativepackets-3`** — required for MC 26.3. Stable `4.0.18` has no 26.3 NMS mapping and crashes with `ExceptionInInitializerError` on load
- **Oraxen `1.220.0`** — required for MC 26.3. `1.219.1` and every earlier release fail to enable on 26.3 (confirmed crash)

---

## Build/Deploy Changes

- `deploy-testbeds.sh`/`.bat` now read the plugin version from `gradle.properties` (`version=`) instead of a hardcoded string, so they won't silently go stale on the next version bump
- Removed `DEPLOY_CLEAN_JAR.bat` — dead script pinned to a `1.3.2` jar that hasn't existed since the 2.0.0 bump; fully superseded by `deploy-testbeds.sh`/`.bat`, which also cover the Native test server this script never did
- Fixed `StructureDatabaseTest` crashing under JDK 25 — a bare MockK relaxed mock of `java.util.logging.Logger` NPEs against JDK 25's changed `Logger.isLoggable` fast path; replaced with a real `Logger` instance

---

## Upgrade Instructions

**From v2.0.0:**
1. Stop the server
2. Replace the existing CharmedChars JAR with `CharmedChars-2.1.0.jar`
3. **ItemsAdder servers targeting MC 26.3**: grab the `4.1.0-beta-nativepackets` build from ItemsAdder's Discord — the Maven-published `4.0.18` line will crash on load
4. **Oraxen servers targeting MC 26.3**: grab `1.220.0` from Oraxen's Discord — `1.219.1` and earlier will fail to enable
5. Start the server
6. No `/iasetup`, `/oraxensetup`, `/nexosetup`, or `/nativesetup` re-run needed — this release doesn't change any generated pack content

**No database or world migration needed** — drop-in replacement.

---

## Compatibility

- ✅ Minecraft 26.3 / PaperMC (beta build)
- ✅ Java 25+
- ✅ ItemsAdder 4.1.0-beta-nativepackets-3+ — verified live on MC 26.3 (Discord beta)
- ✅ Oraxen 1.220.0+ — verified live on MC 26.3 (Discord beta)
- ✅ Nexo 1.29.1+ — verified live on MC 26.3
- ✅ Native mode — verified live on MC 26.3
- ✅ 100% backward compatible — no config or database changes required
