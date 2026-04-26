---
name: Songs of Syx mod iteration workflow & template gotchas
description: Day-to-day edit → build → launch → debug loop, template-specific corrections (System.out vs LOG, placeholder package, DEBUG.run.xml coupling), and the mvn-validate trap on game updates
type: reference
originSessionId: 4e70a062-1427-421c-9733-c0f85b14eb41
---
This memory complements `reference_songsofsyx_mod_packaging.md` (folder layout, _Info.txt, mvn phases) and `reference_songsofsyx_script_api.md` (interface contracts). Read those first for the static facts; this one captures the *workflow* and *gotchas*.

Doc-mirror of this content lives in the project at `doc/WORKFLOW.md`.

## Day-to-day iteration loop

1. Edit `.java` files under `src/main/java/<your.package>/`.
2. `mvn install` — packages + copies staged mod into `${game.mod.directory}/Example Mod/` (default `%APPDATA%\songsofsyx\mods\Example Mod\` on Windows).
3. Launch the game (via the `MainLaunchLauncher` IntelliJ run config, or just open the game). Mods auto-load from that folder.
4. For breakpoints, run the `DEBUG` IntelliJ config with the green-bug icon — JVM will pause on breakpoints in your mod code.
5. Tail logs at `%APPDATA%\songsofsyx\logs\` if behavior is unexpected.

Asset-only changes (no Java touched) — `mvn install` still works; the resources plugin re-copies `mod-files/` content. No need to restart IntelliJ between changes.

## Template-specific gotchas (Argon Script Example)

These are NOT in the deeper reference memories because they are quirks of *this* template, not the game itself:

1. **The example MainScript / InstanceScript use `System.out.println(...)`** — replace with `snake2d.LOG.ln(...)` (and `LOG.err(...)` for errors). LOG output is captured to game logs at `%APPDATA%\songsofsyx\logs\`; raw stdout is harder to find. Once `GAME` exists, `GAME.Notify(msg)` is also available and prefixes a stacktrace.

2. **The placeholder package is `your.mod`** — rename to a globally unique package (e.g., `com.<yourname>.<modname>`) before publishing. Class-name collisions across mod JARs throw `Errors.DataError` at game load (the loader requires every loaded class name to be unique across mods + game).

3. **`.run/DEBUG.run.xml` references `target/Example Mod.jar` literally**. If you change `<mod.name>` in `pom.xml`, also update the `<entry path="$PROJECT_DIR$\target\<NEW NAME>.jar" />` line in `DEBUG.run.xml` — IntelliJ won't auto-sync. The `Examples.jar` and `Tutorial.jar` classpath entries also pin to a Steam install path; adjust if your game install is elsewhere.

4. **`mvn validate` is a one-shot per game version**. After the game updates:
   - Launch the new game once to confirm the new version number.
   - Bump `<game.version.major>` / `<game.version.minor>` in `pom.xml`.
   - `mvn clean && mvn validate` to re-stage the new game JARs into the local Maven repo.
   - The "extracted source tree" used by Claude memories also goes stale — re-extract via the recipe in `reference_songsofsyx_sources.md` and refresh stale accessor lists in the other reference memories if anything was renamed.

5. **Lombok is on the classpath as `provided`** — `@NoArgsConstructor`, `@SuppressWarnings`, etc. work in this project but won't carry into the runtime jar. Don't ship lombok-annotated public APIs to other mods that consume yours.

6. **The shade plugin builds an uber-jar excluding `com.songsofsyx:*`** — your mod jar contains its dependencies but never the game JAR. If you add a third-party dependency, it WILL get bundled (potentially conflicting with another mod). Keep deps minimal; prefer using the game's own `snake2d.util.sets` / `util.data` types over Guava/Apache Commons.

## When something goes wrong

- **Mod doesn't appear in launcher** → `_Info.txt` filename or contents wrong (case-sensitive). Check `<mod.install.directory>/_Info.txt` was actually copied with token substitution (`VERSION: "1.0.0",` not `VERSION: "${mod.version}",`).
- **Mod marked "unsupported"** → `Vxx/` folder name case (must be `V70`, not `v70`); or `_Info.txt` `GAME_VERSION_MAJOR` doesn't match the running game major.
- **Game crashes on load with `DataError`** → class-name collision. Check that no other installed mod has a class at the same FQCN.
- **Save corruption / `handleBrokenSavedState` triggered** → mod's serialized format changed between save and load. Either keep save format stable or implement `handleBrokenSavedState()` to return `true` and skip cleanly.
- **Maven build fails with class-not-found** → `mvn validate` not run since last game-version bump.

## Distribution paths

- **Manual**: zip `target/out/<mod.name>/` (containing `_Info.txt` + `V70/...`) — users extract into `%APPDATA%\songsofsyx\mods\`.
- **Steam Workshop**: `mvn install -P mods-uploader` stages to the Workshop Manager dir; upload via [SteamWorkshopManager](https://github.com/VizardAlpha/SteamWorkshopManager).
