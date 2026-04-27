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
├── _Info.txt                       ← SoS mod metadata
├── pom.xml                         ← Maven build config
├── src/                            ← Java source for scripts
│   └── <package>/
│       └── ...Script.java
└── V70/
    ├── assets/
    │   ├── init/
    │   │   └── stats/boost/        ← boostable init data files
    │   └── text/
    │       └── stats/boost/        ← boostable name/desc text files
    └── script/
        ├── sos-scripting-template.jar
        └── <mod>.jar               ← compiled Java script (apply boost effects)
```

---

## How SoS Boostables Work

### Registration (data files)
Boostables are registered via files in `assets/init/stats/boost/` paired with text in `assets/text/stats/boost/`.

**Init file format:**
```
ICON: <sheet_index>-><sprite_group>-><sprite_index>,
CATEGORY: <CATEGORY_KEY>,
BASE_VALUE: <double>,
```

**Text file format:**
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

### Naming convention for this mod
To produce key `ROOM__SLAVER` (category prefix + room's own internal key `_SLAVER`), the data file must be named `__SLAVER.txt` (double underscore). This pattern is intentional and consistent: `ROOM_` + `_<ROOMKEY>`.

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
**PENDING USER DECISION** — See options below.

---

## Step 3: Effect Target Options

| Option | Effect | Implementation | Status |
|---|---|---|---|
| **A. Worker Efficiency** | Reduces workers required per station below default `ceil(n/4)` | Script adjusts `employees().neededSet()` per instance | Recommended |
| **B. Submission Bonus** | Newly processed slaves receive a `BEHAVIOUR_SUBMISSION` stat boost | Script scans entities each tick, applies to fresh slaves | Viable |
| **C. Processing Speed** | Reduces per-station cycle time | AI timing hardcoded; requires reflection — not cleanly achievable | Not recommended |
| **D. Capacity Multiplier** | Inflates reported station count | May cause inconsistencies | Not recommended |
| **E. NPC Slaver Event Frequency** | Reduces merchant slaver event cooldown | Unrelated to room processing | Out of scope |

---

## Data File Locations

| File | Path |
|---|---|
| Boostable init data | `V70/assets/init/stats/boost/__SLAVER.txt` |
| Boostable text | `V70/assets/text/stats/boost/__SLAVER.txt` |
| Script JAR | `V70/script/<mod>.jar` |
| Script source | `src/<package>/<Script>.java` |

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
| `settlement/room/law/slaver/ROOM_SLAVER.java` | Slaver room blueprint |
| `settlement/room/law/slaver/ExecutionInstance.java` | Per-room-instance state |
| `settlement/room/law/slaver/Constructor.java` | Furnisher/layout definition |
| `settlement/room/law/slaver/SlaverStation.java` | Per-station state machine |
| `settlement/entity/humanoid/ai/work/WorkSlaver.java` | Worker AI |
| `settlement/entity/humanoid/ai/types/prisoner/Enslaved.java` | Prisoner AI |
| `settlement/room/main/ROOMS.java` | All room registrations |
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

## Build System

Maven (`pom.xml` in project root). Compile against `songsofsyx-70.32.jar` and the game's scripting template.

Output JAR goes to `V70/script/`.

---

## Mod Info

`_Info.txt` must declare the mod's name, version, and game version compatibility per SoS modding standards.
