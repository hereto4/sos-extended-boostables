# Songs of Syx stat system — modding seam map (v71.19)

Research notes for designing `STAT_*` boostable keys. Goal: a `STAT_<KEY>` booster that
"applies a single value to all class-children" of a race stat (e.g. `STAT_WORK_RETIREMENT`).

**Bottom line up front:** no engine code path reads a `STAT_<KEY>` boostable to modify a stat.
Stats are *sources* that feed boostables, never *consumers* of one. So any `STAT_*` effect must
be written by the mod via reflection. The cleanest reflective targets are the handful of stats
that carry a **decree** (a single settlement-wide value); the per-class **standing** weights are
closed (final + cached) and only support fragile, static-ish manipulation.

---

## Two layers

1. **Race file `STATS:{}` block** (`assets/init/race/<RACE>.txt`). Each entry is a *standing
   definition*: per-population-class weights (`CHILD` / `CITIZEN` / `SLAVE` / `NOBLE`). Parsed in
   `init.race.RaceStats` → `StatStanding.StandingDef`. This is what `WORK_RETIREMENT` is in a race
   file. **The per-class array the user wants to set uniformly lives here.**
2. **Global `STAT` objects defined in engine code** (`settlement.stats.colls.*`). These are the
   registered stats. The race-file block above only supplies *standing weights* for them.

## Stat key scheme

`STAT.key = <collectionKey> + "_" + <statKey>` with `__` collapsed to `_` (`STAT.java:30`).
Example: collection `WORK` + stat `RETIREMENT` → `WORK_RETIREMENT`. A trailing `*` in a race file
(e.g. `FOOD*`, `POPULATION*`) targets the whole collection.

## Collections (`STATS.java:180-203`)

population, law, govern, equip, **work**, home, **food**, service, environment, access, battle,
needs, education, traits, disease, event, stored, burial, religion, multipliers, appearance,
relations. (Each `StatsXxx` constructor sets its collection key/prefix.)

## The four seams on every `STAT` (`settlement/stats/stat/STAT.java`)

| Seam | What it is | Boostable-backed? | Reflective-hook difficulty |
|---|---|---|---|
| `indu()` → `INT_OE<Induvidual>` | per-individual measured value (0..1-ish); the *input* | no | per-subject runtime data; some stats are stored, some computed (`STATFake`) |
| `standing()` → `StatStanding` | per-class race weight × value → fulfillment/opinion | **no** | **hard**: `StandingDef`/`StandingData` are `final` classes w/ `final` fields, in a `private final` array on each `Race`; totals cached in `StandingCitizen` (recomputed only at init/load) |
| `decree()` → `StatDecree` | **single settlement-wide** player policy value | no | **easiest**: one value, settable; only a few stats have one |
| `boosters` → `BoostSpecs` | the stat's value as a **source** that boosts *other* boostables (configured in `assets .../bonus/` via `StatBooster.make`) | n/a (wrong direction) | irrelevant — does not boost the stat |

## Stats that carry a decree (single-value, cleanest reflective target)

- `FOOD_RATIONS_DECREE` → `FOOD` (target food servings) — `StatsFood.java:133`
- `DRINK_RATION_DECREE` → `DRINK` (target drink servings) — `StatsFood.java:141`
- `RETIREMENT_AGE_DEC` → `WORK_RETIREMENT_AGE` (retirement-age policy) — `StatsWork.java:683`

## Retirement specifics

| Key | Meaning | Shape |
|---|---|---|
| `WORK_RETIREMENT` (`RETIREMENT_HOME`) | retirement *need / desire* | per-class standing |
| `WORK_RETIREMENT_AGE` (`RETIREMENT_AGE`) | retirement-age *policy* (drives `shoudRetire()`) | settlement-wide decree |
| `WORK_EMPLOYED` | employment need | per-class standing |

`shoudRetire()` (`StatsWork.java:875`) reads `RETIREMENT_AGE.decree().getD(class,race)`.

## Implementation options for a `STAT_*` key (all reflection-based)

1. **Periodic reflective rewrite of standing weights** — rebuild each race's `StandingDef` for the
   target stat so all classes share `base × STAT_<KEY>.get(player)`, and force `StandingCitizen`
   to recompute its cached totals. Tracks techs within a few seconds. Most faithful to the
   per-class-array intent, but reflection-heavy and touches cached engine internals → fragile.
2. **Static load-time override** — set the per-class values once at load (effectively a race-data
   edit); will not respond to techs unlocking during play.
3. **Decree drive** — for the 3 decree stats, drive the settlement-wide value from a `STAT_*` key
   (e.g. `STAT_WORK_RETIREMENT_AGE` → `RETIREMENT_AGE_DEC`). Single value, less fragile; refresh on
   a timer to track techs. Only applicable to FOOD / DRINK / RETIREMENT_AGE.

No bytecode instrumentation is available (SoS mods load as plain jars, no Java agent), so a true
in-place boostable read inside the standing computation is not an option.
