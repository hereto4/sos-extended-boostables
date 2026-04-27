---
name: SoS Modding Workflow Constraints
description: Key constraints and patterns for building Songs of Syx mods in this project
type: feedback
originSessionId: f4a7dc19-9fa3-4a12-9315-2717447ae117
---
Minimize overwrites of vanilla data/code to preserve mod compatibility.

**Why:** User's explicit stipulation — mods must be structured for clean packaging and distribution, and should not break when vanilla game updates.

**How to apply:**
- Prefer adding new data files over editing vanilla files
- Use the Java scripting API (`SCRIPT` interface) for runtime behavior rather than patching classes
- When a room has no existing `Industry` hook, use a script to apply boostable effects
- Never edit files under `VANILLA GAME/` assets — treat those as read-only reference

**Additional constraint:** The unzipped game source at `.claude/game-source-java/` is for reference only — never the target of edits. All mod output goes under `V70/`.
