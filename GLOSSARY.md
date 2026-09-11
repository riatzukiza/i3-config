# GLOSSARY — i3 config (stealth)

## Domain terms

- **i3** — tiling window manager for X11. This host runs 4.23, launched
  from inside Plasma (see below), not as a bare-i3 session.
- **Plasma-with-i3** — this device's session pattern: Plasma workspace
  services with i3 replacing KWin as the window manager, wired via the
  `i3-wm.service` systemd user unit ("Launch Plasma with i3",
  `WantedBy=plasma-workspace.target`).
- **plasmashell** — Plasma's shell process; its windows (desktop, panels,
  notifications) appear in i3's tree, hence the floating/kill/`no_focus`
  quirk rules in `config`.
- **workspace** — i3's virtual desktop; bound to `$mod+1..0` in
  `conf.d/wm/workspaces.conf`.
- **bindsym** — bind a keysym chord (`$mod+g`) to an i3 command. Contrast
  `bindcode` (keycode-based, layout-independent).
- **for_window** — criteria + command rule applied automatically when a
  window matching the criteria appears (Plasma quirks).
- **mode** — a named keymap entered from a binding (`mode "resize"`);
  stays active until a binding returns to `mode "default"`. Used for
  resize and brightness control.
- **exec --no-startup-id** — run a command without startup-notification,
  avoiding the spinning launch cursor on non-conforming apps.
- **i3-msg** — send IPC commands to the running i3 (`i3-msg reload`).
- **i3-resurrect** — save/restore workspace layouts (`$mod+Ctrl+s` /
  `$mod+Ctrl+r` in `conf.d/wm/pi-desktop-ops.conf`).
- **rofi** — dmenu-style launcher; the primary launcher surface on this
  host: combi spotlight (`$mod+space`), web search (`$mod+slash`),
  playlists (`$mod+m`), i3 sessions (`$mod+Ctrl+p`).
- **combi mode** — rofi's merged view across `drun`, `window`, `run`, and
  custom `web` script modes; configured with `-modes`/`-combi-modes`.
- **picom** — X11 compositor, running as the `picom.service` systemd user
  unit; toggled with `$mod+Ctrl+Shift+p`.
- **barrier** — software KVM (server side here) as `barrier.service`;
  toggled with `$mod+Ctrl+Shift+b`. This is what the "synergy-style KVM"
  role refers to on this host (Barrier is the Synergy fork actually
  installed).
- **ytwall** — YouTube wallpaper + overlay service as
  `ytwall.service`, HTTP API on `localhost:3323`; media controls in
  `conf.d/yt-wallpaper.conf` (`$mod+Shift+brackets` next/prev,
  `$mod+Shift+p` pause, `$mod+Shift+,`/`.` volume).
- **caffeine** — screen-idle suppression toggle script
  (`~/.local/bin/caffeine-toggle.sh`, `$mod+Shift+c`).
- **emvterm** — the emacs gate wrapper on this host:
  `~/bin/emvterm` → `emacsclient -cn --eval '(vterm t)'`; bound to
  `$mod+Shift+Return` as the terminal entry point. (No
  consult-launcher/launcher-popup generation exists on stealth — the
  launcher surface is rofi.)
- **EDN IR** — intermediate representation: the devops kit parses the
  include tree into EDN data structures so lint rules and tests operate
  on the config as data instead of on the live WM.

## Named things referenced

- **eta-mu** — the Π-tagged fork-tax commits, `receipts.edn` entries, and
  `.ημ/` state in this repo's history come from the eta-mu/π workflow
  tooling. No active runtime dependency on i3's part.
- **i3-devops skill** (`~/.agents/skills/i3-devops/SKILL.md`) — documents
  the lint/sandbox kit; present here.
- **dex** — XDG autostart runner (`conf.d/sys/dex.conf`) under the
  Plasma session.

## Clojure / tooling

- **bb (babashka)** — a fast, native Clojure scripting runtime. The
  devops kit (`devops/`, **present and tracked on this host**) runs as
  `bb lint|parse|check|test|sandbox`: Clojure source executed directly,
  no JVM startup.
- **EDN** — Extensible Data Notation: the Clojure-world data format
  (like JSON but with keywords and richer scalars) used for the config
  IR and receipts. Plain language: "structured text data the linter
  reads."
- **Docker sandbox** (`sandbox/`) — ubuntu:24.04 container with i3 4.23
  (host-version-matched), Xvfb, xdotool probes; driven via
  `bb sandbox <cmd>`.

These are defined here because the i3-devops skill (loaded when
debugging this config) assumes them; unlike the yoga sibling tree, the
kit is present on stealth.
