# GLOSSARY — i3 config (yoga)

## Domain terms

- **i3** — tiling window manager for X11. This host runs 4.23.
- **workspace** — i3's virtual desktop; bound here to `$mod+1..9` in
  `conf.d/wm/workspaces.conf`.
- **bindsym** — bind a keysym chord (`$mod+g`) to an i3 command. Contrast
  `bindcode` (keycode-based, layout-independent).
- **for_window** — criteria + command rule applied automatically when a
  window matching the criteria appears (e.g. floating Emacs launcher
  popups).
- **mode** — a named keymap entered from a binding (`mode "$mode_go"`);
  stays active until a binding returns to `mode "default"`. Used for the
  Go launcher and brightness control.
- **exec --no-startup-id** — run a command without startup-notification,
  avoiding the spinning launch cursor on non-conforming apps.
- **i3-msg** — send IPC commands to the running i3 (`i3-msg reload`).
- **i3-resurrect** — save/restore workspace layouts (`$mod+Ctrl+s` /
  `$mod+Ctrl+r` in `conf.d/wm/pi-desktop-ops.conf`).
- **rofi** — dmenu-style launcher. Its bridge-launcher mode (`rofi-go.conf`)
  is disabled here in favor of the Emacs-native launcher; plain rofi
  (`-show combi`, playlists) is still active on `$mod+space` / `$mod+m`.
- **EDN IR** — intermediate representation: the devops toolkit (referenced
  by the i3-devops skill) parses the include tree into EDN data structures
  so lint rules and tests can operate on the config as data instead of on
  the live WM. (Toolkit directories are currently absent from this tree.)

## Named things referenced

- **eta-mu** — referenced only indirectly: the Π-tagged fork-tax commits and
  `.ημ/` state files in this repo's history come from the eta-mu/π
  workflow tooling. No active runtime dependency. Stated explicitly
  because it appears in git history and receipts.
- No other named external projects are referenced beyond the glossary's
  domain terms.

## Clojure / tooling

- **bb (babashka)** — a fast, native Clojure scripting runtime. The devops
  kit's commands (`bb lint`, `bb parse`, `bb check`, `bb sandbox …`) are
  babashka scripts: Clojure source run directly, no JVM startup.
- **EDN** — Extensible Data Notation: the Clojure-world data format (like
  JSON but with keywords and richer scalars) used for the config IR and
  receipts. Plain language: "structured text data the linter reads."

These are defined here because the i3-devops skill (loaded when debugging
this config) assumes them; the kit itself is currently absent — stated
explicitly per AGENTS.md.
