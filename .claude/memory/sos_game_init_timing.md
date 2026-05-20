---
name: SoS GAME constructor init timing
description: Exact order of subsystem construction in GAME ctor and where SCRIPT lifecycle hooks fire — critical for timing-sensitive engine overrides
metadata:
  type: reference
---

`GAME` constructor sequence (verified via bytecode disassembly of v0.70.33's `SongsOfSyx.jar`; matches v0.70.32 source in `info/SongsOfSyx-sources.jar`):

```
GAME ctor:
  L118:  script = new ScriptEngine(spec.scripts)
           ├─ ScriptEngine ctor:
           │    ├─ ScriptLoad.getAll() returns previously-loaded scripts
           │    ├─ for each load with forceInit()==true OR matched: instantiate
           │    └─ init.initBeforeGameCreated()  ← runs every script's hook
  L123:  INIT init = new INIT()
           └─ new TECHS()  ← reads /init/tech/*.txt, calls TECH.checkUnused() per node
  L125:  new SPRITES(this)
  ...
  L167:  factions = new FACTIONS()
           └─ new Player() → new PTech()  ← registers PTech's BOOSTING.connecter
  L172:  script.init.initBeforeGameInited()  ← runs every script's hook
  L177:  init.finish()
           └─ BOOSTING.finishSetup()  ← FIRES all connecters in registration order, then CLEARS list
  L181:  for (ACTION a : onGameInited) a.exe()
  L184:  view = new VIEW(GAME.this)
  L188:  script.init(null)  ← calls createInstance() on each script
```

**Why this matters for our mod:**

- **`initBeforeGameCreated` fires BEFORE INIT reads tech files.** That's why our `scanTechFiles()` can pre-read `/init/tech/*.txt` using the merged `PATHS.ResFolder("tech", false)` and identify AI_BLOCKED nodes before the engine parses them. PATHS is already initialized at this point (from `MainProcess.PATHS.init()` at app launch).

- **`initBeforeGameInited` fires AFTER PTech registers its connecter (L167) but BEFORE finishSetup (L177).** This is the exact window where we register our own connecter — it lands in the LinkedList<ACTION> after PTech's, so it fires after PTech's during finishSetup. The list is cleared right after, so any registration done in `createInstance()` (L188) is too late.

- **`createInstance()` is too late for connecter registration** but fine for runtime hooks (update/render/save/load).

**Connecters fire in registration order** because `BOOSTING.connecters` is a `LinkedList<ACTION>` (`BOOSTING.java:20`). Last-registered runs last → our override always lands after PTech's.

**One-shot semantics:** `BOOSTING.finishSetup()` clears the connecters list after firing. So any override of state populated by connecters (like `PTech.npcAmount`) sticks for the rest of the session unless something explicitly resets it. Nothing in the engine does.

**Bytecode verification recipe** when GAME constructor order matters:
```
jar xf "SongsOfSyx.jar" game/GAME.class
javap -c -p game.GAME | grep -E "new           #[0-9]+\s+// class (script/ScriptEngine|init/INIT|game/faction/FACTIONS)"
```
Compares constructor invocation offsets to verify the source matches the installed binary.
