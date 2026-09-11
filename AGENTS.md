# AGENTS.md — i3 config (yoga)

## Role

You are working on the i3 window-manager configuration for the **yoga**
laptop (i3 4.23). The live config is `./config`, assembled from fragments
in `conf.d/` via `include` globs. You may be a human or an agent; the rules
are the same.

## Invariants

1. **Config must lint before live reload.** Run a static check (`i3 -C -c
   ~/.config/i3/config`, exit 0) before any `i3-msg reload` touches the
   session. Binding commands are parsed at key-press time — `i3 -C` cannot
   catch exec-string errors; see Validation.
2. **Fragments are the editing surface.** Edit files under `conf.d/` (and
   the include root `config` only for includes/exec/bar/for_window-level
   concerns). There is no build step and no hand-editing of any generated
   assembly.
3. **Bindings that `exec` must target existing binaries.** Every
   `bindsym ... exec` must reference a binary that exists on this host
   (or a well-formed `emacsclient -n -e '(my/...)'` call into a known
   Emacs command). Verify with `command -v` before committing a new binding.
4. **Deleted notes are process history.** Files under `docs/notes/` are
   timestamped process notes. Deletion is deliberate and requires a receipt
   note (see `receipts.edn` protocol) recording what was deleted and why.
5. **Do not commit `receipts.edn` churn.** The receipt-river ledger stays
   untracked working state; never stage it.

## Where things live

| Path | What |
|---|---|
| `config` | Include root: sets, exec lines, bar, gaps, for_window, key bindings |
| `conf.d/` | Fragments by concern: launchers (`consult-launcher.conf` active, `rofi-go.conf` disabled), `helm-spotlight.conf`, `sys/` (audio/backlight/bluetooth/…), `wm/` (movement/resize/workspaces) |
| `docs/notes/` | Timestamped process notes (deletions are history) |
| `~/.agents/skills/i3-devops/SKILL.md` | Validation kit documentation (lint + sandbox) |
| `~/.config/i3/devops/`, `~/.config/i3/sandbox/` | **Referenced but currently absent** — do not assume they exist; restore before relying on `bb lint`/`bb sandbox` |

## Validation

1. **Static parse (always available):**
   `i3 -C -c ~/.config/i3/config` — exit 0 required.
2. **Static lint (when devops kit is restored):** `cd ~/.config/i3/devops && bb lint [config]`
   — parses the include tree into an EDN IR and runs data-driven regex
   rules; catches exec-string bugs `i3 -C` misses (comma-in-quotes,
   mid-word `~`). `bb check` adds live reload + errorlog scan.
3. **Sandbox (when present):** `bb sandbox smoke|probe|snapshot|shot` —
   headless i3 4.23 in Docker, synthetic keypress probes. The only automated
   detector for silently-dead bindings.

## Change rules

- One concern per fragment; add a new `conf.d/*.conf` file rather than
  growing unrelated fragments.
- Disable, don't delete, superseded launchers (see `rofi-go.conf`: fully
  commented out with a header explaining why it was disabled and what
  replaced it).
- Use `--no-startup-id` on `exec` bindings to suppress startup-notification
  cursor spin.
- Never edit files outside this repo's scope, never switch branches, never
  stage unrelated working-tree changes (deleted notes, `receipts.edn`).

## Definition of done

- [ ] `i3 -C -c ~/.config/i3/config` exits 0 (and `bb lint` when available)
- [ ] Every `exec` target verified to exist on this host
- [ ] Fragments follow STYLE.md; intent commented where non-obvious
- [ ] Committed on `device/yoga` with explicit paths (no `git add -A`)
- [ ] `receipts.edn` and unrelated working-tree changes left untouched
