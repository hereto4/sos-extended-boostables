# Round 2 — review of `102a78c` "stealth and alertness fixes"

Follow-up to `HANDOFF_STEALTH_REVIEW.md`. Short, because most of it is good news.

**Verdict: H1 and H2 are closed. I'd merge this on correctness grounds.** What remains is a design
decision (H3), not a defect.

---

## Verified, not assumed

- **Compiles clean** against the v71.44 jar, 0 errors. No dangling references to the removed
  `REPORT_ATTEMPTS`.
- **The reflection actually resolves.** I ran it against the shipped game jar on a real JVM rather than
  eyeballing the signature:

```
found:         private void settlement.room.law.guard.CrimeReporter.report(int,int,int,int,int)
setAccessible: OK
tCrime value:  0      tExecution: 1      maxRadius: 90
```

  Signature matches exactly, `ROOM_GUARD.maxRadius` is the same `90` literal `reportCriminal` passes,
  and `tCrime` is genuinely `0`. `DirectReport.push` is a faithful reproduction of `reportCriminal`
  minus the coin flip.

## What landed well

| Finding | Assessment |
|---|---|
| **H1** | Textbook Option B — method handle cached in a static initializer (not re-resolved per call), verbatim `reportCriminal` fallback, `modeDescription()` surfacing which path ran. Reads once, appends rather than pins, degrades safely. |
| **H2** | `isGenuineCrimeSubject()` is correct — a normal thief still passes all three gates, while raiders and decree-marked subjects no longer drag the settlement through the sweep. |
| **M2** | Descriptions fixed in both code and KEYS.md; they now describe what the code does. |
| **L3** | `to()` returning `GROWTH_CAP` is right — `BUtil.max` now reports 4.5 instead of 1.5, and `progress()` gets a delta of 4.0, so no divide-by-zero. |
| **L1 / L2 / L6** | Documenting rather than changing was the right call. Your L2 note — the LOS-side Chebyshev gate is the one that matters for correctness, the caller's octile check is merely stricter on diagonals — is correct. |

Writing the javadoc to explain *why* the reflection is in-policy, rather than just asserting it, is
exactly the outcome worth having. Thank you for the loss logging too — that single `else` branch is what
makes the next test run readable.

---

## Three small things this introduced

1. **The loss log is unconditional.** The `else` branch sets no cooldown, so an evading criminal with a
   guard in range emits a `System.out.println` **every 2 game-seconds**, indefinitely. Perfect for the
   test run it was written for; debug-gate it before release.
2. **`TCRIME = 0` is hardcoded while the method is reflected.** `tCrime` is a non-`final`
   `private static int`. If those strides are ever reordered, crime reports become *execution* reports
   and `pollExecution` decodes the criminal id as packed tile coords. Tiny risk — but you are already
   reflecting in this class, so one more field read with a fallback to `0` costs nothing and removes the
   inconsistency.
3. **"H1 is fully addressed, not just mitigated" is very slightly overstated.** The per-event
   amplification is genuinely gone (22.7% chance of flooding an empty house per reinforcement → 0). But
   `REPORT_COOLDOWN` is time-based, not episode-based, so a long-uncaught criminal still pushes a
   *duplicate id* every 15 s; five of those (~75 s) still fills a house. H3 makes capture fast enough
   that it rarely bites in practice. **"Report once per crime episode" rather than once per 15 s would
   close it completely** — and it is simpler than what you already wrote.

## Still open — and one of them matters

**H3 is untouched, and is now marginally worse.** `PERIOD` is still 2.0, the contest math is unchanged
(66.6% per contest on your own logged values), and a won contest is now a **100%** report instead of
99.2%. A genuine criminal is still ~96% reported within six game-seconds, and Stealth still barely
changes the outcome. The H2 fix narrows *who* gets swept; it does not change the odds for an actual thief.

This is not a defect — H1 and H2 were the blocking items and you cleared them. But **H3 plus M1** (growth
asymmetry: guards accrue Alertness all session, a criminal gains ~0.02 Stealth per career before being
caught) are together what decide whether this is a *stealth system* or a crime-detection buff, and
neither has moved. The cheapest lever is raising `PERIOD` a lot — one contest per crime *episode* rather
than one every 2 s — so a high-Stealth subject can actually get away with something.

Also still open, all minor: **M3** (no content-level safety net — the mechanic runs at full strength on
vanilla content the moment the flag is set), **M4** (map growth), **L4**, **L5**.

## A correction I owe you

My §2d told you to "gate on queue headroom" via the public `available(GuardInstance)` /
`crimes(GuardInstance)`. Those methods *are* public and `RoomBlueprintIns.getter` *is* public — but
picking the right guard house needs `ROOM_GUARD.finder`, which is package-private. So a precise headroom
gate is not cleanly reachable, and skipping it was the right judgement. The episode-based dedup in point
3 above achieves the same end without needing to know the house, and supersedes that recommendation.

## Suggested next step

Debug-gate the loss log, then decide H3 — it is a tuning/design question rather than a bug, and it is the
only thing between this and a feature that does what its name says.
