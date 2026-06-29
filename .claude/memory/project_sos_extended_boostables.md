---
name: sos-extended-boostables Project State
description: Current state and key decisions for the sos-extended-boostables mod project
type: project
originSessionId: f4a7dc19-9fa3-4a12-9315-2717447ae117
---
Adds new boostable keys to Songs of Syx (now v71.19, see [[v71-migration]]) that don't exist in vanilla. All effects ship in the same jar (`your.mod.MainScript`). Keys: `ROOM__SLAVER`, `ROOM__CANNIBAL`, `CIVIC_PLUNDER`, `ROOM_MINE_ALL`, `ROOM_WORKSHOP_ALL`, `ROOM_FARM_ALL`, `ROOM_REFINER_ALL`, ~~`STAT_WORK_RETIREMENT`~~ (DISABLED — see below), `BATTLE_FEAR` (2026-06-14), `CIVIC_INDOCTRINATION` (2026-06-15), plus the Low-Positive tooltip recolor UI fix (2026-06-15).

**⚠️ `STAT_WORK_RETIREMENT` DISABLED 2026-06-27** (commented out in `MainScript.java`, not deleted; 7 blocks + 6 imports tagged `STAT_WORK_RETIREMENT DISABLED (2026-06-27)`). It inflated player-city **happiness → runaway immigration** in v71, from a fresh start with no techs. Its `restoreDenominators()` pins `StandingCitizen.maxes/defs` every 2s; the "denominator stays static after init" assumption that makes this a no-op held in the v70.32 source but appears broken by the v71 standing rework. It also did nothing useful — no tech/data file boosts the key, so F was always 1.0. CAC was investigated and exonerated (its boosts are all tech-level-gated → neutral at game start). Re-enable only after re-validating the reflection against v71 `StandingCitizen`/`StandingData`. Full analysis: [[stat-system-seams]] and [[immigration-happiness-bug]]. **Rebuild required** to regenerate `V71/script/sos-extended-boostables.jar` (the `V70/` jar still has the old code; out of v71 scope).

**Why:** Boostable keys produced by this mod are consumed by tech files (handled externally). This mod registers the keys and applies the effects at runtime.

**Key naming convention (followed strictly):** room internal key `_X` → boost key `ROOM__X` (double underscore). `BOOSTING.push()` strips ONE leading `_`, then prepends the category prefix, so push key `__X` → strip → `_X` → prepend `ROOM_` → `ROOM__X`.

**Registration pattern (all keys):** `ensureBoostable(fullKey, pushKey, name, desc, icon, cat)` — `BOOSTING.MAP().tryGet(fullKey)` first; if null, fall back to `BOOSTING.push(pushKey, 1.0, name, desc, icon, cat)`. (As of 2026-06-14 `ensureBoostable` takes the `BoostableCat` as a param — `BOOSTABLES.ROOMS()` for ROOM_* keys, `BOOSTABLES.CIVICS()` for CIVIC_PLUNDER.) So registration works whether or not a data file is present.

**Implemented boostables:**

1. `ROOM__SLAVER` — registered programmatically (no data file). Effect attaches a `BoosterValue` to `BOOSTABLES.BEHAVIOUR().SUBMISSION` via a full `BValue`:
   - `vGet(Induvidual)` returns `roomSlaver.get(player) - baseValue` only for `HTYPES.SLAVE()` whose `arrive == CAUSE_ARRIVES.PAROLE()` (i.e., produced by the Slaver room; vanilla sets PAROLE on slaver-room converts in `Enslaved.java:82`; trade slaves get `IMMIGRATED`).
   - `vGet(Player)` / `vGet(PopTime)` returns `delta * processedRatio` for uprising risk.
   - `processedRatio = parole_slaves / total_slaves` recomputed every 4s in `SCRIPT_INSTANCE.update` and immediately in `load`.

2. `ROOM__CANNIBAL` — registered both via data files (`V70/assets/{init,text}/stats/boost/__CANNIBAL.txt`) and the same programmatic fallback. Effect multiplies butcher yields at Cannibal Rooms by replacing each `Race.resources` `RES_AMOUNT.Imp` entry with a `BoostedResAmount` wrapper whose `amount()` returns `round(baseAmount * boostable.get(FACTIONS.player()))`.
   - Why this is scoped to butchering: `Race.resources()` is read by vanilla ONLY in `WorkCannibal.butcher2.produce` (for amounts) and `ROOM_CANNIBAL.resources()` (for resource-set discovery — reads only `.resource()`, not `.amount()`).
   - Patching uses `snake2d.util.sets.ArrayList.replace(int, E)` — no reflection required.
   - Patching runs in `initBeforeGameInited()` (after `RACES.expand()`).

3. `CIVIC_PLUNDER` ("Raid Plunder") — registered programmatically in CIVICS category
   (`BOOSTING.push("PLUNDER", 1.0, ..., BOOSTABLES.CIVICS())`, icon `s.sword`),
   base 1.0. Distinct from vanilla `CIVIC_RAIDING` ("Raid Security", lowers chance of
   *being* raided, read at `RaidingUtil.java:60`). NO engine read-point exists for a
   plunder boostable — raid loot is hardcoded in the `final static WArmyState.raiding`
   anonymous class (`WArmyState.java:155`: `am = ceil(RD.OUTPUT().get(TR.get(res)).loot(reg)*d*10)`
   into a `TRADE_TYPE.spoils` Shipment). `WArmyState` has a **private ctor + is abstract**,
   so it can't be subclassed/replaced; `Shipment` is `final`. So the effect is **additive**:
   a per-tick updater in `SCRIPT_INSTANCE.update(ds)` (`handleRaidPlunder`) walks
   `FACTIONS.player().armies().all()`, and for each army where `a.raiding()` && `region()!=null`,
   accumulates a per-army 120s timer (IdentityHashMap, mirrors `WArmy.stateFloat`); every 120s
   it ships `(plunder-1) × vanilla per-tick loot` via a fresh spoils Shipment, replicating the
   vanilla formula with public APIs (`AD.men(null).get(a)`, `Config.battle().MEN_PER_ARMY`,
   `RD.RACES().popSize`, `RD.OUTPUT().get(TR.get(res)).loot(reg)`, gated on
   `RD.DEVASTATION().current.get<max`). Vanilla still loots 100%, so total = vanilla×plunder.
   Scoped to the raid action only — battle-victory (`world.battle.Util`) & conquest spoils
   untouched; `raiding()`/UI/sprite/devastation/pop all vanilla. Only resources scaled, not pop.
   Fragility: duplicates the engine raid formula, so a future change to it would drift.

4. `ROOM_MINE_ALL` / `ROOM_WORKSHOP_ALL` / `ROOM_FARM_ALL` ("Mines/Workshops/Farms (All)") —
   single umbrella boostables in ROOMS category that cascade onto every matching room key, so a
   tech shows ONE tooltip line ("Mines (All) *1.5") instead of one per room. (The game's `ROOM_FARM*`
   wildcard already applies to all, but lists each room separately on the tech tooltip — the
   umbrella collapses that to one line.) `registerRoomAllCascade` enumerates `BOOSTING.ALL()` for
   keys starting with `ROOM_MINE_`/`ROOM_WORKSHOP_`/`ROOM_FARM_` (excluding the `_ALL` key itself;
   catches modded rooms registered by initBeforeGameInited too), then adds one `UmbrellaBooster`
   (nested final class, `isMul=true`, `getValue(input)=input`, `pget(o)=umbrella.get(o)`) to each
   child via `Booster.add(child)`. So `child.get(o)` gains a `*umbrella.get(o)` factor. Correct for
   ANY query target because room production reads `bonus.get(employeeInduvidual)`
   (`IndustryUtil.java:80,102`) and the umbrella resolves the player's tech boost via the engine's
   `BValueFaction` (PTech `BoostCompound.Boo`) for Induvidual/Player/etc. No deadlock — umbrella
   never references its children. Umbrella icon = first child's `nativeIcon`. Known cosmetic: the
   per-room *production breakdown* (`IndustryUtil.hoverBoosts`) lists every MUL booster incl. ×1.0,
   so an unused umbrella shows a neutral "Mines (All) *1.0" line there (standard `BHoverer` hovers
   hide ×1.0). The tech tooltip — the thing we wanted decluttered — is unaffected.

5. `BATTLE_FEAR` ("Fear") — per-division morale aura, added 2026-06-14. Registered in the BATTLE
   category (`BOOSTING.push("FEAR", 0.0, ..., BOOSTABLES.BATTLE())` → base 0; races grant it via
   `BATTLE_FEAR>ADD` since base 0 makes `>MUL` a no-op). **Morale is division-level**: `DivFactors`
   computes each division's morale as `BOOSTABLES.BATTLE().MORALE.get(div)` (cached ~1s/div). Every
   vanilla morale modifier is a `DivFactor` that adds a `BoosterValue` to `BATTLE_MORALE` — but
   `DivFactor`'s ctor is package-private (can't subclass, like `WArmyState`), so `registerFearAura`
   adds the `BoosterValue` directly (same as the SLAVER→SUBMISSION pattern). The BValue's `vGet(Div d)`
   = `fearIntensity(d)`: walks `d.status().enemiesClosest(buf)` (a bounded nearby-ENEMY list the engine
   already maintains in `DivStatus`, with per-target tiles via `enemyClosestDist(i)`), sums
   `BATTLE_FEAR.get(enemyDiv) × linearFalloff(dist/FEAR_AURA_RANGE)`, clamps to [0,1]. Booster is
   `BoosterValue(bv, info, 1.0, FEAR_MORALE_FLOOR=0.4, isMul=true)`: intensity 0 → ×1 (also all non-Div
   queries return 0 → ×1, so non-battle morale reads are untouched), intensity 1 → ×0.4. No spatial
   code, no per-soldier work, no battle-thread integration. Tuning constants: `FEAR_AURA_RANGE` (8
   tiles), `FEAR_INTENSITY_SCALE`, `FEAR_MORALE_FLOOR`, `FEAR_MAX_ENEMIES`. `fearIntensity` allocates a
   small local `ArrayList<Div>` per call (thread-safe vs battle threads). Since fear is race-sourced and
   a division is single-race, `BATTLE_FEAR.get(div)` is the per-soldier average exactly.

6. **Low-Positive tooltip recolor** (UI fix, added 2026-06-15; package `your.mod.boostcolor`). Inverts
   the engine's green/red for boostables where a *lower* value is the good outcome ("Low-Positive",
   e.g. `PHYSICS_SOILING`). The coloring is hardcoded in `BoosterAbs.hover` (static) + `BoostSpecs.hover`
   (`IGOOD`/`IBAD` from value-vs-neutral) with **no seam**, `Boostable`/`BoostableCat` carry no
   inverted flag, and `ScriptLoad.processJarEntry` hard-blocks shadowing any `game.*` class
   (`Class.forName` succeeds → `Errors.DataError`). So the only way is **runtime bytecode patching**.
   Implementation: a Java agent (`ColorAgent`, premain+agentmain, manifest `Agent-Class`/`Premain-Class`/
   `Can-Retransform-Classes`) + retransform-capable Javassist `ClassFileTransformer` that (a) rewrites
   `GCOLOR.T().IGOOD/.IBAD` reads to `LowPositiveColors.pick(orig, good)`, and (b) pushes the boostable
   key around each render scope: `BoostSpecs.hover` (`$2.boostable.key`), `Boostable.hover`/`hoverDetailed`
   (`$0.key`), tech Boosts browser `InfoBonuses$Boo.hoverInfoGet` (`$0.bo.key`). `pick` swaps IGOOD↔IBAD
   only when the current key ∈ `LowPositiveColors.KEYS` (ships `PHYSICS_SOILING`); the **displayed number
   is untouched**. Delivery = **self-attach** via `ColorAgent.install()` (reflection on
   `com.sun.tools.attach.VirtualMachine`; sets `jdk.attach.allowAttachSelf`), called from
   `initBeforeGameInited()`. Fully guarded — if self-attach is blocked it logs a `-javaagent:<jar>`
   fallback and leaves tooltips vanilla; rest of mod unaffected. pom: added `org.javassist:javassist`,
   shade adds the agent manifest + **relocates** `javassist`→`your.mod.shaded.javassist` (avoids the
   loader uniqueness check / cross-mod collision). Validated against real 70.33 bytecode (transform +
   `javap` placement); in-game render + self-attach permission still to be confirmed by the user. Why
   self-attach over `-javaagent`: keeps install identical to a normal drop-in/Workshop mod (user's
   explicit choice). Main tech-NODE tooltip (`Node.hoverInfoGet`) is uncolored on 70.33 → not a target;
   add `view.ui.tech.Node` if a future version colors it.

7. `CIVIC_INDOCTRINATION` ("Indoctrination", CIVICS, base 1.0, added 2026-06-15) — multiplies the
   **effectiveness (gain rate) of indoctrinating subjects**. Registered programmatically
   (`ensureBoostable("CIVIC_INDOCTRINATION", "INDOCTRINATION", ... BOOSTABLES.CIVICS())`, icon
   `UI.icons().s.admin`). Indoctrination is gained only in `StatsEducation.educate(Induvidual, double)`
   (target = `STATS.EDUCATION().INDOCTRINATION` when `policyIndoctor.is(race)`, else `EDUCATION`), and
   that method reads **no boostable** (the gain `am = EDUCATION.indu().max(i) * speed` has no seam; the
   underlying `data` is shared between EDUCATION and INDOCTRINATION so its `max` can't be scaled
   selectively). The seam used instead: a university's learning speed is
   `learningSpeed*(1-degrade)*quality*bonus().get(subject)` (`ROOM_UNIVERSITY.learningSpeed`), and
   `bonus()` (`RoomBlueprintImp.bonus()`) is a real `Boostable`. So `registerIndoctrinationEffect`
   iterates `SETT.ROOMS().UNIVERSITIES` and adds an `IndoctrinationBooster` (new nested final class,
   mirrors `UmbrellaBooster`: `isMul=true`, `from()=to()=1`, `getValue(input)=input`,
   `pget(o)=o.boostableValue(value)`) to each `u.bonus()` (null-checked). The `BValue` returns
   `civicIndoctrination.get(FACTIONS.player())` for `vGet(Induvidual)` **only when**
   `STATS.EDUCATION().policyIndoctor.is(indu.race())`, else `1.0`; every other query
   (`HCLASS_RACE`/`Player`/`FactionNPC`/`Region`/`Div`) returns the neutral `1.0`. So learning (hence
   indoctrination gain) scales only for races on the indoctrination policy; education-only races are
   untouched. **Known limitation (user accepted):** `ROOM_SCHOOL` computes learning speed with no
   `bonus()` factor (never `pushBo`s → `bonus()` is null), so **school/child indoctrination is NOT
   boosted** — universities (adults) only. The rejected alternative was a bytecode-agent patch of
   `educate()` (would cover both schools+universities at the chokepoint, but ties a gameplay key to the
   in-game-unverified self-attach; agent route remains the documented fallback if school coverage is
   wanted). Cosmetic: `vGet(HCLASS_RACE)`→1.0 means the university's per-race "Learning speed" tooltip
   breakdown omits the indoctrination line (×1.0 is hidden anyway); the actual gain still scales via the
   Induvidual query, and the tech tooltip for `CIVIC_INDOCTRINATION` displays normally.

6. `POPULATION_<RACE>_<CLASS>_OFCLASS_F` — **GVALUES.FACTION values, not boostables** (the registry tech
   `REQUIRES:` blocks query, `init/tech/TECH.java:71`). Added 2026-06-17 to fix a v71 regression: vanilla's
   `POPULATION_<RACE>_<CLASS>_F` silently changed its denominator from the class to the whole population
   (`SValues.java`), so any noble breaks `EQUAL: 1.0` monorace requirements. `registerCitizenRaceFractions()`
   (called from `initBeforeGameInited`) pushes one value per race × player class carrying the exact v70
   expression `STATS.POP().POP.data(class).get(race)/.get(null)`. Computed live, so independent of `SValues`;
   only needs to be in the map before `init.finish()` resolves REQUIRES promises (initBeforeGameInited window).
   Consumed by Create-A-Culture's monorace techs. Full analysis in [[v71-migration]].

**Implementation rules (followed in MainScript):**
- No Lombok. Explicit `public MainScript() {}`.
- `SCRIPT_INSTANCE` inlined as anonymous class in `createInstance()` — no separate `InstanceScript.java`.
- Errors via `System.err.println`, informational via `System.out.println`.
- `BoostedResAmount` is a static nested final class implementing `RES_AMOUNT, Serializable`.

**Build/install:**
- Maven outputs `target/out/sos-extended-boostables/` with full V70/ structure.
- Maven not on PATH; invoke via `"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" -f .../pom.xml package`.
- pom.xml: `mod.name=sos-extended-boostables`, `mod.author=Nate`, `mod.info="Adds ROOM__SLAVER and ROOM__CANNIBAL boostables."`. The `<artifactId>` remains `sos-scripting-template` (template-default, harmless).

**SPEC.md** at project root remains the SLAVER-focused design doc; it is not the authoritative implementation reference anymore — read `MainScript.java` for current behavior.
