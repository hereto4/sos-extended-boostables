# HANDOFF — Search/Stealth review for urliamo

**Branch:** `feature/stealth-alertness-boostables` · **Head at review:** `dd2b849` (code unchanged from
`3b0b09a`) · **Mod version:** 1.5.0 · **Game:** Songs of Syx v71.44

**How this was verified:** the whole mod was type-checked with `javac` against the real v71.44 game jar
(16 sources, **0 errors**), and every engine claim below was read out of the shipped
`SongsOfSyx-sources.jar` (`info/` in your game install — it is real source, not decompiled). Nothing
here comes from memory or guesswork. Where a finding is *inferred* rather than *read*, it says so.

**Nothing in this document contradicts your test.** Your log line proves the feature genuinely works.
What it cannot show is what happens off the happy path, and that is what this covers.

---

## 1. What your test confirmed

```
[sos-extended-boostables] stealth/alertness: crime reinforced-reported
  (guard=0.6613077521324158 vs perp=0.21803614497184753,
   stealth=0.8499563196673989 alertness=1.270659782923758).
```

That single line establishes a lot, all of it good:

- **Opt-in works.** The `[HeroesMod]` line above it means HeroesMod shipped `V71/StealthAlertness.txt`
  and the keys registered. The gate is genuinely inert otherwise (verified at bytecode level —
  `CrimeStealthCheck.update()` compiles to `isInstalled()` then `ifne` then `return`, one static boolean read).
- **`InnateBooster` works.** `stealth=0.84996` is *below* the 1.0 base, so a negative innate roll
  (about -0.150) landed and survived `BUtil.value`'s separate `sub` accumulator. `alertness` is about +0.271.
  Both inside `[0.5, 1.5]`, consistent with `INNATE_SPREAD = 0.5`.
- **Both rolls respect their bounds** (`perp` < stealth, `guard` < alertness), so `RND.rFloat(d)` is
  being used correctly.

**But the log is structurally blind to every problem below.** `resolve()` only prints inside
`if (guardRoll > perpRoll)` — losses are silent, mailbox state is never logged, and nothing downstream
of `reportCriminal` is logged at all. A clean log is not evidence of absence here. **See §6 item 2 for
the one-line change that fixes this.**

---

## 2. BLOCKING — the `REPORT_ATTEMPTS` design, and why it was never necessary

### 2a. The policy that caused it does not exist

`CrimeStealthCheck`'s javadoc said (now corrected on this branch — see §2d):

> *"there is no way to intercept those without reflection, and **per project policy this mod does not
> use reflection**."*

**There is no such policy, and there never has been.** I searched every doc in this repo
(`CLAUDE.md`, `SPEC.md`, `KEYS.md`, `README.md`, all of `.claude/memory/`) and — importantly —
`HANDOFF_SEARCH_STEALTH.md`, the spec you worked from. That file mentions reflection **zero times**.
Nothing you were handed told you this.

The documented position is the opposite:

- `doc/howto/access_game_code.md` — *"Reflection is a mighty tool to do all sorts of 'forbidden' things
  in Java."*
- `doc/howto/add_ui_element.md` — *"I would suggest you create a `ReflectionUtil` class..."*
- **This mod already ships two reflection sites**, both authored by hereto4 and both still working:
  `your.mod.targetfilter.TargetFilters.queueWaitingAction` reaches the package-private
  `BOOSTING.waiting` field (it was `public` in v70, package-private in v71), and
  `ParseWarningSuppressor` reaches `Json.untest` plus the engine error-buffer internals.

This is not your error — you inherited a belief that reads as project canon. But it steered the design
into the workaround in §2c, which is actively harmful, and it should be corrected in both directions.

### 2b. The real rule

There *is* a rule, and it is much narrower. It exists because of one incident:
`STAT_WORK_RETIREMENT` reflectively pinned `StandingCitizen.maxes`/`defs` every ~2 s. That is a no-op
only while those denominators stay static during play — true in v70.32, **false after the v71 standing
rework** — so it inflated fulfillment, then happiness, then produced **runaway immigration on a fresh
save with no techs unlocked**. It failed *silently*: the reflection kept succeeding, so the try/catch
guard never fired. Disabled 2026-06-27; still commented out in `MainScript.java`. Full write-up:
`SPEC.md` header and `.claude/memory/reference_immigration_happiness_bug.md`.

> **Never use reflection to write or pin engine state that the engine itself recomputes.**
> Reflecting to *read*, or to reach a member whose visibility changed between game versions, is fine —
> provided it runs once, and has a non-reflective fallback.

| | Don't | Fine |
|---|---|---|
| Operation | writes final fields / private arrays | reads, or appends to a collection |
| Frequency | every tick, forever | once, or one-shot per event |
| Vs. the engine | fights it for ownership of a recomputed value | reaches a member whose *visibility* moved |
| Fallback | none that helps (it "succeeds" wrongly) | yes — a still-correct non-reflective path |
| Precedent | `STAT_WORK_RETIREMENT` — broke on the v71 bump | `TargetFilters`, `ParseWarningSuppressor` — fine |

Now recorded in `CLAUDE.md` (`## Reflection`) and `.claude/memory/feedback_reflection_policy.md`.

### 2c. What the workaround actually costs

`CrimeReporter.reportCriminal` opens with a flat coin flip (`if (RND.rBoolean()) return;`), so the code
fires it **seven times** to compound past it: `1 - 0.5^7 = 99.2%`. The arithmetic is right. The problem
is the other half of the distribution.

Read `CrimeReporter` carefully:

- Per-guard-house storage is `Alloc.ii(EE*2 + 2)` with `EE = 5` — **capacity 5 crime entries**.
- `push()` silently drops when full. **There is no de-duplication** — seven calls for the same criminal
  push the same id repeatedly.
- When the instance queue fills, `report()` calls `b.finder.report(b.service.get(ins), -1)` which is
  `reportAbsence`, and `SFinderRoomService.getReservable` gates on
  `findableReservedCanBe()` = `reporter.available(ins)`. **That guard house stops being findable for
  any crime.**

Expected pushes per reinforcement = 3.5 into a 5-deep queue.
**P(at least 5 of 7 succeed) = (21+7+1)/128 = 22.7%.**

So roughly **one reinforcement in four completely fills an empty guard house's mailbox**. Your logged
line means this already happened ~3.5 times during your test; you simply could not see it.

**And it does not heal.** This is the part I got wrong on first pass and want to flag precisely, because
it is subtle. `report()` de-registers based on the **instance** queue (a local `data` shadows the
field):

```java
int[] data = data(ins);            // instance array, capacity 5
boolean av = available(data);
push(type, payload, data);
if (av && !available(data)) b.finder.report(b.service.get(ins), -1);
```

But `pollCriminal()`'s re-register check reads the **global** field:

```java
boolean av = available(data);      // field - the 255-slot global array
int id = pop(tCrime, data(ins));   // pops the instance array
if (!av && available(data)) b.finder.report(b.service.get(ins), 1);   // never fires
```

Draining the instance queue never flips `av`, so the `+1` never happens. The only thing that restores
presence is `GuardInstance` construction (`GuardInstance.java:49`). And `FindableDataSingle.has()`
resolves to `get(superComponent) > 0` — a per-component count. **With a single guard house in a path
component, one full queue means no crime can be reported at all until that room is rebuilt.**

> **Fairness note, and please read this one:** the global-vs-instance mismatch is a **vanilla engine
> bug**, reachable without this mod whenever five genuine crimes stack at one house. You did not author
> it. What the mod does is raise the trigger probability from "rare" to ~22.7% per reinforcement. The
> framing is *amplifier*, not *cause*.

### 2d. Recommended rework

Pick either; the first is simpler and probably enough.

**Option A — fire once, let the sweep compound.** Replace the 7-call burst with a single
`reportCriminal` per won contest and drop `REPORT_COOLDOWN` to ~4 s. Each sweep is an independent 50%
flip, so three won contests already give ~87% cumulative, with **at most one duplicate per push** and
no burst that can fill a queue. Zero new machinery.

**Option B — one reflective push.** A single, one-shot, fallback-able call into `CrimeReporter` that
skips the internal coin flip sits squarely in the "Fine" column of §2b: it reads/appends rather than
pinning recomputed state, runs once per event, and can fall back to `reportCriminal()` verbatim if
reflection fails. This is the same shape as `TargetFilters.queueWaitingAction`, which has been in
production here since 2026-07-27.

Whichever you pick, **also gate on queue headroom**: `SETT.ROOMS().GUARD.reporter` exposes
`available(GuardInstance)` and `crimes(GuardInstance)` publicly. Skipping reinforcement when the target
house is near full removes the failure mode entirely, and is worth doing under Option A too.

**The javadoc is already corrected on this branch** — `CrimeStealthCheck`'s class comment now records that
the no-reflection choice was self-imposed rather than a project rule, so the next reader does not inherit
it. Nothing for you to do there; it is noted only so the change does not surprise you in the diff.

---

## 3. Findings, by severity

Everything below was read from v71.44 source. "Confirmed" = deterministic, no in-game test needed.
"Inferred" = the mechanism is confirmed, the real-world magnitude is not.

### High

| # | Finding | Status |
|---|---|---|
| **H1** | **Mailbox flooding / guard-house de-registration** — see §2c. | Confirmed; magnitude inferred |
| **H2** | **`isCriminal()` is far broader than "committing a crime."** `AIModule_Crime:218` returns true for (a) `a.indu().hostile()` — i.e. **every raider during a siege** (`Induvidual.hostile()` = `hType().isHostile()`); (b) **every PROSECUTION-marked subject, permanently** — that is a player *decree* (`view/sett/ui/standing/decree/DPanel.java:119`) applying to CITIZEN/SLAVE; (c) the real crime flag. So with any prosecution decree active, or during a raid, the sweep treats hundreds of subjects as criminals. Each costs a ~101x101-tile grid scan (`getInProximity(e, 50)` — radius is in **tiles**, `C.T_SCROLL = 6`), a Bresenham trace per guard candidate, and up to 7 `finder.reserve()` calls, **each a real `findDest` pathfinding search out to distance 90**. | Confirmed; cost **not profiled** |
| **H3** | **Detection is near-certain; Stealth barely matters.** `criminal` is set in `commitCrime()` (`:179`) and cleared **only** in `init()` on hType switch (`:233`) — i.e. on capture. Verified: those are the only two write sites. So contests run every `PERIOD` until capture; the cooldown only applies *after* a win. Using **your own logged values** (s=0.84996, a=1.27066): P(guard wins) = 1 - s/(2a) = **66.6% per contest**, leaving 33.4% / 11.2% / **3.7%** still unreported after 1 / 2 / 3 contests. **~96% reported within 6 game-seconds**, and your criminal's roll of 0.218 changed nothing. At the default 1.0 vs 1.0 it is 50%/contest, so 87.5% within 6 s. | **Confirmed by arithmetic** |

### Medium

| # | Finding | Status |
|---|---|---|
| **M1** | **The growth mechanic is asymmetric and makes detection strictly better over a save.** `GROWTH_STEP_MAX = 0.01` averages ~0.005/contest; `GROWTH_CAP = 3.0` needs ~600 contests. A criminal gets ~3-4 contests before capture (H3), so **~0.02 stealth per career**, and they are caught each time. A guard is "best guard" for every criminal in range and belongs to a persistent population, so alertness accrues all session. "Practice makes perfect" only ever helps the guards. | Confirmed |
| **M2** | **The key description does not match the mechanic.** `BEHAVIOUR_STEALTH` is described as "ability to commit crimes unseen," but `criminal` is set *inside* `commitCrime()` — **after** vanilla's own detection roll. Stealth cannot affect whether a crime is spotted; it only affects evasion afterwards. Fix the description, the KEYS.md entry, or the trigger. | Confirmed |
| **M3** | **The feature has no content-level safety net.** Every previous key in this mod is inert because nothing references it. This one runs at full strength on pure vanilla content the moment the config flag is set — both keys default to base 1.0, so every criminal and guard enters the contest at 1.0 vs 1.0. Consider skipping subjects still at the authored baseline, so authoring content is required for the mechanic to bite. | Confirmed (design) |
| **M4** | **Unbounded map growth.** The four `IdentityHashMap`s are cleared only on load. `Induvidual` is a real per-humanoid object (`new Induvidual(...)`, not pooled), so entries pin the individual's whole `long[] data` stat block. *Correction to my first pass:* entries are created only for contest participants and hovered individuals, **not every citizen** — so this is tens of MB over a very long session, not a session-killer. Real, but not urgent. | Confirmed; severity **lower** than first reported |

### Low

| # | Finding | Status |
|---|---|---|
| **L1** | `SETT.PATH().solidity` is **walkability, not visibility** (`PATHING.java:154` — `availability.player < 0`, "solid from the players viewpoint"). Water, cliffs and unclaimed terrain block line of sight; a guard cannot see across a pond. Workable proxy — the `TileLOS` javadoc just oversells it. | Confirmed |
| **L2** | **Range checks disagree.** `COORDINATE.tileDistance` is **octile** (`sqrt2*min + (max-min)`), not Chebyshev as `TileLOS`'s javadoc claims, while `TileLOS`'s own gate is `max(dx,dy) > maxRange`. Effective radius is 50 tiles orthogonally but **~35 diagonally**. | Confirmed |
| **L3** | `GrowthBooster.from()/to()` both return 0 while growth reaches 3.0. `BUtil.min/max` feed the tooltip's min-to-max span and `Boostable.progress()`, so the UI advertises 0.5-1.5 for a stat that can reach 4.5. (Inherited from `IndoctrinationBooster`, which has the same flaw.) | Confirmed |
| **L4** | The lazy innate roll consumes the **global** `RND` (one static `Random`) from inside `vGet`, which the UI reaches via `Boostable.hover` / `UIBonus`. So hovering a citizen permanently fixes their disposition and perturbs the shared RNG stream. Harmless in practice; roll eagerly in the sweep if you want determinism. | Confirmed (see §5) |
| **L5** | `getAllEnts()` iterates the full **60,000**-slot array (`ENTETIES.MAX 40000 + 20000`) every sweep regardless of population. `ENTITIES().Imax()` bounds it. | Confirmed |
| **L6** | `getInProximity` returns a **shared mutable `temp` list** (`ENTETIES.java:472`). `findBestGuard` iterates it while calling `Boostable.get()`. Safe today because only this feature's two boosters are attached — but a dependent mod attaching a booster that does its own proximity query would clobber it mid-iteration. Worth a copy-out or a comment. | Confirmed latent |

---

## 4. Verified correct — no action needed

Recording these so you do not re-litigate them:

- **Opt-in gating is correctly scoped.** `install()` is the **final statement** of
  `initBeforeGameInited()`, so its early return cannot skip any other feature's registration; the
  per-tick guard is the first bytecode in `update()`; `<clinit>` for both hot-path classes touches
  nothing but `java.util.IdentityHashMap` (no static blocks anywhere in `your.mod.stealth` or
  `your.mod.los`); and `StealthAlertnessConfig.isEnabled()` wraps the mod scan in three layers of
  `catch (Throwable)`, so a malformed file in an unrelated mod returns `false` rather than aborting init.
- **Boostable indices do not shift.** Registering last means the two new keys take the highest indices;
  no existing key moves. `ROOMS.RBonus` guards with `bo.index() >= map.length` anyway, and the two
  `for (Boostable b : BOOSTING.ALL())` loops in `MainScript` filter on `ROOM_`/`CONSUMPTION_` prefixes.
- **`TargetFilters` still sees the new keys** — it defers onto `BOOSTING.waiting` / `BOOSTING.connecter`,
  which drain at `finishSetup()`, after every push.
- **`HTYPES.GUARD()` is the right guard test** (`AIModules.switchType` assigns it to active-duty soldiers).
- **No reservation leak** from repeated `reportCriminal` — guard `Service.findableReserve()` is an empty
  stub (`Service.java:28`). This was my first suspicion and it is clean.
- **The sweep correctly pauses with the game.** At `speed == 0` the engine passes `ds = 0`, so the timer
  does not advance.

---

## 5. Explicitly ruled out — do not chase this

I found `CORE.java:110` starting a separate `Updater` daemon thread alongside a tracked `glThread`, and
nearly reported the lazy `InnateBooster.roll()` map mutation as a **cross-thread data race** on a plain
`IdentityHashMap`.

**It is not one.** `Updater.run()` is a single sequential loop —
`update(); CORE.getInput().poll(current); render();` — so simulation and UI render on the *same* thread,
strictly serialized. `glThread` only services marshalled `GlJob`s. There is no concurrent map access
anywhere in this feature. L4 is a determinism smell, nothing more.

Flagging it because "threading bug" in a review is the kind of lead that costs a day.

---

## 6. Your test vs. what is still uncovered

Not covered by the run you did: **growth** (both values sat near their innate rolls, so the
`GROWTH_CAP` path is entirely unexercised), **sieges or prosecution decrees** (H2 untouched), **multiple
criminals against one guard house** (H1 cannot manifest), and **outcome-level before/after** (H3
unconfirmed where it matters — did crime actually stop happening?).

Four cheap tests, roughly in order of value:

1. **`grep` your existing full log** for repeated lines with the same stealth/alertness pair. If one
   criminal produces a line every ~15 s until caught, H3 is confirmed from data you already have.
2. **Log the losses too** — one line in the `else` branch of `resolve()`. Single highest-value change;
   it turns a success-only feed into a real sample, and most of the rest follows from it.
3. **The Law panel** already charts `occurence()` vs `caught()` per crime per race. Compare a save with
   the feature off vs on — a free A/B on H3 with no code at all.
4. **Enable a prosecution decree, or let a raid land**, and watch frame time and log volume. That is H2,
   and it will present as "the game got slow," not as "the stealth mod is broken."

---

## 7. Open questions only you can measure

1. **How many guard houses share a path component** in a typical settlement — the difference between H1
   being "bad" and "crime reporting stops entirely until a rebuild."
2. **Actual frame-time cost of the sweep.** I reasoned about work-per-sweep but did **not** profile it.
   H2's cost claim is directional, not measured. Note `ds` is **game-time**, scaled by game speed
   (`GAME.update`: `ds *= speed`, then subdivided into 1/16 s steps), so `PERIOD = 2.0` is 2 *game*-seconds
   — at 5x speed the sweep fires ~5x more often per real second, which is exactly when settlements are large.
3. **How many individuals a PROSECUTION decree marks at once.** H2's severity scales directly with this;
   I confirmed the decree wiring but not the batch size.
4. **Whether per-individual tooltips actually render the new BEHAVIOUR keys** — only affects L4, which is
   minor either way.

---

## 8. Build / repo notes

- **`pom.xml` is now gitignored on this branch.** It carries machine-specific absolute paths
  (`game.install.directory`, `game.workshop.directory`, `game.mod.uploader.directory`), and
  `game.install.directory` is read by `install-game-jar` in the **validate** phase — so a pom pointing at
  another developer's Steam install fails the build before it compiles anything. Keep your local copy;
  it will no longer round-trip between machines. (If you would rather keep the pom shared, the
  alternative is moving those three paths into `~/.m2/settings.xml` properties.)
- Eclipse metadata (`.classpath`, `.project`, `.settings/`) is also gitignored now. Both it and the pom
  were still *tracked* at the time of writing — `.gitignore` does not untrack — so they need a
  `git rm --cached` to take effect.
- Version is **1.5.0**; `_Info.txt` regenerates from the pom on build, so do not hand-edit it.
- `HANDOFF_SEARCH_STEALTH.md` (the original research spec) now lives in this repo root as the single
  canonical copy — the duplicate in the control-room repo was removed. Edit it here.

---

## 9. Summary

The architecture is sound, and the hardest part — genuinely inert opt-in — is correct and verified at the
bytecode level. Three things need work before this merges:

1. **Rework `REPORT_ATTEMPTS`** (§2d). The javadoc that justified it is already corrected here.
2. **Narrow the trigger** so `hostile()` and prosecution-marked subjects do not drag the whole settlement
   through the sweep (H2), and so Stealth actually gets a say (H3, M1).
3. **Decide what the keys mean** — right now Stealth cannot affect committing a crime unseen (M2).

Everything else is small. None of it was visible from the log you had, which is why item 2 of §6 is worth
doing before anything else.
