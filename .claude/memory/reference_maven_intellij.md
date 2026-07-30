---
name: maven-intellij-path
description: mvn is not on PATH; use IntelliJ's bundled Maven at this absolute path for builds
metadata:
  type: reference
---

`mvn` is **not on the user's PATH** in either bash or PowerShell. Building requires invoking IntelliJ's bundled Maven directly.

**Path** (IntelliJ IDEA 2025.3.2, installed to `C:\Game Development\`):
```
C:\Game Development\JetBrains\IntelliJ IDEA 2025.3.2\plugins\maven\lib\maven3\bin\mvn.cmd
```

Bundled Maven version: 3.9.11. Bundled JDK runtime: Eclipse Adoptium 25.0.2.

**To find the IntelliJ install root in future sessions** (in case the version dir changes):
```powershell
Get-Content "$env:LOCALAPPDATA\JetBrains\IntelliJIdea*\.home"
```
That file contains the absolute path to the IntelliJ install root.

**Standard build invocation** (PowerShell):
```powershell
& "C:\Game Development\JetBrains\IntelliJ IDEA 2025.3.2\plugins\maven\lib\maven3\bin\mvn.cmd" install "-Dmaven.test.skip=true"
```

Two non-obvious gotchas:
1. Use the call operator `&` because the path contains spaces.
2. **Quote the `-D...` argument**. PowerShell parses unquoted `-D...=...` and splits it into separate tokens, causing `Unknown lifecycle phase ".test.skip=true"`. Always wrap as `"-Dmaven.test.skip=true"`.

NEVER run `mvn clean` in this project — see CLAUDE.md ("the maven-clean-plugin deletes the project root").

**Deploy** is automatic: the `copy-mod-to-game` resources execution at the end of `install` copies the staged mod into `${mod.install.directory}`, which resolves to the project root itself. After a successful build, `V71/script/sos-extended-boostables.jar` (`V${game.version.major}`, so V71 since the 2026-06-29 bump) plus `_Info.txt` are in place — **the game loads from this directory directly. There is no copy-to-Workshop step for dev builds** (see [[dev-mods-load-from-mods-folder]]).
