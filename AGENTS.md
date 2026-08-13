# i3 config + devops kit

This repo holds the i3 config (`config`, `conf.d/`) and a babashka-based
devops kit for testing it without risking the live session.

## Kit layout

- `devops/` — bb project: config→EDN IR parser, static lint (rules as data),
  live check, CLI. `cd devops && bb lint|parse|check|test|sandbox ...`
- `sandbox/` — Docker sandbox: ubuntu:24.04 (i3 4.23, host-matched), Xvfb,
  xdotool probes, snapshots, screenshots. Driven via `bb sandbox <cmd>`.

## Before editing config

```
cd devops && bb lint          # static rules (comma/tilde/var/path classes)
bb test                       # parser + rule suite
```

After risky edits: `bb sandbox up && bb sandbox smoke && bb sandbox probe <keys>`.
Live sanity: `bb check`.

Load-bearing facts: commas are i3 command separators in binding commands
unless the WHOLE command is quoted; `~` mid-word is not expanded by sh
(use `$HOME`); `i3 -C`/reload cannot see binding-command parse errors —
they surface at key-press time only. See `~/.agents/skills/i3-devops/SKILL.md`.
