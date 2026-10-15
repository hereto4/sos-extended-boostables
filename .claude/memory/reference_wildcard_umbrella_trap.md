---
name: reference-wildcard-umbrella-trap
description: Content wildcard keys (ROOM_FARM*, ROOM*) prefix-match EB-registered keys too; on *_ALL umbrellas that double-applied every such boost. Fixed by UmbrellaWildcardGuard (v1.6.1, in-game verified 2026-10-14).
metadata:
  type: reference
---

**Trap:** the engine expands a wildcard boost key by plain prefix match against every registered
boostable (`RMAP.get` → `k.startsWith(beg)`), and does so in `BOOSTING.finishSetup()` — *after*
`initBeforeGameInited`. So every key EB registers is visible to every wildcard in vanilla/mod content.
`ROOM_FARM*>MUL: 1.25` (Cretonian) also hit `ROOM_FARM_ALL`, and `UmbrellaBooster` then multiplied each
farm by it again → ×1.5625 with no tech. `>ADD` wildcards lifted the umbrella's 1.0 base, turning
`+0.15` into an extra ×1.15. Vanilla sources hitting umbrellas: 8 of 9 races, Athuri/Crator religions,
noble traits, player titles, ~70 event entries, Overtime (`stats/bonus`, `ROOM*>MUL 1.25`). CaC and the
addons had none.

**Fix (v1.6.1, user confirmed in-game 2026-10-14):** `your.mod.umbrella.UmbrellaWildcardGuard` —
`register(umbrella)` from `registerUmbrellaCascade`, `install()` queued on `BOOSTING.waiting` *before*
`TargetFilters.install()`. It reads each private `PromiseList`'s `Promise.key`/`factor`; a resolved
umbrella spec whose booster is the factor of a `*` promise is wildcard-made (one booster instance is
shared across all matches) and is removed via `BoostSpecs.remove(BoostSpec)`. Explicit `ROOM_FARM_ALL`
entries have their own booster and survive. Uncovered: `PBonusSetting` (resolves wildcards itself) and
boost lists queued after EB's hook (logged).

**How to apply:** any NEW EB key whose name starts with a vanilla wildcard prefix (`ROOM_*`, `WORLD_*`,
`BATTLE_*`, …) will silently receive those content boosts. If it's a new umbrella, route it through
`registerUmbrellaCascade` (auto-guarded). If it's some other key that must not inherit wildcards, extend
the guard's scope rather than renaming. Why a simpler fix doesn't exist (rename, late registration,
UmbrellaBooster-side filtering, per-aggregator cleanup) was worked through in full before choosing this.
Related: [[feedback-reflection-policy]], [[sos-game-init-timing]].
