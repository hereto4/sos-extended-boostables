---
name: Race groups (GAQUATIC)
description: POPULATION_<GROUP>_* requirement GVALUEs for data-defined race groups; GAQUATIC shipped; implemented + type-checked 2026-11-13, not built/tested
type: project
---
`your.mod.racegroup.RaceGroups.install()` (called from `initBeforeGameInited` right after
`registerCitizenRaceFractions()`) registers pseudo-race GVALUEs for **race groups** declared in any loaded
mod's `V<major>/RaceGroups.txt` (EB ships `src/main/resources/mod-files/RaceGroups.txt` → `V71/RaceGroups.txt`).
First group: `GAQUATIC` = VERATHID, KHELIAN, THRYXARII, ONARI (third-party race mods; Q_AMEVIA deliberately
excluded by the user). Per group: `POPULATION_<G>__F/__I` + per player class `_<CLASS>_F/_I/_OFCLASS_F` (11 keys).
Status 2026-11-13: written on the Mac, javac type-checked clean against v71.44, **NOT built, NOT tested** —
see `HANDOFF_RACE_GROUPS.md` (repo root) for build + in-game test plan.

**Why:** the user wanted "100% of citizens are any mix of the aquatic races" tech requirements
(`POPULATION_GAQUATIC_CITIZEN_OFCLASS_F` in a GREATERE block). User chose a data file over hardcoding, and
the full 11-key set (not just OFCLASS_F); TYPE_* variants skipped.

**How to apply:**
- Values are one division of integer head-count sums — never sum per-race fractions (GREATERE is exact `>=`,
  summed fractions can read 0.9999… at 100%).
- Merge: RACES union across mods; NAME from the first mod in `PATHS.currentMods()` = highest asset priority
  (`VirtualFolder.getPossibleFile` searches bases first→last, vanilla last). Not "last loaded wins".
- Missing member races are skipped (keys still exist → no "no FACTION named" popup / permanent tech lock).
- Diagnostics to stdout only (stderr trips the engine error handler — see UpdateNotifier).
- **Phase 2 (deferred by user):** make groups work wherever race keys work — TARGET_RACE first (EB-owned).
- Related: OFCLASS_F origin in [[v71-migration]] / project_sos_extended_boostables.md item 6.
