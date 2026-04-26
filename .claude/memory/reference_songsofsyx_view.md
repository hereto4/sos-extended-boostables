---
name: Songs of Syx world/, view/, menu/ — UI, rendering, world map
description: VIEW orchestrator, GuiSection/Interrupter widget framework, hotkey system, menu screens, WORLD strategic layer, world-gen
type: reference
originSessionId: 4e70a062-1427-421c-9733-c0f85b14eb41
---
Source paths under the extracted source tree.

## WORLD (world/, 190 files)

`WORLD` is the strategic-overworld facade. Static accessors return into a `Data` inner holder:
- `WORLD.TWIDTH()` / `THEIGHT()`
- `WORLD.TERRAIN()` — terrain map
- `WORLD.REGIONS()` — `WREGIONS` (regional subdivisions)
- `WORLD.ARMIES()` — strategic-layer armies
- `WORLD.ENTITIES()` — world-layer entities
- `WORLD.BUILDINGS()`, `WORLD.LANDMARKS()`
- `WORLD.FOW()` — fog of war
- `WORLD.LOG()` — world event log
- `WORLD.BATTLES()`
- `WORLD.MINIMAP()`
- `WORLD.RD()` — region data (see below)
- `WORLD.CENTRE()` — view center
- `WORLD.PATH()` — world pathfinding

`WorldResource` / `WorldResourceManager` is the per-subsystem base. `protected WorldResource(CharSequence name, String key)`; override `update(double ds, Profiler)`, `afterTick()`, `initBeforePlay()`, `saver()`. Render entry: `WORLD.render(Renderer r, float ds, int zoomout, RECTANGLE renWindow, int offX, int offY)` delegates to internal `Render`.

**Settlement coupling**: SETT and WORLD are loosely coupled — settlements live on the world map but maintain independent state. Settlement queries world via `SETT.WORLD_AREA().info.initCity(...)`.

### WorldGen (world/WorldGen.java)

Non-instantiable generation state machine. Fields: `hasGeneratedTerrain`, `isEditing`, `isDone`, `lat` (latitude 0–1), `seed`, `playerX`/`playerY`, `map` (`WorldGenMapType`).

`WorldGenMapType` loads PNG height-maps from `sprite/world/generatorMaps/` — square byte-encoded, bilinear-filtered via `h(x, y)` / `h(x, y, w, h)`. Full save/load support. **Mod hook**: register custom `WorldGenMapType` and swap into `WorldGen.map`. Actual terrain generation lives in `world/map/terrain/WorldTerrain` (not in `WorldGen` directly).

### Regions (world/region/, RD.java)

`WORLD.REGIONS()` (`WREGIONS`) divides the world into territorial units, each bound to a faction. `RD` (region data) holds dense per-region caches:
- `RDBuildings`, `RDOutputs`, `RDMilitary`, `RDReligions`, `RDRaces`
- `RDDistance`, `RDHealth`, `RDDevastation`, `RDEvent`, `RDProspects`
- `RDUpdater` — ticks region economy/military each turn.
- `Realm[] drea` — one per faction; faction-wide region bonus / prosperity / military caches.

Mods hook `RDUpdater` for custom region logic.

## VIEW (view/, 362 files)

`VIEW` (in `view/main/`) is the top-level `CORE_STATE` orchestrating render, input, UI.

**Static accessors** (verified against v70.32 VIEW.java):
```java
VIEW.current()       → ViewSubSimple   (active context — Sett/World/Battle)
VIEW.s()             → SettView
VIEW.world()         → WorldView
VIEW.b()             → BattleView
VIEW.mouse()         → Mouse
VIEW.UI()            → UIView          (global tabbed UI)
VIEW.inters()        → Interrupters    (top-level modal/persistent panels)
VIEW.messages()      → Messages
VIEW.hoverBox()      → GBox            (current tooltip box)
VIEW.timeBox()       → GBox
VIEW.hideUI() / hide() / canSave() / existTemp()
VIEW.renderSecond() : double
VIEW.RI() : int
VIEW.renI            // public static int field
VIEW.setKeyPoller(KeyPoller)
VIEW.setPrev() / hoverBoxDistance(int)
```
Note: `VIEW.saver()` (mentioned in doc/README) does not exist in v70.32.

`ViewSub` / `ViewSubSimple` — abstract context renderer. Each holds an `InterManager uiManager` for its own dialogs.

**Frame loop** (`VIEW.update`):
1. `game.afterTick()` → `inters.manager.afterTick()` → `current.uiManager.afterTick()` → `current.afterTick()`.
2. Keyboard polled context-sensitively: `KeyPageSett` if SETT active, `KeyPageWorld` if world, etc.

**Render** (`VIEW.render`):
1. Clear screen + terrain shadows.
2. `inters.manager.render()` — fullscreen modals first (back-to-front via deque).
3. `current.uiManager.render()` — UI panels.
4. `current.render()` — viewport last.
   Each layer can return false to block the layers behind it.

**Hover/tooltip**: `hoverTimer` accumulates; after ~0.4s, nested UI managers populate `GBox` via `hoverTimer(GBox)`.

**Mod injection**: `VIEW` calls `GAME.script().callback.mouseClick(button)`, `hover(mCoo, moved)`, `hoverTimer(time, hoverBox)`, `render(r, ds)` — these route to your `SCRIPT_INSTANCE` hooks.

### View contexts

- `view/sett/SettView` — settlement/city. Holds `GameWindow window` (2D viewport over settlement tiles), `UIPanelTopSett ui` (top toolbar), `ISidePanels panels`, `ToolManager tools` (placement), `SBattleView battle`, `UIMinimapSett mini`.
- `view/world/WorldView` — strategic. `GameWindow window`, `WorldUI UI` (faction/region/army overlays), `ToolManager tools`, `WorldViewEditor editor`. Calls `WORLD.render()` from window callback.
- `view/battle/BattleView` — tactical battle. `DivSelection selection`, `BattlePlacer placer`, `BattleRenderer renderer`, `BattlePanel panel`, `UIMinimapSett minimap`. Active when `BattleView.battle.isActive()`.

Each context: `activate()`, `hover(COORDINATE, moved)`, `update(float ds)`, `render(Renderer, float ds)`.

## UI widget framework (util/gui/ + view/ui/)

**`view/ui/UIView`** — flat registry of 10 fullscreen panels accessed via tabs in `IManager`: `UITreasury`, `UIGoods`, `UITechTree`, `UIProfile`, `UILevel`, `UIHealth`, `UITourists`, `UIRaiding`, `UILog`, `WIKI`, `UIDiv`. Each implements `IFullView` with a `GuiSection section`, `SPRITE icon`, `String title`.

**Foundational widgets** in `util/gui/`:
- `GBox` (misc/) — dynamic info-box: `ArrayList<Ren>`, `ArrayList<GText>`, `GPanel box`, `Scroll`. `clear()`/`title()`/`add(GText)`/`add(RENDEROBJ)`/`sep()` build content with auto-layout.
- `GButt` (misc/) — clickable button. `GButt.Base` wraps 4-state sprites + label `SPRITE`. Methods: `replaceLabel(SPRITE, DIR)`, `hoverSet(INFO)`, `hoverTitleSet()`, `hoverInfoSet()`.
- `GPanel` (panel/) — framed box w/ optional close. `title()`, `pad(x, y)`, `setWidth()`, `setHeight()`. Variants: thin/thick (UI.PANEL()).
- `GText` (misc/) — formatted text sprite. `lablify()`, `lablifySub()`, `normalify()`, `hoverify()`, `clickify()` preset colors.
- `GInput` (misc/) — text input. Wraps `StringInputSprite`. Drag-select + keyboard listen.
- `GDropDown` (misc/) — dropdown menu. SPRITE title + `ArrayListResize<E extends CLICKABLE>` options in a `GuiSection expansion`.
- `GSliderInt` / `GSliderHor` / `GSliderVer` (slider/) — range sliders.
- `GMatrix` / `GRows` / `GStaples` / `GScrollable` (table/) — grid/list layouts; `GTableBuilder` fluent table API.
- Pickers (common/): `UIPickerRes`, `UIPickerRace`, `UIPickerRegion`, `UIPickerArmy`.

**Composition pattern**: nest `snake2d.util.gui.GuiSection` (auto-layouts children). Add via `.addRightC()`, `.addDown()`, `.add(widget)` with positional constraints.

## Interrupter framework (view/interrupter/)

`Interrupter` — abstract base for fullscreen/modal overlays:
```java
boolean hover(COORDINATE, boolean moved);   // true = consume
boolean click(MButt);                        // true = consume
boolean render(Renderer, float ds);          // false = block layers behind
void hoverTimer(GBox);                       // populate tooltip on long hover
show(InterManager) / hide();
```

`InterManager` — deque (LIFO). `add()` pushes top; `render` walks back-to-front. Click/hover routing top-down. `desturbingfuck=true` causes manager to hide non-persistent interrupters below the new one.

Built-in subclasses:
- `InterGuisection` — wraps a `GuiSection`; auto-routes clicks/hovers to children.
- `IPopup` — single centered/anchored renderobj; supports push/pop.
- `IPromtYesNO` — yes/no dialog with `ACTION` callbacks.
- `ITextInput` — text prompt.
- `ISidePanels` — left/right collapsible sidebar.
- `IDebugPanelAbs` — debug overlay base.

**Mod pattern**:
```java
class MyDialog extends Interrupter {
    public boolean render(Renderer r, float ds) { /* draw */ return true; }
    public boolean click(MButt b) { /* handle */ return true; }
}
new MyDialog().show(VIEW.current().uiManager);
```

## Hotkey system (view/keyboard/)

`KEYS` registers 4 `KeyPage`s: `KeyPageMain` (always active), `KeyPageSett`, `KeyPageWorld`, `KeyPageBattle` (active per current ViewSub).

`KeyPage`:
- `ArrayListResize<Key> all` + `MapIndexed<Key> map`
- `get(modCode, keyCode) → Key`

`Key` carries an `ACTION` and pressed/down state with `consumeClick()`/`reset()`.

`KeyPoller` consumes `KeyEvent` lists, ticks all keys, invokes actions. Mods register custom Keys: `KEYS.MAIN().MYACTION = new Key(ACTION)` (or extend `KeyPage` and register in `KEYS` constructor).

## Menu (menu/, 15 files)

`Menu` is the main-menu `CORE_STATE`. Holds `ScMain main`, `ScLoad load`, `ScCampaign campaigns`, `ScOptions options`, `ScRandom sandbox`, `ScCredits credits`, `Background bg`, `Logo logo`, `Intro intro`, `RESOURCES res`, `current : SC`.

`SC` is an **interface** (not abstract class). Methods:
```java
boolean hover(COORDINATE mCoo);
boolean click();
void render(SPRITE_RENDERER r, float ds);
boolean back(Menu menu);                        // true = pop to prior
void poll(KeyEvent e);                          // optional
void renderBackground(Background, float ds, COORDINATE mCoo);  // default impl
```

Subclasses are screens: `ScMain`, `ScLoad`, `ScCampaign`, `ScOptions`, `ScRandom`, `ScCredits`. Mods can implement `SC` and switch via `menu.switchScreen(custom)` (or extend an existing `Sc*` and override).

`Menu.start()` creates `CORE`, then `Menu.make()`. Layout helpers in `menu/GUI.java`.

## Modder UI cheat-sheet

- **Add a settlement-context panel**: build `GuiSection`, wrap in `InterGuisection`, `.show(VIEW.s().uiManager)`.
- **Add a tab in the global UI**: implement `IFullView`, register in `IManager`/`UIView`.
- **Add a hotkey**: `new Key(action)` registered in the appropriate `KeyPage`.
- **Custom render overlay**: `SCRIPT_INSTANCE.render(Renderer, float)` — fires after the game scene renders.
- **Custom tooltip**: `SCRIPT_INSTANCE.hoverTimer(double, GBox)` — populate the box.
