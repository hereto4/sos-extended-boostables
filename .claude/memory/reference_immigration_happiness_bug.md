---
name: immigration-happiness-bug
description: Why running Extended Boostables (+ Create-A-Culture) caused runaway player-city immigration in v71, and which mod was actually at fault
metadata:
  type: reference
---

Investigated 2026-06-26/27. **Symptom:** with Create-A-Culture (`sos-create-a-culture`) + Extended
Boostables installed, player-city **immigration is far higher than it should be**, from a fresh game
start, even with **no Create-A-Culture techs unlocked**. Scope: **v71 only**.

**Root cause: Extended Boostables' `STAT_WORK_RETIREMENT` feature — NOT Create-A-Culture.** Disabled
2026-06-27 (see [[stat-system-seams]] and [[project-sos-extended-boostables]]).

## Mechanism
- Immigration is **happiness-driven**: `settlement/entry/Immigration.java` computes "wanted" immigrants
  from `hap = fulfillment / expectation × HAPPI` (`getImmigrants`, ~line 311; `Immigrator.speed` reads
  `BOOSTABLES.CIVICS().IMMIGRATION.get(POP_CL.clP())` at line 190). Higher happiness → more pull.
- `fulfillment = pow(current / maxes[r.index], fullPow)` in `StandingCitizen.Fulfillment`. `maxes`/`defs`
  are the per-race fulfillment **denominators**, computed in `StandingCitizen.setAll()` at init/load and
  (in the v70.32 source) **static during play**.
- EB's `STAT_WORK_RETIREMENT` (`MainScript.handleStatRetirement`, every 2s) reflectively pins
  `StandingCitizen.maxes/defs` (CITIZEN + SLAVE) back to a baseline via `restoreDenominators()`, run
  **unconditionally** each tick. The "net-boost" design is a no-op only if those denominators are static
  (true in v70.32). The **v71 standing rework** (per-HCLASS `MAX_CITY_POP`/`FULFILLMENT_EXPONENT`, per the
  v71 boost-key dump) appears to break that assumption → pinning holds the denominator below its true
  value → fulfillment inflated → happiness inflated → immigration inflated.
- It ran for **zero benefit**: no shipped tech/data file boosts `STAT_WORK_RETIREMENT`, so `F` (=
  `statRetirement.get(player)`) is always 1.0. Pure liability.

## Why Create-A-Culture was exonerated (the "no techs" giveaway)
CAC ships only tech files + two script jars (`target-race-tech`, `tech-boost-sort`); **no race/standing
files**, no base-value changes. Decisive fact: **every tech-based boost contributes its NEUTRAL value at
tech level 0** — the vanilla `PTech.BoostCompound` scales each tech's contribution by
`level(tech)/levelMax = 0` (→ MUL ×1, ADD +0). So no tech (CAC's or anyone's) can move happiness at game
start. Verified each CAC mechanism is neutral at level 0:
- `tech-boost-sort` — only reorders each tech's tooltip effect list (cosmetic).
- `target-race-tech` — converts `TARGET_RACE` tech boosts into a `RaceFilteredBooster` on the boostable;
  `getValue(0) = from()`, and a tech `BoosterValue` has `from()=1.0` (MUL)/`0.0` (ADD) → exactly neutral
  at level 0. (It also only responds to `Induvidual` queries, while immigration queries `POP_CL` — so the
  human `CIVIC_IMMIGRATION>MUL` tech wouldn't even affect immigration speed.)
- The two mods ARE intentionally coupled: CAC's `RACE_HAVENS.txt` consumes EB keys (`ROOM_*_ALL`,
  `BATTLE_FEAR`, `CIVIC_INDOCTRINATION`, `CIVIC_PLUNDER`, `ROOM__CANNIBAL`). Tech BOOST keys resolve
  lazily via `BoostSpecs.PromiseList` on `BOOSTING.waiting` at `finishSetup` — AFTER EB registers its
  keys in `initBeforeGameInited` — so the coupling resolves fine. All still level-gated.

## How to confirm / fix
- Confirm: run EB **without** CAC → bug should persist (it's EB-only). Or toggle just this feature.
- Fix applied: commented out (not deleted), 7 blocks + 6 imports tagged
  `STAT_WORK_RETIREMENT DISABLED (2026-06-27)`. **Rebuild** to regenerate the v71 jar.
- Before re-enabling: re-validate the reflection against the actual **v71** `StandingCitizen`/
  `StandingData`; if v71 recomputes `maxes/defs` during play, the denominator-pin approach is unworkable
  and the net-boost must be redesigned.

## Caveat
Diagnosis was done from **v70.32 source only** (`.claude/game-source-java/`); no v71 jar/source was
available on the build machine (only `.lastUpdated` placeholders in `~/.m2/.../71.19/`). The v70 source
proves the elimination argument (everything else is neutral at game start); the exact v71 failure mode
inside the standing system is inferred from the documented v71 rework, not read from v71 source.
