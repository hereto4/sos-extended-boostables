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

**Status:** `STAT_WORK_RETIREMENT` IMPLEMENTED 2026-06-14 (net-boost approach). Pattern for future
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
