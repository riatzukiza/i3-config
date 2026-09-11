# STYLE — i3 config + docs

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
  quote wraps the entire command. `exec rofi -modi "combi,window,drun"` is a
  parse error; `exec "rofi -modi combi,window,drun"` is fine. Note:
  `i3 -C` cannot catch this class of bug — it surfaces at key-press time.
- Mid-word `~` is not expanded by sh (`web:~/.local/bin/x` passes a literal
  tilde); use `$HOME`.

### mode blocks
- `set $mode_go …` names the mode; `mode "$mode_go" { … }` defines its
  keymap; `bindsym $mod+g mode "$mode_go"` enters it. Every mode defines
  `Escape`/`Return` → `mode "default"` exits. Binding commands inside a
  mode that launch things end with `; mode "default"`.

### for_window criteria
- Match narrowly: `[class="^Emacs$" title="^emacs-launcher$"]` — anchored
  regexes on both class and title. Unanchored criteria over-match.
- Command chains apply left→right: `floating enable, move position center,
  resize set W H, border pixel N`.
- Device quirks (Plasma `plasmashell` floating/kill rules) live in
  `config`, not fragments.

### Fragment layout
- One concern per file under `conf.d/`; subdirs `sys/` (hardware/desktop
  services) and `wm/` (window management).
- Header comment stating intent; when a block is disabled, comment it out
  entirely with a header saying why and what replaced it (`rofi-go.conf`
  is the reference example).
- `set $var …` names live in the fragment that uses them (e.g.
  `$mode_go`, `$refresh_i3status`).

## Doc formatting

- Markdown, fenced code blocks with language tags (`sh`, `text`, `i3`).
- Tables for path inventories and repo indexes.
- Link relatively between the doc set (README, AGENTS, PROCESS, STYLE,
  GLOSSARY, GIT_MODULE_INDEX).
- Cross-workspace links (PROCESS → epiphany) must be computed and verified
  relative paths, never guessed.
- State absence explicitly: if a tool or directory is referenced but not
  present, say so rather than writing instructions that will fail.
