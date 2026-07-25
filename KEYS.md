# Extended Boostables — added boostable keys

All keys this mod registers, for use in tech files (and anywhere the game consumes boostables).
Reference them in a tech's `BOOST:` block, e.g. `ROOM_MINE_ALL>MUL: 1.5` or `WORLD_PLUNDER>ADD: 0.5`.
Every key has a base value of `1.0`, so `>MUL` is the natural operator (`>MUL: 1.5` = +50%).

Game version: 71.19.

---

## Room keys

| Key | Display | Category | Effect |
|---|---|---|---|
| `ROOM__SLAVER` | Slaver | Rooms | Effectiveness of the Slaver room — raises the **submission** of slaves processed through it (slaves that arrived via the slaver room, i.e. `CAUSE_ARRIVES.PAROLE`). |
| `ROOM__CANNIBAL` | Cannibal | Rooms | Multiplies the resources gained when a corpse is **butchered at a Cannibal Room**. |

> Note the **double underscore**: the room's internal key starts with `_`, and the boost prefix
> adds another, so `_SLAVER` → `ROOM__SLAVER`.

## Room "(All)" umbrella keys

Each umbrella is a single key that shows as **one line** on a tech tooltip but applies its
multiplier to **every** matching room (instead of you listing each room separately). It cascades to
whatever matching `ROOM_*` boostables exist at load — including rooms added by other mods.

| Key | Display | Cascades to (vanilla) |
|---|---|---|
| `ROOM_MINE_ALL` | Mines (All) | Claypits, Coal Mines, Gem Mines, Ore Mines, Sithilon Mines, Stone Mines (`ROOM_MINE_*`) |
| `ROOM_WORKSHOP_ALL` | Workshops (All) | Bowyers, Carpenters, Jewellers, Masonries, Mechanics, Papermakers, Potteries, Rationmakers, Smithies, Tailors (`ROOM_WORKSHOP_*`) |
| `ROOM_FARM_ALL` | Farms (All) | Cotton, Fruit, Grain, Herb, Mushroom, Opiate, Vegetable farms (`ROOM_FARM_*`) |
| `ROOM_REFINER_ALL` | Refineries (All) | Bakeries, Breweries, Charcoalers, Metal Smelters, Weavers (`ROOM_REFINER_*`) |

> The umbrella applies multiplicatively to each room. When the umbrella isn't boosted by any tech it
> is a no-op (the per-room production breakdown may show a neutral `×1.0` line for it).

## World keys

| Key | Display | Category | Effect |
|---|---|---|---|
| `WORLD_PLUNDER` | Raid Plunder | World | Multiplies the **resources your armies plunder while raiding** enemy territory. Vanilla still loots its full amount; this delivers the extra `(value − 1)×` as supplemental spoils. Scoped to the **raid action only** — battle-victory and conquest spoils are unaffected. Not to be confused with vanilla `CIVIC_RAIDING` ("Raid Security"), which lowers the chance of *being* raided. Renamed 2026-07-04 from `CIVIC_PLUNDER` (moved from the Civics group to the World group; now `TYPE_WORLD`). |
| `WORLD_PRODUCTION_SLAVE_ALL` | Production: Slaves (All) | World: Production | **Umbrella** over the vanilla per-race `WORLD_PRODUCTION_SLAVE_<RACE>` region-output keys — one tooltip line that multiplies the production of slaves of **every race** at once. Children are discovered dynamically, so new races are auto-included. The hidden per-race `_YEARLY` display-derivative variants are **excluded**. Same cascade mechanism as the `ROOM_*_ALL` umbrellas. |

## Civic keys

| Key | Display | Category | Effect |
|---|---|---|---|
| `CIVIC_INDOCTRINATION` | Indoctrination | Civics | Multiplies the **effectiveness of indoctrinating subjects** — i.e. how quickly subjects whose **(class, race) education policy** is `INDOCTRINATION` gain the `INDOCTRINATION` stat at **universities**. Implemented as a multiplicative factor on each university's learning-speed (`bonus()`) boostable, active only for subjects currently on the INDOCTRINATION policy (plain-education subjects are unaffected). Universities only — schools have no `bonus()` factor. |

> **University-scoped.** Indoctrination is also gained by children in **Schools**, but schools compute
> learning speed with no boostable factor, so school (child) indoctrination is **not** boosted — only
> universities (adult indoctrination). The indoctrination policy itself is toggled per-race on the
> school/university room UI; this key only amplifies it, it does not enable it.

## Battle keys

| Key | Display | Category | Effect |
|---|---|---|---|
| `BATTLE_FEAR` | Fear | Battle | A **per-division aura**: a division projecting fear lowers the **morale** (`BATTLE_MORALE`) of nearby **enemy** divisions, with the penalty falling off linearly with distance and capped/floored (can't reduce morale below `×0.4` by default). A division's fear is its soldiers' `BATTLE_FEAR` value. Base value is `0`. |

> Grant fear via a **race** `BOOST:` block using `>ADD` (not `>MUL` — the base is 0, so `0 × n = 0`):
> ```
> BATTLE_FEAR>ADD: 0.6,
> ```
> Tuning (aura range, distance falloff, morale floor) lives in named constants in `MainScript.java`.

## Stat keys (`STAT_*` prefix)

`STAT_*` keys scale a value from a race's `STATS:{}` block, applied uniformly across all population
classes (Child/Citizen/Slave/Noble).

| Key | Display | Effect |
|---|---|---|
| `STAT_WORK_RETIREMENT` | Retirement Desire | Multiplies how much **retirement contributes to subjects' fulfillment** (a net boost: retirement satisfaction counts for more without penalizing subjects who lack it). |

> `STAT_WORK_RETIREMENT` *multiplies* the existing per-class retirement weight, so a race that places
> zero value on retirement stays at zero (0 × value = 0) — it amplifies existing desire rather than
> creating it.

## Need-rate keys (`RATES_*` prefix)

| Key | Display | Category | Effect |
|---|---|---|---|
| `RATES_NATURE` | Piety (Nature) | Service Needs | The rate at which the **Worship (Nature)** need increases daily. Subjects fulfill it by worshipping at **Nature Monuments or natural trees**, which grants **Piety (Shrine)** fulfillment. Centered at `1.0` (a per-subject multiplier of that desire). **Water is not nature.** |

> **Semantics — centered at 1.0, not 0.** Unlike the other keys here, `RATES_NATURE` is a *multiplier of desire*, read per-subject as `affinity = value − 1`:
> - **`>1` — loves nature.** Being near nature raises shrine piety ∝ `affinity × proximity`. Any nature-lover also builds a **desire** to seek nature while away from it (faster the higher the value); when that desire crosses a threshold, an idle citizen will **walk to nearby nature and linger** there (a "pilgrimage"), which sates the desire — so they self-pace rather than constantly wandering off. A **population-scaled cap** (a small % of the city, not a fixed number) bounds how many pilgrimage at once.
> - **`<1` — shuns nature.** Gains no piety from nature (v1 makes aversion a non-gain, not an active penalty).
> - **`=1` — neutral (default).** Inert; nothing happens.
>
> **Author it on a race (or tech) with `>MUL`**, since the base is `1.0`:
> ```
> RATES_NATURE>MUL: 2.0,   // this race loves nature
> RATES_NATURE>MUL: 0.5,   // this race is averse
> ```
> The computed value is **floored just above 0** (min `0.01`), so `RATES_NATURE>MUL: 0` can never drive it to 0 (avoids the engine's unguarded divide-by-zero tooltip math).
>
> **It is NOT an engine "need".** The game's need/service/AI-plan system is sealed to mods, so nothing in the engine reads this key. All behaviour — the proximity reward and the pilgrimage — is a **mod-owned per-tick routine** (the `WORLD_PLUNDER` pattern) in `MainScript`. Consequences:
> - Nature is **`MONUMENT_NATURE`** (its harmony spread, read O(1)) **+ wild forest trees** (a mod-maintained proximity map, rebuilt periodically).
> - The reward is written to each subject's **shrine** religious-service satisfaction. The vanilla shrine AI re-clears that for subjects who actively seek a shrine and find none, so the nature-piety is **durably sticky only for subjects without shrine access** (a known interplay; a dedicated "nature religion" is the clean long-term fix).
> - **Pilgrimage (Stage 2) is gated** by a master switch (`NATURE_PILGRIMAGE_ENABLED` in `MainScript`) plus a concurrency cap and a per-episode watchdog; it commandeers only genuinely idle citizens and always releases them, so real needs are never starved. Flip the switch off to run the passive proximity reward alone.

## Class keys (`CLASS_*` prefix) — ⚠️ SHELVED 2026-07-04 (not in current build)

> **These keys are not registered in the current jar.** The feature was confirmed working in-game
> (CLASS_CITIZEN need-rates + CLASS_CITIZEN_MINE all-mine output), then shelved for a later update — all
> `CLASS_*` code is commented out in `MainScript.java`. The section below documents the design for when it
> is re-enabled (uncomment the five `CLASS_*`-marked blocks).

`CLASS_<CLASS>[_<ROOMTYPE>]` keys ("Class Treatment") multiply a curated set of per-subject stats
**only for player-city subjects belonging to that population class** (`HCLASS`). Default `1.0`, and the
applied multiplier is **clamped to [0.5, 1.5]** (so `>MUL: 1.5` is the max useful boost; the engine has
no value cap, so the clamp is what enforces the range). The in-game name follows `<ClassName-plural>
(<aspect>)` — the bare per-class key is `(Needs)`, a room-typed key is the room's gerund (e.g. `(Mining)`).

**Every** `CLASS_<CLASS>*` key multiplies that class's three **need-growth rates** — `RATES_HUNGER`,
`RATES_THIRST`, `RATES_SHOPPING` (higher = the class gets hungry/thirsty/shopping-hungry *faster*;
this rising neediness is the intended trade-off "cost" of better treatment). A **room-typed** variant
`CLASS_<CLASS>_<ROOMTYPE>` *additionally* multiplies that class's **output across every room of the
type**, via the matching `ROOM_<ROOMTYPE>_ALL` umbrella.

| Key | Display | Applies to | Multiplies |
|---|---|---|---|
| `CLASS_CITIZEN` | Plebeians (Needs) | Citizen-class subjects | `RATES_HUNGER`, `RATES_THIRST`, `RATES_SHOPPING` |
| `CLASS_CITIZEN_MINE` | Plebeians (Mining) | Citizen-class subjects | the three rates above **+** all Mine output (`ROOM_MINE_ALL` → every `ROOM_MINE_*`) |

> **Roadmap.** `CLASS_CITIZEN` + `CLASS_CITIZEN_MINE` are live. `CLASS_CITIZEN_FARM` / `_REFINER` /
> `_WORKSHOP` (each = the three rates + its `ROOM_<TYPE>_ALL`) and then the other classes
> (`CLASS_SLAVE*`, `CLASS_NOBLE*`) are one-liners in `registerClassKey`, staged after in-game validation.
>
> **How it works.** A conditional multiplicative `Booster` is attached to each target boostable; it
> returns the clamped key value only for subjects whose class matches (via the subject's `Induvidual` at
> the engine's per-subject read-point) and a neutral `1.0` for everyone else — same shape as
> `ROOM__SLAVER`. For the room target the booster sits on the `ROOM_<TYPE>_ALL` umbrella, whose cascade
> carries the per-class factor into each room's per-employee bonus read.
>
> **Stacking.** The three rate targets are shared, so if you boost both `CLASS_CITIZEN` and a room-typed
> `CLASS_CITIZEN_<ROOMTYPE>`, a Citizen's need-rates are multiplied by *both* factors (each still clamped
> to [0.5, 1.5] individually).
>
> **Zero-multiply safety-net.** Any target boostable whose base value is `0` is skipped (a `×` on a
> zero base is a no-op and would feed the game's known unguarded divide-by-zero tooltip/progress math),
> and the [0.5, 1.5] clamp means the factor can never turn a value into `0` (or a `0` into non-zero).

Grant via a tech `BOOST:` block, e.g. `CLASS_CITIZEN>MUL: 1.25` or `CLASS_CITIZEN_MINE>MUL: 1.4`.

## Requirement keys (`REQUIRES:` blocks — GVALUES, not boostables)

These are **faction GVALUES** (the registry tech `REQUIRES:` blocks query), not `BOOST:` boostables.
They reinstate the pre-v71 meaning of the vanilla population-fraction value.

| Key pattern | Value |
|---|---|
| `POPULATION_<RACE>_<CLASS>_OFCLASS_F` | Fraction of the given population **class** that is the given **race** — `STATS.POP().POP.data(class).get(race) / .get(null)`. Registered for every race × player class (CITIZEN, NOBLE, SLAVE, …). |

> **Why this exists.** In v70 `POPULATION_<RACE>_<CLASS>_F` meant "fraction of that *class* which is this
> race." In v71 the vanilla key's denominator silently changed to the **whole population**, so any noble —
> even a same-race noble (nobles are class NOBLE, not CITIZEN) — drops `POPULATION_<RACE>_CITIZEN_F` below
> `1.0` and breaks `EQUAL: 1.0` "monorace" requirements. Use the `_OFCLASS_F` key for the original behaviour.

Example (tech `REQUIRES:` block — "100% of citizens are Tilapi, nobles irrelevant"):

```
REQUIRES: {
    EQUAL: {
        POPULATION_TILAPI_CITIZEN_OFCLASS_F: 1.0,
    },
},
```

## Tech-scoping keys (`TARGET_RACE`, `TARGET_CLASS`) — tech-node keys, not boostables

These are **not boostables**. They are extra keys you put on a **TECH node** itself (alongside its
`BOOST:` block) to **restrict that whole tech's boosts to a subset of subjects**. Without them, a tech's
`BOOST:` applies to every subject in the city; with them, the same boosts affect only the subjects that
match. (This machinery was moved into this mod from the former standalone *target-race-tech* mod and
generalized — `TARGET_RACE` behaves exactly as before, and `TARGET_CLASS` is the new class analog.)

| Key | Accepted values | Restricts the tech's boosts to… |
|---|---|---|
| `TARGET_RACE` | any race key present under `assets/init/race/` (e.g. `CRETONIAN`) — discovered dynamically, so race mods work | subjects of that **race** |
| `TARGET_CLASS` | `CITIZEN`, `NOBLE`, `SLAVE`, `OTHER`, `EXSLAVE` | subjects of that **population class** |

**`EXSLAVE` is synthetic.** The engine has no ex-slave class; a freed slave becomes a `CITIZEN`-class
subject tagged with the arrival cause `EMANCIPATED` ("Subjects that are freed slaves"). `TARGET_CLASS:
EXSLAVE` matches exactly those subjects (arrival cause `EMANCIPATED`), regardless of current class. Note
this is **freed slaves**, not `PAROLE` (which the engine documents as pardoned *prisoners*).

**Both may be combined on one tech** — the subject must match **all** constraints (logical AND). E.g.
`TARGET_RACE: CRETONIAN` + `TARGET_CLASS: SLAVE` affects only Cretonian slaves.

```
TECHS: {
    MY_TECH: {
        ...
        TARGET_CLASS: NOBLE,        // this tech's boosts apply to nobles only
        BOOST: {
            RATES_SHOPPING>MUL: 0.8,
        },
    },
},
```

> **How it works.** At load the mod scans every tech file for these keys (before the engine parses them,
> so the custom keys never trip the "unknown key" warning). For each flagged tech it pulls the tech's
> `BOOST` specs out of the global tech aggregator and reinstalls each as a **filtered booster** on the
> same boostable: the booster returns the vanilla per-level effect for matching subjects and a neutral
> identity (`×1` / `+0`) for everyone else and for non-subject boost targets. The tech scales with the
> player's tech level exactly as a normal tech would. The tech-tree tooltip still shows the full effect
> list (the originals are re-added for display after aggregation). See `your.mod.targetfilter`.
>
> **Only subject-facing boostables are actually narrowed.** The filter can only distinguish per-subject
> (`Induvidual`) boost targets. A boost aimed at a region/faction/division target has no subject to test,
> so it yields the neutral identity under a target key — pair `TARGET_*` with per-subject boostables
> (need-rates, room per-employee output, submission, etc.) for it to do something.

---

## Tooltip coloring for "Low-Positive" effects

Not a key — a display fix. In tooltips the game colors a boost **green** when it raises a boostable
(`>ADD` positive / `>MUL` > 1) and **red** when it lowers it. That is backwards for effects where a
**lower** value is the good outcome for the player (call these *Low-Positive* effects), e.g.
`PHYSICS_SOILING` ("Soiling" — lower means less filth). For those, this mod **flips green ↔ red** so a
booster that *lowers* the effect shows green and one that *raises* it shows red. The displayed number
is never changed — only the color.

- The list of inverted keys lives in `LowPositiveColors.KEYS` (in
  `src/main/java/your/mod/boostcolor/LowPositiveColors.java`). It ships with `PHYSICS_SOILING`; add
  more vanilla "lower is better" keys there.
- The coloring is hardcoded in the engine with no modding seam, so this is done by a self-attaching
  Java agent that rewrites the relevant tooltip methods at load (`your.mod.boostcolor.ColorAgent`).
  It is fully guarded: if the JVM blocks self-attach, tooltips stay vanilla (the log prints a
  `-javaagent:…` fallback) and the rest of the mod is unaffected.

## Usage example (tech file `BOOST:` block)

```
BOOST: {
    ROOM_MINE_ALL>MUL: 1.25,
    ROOM_REFINER_ALL>MUL: 1.15,
    WORLD_PLUNDER>MUL: 2.0,
    CIVIC_INDOCTRINATION>MUL: 1.5,
    STAT_WORK_RETIREMENT>MUL: 1.5,
},
```
