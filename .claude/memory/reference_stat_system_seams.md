---
name: stat-system-seams
description: Why STAT_* boostable keys can't hook race STATS:{} cleanly; the seam map lives in doc/STAT_system_map.md
metadata:
  type: reference
---

Researched 2026-06-14 for a planned `STAT_*` boostable prefix (e.g. `STAT_WORK_RETIREMENT`) meant
to boost values from a race file's `STATS:{}` block, applied uniformly across all population
classes. **Full map: `doc/STAT_system_map.md`** (committed). Key facts:

- Race `STATS:{}` entries are **per-class standing weights** (`CHILD/CITIZEN/SLAVE/NOBLE`) →
  `init.race.RaceStats` → `StatStanding.StandingDef`. `WORK_RETIREMENT` there = the retirement
  *need* standing.
- Global stats are `STAT` objects in `settlement.stats.colls.*`. Key = `<collectionKey>_<statKey>`
  (`STAT.java:30`), e.g. WORK + RETIREMENT = `WORK_RETIREMENT`.
- **No engine path reads a `STAT_<KEY>` boostable to modify a stat.** Stats are *sources* that feed
  boostables (`STAT.boosters` + `StatBooster.make` + `assets .../bonus/` configs go stat→boostable),
  never consumers. So `STAT_*` effects must be applied by the mod via reflection.
- The four `STAT` seams: `indu()` (per-subject value), `standing()` (per-class weights — **closed**:
  `StandingDef`/`StandingData` `final`, `private final` array on `Race`, totals cached in
  `StandingCitizen`, recomputed only at init/load), `decree()` (single settlement-wide value —
  cleanest reflective target, only on `FOOD_RATIONS_DECREE`, `DRINK_RATION_DECREE`,
  `RETIREMENT_AGE_DEC`), and `boosters` (wrong direction).
- No bytecode instrumentation available (SoS mods are plain jars, no Java agent), so an in-place
  boostable read inside the standing math isn't possible.

**Status:** `STAT_WORK_RETIREMENT` **DISABLED 2026-06-27** (commented out in `MainScript.java`, not
deleted — 7 blocks tagged `STAT_WORK_RETIREMENT DISABLED (2026-06-27)`, plus its 6 now-unused imports;
re-enable by uncommenting all of them). **Why disabled:** in v71 it inflated player-city **happiness →
runaway immigration**, present from a fresh game start with no techs unlocked. Immigration "wanted" is
happiness-driven (`Immigration.getImmigrants` uses `fulfillment/expectation × HAPPI`), and this feature
is the ONLY mod code that mutates the fulfillment machinery directly — `restoreDenominators()` pins
`StandingCitizen.maxes/defs` every 2s. The "net-boost" design assumes those denominators are STATIC
after `setAll()` (true in the v70.32 source → a genuine no-op at F=1), but the **v71 standing rework**
(per-HCLASS `MAX_CITY_POP`/`FULFILLMENT_EXPONENT`) appears to break that assumption, so pinning holds the
denominator below its true value → fulfillment/happiness inflated. It also ran for nothing: **no shipped
tech/data file boosts the key, so F was always 1.0.** Note: this is an EB-only bug; Create-A-Culture was
investigated and exonerated (all its boosts are tech-level-gated → neutral at game start). **Before
re-enabling:** re-validate the reflection against the actual **v71** `StandingCitizen`/`StandingData`
(confirm `maxes/defs` are still static per-race); if v71 recomputes them during play, the
denominator-pinning approach can't work and the net-boost needs a redesign. The diagnosis was made from
v70.32 source only (no v71 jar/source was available on the build machine). See [[immigration-happiness-bug]].

Original design (kept for the eventual re-implementation). Pattern for future
`STAT_*` keys:
- New `BoostableCat("STAT_", ...)` so push key `WORK_RETIREMENT` → `STAT_WORK_RETIREMENT`.
- `MainScript.handleStatRetirement()` runs every 2s: F = `statRetirement.get(player)`; when F changes,
  reflectively set every race's WORK_RETIREMENT `StandingData.max/from/to` (`final` instance fields,
  set via `Field.setDouble` after `setAccessible`) to `baselineWeight × F`. Baseline weights +
  baseline `StandingCitizen.maxes/defs` captured once on first tick (before any scaling).
- **Net-boost (not reweight):** after scaling weights, `restoreDenominators()` arraycopies the
  baseline back into `STANDINGS.CITIZEN()/SLAVE()` private `maxes`/`defs` arrays each tick, so the
  scaled retirement weight raises the fulfillment numerator while the denominator stays baseline →
  genuine happiness gain for satisfied retirement, no penalty for subjects without it. Reasserting
  each tick also repairs the arrays after a load (engine `setAll()` rewrites them).
- Reads are public (`RaceStats.def(StatStanding)`, `StandingData.max`); only the `final` weight
  writes + private `maxes/defs` arrays need reflection. Whole thing in try/catch → on any reflection
  failure sets `statDisabled` and no-ops (never crashes the game). Note: F multiplies EXISTING
  per-class weight, so races with 0 retirement weight are unaffected (0×F=0).
See [[project-sos-extended-boostables]] and `doc/STAT_system_map.md`.
