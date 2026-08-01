---
name: dev-mods-load-from-mods-folder
description: "POLICY: never read the Steam Workshop directory. All dev mods live in ...\\songsofsyx\\mods\\ — that folder is both the repo and the deploy target"
type: feedback
---

> **POLICY (user, 2026-07-31): the Steam Workshop directory is off-limits.**
> Do not read, search, cite, or propose edits to `…\steamapps\workshop\content\1162750\`. It holds stale
> published snapshots. `C:\Users\Nate\AppData\Roaming\songsofsyx\mods\<mod>\` is the only source of truth
> for mod code, jars, and deploy state. Never report a stale artifact found in a Workshop folder as an
> issue. Workshop paths matter only in an explicit, user-driven release discussion.


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
