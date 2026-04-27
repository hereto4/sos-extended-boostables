---
name: sos-extended-boostables Project State
description: Current state and key decisions for the sos-extended-boostables mod project
type: project
originSessionId: f4a7dc19-9fa3-4a12-9315-2717447ae117
---
Adding new ROOM_*-style boostable keys to Songs of Syx v70.32 that don't exist in vanilla.
First target: `ROOM__SLAVER` for the Slaver room (`settlement.room.law.slaver.ROOM_SLAVER`).

**Why:** The boostable keys produced by this mod are consumed by technologies (handled externally by the user). This mod registers the key and applies the effect at runtime.

**How to apply:** File in `V70/assets/init/stats/boost/` named `__SLAVER.txt` with `CATEGORY: ROOM` produces key `ROOM__SLAVER`. Paired text file in `V70/assets/text/stats/boost/__SLAVER.txt`.

**Key naming insight:** `BOOSTING.push()` strips ONE leading `_`, then prepends the category prefix. File `__SLAVER` → strip `_` → `_SLAVER` → prepend `ROOM_` → `ROOM__SLAVER`.

**ROOM_SLAVER facts:**
- Internal key: `"_SLAVER"`, no existing boostable (bonus() returns null)
- Converts prisoners to slaves via `Enslaved` AI + `WorkSlaver` AI
- Station count determined by furniture; default workers = ceil(stations/4)
- No `Industry` hook — effect application requires a Java script

**Effect target decision (FINALIZED):** Option B — Per-Entity Submission Bonus.

The bonus applies a `BEHAVIOUR_SUBMISSION` modifier only to slaves that were processed through the Slaver room, not to trade slaves or born slaves.

**Implementation approach:**
- Uses vanilla per-entity `DataNibble("POP_ARRIVE")` accessed via `STATS.POP().COUNT.arrive.get(Induvidual)`, which stores the last arrival `CAUSE_ARRIVE` for each entity. Save-compatible.
- Slaver-room converts: `arrive == CAUSE_ARRIVES.PAROLE()` (set in `Enslaved.java:82` via `HTypeSet(HTYPES.SLAVE(), null, CAUSE_ARRIVES.PAROLE())`)
- Trade slaves: `arrive == CAUSE_ARRIVES.IMMIGRATED()` (set in `PeopleSpawner.java:151`)
- PAROLE on a SLAVE is unambiguous — all other PAROLE uses result in HTYPES.SUBJECT(), not slaves.
- Script maintains `processedRatio = parole_slaves / total_slaves`, recomputed every 4 seconds.
- Custom `BValue` (full interface, not BValuePlayerOnly): `vGet(Induvidual)` does per-entity check; `vGet(Player)` / `vGet(PopTime)` returns delta * processedRatio for uprising risk.

**SPEC.md** is the master reference document at project root.

**How to apply:** See `SPEC.md` in project root for file format, naming conventions, and implementation guide.
