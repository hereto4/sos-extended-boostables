---
name: Memory File Location Policy
description: Always load and save project memory files in the project's .claude/memory/ directory, not the user-level auto-memory path
type: feedback
---
For this project, all project-level memory files live in `[project]/.claude/memory/` (the repo-relative directory, e.g. `C:\Users\Nate\AppData\Roaming\songsofsyx\mods\sos-extended-boostables\.claude\memory\`), NOT in the user-level auto-memory path at `~/.claude/projects/<encoded-path>/memory/`.

**Why:** User wants memory files co-located with the project so they can be version-controlled, shared with the project, and discovered alongside other project documentation (SPEC.md, CLAUDE.md, etc.) rather than living in user-level state divorced from the codebase.

**How to apply:**
- **On new sessions**: read `[project]/.claude/memory/MEMORY.md` early and follow its index to load relevant memories. The user-level auto-memory `MEMORY.md` should contain only a redirect note pointing here.
- **When saving new memories**: write the memory file to `[project]/.claude/memory/<name>.md` and add an entry to `[project]/.claude/memory/MEMORY.md`. Do **not** write project memories to the user-level auto-memory directory.
- **When updating existing memories**: edit the file at `[project]/.claude/memory/<name>.md`.
- Treat the user-level auto-memory directory as deprecated for this project — only its `MEMORY.md` redirect note belongs there.
