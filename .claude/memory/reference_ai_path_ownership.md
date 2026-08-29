---
name: AIManager.path ownership — the walkTo.coo CTD trap
description: Never call AI.SUBS().walkTo.coo() (or overwrite a sub) on a humanoid the engine still owns; a failed coo() poisons the shared AIManager.path and the engine throws at SPath.setNext. h.interrupt() is the ownership seam.
type: reference
---
**This trap shipped a crash-to-desktop to players** (the first `RATES_NATURE` "Piety (Nature)" release,
fixed 2026-08-29). Read before writing any code that drives settlement AI.

## The crash

```
java.lang.RuntimeException
    at settlement.path.path.SPath.setNext(SPath.java:357)
    at settlement.entity.humanoid.ai.subwalk.PathWalker$1.res(PathWalker.java:58)
    at settlement.entity.humanoid.ai.main.AISUB$Resumable.resume(AISUB.java:204)
    at settlement.entity.humanoid.ai.subwalk.PathWalker.resume(PathWalker.java:239)
    at settlement.entity.humanoid.ai.main.AIManager.setNextState(AIManager.java:386)
    at settlement.entity.humanoid.ai.main.AIManager.update(AIManager.java:356)
    at settlement.entity.humanoid.Humanoid.update(Humanoid.java:223)
    at settlement.entity.ENTETIES.update(ENTETIES.java:360)
```

`SPath.java:357` is literally `if (!successful) throw new RuntimeException();` at the top of
`SPath.setNext()`. Read the stack as: **an engine `PathWalker` sub, already past `init` and mid-route, was
resumed with `d.path.successful == false`.** (All line numbers v71.44.)

## Why a mod causes it

`AISUB_walkTo.coo()` is **not a query — it writes shared state before it knows the answer, and never rolls
back**:

```java
// AISUB_walkTo.coo
d.path.request(a.physics.tileC(), dx, dy);          // AIManager.path — SHARED, per-humanoid
if (d.path.isSuccessful()) return vanilla.activate(a, d);
return null;                                        // <- leaves the path POISONED

// SPath.request
this.successful = false;   ...search...   return successful;   // false is never restored
```

A **failed** `coo()` hands back `null` *and* leaves the humanoid's path unsuccessful. If that humanoid's
active `AISUB` is an engine `PathWalker` mid-route, its next `AIManager.update` → `setNextState` →
`PathWalker$next.res` → `path.setNext()` throws. Requests fail routinely: destination behind walls, across
water, in a different path component, or a solid tile in the middle of a blob.

`coo()` also runs `vanilla.activate()` → `PathWalker.init()` *inside* the call, mutating `d.subByte` /
`d.subPathByte2` — a second reason it can never be called speculatively.

**Gating on `AI.modules().idle.is(h, d)` does NOT protect you** — that was the original mitigation and it
failed. The idle module *walks constantly*: strolls (`SubMove`), steps out of the way (`getOutofWay` +
`walkTo.pathFull`) and walks to benches (`walkTo.serviceInclude`). All are `PathWalker`s. "Module == idle"
says nothing about whether `d.path` is in use.

## The rule

**Never touch `d.path` — never call `coo()`/`overwrite()` — on a humanoid the engine still owns.**
`h.interrupt()` (= `AIManager.overwrite(a, AI.plans().NOP)`) is the ownership-transfer seam, and it must
come **first**:

```java
AIManager d = (AIManager) h.ai();
if (d.plansub() == null || d.plan() == null) return;  // mid-interruption: interrupt() would NPE on sub.cancel()
h.interrupt();                                        // cancels the live sub (runs its abort() → releases
                                                      // reservations), clears a pending interruption,
                                                      // cancels the plan, parks them on NOP (sub = STAND,
                                                      // which never reads d.path)
AISUB.AISubActivation walk = AI.SUBS().walkTo.coo(h, d, tx, ty);
if (walk == null) return;                             // harmless now: path dead but unowned
d.overwrite(h, walk);
```

- **Ownership test:** `d.plan() == AI.plans().NOP`. Check it before *every* later
  `interrupt()`/`overwrite()`. Once the engine has handed the citizen a real plan again, **end your episode
  — never re-issue a walk to a reclaimed humanoid** (that is the same crash).
- **`overwrite(a, AISubActivation)` does not cancel the sub it replaces.** Replacing a sub that held a
  reservation (bench/service/resource/storage) skips its `abort()` and **leaks the reservation forever**.
  `overwrite(a, AIPLAN)` / `h.interrupt()` do run the cancel chain — use them.
- **Pre-check reachability before claiming** so a hopeless target costs nothing:
  `SETT.PATH().comps.superComp.get(x,y) == superComp.get(citizenTile)` for the destination **or an
  orthogonal neighbour** (`coo` requests a *non-full* path, so the destination itself may be solid — a
  tree). This is the engine's own guard, copied from `AISUB_walkTo.room`.
- `PathWalker.init` returns its `stop` resumer ("This shouldn't happen") when `a.speed.magnitude() > 0`, so
  a moving citizen brakes for one state first. Harmless, expected.
- Mod `update()` runs as a `GameResource` sibling of `SETT`, never nested inside `ENTETIES.update()`, so
  your writes always land *between* entity updates — which is exactly why the crash surfaces on the
  victim's next update rather than at the call site.

## Do NOT define your own AIPLAN/AISUB (save safety)

`AIPLAN.PLANRES` is technically subclassable from a mod (public ctor, only `protected init(...)` left
abstract) — **but don't.** `AIPLAN`/`AISUB` extend `AIElement`, which registers into `AI.s.all` and is
persisted **by index** in `AIManager.save`. A mod-defined plan/sub active at save time writes a mod-only
index into the save. Driving the *engine's own* `walkTo`/`STAND` subs under the engine's own `NOP` plan
keeps every saved index vanilla, so a save taken mid-episode loads with or without the mod.

## Where this is used

`MainScript.maybeStartPilgrimage` / `updatePilgrims` / `findNearestNature` / `pathReachable`
(`RATES_NATURE` Stage 2). Full write-up: `sos-scripting-template/.claude/SPEC_NATURE_PIETY.md` **§7.1**;
chronology in that repo's `SESSION_LOG.md` (2026-08-29). Related: [[reference_rates_need_semantics]],
[[reference_songsofsyx_settlement]].
