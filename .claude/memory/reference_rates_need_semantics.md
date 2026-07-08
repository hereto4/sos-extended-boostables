---
name: RATES_* need-rate boostable semantics
description: Every RATES_* boostable is a need-GROWTH-rate knob (higher = needier), never a fulfillment-efficiency knob; how it's consumed and how CLASS_* per-class filtering interacts
type: reference
---
All `RATES_*` boostables (e.g. `RATES_SHOPPING`, `RATES_HUNGER`, `RATES_THIRST`, `RATES_TEMPLE`,
`RATES_SHRINE`, `RATES_SKINNYDIP`, and any file-defined need) mean **"the rate at which the need
increases"** — i.e. how fast/urgently a subject *develops the desire* for that service. **Higher
`RATES_*` = needier subject; it is NOT a fulfillment-efficiency term.** A `MUL:1.5` makes them want it
faster (need sits unmet more → generally worse contentment); a `MUL<1.0` makes them less demanding.

**Authoritative definition (single source of truth):** `init/type/NEED.java:22` + `:59` — every need's
`rate` boostable is minted by the `NEED` constructor:
`this.rate = BOOSTING.push(key, jd.d("RATE"), …, "The rate at which the need of {0} increases daily.", …, cat)`.
Because `NEED_E extends NEED`, this applies to **both** need families defined in `init/type/NEEDS.java`:
- **Basic Needs** (`NEED_E`, cat `bCatE`, prefix `RATES_`): HUNGER, THIRST, SHOPPING.
- **Service Needs** (`NEED`, cat `bCat`, prefix `RATES_`): SKINNYDIP, TEMPLE, SHRINE + file-defined.

**How it's consumed — every read site confirms "higher = needier", none is fulfillment efficiency:**
- `settlement/stats/colls/StatsNeeds.java:171` (Basic Needs / `SNEEDS`=`NEED_E` only): each 16-tick,
  `if (RND.rFloat() < rate.get(i)) need.inc(i,1)` — `rate` is the **probability the need stat grows by 1**.
- `settlement/entity/humanoid/ai/service/S_Plans.java:143/155/172` (ALL needs; AI service-seeking):
  `v = rate.get(indu) * usage` — weight/priority the subject assigns to seeking that service. This is how
  the simple service NEEDs (TEMPLE/SHRINE/…) drive behaviour (they don't accumulate via StatsNeeds).
- `settlement/room/service/module/RoomService.java:141/150`: `1.0/(ne*rate.get(...))` — service interval
  is inversely proportional to rate → higher rate = must be re-serviced sooner.
- `settlement/stats/colls/StatsFood.java:114` + `view/…/UIFood.java`: `HUNGER.rate*pop*decree` = settlement
  food-demand estimate → higher rate = more food demanded.
- `view/…/UISubjectProperties.java:189`: display only.

**CLASS_* per-class filtering interaction** (relevant to the `CLASS_CITIZEN` feature, see project mem #8):
the class-filtered `ClassTreatmentBooster` only returns its multiplier for `vGet(Induvidual)` matching the
class; it returns neutral `1.0` for the other five `vGet` overloads. So the class multiplier bites **only
on read sites that pass an `Induvidual`**:
- Scaled per-Citizen: StatsNeeds:171 (need growth), S_Plans (seeking priority), UISubjectProperties (display).
- NOT scaled (pass `HCLASS_RACE`/aggregates → neutral 1.0): RoomService:141/150 (throughput), StatsFood/UIFood
  (demand estimate). This is benign/desirable — settlement-level planning math isn't double-counted.

**Design intent for `CLASS_<CLASS>` "Class Treatment" (confirmed by user 2026-07-03):** the neediness rise is
a deliberate **trade-off** — better treatment buffs productive stats (e.g. `ROOM_MINE_ORE`) *at the cost of*
raising that class's neediness (`RATES_SHOPPING`). So a `+MUL` on a `RATES_*` target is intended, not a
polarity bug. See [[stat-system-seams]], project memory #8.
