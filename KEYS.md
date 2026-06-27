# Extended Boostables — added boostable keys

All keys this mod registers, for use in tech files (and anywhere the game consumes boostables).
Reference them in a tech's `BOOST:` block, e.g. `ROOM_MINE_ALL>MUL: 1.5` or `CIVIC_PLUNDER>ADD: 0.5`.
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

## Civic keys

| Key | Display | Category | Effect |
|---|---|---|---|
| `CIVIC_PLUNDER` | Raid Plunder | Civics | Multiplies the **resources your armies plunder while raiding** enemy territory. Vanilla still loots its full amount; this delivers the extra `(value − 1)×` as supplemental spoils. Scoped to the **raid action only** — battle-victory and conquest spoils are unaffected. Not to be confused with vanilla `CIVIC_RAIDING` ("Raid Security"), which lowers the chance of *being* raided. |
| `CIVIC_INDOCTRINATION` | Indoctrination | Civics | Multiplies the **effectiveness of indoctrinating subjects** — i.e. how quickly subjects whose race is on the **indoctrination policy** gain the `INDOCTRINATION` stat at **universities**. Implemented as a multiplicative factor on each university's learning-speed (`bonus()`) boostable, active only for races currently being indoctrinated (education-only races are unaffected). |

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
    CIVIC_PLUNDER>MUL: 2.0,
    CIVIC_INDOCTRINATION>MUL: 1.5,
    STAT_WORK_RETIREMENT>MUL: 1.5,
},
```
