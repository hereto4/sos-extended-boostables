---
name: v71-migration
description: sos-extended-boostables migrated from game v70.32 → v71.19; code break was BValue.PopTime removal; also documents v71's silent POPULATION_*_F GVALUE semantic change + the fix we shipped
metadata:
  type: project
---

Migrated **sos-extended-boostables** from Songs of Syx **v70.32 → v71.19** (done 2026-05-29). v71.19 was already staged in `.m2` (shared repo, from the sibling `sos-scripting-template` migration), so no manual jar-staging was needed — the normal build re-copies the installed `SongsOfSyx.jar` into `.m2/.../71.19/`.

**The single compile break for this mod:** `game.boosting.BValue` changed its interface.
- `BValue.PopTime` (inner class) was **removed** in v71 (commented out at `BValue.java:300-321`).
- A new abstract method `vGet(HCLASS_RACE reg)` (`init.type.HCLASS_RACE`) **replaces** it as the per-population-class query.
- Fix in `MainScript.registerSlaverEffect`'s anonymous `BValue`: deleted `import game.boosting.BValue.PopTime;` and the `vGet(PopTime)` override; added `import init.type.HCLASS_RACE;` and a `vGet(HCLASS_RACE)` override returning the same expression the old PopTime override used (`(roomSlaver.get(FACTIONS.player()) - roomSlaver.baseValue) * processedRatio`).
- v71 `BValue` abstract methods (all six must be implemented): `vGet(Region)`, `vGet(Induvidual)`, `vGet(Div)`, `vGet(HCLASS_RACE)`, `vGet(Player)`, `vGet(FactionNPC)`. (`vGet(Royalty)` and `vGet(Faction)` are defaults.)

**Everything else this mod uses verified unchanged in 71.19:** `BOOSTING.push(String,double,CharSequence,CharSequence,SPRITE,BoostableCat)`, `BOOSTING.MAP().tryGet(String)` (RMAP is `util.keymap.RMAP`, `tryGet` is public), `BOOSTABLES.ROOMS()`/`BEHAVIOUR().SUBMISSION`, `BoosterValue(BValue,BSourceInfo,double,boolean)` + `Booster.add(Boostable)`, `BSourceInfo(CharSequence,SPRITE)`, `Boostable.baseValue`/`get(BOOSTABLE_O)` (Player/Faction implement BOOSTABLE_O), `STATS.POP().COUNT.arrive.get(indu)`, `CAUSE_ARRIVES.PAROLE()`, `HTYPES.SLAVE()`, `SETT.ROOMS().CANNIBAL.icon`, `SETT.ENTITIES().getAllEnts()` (return type is `ENTETIES`), `RES_AMOUNT`/`RESOURCE.bIndex()`/`RESOURCES.ALL()`, `Race.resources()`.

**The NPC-tech-pipeline gutting that broke the sibling `ai-blocked` mod (see template's `V70_to_V71_changes.md`) does NOT affect this mod** — sos-extended-boostables operates on player-side room boostables + per-race resource wrapping, not `PTech.npcAmount`/`BoostCompound$Boo`. The `BOOSTING.waiting` package-private break also doesn't apply (this mod never touches `BOOSTING.waiting`).

**Build/deploy mechanics for the version bump:**
- `pom.xml`: `<version>` → 1.2.0 (sync to user's `_Info.txt`), `<game.version.major>` 70→71, `<game.version.minor>` 32→19. `<game.version.directory>` is `V${game.version.major}`, so the build auto-targets `V71/`.
- User pre-created `V71/` (assets + a stale v70 jar copy) and pre-set `_Info.txt` to 71/19 + VERSION 1.2.0. Per [[do-not-clobber-info-txt]], synced the pom to those values BEFORE building so `copy-mod-info` re-rendered `_Info.txt` to the same values.
- **V70 left as-is** (user instruction): the v71 build's `copy-mod-to-game` deploys only `V71/` once `target/out/` is clean. NOTE: if stale `target/out/<mod.name>/V70/` staging exists from earlier same-session builds, `copy-mod-to-game` will re-deploy it over the project-root `V70/` (byte-identical content, but touches mtimes, and can drop stray jars like a leftover `extended-boostables.jar`). Fix: `rm -rf target/out` before/after building when V70 must stay pristine. Do NOT use `mvn clean` (see CLAUDE.md — it deletes the project root).

See [[maven-intellij-path]] for the build command and [[do-not-clobber-info-txt]] for the `_Info.txt` trap.

## v71 silent semantic change: `POPULATION_<RACE>_<CLASS>_F` GVALUE (found 2026-06-17)

A **behavioural** (not compile) v71 regression that broke tech content in **Create-A-Culture** (CaC), which depends on this mod. The vanilla faction GVALUE `POPULATION_<RACE>_<CLASS>_F` — the value tech `REQUIRES:` blocks query — changed its denominator in `settlement/stats/SValues.java`:

```
v70.32 (SValues.java:205):  STATS.POP().POP.data(cl).get(r) / STATS.POP().POP.data(cl).get(null)   // fraction of THIS CLASS that is race r
v71.19 (SValues.java:206):  POP.tot(cl, r)                 / POP.tot(null, null)                    // fraction of TOTAL population
```

`POP.tot(null,null)` (`settlement/stats/POP.java:21`) sums **every** class. So `POPULATION_TILAPI_CITIZEN_F` went from "Tilapi ÷ all citizens" to "Tilapi citizens ÷ everyone." Any noble inflates the denominator but not the numerator (numerator is class-restricted; nobles are class NOBLE, not CITIZEN) — **even a Tilapi noble** — so `EQUAL: 1.0` "monorace" requirements can no longer pass once a noble is appointed. In v71 every `_F` variant now uses the total-population denominator (a consistency normalization; in v70 the per-class `_F` used the class denominator while the per-race `_F` already used total). Likely intentional on Jake's side, so don't expect a revert. Tech REQUIRES resolves against `GVALUES.FACTION` (`init/tech/TECH.java:71` `requires.push(data)` → `Lockable.RPromise` → resolved in `GValueCat.init()` at `init.finish()`); unknown keys resolve to a dummy that returns 0 (always-locked), so a missing key fails silently-closed.

**Fix shipped in this mod** (`MainScript.registerCitizenRaceFractions`, called from `initBeforeGameInited`): registers a parallel GVALUE `POPULATION_<RACE>_<CLASS>_OFCLASS_F` for every race × player class, carrying the **exact v70 expression** `STATS.POP().POP.data(class).get(race) / STATS.POP().POP.data(class).get(null)`. Computed live per query, so it needs nothing from `SValues`; it only must exist in the `GVALUES.FACTION` map before `init.finish()` resolves the REQUIRES promises — `initBeforeGameInited` guarantees that (the per-game `GVALUES`/GameDisposable clear has already run; lock resolution has not). Couldn't reuse the vanilla key (SValues' `GValueCat.push` throws on duplicate key → crash), hence the new `_OFCLASS_F` suffix. CaC's 6 monorace techs (`TILMO1` etc., one per race) were repointed from `POPULATION_<RACE>_CITIZEN_F` → `POPULATION_<RACE>_CITIZEN_OFCLASS_F` in `<workshop>/3694649839/V71/assets/init/tech/RACE_*.txt`. **In-game verified working 2026-06-17** (monorace requirement now met in a 100%-citizen-race city regardless of nobles).
