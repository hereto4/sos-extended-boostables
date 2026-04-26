---
name: Songs of Syx settlement/ — SETT, rooms, entities, jobs, tilemap
description: Settlement-layer architecture (989 files); how to register a new room blueprint, entity type, or job; tile/path/stats APIs
type: reference
originSessionId: 4e70a062-1427-421c-9733-c0f85b14eb41
---
Source: `settlement/` (989 files — by far the largest pkg). Subdirs: `battle/`, `entity/`, `entry/`, `environment/`, `job/`, `main/`, `maintenance/`, `misc/`, `overlay/`, `path/`, `room/`, `stats/`, `thing/`, `tilemap/`, `weather/`.

## SETT — the settlement hub (settlement.main)

Constructed during game init. All accessors are public static methods (verified against v70.32 SETT.java).

**Static accessors**:
```java
// Entities
SETT.ENTITIES()      → ENTETIES        (master entity handler)
SETT.HUMANOIDS()     → Humanoids       (humanoid factory)
SETT.ANIMALS()       → Animals         (animal factory/lifecycle)
SETT.HALFENTS()      → HalfEnts
SETT.PROJS()         → SProjectiles    (arrows/projectiles)

// Tile data
SETT.TILE_MAP()      → TileMap         (composite of layers below)
SETT.TERRAIN()       → Terrain         (collision/walkability)
SETT.GROUND()        → Ground          (sand/dirt/grass/water/stone…)
SETT.FLOOR()         → Floors          (built floor layers)
SETT.GRASS()         → Grass           (vegetation/crops)
SETT.MAPS()          → SettMaps
SETT.MINERALS()      → Minables

// Rooms / jobs / pathing
SETT.ROOMS()         → ROOMS
SETT.JOBS()          → JOBS
SETT.PATH()          → PATHING
SETT.PLACA()         → SettPlacability (obstruction lookup)
SETT.PLACERS()       → ComplexPlacers

// Visual / overlay
SETT.MINIMAP()       → Minimap
SETT.OVERLAY()       → SettOverlay
SETT.PARTICLES()     → ParticleRenderer
SETT.LIGHTS()        → POINTLIGHTS

// Misc
SETT.THINGS()        → THINGS          (corpses/blood/objects)
SETT.MAINTENANCE()   → MAINTENANCE     (room wear/decay)
SETT.ENV()           → ENVIRONMENT     (placed monuments/decor)
SETT.WEATHER()       → SWEATHER
SETT.WORLD_AREA()    → CapitolArea     (back-link to world region)
SETT.FACTION()       → Faction         (settlement's owning faction)
SETT.CITY()          → SETT            (self — convenience)
SETT.BATTLE()        → SBattle         (invasion view)
SETT.INVADOR()       → Invador
SETT.ENTRY()         → SENTRY          (immigration/spawning)

// Bounds & utilities (public static finals + helpers)
SETT.TWIDTH / THEIGHT / PWIDTH / PHEIGHT / TAREA   // tile + pixel dims
SETT.TILE_BOUNDS / TILE_BOUNDS_I / PIXEL_BOUNDS    // RECTANGLEs
SETT.GRID                                          // SettlementGrid
SETT.IN_BOUNDS(int tx, int ty)                     // bounds check
SETT.IN_BOUNDS(COORDINATE c) / (c, DIR d) / (tx, ty, DIR d)
SETT.PIXEL_IN_BOUNDS(int x, int y)
SETT.tileRan(int tx, int ty)                       // deterministic per-tile rand
SETT.reGenerate()
```

`SettResource` (line 729) is the abstract per-subsystem base — analogous to `GameResource`. Subsystem lifecycle: `save/load/update(ds, Profiler)/clear/init`.

(Note: the doc/README's test list mentions `SETT.STATS()`, `SETT.ARMIES()`, `SETT.ARMY_AI()`, `SETT.BORDERS()`, `SETT.FERTILITY()` — these are NOT in v70.32; likely v71+. Don't reach for them when targeting V70.)

`SettResource` is the per-subsystem base (analogous to `GameResource` at the world layer). Lifecycle: `save/load/update(ds, Profiler)/clear/init`.

## Rooms (settlement/room/) — the major moddable surface

Two-layer model:

**`Room` (abstract)** — instance on the map. One Room per occupied tile cluster.
```java
abstract boolean render(Renderer r, ShadowBatch shadowBatch, RenderData.RenderIterator i);
abstract TmpArea remove(int tx, int ty, boolean scatter, Object user, boolean forced);
abstract AVAILABILITY getAvailability(int tile);
abstract CharSequence name(int tx, int ty);
abstract SPRITE icon();
```

**`RoomBlueprint` (abstract)** — type definition (registry entry). Modders extend this.
```java
abstract SFinderFindable service(int tx, int ty);   // service this type provides
abstract COLOR miniC(int tx, int ty);               // minimap color
RoomEmploymentSimple employment();                  // optional jobs
```
Registry: static `ArrayListGrower<RoomBlueprint> RoomBlueprint.ALL` (index = registration order). Constructor `new RoomBlueprint("key")` auto-adds.

**JSON ↔ code binding** via `RoomsCreator<T>`:
```java
abstract T create(String key, RoomInitData data, RoomCategorySub cat, int index) throws IOException;
```
File-name prefix selects the creator (e.g., `FARM_barley.txt`/`FARM_wheat.txt` go to `RoomsCreator<ROOM_FARM>` and become `ROOMS.FARMS` list entries).

**`ROOMS` master registry**:
- Singleton rooms: `THRONE`, `HOME`, `STOCKPILE`, `PRISON`, ... (and `MONUMENT` per recent commits — see git log).
- Multi-instance lists: `BARRACKS`, `ARCHERIES`, `GATES`, `FARMS`, `MINES`, `FISHERIES`, `WORKSHOPS`, `REFINERS`, etc.
- Internal: `RoomsMap map` (tile → Room), `MapRoomData data` (per-room metadata), `PLACEMENT` (placement tool), `CONSTRUCTION` (build/destroy), `DELETE`, `CATS` (categories).

**To add a new room type**: subclass `RoomBlueprint`, override `service()`/`miniC()` (and `employment()` if jobs needed); supply JSON files in `assets/init/settlement/room/<TYPE>/<variant>.txt` with the matching key prefix; the `RoomsCreator` for that type loads them automatically. See `doc/README.md` § "Adding your own custom room" for the JSON schema, and the recent commits for a list of room "MONUMENT" anchors and TYPE properties.

## Entities (settlement/entity/)

**`ENTITY`** — abstract base.
```java
EPHYSICS.Solid physics;
ESpeed.Imp speed;
abstract boolean update(double ds);          // false = remove
collide(ECollision coll);
collideTile(...);
willCollideWith(ENTITY other);
renderSimple(...) / render(...);
removeAction();
add(boolean collide);                         // register with ENTETIES
```

**Subclasses**:
- `Humanoid` — population units. `Induvidual induvidual` (stats/type/race), `AIManager ai` (behavior), health/hunger/happiness/traits/skills. `kill(boolean corpse, CAUSE_LEAVE cause)`.
- `Animal` — wildlife/livestock. `AnimalSpecies spec`, `state`, `domesticated`, `PastureInstance`. Lifecycle via `Animals` factory + `AnimalSpawnSpot`.

**Registries**:
- `Humanoids` — factory: create by Race × HTYPE (`SUBJECT`, `PRISONER`, `SLAVE`, `CHILD`, ...).
- `Animals` — manages spawning/aging.
- `ENTETIES` — master handler. `add(ENTITY e, boolean collide)`, `remove(ENTITY e)`, `getAtTile(tx, ty) → Iterable<ENTITY>`. Spatial lookup via `Grid`. `update()` runs physics + collisions + dead-removal.

## Jobs (settlement/job/)

**`Job`** — abstract task. State machine: NOTHING → RESERVABLE → RESERVED → assigned. Static `ArrayList<Job> all` registry (max 127 — byte index).
```java
boolean get(int tx, int ty);          // can do here?
void cancel(int tx, int ty);
jobReserve(RESOURCE r) / jobReservedIs(RESOURCE r);
```

**Subtypes**: `JobBuild` (`JobBuildStructure`, `JobBuildRoad`, `JobBuildFence`, `JobBuildFort`), `JobClear` (terrain), `JobRoom` (room-bound work).

**`JOBS extends SettResource`** — `byte[] map` (tile → Job index), `StateManager state`, `progress` bitmap. Drives humanoid AI assignment via path + priority. `get(tx, ty) → Job`, `remove(tx, ty)`.

## Pathing (settlement/path/)

**`PATHING extends SettResource`**:
- `SFINDERS finders` — A*/BFS variants
- `SCOMPONENTS comps` — connectivity graph (walkable areas)
- `AvailabilityMap availability` — per-tile cost
- `FinderThread thread` — background pathfinding
- `CostMethods coster` — pluggable cost computation

API: `finders().find(from, to) → Path`, `availability.get(tx, ty) → double cost`.

## TileMap (settlement/tilemap/)

`TileMap extends SettResource`. Layers:
- `topology` — collision/walkability
- `ground` — base type (sand/dirt/grass/water/stone/ice…)
- `floors` — built floors (wood/stone/tile/gravel)
- `grass` — vegetation/crops
- `snow` — seasonal ice
- `growth` — `TGrowth` per-crop/tree state

Examples: `SETT.TILE_MAP().ground.get(tx, ty)`, `SETT.TILE_MAP().floors.set(tx, ty, floor)`, `SETT.TERRAIN().isWalkable(tx, ty)`.

## Stats (settlement/stats/)

`STATS extends SettResource`, aggregates per-individual into globals:
- `StatsBattle`, `StatsEnv` (pollution/temp), `StatsPopulation` (by race/class/status), `StatsFood`, `StatsWork`, `StatsHome`, `StatsNeeds` (hunger/happy/health), `StatsLaw`.
- `StatMultiplier` — modifier system (boons/curses).

Per-individual stats live on `Induvidual` (held by `Humanoid`). Globals: `STATS.get(STAT s, Race r, HTYPE t) → double`, `STATS.add(Humanoid h, STAT s, double v)`. Add custom stats via `StatsInit.Addable`.

`settlement/stats/standing/STANDINGS.java` — separate faction-reputation system.

## Other subdirs (one-liners)

- `battle/` — `SBattle`, `invasion/`: military combat in-settlement (divisions, banner rendering).
- `entry/` — `SENTRY`, `PeopleSpawner`, `Immigration`: arrivals from world map / immigration waves.
- `environment/` — `ENVIRONMENT`, `SettEnvShape`: monuments, water features, decoration (non-room placeables).
- `maintenance/` — `MAINTENANCE`, `ROOM_DEGRADER`: room wear/decay over time, repair jobs.
- `misc/` — `ParticleRenderer`, `SettPlacability`, `placers/`.
- `overlay/` — `SettOverlay`, `Homeless`, `RoomProblem`: UI overlays for problems (homelessness, idle workers, broken rooms).
- `thing/` — `THINGS`, `Cadavers`, `HalfEnts`, `SProjectiles`: corpses, blood, arrows — physical objects, not entities.
- `weather/` — `SWEATHER`, `Rain`, `Snow`, `WeatherGrowth`: weather state, growth modifiers, ambient sound.

## Modder checklist

1. **New room**: subclass `RoomBlueprint`; ship JSON variants under `assets/init/settlement/room/<TYPE>/...`; let `RoomsCreator` instantiate.
2. **New entity**: subclass `ENTITY` (or `Humanoid`/`Animal`); register via `Humanoids`/`Animals` factory.
3. **New job**: subclass `Job` with no-arg public constructor (auto-registers in `Job.all`); implement `get(tx, ty)` / `cancel(tx, ty)`.
4. **New stat**: implement `StatsInit.Addable`; hook into `Induvidual`/global aggregation.
5. **Custom pathfinding cost**: extend `CostMethods` and feed into `SFINDERS`.
