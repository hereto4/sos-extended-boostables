---
name: dev-mods-load-from-mods-folder
description: In-dev mods run from the /mods project folder (enabled in the launcher), NOT the Steam Workshop copy — never conclude otherwise from a stale LauncherSettings.txt MODS list
type: feedback
---

The user runs in-development mods from the **project folder under
`C:\Users\Nate\AppData\Roaming\songsofsyx\mods\<mod>\`**, enabled in the launcher. A build's automatic
deploy (`copy-mod-to-game` → project root, see [[maven-intellij-path]]) is therefore the whole deploy —
**there is no copy-to-Workshop step, and no need to suggest one.**

**Why this needs writing down:** `%APPDATA%\songsofsyx\settings\LauncherSettings.txt` `MODS: [...]`
lists Workshop numeric IDs plus local folder names, and a session may read it *before* the user enables
the in-dev folder in the launcher. In 2026-07-29 that stale snapshot led to asserting "the game loads
the Workshop copy `3715764503`, not this project folder — copy the jar there to test," which was wrong.
The user corrected it.

**How to apply:**
- Treat `LauncherSettings.txt` `MODS` as a point-in-time snapshot the user changes freely in the
  launcher UI. Never infer from it which copy of a mod is authoritative.
- After a successful build, report the mod as deployed and testable as-is.
- Only bring up the Workshop copy when the topic is genuinely **publishing a release** to subscribers.
- Never write into `steamapps\workshop\content\...` — Steam manages it and can revert it.
