---
name: v71-migration
description: sos-extended-boostables migrated from game v70.32 → v71.19; the one code break was BValue.PopTime removal
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
