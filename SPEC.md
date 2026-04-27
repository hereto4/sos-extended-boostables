# sos-extended-boostables — Project Specification

## Purpose
Add new `ROOM_*`-style boostable keys to Songs of Syx (v70.32) that are not present in vanilla, enabling other mods and technologies to target room types that previously had no boostable. The first implementation is `ROOM__SLAVER`, targeting the Slaver room (`settlement.room.law.slaver.ROOM_SLAVER`).

The boostable keys produced by this mod are **consumed by technologies** (handled externally). This mod is responsible only for:
1. Registering the boostable key so it exists in the game's boost system
2. Reading that key's value at runtime and applying it to the target room's behavior

---

## Project Structure

```
sos-extended-boostables/
├── SPEC.md                         ← this file
├── _Info.txt                       ← SoS mod metadata (generated from pom.xml properties)
├── pom.xml                         ← Maven build config
├── src/main/java/your/mod/
│   └── MainScript.java             ← Java source: registers boostable + booster
└── V70/
    └── script/
        └── sos-extended-boostables.jar  ← compiled mod jar (deployed by `mvn install`)
```

**Note on registration approach:** In principle, boostables can be registered via data files in `V70/assets/init/stats/boost/`. **For this mod's key (`ROOM__SLAVER`, double underscore), data files do not work** — see [File scanner gotcha](#file-scanner-gotcha) below. We register programmatically from `MainScript`.

---

## How SoS Boostables Work

### Registration paths
Boostables enter `BOOSTING.MAP()` via two paths:

1. **Data files** in `assets/init/stats/boost/` paired with text in `assets/text/stats/boost/`. Loaded by `BOOSTABLES.<init>` during INIT-resource construction.
2. **Programmatic** via `BOOSTING.push(key, baseValue, name, desc, icon, cat)`. Vanilla rooms use this via `RoomBlueprintIns.pushBo`. Mods can call it from a `SCRIPT.initBeforeGameInited()` hook.

### Data file format (FYI — not used by this mod)
**Init file:**
```
ICON: <sheet_index>-><sprite_group>-><sprite_index>,
CATEGORY: <CATEGORY_KEY>,
BASE_VALUE: <double>,
```
**Text file:**
```
NAME: "<display name>",
DESC: "<description>",
```

### Key derivation (`BOOSTING.push`)
`game.boosting.BOOSTING.push(String key, ...)`:
1. Strips ONE leading `_` if present: `"__SLAVER"` → `"_SLAVER"`
2. Prepends the category prefix: `"ROOM_" + "_SLAVER"` = `"ROOM__SLAVER"`

**Category → Prefix map:**
| CATEGORY value | Prefix |
|---|---|
| `ROOM` | `ROOM_` |
| `PHYSICS` | `PHYSICS_` |
| `BEHAVIOUR` | `BEHAVIOUR_` |
| `ACTIVITY` | `ACTIVITY_` |
| `BATTLE` | `BATTLE_` |
| `CIVIC` | `CIVIC_` |
| `NOBLE` | `NOBLE_` |

### File scanner gotcha
`init.paths.VirtualFolder.getClean()` filters out **any filename starting with `_`** when listing asset files:

```java
private static String getClean(String s, String ending) {
    if (s.charAt(0) == '_')
        return null;   // skipped silently
    ...
}
```

To produce `ROOM__SLAVER` (double underscore) via a data file, the filename would need to be `__SLAVER.txt` so `BOOSTING.push` strips one underscore and the result has the second underscore. **But that filename starts with `_` and is silently skipped.** Therefore: **data files cannot register any key whose post-prefix portion begins with `_`** — including `ROOM__SLAVER`. The only path is programmatic registration.

---

## ROOM__SLAVER — Boostable Specification

### Boostable key
`ROOM__SLAVER`

### Category
`ROOM` (appears in the Buildings boostable collection in-game UI)

### Base value
`1.0` (multiplicative; 1.0 = no change from base)

### Target room
`settlement.room.law.slaver.ROOM_SLAVER`
- Internal key: `"_SLAVER"`
- Category: `CATS.LAW`
- Has no vanilla boostable — `bonus()` returns null

### What the room does
Converts prisoners to slaves. A prisoner AI (`Enslaved`) walks to a processing station, a worker AI (`WorkSlaver`) processes them, and the prisoner is converted to `HTYPES.SLAVE()`. Station count is determined by room furniture layout.

### Boostable effect target
**Option B — Per-Entity Submission Bonus** (finalized)

Adds an additive bonus to `BOOSTABLES.BEHAVIOUR().SUBMISSION` that applies only to slaves processed through the Slaver room. Two-layer effect:
- **Per-entity display**: individual processed slaves show the bonus in their info panel; trade/born slaves show 0
- **Population uprising risk**: scales proportionally by `processedRatio = parole_slaves / total_slaves`

---

## Step 3: Effect Target Options

| Option | Effect | Implementation | Status |
|---|---|---|---|
| **A. Worker Efficiency** | Reduces workers required per station below default `ceil(n/4)` | Script adjusts `employees().neededSet()` per instance | Not chosen |
| **B. Submission Bonus** | Processed slaves receive a `BEHAVIOUR_SUBMISSION` boost; scales by % processed | Custom `BValue` using per-entity `CAUSE_ARRIVE` DataNibble + processedRatio | **CHOSEN** |
| **C. Processing Speed** | Reduces per-station cycle time | AI timing hardcoded; requires reflection — not cleanly achievable | Not recommended |
| **D. Capacity Multiplier** | Inflates reported station count | May cause inconsistencies | Not recommended |
| **E. NPC Slaver Event Frequency** | Reduces merchant slaver event cooldown | Unrelated to room processing | Out of scope |

---

## Per-Entity Slave Origin Tracking

### How vanilla tracks slave origin
Every `Induvidual` carries a `DataNibble("POP_ARRIVE")` set via `STATS.POP().COUNT.arrive`. This records the `CAUSE_ARRIVE` of the entity's last type transition. It is save-compatible (part of `STATS.count()` serialization).

**Slaver-room converts** (`Enslaved.java:82`):
```java
a.HTypeSet(HTYPES.SLAVE(), null, CAUSE_ARRIVES.PAROLE());
```
→ `arrive = PAROLE`

**Trade slaves / NPC slaver event** (`PeopleSpawner.java:151`):
```java
Humanoid h = SETT.HUMANOIDS().create(r, tx, ty, t, CAUSE_ARRIVES.IMMIGRATED());
```
→ `arrive = IMMIGRATED`

**Why PAROLE on a slave is unambiguous:**
`CAUSE_ARRIVES.PAROLE()` is also used in `ResFree.java`, `Prison.java`, and `AIModule_Prisoner.java` — but all of those result in `HTYPES.SUBJECT()` (citizen), not slave. So any entity with `hType() == SLAVE` and `arrive == PAROLE` definitively went through the Slaver room.

### Script-side access
```java
STATS.POP().COUNT.arrive.get(induvidual) == CAUSE_ARRIVES.PAROLE()
```

### `processedRatio` computation
Iterate `SETT.ENTITIES().getAllEnts()`, filter to `Humanoid` instances with `hType() == SLAVE`, count those with `arrive == PAROLE`, divide by total. Recompute every 4 seconds in `SCRIPT_INSTANCE.update()`. Recompute immediately in `SCRIPT_INSTANCE.load()` — no extra save state needed.

---

## Deployed File Locations

| File | Path |
|---|---|
| Script JAR | `V70/script/sos-extended-boostables.jar` |
| Script source | `src/main/java/your/mod/MainScript.java` |
| Mod metadata | `_Info.txt` (generated by `mvn install` from pom.xml properties) |

No data files are deployed — registration is fully programmatic (see [File scanner gotcha](#file-scanner-gotcha)).

---

## Game Source Reference

Unzipped sources (v70.32) are located at:
```
.claude/game-source-java/
```

Key files for this project:
| File | Purpose |
|---|---|
| `game/boosting/BOOSTABLES.java` | Defines all boostable collections including ROOMS |
| `game/boosting/BOOSTING.java` | `push()` method — key derivation logic |
| `game/boosting/BValue.java` | `BValue` interface; `BValueInduOnly`, `BValuePlayerOnly` abstract classes |
| `game/boosting/BoosterValue.java` | Booster implementation; ctor: `(BValue, BSourceInfo, to, isMul)` |
| `settlement/stats/colls/StatsPopulation.java` | `StatsDeath.arrive` — per-entity `CAUSE_ARRIVE` DataNibble |
| `settlement/stats/standing/StandingSlave.java` | How `BEHAVIOUR_SUBMISSION` boosters are structured |
| `settlement/entity/humanoid/ai/types/prisoner/Enslaved.java` | Conversion event; sets `CAUSE_ARRIVES.PAROLE()` on slave |
| `settlement/entry/PeopleSpawner.java` | Trade slave spawn; sets `CAUSE_ARRIVES.IMMIGRATED()` |
| `settlement/room/law/slaver/ROOM_SLAVER.java` | Slaver room blueprint |
| `settlement/room/law/slaver/SlaverStation.java` | Per-station state machine |
| `settlement/entity/humanoid/ai/work/WorkSlaver.java` | Worker AI |
| `script/SCRIPT.java` | Scripting interface |

---

## SCRIPT Interface Summary

Mods implement `script.SCRIPT` with these lifecycle hooks:
- `initBeforeGameCreated()` — called before game objects exist; can register hooks
- `initBeforeGameInited()` — called after game created, before finalization
- `createInstance()` — returns a `SCRIPT_INSTANCE` for runtime behavior
  - `SCRIPT_INSTANCE.update(double ds)` — called each game tick (~60/sec)
  - `SCRIPT_INSTANCE.save(FilePutter)` / `load(FileGetter)` — save support

---

## GAME.<init> Timing (critical for boostable registration)

When a new game is started or loaded, the GAME constructor runs in this order (`game/GAME.java`):

| Line | Action | What it means for us |
|---|---|---|
| 118 | `script = new ScriptEngine(...)` | `MainScript` constructor + `initBeforeGameCreated` runs here. **BOOSTING/BOOSTABLES not yet initialized for this game.** |
| 123 | `INIT init = new INIT()` | Constructs all `INIT.InitResource`s: BOOSTING runs `clear()` → `BOOSTABLES.init()` (vanilla registered) → other resources (incl. TECH which **adds promises** for unresolved boostable references). |
| 172 | `script.init.initBeforeGameInited()` | **Our registration hook**. Map is set up, vanilla is registered, TECH promises are queued — but `finishSetup` has not run yet. **Register custom boostables here.** |
| 177 | `init.finish()` | Calls `BOOSTING.finishSetup()` which runs `waiting`/`connecters` actions, including resolving deferred TECH promises. Custom boostables registered at line 172 resolve here. |

**Implication**: registering in `initBeforeGameCreated()` (line 118) or in the script constructor is **wiped** by `BOOSTING.clear()` at line 123. The window between line 123 and line 177 — i.e., `initBeforeGameInited()` — is the only safe place to register a custom boostable that other mods' tech files (data-driven) need to bind to.

---

## MainScript.java Implementation

The actual source is in `src/main/java/your/mod/MainScript.java`. Key responsibilities:

1. **Register `ROOM__SLAVER` in `initBeforeGameInited()`** via `BOOSTING.push("__SLAVER", ..., BOOSTABLES.ROOMS())` — see [GAME.<init> Timing](#gameinit-timing-critical-for-boostable-registration) for why this hook (not the constructor or `initBeforeGameCreated`).
2. **Add a `BoosterValue` on `BEHAVIOUR_SUBMISSION`** with a custom `BValue` that splits behavior by query type:
   - `vGet(Induvidual indu)` — per-entity: returns the bonus only for slaves whose `STATS.POP().COUNT.arrive == CAUSE_ARRIVES.PAROLE()` (i.e. slaver-room-processed).
   - `vGet(Player f)` / `vGet(PopTime t)` — population-level: returns `(roomSlaver.get(f) - 1.0) * processedRatio`. Feeds the uprising-risk calculation in `StandingSlave`.
   - `vGet(FactionNPC)` / `vGet(Region)` / `vGet(Div)` — `0`.
3. **Maintain `processedRatio`** in `SCRIPT_INSTANCE.update()` every 4 seconds: iterate `SETT.ENTITIES().getAllEnts()`, count slaves with `arrive == PAROLE` vs total slaves. Recompute on `load()`. No save-state needed — derived from per-entity `DataNibble` which is already saved.

Key implementation details:
- `MainScript` is `final`, has an explicit no-arg constructor (no Lombok `@NoArgsConstructor`, since SoS instantiates it via reflection at script-load time and we need to make sure no logic runs in the constructor that depends on game state).
- `forceInit() = true` so the script applies to all games without selection.
- The `BValue` is implemented directly (not `BValuePlayerOnly`/`BValueInduOnly`) because we need different behavior per query context.
- `BoosterValue(bv, info, 2.0, false)` — additive booster, range 0→2.0 (matches scale of vanilla Army submission booster).

---

## Build System

Maven (`pom.xml` in project root). Compile against `songsofsyx-70.32.jar` and the game's scripting template.

Output JAR goes to `V70/script/`.

### Build / install commands

```
mvn install -Dmaven.test.skip=true   # build + deploy mod into project root
```

The `-Dmaven.test.skip=true` is needed because the template's `ExampleTest.java` uses Lombok `@Data` and the annotation processor isn't wired up correctly — pre-existing template breakage, unrelated to mod functionality.

### ⚠️ DO NOT run `mvn clean`

The template's `maven-clean-plugin` is configured to delete `${mod.install.directory}`, which **resolves to the project root itself** because the mod is built and installed in-place at `~/AppData/Roaming/songsofsyx/mods/sos-extended-boostables`. Running `mvn clean` will **delete every tracked file** in the project root (`SPEC.md`, `pom.xml`, `src/`, `doc/`, `LICENSE`, `_Info.txt`, etc.) and leave only `.git/` and `.claude/` as survivors.

If `mvn clean` has already been run, recovery requires restoring from `.git/` (which itself may have lost `refs/` — reconstruct from `.git/logs/refs/heads/main` if so).

To run a clean build safely, fix the pom first (e.g. add an `<excludes>` keeping `pom.xml`, `src/`, `SPEC.md`, etc.) or use plain `mvn install` which is non-destructive.

---

## Mod Info

`_Info.txt` must declare the mod's name, version, and game version compatibility per SoS modding standards.
