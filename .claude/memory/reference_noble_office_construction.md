---
name: noble-office-construction
description: How GAME.NOBLE().OFFICES is built (v71.19, verified) + the adopted assumption that v71.44 is unchanged; the race/nobility data files are NOT the office source
metadata:
  type: reference
---

Context for the `CLASS_NOBLE_*` "Class Treatment" keys (project memory [[project-sos-extended-boostables]] #8) and their `registerNobleOffices` / `NobleOfficeBooster` in `MainScript.java`.

## Adopted assumption (2026-07-31, user decision — CONFIRM LATER)

**v71.44 constructs noble offices the same way as v71.19** — an incremental `.19→.44` bump is unlikely
to have reworked office construction. So we treat `GAME.NOBLE().OFFICES` as the **code-generated,
room-derived** set below, and treat the `assets/init/race/nobility/` + `assets/text/race/nobility/`
data files as **NOT the office source**. **Confirm** when v71.44 source is available (or from the mod's
runtime log / in-game office titles — see bottom).

## v71.19 facts (VERIFIED from source at `../../sos-scripting-template/.claude/game-source-v71.19/`)

- `NOBLES.OFFICES = NobleOfficeUtil.make()` (`game/nobility/NOBLES.java:63`). Offices are built **in
  code**, not loaded from data files.
- `NobleOfficeUtil.make()` (`game/nobility/NobleOfficeUtil.java:34`) iterates the room registries —
  `SETT.ROOMS().MINES`, `.WOOD_CUTTER`, `.FARMS`, `.ORCHARDS`, `.PASTURES`, `.FISHERIES`, `.REFINERS`,
  `.WORKSHOPS` — and makes **one `NobleOffice` per industry room-type**: `target =
  room.industries().get(0).bonus()` (that room's `ROOM_<TYPE>_<VARIANT>` boostable), `name = "Master of
  <RoomName>"`, `add = 2.0`, `value(slots) = clamp(slots*workers/employed, 0, 1)`.
- Plus **2 special offices** (`special=true`): **Governor** → `BOOSTABLES.CIVICS().GOV` (key `CIVIC_GOV`,
  base 5, `leavesMap()=true`) and **Diplomat** → `BOOSTABLES.CIVICS().DIPLOMACY` (key **`CIVIC_DIPLOMACY`**
  — `make("DIPLOMACY",…)` under the `CIVIC` collection; **base 0.0**; the "Emissary"/"EMISSARY" string is
  only its display-name key, NOT the boostable key. v70.32 used key `EMISSARY`; do not use that on v71).
- `NobleOffice` is **single-target** (`game/nobility/NobleOffice.java:14`): public final `target`
  (Boostable), `add` (double), `name`/`desc`, `boosts` (a `BoostSpecs` holding just that one target),
  `index`, `special`, `value(int slots)`, `room()`. Contribution to its target ≈ `add *
  clamp(value(allocations),0,1)`.
- The `game.nobility.Init` stub (`Init.java`) has `pData`/`pText` pointing at the `race/nobility` folders
  but is **never instantiated/used** (zero references anywhere in the tree), and **no other v71.19 code
  constructs the `race/nobility` path** — an exhaustive tree grep found that path only inside this dead
  stub. → the data files are read by nothing in v71.19.
- Cross-checked the generic race loaders: `RACES`/`ExpandInit` load races from **named top-level files**
  in `race/` (`for (String s : files) new Race(s, p.gets(s), …)`), so the `nobility/` subfolder is not
  swept in as race data either. Verification is exhaustive, not inferred.

## Confidence & caveat

**v71.19: HIGH confidence** — the office-building path is fully traced (`NobleOfficeUtil.make()` →
room-generated) and the data files provably have no consumer.

**But one honest yellow flag against the "v71.44 == v71.19" assumption:** the dead `game.nobility.Init`
stub points at *exactly* these two data folders — it reads like the **skeleton of a not-yet-wired
file-based office loader**. Combined with the shipped, curated, multi-target office files, that's two
independent signals that a file-defined office system was *planned* and may have been *wired up* in a
later patch. So the assumption is more than a rubber-stamp: if `.44` implemented that loader, offices
become the 18 curated named offices and the room-generated model (and any `CLASS_NOBLE_*` realignment
built on it) would be wrong. Cheapest decisive check: the mod's `registerNobleOffices` runtime log, or
the in-game office titles ("Master of <Room>" per room = room-generated; "Narcocrat"/"Master Builder" =
file-defined).

## The contradiction being deferred

The `race/nobility` data files (in the shipped v71 data folder) define **18 curated, *named*,
*multi-target* offices** grouped MINES/FARM/INDUSTRY/PASTURE by `COLOR:` — e.g. "Master of Mines"
(`ROOM_MINE*`), "Narcocrat" (spices+submission), "Master of The Feast" (brewery+bakery+ration),
"Slavemaster" (`BEHAVIOUR_SUBMISSION` only), "Master Builder" (carpenter+stockpile+maintenance), "Master
Baiter" (fisheries), … These do **not** match the room-generated runtime model above (single-target,
`"Master of <Room>"` per room). Under the adopted assumption they are treated as vestigial/legacy (or
serving some non-office purpose). **This is the thing to confirm.**

## Implication for the mod (holds under the assumption)

`registerNobleOffices` reads `GAME.NOBLE().OFFICES`, assumes a single `office.target`, and groups by
`office.target.key` via `nobleOfficeCategory` (→ `CLASS_NOBLE_<CATEGORY>` + `CLASS_NOBLE_ALL`). That
matches the room-generated, single-target model, so it is structurally valid under the assumption. Note
`nobleOfficeCategory` currently has no case for the Diplomat office's `CIVIC_EMISSARY` target (→ generic
`OFFICE`; and that boostable is base-0). Any `CLASS_NOBLE_*` rename should therefore target the
**room-generated** office set (per-room offices grouped by target category, + Governor + Diplomat), **not**
the data-file office names.

## How to confirm later (any one is decisive)

1. Read v71.44 `game/nobility/NobleOfficeUtil.java` + `NOBLES.java` (and check whether anything now reads
   `race/nobility/*.txt`).
2. Read the mod's `registerNobleOffices` runtime log: `"CLASS_NOBLE offices: N category key(s) …
   attached to X of Y office(s)"` and any `"unmapped office target '<key>'"` lines reveal the real
   `GAME.NOBLE().OFFICES` contents.
3. In-game: appointing a noble shows **"Master of <Room>"**-style per-room titles (→ confirms) vs
   **"Narcocrat"/"Master Builder"**-style curated titles (→ refutes; system is file-defined).

See [[project-sos-extended-boostables]] (#8) and [[v71-migration]].
