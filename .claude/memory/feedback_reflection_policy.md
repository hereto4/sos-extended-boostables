---
name: reflection-policy
description: There is no blanket "no reflection" rule in this project; the real rule is never to reflectively write state the engine recomputes
metadata:
  type: feedback
---

**There is no project-wide "avoid reflection" policy, and no document has ever stated one.** If you
find code or a comment claiming otherwise, it is wrong — fix it rather than propagating it. The
narrow rule that this project's history actually supports is:

> **Never use reflection to write or pin engine state that the engine itself recomputes.**
> Reflecting to *read*, or to reach a member whose visibility changed between game versions, is
> established practice here — provided it runs once at init and has a non-reflective fallback.

**Why:** the one time reflection genuinely hurt this project was `STAT_WORK_RETIREMENT`. The race
`STATS:{}` standing system has no boostable seam and is `final`/cached, so it reflected every ~2s to
`setAccessible`+`setDouble` the final `StandingData.max` field and pin the private
`StandingCitizen.maxes`/`defs` arrays to a baseline. That is a no-op *only while those denominators
stay static during play* — true in v70.32, **false after the v71 standing rework**. The pin then held
the fulfillment denominator below its true value → `fulfillment = pow(current/maxes[...])` inflated →
happiness inflated → **runaway immigration from a fresh start with no techs unlocked**.

Three things made it expensive, and they are the actual lesson:
1. **It failed silently.** The reflection kept *succeeding*. The `statDisabled` guard caught reflection
   *throwing*, not reflection working correctly against a changed invariant. A try/catch cannot protect
   you from this class of bug.
2. **It ran for zero benefit** — nothing shipped ever boosted the key, so the factor was always 1.0.
3. **It cost a misdiagnosis** — Create-A-Culture was investigated and exonerated before EB was found to
   be at fault.

Disabled 2026-06-27 (7 blocks + 6 imports commented out in `MainScript.java`, not deleted).
See [[immigration-happiness-bug]] and [[stat-system-seams]].

**How to apply.** Judge a proposed reflection against the axis that separated the failure from the
successes, not against a blanket ban:

| | Don't | Fine |
|---|---|---|
| Operation | **writes** final fields / private arrays | **reads**, or appends to a collection |
| Frequency | every tick, forever | once, at init |
| Vs. the engine | fights it for ownership of a recomputed value | reaches a member whose *visibility* moved between versions |
| Fallback | none that helps (it "succeeds" wrongly) | yes — a non-reflective path that still works |
| Precedent | `STAT_WORK_RETIREMENT` (broke on a version bump) | `TargetFilters.queueWaitingAction`, `ParseWarningSuppressor` (still working) |

This mod already ships the right-hand column: `your.mod.targetfilter.TargetFilters.queueWaitingAction`
reaches the package-private `BOOSTING.waiting` field (it was `public` in v70, package-private in v71)
and falls back to running the action inline; `ParseWarningSuppressor` reaches `Json.untest` and the
engine's error-buffer internals the same way. The upstream template docs recommend reflection outright
(`doc/howto/access_game_code.md`, `doc/howto/add_ui_element.md`).

**Cost of getting this wrong the other way:** `your.mod.stealth.CrimeStealthCheck` cited a nonexistent
no-reflection policy as its reason for firing `reportCriminal()` seven times instead of reaching into
the sealed `CrimeReporter` — and that workaround jams a 5-entry guard mailbox with duplicates and can
de-register a guard house from the finder. An invented ban pushed the design toward the worse option.
