---
name: do-not-clobber-info-txt
description: mvn install rewrites the project-root _Info.txt from a pom-templated source every build; never overwrite user edits to it
metadata:
  type: feedback
---

**Rule:** Do not let `mvn install` clobber the user's hand-edits to the project-root `_Info.txt`. If the user has changed NAME / VERSION / DESC / AUTHOR / INFO in that file, those values are authoritative — treat the pom properties as out-of-date until the user says otherwise.

**Why:** Burned the user twice. Most recently I bumped NAME back to `sos-extended-boostables` and VERSION back to `1.0.0` after they had set `Extended Boostables` and `1.1.0`. They had to fix it by hand again.

**Mechanism (the trap):** `pom.xml` has a `copy-mod-info` resources execution that filters `src/main/resources/mod-files/_Info.txt` (a `${...}`-templated file) and writes the rendered output to `${mod.install.directory}`. `mod.install.directory` is `${game.mod.directory}/${mod.name}`, which on this user's disk resolves to **the project root itself** because the repo's folder name happens to equal `mod.name`. So every `mvn install` overwrites the user's project-root `_Info.txt` with values from `pom.xml`:

- `pom.xml:100` → `<mod.version>${project.version}</mod.version>` (drives `VERSION`)
- `pom.xml:101` → `<mod.name>sos-extended-boostables</mod.name>` (drives `NAME`)
- `pom.xml:102` → `<mod.description>` (drives `DESC`)
- `pom.xml:103` → `<mod.author>` (drives `AUTHOR`)
- `pom.xml:104` → `<mod.info>` (drives `INFO`)

**How to apply:**
1. Before running `mvn install`, `Read` the project-root `_Info.txt` AND the pom properties listed above. If any of `NAME`/`VERSION`/`DESC`/`AUTHOR`/`INFO` differ, the user's file is the source of truth — **stop and ask** before building, or sync the pom to match.
2. Never edit `_Info.txt` directly to "fix" it post-build — the next build will undo it. Fix the pom properties (and/or `${project.version}` in `<version>` near the top of pom.xml) so the rendered template matches.
3. Note that `<mod.name>` also drives `mod.install.directory`, `<finalName>`, and the `target/${mod.name}.jar` path referenced by `.run/DEBUG.run.xml`. Changing it isn't free — decoupling the user-facing display name from the install-dir slug would require a new `<mod.display.name>` property and a tweak to the `_Info.txt` template.
