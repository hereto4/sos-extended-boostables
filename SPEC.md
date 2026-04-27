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

## MainScript.java Implementation

```java
package your.mod;

import game.boosting.*;
import game.boosting.BValue.PopTime;
import game.battle.div.Div;
import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import game.faction.player.Player;
import init.sprite.UI.UI;
import init.type.CAUSE_ARRIVES;
import init.type.HTYPES;
import lombok.NoArgsConstructor;
import script.SCRIPT;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import world.map.regions.Region;

@NoArgsConstructor
@SuppressWarnings("unused")
public final class MainScript implements SCRIPT {

    private static final String BOOSTABLE_KEY = "ROOM__SLAVER";
    private double processedRatio = 0.0;

    @Override public CharSequence name() { return "sos-extended-boostables"; }
    @Override public CharSequence desc() { return "Adds extended boostable support for the Slaver room."; }
    @Override public boolean forceInit() { return true; }

    @Override
    public void initBeforeGameInited() {
        Boostable roomSlaver = BOOSTING.MAP().tryGet(BOOSTABLE_KEY);
        if (roomSlaver == null) {
            System.err.println("[sos-extended-boostables] Could not find boostable: " + BOOSTABLE_KEY);
            return;
        }

        Boostable submission = BOOSTABLES.BEHAVIOUR().SUBMISSION;
        BSourceInfo info = new BSourceInfo("Slaver Training", UI.icons().s.slave);

        BValue bv = new BValue() {
            @Override
            public double vGet(Induvidual indu) {
                if (indu.hType() != HTYPES.SLAVE()) return 0;
                if (STATS.POP().COUNT.arrive.get(indu) == CAUSE_ARRIVES.PAROLE())
                    return roomSlaver.get(FACTIONS.player()) - roomSlaver.baseValue;
                return 0;
            }
            @Override
            public double vGet(Player f) {
                return (roomSlaver.get(f) - roomSlaver.baseValue) * processedRatio;
            }
            @Override
            public double vGet(PopTime t) {
                return (roomSlaver.get(FACTIONS.player()) - roomSlaver.baseValue) * processedRatio;
            }
            @Override public double vGet(FactionNPC f) { return 0; }
            @Override public double vGet(Region reg) { return 0; }
            @Override public double vGet(Div div) { return 0; }
        };

        new BoosterValue(bv, info, 2.0, false).add(submission);
    }

    @Override
    public SCRIPT_INSTANCE createInstance() {
        return new SCRIPT_INSTANCE() {
            private double timer = 0;

            @Override
            public void update(double ds) {
                timer -= ds;
                if (timer > 0) return;
                timer = 4.0;
                recomputeRatio();
            }

            @Override public void save(FilePutter file) {}

            @Override
            public void load(FileGetter file) throws Exception {
                recomputeRatio();
            }
        };
    }

    private void recomputeRatio() {
        int total = 0;
        int processed = 0;
        for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
            if (!(e instanceof Humanoid)) continue;
            Humanoid h = (Humanoid) e;
            if (h.indu().hType() != HTYPES.SLAVE()) continue;
            total++;
            if (STATS.POP().COUNT.arrive.get(h.indu()) == CAUSE_ARRIVES.PAROLE())
                processed++;
        }
        processedRatio = total == 0 ? 0.0 : (double) processed / total;
    }
}
```

---

## Build System

Maven (`pom.xml` in project root). Compile against `songsofsyx-70.32.jar` and the game's scripting template.

Output JAR goes to `V70/script/`.

---

## Mod Info

`_Info.txt` must declare the mod's name, version, and game version compatibility per SoS modding standards.
