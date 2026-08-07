---
name: noble-happiness-no-consequence
description: v71.19 — noble happiness/needs drive nothing mechanical (no noble standing); every unhappiness consequence is citizen/slave-gated; the only real cost of needier nobles is starvation under famine. Informs whether CLASS_NOBLE is a viable cost lever.
metadata:
  type: reference
---

Established by a 3-way source sweep of v71.19 (2026-07-31), to judge whether `CLASS_NOBLE>MUL:<1`
(making nobles needier — higher `RATES_HUNGER/THIRST/SHOPPING`) is a meaningful player cost. See
[[noble-office-construction]] and project memory #8.

## Root cause: there is NO noble standing
`settlement/stats/standing/STANDINGS.java:33-34` creates class happiness/loyalty standings **only** for
`HCLASSES.CITIZEN()` (HAPPI + LOYALTY) and `HCLASSES.SLAVE()` (HAPPI_SLAVES + SUBMISSION). There is no
`StandingNoble`. `STANDINGS.get(NOBLE)` (`:115-121`) falls back to the **citizen** object. So noble
happiness/fulfillment/loyalty is **never computed**, and nothing reads it for nobles (the noble HAPPI/
LOYALTY HCLASS_RACE keys exist at base 1.0 but are derived from nothing and consumed by nothing).

## Every unhappiness / unmet-need consequence excludes NOBLE (citizen/slave-gated)
- **Immigration** — citizen-only: `settlement/entry/Immigration.java:91` returns early if class != CITIZEN.
- **Emigration** — `settlement/entity/humanoid/ai/subject/PlanEmmigrate.java:36`: `if (clas()!=CITIZEN) return false`. Nobles never emigrate.
- **Rioter conversion** — only `HTYPES.SUBJECT()` (citizen) converts: `EventCitizenRiot.java:146,163`; fires off `STANDINGS.CITIZEN().loyalty`.
- **Deranged / insanity** — `StatsNeeds.java:195` & `:426` require `i.clas()==HCLASSES.CITIZEN()`. Nobles never go insane.
- **Crime** — criminal AI module only on citizen types (`AIModules.java:99-105`); nobles don't get it; driven by LAWFULNESS, not needs.
- **Slave uprising** — SLAVE only (`EventUprising.java:111,120`).
- **Discontent events/messages** — all off `STANDINGS.CITIZEN().loyalty` (`EventCitizen.java`); no noble discontent event. The only noble message is `NOBLES.DeathMess` (fires on death/vacate, not mood).

## Office output is mood-independent
`NobleOffice.value(slots)` = allocations (assignment + rank) + room employment only — no happiness term
(`NobleOfficeUtil.java:60-62,92-94,136-140`; `NOBLES.java:104-106,161-173`). `Noble.update()` is empty
(`Noble.java:58-62`); no resignation/coup/discontent; a noble vacates ONLY when its Humanoid is removed
(death → `Humanoid.java:493-494` → `NOBLES.vacateOnlyCallFromHumanoid`). An unhappy-but-alive noble
delivers full office boosts.

## The ONE real cost: starvation death (not class-gated)
Hunger accumulation (`StatsNeeds.java:167-178`, no class gate; excludes only disease/TOURIST) and the
starvation-death path (`F_PlanStarve.java:88-89` → `CAUSE_LEAVES.STARVED()`) are **not** class-gated, and
nobles carry the food/consumption AI module (`AIModules.java:105` + `:72-75`). So needier nobles reach the
hunger max ~ (1/factor) sooner (e.g. `>MUL:0.7` → factor 1/0.7 ≈ ×1.43 hunger growth) and consume more
food. **But this only kills under an actual food shortage** — a fed noble is unaffected. Thirst and
shopping have **no death path** (they only lower fulfillment/happiness, which does nothing for a noble).
Old-age death is need/happiness-independent.

## Implication for design
Making nobles needier is a **negligible cost** for a well-supplied city (nobles are a tiny, standing-less
slice; the only bite is faster starvation during famine, i.e. only when you're already failing). It is
**not a viable balancing lever**. A real noble-side cost must instead scale the **office output**
(`CLASS_NOBLE_<office>` / `CLASS_NOBLE_ALL`, which have city-wide reach via room bonuses) or a genuinely
city-wide effect. Confidence: high (three independent sweeps converged). Rests on the
[[noble-office-construction]] assumption (v71.44 == v71.19).
