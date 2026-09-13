# HANDOFF — Search / Stealth skill for Songs of Syx: feasibility evaluation

**Status: RESEARCH COMPLETE (static analysis), DESIGN SKETCHED, NOTHING IMPLEMENTED.**
No code has been written. No build exists. One step remains before design can be finalised:
an **in-game observation pass** (Part IV), which must be run by a human with a running game.

**Audience:** a developer on a **different machine and a different repo** from where this research was
done. This file is deliberately **self-contained** — every finding, line reference, seam, design
decision and open question is reproduced here. You do not need any other document from the
originating repo. Where a companion doc existed there, its relevant content has been folded in.

| | |
|---|---|
| Game version all findings were read against | **Songs of Syx v71.44** |
| Research dates | 2026-08-02 → 2026-08-08 |
| Consolidated into this file | 2026-09-11 |
| Target mod (originating project) | a boostable-key mod (`sos-extended-boostables`) that registers custom `BOOSTING.push` keys. **Not required** — the design works in any script mod |
| Implementation status | **none** |
| Blocking next step | Part IV, item 1 (in-game observation) |

---

## 0. The one-paragraph summary

Songs of Syx **has no search mechanic and no stealth mechanic**. The words do not appear anywhere in
the engine source or the data folder. What it has instead is a **report queue**: a crime is either
pushed into a guard-house mailbox or it isn't, and guards pop that mailbox while standing still
inside a guard house. Patrols detect nothing. Once a guard reaches a criminal, capture is
**deterministic** — no evasion roll exists anywhere. Critically, **three of the six crime types
(Theft, Disrespect, Speech) are never reported and never witnessed**, so in an unmodded game those
criminals are caught only by accident. That is the gap a Search key fills, and the report queue is
**public API**, so both a Search and a Stealth concept can be layered on additively without touching
any sealed engine internals. The feature is **feasible**; the remaining risk is behavioural, not
technical.

---

## 1. Prerequisites — getting the sources this file cites

Every `File.java:NNN` below is from the **official game source**, not a decompilation. The game
ships its own source jar; extract it and you have real parameter names, comments and generics:

```bash
unzip -q -o "<game install>/info/SongsOfSyx-sources.jar" -d game-source-v71.44
grep -E 'VERSION_MAJOR|VERSION_MINOR' game-source-v71.44/game/VERSION.java   # confirm version
```

Vanilla content/config values cited (e.g. `LAW.txt`, `_GUARD.txt`) come from the data archive:

```bash
unzip -q -o "<game install>/base/data.zip" -d "SoS Base Game (Data Folder)"
head -3 "SoS Base Game (Data Folder)/base/txt/Patchnotes.txt"   # cheapest version marker
```

Paths in this file are written **relative to the extracted source root** (e.g.
`settlement/room/law/guard/CrimeReporter.java`) or to the extracted data root (`data/assets/...`).
Prefix them with wherever you extracted.

> ⚠️ **Line numbers are v71.44, read 2026-08-08.** If your game has moved on, **re-locate every
> symbol by name** rather than trusting `File.java:NNN`. The *claims* have all been verified; only
> the line offsets are fragile.

---

# PART I — How vanilla actually works

This is the "how the machine works" layer. Everything in Parts II–IV rests on it.

## 2. TL;DR — the complete list of gates between "crime" and "arrest"

There is no skill roll against a stat anywhere in the chain. The whole "did they get away with it?"
question collapses to these discrete gates:

| Gate | Where | Effect |
|---|---|---|
| Crime type decides whether the crime is announced at all | the six `AIPLAN`s in `ai/crime/` | **Theft, Disrespect and Speech are never reported or witnessed.** Murder / Vandalism / Flashing are |
| A flat 50 % coin flip on every report | `CrimeReporter.reportCriminal:57` | `if (RND.rBoolean()) return;` — half of all reportable crimes dropped, unconditionally |
| Guard house within 90 **path**-tiles? | `CrimeReporter.report:81` | yes → queued at that house; no → only a 25 % chance of landing in the settlement-wide queue |
| Mailbox full? | `CrimeReporter.push:97` | 5 slots per guard house, 255 settlement-wide; a full queue **silently drops** the report |
| Pursuit succeeds? | `AISub_follow` | 20 re-path attempts; deterministic capture once Manhattan distance ≤ 1. **No evasion roll** |

Once a guard reaches the criminal, capture is **certain**. There is no dodge, no chase-off, no
"criminal notices the guard and runs" — the criminal keeps committing its crime while being walked
down. **"Criminals avoiding detection" in vanilla means *never being reported*, not *escaping*.**

One event type overrides all of it: a **riot** (§7) jams every mailbox with rioter self-reports *and*
pulls every nearby guard into `PlanMop`, so for the 0.25–1.0 game day it lasts, the settlement has no
crime response at all.

## 3. Cast of characters

| Thing | Class | Notes |
|---|---|---|
| Would-be criminal | any `Humanoid` whose `HTYPE` isn't `GUARD` | citizens and slaves both; the crime module is on nearly every type |
| Crime intent | `AIModule_Crime` (`ai/crime/AIModule_Crime.java`) | one bit (`commitCrime`) + one bit (`criminal`) packed into `AIModules.data().byte1` |
| The crime itself | 6 `AIPLAN`s: `Theft`, `Murder`, `Vandalism`, `Flasher`, `Disrespect` (×2 — also used for Speech), `SerialKiller` | |
| Rioter | `Humanoid` with `HTYPES.RIOTER()` (`HCLASSES.OTHER()`, `isHostile()`) | mass-converted citizens; runs **only** `AIModule_Rioter`. Third report channel and settlement-wide disruptor — §7 |
| Guard | `Humanoid` with `HTYPES.GUARD()` | **a soldier**, not an employee — §5 |
| Guard behaviour | `AIModule_Guard` + `PlanWork` / `PlanPatrol` / `PlanGear` / `PlanMop` / `PlanExecute` (`ai/types/guard/`) | |
| The mailbox | `CrimeReporter` (`settlement/room/law/guard/CrimeReporter.java`) | the whole detection system, 200 lines |
| Guard house | `ROOM_GUARD` / `GuardInstance` | `maxRadius = 90` |
| Capture → punishment | `AIEventListeners.followCriminal` → `AIModule_Prisoner` | |
| Bookkeeping | `STATS.LAW()` = `StatsLaw`, `StatCrime`, `CrimesData` | |

## 4. Becoming a criminal

`AIModule_Crime.update` (`AIModule_Crime.java:130`) runs **16× per game day per humanoid**
(`Humanoid.HumanoidResource.updatesPerDay = 16`, ticked at `Humanoid.java:294`).

```java
if (STATS.LAW().getCurfew().isSetForADay()) {
    commitCrime.set(d, 0);                       // curfew wipes intent outright
} else {
    double r = BOOSTABLES.BEHAVIOUR().LAWFULNESS.get(a.indu());
    if (a.indu().clas() == HCLASSES.CITIZEN()) {
        double pop    = STATS.POP().POP.data(HCLASSES.CITIZEN()).get(null) + 1;
        double guards = STATS.POP().POP.type().get(HTYPE_RACE.get(a.race(), HTYPES.GUARD())) + 1;
        r *= (pop + guards) / pop;               // more same-race guards -> fewer crimes
    }
    if (r < 0) r = 0;
    r *= 16*16*5;                                // = 1280
    r += 16;
    if (RND.oneIn(r)) commitCrime.setMax(d);     // 1-in-r per update
}
```

- `LAWFULNESS` is the **only** boostable in the intent path. `StatLawBoosts.lawfulness()` feeds it
  three vanilla boosters: population size (big city = lawful, `1 − (1000/pop)^1.5` clamped),
  happiness, and `CIVIC_LAW × 2`.
- Guards suppress crime **twice over**: here (the `(pop+guards)/pop` factor) and via `CIVIC_LAW`
  (`StatsLaw.guards` → `sqrt(0.5 × guardPower / pop)`, added multiplicatively in `StatLawBoosts:52`).
- **Guards are immune**: `if (a.indu().hType() == HTYPES.GUARD()) return;` (`:136`) — an early return
  placed *after* the persecution report, so guards can still be persecuted but never turn criminal.
- Once `commitCrime` is set, `getPriority` returns **6**, which beats work/needs/idle but loses to
  `AIModule_Prisoner` (10).

**Which crime** is decided by `STATS.LAW().prisonerType` — a per-individual `CRIME` slot set at
population-add time (`CrimesData` addable, `:115-131`) by a weighted roll over
`c.tyrrany(class, race)`, i.e. **how much that race resents the crime being punished**. So a race's
`crimeFreedom` preferences decide what its criminals *do*, not just how they feel about punishment.
Individuals marked by the **Prosecution decree** are re-rolled to a real crime the moment they act
(`:77-94`).

Vanilla crime list and weights — `data/assets/init/config/LAW.txt`:

| Crime | FREEDOM (tyranny if punished) | LAW | Class |
|---|---|---|---|
| `MURDER` / `S_MURDER` | 0.025 | 0.25 | citizen / slave |
| `THEFT` / `S_THEFT` | 0.05 | 0.25 | citizen / slave |
| `VANDALISM` | 0.1 | 0.25 | citizen |
| `FLASHING` | 0.15 | 0.25 | citizen |
| `DISRESPECT` / `S_DISRESPECT` | 0.25 / 0.20 | 0.25 | citizen / slave |
| `SPEECH` | 0.3 | 0.25 | citizen |
| `PLEASURE` / `S_PLEASURE` | 1.0 | 0.0 | the **Persecuted** pseudo-crime; `isCriminal = false` |
| `WAR` | 0 | 0 | `HCLASSES.OTHER()` — war criminals |

## 5. Guards are soldiers, not staff

There is no "hire a guard" job. `AIModule_Guard.shouldBe` (`:83`):

```java
Div div = STATS.BATTLE().DIV.get(a);
return div != null && SETT.ROOMS().GUARD.activeDuty.is(div);
```

Put a **division on Active Duty** (`ROOM_GUARD.activeDuty`, a `Bitmap1D` over army division slots)
and `AIModules.switchType` (`:298`) flips its soldiers from `SUBJECT` to `HTYPES.GUARD()`; clearing
Active Duty flips them back (`:330`). A `GUARD` humanoid runs only two modules: `guard` and `battle`
(`AIModules:102`).

- **Guard houses do not create guards.** `ROOM_GUARD.emp` employs *existing* guards into stand-spots;
  with no active-duty division a guard house is an empty building.
- **Guard Power** (`GuardPower`) = the battle power of every active-duty division, computed one
  division per game update round-robin. It feeds `StatsLaw.guards` → `CIVIC_LAW`, so guard *quality*
  matters to law and crime rate — but **not** to detection or capture, which are power-blind.
- `AIModule_Guard.getPriority` is 0 once `WORK_TIME ≥ 1` (shift done), 4 below 0.5, and 4 above 0.5
  only if their guard house still has a free stand-spot.

### 5.1 What guards actually do

`AIModule_Guard.getPlan` (`:45`), in order:

1. `PlanGear` — fetch missing battle equipment.
2. `PlanExecute` — man an execution station if one reported work.
3. If `SETT.ROOMS().GUARD.emp.employ(a)` succeeds → `PlanWork`: **stand on a marked spot inside a
   guard house** (`codeStand` tiles, `guardSpot`), facing the direction the furniture dictates, in
   5–20 s `STAND_SWORD` bouts. **Between bouts it calls `reporter.pollCriminal(ins)`**; a 1-in-10
   chance per bout sends it wandering to another guard house instead.
4. `reporter.pollCriminal(null)` — check the settlement-wide queue. **Only reached when step 3
   failed**, i.e. no guard house had a free stand-spot. An employed guard returns at step 3 and never
   executes this line — and loses nothing, because `pollCriminal(ins)` drains the house queue *and
   then falls through to the global one* (§6.2).
5. `PlanPatrol` — join a patrol.

**So the settlement's actual crime-response throughput is "number of guards standing on guard-house
spots × one poll per 5–20 s bout".** Steps 4 and 5 are what surplus guards do.

`PlanMop` is force-overwritten from `update()` whenever enemies or rioters are on a connected path
component (`AIModule_Guard.update:110`).

> **`PlanMop` is the one exception to "no scanning" — and the only radius scan in the whole guard AI.**
> `PlanMop.res` (`:58`) calls `SETT.PATH().finders.otherHumanoid.enemy(a, 64)`: a genuine pathfinding
> sweep out to **64 tiles** for a humanoid whose `hType().isHostile()` differs from the guard's
> (`SFinderHumanoid.enemy:105`). If found — and if friendlies outnumber hostiles 2:1 in the local path
> components — it routes straight into `AI.listeners().catchCriminal(aa)`; otherwise the guard flees.
>
> Two limits keep this out of the ordinary-crime path: it is gated behind `AIModule_Guard.hasEnemies`
> (`:118`), which returns false unless `HTYPES.ENEMY()` or `HTYPES.RIOTER()` population is non-zero;
> and `enemy()` matches on **`isHostile()` htype**, not on `isCriminal()`. So it finds invaders and
> rioters, **never a thief or a murderer**. Rioters are captured by the same `followCriminal` pursuit
> code as criminals, which is why they share it.
>
> **The gate is two-part, and the second part bounds the blast radius** (`hasEnemies:118-133`).
> Population non-zero is only the cheap pre-check; it then requires hostiles in the guard's **own path
> component or one edge-hop away** (`PATH().comps.data.people(a.indu().hostile())` over `ss` and
> `ss.edgefirst()`). A riot does not flip *every* guard — it flips every guard within one component
> hop of a rioter. Since `EventCitizenRiot` seeds rioters by a **connectivity flood fill** (§7), they
> are by construction spread across contiguous walkable space, so in a normal single-component city
> that is effectively all of them.
>
> **And the flip is not one-shot.** `AIModule_Guard.update` calls `d.overwrite(a, mop)`
> *unconditionally* whenever `hasEnemies` is true (`:110-115`), and `update` runs 16×/day per
> humanoid. A guard cannot stay in `PlanWork` (and therefore cannot call `pollCriminal`) for as long
> as the riot lasts; an in-flight `followCriminal` pursuit of an ordinary criminal is also overwritten
> away.
>
> **For modding this matters twice over:** it is the **existing engine precedent** for a proactive
> guard-side scan (Part III), and `SFinderHumanoid` shows the intended idiom — a component-gated
> `SFINDER` rather than a brute-force entity loop.

### 5.2 How a plan lives and dies — the `PLANRES` / `Resumer` contract

Generic to all settlement AI, not just guards, but every claim below about "when does a guard
re-check X" rests on it. Sources: `AIPLAN.java` (all 276 lines) and `AIManager.setNextState`
(`:374-440`).

An `AIPLAN.PLANRES` is a **state machine of `Resumer`s**. Each `Resumer` registers itself into
`plan.resumers` at construction and gets a byte index; the *currently active* one is
`d.planResumerByte`, one byte of per-humanoid AI state. Four abstract methods:

| Method | Called when | Contract |
|---|---|---|
| `setAction(a,d)` | on `resumer.set(a,d)` — which first writes `planResumerByte = index` | returns the `AISubActivation` to run, or `null` |
| `res(a,d)` | the running sub finished successfully | returns the **next** `AISubActivation`, or `null` |
| `con(a,d)` | periodically, see below | `false` ⇒ cancel the plan now |
| `can(a,d)` | on cancel | cleanup (release reservations, return spots) |

`AIManager.setNextState` drives it once per AI frame:

```java
state = sub.resume(a, this);
if (state != null) {                      // sub still producing states
    if (stateI == 30) {                   // every 30th transition only
        stateI = 0;
        if (!plan.shouldContinue(a, this)) { plan.cancel(); sub.cancel(); newPlan(a); }
    }
} else {                                  // sub finished
    if (!sub.isSuccessful(a, this) || !plan.shouldContinue(a, this)) {
        plan.cancel(); sub.cancel(); resourceDrop(a); newPlan(a);
    } else {
        AISubActivation s = plan.resume(a, this);   // -> resumers.get(planResumerByte).res(a,d)
        if (!setSub(s)) { resourceDrop(a); newPlan(a); }   // setSub(null) == false
    }
}
```

Three consequences, all load-bearing:

1. **`res()` returning `null` ends the plan** and calls `newPlan(a)` → `AIModules.getNextPlan` → the
   module `getPlan` chain. This is the *only* routine way a module re-runs plan selection.
2. **`con()` is polled, but sparsely** — every 30 state transitions while a sub is mid-run, and once
   whenever a sub completes. It is the cheap "abort" channel.
3. **There is no priority preemption.** `AIModules.update` computes `nextModulePrio` 16×/day, but
   nothing acts on it — grep shows it is read by exactly two plans that *volunteer* to yield
   (`PlanInterract:551`, `WorkEmissary:30/80`). A running plan runs to completion regardless of what
   became more urgent, unless something explicitly calls `d.overwrite(...)`.

That last point is why `NOTIFY_CRIME` is handled by `overwrite` in four separate guard plans rather
than by raising a priority: **events are the only interrupt mechanism the engine has.**

> **You cannot add a `Resumer` to an engine plan** — `resumers` is populated at construction,
> `PLANRES` subclasses are `final` or package-private, and index bytes are positional.

### 5.3 Patrols are scenery, not sensors

`Patrols` holds a fixed **16 `Patrol` objects**, each a moving formation of `MAX = (1+1*2)*8 = 24`
slots (3 wide × 8 deep). A guard entering `PlanPatrol` reserves one slot index out of 16×24 = 384,
walks to `patrols.pos(index)` and stands facing `patrols.dir(index)`.

The patrol *route* is `Patrol.find()` — a flood fill from a random guard house (or the throne if
none) out to `120 + rnd(120)` tiles, cost-weighted to prefer urban, open, floored, non-room tiles
heading away from the throne. Re-picks a destination every 3–6 in-game hours, crawls at
`speed = 0.5` tiles/sec.

> **A patrol does not detect anything.** No proximity check, no scan, no reporting. The only route by
> which a patrolling guard catches an ordinary criminal is the `NOTIFY_CRIME` event
> (`PlanPatrol.event:301`) — i.e. the 8-tile witness radius, same as any bystander. The visible patrol
> is pure flavour plus body-presence.

It also cannot fall back on the reporter queue, and the reason follows from §5.2 rather than from
anything in `PlanPatrol`. `pollCriminal(null)` lives in `AIModule_Guard.getPlan`, which only runs on
`newPlan` — so a patrolling guard polls the queue exactly as often as its plan ends. Tracing the
three `PlanPatrol` resumers against the contract:

- every `con()` returns `true`, so the 30-transition `shouldContinue` check never kills it;
- `beBraced.res` (`:255`) returns `retry2(...)`, and `retry2` returns `null` **only** on shift end
  (`WORK_TIME ≥ 1`), a null division, or a null/disconnected slot coordinate;
- `cutToPosition.res` → `retry2`; `pathToPosition.res` → `retry2` or the next path step, `null` only
  on path failure.

So the plan is a closed loop that ends on shift boundaries and error conditions, not on a timer.
**A guard patrols an entire shift without ever reading a report.**

> **Dead code worth knowing about.** `beBraced.res` reads
> `if (d.subByte < 50 || !isInPosition(...)) return retry2(a, d);` and otherwise turns to the patrol
> facing and stands. That `else` is **unreachable**: `retry2` on an in-position guard calls
> `beBraced.set`, whose `setAction` sets `d.subByte = 0` (and `AISUB.Simple.activate` zeroes it again,
> `AISUB.java:128/134`). The counter oscillates 0↔1 and never reaches 50.

Compounding all of it: patrol is the **last** branch of `getPlan`, reached only when
`SETT.ROOMS().GUARD.emp.employ(a)` fails. Patrolling guards are precisely the surplus beyond
guard-house stand-spot capacity — so a settlement with a large active-duty division and few guard
houses puts most of its guards into the one state that is blind to the report queue.

**Corollary, and one of the most useful findings in this document: guard-house stand-spots, not guard
headcount, are what convert reports into arrests.**

## 6. Detection — the report queue

Two independent channels feed it; a third event type (riots, §7) both feeds and breaks it.

### 6.1 Channel A — the witness broadcast (`NOTIFY_CRIME`)

`AIModule_Crime.notify(criminal)` (`:210`):

```java
for (ENTITY e : SETT.ENTITIES().getInProximity(criminal, 8))
    if (e instanceof Humanoid) HEvent.Handler.notifyCrime((Humanoid) e, criminal);
```

Radius **8 tiles**, everyone in it, **no line-of-sight, no roll**. `AIEventListeners:135` dispatches:

- `AI.modules().work.isLawEnforcement(a, d)` — i.e. `HTYPES.RECRUIT()` **or** `HTYPES.GUARD()`
  (`AIModule_Work:267`) → `d.overwrite(a, followCriminal)`, drop everything and chase.
- anyone else who isn't a `PRISONER` → **flee**.
- Guard plans also intercept the event themselves so it lands mid-task: `PlanWork:189`,
  `PlanPatrol:302`, `PlanGear:92` each `overwrite` into `catchCriminal`.
- **Every crime plan swallows the event** (`if (e.event == HEvent.NOTIFY_CRIME) return false;` —
  Theft:73, Murder:195, Vandalism:92, Flasher:72, Disrespect:70) without calling `super`, so the
  criminal itself never flees, not even from someone else's crime.
- `InterBattle:54` also swallows it — a guard already fighting ignores crime.

**Recruits count as law enforcement**, which is easy to miss: an untrained recruit walking past a
murder chases and captures exactly as well as a guard.

### 6.2 Channel B — the guard-house mailbox (`CrimeReporter`)

```java
public void reportCriminal(Humanoid a) {
    if (RND.rBoolean()) return;                              // 50% dropped, flat
    report(tCrime, a.tc().x(), a.tc().y(), 90, a.id());
}

private void report(int type, int sx, int sy, int radius, int payload) {
    COORDINATE c = b.finder.reserve(sx, sy, radius);         // nearest guard house within 90
    if (c != null) {
        GuardInstance ins = b.getter.get(c);
        int[] data = data(ins);                              // per-house array, 5 crime slots
        boolean av = available(data);
        push(type, payload, data);
        if (av && !available(data))
            b.finder.report(b.service.get(ins), -1);         // house full -> unfindable
    } else if (RND.oneIn(4)) {
        push(type, payload, data);                           // settlement-wide, 255 slots
    }
}
```

- `b.finder.reserve(sx, sy, 90)` is a **pathfinding** search (`SFinderFindable.reserve:116`), not a
  radius circle — walls and unreachable ground genuinely hide a crime from a nearby house.
- The payload is the criminal's **entity id**, so the report survives the criminal moving.
- `push` returns `false` and **silently discards** when the array is full.
- `pollCriminal` pops **LIFO** (newest report first) and re-validates: entity still exists, is a
  `Humanoid`, not removed, and `AI.modules().isCriminal(a)`. Stale entries are discarded as it walks
  the stack. Per-house entries are drained before the settlement-wide ones.
- `available()` doubles as the room's findability: a house with a full mailbox is reported absent
  from the finder map, so new reports route elsewhere.
- The same object also carries `tExecution` reports (`reportExecution`, radius 180) packing tile
  coords into one int — same storage, different stride.

**Callers of `reportCriminal` — the complete list** (exhaustive grep):

| Caller | Condition |
|---|---|
| `AIModule_Crime.update:133` | `(updateOfDay & 0b011) == 0 && PROSECUTION.markIs(a)` — persecuted subjects self-report **4× per day, forever** |
| `AIModule_Crime.commitCrime:175` | `notify == true` **and** the crime's punishment isn't `PARDON` |
| `AIModule_Rioter:82` | **unconditional**, once, when the `riot` plan's `go` resumer first activates |
| `AIModule_Rioter:101` | `RND.oneIn(5)`, on **every** `go.res` cycle — continuously for the whole riot |

### 6.3 The asymmetry that decides everything: `notify`

`commitCrime(a, d, notify, crime)` (`:169`) only reports/broadcasts when `notify` is `true`:

| Crime plan | `commitCrime(..., notify, ...)` | Extra `notify()` calls | Net |
|---|---|---|---|
| `Theft:48` | **false** | none | **silent — never detectable** |
| `Disrespect:48` (also Speech) | **false** | none | **silent — never detectable** |
| `Vandalism:73` | true | none | reported once |
| `Flasher:38` | true | `notify()` again on each 1-in-4 re-run (`:53`) | very loud |
| `Murder:78` | true (on reaching the victim) | `notify()` after every stab, every chase leg, and the cool-down (`:107,:137,:157`) | loudest |
| `SerialKiller` | never calls `commitCrime` | — | invisible by design; waits until the victim is **off-screen** (`leave.res:125` checks `VIEW.s().getWindow()`) before killing |

So in an unmodded game **a thief or an agitator is caught only if the Prosecution decree happens to
have marked them, or if a riot sweeps them up.** Everything the player perceives as "the guards
caught someone" is a murderer, a vandal, a streaker, a persecuted subject or a rioter.

**But the *statistics* see all of it.** `commitCrime` (`:169-181`) increments the crime counter
**before** and independently of the `notify` gate:

```java
void commitCrime(Humanoid a, AIManager d, boolean notify, CRIME crime) {
    STATS.LAW().crimes.get(crime.index()).commit(a.indu());   // always
    STATS.LAW().prisonerType.set(a.indu(), crime);
    if (notify && ...punishment(a.indu()) != PARDON) {         // only sometimes
        SETT.ROOMS().GUARD.reporter.reportCriminal(a);
        AIModule_Crime.notify(a);
    }
    ...
}
```

`StatCrime` keeps **two** parallel `HistoryObject<HCLASS_RACE>`s — `occurence` (bumped by `commit`)
and `caught` (bumped by `catchh`, only from `catchPrisoner`) — and the Law panel prints both (§9.3).
So the silent crimes are **visible as a statistic while being invisible as an event**: a vanilla save
should show Theft/Disrespect/Speech accumulating in "Crimes" with "Arrests" pinned at or near zero.
**That is the cheapest possible test of this entire document, and Part IV item 1 is built on it.**

> ⚠️ **`catchh` is charged to the individual's `prisonerType`, not to what they were caught doing**
> (`catchPrisoner:190-193`). Whoever a guard knocks out increments the `caught` counter of whatever
> crime slot that individual happens to carry. A captured **rioter** therefore posts an arrest against
> their assigned crime — which can be `THEFT` — even though no theft was ever reported. **This is the
> one way the "Arrests: 0 for theft" prediction can be muddied without the prediction being wrong.**

## 7. Channel C — riots, and what they do to channels A and B

Read in full: `AIModule_Rioter.java` (282 lines), `EventCitizenRiot.java` (208 lines),
`EventCitizen.update/getAmount`.

**Trigger.** Riots are not a crime behaviour at all — they are a **citizen-discontent event**.
`EventCitizen.update` ticks every `timerD = 15` s (does nothing below 15 citizens). Each tick it
computes, per race, a rebel count from loyalty:

```java
double m = max(STANDINGS.CITIZEN().loyalty.getD(r), loyaltyTarget.getD(r));
if (m >= breakPoint /* 0.85 */) return 0;
m = 1.0 - m/breakPoint;
m *= 0.1 + 0.9*clamp(totalPop/600, 0, 1);      // small settlements rebel less
rebels = pop_citizen(r) * m;
```

With rebels present a `count` meter drains; the ladder is **emigration → the "Ungrateful plebs!"
warning → `riot.riot(amounts)`**, and after each riot `emigrate` is re-armed `!RND.oneIn(3)`, so
roughly a third of the time the next escalation is another riot. **Sustained citizen loyalty below
0.85 is the whole cause.**

**Seeding is a flood fill, not a random draw.** `riot(int[] races)` finds the first `SUBJECT` of an
eligible race, then flood-fills outward across walkable tiles (`SETT.PATH().solidity`), converting
every eligible `SUBJECT` it reaches with `a.HTypeSet(HTYPES.RIOTER(), null, null)` until each race's
quota is spent. Rioters are therefore a **contiguous blob on one path component** — exactly the
condition `AIModule_Guard.hasEnemies` tests (§5.1). It also calls
`STANDINGS.emergency(CITIZEN, 4 days)` and `GAME.count().RIOTS.inc(1)`.

**What a rioter does.** `modules[RIOTER]` is `{ rioter }` — one module, one plan, key `"riot"`
(`AIModules:112`). `AIModule_Rioter.getPriority` returns 11, which is moot: nothing else is in the
list. The `go` resumer:

- on activation: `AIModule_Crime.notify(a)` + `reporter.reportCriminal(a)`, then `SubFlee`;
- on every `res`: `notify(a)` again (the 8-tile witness broadcast, **every cycle**),
  `reportCriminal` at `RND.oneIn(5)`, then randomly a fist/grab/lay animation, a stand, or more
  fleeing;
- `res` returns `null` — ending the plan — **only** when the humanoid stops being a `RIOTER`.

`SubFlee` is genuinely just fleeing: turn by a random ±90°, jog 2–5 s, and if standing in
`WATER.DEEP` for 2–16 consecutive cycles the rioter **drowns** (`CAUSE_LEAVES.DROWNED`). There is no
target selection, no arson, no attack code in this module — the riot's damage comes from the battle
module rioters *don't* have and from `InterBattle.listener` handling combat events, plus
`TargetMap:115` letting rioters target graves. **The event description's "murdering and vandalizing"
is flavour text, not this code path.**

**Capture.** Rioters go through the ordinary criminal machinery by two routes: the 8-tile `notify`
broadcast (any guard/recruit → `followCriminal`), and `PlanMop`'s 64-tile hostile sweep →
`catchCriminal` → the same `followCriminal` → `knockCriminal` → `AI.modules().makePrisoner`.
`makePrisoner` calls `criminal.catchPrisoner(h)`, which **always returns `true`** and charges
`catchh` to the individual's `prisonerType` (§6.3's warning).

**Ending.** `EventCitizenRiot.update` polls every 10 s:

| Path | Condition | Result |
|---|---|---|
| Crushed | live rioters < **30 %** of original count | *every* remaining rioter converted on the spot: `RND.oneIn(5)` → `PRISONER` (`CAUSE_LEAVES.PUNISHED`), else back to `SUBJECT` |
| Timed out | `secondsToRiot` (0.25–1.0 game days, rolled at riot start) reaches 0 | every rioter reverts to `SUBJECT`, **no arrests at all** |

The crushed branch converts **all** survivors at once, so the 1-in-5 prison draw applies to the whole
remainder in a single tick — killing rioters past the 70 % mark buys nothing extra.

### 7.1 The interaction that matters for a Search/Stealth design

A riot simultaneously:

1. **floods the mailboxes** — every rioter self-reports on activation and then at 1-in-5 per cycle
   (× the internal 50 % flip ⇒ ~1-in-10), into the same 5-slot-per-house / 255-global arrays. `push`
   silently drops on full; and
2. **stops anyone draining them** — every guard within a component hop is force-overwritten into
   `PlanMop` 16×/day (§5.1), and `pollCriminal` only runs from `PlanWork` bouts and `getPlan`.

**During a riot the detection system is both jammed and unattended**, while the arrest counters still
move (from rioters). Any mod-side sweep must treat "a riot is running" as a **distinct regime**
rather than assuming its `reportCriminal` calls will land. This is Open Decision 3 in Part III.

## 8. Pursuit and capture

`AI.listeners().catchCriminal(other)` stashes the target in a **static** `AIEventListeners.tmp` and
returns the shared `followCriminal` plan (`:214`). *(Static single-slot handoff — read it
immediately; it is not safe to hold.)*

`followCriminal` (`:252`) is three `Resumer`s:

1. **`first`** — `AI.SUBS().walkTo.follow(a, d, tmp, run = true, trials = 20)`. `AISub_follow` stores
   the target id in `d.planObject`, paths to its tile, re-paths on arrival/collision up to 20 times.
   Success = `isSuccess`: **Manhattan distance ≤ 1** (`AISub_follow:65`). A `MEET_HARMLESS` collision
   with the target short-circuits straight to the knockout.
2. **`knockCriminal`** — turn to face, stop, `AI.modules().makePrisoner(target, targetAi)`, play the
   `box` animation. **No roll.** `makePrisoner` overwrites the victim's AI with a "lay unconscious"
   plan that flips them to `HTYPES.PRISONER()`.
3. **`killCriminal`** — if `AIModule_Prisoner.punishment(...) == EXECUTE`, the guard keeps beating
   them where they stand: `+0.2 + rnd(1.0)` injuries per cycle, gore, and at `> 0.75` a kill with
   `CAUSE_LEAVES.EXECUTED()` and `GAME.count().EXECUTIONS`. Street-side execution, no room needed.

`followCriminal` also swallows `NOTIFY_CRIME` (`:378`) so a chasing guard doesn't switch targets.

### 8.1 So how *does* a criminal get away? — the exhaustive list

1. Their crime is `Theft` / `Disrespect` / `Speech` (never reported, never witnessed).
2. The 50 % coin flip in `reportCriminal`.
3. No guard house reachable within 90 path-tiles **and** the 75 % global-queue miss.
4. Every candidate mailbox is full (5 per house / 255 global).
5. No guard/recruit within 8 tiles at the moment of `notify()`, **and** nobody ever polls their queue
   entry before it goes stale.
6. `isCriminal(a)` goes false before the poll — punishment for their crime is set to `PARDON`, or the
   individual leaves/dies. `pollCriminal` then throws the report away.
7. The pursuit exhausts its 20 path attempts (target unreachable — behind a closed door, on a
   disconnected component).
8. The guard is interrupted by something higher priority — `PlanMop` (enemies/rioters), battle, or
   shift end (`WORK_TIME ≥ 1` kills `AIModule_Guard`'s priority).
9. **A riot is running** (§7). Gate 8 at settlement scale plus gate 4 at settlement scale.

**Note what is *not* on that list:** speed, sneaking, hiding, darkness, crowds, disguise, or any
attribute of the criminal whatsoever. `TIME.light().nightIs()` gates only the serial killer's
activity (`AIModule_Crime.getPriority:200`) and the prisoner stocks — **night has no effect on
detection.**

## 9. After capture, and what the player is actually shown

`HTYPES.PRISONER()` → `AIModule_Prisoner` (priority 10, beats everything):

- **Judged**: if `crime.isJudged` and not yet judged → `Judged` plan at a `ROOM_COURT` (`_COURT.txt`:
  service `NEED: COURT`, radius 500). With no court staffed, `judgeWait` maxes and they go to jail
  untried.
- **Stocks**: during daylight, `Stocked` puts them in `ROOM_STOCKS` for public display. (The
  `_PUNISHMENT` environment — `CIVIC_LAW>ADD: 0.2` plus an inverted happiness standing — is emitted
  by `_EXECUTION.txt` (`VALUE: 0.5, RADIUS: 1`), **not** by the stocks.)
- **Punishment** comes from `StatCrime.autoPunishment[HCLASS_RACE]` — the per-crime, per-class,
  per-race decree the player sets in the Law UI: `PARDON`, `NONE`, `BANISH`, `PRISON`, `EXECUTE`
  (arena/temple/street), `HARVEST` (cannibal), `ENSLAVE`.
- **Escape is not an AI behaviour — it is a room failure.** `PrisonInstance`/`StockInstance`
  accumulate a `riotChance` from understaffing and missing supplies; at 0, every prisoner in that room
  is `kill(false, OTHER)`-ed and `STATS.LAW().escapeInc()` is called. Escapees decay
  (`CrimesData.up`) and feed a `CIVIC_LAW` penalty booster (`StatLawBoosts:43`, span 1 → 0.25).

### 9.1 The Police room is not a detective office

`ROOM_POLICE` / `PoliceWork` **abducts random subjects, criminal or not**. `validVictim` (`:134`)
excludes only: removed entities, anyone employed at a guard or police room, and anyone already inside
the room. A worker fetches a "suspect" (`WorkPolice.initBegin:38`, `follow(..., run = false)`),
delivers them, and after `2 + rnd(2)` ticks there is a **1-in-5 chance the victim is killed**
(`PoliceWork:44`, `CAUSE_LEAVES.PUNISHED()`). In exchange the room grants
`BEHAVIOUR_LAWFULNESS>ADD: 10`, `LOYALTY>MUL: 4`, `SUBMISSION>MUL: 4`, `HAPPINESS>MUL: 0.25`
(`_POLICE.txt`). It is a terror/interrogation building, and it is the closest vanilla thing to a
"search" mechanic — **but it searches the innocent.**

### 9.2 The guard house's own aura

`_GUARD.txt` declares `ENVIRONMENT_EMIT: { _GUARD: { VALUE: 1, RADIUS: 1 } }`, and
`settlement/environment/_GUARD.txt` gives that env `CIVIC_LAW>MUL: 2.0` with `DECLINE_VALUE: 1.0`.
`Constructor.envValue` (`:381`) overrides the flat numbers per tile: only `codeStand` tiles emit, the
radius is scaled by `GuardInstance.eff` and the value is `1/15` (or `2/15` in a 3-direction cone for
the `gg` furniture item). `eff()` is `(1 − degrade×0.5) × (upgrade + 1) × (employed / max)` — so a
decayed, unmanned or un-upgraded guard house projects a smaller law aura. Again: this affects **law
and crime rate, not detection.**

### 9.3 The Law panel — the readout a Search/Stealth key needs already exists

Read in full: `view/sett/ui/law/` (`UILaw`, `UILawCrimeList`, `CrimeChart`, `LawChart`, `Selector`,
`WarCriminals`), `settlement/room/law/guard/Gui.java`, `settlement/room/law/police/Gui.java`,
`settlement/stats/law/StatCrime.java`.

**The UI never claims detection.** Nothing in the law UI, the guard-room UI or the tooltips says
guards search, patrol *to find*, or have any chance to spot anything. The strongest claim anywhere is
the `HTYPES.GUARD` blurb — *"Guards are soldiers on guard duty. They spend their days guarding your
city, catch criminals and make your citizens obedient"* — which is true as written (they do catch
criminals) and simply silent on how a criminal comes to their attention. The riot event text likewise
says only *"Guards will also do their fair share."*

> **The player is never promised a mechanic that isn't implemented. A Search key is filling a gap,
> not fixing a lie.** That matters for how the feature is described to players.

**The Law panel is `ISidePanel` `UILaw`**: tabs for Citizen / Slave (`UILawCrimeList`) + War Criminals
(`WarCriminals`), plus a Curfew toggle, plus a race `Selector` (all-races or one race).

`StatCrime` maintains two `HistoryObject<HCLASS_RACE>` series over `STATS.DAYS_SAVED` days, and the
per-crime hover box prints all three of these per crime **and per race** (`StatCrime:428-455`):

| Label | Source | Meaning |
|---|---|---|
| `In Custody` | `criminals(race)` | live count currently held |
| `Crimes (today/yearly)` | `occurence().get(r)` / `.history(r).getPeriodSum(-16, 0)` | **committed** — bumped by `commit()`, unconditionally, for every crime including the silent three |
| `Arrests (today/yearly)` | `caught().get(r)` / same | **caught** — bumped only by `catchPrisoner` |

`CrimeChart` stacks daily `occurence` by crime type; `LawChart` charts `Arrests` broken down by
*punishment*.

> **So "detection rate per crime type per race" is already a first-class, saved, charted statistic.
> A Search/Stealth key needs no new UI to be legible — it only needs to move the gap between those two
> series.** That is both the cleanest player-facing story for the feature *and* the A/B measurement
> instrument during development.

**The guard room's own panel exposes nothing about throughput** (`guard/Gui.java`): Efficiency,
Divisions, Soldiers, Power, the `_GUARD` environment percentage, Upgrade, and a division selector.
§5.3 concludes stand-spots are the real limit on arrests — **the UI does not surface stand-spots at
all.** The one place queue depth appears is behind `S.get().developer`: two `GStat`s printing
`blueprint.reporter.crimes(instance)` / `crimes(null)` and the same for executions (`:55-74`), i.e. a
**live per-house and settlement-wide mailbox depth readout in developer mode** — the single most
useful instrument for Part IV.

**The Police panel confirms §9.1.** `police/Gui.java` lists a "Suspects" column with rotating flavour
labels (*Suspected Witch / Warlock / Shapeshifter / Heretic / Turncoat / …*) and a Value stat whose
tooltip is *"The effect of your police force. Depends on the amount of police divided by the
population to keep in check. Diminishing returns."* Population-ratio terror, no investigation.

**War criminals never touch the reporter — drop them from scope.** `CRIMES.WAR()` is assigned at
*spawn* (`PeopleSpawner:151`, `Induvidual:52`) to captives who arrive already `HTYPES.PRISONER()`.
`WarCriminals` is a bulk-assignment UI over those existing prisoners, gated by `ROOM_STOCKADE`
capacity and feeding `PBuyerSlave`. There is no crime, no report, no capture — it is a disposal
screen, irrelevant to this feature.

---

# PART II — Modding seams

## 10. What is sealed

**Nothing in §6–§8 is overridable.** `AIModule_Crime`, `CrimeReporter.report/push/pop`, the crime
plans, and `followCriminal` are all `final` / private. Additionally, `script/ScriptLoad` performs a
`Class.forName` uniqueness check and throws `DataError` if a mod jar redefines a `game.*` FQCN — **you
cannot shadow or same-package-override any engine class.**

The settlement AI **need / service / plan system is likewise sealed**:

- **Needs** (`init/type/NEEDS.java`): file-defined needs in `data/assets/init/stats/need/*.txt` *are*
  auto-discovered (`NEEDS.java:40-43`) and each auto-mints a `RATES_<KEY>` boostable (`NEED.java:59`).
  **But a new file-defined need is never SOUGHT by AI** unless a plan is registered under its index —
  `S_Plans` ctor logs `LOG.err(n.key)` for any non-basic need with zero plans (`S_Plans.java:63-68`).
- **Service plans**: `S_Plan` is package-private abstract with a package-private ctor
  (`S_Plan.java:9,15`); concrete plans are added by a hardcoded `add(new …)` sequence in the
  package-private `S_Plans` ctor (`S_Plans.java:28-70`); the `add(...)` overloads are private.
  **No registration hook exists** (grep: no `addPlan`/`registerPlan`).
- **Free-standing services**: `StatServiceSkinny`/`StatServiceSimple` have package-private ctors;
  `StatsService` is `final` with a hardcoded ctor; `STATS.SERVICE()` returns one fixed instance.
- **Idle/bench plan** is a `private final AIPLAN` inside `AIModule_Idle` (`:167`). Not hookable.

**Takeaway: treat the AI plan/service/need registry as read-only. Anything built here has to be
additive, driven from a per-tick routine in `SCRIPT_INSTANCE.update()`.**

## 11. What IS public and usable — the seam inventory

| Seam | Signature | Use |
|---|---|---|
| **Report a criminal** | `SETT.ROOMS().GUARD.reporter.reportCriminal(Humanoid)` | **the "Search" hook** — call it yourself to detect crimes vanilla never reports (theft/speech), or to re-report past the 50 % flip. Note it re-rolls its own 50 % internally, so call twice for ~75 % |
| **Drain a report** | `CrimeReporter.pollCriminal(GuardInstance ins)` — `ins` may be `null` | **the "Stealth" hook** — popping an entry destroys it. Poll-and-discard a stealthy criminal's report and no guard will ever see it |
| Queue state | `crimes(ins)`, `executions(ins)`, `available(ins)` | read how backed-up a house is |
| Is this person wanted? | `AI.modules().isCriminal(Humanoid)` (`AIModules:151`) | true for hostiles, persecuted-and-not-pardoned, and flagged criminals |
| Force a guard onto a target | `AI.listeners().catchCriminal(Humanoid)` → `AIPLAN`, then `aiManager.overwrite(guard, plan)` | manufacture a pursuit; `AIManager.overwrite(Humanoid, AIPLAN)` is public (`:258`) |
| Identify what anyone is doing | `((AIManager) h.ai()).plan().key` — `AI.AIElement.key` is public | plan keys: `GUARD_PATROL`, `GUARD_GUARD`, `GUARD_GEAR`, `GUARD_EXECUTE`, `eventFloolowCrim`, `CrimeTheft`, `CrimeMurder`, `CrimeVandal`, `crimeFlash`, `crimeDisres`, `crimeSpeech`, `crimeSerial`, `prisStart` |
| Current pursuit target | `AIManager.planObject` (public `int`) → `SETT.ENTITIES().getByID(id)` | during `eventFloolowCrim` this is the hunted criminal |
| Broadcast a sighting | `HEvent.Handler.notifyCrime(Humanoid witness, ENTITY criminal)` (`HEvent:181`) | wake a specific guard, or widen the 8-tile radius yourself |
| Manual capture | `AI.modules().makePrisoner(h, (AIManager) h.ai())` | |
| Guard census / houses | `SETT.ROOMS().GUARD.instancesSize()`, `.getInstance(i)`, `.power.get()`, `.patrols`, `STATS.POP().pop(HTYPES.GUARD())` | |
| Proactive scan, engine idiom | `SETT.PATH().finders.otherHumanoid.enemy(h, radius)` / `.find(h, radius)` (`SFinderHumanoid`) | the pattern `PlanMop` uses; component-gated pathfinding sweep. Matches on `isHostile()` htype, so a criminal-finder needs its own `SFINDER` or a filtered entity pass |
| Entity iteration | `SETT.ENTITIES().getAllEnts()`, `.getByID(id)`, `.getAtTile(...)`, `.getInProximity(e, r)` | all public |
| Existing lever, no code | `BOOSTABLES.BEHAVIOUR().LAWFULNESS` and `BOOSTABLES.CIVICS().LAW` | already boostable from data files; changes crime *rate*, not detection |
| **Read the detection rate** | `STATS.LAW().crimes.get(i).occurence()` / `.caught()` — both `HISTORY_COLLECTION<HCLASS_RACE>`, `public` (`StatCrime:331,335`) | committed vs arrested, per crime, per race, with `DAYS_SAVED` history. Already charted (§9.3) — the natural before/after metric **and** the player-facing story |
| Post an arrest / a crime | `StatCrime.catchh(Race)` and `commit(HCLASS_RACE, int)` — both `public` | lets a mod-side capture stay consistent with the vanilla counters. Note `catchh` charges the *crime object it is called on*, so pick deliberately |
| Live queue depth | `reporter.crimes(ins)` / `crimes(null)`, surfaced in the guard room panel when `S.get().developer` (`guard/Gui.java:55-74`) | non-destructive backlog readout — the peek that `pollCriminal` doesn't give you |
| Is a riot running? | `STATS.POP().pop(HTYPES.RIOTER()) > 0` | the regime switch of §7.1 |
| Per-tile environment | `SETT.ENV().map` — read the `_GUARD` env value at a tile | proximity/AOE proxy for "how policed is this spot" |

### 11.1 🔴 If you ever command a humanoid: claim ownership FIRST

A sibling feature in the originating project shipped a **crash-to-desktop** by getting this wrong.
Recording it here because any Search implementation that force-marches a guard will hit it.

`AISUB_walkTo.coo()` writes the humanoid's **shared** `AIManager.path` *before* it knows a route
exists and never rolls back — `SPath.request` opens with `this.successful = false`. So a **failed**
`coo()` returns `null` *and leaves the path poisoned*. If that humanoid's active sub is an engine
`PathWalker` mid-route (the idle module walks constantly), its next resume hits `SPath.setNext`'s
`if (!successful) throw new RuntimeException()`:

```
AIManager.update:356 → setNextState:386 → PathWalker.resume:239 → PathWalker$1.res:58 → SPath:357
```

**The rule: `h.interrupt()` before you touch `d.path`.** The safe idiom:

```java
Humanoid h = (Humanoid) SETT.ENTITIES().getByID(id);
AIManager d = (AIManager) h.ai();
if (d.plansub() == null || d.plan() == null) return;   // mid-interruption: interrupt() would NPE
h.interrupt();                                          // = overwrite(a, AI.plans().NOP): cancels the
                                                        // live sub (runs abort() -> releases
                                                        // reservations), clears a pending
                                                        // interruption, cancels the plan, parks on
                                                        // NOP (sub = STAND, never reads d.path)
AISUB.AISubActivation walk = AI.SUBS().walkTo.coo(h, d, tx, ty);  // ONLY safe now
if (walk == null) return;                               // harmless: d.path dead but unowned
d.overwrite(h, walk);                                   // AIManager.overwrite(...,AISubActivation):229
```

Two further cautions from the same incident:

- **`AIManager.overwrite(a, AISubActivation)` does NOT cancel the sub it replaces**, so hijacking a
  citizen mid-task skips that sub's `abort()` and **leaks reservations** (seats reserved forever).
  `h.interrupt()` is what runs `abort()`.
- Keep an **ownership test** before every later `interrupt()`/`overwrite()` on a humanoid you claimed
  (e.g. `d.plan() == AI.plans().NOP`), and **never re-issue a walk mid-flight** to a humanoid the
  engine has already reclaimed.
- **Do not define a mod-owned `AIPLAN.PLANRES`.** `AIPLAN`/`AISUB` extend `AIElement`, which is
  persisted **by index** in `AIManager.save`, so a mod-defined plan/sub active at save time writes a
  mod-only index into the save file. Drive vanilla subs under vanilla `NOP` instead — saves stay clean
  whether or not the mod is present.

---

# PART III — Design sketch and open decisions

**Sketch only. Nothing is built.** Recorded so the next session does not re-derive it.

## 12. Shape the feature would take

- **Both keys are pure carriers**, exactly like any custom boostable: the engine reads neither, and a
  per-tick routine in `createInstance().update()` is the sole consumer.
- **Search** (guard-side; plausibly `CIVIC_SEARCH` in the CIVICS category, base 1.0) drives a per-tick
  sweep: for each `isCriminal` humanoid the game left silent, roll against `SEARCH.get(player)` scaled
  by local guard presence (e.g. the `SETT.ENV().map` `_GUARD` value at the criminal's tile, or distance
  to the nearest `GuardInstance`), and on success call `reportCriminal`. That directly buys the two
  things vanilla lacks — **detection of theft/speech, and a way to beat the 50 % flip.**
- **Stealth** (criminal-side, per-subject, base 1.0) is the counter-roll in the same pass, and
  additionally a **poll-and-discard**: on a stealth win, `pollCriminal` the entry out of the queue
  before any guard reads it. Because `pollCriminal` hands back a `Humanoid`, a mod can inspect and
  re-`reportCriminal` anyone it didn't mean to hide, so a discard pass **need not be lossy**.
- **Per-subject vs per-faction matters.** Search is naturally a player-faction value
  (`b.get(FACTIONS.player())`); Stealth is naturally per-`Induvidual` (`b.get(indu)`), which means
  races can author it (`BOOST: { ... }` in a race file) and it participates in `HCLASS_RACE` queries.
  Check `BValue`'s full method set (`vGet(Induvidual/Player/FactionNPC/Region/Div/HCLASS_RACE)`) before
  choosing.
- **Registration**: `BOOSTING.push(pushKey, base, name, desc, icon, cat[, minValue])` from
  `initBeforeGameInited()` — **the only valid window**. Use the `minValue` overload if the key will ever
  be authored `>MUL: 0`.
- **Polarity**: a higher Stealth value is *bad for the player* while being numerically higher, so it
  needs a `BoostFormats` colour rule the same way vanilla low-positive `RATES_*` keys do — **unless** it
  is defined as a divisor instead (the inversion used by the `CLASS_*` keys in the originating project).
- **Cost**: the sweep is over criminals, not the whole population — `isCriminal` is cheap, but *finding*
  criminals means iterating `SETT.ENTITIES().getAllEnts()`. Budget it on a timer, not every frame.
  `EventCitizenRiot.update` is the engine's own precedent for a full `getAllEnts()` scan on a 10-second
  timer, so that budget is acceptable in this codebase.
- **Legibility is free** (§9.3). `occurence()` vs `caught()` per crime per race is already saved,
  charted and hovered. A Search key's whole visible effect is *"the Arrests line for Theft stops being
  zero"*; a Stealth key's is *"Arrests fall while Crimes don't"*. No new UI is needed for the feature to
  read, and the same two series are the A/B measurement during development.
- **Riots are a distinct regime, not noise** (§7.1). While `pop(RIOTER) > 0` the mailboxes are flooded
  and no guard polls them, so a `reportCriminal`-based Search key **silently does nothing**. Decide this
  explicitly rather than discovering it in testing.
- **Don't let Stealth's discard leak into arrests.** `catchh` is charged to the individual's
  `prisonerType`, not to what they were caught doing (§6.3), so any mod-side capture or release must be
  deliberate about which `StatCrime` it credits, or the very counters the feature is measured by drift.

## 13. The three open decisions — settle these with the project owner before writing code

1. **Is Stealth per-`Induvidual` (race-authorable) or faction-level?** Per-`Induvidual` lets race files
   author it and makes it participate in `HCLASS_RACE` queries; faction-level is simpler and matches
   Search.
2. **Multiplier + colour rule, or inverted divisor?** A multiplier needs a `BoostFormats` low-positive
   colour rule so a "high" value reads as bad; a divisor sidesteps that entirely (the route the
   `CLASS_*` keys took).
3. **What does Search do during a riot?** (§7.1.) Either back off while `pop(RIOTER) > 0`, or bypass
   the jammed queue entirely via `catchCriminal` + `overwrite` on a specific guard. **This is the
   difference between the key working and silently doing nothing for a game day at a time.**

**Legibility is no longer an open question** — §9.3 settled it: the feature needs no new UI.

---

# PART IV — The one remaining research step (human-run, requires a running game)

**Status: ⬜ NOT DONE. This is the blocking item.** Nothing in this document has been observed
running; Parts I–III are static analysis of the source. **No mod build is needed — every prediction
below is about vanilla.**

Two unknowns to settle first and record: **how to open `IDebugPanelSett`**, and **where
`S.get().developer` is toggled** (Settings, or `init/settings/`), since the instrumentation depends on it.

## 14. Instrumentation

- **Law panel** (`UILaw`) → Citizen tab → hover each crime row: `In Custody`, `Crimes (today/yearly)`,
  `Arrests (today/yearly)`, filterable per race with the `Selector`. **This is the primary measurement
  — no log-watching required.**
- **Guard room panel with `developer` on**: live per-house and settlement-wide mailbox depth.
- **`IDebugPanelSett`**: `CRIMES_TEST_TOGGLE` (makes **every** subject want to commit a crime every
  update — `AIModule_Crime:144`), `show patrols` (also `Patrols.debugButt()`), `Event: Riot` (fires a
  riot on demand, `EventCitizenRiot:51`).
- ⚠️ **With developer mode on, clicking the crime chart opens fake `commit:`/`arrest:` buttons**
  (`CrimeChart:31-42`); `LawChart` has an equivalent punish popup. **A stray click corrupts the exact
  statistics you are measuring.** Do the measured runs with developer mode off, or at minimum never
  click the chart.

## 15. Confirm or refute, in priority order

1. **Theft/Disrespect/Speech are committed but never arrested** (§6.3). *The sharpest prediction in this
   document and the whole premise of a Search key.* §9.3 makes it a direct read rather than an
   inference: in the Law panel those three should show a **rising `Crimes` count with `Arrests` at or
   near zero**, while Murder/Vandalism/Flashing show both moving. Run with `CRIMES_TEST_TOGGLE` on to get
   volume fast.
   **⚠️ Two confounds must be excluded:** *no Prosecution decree active*, and **no riot during the
   sample** (`pop(RIOTER)` must stay 0). Either one posts arrests against those crimes without any
   detection having happened (§6.3, §7).
2. **Patrolling guards ignore queued criminals** (§5.3). Put a criminal well outside the 8-tile witness
   radius of a patrol route but inside 90 tiles of a guard house with **no free stand-spots**.
   Prediction: *nobody comes.* The developer mailbox readout makes this directly checkable — the report
   should sit in the queue at non-zero depth while patrols walk past.
3. **Stand-spots, not headcount, drive arrests** (§5.1/§5.3). Same active-duty division; vary
   guard-house spot count; compare the `Arrests` series. This is the claim most likely to surprise a
   player, and the one the UI gives no hint of.
4. **A riot suspends crime response** (§7.1). Fire `Event: Riot`, then watch mailbox depth pin at its
   cap while `Arrests` for ordinary crimes flatlines and rioter arrests continue. Also worth timing the
   riot end (predicted 0.25–1.0 game days, or earlier at the 30 % attrition threshold).
5. **Rough arrest rate** vs. the predicted 50 %-flip × 90-tile-pathfind, to sanity-check §6.2.

**After this pass**, fold the results back into Part I, then settle the three decisions in §13 and
write the implementation spec.

---

# PART V — Gotchas (do not relearn the hard way)

- **"Search"/"Stealth" are not engine words.** Don't grep for them; nothing will match. The nearest
  existing name is `GuardInstance.search`, an **unrelated boolean** meaning "this house may still have a
  free stand-spot" (`:25`, reset daily in `updateAction`).
- **Module priority does not preempt a running plan** (§5.2). `AIModule_Crime.getPriority` returning 6,
  or `AIModule_Prisoner` returning 10, decides which module wins *at the next plan selection* — not what
  happens to a plan already in flight. Priorities only matter at `newPlan`.
- **`pollCriminal` is destructive.** Calling it to "check" whether a criminal is queued **removes the
  report. There is no peek.** (The developer-mode `reporter.crimes(ins)` readout is the only
  non-destructive depth check.)
- **`AIEventListeners.tmp` is a static single slot** shared by `catchCriminal`, `flee` and `fight`. Call
  `catchCriminal(x)` and hand the plan to `overwrite` in the same breath.
- **The `else if (RND.oneIn(4))` global fallback fires only when no guard house was found.** Building
  guard houses everywhere therefore *removes* the fallback path — reports concentrate into 5-slot
  mailboxes that can jam. Counter-intuitive and worth designing around.
- **`catchPrisoner` always returns `true`** (`AIModule_Crime:183-194`), so `makePrisoner` never fails,
  and it charges the arrest to the individual's `prisonerType` — *not* to the crime they were observed
  committing.
- **Persecuted subjects self-report 4×/day forever** until caught, so a Prosecution decree floods the
  queues and can starve real crime reports out of the 5-slot mailboxes.
- **Recruits (`HTYPES.RECRUIT()`) are law enforcement** for `NOTIFY_CRIME` purposes but are **not**
  `HTYPES.GUARD()`, so they don't count toward guard power, the anti-crime population factor, or
  guard-house employment.
- **A riot is a settlement-wide crime-response outage** (§7), not a local event. It is also the one
  vanilla path that produces arrests for Theft/Disrespect/Speech, so **a save that has ever rioted is a
  poor place to test §6.3.**
- **Rioters can drown**: standing in `WATER.DEEP` for 2–16 `SubFlee` cycles sets `CAUSE_LEAVES.DROWNED`
  (`AIModule_Rioter:220-225`). Riot attrition is not all guard work.
- `EventCitizenRiot.update` divides by `currentRioteers` **without a zero guard** (`:98`); reachable only
  when `pop(RIOTER) > 0`, which in vanilla implies a riot set that field. **A mod that spawns
  `HTYPES.RIOTER()` humanoids directly would make that an `Infinity` comparison** — harmless here (the
  branch just converts everyone) but know it before spawning rioters.
- **The `d.subByte >= 50` branch of `PlanPatrol.beBraced.res` is dead code** (§5.3) — don't design around
  a "stand at attention" behaviour that never fires.
- **`pollExecution` has two latent engine bugs** worth knowing before trusting that path: it masks the
  packed tile coords with `0x0FF` instead of `0x0FFFF` in the global branch (`CrimeReporter:165-166`),
  and both loops re-poll with `pop(tCrime, …)` instead of `tExecution` (`:159, :171`).
- **The `PLEASURE` / `S_PLEASURE` "crime" has `isCriminal = false`** and is the Prosecution decree's
  carrier, not a behaviour. `CRIMES.PERSECUTED(cl)` returns it.
- **`S.get().developer` unlocks the only real instrumentation** — and also the chart buttons that can
  corrupt your measurements (§14).

---

## 16. Verification ledger — how much to trust each part

| Confidence | Sections | Basis |
|---|---|---|
| **High** | §2, §4, §5.1–§5.3, §6, §7, §8, §9.3, §10, §11 | source files read end to end; `reportCriminal` callers confirmed by exhaustive grep; `AIPLAN.java` + `AIManager.setNextState` + `AISUB.Simple.activate` read in full; `AIModule_Rioter`, `EventCitizenRiot`, `StatCrime`, `UILaw`, `CrimeChart`, guard/police `Gui` read in full |
| **Medium** | §5 (guard-house employment), §9.1, §7 (riot trigger ladder) | `EmployerSimple.employ` and `ROOM_POLICE`/`PoliceInstance` access gating not opened; `PoliceWork` itself read in full; `EventCitizen.update` read but its `SMALL_EVENT` ladder only skimmed |
| **Low — open** | §9 (post-capture detail) | not the focus of this evaluation |
| **Zero** | **all runtime behaviour** | **nothing has been observed in a running game. This is static analysis only. Part IV is what closes this row.** |

## 17. Provenance and what was superseded

- Originating research: a Songs of Syx modding control-room repo, in
  `.claude/memory/reference_songsofsyx_guards_crime.md` (the full 60 KB reference) plus session-log
  entries dated **2026-08-08** and a companion doc on behaviour/environment seams. **This file
  consolidates all of it**; you do not need those.
- The 2026-08-08 pass closed two previously-open research items (read `AIModule_Rioter` → §7; read the
  law UI → §9.3). Four findings from that pass changed the *shape* of the feature and are already folded
  in above: riots as a third channel that jams the other two; `commit()` unconditional while `catchh`
  is not; **the feature needs no new UI**; and **no vanilla tooltip ever claims guards detect
  anything**.
- Two scope reductions were made and should stay made: **war criminals** are out of scope (they never
  touch the reporter, §9.3), and **legibility/UI** is a closed question, not an open decision.
- Superseded claim, do not reintroduce: an earlier note recorded `coo(...)` then
  `if (walk != null) overwrite(...)` **with no ownership claim step**. That snippet, implemented
  literally, shipped a crash-to-desktop. §11.1 is the corrected form.

---

**Bottom line for the recipient:** the engineering is unblocked and the seams are all public. What is
*not* yet established is that the vanilla behaviour behaves at runtime the way the source says it does.
**Run Part IV item 1 first** — one session in a test save with `CRIMES_TEST_TOGGLE` on and the Law panel
open either confirms the entire premise or kills the feature. Everything else follows from that.
