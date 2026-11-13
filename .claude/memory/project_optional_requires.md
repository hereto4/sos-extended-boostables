---
name: Optional REQUIRES for missing race mods (SHELVED)
description: Feasibility of waiving tech REQUIRES lines whose race mod isn't installed; recommended design B′ (REQUIRES_IF_PRESENT block, lazy Locker); shelved 2026-11-13, nothing implemented
type: project
---
**Shelved 2026-11-13, nothing implemented.** The full record is in
`sos-scripting-template/.claude/SPEC_OPTIONAL_REQUIRES.md`.

**Problem:** a tech `REQUIRES` line naming a race whose mod isn't installed locks the tech forever, with no
player-visible reason. The engine's "unknown" stand-in reads 0 and is compared against 1 whatever was
authored. The warning appears only in developer/debug mode. Affected: the aquatic synergy techs `VERSY1`,
`ONASY1` and `THRSY1`.

**Recommended (B′):** a new tech-node block, working name `REQUIRES_IF_PRESENT`, read by
`TargetFilterRegistry.scanTechFiles()`. In `initBeforeGameInited`, EB pushes one custom `Locker` per entry
onto `tech.requires`. The Locker looks up `GVALUES.FACTION.get(key)` lazily, waives the line when the key
is missing, and otherwise mirrors `LockerValue`. Public API only.

**Why not a simple existence check at init:** `GVALUES` resolves before `BOOSTING` in `INIT.finish()`, so
there's no pre-resolution point after other mods' hooks.

**Alternative (A):** strip "unknown" locks in `GAME.addOnInit`. Needs a reflective read of the
package-private `LockerValue.getter.key`.

**Open decisions:** B′ vs A; what happens if every partner race is missing; show or hide waived lines
(recommended: show).

**How to apply:** when this is resumed, read the SPEC first, and ask the user the three open decisions
before writing code. Related: [[project_race_groups]].
