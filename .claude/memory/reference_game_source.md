---
name: Game Source Reference Location
description: Where to find Songs of Syx game source code for reference when building mods
type: reference
originSessionId: f4a7dc19-9fa3-4a12-9315-2717447ae117
---
The Songs of Syx v70.32 game source code (Java) is unzipped at:
`C:\Users\Nate\AppData\Roaming\songsofsyx\mods\sos-extended-boostables\.claude\game-source-java\`

Key subdirectories:
- `game/boosting/` — All boostable/boosting system source files (BOOSTABLES.java, Boostable.java, etc.)
- `game/` — Core game systems
- `settlement/` — Settlement/room/building systems
- `script/` — Scripting API

The compiled .class files (no source) are at:
`C:\Users\Nate\AppData\Roaming\songsofsyx\mods\sos-extended-boostables\.claude\game-source\`

Always prefer the `-java` directory for reading source. These files come from `songsofsyx-70.32-sources.jar` located at `C:\Users\Nate\.m2\repository\com\songsofsyx\songsofsyx\70.32\`.
