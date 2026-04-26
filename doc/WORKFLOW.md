# Mod Editing & Packaging Workflow

A walkthrough of how to edit/add scripts, where they live, and how to package the result for distribution. Verified against this project's `pom.xml`, the example `MainScript`/`InstanceScript`, and Songs of Syx v70.32 source.

## A. Where the code lives

```
src/main/java/your/mod/MainScript.java       implements SCRIPT (entry point)
src/main/java/your/mod/InstanceScript.java   implements SCRIPT_INSTANCE (per-game state)
src/main/java/your/mod/...                   any helper classes; package layout is yours
src/test/java/your/mod/                      JUnit 5 tests
src/main/resources/mod-files/                non-Java mod files
src/main/resources/mod-files/_Info.txt       templated; tokens filled at package time
src/main/resources/mod-files/V70/assets/...  (you create) per-version asset overlays
```

The package `your.mod` is illustrative — rename it to anything mod-unique to avoid class-name collisions with other mods (the loader throws `Errors.DataError` on collision).

Lombok is on the classpath (`provided` scope), which is why `MainScript` uses `@NoArgsConstructor` to satisfy SCRIPT's "public no-arg ctor required" rule. You can keep using it or write the constructor yourself.

## B. The two interfaces you override

### `SCRIPT` (one-shot factory + metadata) — `MainScript.java`

| Method | When called | Override for |
|---|---|---|
| `name()` / `desc()` | Mod-selection UI on new game | Display strings |
| `initBeforeGameCreated()` | Before `GAME` exists | Reflection / asset bootstrapping (rare) |
| `initBeforeGameInited()` | After `GAME` constructed, before fully wired | `IDebugPanel.add("My Tool", () -> ...)` registrations |
| `createInstance()` | Once per new-game / save-load, after all resources up | Return your `SCRIPT_INSTANCE` |
| `isSelectable()` | Determines if user can toggle in launcher | Return `false` to auto-inject |
| `forceInit()` | Forces load even if user disabled | Return `true` (breaks save compat) |

### `SCRIPT_INSTANCE` (per-game runtime) — `InstanceScript.java`

| Method | When | Use |
|---|---|---|
| `update(double ds)` | Every tick (~60 Hz), `ds` = in-game seconds | Primary logic |
| `save(FilePutter)` / `load(FileGetter)` | At save/load | Persist mod state |
| `render(Renderer, float ds)` | After game scene renders | HUD overlays |
| `hover(COORDINATE, boolean moved)` | Mouse moves over game world | Tracking position |
| `hoverTimer(double, GBox)` | Long-hover; populate tooltip | Custom tooltip text |
| `keyPush(KEYS)` / `mouseClick(MButt)` | Input | Bind hotkeys / clicks |
| `handleBrokenSavedState()` | If save block size mismatch | Return `true` to recover gracefully |

### Two corrections to apply to the existing example template

- `MainScript.java` and `InstanceScript.java` use `System.out.println(...)`. The proper API is `snake2d.LOG.ln(...)` (or `LOG.err(...)`), which captures into the game log at `%APPDATA%\songsofsyx\logs\`. Once `GAME` is constructed, `GAME.Notify(...)` is also available — it logs with a stacktrace.
- The `[EXAMPLE MOD]` prefix is fine but `LOG.ln` already prepends caller `class:line`.

## C. How to add new code

1. **New script class**: just add a class to `src/main/java/your/mod/`. If it's something the engine should call into, instantiate it from `createInstance()` or hold a reference on `InstanceScript`. Constraints:
   - One `SCRIPT`-implementing class per JAR (multiple JARs OK).
   - Public no-arg ctor on the SCRIPT class.
2. **New asset overlay** (e.g., add a race or override boost values): create the file under `src/main/resources/mod-files/V70/assets/init/<domain>/<name>.txt`. Naming rules:
   - V70: same-named file fully replaces vanilla.
   - V71+: requires `__OVERWRITE: true,` for full replacement; otherwise treated as partial-overlay merge.
   - `_IgnoreVanilla.txt` in any folder skips all vanilla entries there.
   - Rooms: `_NAME.txt` (leading underscore) = code-bound singleton (e.g., `_THRONE.txt`); `TYPE_VARIANT.txt` (e.g., `WORKSHOP_CROSSBOW.txt`) is freely addable and auto-loaded by `RoomsCreator<ROOM_WORKSHOP>`.
3. **Mod-only asset folders** that don't exist in vanilla also work — drop e.g. `V70/assets/sprite/your_mod/icon.png` and reference via `PATHS.SPRITE().getFolder("your_mod")`.

## D. Build pipeline (this project's pom.xml)

| Phase / command | What happens | Where output lands |
|---|---|---|
| `mvn validate` | Installs `${game.install.directory}/SongsOfSyx.jar` and `info/SongsOfSyx-sources.jar` into the local Maven repo as `com.songsofsyx:songsofsyx:70.32`. **Run once per game-version bump.** | `~/.m2/repository/com/songsofsyx/songsofsyx/70.32/` |
| `mvn package` | Compiles → shaded uber-jar (excluding `com.songsofsyx:*`) → `${mod.name}.jar`; copies templated `_Info.txt`, mod-files, jar, and `-sources.jar` into the staged mod folder | `target/out/Example Mod/V70/script/Example Mod.jar` (+ `_src/`, plus `target/out/Example Mod/_Info.txt`) |
| `mvn install` | Runs `package`, then copies the staged folder into the game mod dir (excluding `_src/`) | `%APPDATA%\songsofsyx\mods\Example Mod\` |
| `mvn clean` | Deletes `target/` AND the installed mod folder (per maven-clean-plugin config) | — |
| `mvn install -P mods-uploader` | Also copies into `${game.mod.uploader.directory}/WorkshopContent/Example Mod/` for Steam Workshop upload | — |

### Key pom properties

- `<mod.name>` — controls jar filename and staged folder name.
- `<mod.version>` — fed into `_Info.txt` `VERSION:`.
- `<game.version.major>` / `<minor>` — drives `Vxx/` folder name and `_Info.txt`. **Bump both, then `mvn clean && mvn validate && mvn install`** when the game updates.
- `<game.install.directory>` — defaults to `C:/Gaming/Steam/steamapps/common/Songs of Syx` on Windows. Update if your install path differs (Steam default is `C:\Program Files (x86)\Steam\...`).

## E. Run / debug from IntelliJ

Three `.run/` configs ship with the template:

- `MainLaunchLauncher` → game launcher → main menu (normal flow).
- `Main` → skips launcher, straight into menu.
- `DEBUG` → adds the green-bug-icon classpath modifications. Note the classpath entries reference `target/Example Mod.jar`; if you change `<mod.name>` in the pom, update `.run/DEBUG.run.xml` to match.

Working directory on all three must point at the game install (so `base/data.zip` and friends resolve). Main class for Eclipse: `init.MainLaunchLauncher`.

## F. Distribution

Two paths:

1. **Manual**: zip the `target/out/Example Mod/` folder (which contains `_Info.txt` + `V70/...`) and share. Users drop it into `%APPDATA%\songsofsyx\mods\` (or the Linux equivalent).
2. **Steam Workshop**: `mvn install -P mods-uploader` stages into the Steam Workshop Manager's content dir, then upload via [SteamWorkshopManager](https://github.com/VizardAlpha/SteamWorkshopManager).

## G. Iteration loop (day-to-day)

1. Edit a `.java` file under `src/main/java/your/mod/`.
2. `mvn install` (rebuilds, ships to `%APPDATA%\songsofsyx\mods\Example Mod\`).
3. Launch the game via `MainLaunchLauncher` config (or just run the game). The launcher auto-loads from the mods folder.
4. For interactive debugging: run the `DEBUG` config; set breakpoints in your mod code; the game JVM will stop on them.
5. Check logs at `%APPDATA%\songsofsyx\logs\` if behavior is wrong.

## H. Two easy-to-miss gotchas

1. **`mvn validate` is a one-shot per game version** — easy to forget after a game update, and you get cryptic class-not-found errors. After updating: bump `<game.version.major>`/`<minor>`, then `mvn clean && mvn validate && mvn install`.
2. **Class names across mod JARs must be globally unique** — always use a real package like `com.<yourname>.<modname>.*` rather than the placeholder `your.mod.*`. The loader rejects collisions with `Errors.DataError`.
