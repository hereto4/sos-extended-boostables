---
name: Songs of Syx SCRIPT mod API
description: The SCRIPT and SCRIPT_INSTANCE interfaces every mod implements — full hook lifecycle, signatures, and load mechanics
type: reference
originSessionId: 4e70a062-1427-421c-9733-c0f85b14eb41
---
**Source files** (3): `script/SCRIPT.java`, `script/ScriptEngine.java`, `script/ScriptLoad.java` under the extracted source tree.

## The SCRIPT interface (factory + metadata)

A mod's main class implements `SCRIPT`. Required:

- `CharSequence name()` — display name in mod selection UI.
- `CharSequence desc()` — description.
- `SCRIPT_INSTANCE createInstance()` — factory; called once per game (start or load), AFTER all game resources are inited. Per-game state belongs on the returned instance.

Default-implemented (override only if needed):

- `void initBeforeGameCreated()` — before the `GAME` object is constructed. Use for reflection/meta-setup; usually no-op.
- `void initBeforeGameInited()` — after `GAME` exists but before everything has been "tightened". **Returns `void`** (the doc/README example showing `public SCRIPT_INSTANCE initBeforeGameInited()` is incorrect for v70.32 — that signature won't compile against `SCRIPT.java`). Common use: register debug panel commands.
- `boolean isSelectable()` — default `true`; return `false` to auto-inject (no UI checkbox).
- `boolean forceInit()` — default `false`; return `true` to force-load even when user disabled it (warning: breaks save compatibility).

**In-game debug commands**: from `initBeforeGameInited()` (or `createInstance()` for instance state), register custom dev-tool buttons:
```java
import view.interrupter.IDebugPanel;
IDebugPanel.add("My Tool", () -> { /* runs when clicked */ });
```
The button appears in the in-game **developer-tools** panel (top-right above minimap). Requires Developer mode enabled in the launcher.

## SCRIPT_INSTANCE (per-game runtime)

Required:

- `void update(double ds)` — called every game tick (~60 Hz). `ds` = in-game seconds elapsed. **Primary logic hook.**
- `void save(FilePutter file)` — serialize state.
- `void load(FileGetter file) throws IOException` — deserialize state.

Optional (default no-op):

- `void render(Renderer r, float ds)` — post-game-render overlay. `ds` = real frame seconds.
- `void hover(COORDINATE mCoo, boolean mouseHasMoved)` — mouse position + motion flag.
- `void hoverTimer(double mouseTimer, GBox text)` — populate tooltip text after hover dwell.
- `void keyPush(KEYS key)` — key-press listener.
- `void mouseClick(MButt button)` — click listener.
- `boolean handleBrokenSavedState()` — return `true` to continue when saved-state size mismatches; `false` to skip mod load (graceful degrade).

## Mod loading flow

1. Mod JARs live in mod folders under `PATHS.local().MODS` (Steam Workshop folder also auto-detected).
2. `ScriptLoad.Init` scans each JAR's entries; for every `.class` (skip META-INF), extracts FQCN.
3. **Validates uniqueness**: class names must not collide across JARs (utility libs OK), nor with game classes — throws `Errors.DataError` on conflict.
4. Loads via `ClassLoader.getSystemClassLoader()` — no per-mod isolation.
5. Instantiates with `clazz.newInstance()` → **requires a public no-arg constructor**.
6. Wraps in `ScriptLoad(script, className, jarFile)`; registered in static `KeyMap` cache (key format `"jarfile->classname"`).
7. `ScriptEngine` matches user-selected mod keys against the cache, honors `forceInit()`, then constructs an aggregator `SCRIPT` that fan-outs every hook to all loaded mods.
8. Each hook call is wrapped in try/catch; errors are logged with full stacktrace and rethrown as `Errors.DataError` (terminates game load).

**Constraint**: One SCRIPT-implementing class per JAR (multiple mod JARs are fine; multiple SCRIPT classes in one JAR are not).

## Save/load protocol

Each mod's save block is prefixed with its key + byte-size. On load, sizes are checked; if mismatched, `handleBrokenSavedState()` is consulted. Returning `false` causes the mod to be skipped (logged) rather than crashing the load. This lets mods evolve their save format with controlled fallback.

## Idiomatic mod skeleton

```java
public class MyMod implements SCRIPT {
    public CharSequence name() { return "My Mod"; }
    public CharSequence desc() { return "Does cool stuff."; }
    public SCRIPT_INSTANCE createInstance() { return new Inst(); }

    private static class Inst implements SCRIPT_INSTANCE {
        public void update(double ds) { /* logic */ }
        public void render(Renderer r, float ds) { /* HUD */ }
        public void save(FilePutter f) { f.i(myInt); }
        public void load(FileGetter f) throws IOException { myInt = f.i(); }
        int myInt;
    }
}
```

The Argon SDK (used by More Options mod) wraps this with phase callbacks (`onModsLoaded`, `onGameSaveLoaded`, etc.) — see Argon SDK docs in `doc/howto/mod_sdk_setup.md`.
