# Claude Operating Notes — sos-extended-boostables

This file is read by Claude at session start. It documents project-specific gotchas that aren't obvious from the code.

## ⚠️ NEVER run `mvn clean` in this project

The template's `maven-clean-plugin` is configured to delete `${mod.install.directory}`, which **resolves to the project root itself** (because the mod's install path is the project's own directory: `~/AppData/Roaming/songsofsyx/mods/sos-extended-boostables`).

`mvn clean` will wipe the project root — `pom.xml`, `src/`, `SPEC.md`, `_Info.txt`, `LICENSE`, `doc/`, etc. — leaving only `.git/` and `.claude/`. Recovery requires `.git/` (which itself may need `refs/heads/main` rebuilt from `.git/logs/refs/heads/main`).

**Always use plain `mvn install -Dmaven.test.skip=true`** for builds. It is non-destructive.

If you genuinely need a clean build, first edit `pom.xml` to exclude project files from the clean plugin's `<filesets>`.

## Tests are skipped on every build

The template ships a broken `ExampleTest.java` (Lombok `@Data` annotation processor isn't wired up correctly under the current Maven/JDK combo). It is unrelated to mod functionality. Always pass `-Dmaven.test.skip=true`.

## Boostable registration must happen in `initBeforeGameInited`

This project registers `ROOM__SLAVER` programmatically rather than via a data file because:
- The asset file scanner (`init.paths.VirtualFolder.getClean`) silently skips any filename starting with `_`, so `__SLAVER.txt` (the filename needed to derive `ROOM__SLAVER`) is never loaded.

And specifically in **`initBeforeGameInited()`**, not earlier hooks, because:
- `GAME.<init>` constructs `ScriptEngine` (line 118) **before** it constructs `INIT` (line 123).
- `INIT`'s construction includes `BOOSTING.clear()` which empties the boostable map.
- So anything registered in our script's constructor or in `initBeforeGameCreated` is wiped.
- `initBeforeGameInited` runs at line 172, after `INIT` is constructed but before `init.finish()` resolves deferred tech promises at line 177. This is the only valid window.

See `SPEC.md` → "GAME.<init> Timing" for the full call-order table.

## Project memory location

Project memory lives in `.claude/memory/` (committed to repo), not the user-level auto-memory directory. See `.claude/memory/feedback_memory_location.md` and the redirect note in the user-level auto-memory `MEMORY.md`.

## Game source reference

Read-only Songs of Syx v70.32 source is unzipped at `.claude/game-source-java/`. Reference for all engine/system questions; never edit.
