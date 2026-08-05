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
| `SLAVE_PRODUCTION_ALL` | Captives (All) | Captives | **Umbrella** over the vanilla per-race `SLAVE_PRODUCTION_<RACE>` ("Captives: X") keys — one tooltip line that multiplies a region's ability to produce captives of **every race** at once. Registered into the engine's own *Captives* category, so it groups with its children rather than making a near-duplicate group. Children are discovered dynamically (races added by other mods are auto-included), and this family has no `_YEARLY` derivatives, so nothing is excluded. |

> ### ⚠️ `SLAVE_PRODUCTION_ALL` vs `WORLD_PRODUCTION_SLAVE_ALL` — two different things
>
> The game ships **two** similarly-named per-race slave families, and this mod now has an umbrella over
> each. They are easy to mix up, so:
>
> | Umbrella | Children | What the children mean |
> |---|---|---|
> | `SLAVE_PRODUCTION_ALL` | `SLAVE_PRODUCTION_<RACE>` — *"Captives: Humans"* | A region's **ability/target for producing captives** of that race (`Recipes.boostsSlave`). |
> | `WORLD_PRODUCTION_SLAVE_ALL` | `WORLD_PRODUCTION_SLAVE_<RACE>` — *"Production: Humans"* | The per-region **daily slave production output** (plus hidden `_YEARLY` variants). |
>
> Both are multiplicative over base `1`, so author either with `>MUL`.

## Civic keys

| Key | Display | Category | Effect |
|---|---|---|---|
| `CIVIC_INDOCTRINATION` | Indoctrination | Civics | Multiplies the **effectiveness of indoctrinating subjects** — i.e. how quickly subjects whose **(class, race) education policy** is `INDOCTRINATION` gain the `INDOCTRINATION` stat at **universities**. Implemented as a multiplicative factor on each university's learning-speed (`bonus()`) boostable, active only for subjects currently on the INDOCTRINATION policy (plain-education subjects are unaffected). Universities only — schools have no `bonus()` factor. |
| `CIVIC_VASSAL_OPINION` | Vassal Loyalty | Civics | Adds **opinion points to factions that are currently your vassals**, and to nobody else. Every other faction — including one you are still negotiating vassalage with — is completely unaffected. Base value is `0`. |

> **University-scoped.** Indoctrination is also gained by children in **Schools**, but schools compute
> learning speed with no boostable factor, so school (child) indoctrination is **not** boosted — only
> universities (adult indoctrination). The indoctrination policy itself is toggled per-race on the
> school/university room UI; this key only amplifies it, it does not enable it.

### `CIVIC_VASSAL_OPINION` — "Vassal Loyalty"

Grant it from a tech (or race) `BOOST:` block with **`>ADD`**:

```
CIVIC_VASSAL_OPINION>ADD: 3,
```

> **Use `>ADD` only.** The base value is `0`, so `>MUL` parses fine but does nothing (`0 × n = 0`).
> **Magnitude:** vanilla `CIVIC_OPINION`'s base is `1.5` and the vassal stance's reference opinion is
> `6`, so **2–5** is the sensible band. Because vassals carry a hardcoded ×0.5 trust penalty (below), the
> effective *trust* gain is roughly **half** the opinion you add.

**What it actually does — read this before writing tooltip/mod-page copy.** It does **not** stop a vassal
from leaving the stance: no opinion threshold does that. The engine degrades only Trade, Pact and Alliance
stances when opinion falls; there is no vassal branch, and the vassal stance's minimum opinion is checked
only when the vassalage deal is first *proposed*.

What it does is **keep their trust above the point where they'd turn on you**:

1. Opinion feeds **trust** at roughly 1:1.
2. Vassals carry a **hardcoded ×0.5 trust penalty** — the engine models vassalage as breeding resentment,
   so your vassals are half as trustworthy as anyone else at the same opinion. This key is the direct
   counterweight to that penalty.
3. A faction at **trust ≥ 1 will not move to war against you**, and trust shifts attack likelihood either
   way below that.

Side effects of raising real opinion (rather than trust alone): better gift/deal drafting and emissary
targeting with that vassal, and more headroom to decline their tribute before it costs you.

> **It will not help you *acquire* vassals.** The bonus is gated on "this faction's overlord is the
> player", so it is inert on anyone you're still negotiating with. Use vanilla `CIVIC_OPINION` to reach
> vassalage and `CIVIC_VASSAL_OPINION` to hold it — cleanly separable techs.

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

## Class keys (`CLASS_*` prefix)

`CLASS_<CLASS>[_<ROOMTYPE>]` keys ("Class Treatment") multiply a curated set of per-subject stats
**only for player-city subjects belonging to that population class** (`HCLASS`). Default `1.0`, and the
applied multiplier is **clamped to [0.5, 1.5]** (so `>MUL: 1.5` is the max useful boost; the engine has
no value cap, so the clamp is what enforces the range). The in-game name follows `<ClassName-plural>
(<aspect>)` — the bare per-class key is `(Contentment)`, a room-typed key is the room's gerund (e.g. `(Mining)`).

> **⚠️ Polarity flipped 2026-07-30 — re-check any content using a bare `CLASS_<CLASS>` key.** The bare
> per-class key used to **multiply** the need rates, so `>MUL: 1.1` made that class 10% *needier* — a pure
> cost that the game nonetheless colored blue (it picks the color from the number alone). It now
> **divides** them: `>MUL: 1.1` makes that class 10% *calmer*. Higher is now better, matching the color.
> **Key strings are unchanged**, so nothing breaks by reference — only the direction of the effect. To
> author the old "spoiled" cost, use a value **below 1** (`CLASS_CITIZEN>MUL: 0.9`). Room-typed keys are
> unaffected (they were always high-positive). The only content affected was
> `sos-cac_addon-aruan` (`RACE_ARUAN.txt:82,191`, `CLASS_NOBLE>MUL: 1.1`/`1.15`), which sat among that
> tech's other buffs and now finally does what it looks like — **no edit needed there**.

> **Exception — `CLASS_NOBLE` (contentment) is uncapped on the high end** (2026-07-29): its multiplier keeps
> the `0.5` floor but has **no upper cap**, so `CLASS_NOBLE>MUL` scales without limit — and since the key now
> *divides* the need rates, an unbounded value drives noble needs arbitrarily close to zero. Every other Class key
> (all other need keys, the room-output keys, and the noble-office keys below) keeps the `[0.5, 1.5]` band.

> **`TARGET_RACE` / `TARGET_CLASS` are respected.** These keys read their value with the affected
> **subject's** `Induvidual`, so a filtered grant (e.g. a tech with `TARGET_RACE: HUMAN` granting
> `CLASS_CITIZEN_MINE>MUL:1.5`) only lifts subjects that pass the filter — here, only Human citizen miners.
> For the noble-office keys the filter is matched against the **office-holding noble** (see that section).

There are two kinds of key, with **disjoint** effects:
- A **bare per-class** key `CLASS_<CLASS>` ("(Contentment)") **divides** that class's three **need-growth
  rates** — `RATES_HUNGER`, `RATES_THIRST`, `RATES_SHOPPING` (higher = the class gets
  hungry/thirsty/shopping-hungry *slower*). Author a value **below 1** to impose the "spoiled class"
  neediness cost instead.
- A **room-typed** key `CLASS_<CLASS>_<ROOMTYPE>` multiplies **only** that class's **output across every
  room of the type**, via the matching `ROOM_<ROOMTYPE>_ALL` umbrella. It does **not** touch the need
  rates — room-typed keys are a pure skill/output boost.

| Key | Display | Applies to | Multiplies |
|---|---|---|---|
| `CLASS_CITIZEN` | Plebeians (Contentment) | Citizen-class subjects | **divides** `RATES_HUNGER`, `RATES_THIRST`, `RATES_SHOPPING` |
| `CLASS_CITIZEN_MINE` / `_FARM` / `_REFINER` / `_WORKSHOP` | Plebeians (Mining/Farming/Refining/Crafting) | Citizen-class subjects | that room type's output (`ROOM_<TYPE>_ALL` → every `ROOM_<TYPE>_*`) |
| `CLASS_CITIZEN_ALL` | Plebeians (All Work) | Citizen-class subjects | output across **all four** room umbrellas (Mines + Farms + Refineries + Workshops) |
| `CLASS_SLAVE` | Slaves (Contentment) | Slave-class subjects | **divides** `RATES_HUNGER`, `RATES_THIRST`, `RATES_SHOPPING` |
| `CLASS_SLAVE_MINE` / `_FARM` / `_REFINER` / `_WORKSHOP` | Slaves (Mining/Farming/Refining/Crafting) | Slave-class subjects | that room type's output (`ROOM_<TYPE>_ALL` → every `ROOM_<TYPE>_*`) |
| `CLASS_SLAVE_ALL` | Slaves (All Work) | Slave-class subjects | output across **all four** room umbrellas (Mines + Farms + Refineries + Workshops) |
| `CLASS_NOBLE` | Nobles (Contentment) | Noble-class subjects | **divides** `RATES_HUNGER`, `RATES_THIRST`, `RATES_SHOPPING` |

> **Roadmap.** Live: full CITIZEN and SLAVE sets — `_MINE`/`_FARM`/`_REFINER`/`_WORKSHOP` + `_ALL`, plus the
> bare contentment key (2026-08-04). `CLASS_NOBLE` (contentment) + the per-**office** keys
> (`CLASS_NOBLE_<OFFICE>` + `CLASS_NOBLE_ALL`, see the Noble office keys section) — nobles have no per-*room*
> variants (they don't work rooms).
>
> **How it works.** A conditional multiplicative `Booster` is attached to each target boostable; it
> returns the clamped key value only for subjects whose class matches (via the subject's `Induvidual` at
> the engine's per-subject read-point) and a neutral `1.0` for everyone else — same shape as
> `ROOM__SLAVER`. For a room-typed key the booster sits on the `ROOM_<TYPE>_ALL` umbrella, whose cascade
> carries the per-class factor into each room's per-employee bonus read.
>
> **Needs vs. output are separate keys.** The bare `CLASS_<CLASS>` (needs) and the room-typed
> `CLASS_<CLASS>_<ROOMTYPE>` (output) target disjoint boostables, so boosting the room key raises skill
> **without** raising neediness. To apply the neediness trade-off, boost the bare key as well.
>
> **Zero-multiply safety-net.** Any target boostable whose base value is `0` is skipped (a `×` on a
> zero base is a no-op and would feed the game's known unguarded divide-by-zero tooltip/progress math),
> and the [0.5, 1.5] clamp means the factor can never turn a value into `0` (or a `0` into non-zero).

### Noble office keys (`CLASS_NOBLE_<OFFICE>` / `CLASS_NOBLE_ALL`)

Nobles don't work rooms — they hold **offices** (Governor, or "Master of \<building\>"). Each office adds
a contribution to a target boostable (a room's worker-skill `bonus()`, or `CIVIC_GOV` for the Governor).
These keys **scale that office's own contribution** by the usual [0.5, 1.5] clamp — they do **not** touch
base worker skill, tech, or race stats, only the noble's office effect.

- **`CLASS_NOBLE_<OFFICE-CATEGORY>`** — scales the effect of that category's offices. Display "Nobles (\<aspect\>)".
- **`CLASS_NOBLE_ALL`** — "Nobles (All Offices)", scales the effect of **every** office a noble holds.

Both stack multiplicatively (each clamped), so `CLASS_NOBLE_MINE × CLASS_NOBLE_ALL` both apply to a
Master-of-Mines office. Categories are discovered dynamically from the game's office list, so modded
offices are covered (an unrecognized office target falls into a generic `CLASS_NOBLE_OFFICE`).

| Key | Display | Scales |
|---|---|---|
| `CLASS_NOBLE_MINE` | Nobles (Mining) | Master-of-Mines offices |
| `CLASS_NOBLE_FARM` | Nobles (Farming) | Master-of-Farms offices |
| `CLASS_NOBLE_REFINER` | Nobles (Refining) | Master-of-Refiner offices |
| `CLASS_NOBLE_WORKSHOP` | Nobles (Crafting) | Master-of-Workshop offices |
| `CLASS_NOBLE_ORCHARD` | Nobles (Orchards) | Master-of-Orchard offices |
| `CLASS_NOBLE_PASTURE` | Nobles (Pastures) | Master-of-Pasture offices |
| `CLASS_NOBLE_FISHERY` | Nobles (Fishing) | Master-of-Fishery offices |
| `CLASS_NOBLE_WOOD` | Nobles (Woodcutting) | Master-of-Wood-Cutter office |
| `CLASS_NOBLE_EMBASSY` | Nobles (Diplomacy) | Master-of-Embassy office |
| `CLASS_NOBLE_LIBRARY` | Nobles (Libraries) | Master-of-Library office |
| `CLASS_NOBLE_LABORATORY` | Nobles (Laboratories) | Master-of-Laboratory office |
| `CLASS_NOBLE_ADMIN` | Nobles (Administration) | Master-of-Administration office |
| `CLASS_NOBLE_GOVERNOR` | Nobles (Governing) | Governor office (`CIVIC_GOV` gov-points output) |
| `CLASS_NOBLE_ALL` | Nobles (All Offices) | every office, all categories |

> **How it works.** Each office gets two additive `NobleOfficeBooster`s on the office's target boostable
> (player-faction only) whose sum makes the net office contribution `officeContribution × factor`, where
> `factor` is the allocation-weighted average, across the office's holders, of `techBoost × contentment`
> read **per holder** (`techBoost = clamp(CLASS_NOBLE_<CAT>) × clamp(CLASS_NOBLE_ALL)`). Exact for buffs
> (`>MUL ≥ 1`); a close approximation for deflation combined with a multiplier on the same room bonus.
>
> **Where you see it.** The noble *panel* shows the office's base `value×add` and can't reflect these
> modifiers (it renders no boosters). The effect and its two itemised lines — **"Noble Contentment"** (the
> discontent penalty) and **"Noble Class Boost"** (the tech boost) — appear in the **room's production-rate
> breakdown** (hover the room's output), where the game lists every skill modifier.
>
> **Office effect scales with each noble's contentment.** Nobles have no engine happiness/loyalty stat, so
> `contentment` is that noble's own **need-satisfaction** — the mean of `1 − level/max` over the three needs
> `CLASS_NOBLE` governs (hunger/thirst/shopping), mapped linearly onto **[0.5, 1.0]** (fully satisfied → ×1.0,
> chronically unmet → ×0.5). So a discontent noble runs their office worse **even with no tech** (a change
> from vanilla, where office output is contentment-independent). This is what makes the `CLASS_NOBLE`
> ("Contentment") key a **real cost**: authoring `CLASS_NOBLE>MUL:<1` speeds nobles' needs → they're
> satisfied less often → their offices weaken. (Off-map Governors aren't need-simulated → treated as fully content.)
>
> **`TARGET_RACE` / `TARGET_CLASS`.** Because the factor is read with each **office-holding noble's**
> `Induvidual`, a filtered grant only lifts the office effect of nobles that pass the filter (e.g.
> `TARGET_RACE: HUMAN` + `CLASS_NOBLE_MINE>MUL:1.5` → only Human nobles holding a mine office get the ×1.5;
> a mixed-race set of holders is scaled proportionally by their allocation share). **Caveat:** a Governor
> who has *left the map* can't be race-tested, so a race-filtered `CLASS_NOBLE_GOVERNOR` won't reach an
> off-map Governor (it falls back to the unfiltered value).
>
> **`CLASS_NOBLE_GOVERNOR` touches `CIVIC_GOV`** — the shared region gov-points currency (see the CaC × PrRR
> govbridge notes). Boosting it scales the gov-points a Governor noble generates.

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

> **Palette note.** The engine's "good" colour (`GCOLOR.T().IGOOD` / `IGREAT`) renders **blue** in this
> game's theme, not green — it has always been blue. "Bad" is red, and the neutral is an off-white.
> Docs here say blue/red for that reason.

Not a key — a display problem. In tooltips the game colors a boost **blue** when it raises a boostable
(`>ADD` positive / `>MUL` > 1) and **red** when it lowers it. That is backwards for effects where a
**lower** value is the good outcome for the player (call these *Low-Positive* effects), e.g.
`PHYSICS_SOILING` ("Soiling" — lower means less filth): a tech that genuinely helps reads as a penalty.

**The fix is a re-colour, and it needs no new keys.** Boost the **vanilla** key as normal
(`PHYSICS_SOILING>MUL: 0.75`, `RATES_HUNGER>MUL: 0.5`) — this mod flips the colour so lowering it reads
as the benefit it is.

How: the tech-node tooltip does **not** use the hardcoded `BoosterAbs.hover` colouring — it renders each
effect with `bb.booster.format(b.text(), v)`, a *virtual* call on the booster. `format` delegates the
number to `GFORMAT.f1`/`iIncr`, and those set the colour (`>1` blue, `<1` red, `==1` neutral). Since
`format` is public and non-final, this mod swaps in a display-only wrapper (`your.mod.boostformat`) for
selected keys and re-colours the line. The number is untouched, and because a tech's `BoostSpecs` has
`connect == false`, the swap cannot affect gameplay at all.

Current rules (`BoostFormats.RULES`):

| Keys | Mode | Why |
|---|---|---|
| `PHYSICS_SOILING` | `INVERTED` | rate at which subjects get dirty — lower is better |
| all 19 vanilla `RATES_*` keys | `INVERTED` | every one is a need-**growth** rate (`NEED.java:59`) — lower is better |
| `ACTIVITY_JUDGE` / `_MOURN` / `_PUNISHMENT` / `_SOCIAL` | `NEUTRAL` | idle-activity desire weights — neither good nor bad |

> The `RATES_*` entries are listed **individually, never by prefix** — the prefix is shared with
> `RATES_NATURE`, which is genuinely high-positive and must not be inverted.
>
> **This only reaches the tech node.** The Boosts browser and the boostable tooltips colour inline in
> `BoostSpecs.hover`/`BoosterAbs.hover`, which never call `format`; those would need bytecode patching
> and stay vanilla. Accepted trade-off (2026-07-30) — the tech node is where players read tech effects.
>
> The `CLASS_<CLASS>` keys were handled differently again: we own them, so their polarity was inverted
> **in place** (see the Class keys section) rather than re-coloured.

**Effect lists are ordered to match.** The same polarity table drives the order of a tech's effect list
(`BoostOrder`), so a line's position and its colour can never disagree:

1. additive benefits, 2. multiplicative benefits (strongest first), 3. **costs** — including a
low-positive key pushed *above* its neutral point, which sorts down with the negatives, 4. **neutral**
keys (`ACTIVITY_*`) at the very bottom.

Within a tier, entries are ranked by benefit magnitude measured *after* polarity, so an inverted `×0.5`
outranks an inverted `×0.9`. This replaces the standalone **`tech-boost-sort`** mod, which sorted by raw
sign and had no access to the polarity table — **do not load that jar alongside this one**, or the two
will fight over the same list.

**Not every backwards-looking key is one.** Verified high-positive despite the name: `ROOM_CONSUMPTION_*`
(the engine *divides* by it — its own tooltip calls it "Consumption Bonus"), `WORLD_PROXIMITY`(`_TOLL`),
`CIVIC_FURNITURE`, `WORLD_HEALTH`, and the vanilla `CIVIC_ACCIDENT`/`DEFLATION`/`MAINTENANCE`/`RAIDING`/
`SPOILAGE` set — those last five are the base game fronting its own low-positive values with
high-positive keys.

> **Removed 2026-07-30 — the front keys.** An earlier build shipped `PHYSICS_CLEANLINESS` plus 19
> `RATES_*` front keys (Satiety, Hydration, Frugality, …): high-positive keys that *divided* the vanilla
> low-positive one so the colour came out right. The re-colour above does the same job with no new keys,
> so they were dropped. If you have content referencing them, repoint it at the vanilla key and invert
> the value (`RATES_SATIETY>MUL: 2.0` → `RATES_HUNGER>MUL: 0.5`). Recoverable from git history.

> **Removed 2026-07-29 — the bytecode recolor agent.** Earlier versions shipped `your.mod.boostcolor`
> (`ColorAgent` + `LowPositiveColors`), a Javassist Java agent that rewrote the engine's hardcoded
> `IGOOD`/`IBAD` tooltip reads, since the coloring has no modding seam. It could never arm itself: the
> game's bundled JRE has **no `jdk.attach` module**, so self-attach is impossible, and the only way to
> load it was a per-user `-javaagent:` entry in `JVM_ARGS2` (`LauncherSettings.txt`) — a setting with no
> launcher UI. It was removed along with the shaded Javassist dependency, which cut the mod jar from
> ~880 KB to ~62 KB. Recoverable from git history if the engine ever gains a seam.

## Usage example (tech file `BOOST:` block)

```
BOOST: {
    ROOM_MINE_ALL>MUL: 1.25,
    ROOM_REFINER_ALL>MUL: 1.15,
    WORLD_PLUNDER>MUL: 2.0,
    SLAVE_PRODUCTION_ALL>MUL: 1.25,
    CIVIC_INDOCTRINATION>MUL: 1.5,
    CIVIC_VASSAL_OPINION>ADD: 3,
    STAT_WORK_RETIREMENT>MUL: 1.5,
},
```
