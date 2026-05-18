---
name: Conform New Features to Existing Project Patterns
description: When adding a new feature to a mod project, follow the conventions of features already in that project rather than introducing parallel ones
type: feedback
---
When extending a mod project that already implements a similar feature, conform to that feature's existing patterns rather than introducing new ones for the new feature.

**Why:** In this session the user added `ROOM__CANNIBAL` alongside an already-implemented `ROOM__SLAVER`. The first pass diverged from SLAVER's style — used Lombok `@NoArgsConstructor`, a separate `InstanceScript.java`, single-underscore key `ROOM_CANNIBAL`, `snake2d.LOG` instead of `System.err`, and data-file-only registration with no programmatic fallback. After the user fetched the SLAVER work from the remote, they explicitly asked to "conform to ROOM_SLAVER's implementation where needed" and merge both into one jar.

**How to apply (for any new feature in sos-extended-boostables, and the same principle for similar mod projects):**
- **Key/file naming**: room internal key `_X` → boost key `ROOM__X` (double underscore). Push key passed to `BOOSTING.push` is `__X` (the prefix strip is one `_`). Asset filenames mirror the push key: `assets/init/stats/boost/__X.txt`.
- **Registration**: `tryGet → BOOSTING.push` fallback so the key registers whether or not a data file is shipped.
- **Lifecycle**: register effects and patch state in `initBeforeGameInited()` after `RACES.expand()`; inline `SCRIPT_INSTANCE` as an anonymous class in `createInstance()` rather than splitting into a separate file.
- **Style**: explicit `public Foo() {}` constructors (no Lombok); `System.err.println` / `System.out.println` for log lines; static nested final classes for value-type helpers (e.g. `BoostedResAmount`).
- **Single jar**: every effect lives in one `MainScript` so the mod ships as one jar — don't split classes across files unless there's a real reason.

**How to apply more generally:** When a project already has a worked example of the kind of code you're about to write (same domain, same lifecycle hooks, same registration system), read it first and use its conventions as the spec. Surfaces like naming, registration, and file layout almost always need to match; the implementation-specific logic is where divergence is fine.
