---
name: Songs of Syx game sources location
description: Path to Songs of Syx game source code — both the JAR and a pre-extracted tree usable directly with Read/Grep/Glob
type: reference
originSessionId: 8119bb26-5ef2-447b-8eb9-21884156d9fc
---
The game's Java source is available in the local Maven repository.

**Pre-extracted source tree (preferred — use Read/Grep/Glob directly):**

```
C:/Users/Nate/.m2/repository/com/songsofsyx/songsofsyx/<version>/sources/
```

Top-level packages: `game/`, `init/`, `launcher/`, `menu/`, `script/`, `settlement/`, `snake2d/`, `util/`, `view/`, `world/`. Real `.java` files (not decompiled bytecode), ~2,370 files total.

**Source JAR (fallback if extracted tree is missing):**

```
C:/Users/Nate/.m2/repository/com/songsofsyx/songsofsyx/<version>/songsofsyx-<version>-sources.jar
```

- List entries: `unzip -l <jar>`
- Stream one file: `unzip -p <jar> <internal/path/File.java>` — pipes well into Grep
- Re-extract: `unzip -q -o <jar> -d <same dir>/sources`

**Version:** Check `game.version.major` / `game.version.minor` in `pom.xml` — as of 2026-04-25 it is `70.32`. Re-extract when the game updates.

**Note:** The binary JAR (`songsofsyx-<version>.jar`) sits next to these but is not directly readable — always prefer the source tree or the `-sources.jar`.
