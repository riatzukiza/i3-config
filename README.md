# i3 config — yoga laptop

i3 window-manager configuration for the **yoga** laptop (i3 4.23). The live
config (`./config`) is assembled at load time from fragments in `conf.d/`
via i3 `include` globs — edit fragments, not the assembly.

## Quick Start

```sh
# 1. Validate before touching the live session (static check; exit 0 = ok)
i3 -C -c ~/.config/i3/config

# 2. Reload the live session
i3-msg reload
# or in i3: $mod+Shift+r (restart in place), Alt+Shift+c (reload)
```

A richer validation kit (config → EDN IR, static lint, Docker sandbox with
synthetic keypress probes) is described by the `i3-devops` skill
(`~/.agents/skills/i3-devops/SKILL.md`) under `~/.config/i3/devops` and
`~/.config/i3/sandbox`. **Note:** those directories are not currently
present in this working tree — see [Status](#status).

## Example: one binding chain

`$mod+g` enters the "Go" launcher mode (conf.d/consult-launcher.conf):

```text
$mod+g  →  mode "$mode_go"
  g     →  exec --no-startup-id emacsclient -n -e '(my/launcher-go)'; mode "default"
```

The emacsclient call opens a floating Emacs Consult/Vertico popup frame,
matched and styled by a `for_window` rule in the same fragment:

```i3
for_window [class="^Emacs$" title="^emacs-launcher$"] \
  floating enable, move position center, resize set 1200 500, border pixel 2
```

## Concepts

- **conf.d assembly** — `config` pulls in every fragment with
  `include "./conf.d/**/*.conf"` + `include "./conf.d/*.conf"`. Fragments
  are the editing surface; there is no build step, i3 assembles at load.
- **`for_window` rules** — match windows by criteria (`class`, `title`) and
  apply commands (floating, position, size, kill). Used for launcher popups
  and Plasma quirks.
- **Launcher modes** — two generations exist:
  - `conf.d/consult-launcher.conf` — **ACTIVE**: Emacs-native launcher;
    bindings exec `emacsclient -n -e '(my/...)'` into Consult popups.
  - `conf.d/rofi-go.conf` — **DISABLED** (fully commented out), a rofi-based
    bridge launcher kept for reference.
- **Mode blocks** — `mode "$name" { ... }` defines a sticky keymap entered
  via `bindsym $mod+... mode "$name"`; escape with `Escape`/`Return` →
  `mode "default"`. Used for the Go launcher and brightness.

## Repository Structure

```text
config                      # entry point: sets, exec lines, bar, for_window, includes
conf.d/                     # fragments (the editing surface)
  consult-launcher.conf     #   ACTIVE Emacs-native launcher ($mod+g)
  rofi-go.conf              #   DISABLED rofi launcher (reference only)
  helm-spotlight.conf       #   Helm popup frames ($mod+Ctrl+f/b/r/p)
  caffeine.conf             #   caffeine toggle
  yt-wallpaper.conf         #   ytwall service + media keys (localhost:3323)
  sys/                      #   audio, backlight, bluetooth, dex, font, nm, touch
  wm/                       #   movement, resize, workspaces, pi-desktop-ops
docs/notes/                 # timestamped process notes (deletions = history)
.agent-shell/               # agent-shell transcripts (untracked artifact)
```

## Development

- **Law: lint before reload.** Run `i3 -C` (or the devops `bb lint` when
  present) before every `i3-msg reload`. Binding commands are parsed at
  key-press time, so `i3 -C` cannot catch everything — when a binding is
  silently dead, consult the i3-devops skill.
- Edit fragments in `conf.d/`, never hand-edit anything you consider
  "generated" — there is no generated assembly; `config` is the include root.
- Commit on branch **device/yoga** (device-specific).

## Status

- Active branch: `device/yoga`; remote: `git@github.com:riatzukiza/i3-config.git`.
- rofi-go mode disabled in favor of the Emacs-native consult launcher.
- `devops/` (bb lint kit) and `sandbox/` (Docker i3 4.23 sandbox) are
  referenced by the i3-devops skill but **absent from the current tree** —
  the skill's commands will fail until they are restored.
- See GIT_MODULE_INDEX.md for repo topology.

## Documentation

- [AGENTS.md](AGENTS.md) — agent contract: invariants, validation, definition of done
- [PROCESS.md](PROCESS.md) — process charter (epiphany lineage)
- [STYLE.md](STYLE.md) — i3 config + doc style conventions
- [GLOSSARY.md](GLOSSARY.md) — domain terms (i3, modes, EDN IR, bb, …)
- [GIT_MODULE_INDEX.md](GIT_MODULE_INDEX.md) — parent/children repo map

## Contributing

Config changes are agent- and human-edited fragments under `conf.d/`. Keep
one concern per fragment, comment intent (see `rofi-go.conf` for the
house style of documenting *why* something is disabled), lint before reload,
and commit on `device/yoga`.

## License

GPL-3.0-or-later. This is a configuration repository distributed as free
software; see the epiphany lineage in PROCESS.md for licensing context.
