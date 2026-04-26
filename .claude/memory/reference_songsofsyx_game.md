---
name: Songs of Syx game/ — GAME singleton, save/load, time, subsystems
description: GAME state hub; how to register Savables, hook ticks, observe TIME cycles; survey of audio/battle/factions/events/raiding/nobility/boosting subsystems
type: reference
originSessionId: 4e70a062-1427-421c-9733-c0f85b14eb41
---
Source: `game/` (342 files). Top-level: `GAME.java`, `GameDisposable.java`, `GameSpec.java`, `GameSpeed.java`, `VERSION.java`. Subdirs: `audio/`, `battle/`, `boosting/`, `debug/`, `event/`, `events/`, `faction/`, `nobility/`, `raiding/`, `save/`, `time/`, `tourism/`, `values/`.

## GAME — the per-session hub

`GAME.create(...)` constructs a `GAME` (constructor first calls `GameDisposable.disposeAll()` to scrub prior session state). Subsystems are private fields exposed via static methods:

**Static accessors** (verified against v70.32 GAME.java):
```java
GAME.s()          → SETT settlement
GAME.world()      → WORLD
GAME.factions()   → FACTIONS         (player + NPC factions)
GAME.player()     → Player
GAME.events()     → EVENTS           (riot/disease/killer/advisor/uprising/factionWar/Peace/worldRebellion)
GAME.EVENT()      → EVENT_HANDLER    (narrative dialog/choice engine)
GAME.battle()     → BattleUtil       (damage/morale calc)
GAME.ARMIES()     → Armies           (2 armies × ~24 divisions)
GAME.BATTLE_THREADS() → BattleThreads (off-thread combat AI)
GAME.NOBLE()      → NOBLES
GAME.raiders()    → RAIDING
GAME.script()     → ScriptEngine; mod hook callback = GAME.script().callback (SCRIPT_INSTANCE)
GAME.intervals()  → Intervals        (fixed-frequency polling helper)
GAME.saver()      → GameSaver
GAME.count()      → GCOUNTS          (achievement counters)
GAME.BOOST()      → TmpBoosting      (transient boosts)
GAME.BOOSTS()     → SuperBoostables  (persistent boosts)
GAME.texture()    → TextureHolder    (running game only — null before)
GAME.SPEED        → GameSpeed        (public static field, NOT a method)
```

**Lifecycle / control**:
```java
GAME.update(double seconds)            // main tick driver
GAME.updateI() : int                   // update iteration counter
GAME.version() : int
GAME.achieving() / achieve(boolean)    // achievement gating

GAME.addOnInit(ACTION)                 // after game inited
GAME.addOnViewInit(ACTION)             // after view inited
GAME.addBeforeGameStarts(ACTION)       // just before play starts
GAME.addAfterUpdate(ACTION)            // after each tick
```

**Logging convenience** (alternatives to `LOG.ln`):
```java
GAME.Notify(CharSequence)              // logs message + stacktrace
GAME.Notify(Object)
GAME.Error(CharSequence) / Error(String)
GAME.Warn(String)
GAME.WarnLight(String)
```

Note: there is no `GAME.stats()` accessor in v70.32 (the doc/README's test references it but it's absent here). Settlement stats are reached via `SETT.STATS()` if present, or `GAME.s()`-mediated paths.

## GameSpec — session metadata + save compatibility

Holds `version`, `population`, `enemies`, `regions`, `wx`, `wy`, `race`, `city`, `ruler`, `desc`, `String[] scripts`, `String[] mods`, plus checksums `races/rooms/resources/industries`. Mismatched checksums → `warning()` / `crashCause()` returns a non-null reason → game refuses load. `boolean fubar` indicates corrupted save.

## GameSpeed

`speed` (target), `actualSpeed` (smoothed), `tmpPaused`, `updateOnce`. Speed presets: 0/1/5/25/250.
- `speedSet(double)`, `togglePause()`, `tmpPause()`, `updateOnce()`, `isPaused()`, `save/load`.

## VERSION

Compile constants: `VERSION_MAJOR`, `VERSION_MINOR`, `VERSION_STRING`, `VERSION` (encoded). Helpers: `version(major, minor)`, `versionMajor(int)`, `versionMinor(int)`, `versionIsBefore(major, minor)`.

## GameDisposable

Static cleanup pattern. `extends GameDisposable` auto-registers (constructor pushes to `initers` list); `dispose()` is called by `disposeAll()` at next game creation. Used by event system, faction trackers, BOOSTING — anywhere static state survives `new GAME()`.

## Save system (game/save/)

`Savable` is the base class — every persistable subsystem subclasses it.
```java
public abstract class Savable {
    final CharSequence key;     // unique id (collision = error)
    protected abstract void save(FilePutter file);
    protected abstract void load(FileGetter file) throws IOException;
    protected void loadFail() { throw RuntimeException(...); }
}
```

`GameSaver`:
- `add(Savable s)` — register in normal queue.
- `addSpecialSaver(Savable s)` — high-priority (init-phase) queue.
- `Path save(String name, boolean minified)` — manual save.
- `void autoSave(double ds)` — auto-save tick.
- Lifecycle hooks (each takes `ACTION_O<Path>`):
  - `onBeforeSave`, `onAfterSave`, `onBeforeLoad`, `onAfterLoad`.

Saves are zipped; per-`Savable` block is prefixed with key + size for crash-safe skipping. **For mods**: prefer the SCRIPT_INSTANCE save/load hooks (see `reference_songsofsyx_script_api.md`); only register your own `Savable` if you have data outside the mod's instance lifecycle.

## TIME (game/time/)

`TIME extends GameResource`. Cycle hierarchy via `TIMECYCLE`: `Hours` → `Days` (default 2 hours/day per season — note: actually configurable) → `Seasons` (4/year) → `Years` → `Ages`.

Per-cycle methods: `bitSeconds()`, `bitCurrent()`, `bitPartOf()` (0–1 progress through current bit), `bitName(int)`.

Static accessors:
```java
TIME.currentSecond()        // wraps at age boundary
TIME.hours() / .days() / .seasons() / .years() / .age()
TIME.secondsPerHour() / hoursPerDay() / secondsPerDay()
TIME.servicePerDay() = 4    // services (hospital/temple) tick 4×/day
TIME.workSeconds()          // daily working window
TIME.light()                // Light cycle driven by hours
```

**No event-broadcasting on day/season change** — pattern is to poll. Idiomatic:
```java
int lastDay = TIME.days().bitCurrent();
public void update(double ds) {
    if (lastDay != TIME.days().bitCurrent()) { /* day boundary */ lastDay = TIME.days().bitCurrent(); }
}
```
Or use `Intervals` for fixed-frequency polling without explicit timers.

## Subsystem one-liners

- **audio/** — `AUDIO` orchestrates `Music`, `SoundFactory`, `Ambiances`, `SoundRaces` (race-specific SFX). `update(ds)` per frame.
- **battle/** (~80 files) — `Armies`/`Army`/`Div`; `BattleThreads` runs combat AI; `Strategos2000` plans offense/defense; `BattleStatus` tracks HP/morale; `DivFormation` positions; `BattleOrders` queues commands; `DivFactors` computes damage.
- **boosting/** — modifier framework. `Boostable` values (e.g., pop cap) modified by `Booster` instances. Mods register boostables via `BOOSTING.push()`. `TmpBoosting` for transient, `SuperBoostables` for persistent.
- **debug/** — `Profiler.LIVE` (instrumented) / `.DUMMY` (no-op); used pervasively as `update(ds, Profiler)`.
- **event/** — narrative engine. `Event` (JSON-driven) with `EInfo`, `EOccurence`, `EDuration`, `ESelection`, `EChoice`, `ECondition`, `EActions`. `EContext` carries dispatch data.
- **events/** — concrete event types. `EventCitizen` (riots/strikes), `EventDisease`, `EventKiller`, `EventAdvisor`, `EventUprising`, `EventFactionWar/Peace`, `EventWorldRebellion`. Each is a `GameResource` persisted via `SuperSaver<EventResource>`.
- **faction/** (~50 files) — `FACTIONS` manages `Faction[64]`. `Player` is user. `FactionNPC` driven by `UpdaterNPC`. `DIP` tracks wars/peace. `TradeManager`/`ResourcePrices` simulates trade. Updates spread across days via `IUpdater`.
- **nobility/** — `NOBLES`, `Noble`, `NobleOffice`. Office bonuses via `BoostCompound<NobleOffice>`. Cap controlled by `MAX` boostable.
- **raiding/** — `RAIDING`, `Raider`, `RaidingCurrent`, `Updater`/`UpdaterRegions` (region invasion polling), `RaidingMap` (entry points).
- **save/** — see "Save system" above.
- **time/** — see "TIME" above.
- **tourism/** — `TOURISM` tracks per-race `Review[]`, history, allowed races. Updater handles arrivals/departures/credit grants.
- **values/** — `GCOUNTS`: global achievement counters (cumulative `SAccumilator`s). Stored as `GameResource`.

## Idiomatic mod patterns

- **Tick into a system**: implement `SCRIPT_INSTANCE.update(ds)` (mod's natural hook) — `GAME.addAfterUpdate` is more invasive.
- **Detect day/season change**: cache `TIME.days().bitCurrent()` in instance state; compare each tick.
- **Add a stat**: extend `Savable` for the counter, register with `GAME.saver().add()` from `initBeforeGameInited()`.
- **Modify a global value (e.g., max pop)**: register a `Booster` against the appropriate `Boostable` via `BOOSTING.push(...)`.
- **Watch a faction**: hold reference; in `update()`, query `GAME.factions.player()` / `GAME.factions.all()` and compute deltas.
