---
name: Songs of Syx codebase overview
description: Package map of the game's source JAR — which top-level package owns what, file counts, and which other reference memories drill into each one
type: reference
originSessionId: 4e70a062-1427-421c-9733-c0f85b14eb41
---
Songs of Syx is a Java/LWJGL city-builder/strategy game built on a custom 2D engine (`snake2d`). Source extracted to `C:/Users/Nate/.m2/repository/com/songsofsyx/songsofsyx/70.32/sources/` — see `reference_songsofsyx_sources.md` for path/version details. Total ~2,370 .java files. Snapshot taken 2026-04-25 against game version reported in pom as 70.32 (`VERSION.java` constants reported 70.33 — re-check after game updates).

## Top-level packages

| Pkg | Files | Role | Drill-down memory |
|-----|-------|------|-------------------|
| `script/` | 3 | Mod loader & SCRIPT interface — **mod entry point** | `reference_songsofsyx_script_api.md` |
| `init/` | 135 | Bootstrap, PATHS, config-file loading, content registries (races/rooms/resources/tech/etc) | `reference_songsofsyx_init.md` |
| `launcher/` | 13 | Pre-game launcher window (settings, mod selection, language) — rarely modded |
| `menu/` | 15 | Main menu screens (ScMain, ScLoad, ScCampaign, ScOptions) — `SC` is the screen interface | `reference_songsofsyx_view.md` |
| `game/` | 342 | `GAME` singleton + subsystems: audio, battle, factions, events, save, time, tourism, raiding, nobility, boosting | `reference_songsofsyx_game.md` |
| `settlement/` | 989 | The city/colony layer — rooms, entities (humans/animals), jobs, pathfinding, tilemap, stats, weather | `reference_songsofsyx_settlement.md` |
| `world/` | 190 | Strategic overworld — terrain, regions, armies, factions, world-gen, fog of war | `reference_songsofsyx_view.md` |
| `view/` | 362 | Rendering & UI pipeline — `VIEW` orchestrates SettView/WorldView/BattleView; widgets, interrupters | `reference_songsofsyx_view.md` |
| `snake2d/` | 210 | Custom 2D engine — `CORE`, Renderer, Input, GraphicContext, shaders (.txt), low-level collections | `reference_songsofsyx_engine.md` |
| `util/` | 111 | Shared utilities — colors, custom collections, GUI primitives, INFO tooltips, text/localization, sprite composer | `reference_songsofsyx_engine.md` |

## Cross-cutting key singletons / static accessors

- **CORE** (snake2d) — engine root: `CORE.renderer()`, `CORE.getInput()`, `CORE.getGraphics()`, `CORE.getSoundCore()`, `CORE.getUpdateInfo()`. Lifecycle: `CORE.init()` → `CORE.create(SETTINGS)` → `CORE.start(state)`.
- **GAME** (game) — game-state hub: `GAME.SETT`, `GAME.WORLD`, `GAME.factions`, `GAME.events`, `GAME.script()`, `GAME.saver()`, `GAME.speed`, etc. Recreated per session via `GAME.create(GameSpec)`.
- **SETT** (settlement.main) — settlement hub: `SETT.ROOMS()`, `SETT.ENTITIES()`, `SETT.JOBS()`, `SETT.PATH()`, `SETT.TILE_MAP()`, `SETT.STATS()`, `SETT.THINGS()`.
- **WORLD** (world) — overworld hub: `WORLD.TERRAIN()`, `WORLD.REGIONS()`, `WORLD.ARMIES()`, `WORLD.ENTITIES()`, `WORLD.LANDMARKS()`, `WORLD.FOW()`, `WORLD.MINIMAP()`, `WORLD.RD()`.
- **VIEW** (view.main) — render orchestrator: `VIEW.current()`, `VIEW.world()`, `VIEW.s()`, `VIEW.b()`, `VIEW.UI()`, `VIEW.inters()`.
- **PATHS** (init.paths) — every asset/data location. See init memory.
- **TIME** (game.time) — game clock cycles (hours/days/seasons/years/ages).
- **D** (util.text) — localization dictionary; use `D.get("KEY")` / `D.ts(class)` for text.
- **LOG** (snake2d) — `LOG.ln(msg)` / `LOG.err(msg)` for debug output (auto-prefixes caller).

## Where mods plug in (5 main hook points)

1. **`script.SCRIPT` implementation** — main entry; ships in your mod JAR. See `reference_songsofsyx_script_api.md`.
2. **JSON config files** in `assets/init/.../*.txt` — overlay vanilla via `VirtualFolder` precedence (mod → lang → vanilla, first match wins).
3. **`game.save.Savable` subclasses** registered with `GAME.saver().add(s)` — persist mod state with each save.
4. **`view.interrupter.Interrupter` subclasses** + custom `GuiSection` widgets — inject UI panels/dialogs.
5. **`view.keyboard.KeyPage` / `Key`** — bind hotkeys to `ACTION` callbacks.

## Build/runtime conventions

- **Custom collections everywhere**: don't use `java.util.ArrayList` for shared data; the codebase uses `snake2d.util.sets.ArrayList`/`ArrayListGrower`/`LIST` and primitive wrappers (`INT`, `DOUBLE`).
- **Mutable primitive boxes**: `INT`, `DOUBLE`, `BOOLEAN` etc. with `.set()` / `.get()` — observable variants (`INT_O`) for property binding.
- **GL thread vs update thread**: rendering on GL thread, logic on Updater thread. Use `CORE.GlJob` to queue cross-thread GL work.
- **Resource cleanup**: extend `GameDisposable` for static state that should reset on game restart. `CORE.addDisposable(CORE_RESOURCE)` for engine-level resources.
- **No-arg public constructors required** for any class loaded via reflection (SCRIPT impls, RoomBlueprint, etc.).
