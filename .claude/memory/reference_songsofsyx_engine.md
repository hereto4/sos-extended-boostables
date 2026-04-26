---
name: Songs of Syx snake2d engine + util — CORE, rendering, input, collections
description: Engine APIs mods call directly — Renderer/SPRITE drawing, Input, LOG, custom collections (LIST/ArrayListGrower/INT), text/localization, sprite composer
type: reference
originSessionId: 4e70a062-1427-421c-9733-c0f85b14eb41
---
Source: `snake2d/` (210 files: 73 top-level + 13 `util/` subdirs) and `util/` (111 files).

## CORE — engine root (snake2d.CORE)

Singleton entry point. **Mods don't call lifecycle methods**; the engine does. Mods consume the static accessors:

```java
CORE.getGraphics()      → GraphicContext      (display metrics)
CORE.getInput()         → Input               (keyboard/mouse)
CORE.renderer()         → Renderer            (sprite pipeline)
CORE.getSoundCore()     → SOUND_CORE
CORE.getUpdateInfo()    → CoreTime            (elapsed s/ms/ns + pendulum oscillator)
CORE.addDisposable(CORE_RESOURCE r);          // register engine-level cleanup
```

**Threading**: GL operations on `glThread`, logic on Updater thread. Cross-thread GL work via `CORE.GlJob`:
```java
new CORE.GlJob() { protected void perform() { /* on GL thread */ } }.perform();
```

**State swap**: `CORE.setCurrentState(state)` — `state` is a `CORE_STATE`. The engine uses this for menu ↔ game transitions.

## Rendering — Renderer / SPRITE

Canonical draw call:
```java
CORE.renderer().renderSprite(int x1, int x2, int y1, int y2, TextureCoords texture);
```

`SPRITE` (interface, snake2d.util.sprite) — preferred high-level API:
```java
sprite.render(SPRITE_RENDERER r, int x1, int x2, int y1, int y2);   // explicit bounds
sprite.render(r, int x1, int y1);                                    // top-left, auto-size
sprite.renderC(r, int cx, int cy);                                   // centered
sprite.renderCScaled(r, int cx, int cy, double scale);
```

Render state on `Renderer`:
```java
setColor(COLOR) / setNormalColor()
setOpacity(OPACITY) / setNormalOpacity()
shadeLight(boolean)
shadowDepthSet(byte) / lightDepthSet(byte)
```

`TextureCoords` — UV bounds (`x1, x2, y1, y2` shorts) on a texture atlas. `TILE_SHEET` and `SpriteSheet` build on it.

**Shaders** — GLSL 3.30 core, source in `snake2d/*.txt` (27 files):
- `Particle_v/f.txt`, `Particle_texture_v/f.txt`, `Particle_debug_v/f.txt`
- `LightPoint_v/g/f.txt`, `LightPointUni_v/g/f.txt` — point lights
- `LightTile_v/g/f.txt` — tile-based ambient
- `LightAmbient_v/f.txt` — global ambient
- `Displace_v/g/f.txt` — water/wave displacement
All use `const vec2 screen = vec2(SCREEN_X, SCREEN_Y)` and `const vec2 trans = vec2(-1.0, 1.0)` for screen→NDC.

`util/rendering/` builds higher-level helpers on top: `RenderData` (viewport/culling state for tile rendering), `Minimap`, `ShadowBatch` (dynamic shadow casting). Settlement/world rendering goes through `RenderData.RenderIterator`.

## Input — Input/KeyBoard/Mouse

`CORE.getInput()` returns `Input` with `getMouse()` / `getKeyboard()`:

`KeyBoard`:
- `KeyEvent[]` storage, `KEYACTION` enum: `PRESS` / `RELEASE` / `REPEAT`.
- `keyboard.listener = CHAR_LISTENER` for typed text:
  ```java
  acceptChar(char c); enter(); backspace();
  left(boolean mod); right(boolean mod);
  ```
- `KEYCODES` — constants (`KEY_ESCAPE`, `KEY_ENTER`, `KEY_LEFT_SHIFT`, `KEY_PRINT_SCREEN`, ...).

`Mouse`:
- `getMouse_Coo()` → `Coo` (game-space x, y).
- `MButt.LEFT` / `RIGHT` / `WHEEL` / `WHEEL_SPIN`.
- `MButt.consumeClick()` / `consumeAllClick()` / `isDouble()`.
- `MButt.peekWheel()` / `clearWheelSpin()`.

Polling is engine-driven (`Input.poll(nano, focused)`). View layer (`VIEW`) routes events via context-aware `KeyPage`s and `Interrupter` chains — see `reference_songsofsyx_view.md`.

## Logging — LOG and GAME.Notify

```java
LOG.ln(Object info)            // print w/ caller class:line — to game log
LOG.ln(Object a, Object b)
LOG.ln(Object[] info)
LOG.err(Object info)           // stderr
LOG.bits(long l) / LOG.WS(int) / LOG.NL()
```
**Always use `LOG` instead of `System.out`** — output is captured to game logs (`PATHS.local().LOGS`).

For game-aware logging (after `GAME` is constructed), `GAME.Notify(...)` writes the message **with a stacktrace** — useful for tracing where unexpected calls originate. Plus `GAME.Error(...)` / `GAME.Warn(...)` / `GAME.WarnLight(...)`.

## PathGame / PathTile

These are A* pathfinding primitives, **not** file paths. `PathGame` interface (`getCapacity`/`length`/`isStart`/`isDest`/`hasNext`/`next`); `PathTile` is the A* node (`x, y`, `accCost`, `value`, `pathParent`). Mods rarely instantiate; access via `SETT.PATH().finders().find(...)` and `WORLD.PATH()`.

## Custom collections (snake2d.util.sets + util.data)

The codebase uses its own collection types instead of `java.util.*`. **Use these for any data that crosses the API boundary**:

| Type | Replaces | Notes |
|------|----------|-------|
| `LIST<E>` | `List<E>` (read-only) | Immutable view |
| `ArrayList<E>` | `ArrayList<E>` (fixed) | No grow; faster, no GC |
| `ArrayListGrower<E>` | `ArrayList<E>` (dynamic) | Grows; preferred for variable-size data |
| `ArrayListResize<E>` | — | Reusable backing array |
| `ArrayListInt` | `int[]` / `List<Integer>` | Primitive, no autobox |
| `Bitmap1D` | `BitSet` | Compact bit storage |
| `HashMap` (custom) | `HashMap` | Often keyed by index/coord |
| `KeyMap<E>` | — | String-keyed |
| `MapIndexed<E>` | — | int-keyed |
| `Stack`, `Queue` | java.util counterparts | |

Geometric primitives (`snake2d.util.datatypes`):
- `COORDINATE` — interface (`x()`, `y()`)
- `Coo` / `ShortCoo` — mutable coords
- `RECTANGLE` / `Rec` — bounds (immutable / mutable)
- `BODY` — collision shape
- `DIR` — direction enum
- `DIMENSION` — w/h.

Mutable primitive boxes (`util.data`):
- `INT`, `DOUBLE`, `LONG`, `BOOLEAN` — `.set(v)` / `.get()`
- `INT_O`, `DOUBLE_O`, `LONG_O` — observable; trigger listeners on change (use for reactive UI binding)
- `GETTER`, `GETTER_TRANS` — lazy/derived values
- `RANMAP`, `DataO`, `DataOSimple`

Maps (`snake2d.util.map`): `MAP` (immutable 2D), `MapInt`, `MapArray2D`.

## Colors — COLOR / OPACITY

`COLOR` interface with 50+ presets:
```java
COLOR.WHITE100, WHITE150, WHITE200, ...
COLOR.RED100, RED200, GREEN100, BLUE100, ...
COLOR.UNIQUE[]              // palette of distinct colors
new ColorImp(r, g, b)       // custom (0–255)
new ColorShifting(from, to) // animated transition
```
`OPACITY`: `OpacityImp.O100` / `O50` / `O25` / etc. for alpha.

`util/colors/` adds game-specific palettes (faction colors, status indicators).

## Text & localization (util.text)

`D` — main dictionary class. `Dic` per-locale; `DicTime` for time strings ("3 days").

```java
D.get("KEY_NAME")                   // localized CharSequence
D.ts(SomeEnum.class)                // typed lookup for an enum/registry
D.get("HELLO").insert(InsertHuman.h(myHuman), InsertFaction.f(myFac));
```

`Inserter` subclasses for variable substitution: `InsertHuman` (`{HUMAN}`), `InsertFaction`, `InsertRace`, `InsertRegion`. Mods add custom `Inserter`s for their own entities.

`snake2d.util.sprite.text`: `Text` (rendered text object), `Str` (mutable string).

## INFO — tooltip protocol (util.info)

```java
class INFO {
    CharSequence name, names, desc;
    ACTION wiki;
    void hover(GUI_BOX box);
}
new INFO(json)   // extracts NAME / NAMES / DESC fields
```

Used pervasively — when the player hovers over an entity/building, the game calls `info.hover(tooltipBox)`. Pair with `GBox.title()` to render structured tooltips.

## Sprite composer (util.spritecomposer, 13 files)

Pre-renders entity sprite combinations (e.g., humanoids = body + clothing + hair) into texture atlases at startup, then references via `SpriteData`.

```
SpriteData          per-sprite metadata (UV, size)
ComposerSources     load source images
ComposerDests       assemble into atlases
ComposerThings      entity composition rules        ← extend this for new entities
ComposerTexturer    bind textures
ComposerFonter      bind fonts
Result              final sheet output
Optimizer           repack into power-of-2 textures
```

## File I/O (snake2d.util.file)

- `FileGetter` — read primitives (`f.i()`, `f.d()`, `f.s()`, `f.b()`).
- `FilePutter` — write primitives.
- `SAVABLE` — interface (note: distinct from game's `Savable` class).
- `Json` — typed JSON config reader: `j.d("KEY")`, `j.i("KEY", min, max)`, `j.text("KEY")`, `j.json("SUBKEY")`.
- `SnakeImage` — PNG load/save.

## util/ subdirectory roles

- `util/colors/` — color types/palettes.
- `util/data/` — `INT`/`DOUBLE`/observable boxes (above).
- `util/error/` — error formatting.
- `util/gui/` — foundational widgets (covered in view memory).
- `util/info/` — `INFO` tooltip class.
- `util/keymap/` — keymap helpers.
- `util/race/` — race-rendering helpers.
- `util/rendering/` — `RenderData`, `Minimap`, `ShadowBatch`.
- `util/spritecomposer/` — entity sprite assembly.
- `util/statistics/` — statistical helpers.
- `util/text/` — `D`/`Dic`/`Inserter` localization.
- `util/updating/` — update helpers.

## Engine cheat-sheet for mods

| Need | Use |
|------|-----|
| Draw a sprite | `mySprite.render(CORE.renderer(), x, y)` or `renderC(...)` |
| Read mouse position | `CORE.getInput().getMouse().getMouse_Coo()` |
| Detect a key press | Register a `Key` in a `KeyPage` (preferred) or check `MButt.consumeClick()` |
| Log debug info | `LOG.ln(...)` |
| Localize text | `D.get("KEY")` / `D.ts(class)` |
| Pop a tooltip | populate the `GBox` passed to `hoverTimer(...)` |
| Iterate over many ints | `ArrayListInt` (no boxing) |
| Coordinate type | `COORDINATE` (interface) / `Coo` / `ShortCoo` |
| Rectangle | `Rec` (mutable) / `RECTANGLE` (immutable) |
| Color | `COLOR.WHITE100` etc.; `ColorShifting` for animation |
