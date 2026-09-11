# STYLE — i3 config + docs (stealth)

## i3 config conventions

### bindsym vs bindcode
- `bindsym $mod+x …` binds by keysym (layout-dependent, readable). Default
  choice.
- `bindcode $mod+40 …` binds by keycode (layout-independent). Used in
  `config` for `i3-dmenu-desktop` (keycode 40 = `d` position on qwerty).
  Prefer `bindsym` unless the binding must survive keyboard-layout changes.

### exec and --no-startup-id
- `exec --no-startup-id <cmd>` on bindings: suppresses the
  startup-notification protocol so the "waiting" cursor doesn't spin for
  apps that don't implement it. Required on all new bindings.
- Whole-command quoting matters: **commas are command separators** in
  i3's binding-command parser and quoting does NOT protect them unless the
  quote wraps the entire command. `exec rofi -modi combi,window,drun` is a
  parse error; `exec "rofi -modi combi,window,drun"` is fine. Note:
  `i3 -C` cannot catch this class of bug — it surfaces at key-press time.
  This exact bug was root-caused and regression-tested in
  `devops/test/i3/lint_test.clj`.
- Mid-word `~` is not expanded by sh (`web:~/.local/bin/x` passes a literal
  tilde); use `$HOME`.
- Comma-separated rofi mode lists additionally prefer the modern
  `-modes`/`-combi-modes` spelling (see the `$mod+space` binding in
  `config` for the current house form).

### mode blocks
- `mode "resize"` / `mode "brightness"` define sticky keymaps entered from
  a binding (`bindsym $mod+r mode "resize"`). Every mode defines
  `Escape`/`Return` → `mode "default"` to exit.

### for_window criteria
- Match narrowly with anchored regexes on both `class` and `title`.
  Unanchored criteria over-match.
- Command chains apply left→right:
  `kill, floating enable, border none`.
- Plasma-session quirks (`plasmashell` floating rules, Desktop-window
  kill, notification `no_focus`) live in `config`, not fragments.
- i3 version limits are worth a comment: e.g. i3 4.23 does not know
  Plasma's on-screen-display window type — say so where the limitation
  bites (see the `no_focus` rule in `config`).

### Fragment layout
- One concern per file under `conf.d/`; subdirs `sys/` (hardware/desktop
  services) and `wm/` (window management).
- Header comment stating intent; when a block is disabled, comment it out
  entirely with a header saying why and what replaced it.
- `set $var …` names live in the fragment that uses them (e.g.
  `$refresh_i3status` in `config`, `$ws1..$ws10` in
  `conf.d/wm/workspaces.conf`).

### Plasma-with-i3 specifics (stealth)
- Session is launched by `~/.config/systemd/user/i3-wm.service`
  ("Launch Plasma with i3"); Plasma owns session startup, i3 owns windows.
- Compositing (picom), KVM sharing (barrier), and wallpaper (ytwall) run
  as systemd **user units** — bind to `systemctl --user` start/stop, do
  not exec the daemons directly.
- Let `dex` handle XDG autostart; don't duplicate autostarts in i3.

## Doc formatting

- Markdown, fenced code blocks with language tags (`sh`, `text`, `i3`).
- Tables for path inventories and repo indexes.
- Link relatively between the doc set (README, AGENTS, PROCESS, STYLE,
  GLOSSARY, GIT_MODULE_INDEX).
- Cross-workspace links (PROCESS → epiphany) must be computed and verified
  relative paths, never guessed.
- State absence explicitly: if a tool or directory is referenced but not
  present, say so rather than writing instructions that will fail.
