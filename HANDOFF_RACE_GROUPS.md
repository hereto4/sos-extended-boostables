# HAND-OFF — Race groups (`GAQUATIC`): `POPULATION_<GROUP>_*` requirement keys

**For:** a future Claude/dev session, **on the Windows build device**.
**Written:** 2026-11-13 (on the **Mac** — design device, cannot build).
**Mod:** `sos-extended-boostables`. Paths are relative to this mod's root unless marked otherwise. Engine
citations are against `sos-scripting-template/.claude/game-source-v71.44/`.

> ## ⚠️ IMPLEMENTED + TYPE-CHECKED — NOT BUILT, NOT TESTED
>
> Code written and `javac`-type-checked on the Mac against the in-repo v71.44 source (whole mod, exit 0,
> zero errors). The data file was parsed with the game's own `snake2d.util.file.Json`, and the cross-mod
> merge logic was exercised with a scratch harness. **No Maven build, no deploy, no in-game run yet.**
> Still to do: **Build** and the **In-game test plan** below.

---

## TL;DR

A **race group** is a named set of races that counts as one pseudo-race in the `POPULATION_*` faction
values that `REQUIRES:` blocks query. The first group is **`GAQUATIC`** = `VERATHID`, `KHELIAN`,
`THRYXARII`, `ONARI` (all third-party race mods; vanilla `Q_AMEVIA` deliberately excluded). The motivating
use, in a tech:

```
REQUIRES: {
    GREATERE: {
        POPULATION_GAQUATIC_CITIZEN_OFCLASS_F: 1,
    },
},
```

= 100% of citizens are aquatic, in any mix of the four races.

Groups are **data-driven**: any loaded mod may ship `V71/RaceGroups.txt`; EB merges all of them. EB ships
the `GAQUATIC` definition itself.

## What changed

### `src/main/java/your/mod/racegroup/RaceGroups.java` (new)

- `install()` scans `PATHS.currentMods()` for `<mod>/V<major>/RaceGroups.txt` — the same "any mod ships a
  file" idiom as `your.mod.update.UpdateNotifier` and `your.mod.stealth.StealthAlertnessConfig`.
- **Merge rules:** a group's `RACES` = union across files; `NAME` = from the first mod in
  `PATHS.currentMods()` that sets it, which is the highest-priority mod in the game's own asset overriding
  (`init/paths/VirtualFolder.getPossibleFile` searches bases in that order, vanilla last).
- **Per group `<G>`, 11 keys** pushed to `GVALUES.FACTION`: `POPULATION_<G>__F`, `__I`, and for each player
  class (NOBLE, CITIZEN, SLAVE) `_<CLASS>_F`, `_<CLASS>_I`, `_<CLASS>_OFCLASS_F`. The `_F`/`_I` mirror vanilla
  `settlement/stats/SValues.java` (`POP.tot` terms); `_OFCLASS_F` mirrors
  `MainScript.registerCitizenRaceFractions` (`POP.pop(cl, r) / POP.pop(cl, null)`).
- **Exactness:** each value is one division of *integer* head-count sums, never a sum of per-race fractions.
  `COMPARATOR.GREATERE` is an exact `a >= b`, so summed fractions could read 0.9999… at 100% and fail.
- **Robustness:** member races that aren't loaded are skipped and logged, so the keys always exist. A group
  whose key equals a loaded race key is skipped. Any key already present in `GVALUES.FACTION` is skipped
  (`GValueCat.push` throws on duplicates, which would abort init). Vanilla's keys are already registered at
  this point (`STATS` → `new SValues()` runs during `new SETT()`, before the script hook), so that check
  covers vanilla collisions too. A file with a syntax error is ignored whole; a malformed group is skipped
  alone.
- All diagnostics go to **stdout** (`[sos-extended-boostables] race groups: …`), never stderr, because the
  engine tees stderr into its error handler.

### `src/main/java/your/mod/MainScript.java`

One call, `your.mod.racegroup.RaceGroups.install();`, in `initBeforeGameInited()` directly after
`registerCitizenRaceFractions()`. It's the same window and the same reason: the keys must be in the map
before `init.finish()` → `GVALUES.finishSetup()` resolves every `REQUIRES` promise (techs, events, room
unlocks, titles).

### `src/main/resources/mod-files/RaceGroups.txt` (new)

Declares `GAQUATIC` (`NAME: "Aquatic"`). The pom's `copy-mod-files` step deploys it to **`V71/RaceGroups.txt`**
(the mod's `V71/` root, not under `assets/`).

### `KEYS.md`

New subsection "Race groups (`POPULATION_<GROUP>_*`)" under "Requirement keys".

---

## Build (Windows)

```
cd C:\Users\Nate\AppData\Roaming\songsofsyx\mods\sos-extended-boostables
<intellij-mvn> install -Dmaven.test.skip=true
```

- `mvn` is **not on PATH**, so use the IntelliJ-bundled one. **Never `mvn clean`.**
- A successful build **is** the deploy.
- **Verify the jar really contains the change** (a `BUILD SUCCESS` can ship stale bytecode):
  in Git-Bash, `unzip -l V71/script/sos-extended-boostables.jar | grep racegroup` must list
  `RaceGroups.class` and `RaceGroups$Def.class`.
- **Verify the data file deployed:** `V71/RaceGroups.txt` must exist after the build.

Compile risk is low. Everything was type-checked against v71.44, and the only engine API the new class uses
is already used elsewhere in EB (`POP`, `GVALUES.FACTION.push`, `HCLASSES`, `PATHS.currentMods()`, `Json`).

---

## In-game test plan

**Stdout matters:** the log lines are stdout-only, so launch from IntelliJ (`.run/Main`) to see them.
Load Extended Boostables plus as many of the four aquatic race mods as are available.

1. **Log — registration.** At game start there should be one line per group and a summary:
   `race groups: GAQUATIC "Aquatic (…)": 4/4 member races loaded; 11 keys; defined by [Extended Boostables].`
   and `race groups: registered 11 POPULATION_<GROUP>_* values for 1 group(s).`
   With fewer race mods loaded, expect `k/4 … (not loaded: [...])`, still with 11 keys.
2. **No boot warning.** No "no FACTION named …" popup, and no new entries in the error log.
3. **Values match per-race keys.** Turn on developer mode. The cog button on the top panel opens the debug
   panel; its `values` entry lists every faction value live (`init/value/GVALUES.java` `finishSetup`).
   Check that:
   - `POPULATION_GAQUATIC_CITIZEN_I` = the sum of `POPULATION_<RACE>_CITIZEN_I` over the loaded members;
   - `POPULATION_GAQUATIC_CITIZEN_OFCLASS_F` = the sum of the members' `POPULATION_<RACE>_CITIZEN_OFCLASS_F`.
4. **The motivating case (needs a test tech).** Temporarily give a tech in a locally enabled mod the
   `REQUIRES` from the TL;DR. Don't commit it.
   - City whose citizens are **all** one aquatic race: the requirement reads 100% and is met.
   - Add a **second** aquatic race (immigration): still 100% and met. **This is the core feature.**
   - One **non-aquatic** adult citizen: drops below 100% and fails.
   - Non-aquatic **nobles, slaves, children or prisoners** must *not* break it, because they aren't
     citizen-class, or aren't counted in `_OFCLASS_F`.
5. **Tooltip label.** Hovering the requirement shows "Plebeians: Aquatic (…)", listing the loaded member
   races, then `>=`, `100%`, and the current value.
6. **Missing member.** Disable one aquatic race mod and restart. Step 1 shows it under `not loaded`, the keys
   still exist, and there's no popup.
7. **Second game in one session.** Return to the main menu and start or load another game. There should be
   no crash (that would be a duplicate key), and step 1's lines appear again with the same counts. The
   `GVALUES` map is cleared per game, so re-registering is expected.

## Rollback

Delete the one `RaceGroups.install()` line in `MainScript` and nothing registers. Any tech file that
references a group key would then show the "no FACTION named" warning and lock, so revert those too.

## Not in scope (phase 2)

Making a group work wherever a race key works: `TARGET_RACE: GAQUATIC` (EB's own `your.mod.targetfilter`,
so feasible), `FACTION_IS_*`, `LOYALTY_*`, and race-specific boosts. A group is not a `Race`, so each of
those needs separate work.
