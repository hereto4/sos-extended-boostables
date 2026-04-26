---
name: Songs of Syx mod packaging — folder layout, _Info.txt, build flow
description: Mod folder structure & _Info.txt schema, V70/V71 overlay rules, _IgnoreVanilla, room-naming semantics, mvn validate/install flow, IntelliJ run configs, phase availability
type: reference
originSessionId: 4e70a062-1427-421c-9733-c0f85b14eb41
---
Source: project's `doc/README.md`, Argon Script Example mod template (this project).

## Folder layout

```
<Mod Name>/                  ← top-level folder name = display name
├── V70/                     ← MUST match the major game version (case-sensitive: V70, not v70)
│   ├── assets/              ← configs, sprites, sounds, text — mirrors game's base/data.zip/data
│   ├── campaigns/           ← custom campaigns (WIP)
│   ├── examples/            ← example save games for the mod
│   ├── saves/               ← downloadable save mod
│   └── script/              ← compiled .jar files (mod code)
└── _Info.txt                ← REQUIRED metadata (exact filename, case-sensitive)
```

A mod can ship multiple `Vxx/` folders side-by-side to support several game versions (`V68/`, `V69/`, `V70/`...).

## _Info.txt schema

```
VERSION: "1.0.0",
GAME_VERSION_MAJOR: 70,
GAME_VERSION_MINOR: 32,
NAME: "Example Mod",
DESC: "What this mod does.",
AUTHOR: "Your Name",
INFO: "Additional notes",
TEXTURE_CACHE_SIZE: 4069,    // OPTIONAL — default 4069 bytes, max 16384. Increase if mod has many sprites.
```

- `VERSION` follows semver (`MAJOR.MINOR.PATCH`).
- `GAME_VERSION_MAJOR` = highest supported game major version (gates which `Vxx/` is loaded).
- `GAME_VERSION_MINOR` = highest supported minor; less critical than major.
- File MUST be exactly `_Info.txt` (capital I — `_info.txt` won't work).

If `_Info.txt` is missing or malformed, the mod shows as **unsupported** in the launcher.

## Asset overlay rules (V70 vs V71)

**V70**: any same-named file under `mod/V70/assets/...` fully replaces the vanilla file. There is no partial-merge; you must ship the entire file's content.

**V71+** (huge compatibility win):
- A same-named file is treated as a **partial overlay** and merges its keys into vanilla.
- To force full replacement, add `__OVERWRITE: true,` at the top of the file.
- Example: `mod/V71/assets/init/race/CANTOR.txt` containing only `PLAYABLE: true,` flips that single key on existing CANTOR — the rest stays vanilla.

**`_IgnoreVanilla.txt`**: drop any file with this exact name in any folder; the loader skips ALL vanilla files in that folder, leaving only your mod files. Useful to e.g. remove every vanilla race.

**Adding new content**: drop a new file with a unique name into the relevant folder. For rooms, the prefix matters:
- `_<NAME>.txt` (leading underscore) = unique singleton room, code-bound — **cannot add new ones without code changes** (e.g., `_THRONE.txt`, `_HOSPITAL.txt`).
- `<TYPE>_<VARIANT>.txt` = freely addable variant of an existing type (e.g., `WORKSHOP_CROSSBOW.txt`, `FARM_wheat.txt`, `MINE_SALT.txt`). The matching `RoomsCreator<ROOM_TYPE>` auto-loads them.

## Mod install locations

**General (manual install — recommended for development)**:
- Windows: `%APPDATA%\songsofsyx\mods\`
- Linux: `~/.local/share/songsofsyx/mods/`

**Steam Workshop (auto-managed)**:
- Windows: `C:\Program Files (x86)\Steam\steamapps\workshop\content\1162750\`
- Linux: `~/.steam/steam/steamapps/workshop/content/1162750/`

(Linux Proton workaround for Steam mods not appearing: symlink `<proton-prefix>/drive_c/users/steamuser/Application Data/songsofsyx/mods` → the workshop folder. See `doc/README.md` FAQ for full steps.)

## Build flow (this project's pom.xml)

`pom.xml` properties drive `_Info.txt` generation and the output path:
- `<game.version.major>` / `<game.version.minor>` — the `Vxx` folder.
- `<mod.name>`, `<mod.version>`, `<mod.author>`, `<mod.desc>`, `<mod.info>`.
- `<game.installation.directory>` — where the game JAR lives (used by `validate`).
- `<game.mod.directory>` — destination for `install` (defaults to `%APPDATA%\songsofsyx\mods` on Windows).
- `<game.mod.uploader.directory>` — for the `mods-uploader` profile.

Maven phases:
- `mvn validate` — copies `SongsOfSyx.jar` and `info/SongsOfSyx-sources.jar` from the game install into the local Maven repo as a dependency. **Run once after install or game update.**
- `mvn package` — compiles + assembles the mod under `target/out/<mod-name>/V70/...` (includes `_src` source dump).
- `mvn install` — same as package, but also copies the result into `<game.mod.directory>` (excluding `_src`). Game will load it on next launch.
- `mvn clean` — deletes `target/` AND removes the mod from the game mod folder.
- `mvn install -P mods-uploader` — copy into the Steam Workshop Uploader staging dir.
- `mvn install -P linux` — force linux profile (auto-detected on Linux).

**Mod source location in this template**: `src/main/java/your/mod/...`. Two example classes:
- `MainScript implements SCRIPT` — the entry point.
- `InstanceScript implements SCRIPT.SCRIPT_INSTANCE` — per-game state.

**Resources** (assets, sprites): `src/main/java/resources/mod-files/...` — copied at `package` phase to the output mod folder.

## Run/debug from IntelliJ

`.run/` ships three configs:
- `MainLaunchLauncher` — launcher window first (normal flow).
- `Main` — skips the launcher, jumps straight to game.
- `DEBUG` — for green-bug-icon debugging; needs extra classpath entries:
  ```xml
  <entry path="...\Songs of Syx\base\script\Examples.jar" />
  <entry path="...\Songs of Syx\base\script\Tutorial.jar" />
  <entry path="$PROJECT_DIR$\target\Example Mod.jar" />
  ```
  Adjust paths and the built jar's filename per your environment.

Working directory must be set to the game install folder (e.g., `C:/Program Files (x86)/Steam/steamapps/common/Songs of Syx`). Main class for Eclipse: `init.MainLaunchLauncher`.

## Game phase availability (for mod hooks)

Confirmed in v70.32 against doc/README's test classes — what's safe to access in each lifecycle phase:

**`initBeforeGameCreated()`** — before `GAME` exists. Available:
- `CORE.*` (renderer, graphics, input, soundCore, updateInfo, GLThread)
- `PATHS.*` (all paths, currentMods, isDevelop, isSteam)
- `TERRAINS.*` (INFO, MAP, FOREST, WET, MOUNTAIN, NONE, OCEAN, ALL)
- `UI.decor()`, `UI.FONT()`, `UI.PANEL()`
- `TECHS.INFO()`

**`initBeforeGameInited()`** — `GAME` exists, partially init. Suitable for `IDebugPanel.add(...)` registrations.

**`createInstance()`** — all game resources up. Available:
- All of the above PLUS
- `GAME.s/world/factions/events/battle/...` accessors
- `SETT.*`, `WORLD.*`, `TIME.*`, `STATS.*`, `STANDINGS.*`, `LAW.*`
- `BOOSTABLES.*`, `DISEASES.all`, `RACES.*`, `RESOURCES.*`, `SPRITES.*`
- `WARMYD.*` (world-army data), `WINDU.*` (world-individual data)
- `FACTIONS.*` (player/tradeUtil/rel/other/all)

**During `update(ds)`** (running game) — additionally:
- `GAME.texture()` (TextureHolder; null before)
- `VIEW.*` (s, b, world, messages, mouse, hoverBox, inters, UI)

## Steam Workshop publishing

Use the bundled mods-uploader profile to stage:
```
mvn install -P mods-uploader   # copies to upload-staging dir
mvn clean   -P mods-uploader   # removes from staging (for re-staging)
```
Then upload from `${user.home}/AppData/Roaming/songsofsyx/mods-uploader/` (Windows) via the Steam Workshop Manager tool (`https://github.com/VizardAlpha/SteamWorkshopManager`).

## Common gotchas

- `_Info.txt` typo (`_info.txt`) → mod doesn't load.
- Wrong case `v70` instead of `V70` → mod shows as unsupported.
- Forgetting `__OVERWRITE: true,` on V71 when you actually want to replace, not merge.
- Targeting V70 but using V71-only PATHS/SETT/STATS accessors (the doc/README's test class lists them; many don't exist in v70.32 — verify against source).
- Class name collisions across mod JARs throw `Errors.DataError` at load time. Use mod-unique package names.
- Multiple SCRIPT-implementing classes in one JAR is unsupported — split into separate JARs.
