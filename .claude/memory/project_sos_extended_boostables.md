---
name: sos-extended-boostables Project State
description: Current state and key decisions for the sos-extended-boostables mod project
type: project
originSessionId: f4a7dc19-9fa3-4a12-9315-2717447ae117
---
Adds new ROOM_*-style boostable keys to Songs of Syx v70.32 that don't exist in vanilla. Both effects ship in the same jar (`your.mod.MainScript`).

**Why:** Boostable keys produced by this mod are consumed by tech files (handled externally). This mod registers the keys and applies the effects at runtime.

**Key naming convention (followed strictly):** room internal key `_X` → boost key `ROOM__X` (double underscore). `BOOSTING.push()` strips ONE leading `_`, then prepends the category prefix, so push key `__X` → strip → `_X` → prepend `ROOM_` → `ROOM__X`.

**Registration pattern (both keys):** `BOOSTING.MAP().tryGet(fullKey)` first; if null, fall back to `BOOSTING.push(pushKey, 1.0, name, desc, icon, BOOSTABLES.ROOMS())`. So registration works whether or not a data file is present.

**Implemented boostables:**

1. `ROOM__SLAVER` — registered programmatically (no data file). Effect attaches a `BoosterValue` to `BOOSTABLES.BEHAVIOUR().SUBMISSION` via a full `BValue`:
   - `vGet(Induvidual)` returns `roomSlaver.get(player) - baseValue` only for `HTYPES.SLAVE()` whose `arrive == CAUSE_ARRIVES.PAROLE()` (i.e., produced by the Slaver room; vanilla sets PAROLE on slaver-room converts in `Enslaved.java:82`; trade slaves get `IMMIGRATED`).
   - `vGet(Player)` / `vGet(PopTime)` returns `delta * processedRatio` for uprising risk.
   - `processedRatio = parole_slaves / total_slaves` recomputed every 4s in `SCRIPT_INSTANCE.update` and immediately in `load`.

2. `ROOM__CANNIBAL` — registered both via data files (`V70/assets/{init,text}/stats/boost/__CANNIBAL.txt`) and the same programmatic fallback. Effect multiplies butcher yields at Cannibal Rooms by replacing each `Race.resources` `RES_AMOUNT.Imp` entry with a `BoostedResAmount` wrapper whose `amount()` returns `round(baseAmount * boostable.get(FACTIONS.player()))`.
   - Why this is scoped to butchering: `Race.resources()` is read by vanilla ONLY in `WorkCannibal.butcher2.produce` (for amounts) and `ROOM_CANNIBAL.resources()` (for resource-set discovery — reads only `.resource()`, not `.amount()`).
   - Patching uses `snake2d.util.sets.ArrayList.replace(int, E)` — no reflection required.
   - Patching runs in `initBeforeGameInited()` (after `RACES.expand()`).

**Implementation rules (followed in MainScript):**
- No Lombok. Explicit `public MainScript() {}`.
- `SCRIPT_INSTANCE` inlined as anonymous class in `createInstance()` — no separate `InstanceScript.java`.
- Errors via `System.err.println`, informational via `System.out.println`.
- `BoostedResAmount` is a static nested final class implementing `RES_AMOUNT, Serializable`.

**Build/install:**
- Maven outputs `target/out/sos-extended-boostables/` with full V70/ structure.
- Maven not on PATH; invoke via `"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" -f .../pom.xml package`.
- pom.xml: `mod.name=sos-extended-boostables`, `mod.author=Nate`, `mod.info="Adds ROOM__SLAVER and ROOM__CANNIBAL boostables."`. The `<artifactId>` remains `sos-scripting-template` (template-default, harmless).

**SPEC.md** at project root remains the SLAVER-focused design doc; it is not the authoritative implementation reference anymore — read `MainScript.java` for current behavior.
