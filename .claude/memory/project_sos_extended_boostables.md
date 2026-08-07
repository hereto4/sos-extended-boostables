---
name: sos-extended-boostables Project State
description: Current state and key decisions for the sos-extended-boostables mod project
type: project
originSessionId: f4a7dc19-9fa3-4a12-9315-2717447ae117
---
Adds new boostable keys to Songs of Syx (now v71.19, see [[v71-migration]]) that don't exist in vanilla. All effects ship in the same jar (`your.mod.MainScript`). Keys: `ROOM__SLAVER`, `ROOM__CANNIBAL`, `WORLD_PLUNDER` (renamed 2026-07-04 from `CIVIC_PLUNDER`), `ROOM_MINE_ALL`, `ROOM_WORKSHOP_ALL`, `ROOM_FARM_ALL`, `ROOM_REFINER_ALL`, `WORLD_PRODUCTION_SLAVE_ALL` (2026-07-09, umbrella over per-race slave production — see #4), ~~`STAT_WORK_RETIREMENT`~~ (DISABLED — see below), `BATTLE_FEAR` (2026-06-14), `CIVIC_INDOCTRINATION` (2026-06-15; SHELVED 2026-06-29, RE-ENABLED 2026-07-05 on the v71.40 policy API — see #7), `CLASS_CITIZEN`(+`_MINE`), `CLASS_SLAVE`(+`_MINE`), `CLASS_NOBLE`(+per-office `CLASS_NOBLE_<OFFICE>`/`CLASS_NOBLE_ALL`) ("Class Treatment" family — un-shelved & expanded to SLAVE 2026-07-11, NOBLE + noble-office keys 2026-07-29; see #8 below), plus the Low-Positive tooltip recolor UI fix (2026-06-15).

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
   umbrella collapses that to one line.) `registerUmbrellaCascade` (generalized 2026-07-09 from the old
   room-only `registerRoomAllCascade` — now takes the umbrella's `BoostableCat` and a nullable
   `excludeSuffix`) enumerates `BOOSTING.ALL()` for
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

   **`WORLD_PRODUCTION_SLAVE_ALL` (added 2026-07-09)** reuses this same helper for a *world* umbrella:
   `registerUmbrellaCascade("WORLD_PRODUCTION_SLAVE_ALL", "PRODUCTION_SLAVE_ALL", "Slave Production (All)",
   "WORLD_PRODUCTION_SLAVE_", …, BoostableCat.ALL().WORLD_PRODUCTION, "_YEARLY")`. Cascades to the 8 vanilla
   per-race `WORLD_PRODUCTION_SLAVE_<RACE>` region-output keys (base 0, `WORLD_PRODUCTION` cat), **excluding**
   the hidden per-race `_YEARLY` display-derivatives (`WORLD_DUMP` cat) per user. Children are queried
   per-`Region` (`RDOutput.getDelivery/loot` read `boost.get(reg)`); `umbrella.get(Region)` supplies the tech
   factor. Timing verified against v71.40 `GAME.<init>`: `WORLD`→`RD`→`RDOutputs` construct at line 158,
   before `initBeforeGameInited` (172), so the children already exist when the cascade enumerates (same
   guarantee as `ROOM_*` from `SETT` at line 142). Built clean v71.40; in-game verification pending.

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
   add `view.ui.tech.Node` if a future version colors it. **DORMANT / SUPERSEDED (2026-07-29):**
   self-attach is impossible on the bundled JRE (no `jdk.attach` module, confirmed in-game 2026-06-29),
   and the only way to arm it is a per-user launch arg — `JVM_ARGS2` in
   `%APPDATA%\songsofsyx\settings\LauncherSettings.txt`, which the launcher forwards to the game JVM
   (`init/Main.java:85` → `Proccesser.executeLwjgl(MainProcess…)`); the bundled JRE **does** ship
   `java.instrument`, so the premain path works, but there is **no launcher UI** for that setting
   (`jvmArguments` is referenced nowhere in `launcher/`), so it can never be the shipping answer.
   **REMOVED ENTIRELY 2026-07-29 (user decision).** Deleted: `src/main/java/your/mod/boostcolor/`
   (`ColorAgent`, `LowPositiveColors`), the `ColorAgent.install()` call in `initBeforeGameInited`, and in
   `pom.xml` the `org.javassist:javassist` dependency + `javassist.version` property, the HotSwapper
   shade filter, the `javassist`→`your.mod.shaded.javassist` relocation, and the
   `ManifestResourceTransformer` agent manifest entries. **Jar dropped 882 KB → 62 KB**; manifest now has
   no `Premain-Class`/`Agent-Class`, jar has zero javassist/boostcolor entries. Recover from git history
   (both files were tracked) if a seam ever appears — and restore the HotSwapper filter with it. The
   shipped fix is key #10.

10. **`PHYSICS_CLEANLINESS`** ("Cleanliness", PHYSICS cat, base 1.0, added 2026-07-29; listed here
   out of numeric order because it is the resolution of #6) — the **no-agent
   answer to Low-Positive coloring**. Vanilla `PHYSICS_SOILING` (base 0.125, "rate at which a subject
   becomes dirty") is low-positive, so helping the player renders red. This key is its **high-positive
   front**: a `CleanlinessBooster extends Booster` (mul, identity `getValue`, factor from `pget` — same
   shape as `UmbrellaBooster`) placed on `BOOSTABLES.PHYSICS().SOILING` returning
   `1 / CLAMP.d(cleanliness.get(o), CLEAN_MIN=0.1, CLEAN_MAX=10)`. `pget(o)` resolves per query object,
   which matches the engine's per-subject read (`StatsNeeds:180` `SOILING.get(indu)`). Registered via
   the 8-arg `ensureBoostable` (min floor 0.1) so `>MUL: 0` can't zero the divisor. Content boosts
   `PHYSICS_CLEANLINESS>MUL: 1.333` (green) instead of `PHYSICS_SOILING>MUL: 0.75` (red) — identical
   effect, correct color, ships to everyone. **Residual cosmetic limit:** Soiling's own detailed tooltip
   lists "Cleanliness" as a sub-1 source and renders that line red. **Built clean on v71.40 and IN-GAME
   VERIFIED 2026-07-29 (user-confirmed): the key registers and the tooltip colors correctly.**
   Generalized 2026-07-30 into `registerInverseFront(...)` + `InverseFrontBooster`, then
   **⚠️ REMOVED ENTIRELY 2026-07-30 along with #11** — superseded by the #13 colour override, which needs
   no keys at all. Gone: `PHYSICS_CLEANLINESS`, `CLEANLINESS_KEY`, `physicsCleanliness`, `INV_MIN`/
   `INV_MAX`, `RATE_FRONTS`, both `registerInverseFront` overloads, `InverseFrontBooster`. Content boosts
   the vanilla keys again. Recover from git history if the format-override approach is ever abandoned.
   Kept here as the record of what was tried and why it worked (both facts stay true: `BOOSTING.push`
   forms the full key as `cat.prefix + pushKey` after stripping one leading `_`; a divisor booster on a
   low-positive target reads correctly on *every* surface, unlike the colour override).

11. **⚠️ REMOVED 2026-07-30 (never shipped past one build) — 19 `RATES_*` front keys** ("Satiety", "Hydration", "Frugality", "Freshness", "Continence",
   "Ruggedness", "Independence", "Placidity", "Austerity", "Stoicism", "Detachment", "Humility", "Vigour",
   "Hardiness", "Reserve", "Clemency", "Forbearance", "Secularity (Shrine)/(Temple)"; added 2026-07-30).
   Every vanilla `RATES_*` is a need-GROWTH rate (`NEED.java:59`), so lower is better on all of them — see
   [[rates-need-semantics]]. One front key each, driven by the `RATE_FRONTS` table
   (`{vanilla key, push key, display, cat}` where cat `E`=`NEEDS.bCatE()` Basic Needs, `S`=`NEEDS.bCat()`
   Service Needs — both prefix `RATES_`), each registered into the same category as the key it fronts and
   wired through `registerInverseFront`. Missing vanilla keys are logged + skipped (needs are partly
   file-defined, so the set is content-driven). **`RATES_NATURE` is excluded** — our own key, already
   high-positive. Names are bespoke per user choice (2026-07-30); least-settled: AUSTERITY (Spectacle),
   DETACHMENT (News Craving), RESERVE (Skinny dip), CLEMENCY (Punishment — the underlying
   `RATES_STOCKS` semantics are themselves unclear), RUGGEDNESS (Bathing). Built clean v71.40; **in-game
   not yet verified.**

> **Palette facts (user-reported, 2026-07-30/31).** (a) The engine's "good" colour renders **BLUE** in
> this game's theme — always has, not green. "Bad" is red; neutral is off-white (`COLOR.WHITE85`).
> Earlier notes here that say "green" mean this blue. (b) **There are TWO good/bad pairs and they are
> visibly different shades** — `IGOOD`/`IBAD` are the *lighter* pair. `BoosterAbs.format` picks per
> branch: `isMul`→`GFORMAT.f1` and additive-non-integral→`f0` both use **`IGREAT`/`IWORST`**; only
> additive-**integral** goes through `iIncr` and uses `IGOOD`/`IBAD`. Any code re-colouring a boost line
> must mirror that branch choice or the mismatch is visible (shipped a too-light red on 2026-07-30 by
> using `IGOOD`/`IBAD` unconditionally; fixed 2026-07-31 in `FormattedBooster.colorFor`).

13. **Tech-node colour override (`your.mod.boostformat`, 2026-07-30).** The tech-node effect list does NOT
   use `BoosterAbs.hover`'s hardcoded colouring — `Node.hoverInfoGet` renders each line with
   `bb.booster.format(b.text(), v)` (confirmed in the **installed v71.44 jar**, offsets 1201/1305).
   `BoosterAbs.format` sets no colour itself but delegates to `GFORMAT.f1` (`<1`→IWORST, `>1`→IGREAT,
   `==1`→WHITE85) / `GFORMAT.iIncr`/`f0` — **those colour it**. Since `format` is public + non-final and
   the call is virtual on the booster, owning the `Booster` in `tech.boosters` = owning that line's colour.
   `FormattedBooster` forwards `from`/`to`/`getValue`/`get` (overrides the public `get` rather than
   `pget`, which is protected in `game.boosting` and so uncallable on another instance from our package)
   and overrides only `format`: it lets the engine format the number, then re-sets the colour —
   `GText.color(..)` stores ONE colour for the whole text object, bound at render, so last call wins.
   `BoostFormats.install()` registers a `BOOSTING.connecter` that swaps matching specs per a `RULES`
   table (`NEUTRAL` | `INVERTED`, exact key or `PREFIX_*`). Ships `ACTIVITY_* → NEUTRAL`
   (**in-game VERIFIED 2026-07-30, user-confirmed**) plus `INVERTED` for `PHYSICS_SOILING` and all 19
   vanilla `RATES_*` keys (added 2026-07-30, in-game not yet verified). The `RATES_*` entries are listed
   INDIVIDUALLY, never by prefix — the prefix is shared with `RATES_NATURE` and the `RATES_*` front keys,
   which are high-positive and must not be inverted.
   **`ROOM_CONSUMPTION_*` is deliberately NOT classified.** It was added as an `INVERTED` prefix rule on
   2026-07-31 (user request) and **reverted the same day** once the arithmetic was re-confirmed: the
   engine *divides* by that boostable (`IndustryUtil.calcConsumptionRate = calcProductionRate(..)/conBonus`,
   verified in **v71.44**) and the room tooltip calls it "Consumption Bonus", so above 1.0 the room
   consumes **less** and vanilla's colouring is already right. Don't re-add it.
   **Provably display-only:** `TECH`'s `BoostSpecs` has `connect == false` (`TECH.java:122`), so
   `push`/`remove` there never touch the boostable's factor list (the live effect is the original booster
   object stored by `Boostable.addFactor`). Order-preserving (remove+push in sequence) and idempotent.
   **Install AFTER `TargetFilters.install()`** so this connecter is queued after the one re-adding
   originals for the UI. **Reach: tech node only** — `BoostSpecs.hover:363`/`BoosterAbs.hover:67` colour
   inline and never call `format`, so the Boosts browser + boostable tooltips still need the (deleted)
   agent; that is why front keys (#10/#11) still earn their place. NB `GFORMAT.f1Inv`/`f0Inv` already
   exist as colour-swapped formatters. Built clean v71.40; **in-game not yet verified.**

14. **Tech effect-list ordering (`your.mod.boostformat.BoostOrder`, 2026-07-31)** — **ported in from the
   standalone `tech-boost-sort` mod**, which sorted by raw sign and could not see the polarity table.
   Tiers: 0 additive benefit, 1 multiplicative benefit, 2 **cost**, 3 **neutral (very bottom)**;
   within a tier, benefit magnitude descending. Benefit/cost is judged AFTER polarity via
   `BoostFormats.modeForKey` (now public — the single source of truth shared with the colouring), so a
   low-positive key above its neutral point is a cost and sorts with the negatives, and `>MUL: 0.5` on an
   inverted key outranks `>MUL: 0.9`. Rebuilds the list with the public `remove`+`push` in sorted order
   (no `ArrayListGrower` cast needed; `connect == false` makes it display-only). Registered last in
   `initBeforeGameInited` so it runs after the colour swap. **⚠️ The old `tech-boost-sort.jar` must be
   removed wherever it is deployed** (it was bundled in the CaC workshop folder
   `…/1162750/3694649839/V71/script/`) — both register a connecter that reorders the same list and the
   last one to run wins. Built clean v71.40; **in-game not yet verified.**

12. **`CLASS_<CLASS>` polarity INVERTED IN PLACE 2026-07-30** (user decision; the bare per-class keys only).
   They targeted only the three need-growth rates, so raising one was a pure cost that rendered green.
   `ClassTreatmentBooster` took a new `invert` flag: `factor()` now returns `1/CLAMP.d(...)` for the
   need-rate targets (floored at `CLASS_MIN`=0.5, so no /0); room-typed keys pass `invert=false` and are
   untouched. Display aspect `(Needs)` → `(Contentment)`. **Key strings unchanged** → no cross-mod break,
   but the DIRECTION of existing content flipped: author `<1` for the old "spoiled/needier" cost. Only
   affected content was `sos-cac_addon-aruan/V71/assets/init/tech/RACE_ARUAN.txt:82,191`
   (`CLASS_NOBLE>MUL: 1.1`/`1.15`) — those sat among that tech's other buffs (its downsides are all
   written `<1`), so they were almost certainly authored expecting a benefit and now finally deliver one;
   **left as-is deliberately.** NB the uncapped `CLASS_NOBLE` now drives noble needs toward 0 as it grows.

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

8. `CLASS_<CLASS>[_<ROOMTYPE>]` "Class Treatment" keys — **LIVE. Full CITIZEN + SLAVE sets registered
   2026-08-04: `CLASS_<C>` (Contentment, inverted need-rates), `CLASS_<C>_MINE/_FARM/_REFINER/_WORKSHOP`
   (each → its `ROOM_<TYPE>_ALL` umbrella), and `CLASS_<C>_ALL` ("All Work" — targets all four umbrellas at
   once via new helper `registerClassAllKey`).** `CLASS_NOBLE` ("Nobles (Contentment)"; display overridden
   from vanilla "Nobilities" via `classDisplayName`) has **no per-room variants** — nobles get per-OFFICE
   keys instead (see below). History: added 2026-07-02 (CITIZEN test), expanded 2026-07-04 to the room-typed family,
   SHELVED 2026-07-04 after confirming CITIZEN worked in-game, un-shelved + expanded to SLAVE 2026-07-11,
   NOBLE added 2026-07-29. Class accessors are all player classes: `HCLASSES.CITIZEN()`.names="Plebeians",
   `.SLAVE()`.names="Slaves", `.NOBLE()`.names="Nobilities". Built clean v71.40; SLAVE + NOBLE gameplay
   verification pending (CITIZEN confirmed pre-shelving). To shelve again: re-comment the five
   CLASS_*-marked blocks; no save-format impact. New `CLASS_` `BoostableCat`
   (prefix applied by `BOOSTING.push`, so push `"CITIZEN"`→`CLASS_CITIZEN`, `"CITIZEN_MINE"`→`CLASS_CITIZEN_MINE`).
   Multiplies **per-subject** boostables only for subjects of the matching `HCLASS`. **Design (two
   disjoint key kinds — updated 2026-07-29):** a **bare per-class** key `CLASS_<CLASS>` scales the three
   need-growth rates **`RATES_HUNGER`/`RATES_THIRST`/`RATES_SHOPPING`** (single underscore — confirmed via
   m2 `71.19/boost_dump.txt`; higher = needier, the intended trade-off cost — see [[rates-need-semantics]]).
   A **room-typed** key `CLASS_<CLASS>_<ROOMTYPE>` scales **ONLY** that class's output across all rooms of
   the type via the mod's `ROOM_<ROOMTYPE>_ALL` umbrella (base 1.0) — **it no longer scales the need rates**
   (removed 2026-07-29 per user; room-typed keys are a pure skill/output boost, the neediness trade-off
   lives solely on the bare key). The umbrella's `UmbrellaBooster.pget→umbrella.get(o)` cascade carries the
   per-class factor into each `ROOM_<TYPE>_*` child's per-employee read at `IndustryUtil.java:80`
   `bonus.get(h.indu())`. Registered order matters: `registerUmbrellaCascade(MINE_ALL…)` runs before the
   CLASS block, so `ROOM_MINE_ALL` exists when `registerClassKey` looks it up. In `registerClassKey`, the
   room branch now sets `targets = { roomAllKey }` (was rates+umbrella). **Live now:** `CLASS_CITIZEN` /
   `CLASS_SLAVE` / `CLASS_NOBLE` (3 rates each) + `CLASS_CITIZEN_MINE` / `CLASS_SLAVE_MINE` (ROOM_MINE_ALL
   only → attach to 1 boostable, fans to 6 mine types). FARM/REFINER/WORKSHOP for CITIZEN/SLAVE are
   commented one-liner `registerClassKey(...)` calls. **Note replaced target:** the old CITIZEN ore-only
   `ROOM_MINE_ORE` target is gone — mine output flows through `ROOM_MINE_ALL` on the `_MINE` keys (all
   mines, not just ore). Effect = nested `ClassTreatmentBooster extends Booster`
   (multiplicative, `from()=to()=1`, `getValue(input)=input`, `pget(o)=o.boostableValue(value)`);
   `vGet(Induvidual)` returns `CLAMP.d(classKey.get(indu), CLASS_MIN, maxCap)` iff `indu.clas()==hclass`,
   neutral `1.0` for the other five `vGet` overloads. **TARGET_RACE/TARGET_CLASS fix (2026-07-29):** the
   factor is read with the **subject's Induvidual** (`classKey.get(indu)`), NOT `.get(player)` — earlier it
   read `.get(player)`, which is BROKEN for filtered grants because [[target-race-tech]]'s
   `TargetFilteredBooster.pget` returns identity for any non-Induvidual query (a `TARGET_RACE`-filtered
   CLASS grant is removed from `tech.boosters` and only fires for matching Induviduals → `.get(player)`=1.0
   = silently ignored). Reading `.get(indu)` makes the filter match the affected subject; identical to
   `.get(player)` for unfiltered grants (per-Induvidual resolves via faction→player tech value). New helper
   **`registerClassKey(cat, hclass, roomType, roomAllKey, activity)`** builds key/display/desc/targets and
   delegates to `registerClassTreatment(..., maxCap)`. **Min floor 0.5 via clamp; max cap CLASS_MAX=1.5 for
   all keys EXCEPT the bare `CLASS_NOBLE` needs key, which is UNCAPPED (`maxCap=Double.MAX_VALUE`, user
   2026-07-29 — floor still 0.5).** **Zero-multiply safety-net** (user-required): `registerClassTreatment`
   skips any `baseValue==0` target; the 0.5 floor keeps the factor away from the zero boundary. Full design:
   `../../sos-scripting-template/.claude/SPEC_CLASS_KEYS.md`. Expansion is data-only — **every added target
   must be a per-`Induvidual` boostable** (or a `ROOM_*_ALL` umbrella that reaches one); non-boostable stats
   need the reflection path (higher risk, see [[stat-system-seams]]).

   **NOBLE office keys (added 2026-07-29) — a different mechanism from CITIZEN/SLAVE room-typed keys.**
   Nobles don't work rooms; they hold **offices** (`game.nobility.NobleOffice`, list `GAME.NOBLE().OFFICES`,
   built in `NobleOfficeUtil.make()`): a **Governor** (targets `CIVIC_GOV`, leaves map) + one **"Master of
   <building>"** office per production/knowledge building type (targets that room's `bonus()` worker-skill).
   Each office adds `C = office.add * CLAMP.d(office.value(GAME.NOBLE().allocations(office)),0,1)` to its
   target (delivered by the engine's `BoostCompound.Boo`, additive, faction-scoped via `BValue.BValueFaction`).
   `registerNobleOffices(classCat)` creates **one `CLASS_NOBLE_<CATEGORY>` key per office category** (grouped
   by target key prefix in `nobleOfficeCategory()`: ROOM_MINE_→MINE/"Mining", …, CIVIC_GOV→GOVERNOR/"Governing";
   unknown→OFFICE/"Offices") **plus `CLASS_NOBLE_ALL`**, then attaches a nested **`NobleOfficeBooster`**
   (additive, `isMul=false`, `from()=to()=0`, `getValue`=identity, `BValueFaction` player-only) to each
   office's `office.target`. Its supplement = `C * (avgFactor-1)` → net office contribution `C*avgFactor`,
   scaling ONLY the office's own part (base/tech/race untouched). CAT + ALL stack. Display "Nobles
   (<aspect>)" / "Nobles (All Offices)". **TARGET_RACE/TARGET_CLASS (2026-07-29):** `avgFactor` is the
   **allocation-weighted average over the office's HOLDING NOBLES** of `clamp(catKey.get(nobleIndu),0.5,1.5)
   * clamp(allKey.get(nobleIndu),0.5,1.5)`, read with each holder's `n.subject().indu()` so the filter
   matches the NOBLE (not the room's workers). `supplement()` iterates `GAME.NOBLE().active()` for nobles
   whose `office()==this`, weight `slots=1+NOBLES.RANK_INCREASE*rank`; **memoised per `GAME.updateI()` tick**
   (hot per-worker read — mirrors the engine `Boo` cache). **Off-map Governor caveat:** `n.subject()==null`
   → fall back to `.get(player)` (unfiltered), so a race-filtered `CLASS_NOBLE_GOVERNOR` won't reach an
   off-map governor. (Only Governor `leavesMap()`; Master-of-X nobles stay on map.)

   **CONTENTMENT → OFFICE SCALING (2026-08-03, user):** each holder's per-noble factor is now
   `nobleEffFactor = techBoost × contentment(noble)`, where `techBoost = clamp(catKey)*clamp(allKey)` and
   `contentment ∈ [NOBLE_CONTENT_FLOOR=0.5, 1.0]`. **Why:** the CLASS_NOBLE "Contentment" cost (need-rates)
   was inert — nobles have **no engine happiness/loyalty** (`STANDINGS.java:34-35` builds StandingCitizen
   only for CITIZEN + SLAVE, per-(class,race) aggregate, never per-Induvidual; nobles are outside both).
   The one per-noble signal that exists and that CLASS_NOBLE already moves: their **need levels** (StatsNeeds
   accumulates for all non-tourist Humanoids incl. nobles, `:167,171`). So `contentment(indu)` = mean of
   `1 − level/max` over the 3 CLASS need STATs (`STATS.NEEDS().SNEEDS`, match `sn.need.rate.key` ∈
   CLASS_RATE_TARGETS, read `sn.stat().indu().get/max(indu)`), mapped linearly onto [0.5,1]. Result: a
   discontent noble runs their office worse **even with no tech** (a change from vanilla), and `CLASS_NOBLE>MUL:<1`
   (faster needs → less satisfied) is finally a real cost that weakens the offices. Non-redundant with
   CLASS_NOBLE_ALL (that's a flat tech mul; this is a dynamic per-noble gate on live need-satisfaction).
   Off-map Governor → contentment 1.0 (needs not simulated). Built clean.
   **Timing OK:** NOBLES ctor at GAME.<init>:167 and `game=this` at :117, both before initBeforeGameInited(:173).
   **Re-entrancy safe:** office.value reads employment (not boostables); CLASS_NOBLE_* are distinct boostables
   from the target. **Accuracy:** additive supplement is exact for factor≥1 (buffs); slight under-scaling for
   factor<1 combined with a MUL on the same room bonus (reduction lands in engine `sub` pool, unmultiplied) —
   accepted, WORLD_PLUNDER-style. **GOVERNOR key scales `CIVIC_GOV`** (user opted in; see [[cac-prrr-govpoints-conflict]]).
   Built clean v71.40. **In-game DIAGNOSTIC 2026-08-04 — the mechanism WORKS.** Log line proved: for
   office "Master of Herb Farms" (target `ROOM_FARM_HERB`), `computeSupplement` found the holder
   (counted=1), read catKey/allKey correctly, and produced `supplement=-0.641` from the noble's
   `contentment=0.74` (unboosted) — i.e. a discontent noble already reduces that office's contribution
   2.5→1.86. So the supplement reaches the room bonus and contentment scaling is live. **Why it looked
   inert:** the effect lands on the room's `bonus()` (shows in the ROOM's production-boost breakdown /
   actual output), but the **noble office panel renders `NobleOffice.hoverValue` = `value×add`, computed
   directly from the office and bypassing all boosters — so no boostable-based effect can EVER show there.**
   The per-type→per-room mapping is fine (CLASS_NOBLE_FARM correctly hit the per-room Herb-Farms office).
   Lifecycle ruled out: `game=this`(GAME:117) → NOBLES(:167) → initBeforeGameInited(:173) →
   createInstance(:187), all one instance, so captured `office` refs are live. **Display: the noble office
   PANEL (`NobleOffice.hoverValue`) renders `value×add` directly and CANNOT be augmented cleanly** (no
   booster seam; only bytecode-patching, which is the dead javassist route). So visibility lives in the
   ROOM's production-boost breakdown (`IndustryUtil.hoverBoosts` lists every additive booster on the room
   bonus by name — user-confirmed the modifier shows there). **Two-line split (2026-08-04):** each office
   now gets TWO `NobleOfficeBooster`s (a `contentmentPart` flag) so the room breakdown itemises **"Noble
   Contentment"** = `C*(avgContent-1)` and **"Noble Class Boost"** = `C*(avgEff-avgContent)`; they sum to
   the net `C*(avgEff-1)`. `avgContent`/`avgEff` = allocation-weighted means of contentment and of
   tech×contentment over holders. Diagnostic logging removed. **CONFIRMED WORKING in-game 2026-08-04
   (two lines show in the room production breakdown; contentment penalty + class boost behave as designed).
   The whole CLASS_NOBLE office system (per-office keys + ALL + contentment scaling) is done.**

9. **`TARGET_RACE` / `TARGET_CLASS` tech-scoping keys** — **moved into this mod 2026-07-24 from the
   former standalone `target-race-tech` mod, and generalized to add `TARGET_CLASS`.** These are NOT
   boostables — they are extra keys placed on a **TECH node** (alongside its `BOOST:` block) that restrict
   that tech's boosts to a subset of subjects. New self-contained package **`your.mod.targetfilter`**
   (public entry `TargetFilters`; `MainScript.initBeforeGameCreated()` → `TargetFilters.scan()`,
   end of `initBeforeGameInited()` → `TargetFilters.install()`). Accepted values: `TARGET_RACE` = any race
   key under `assets/init/race/` (discovered dynamically); `TARGET_CLASS` = `CITIZEN`/`NOBLE`/`SLAVE`/`OTHER`
   (→ `HCLASSES.*()`, matched via `Induvidual.clas()`) **plus synthetic `EXSLAVE`** = freed slave, matched
   by `STATS.POP().COUNT.arrive.get(ind)==CAUSE_ARRIVES.EMANCIPATED()` ("Subjects that are freed slaves";
   deliberately NOT `PAROLE`, which the engine documents as pardoned *prisoners*). A tech may declare both
   keys → subject must match **all** (logical AND; `SubjectFilter`). **Architecture** (unchanged from the
   original, see [[target-race-tech-project]] for the deep dive): `TargetFilterRegistry.scanTechFiles()` in
   `initBeforeGameCreated` reads tech files (before `new TECHS()` parses them, so the custom keys never trip
   the unknown-key warning — `ParseWarningSuppressor` flips `Json.untest` + scrubs the `Errors` buffer for
   BOTH keys); `TargetFilterApplier.applyAll()` runs from a `BOOSTING.waiting` action (queued reflectively —
   field is package-private in v71) after BoostSpecs resolve but before BoostCompound aggregates: for each
   flagged tech it pulls the `BoostSpec`s out of `tech.boosters` and reinstalls each as a
   `TargetFilteredBooster` on the same `Boostable` via `addFactor` (identity `×1`/`+0` for non-matching
   subjects and non-`Induvidual` targets; vanilla per-level MUL "stretch" `(to-1)*levelMax+1` preserved);
   a `BOOSTING.connecter` re-adds the originals to `tech.boosters` for the tech-tree UI after aggregation.
   **Only per-subject (`Induvidual`) boost targets can actually be narrowed** — region/faction/division
   targets have no subject to test and yield identity. **Retirement:** all three deployed
   `target-race-tech.jar` copies were deleted (standalone `V71/script` + CAC `V70`/`V71`) so TARGET_RACE
   isn't applied twice (the double-apply is mostly self-neutralizing since the 2nd applier sees an emptied
   `tech.boosters`, but a tech with BOTH keys would lose its class constraint if the old race-only jar won
   the race). Built clean v71.40, deployed; in-game verification pending. Player doc: `KEYS.md`
   "Tech-scoping keys".

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
