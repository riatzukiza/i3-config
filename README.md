# i3 config — stealth laptop

i3 window-manager configuration for the **Stealth 16** laptop (i3 4.23),
launched **from inside Plasma** via the `i3-wm.service` systemd user unit
("Launch Plasma with i3", `Before=plasma-workspace.target`,
`WantedBy=plasma-workspace.target`). Plasma supplies the session shell and
services; i3 manages windows. The live config (`./config`) is assembled at
load time from fragments in `conf.d/` via i3 `include` globs — edit
fragments, not the assembly.

This is a different WM integration pattern than a bare-i3 session: do not
add i3 `exec` lines for things Plasma already autostarts (applets, xdg
autostart via `dex`), and expect plasmashell windows in the window tree
(see the `for_window` quirk rules in `config`).

## Quick Start

```sh
# 1. Validate before touching the live session (static check; exit 0 = ok)
i3 -C -c ~/.config/i3/config

# 2. Run the static lint kit (catches bugs i3 -C cannot see)
cd ~/.config/i3/devops && bb lint && bb test

# 3. Reload the live session
i3-msg reload
# or in i3: Alt+Shift+c (reload), $mod+Shift+r (restart in place)
```

For risky edits, exercise them headless first in the Docker sandbox
(`bb sandbox up && bb sandbox smoke && bb sandbox probe <keys>`) — binding
commands parse at key-press time, so only synthetic keypress probes detect
silently-dead bindings. See `~/.agents/skills/i3-devops/SKILL.md`.

## Example: a whole-command quoting bug (why the kit exists)

`$mod+space` runs a rofi combi launcher whose mode list contains commas.
Commas are i3 command separators in binding commands, so the mode list
must live inside a quote that wraps the entire command:

```i3
bindsym $mod+space exec --no-startup-id "rofi -show combi -modes combi,drun,window,run,web:$HOME/.local/bin/rofi-web-search -combi-modes drun,window,run"
```

`i3 -C`/reload accept a mis-quoted version of this line without complaint;
the failure only appears when the key is pressed. This exact bug is
reproduced as a regression test in `devops/test/i3/lint_test.clj`.

## Concepts

- **Plasma-with-i3 session** — Plasma workspace target starts i3 instead of
  KWin. Plasma services (notifications, OSD, applets) coexist with i3
  tiling; `for_window` rules in `config` float `plasmashell`, kill
  Plasma's "Desktop" window, and suppress notification focus.
- **conf.d assembly** — `config` pulls in every fragment with
  `include "./conf.d/**/*.conf"` + `include "./conf.d/*.conf"`. Fragments
  are the editing surface; there is no build step, i3 assembles at load.
- **Launcher generation (rofi, active)** — this host uses rofi as its
  launcher surface: combi spotlight (`$mod+space`), web search with
  Firefox history (`$mod+slash`), playlists (`$mod+m`), and i3 session
  save/restore (`$mod+Ctrl+p`). Emacs is the terminal/editor surface
  (`~/bin/emvterm` opens an emacsclient vterm frame on
  `$mod+Shift+Return`), not a launcher popup.
- **Mode blocks** — `mode "resize"` / `mode "brightness"` define sticky
  keymaps; escape with `Escape`/`Return` → `mode "default"`.
- **systemd --user services** — picom, barrier, and ytwall are user units
  toggled/started from bindings and `exec_always` lines rather than
  spawned directly by i3.

## Repository Structure

```text
config                      # entry point: mod key, exec lines, bar, gaps,
                            #   for_window Plasma quirks, rofi bindings, includes
conf.d/                     # fragments (the editing surface)
  caffeine.conf             #   caffeine toggle ($mod+Shift+c)
  yt-wallpaper.conf         #   ytwall service + media keys (localhost:3323)
  sys/                      #   audio, backlight, bluetooth, dex, font, nm, touch
  wm/                       #   movement, resize, workspaces, pi-desktop-ops
devops/                     # bb project: config→EDN IR parser, static lint, CLI
sandbox/                    # Docker sandbox: i3 4.23 + Xvfb + xdotool probes
docs/inbox/                 # timestamped process notes (deletions = history)
receipts.edn                # receipt-river ledger (tracked here)
scripts/                    # (empty scratch dir)
```

## Development

- **Law: lint before reload.** `i3 -C` is the floor; the devops `bb lint`
  is the real gate (it catches comma-in-quotes and mid-word-`~` bugs in
  exec strings that `i3 -C` cannot see). Then `i3-msg reload`.
- Edit fragments in `conf.d/`; `config` holds include-level, bar,
  Plasma-quirk, and rofi-binding concerns.
- Commit on branch **device/stealth** (device-specific).

## Status

- Active branch: `device/stealth`; remote: `git@github.com:riatzukiza/i3-config.git`.
- Unlike the yoga sibling tree, `devops/` and `sandbox/` **are present**
  in this working tree and tracked.
- Plasma session: `i3-wm.service` user unit replaces KWin; picom, barrier,
  ytwall run as systemd user units.
- See GIT_MODULE_INDEX.md for repo topology.

## Documentation

- [AGENTS.md](AGENTS.md) — agent contract: invariants, validation, definition of done
- [PROCESS.md](PROCESS.md) — process charter (epiphany lineage)
- [STYLE.md](STYLE.md) — i3 config + doc style conventions
- [GLOSSARY.md](GLOSSARY.md) — domain terms (i3, plasma, picom, synergy/barrier, bb, …)
- [GIT_MODULE_INDEX.md](GIT_MODULE_INDEX.md) — parent/children repo map

## Contributing

Config changes are agent- and human-edited fragments under `conf.d/`. Keep
one concern per fragment, comment intent, lint before reload, and commit
on `device/stealth`.

## License

GPL-3.0-or-later. This is a configuration repository distributed as free
software; see the epiphany lineage in PROCESS.md for licensing context.
