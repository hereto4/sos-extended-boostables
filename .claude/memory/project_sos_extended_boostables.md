---
name: sos-extended-boostables Project State
description: Current state and key decisions for the sos-extended-boostables mod project
type: project
originSessionId: f4a7dc19-9fa3-4a12-9315-2717447ae117
---
Adds new boostable keys to Songs of Syx (now v71.19, see [[v71-migration]]) that don't exist in vanilla. All effects ship in the same jar (`your.mod.MainScript`). Keys: `ROOM__SLAVER`, `ROOM__CANNIBAL`, `WORLD_PLUNDER` (renamed 2026-07-04 from `CIVIC_PLUNDER`), `ROOM_MINE_ALL`, `ROOM_WORKSHOP_ALL`, `ROOM_FARM_ALL`, `ROOM_REFINER_ALL`, ~~`STAT_WORK_RETIREMENT`~~ (DISABLED — see below), `BATTLE_FEAR` (2026-06-14), `CIVIC_INDOCTRINATION` (2026-06-15; SHELVED 2026-06-29, RE-ENABLED 2026-07-05 on the v71.40 policy API — see #7), ~~`CLASS_CITIZEN` + `CLASS_CITIZEN_MINE`~~ ("Class Treatment" family — confirmed working then SHELVED 2026-07-04, code `//`-commented; see #8 below), plus the Low-Positive tooltip recolor UI fix (2026-06-15).

**⚠️ `STAT_WORK_RETIREMENT` DISABLED 2026-06-27** (commented out in `MainScript.java`, not deleted; 7 blocks + 6 imports tagged `STAT_WORK_RETIREMENT DISABLED (2026-06-27)`). It inflated player-city **happiness → runaway immigration** in v71, from a fresh start with no techs. Its `restoreDenominators()` pins `StandingCitizen.maxes/defs` every 2s; the "denominator stays static after init" assumption that makes this a no-op held in the v70.32 source but appears broken by the v71 standing rework. It also did nothing useful — no tech/data file boosts the key, so F was always 1.0. CAC was investigated and exonerated (its boosts are all tech-level-gated → neutral at game start). Re-enable only after re-validating the reflection against v71 `StandingCitizen`/`StandingData`. Full analysis: [[stat-system-seams]] and [[immigration-happiness-bug]]. **Rebuild required** to regenerate `V71/script/sos-extended-boostables.jar` (the `V70/` jar still has the old code; out of v71 scope).

**Why:** Boostable keys produced by this mod are consumed by tech files (handled externally). This mod registers the keys and applies the effects at runtime.

**Key naming convention (followed strictly):** room internal key `_X` → boost key `ROOM__X` (double underscore). `BOOSTING.push()` strips ONE leading `_`, then prepends the category prefix, so push key `__X` → strip → `_X` → prepend `ROOM_` → `ROOM__X`.

**Registration pattern (all keys):** `ensureBoostable(fullKey, pushKey, name, desc, icon, cat)` — `BOOSTING.MAP().tryGet(fullKey)` first; if null, fall back to `BOOSTING.push(pushKey, 1.0, name, desc, icon, cat)`. (As of 2026-06-14 `ensureBoostable` takes the `BoostableCat` as a param — `BOOSTABLES.ROOMS()` for ROOM_* keys, `BoostableCat.ALL().WORLD` for WORLD_PLUNDER — was `BOOSTABLES.CIVICS()` until the 2026-07-04 rename.) So registration works whether or not a data file is present.

**Implemented boostables:**

1. `ROOM__SLAVER` — registered programmatically (no data file). Effect attaches a `BoosterValue` to `BOOSTABLES.BEHAVIOUR().SUBMISSION` via a full `BValue`:
   - `vGet(Induvidual)` returns `roomSlaver.get(player) - baseValue` only for `HTYPES.SLAVE()` whose `arrive == CAUSE_ARRIVES.PAROLE()` (i.e., produced by the Slaver room; vanilla sets PAROLE on slaver-room converts in `Enslaved.java:82`; trade slaves get `IMMIGRATED`).
   - `vGet(Player)` / `vGet(PopTime)` returns `delta * processedRatio` for uprising risk.
   - `processedRatio = parole_slaves / total_slaves` recomputed every 4s in `SCRIPT_INSTANCE.update` and immediately in `load`.

2. `ROOM__CANNIBAL` — registered both via data files (`V70/assets/{init,text}/stats/boost/__CANNIBAL.txt`) and the same programmatic fallback. Effect multiplies butcher yields at Cannibal Rooms by replacing each `Race.resources` `RES_AMOUNT.Imp` entry with a `BoostedResAmount` wrapper whose `amount()` returns `round(baseAmount * boostable.get(FACTIONS.player()))`.
   - Why this is scoped to butchering: `Race.resources()` is read by vanilla ONLY in `WorkCannibal.butcher2.produce` (for amounts) and `ROOM_CANNIBAL.resources()` (for resource-set discovery — reads only `.resource()`, not `.amount()`).
   - Patching uses `snake2d.util.sets.ArrayList.replace(int, E)` — no reflection required.
   - Patching runs in `initBeforeGameInited()` (after `RACES.expand()`).

3. `WORLD_PLUNDER` ("Raid Plunder") — **renamed 2026-07-04 from `CIVIC_PLUNDER`**; registered
   programmatically under the engine WORLD category (`BOOSTING.push("PLUNDER", 1.0, ...,
   BoostableCat.ALL().WORLD)`, prefix `"WORLD_"`, `TYPE_WORLD`, icon `s.sword`), base 1.0. Moved out of
   CIVICS (TYPE_SETT) so it groups with the other `WORLD_*` keys in the World boost panel; the effect is
   unchanged (read via the player faction, so type-agnostic). `BoostableCat.ALL()` is populated well
   before `initBeforeGameInited` (vanilla region init registers `WORLD_*` boostables against it).
   Distinct from vanilla `CIVIC_RAIDING` ("Raid Security", lowers chance of
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

7. `CIVIC_INDOCTRINATION` ("Indoctrination", CIVICS, base 1.0, added 2026-06-15) — **SHELVED 2026-06-29
   (v71.40 removed `StatsEducation.policyIndoctor`), then RE-ENABLED 2026-07-05 with the effect migrated
   to the v71.40 education-policy API.** Root cause it resurfaced: CAC tech (`RACE_HUMAN`, `RACE_AMEVIA`)
   boosts this key, so with EB not registering it the game logged it as an unknown key on exit; the fix
   was to re-register + re-implement (chosen over stubbing an inert key or stripping the CAC refs).
   Multiplies the **effectiveness (gain rate) of indoctrinating subjects**. Registered programmatically
   (`ensureBoostable("CIVIC_INDOCTRINATION", "INDOCTRINATION", ... BOOSTABLES.CIVICS())`, icon
   `UI.icons().s.admin`). Indoctrination is gained through the education path, which reads **no boostable**
   for the gain amount itself; the seam used instead: a university's learning speed is
   `bonus().get(subject) * (1-degrade) * boosts` (v71.40 `RoomEducationHelper.learningSpeed(ins, h)`,
   `h = student.indu()`), and `bonus()` (`RoomBlueprintIns.bonus()`) is a real per-`Induvidual` `Boostable`.
   So `registerIndoctrinationEffect` iterates `SETT.ROOMS().UNIVERSITIES` (`ROOMS.java:506`,
   `LIST<ROOM_UNIVERSITY>`) and adds an `IndoctrinationBooster` (nested final class, mirrors
   `UmbrellaBooster`: `isMul=true`, `from()=to()=1`, `getValue(input)=input`, `pget(o)=o.boostableValue(value)`)
   to each `u.bonus()` (null-checked). **v71.40 policy check:** the booster resolves the INDOCTRINATION
   `StatsEducation.StatEducation` once (match `total.key()=="INDOCTRINATION"`, fallback `edu.all.get(1)` —
   engine order is [EDUCATION, INDOCTRINATION]) and its `vGet(Induvidual)` returns
   `civicIndoctrination.get(FACTIONS.player())` **only when**
   `STATS.EDUCATION().policy(indu.clas(), indu.race()) == that INDOCTRINATION instance`, else `1.0`; every
   other query (`HCLASS_RACE`/`Player`/`FactionNPC`/`Region`/`Div`) returns neutral `1.0`. Note this is now
   correctly **per (class, race)** — v71.40 policy is per-`HCLASS_RACE`, not per-race as the old
   `policyIndoctor.is(race)` was. Learning (hence indoctrination gain) scales only for subjects on the
   INDOCTRINATION policy; plain-education subjects untouched. **Known limitation (user accepted):** schools
   compute learning speed with no `bonus()` factor, so **school/child indoctrination is NOT boosted** —
   universities (adults) only. The rejected alternative was a bytecode-agent patch of the educate chokepoint
   (covers schools too, but ties a gameplay key to the in-game-unverified self-attach). Cosmetic:
   `vGet(HCLASS_RACE)`→1.0 means the university per-race "Learning speed" tooltip omits the indoctrination
   line (×1.0 hidden anyway); the actual gain still scales via the Induvidual query. **Built clean v71.40. In-game
   2026-07-05: key appears and is recognized (unknown-key-on-exit report gone) — CONFIRMED. Applied
   multiplier effect NOT yet verified** (still to check: grant the key + set a race's class to INDOCTRINATION,
   watch university learning speed / indoctrination gain rise for those subjects only).

6. `POPULATION_<RACE>_<CLASS>_OFCLASS_F` — **GVALUES.FACTION values, not boostables** (the registry tech
   `REQUIRES:` blocks query, `init/tech/TECH.java:71`). Added 2026-06-17 to fix a v71 regression: vanilla's
   `POPULATION_<RACE>_<CLASS>_F` silently changed its denominator from the class to the whole population
   (`SValues.java`), so any noble breaks `EQUAL: 1.0` monorace requirements. `registerCitizenRaceFractions()`
   (called from `initBeforeGameInited`) pushes one value per race × player class carrying the exact v70
   expression `STATS.POP().POP.data(class).get(race)/.get(null)`. Computed live, so independent of `SValues`;
   only needs to be in the map before `init.finish()` resolves REQUIRES promises (initBeforeGameInited window).
   Consumed by Create-A-Culture's monorace techs. Full analysis in [[v71-migration]].

8. `CLASS_<CLASS>[_<ROOMTYPE>]` "Class Treatment" keys — **SHELVED 2026-07-04 (all CLASS_* code
   `//`-commented in MainScript, per user; jar no longer contains it). Confirmed WORKING in-game first
   (CLASS_CITIZEN 3 need-rates + CLASS_CITIZEN_MINE all-mine output), then shelved for a later update.**
   To restore: uncomment the five CLASS_*-marked blocks (constants, initBeforeGameInited registration,
   `registerClassKey`, `registerClassTreatment`, `ClassTreatmentBooster`); no save-format impact. Design
   history below is retained for that re-enable. Added 2026-07-02 (CITIZEN test), expanded 2026-07-04 to
   the room-typed family; built clean against v71.40. New `CLASS_` `BoostableCat`
   (prefix applied by `BOOSTING.push`, so push `"CITIZEN"`→`CLASS_CITIZEN`, `"CITIZEN_MINE"`→`CLASS_CITIZEN_MINE`).
   Multiplies **per-subject** boostables only for subjects of the matching `HCLASS`. **Design:** *every*
   `CLASS_<CLASS>*` key scales the three need-growth rates **`RATES_HUNGER`/`RATES_THIRST`/`RATES_SHOPPING`**
   (single underscore — confirmed via m2 `71.19/boost_dump.txt`; higher = needier, the intended trade-off
   cost — see [[rates-need-semantics]]). A **room-typed** variant `CLASS_<CLASS>_<ROOMTYPE>` additionally
   scales that class's output across **all** rooms of the type by ALSO targeting the mod's
   `ROOM_<ROOMTYPE>_ALL` umbrella (base 1.0) — the umbrella's `UmbrellaBooster.pget→umbrella.get(o)` cascade
   carries the per-class factor into each `ROOM_<TYPE>_*` child's per-employee read at `IndustryUtil.java:80`
   `bonus.get(h.indu())`. Registered order matters: `registerRoomAllCascade(MINE_ALL…)` runs before the CLASS
   block, so `ROOM_MINE_ALL` exists when `registerClassKey` looks it up. **Live now:** `CLASS_CITIZEN` (3 rates)
   + `CLASS_CITIZEN_MINE` (3 rates + `ROOM_MINE_ALL`, which fans to 6 mine types). FARM/REFINER/WORKSHOP +
   other classes are commented one-liner `registerClassKey(...)` calls, staged after MINE validation.
   **Note replaced target:** the old CITIZEN ore-only `ROOM_MINE_ORE` target is gone — mine output now flows
   through `ROOM_MINE_ALL` on `CLASS_CITIZEN_MINE` (covers all mines, not just ore). **Stacking:** rates are
   shared across the family, so boosting both `CLASS_CITIZEN` and a room-typed variant multiplies the rates
   by both factors (each clamped independently). Effect = nested `ClassTreatmentBooster extends Booster`
   (multiplicative, `from()=to()=1`, `getValue(input)=input`, `pget(o)=o.boostableValue(value)`);
   `vGet(Induvidual)` returns `CLAMP.d(classKey.get(player), 0.5, 1.5)` iff `indu.clas()==hclass`, neutral
   `1.0` for the other five `vGet` overloads. New helper **`registerClassKey(cat, hclass, roomType,
   roomAllKey, roomLabel)`** builds key/display/desc/targets and delegates to `registerClassTreatment`.
   **Min/Max 0.5/1.5** via clamp. **Zero-multiply safety-net** (user-required): `registerClassTreatment`
   skips any `baseValue==0` target; the clamp keeps the factor away from the zero boundary. Full design:
   `../../sos-scripting-template/.claude/SPEC_CLASS_KEYS.md`. Expansion is data-only — **every added target
   must be a per-`Induvidual` boostable** (or a `ROOM_*_ALL` umbrella that reaches one); non-boostable stats
   need the reflection path (higher risk, see [[stat-system-seams]]).

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
