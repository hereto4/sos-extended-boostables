---
name: Songs of Syx init/ — bootstrap, PATHS, config files
description: Startup sequence, asset/path constants, JSON config-file system, content-registry per domain (race/room/tech/etc)
type: reference
originSessionId: 4e70a062-1427-421c-9733-c0f85b14eb41
---
Source: `init/` subtree (135 files). Subdirs: `constant/`, `paths/`, `race/`, `religion/`, `resources/`, `settings/`, `sprite/`, `structure/`, `tech/`, `type/`, `value/`. Top-level: `INIT.java`, `Main.java`, `MainLaunchLauncher.java`, `MainProcess.java`.

## Startup sequence

`Main.main(args)` (when `args[0] == "launcher"`):
1. Parse `LSettings`.
2. `PATHS.init(...)` with the user's selected mod list — `ModInfo` objects validate version against `VERSION.VERSION_MAJOR`; mod folders are added to the priority-ordered VirtualFolder.
3. Compute mod classpaths via `PATHS.SCRIPT().modClasspaths()`.
4. Spawn `MainProcess` via `Proccesser.executeLwjgl()`.

`MainProcess.main()`:
1. `PreLoader.load()` — splash.
2. `CORE.init(new ErrorHandler())` — graphics/sound.
3. `PATHS.init()` — re-init with language + easy-mode flags.
4. `D.init()` — localization.
5. `Menu.start()` — enter main menu loop.
6. `PTitles.achieve()` — process achievements.
7. `PreLoader.exit()`.

`INIT` constructor (called from new-game initialization, in this order):
```
new Config()      // reads assets/init/config/*.txt (Battle, Sett, etc.)
new UI()          // sprite/UI assets
new GVALUES()     // dynamic Value<T> registries
new BOOSTING()    // boost framework
new RACES()       // race definitions
new TYPEINIT()    // hardcoded enums (traits, diseases, climates...)
new RESOURCES()   // craftables/foods/ores
new RELIGIONS()
new TECHS()
new STRUCTURES()  // buildable structures
```
Each calls `CORE.checkIn()` health-checks. Then `INIT.finish()` triggers `InitResource.finishSetup()` for late-stage cross-references (e.g., `GVALUES.finishSetup() → GValuesInit.init()`).

## PATHS — every asset/data location

`PATHS` provides typed accessors (each returns a `PATH`/`SemiMod`/`ResFolder` rooted in the merged VirtualFolder). Mod folders are added BEFORE vanilla in the resolution chain — first match wins.

**Game-data roots** (verified v70.32 PATHS.java — these are the actual method names):
```
PATHS.INIT()                 → assets/init                (config definitions)
PATHS.CONFIG()               → assets/init/config         (Battle.txt, Sett.txt, ...)
PATHS.INIT_SETTLEMENT()      → assets/init/settlement
PATHS.INIT_WORLD()           → assets/init/world

PATHS.TEXT()                 → assets/text
PATHS.TEXT_MISC()            → assets/text/misc
PATHS.TEXT_SETTLEMENT()      → assets/text/settlement
PATHS.TEXT_WORLD()           → assets/text/world
PATHS.NAMES()                → assets/text/names
PATHS.DICTIONARY()           → assets/text/dictionary

PATHS.SPRITE()               → assets/sprite              (.png files)
PATHS.SPRITE_UI()            → assets/sprite/ui
PATHS.SPRITE_SETTLEMENT()    → assets/sprite/settlement
PATHS.SPRITE_SETTLEMENT_MAP()→ assets/sprite/settlement/map
PATHS.SPRITE_GAME()          → assets/sprite/game
PATHS.SPRITE_WORLD()         → assets/sprite/world
PATHS.SPRITE_WORLD_MAP()     → assets/sprite/world/map

PATHS.AUDIO()                → PATHS_AUDIO holder         (mono / music / ambience / config sub-paths)
PATHS.BASE()                 → PATHS_BASE holder          (game launcher / icons / preloader)
PATHS.MISC()                 → PATHS_MISC holder
PATHS.SCRIPT()               → Script (mod-aware classpath)
PATHS.CACHE_DATA()           → user cache dir
PATHS.CACHE_TEXTURE()        → user cache texture dir

// Combined ResFolders (init + text + sprite for one content type):
PATHS.SETT() / RACE() / WORLD() / STATS() / EVENT() / PLAYER()

// Runtime info:
PATHS.local()                → PATHS_LOCAL                (user dirs — see below)
PATHS.currentMods()          → LIST<ModInfo>              (loaded mods)
PATHS.modHash()              → int                        (hash of active mod set)
PATHS.textureSize() / inited() / isDevelop() / isSteam()
PATHS.getSavePath(Path)
```
**The doc/README references `PATHS.SOUND()`, `PATHS.CACHE_SCRIPT()`, `PATHS.TEXT_CONFIG()`, `PATHS.TEXT_NAMES()` — these do NOT exist in v70.32.** Use `AUDIO()`, `CACHE_DATA()`/`CACHE_TEXTURE()`, no equivalent for `TEXT_CONFIG`, and `NAMES()` respectively.

**Combined ResFolders** (init + text + sprite for a content type):
```
PATHS.SETT()    → init/settlement, text/settlement, sprite/settlement
PATHS.RACE()    → init/race, text/race, sprite/race
PATHS.WORLD()   → init/world, text/world, sprite/world
PATHS.STATS()   → init/stats, text/stats
PATHS.EVENT()   → init/event, text/event
PATHS.PLAYER()  → init/player, text/player
```

**Local (user) paths** via `PATHS.local()`:
```
ROOT             → %APPDATA%\songsofsyx\
SETTINGS         → %APPDATA%\songsofsyx\settings\
MODS             → %APPDATA%\songsofsyx\mods\         (+ Steam workshop if found)
SAVE             → %APPDATA%\songsofsyx\saves\saves\
SAVE_CAMPAIGN    → %APPDATA%\songsofsyx\saves\campaign\
SCREENSHOT       → %APPDATA%\songsofsyx\screenshots\
LOGS             → %APPDATA%\songsofsyx\logs\
CACHE_DATA       → %APPDATA%\songsofsyx\cache\data\
CACHE_TEXTURE    → %APPDATA%\songsofsyx\cache\texture\
```

## Config-file system

Configs are JSON-like text under `assets/init/config/` (and other init/* dirs). Read via `snake2d.util.file.Json` — typed extractors `j.d("KEY")`, `j.i("KEY", min, max)`, `j.text("KEY")`.

**Resolution rule** (`VirtualFolder.getPossibleFile`): iterate `[mods..., lang, vanilla]`; first existing file wins. So a mod that ships its own `assets/init/race/Cantor.txt` overrides vanilla's. To **append** to a multi-file folder (add a new race), drop a new file alongside vanilla — both are read.

**V70 vs V71 overlay rules** (a critical compatibility gotcha):

- **V70**: same-named file fully replaces vanilla. No partial-override mechanism.
- **V71+**: full replacement REQUIRES `__OVERWRITE: true,` in the file, otherwise the file is treated as a partial overlay and individual keys merge into vanilla. This drastically improves cross-mod compatibility.
- **Partial overlay (V71+)**: a one-line file like `PLAYABLE: true,` placed at `Mod/V71/assets/init/race/CANTOR.txt` flips that single key on the existing CANTOR race.
- **Wipe-vanilla**: drop a `_IgnoreVanilla.txt` (any content) in a folder; the loader skips all vanilla files in that folder, leaving only mod files. Useful to remove all vanilla races, etc.

**Room file naming semantics** (from `assets/init/room/`):
- Files prefixed with `_` (e.g., `_ASYLUM.txt`, `_HOSPITAL.txt`, `_THRONE.txt`) are **unique/singleton rooms** — code-bound. You cannot create new instances of these without modifying game code.
- Other files use the convention `<TYPE>_<VARIANT>.txt` (e.g., `WORKSHOP_BOWYER.txt`, `WORKSHOP_CROSSBOW.txt`, `FARM_wheat.txt`). These are freely addable: drop a new `WORKSHOP_*.txt` and the matching `RoomsCreator<ROOM_WORKSHOP>` will auto-load it. This is how 99% of mod-added rooms work.

**Booster syntax** — pervasive in `BOOST: { ... }` and `BONUS: { ... }` config blocks:
```
{BOOSTER_KEY}>{ADD|MUL}: <value>,
```
e.g., `PHYSICS_RESISTANCE_COLD>MUL: 2.0,` or `RELIGION_CRATOR>ADD: -1.0,`. Full key list lives in `doc/res/boosters_all.md` of this mod template.

## Per-domain content registries (one row each)

| Subdir | Registry class | Asset locations | What it defines |
|--------|---------------|-----------------|-----------------|
| `race/` | `RACES extends InitResource` | `assets/init/race/[Name].txt`, `assets/text/race/[Name].txt`, sprite/race/ | Playable + NPC species; appearance, stats, services, boosts, preferences. `RACES.expand()` does sprite-aware late init. |
| `religion/` | `RELIGIONS extends InitResource` | `assets/init/religion/[Name].txt` | Belief systems w/ gameplay effects |
| `resources/` | `RESOURCES extends InitResource` | `assets/init/resource/[Name].txt`, `assets/text/resource/`, `assets/sprite/resource/debris/` | Craftables, foods, drinks, mineable ores, growables |
| `sprite/` | `UI extends InitResource` (with `UIFonts`, `UIPanels`, `UIDecor`, `Icons`) | `assets/sprite/ui/`, `sprite/game/`, `sprite/settlement/`, `sprite/world/` | Fonts, panels, icons, image sheets |
| `structure/` | `STRUCTURES extends InitResource` | `assets/init/structure/[Name].txt`, `text/structure/`, `sprite/settlement/` | Buildable structures (placement rules, sprites, effects) |
| `tech/` | `TECHS` (static) | `assets/init/tech/[Tree].txt`, `text/tech/` | Tech trees: prereqs, costs, unlocks (rooms, boosts) |
| `type/` | `TYPEINIT extends InitResource` (registers `HCLASSES`, `TRAITS`, `DISEASES`, `NEEDS`, `CLIMATES`, `TERRAINS`, `HTYPES`, `WGROUP`, `HGROUP`, `POP_CL`) | None — hardcoded in Java; localization-driven via `D.ts(TRAITS.class)` | Enumerations (hero classes, traits, diseases, needs, climates, terrains, pop classes, arrival/leave causes) |
| `value/` | `GVALUES extends InitResource` | likely `assets/init/value/` | Dynamic `Value<T>` for `INDU` (individual), `REGION`, `FACTION`, `ROYALTY`. Late init in `finishSetup() → GValuesInit.init()`. |

## settings/ — player-facing settings

Class `init/settings/S.java` exposes a flat field-bag, each backed by an `INFO`-extending `Setting` wrapper (or `SettingPerc` for percentage sliders):

- **Graphics**: `shadows`, `particles`, `gore`, `graphics` (detail), `brightness`, `lightCycle`, `uilightCycle`, `downpour`.
- **Audio**: `volumeMaster`, `volumeSound`, `volumeMusic`, `volumeAmbience`, `muteUnfocused`.
- **Gameplay**: `autoSaveInterval` (1–21 min), `autoSaveFiles` (1–10), `scroll` (edge-scroll on/off).
- **Display** (from `SETTINGS` impl): screen mode, resolution, vsync, linear filter, monitor, decoration.

Persistence: `LSettings` (launcher pkg) loads at startup; `S.get().applyRuntimeConfigs()` syncs audio to `CORE.getSoundCore()`. Version drift detected by `S.isNewVersion()`.

## Mod versioning

The mod folder convention is `mod/V<major>/...`. `pom.xml` properties `<game.version.major>` (gating major version) + `<game.version.minor>` (minimum compatible minor) drive the `_Info.txt` and folder layout. See `doc/README.md` "Supporting multiple game versions" and `doc/config/README.md`.
