# AGENTS.md — i3 config (stealth)

## Role

You are working on the i3 window-manager configuration for the **Stealth
16** laptop (i3 4.23), launched from inside Plasma via the `i3-wm.service`
systemd user unit. The live config is `./config`, assembled from fragments
in `conf.d/` via `include` globs. You may be a human or an agent; the rules
are the same.

## Invariants

1. **Config must lint before live reload.** Run the static check (`i3 -C
   -c ~/.config/i3/config`, exit 0) **and** the devops lint
   (`cd ~/.config/i3/devops && bb lint`) before any `i3-msg reload`
   touches the session. Binding commands are parsed at key-press time —
   `i3 -C` cannot catch exec-string errors; the lint kit is the gate for
   those (see Validation).
2. **Fragments are the editing surface.** Edit files under `conf.d/` (and
   the include root `config` only for includes/exec/bar/for_window-level
   concerns). There is no build step and no hand-editing of any generated
   assembly.
3. **Bindings that `exec` must target existing binaries.** Every
   `bindsym ... exec` must reference a binary that exists on this host
   (`rofi`, `i3-resurrect`, `brightnessctl`, `~/bin/emvterm`,
   `~/.local/bin/rofi-*` scripts, …). Verify with `command -v` (or
   `ls` for `~/bin`/`~/.local/bin` paths) before committing a new binding.
   `curl` targets must reference a service that actually runs
   (e.g. ytwall on `localhost:3323`).
4. **Plasma is the session shell; don't fight it.** This session is
   Plasma-with-i3 (`i3-wm.service`). Do not add i3 `exec` lines that
   duplicate Plasma autostart (`dex` already handles XDG autostart), and
   do not delete or "clean up" the `for_window` plasmashell quirk rules —
   they exist because plasmashell windows appear in i3's tree.
5. **Deleted notes are process history.** Files under `docs/inbox/` are
   timestamped process notes. Deletion is deliberate and requires a
   receipt note (see `receipts.edn` protocol) recording what was deleted
   and why.
6. **Do not commit `receipts.edn` churn.** The receipt-river ledger is
   tracked, but it is append-only working state — stage it only when you
   deliberately appended a receipt for the change being committed. Never
   sweep it into unrelated commits.

## Where things live

| Path | What |
|---|---|
| `config` | Include root: sets, exec lines, bar, gaps, Plasma `for_window` quirks, rofi bindings |
| `conf.d/` | Fragments by concern: `caffeine.conf`, `yt-wallpaper.conf`, `sys/` (audio/backlight/bluetooth/…), `wm/` (movement/resize/workspaces/pi-desktop-ops) |
| `devops/` | bb project: config→EDN IR parser, data-driven static lint, CLI (`bb lint\|parse\|check\|test\|sandbox`) |
| `sandbox/` | Docker sandbox: ubuntu:24.04 with i3 4.23 (host-matched), Xvfb, xdotool probes, snapshots, screenshots |
| `docs/inbox/` | Timestamped process notes (deletions are history) |
| `receipts.edn` | receipt-river ledger (tracked; append-only) |
| `~/.agents/skills/i3-devops/SKILL.md` | Validation kit skill documentation |

## Validation

1. **Static parse (always available):**
   `i3 -C -c ~/.config/i3/config` — exit 0 required. Necessary, not
   sufficient: it cannot see binding-command parse errors.
2. **Static lint (primary gate):** `cd ~/.config/i3/devops && bb lint [config]`
   — parses the include tree into an EDN IR and runs data-driven regex
   rules; catches exec-string bugs `i3 -C` misses (comma-in-quotes,
   mid-word `~`). `bb test` runs the parser + rule suite (includes the
   reproduced comma-in-quotes regression). `bb check` adds live reload +
   errorlog scan.
3. **Sandbox (before risky edits):** `bb sandbox up && bb sandbox smoke
   && bb sandbox probe <keys>` — headless i3 4.23 in Docker, synthetic
   keypress probes. The only automated detector for silently-dead
   bindings.

## Change rules

- One concern per fragment; add a new `conf.d/*.conf` file rather than
  growing unrelated fragments.
- Disable, don't delete, superseded bindings/fragments: comment them out
  with a header explaining why and what replaced them.
- Use `--no-startup-id` on `exec` bindings to suppress startup-notification
  cursor spin.
- Never edit files outside this repo's scope, never switch branches
  (stay on `device/stealth`), never stage unrelated working-tree changes
  (unrelated dirty `config` hunks, `receipts.edn` churn).
- Never touch `.gitignore`.

## Definition of done

- [ ] `i3 -C -c ~/.config/i3/config` exits 0 **and** `bb lint` reports no errors
- [ ] `bb test` passes (12 tests / 28 assertions baseline; do not go below)
- [ ] Every `exec` target verified to exist on this host
- [ ] Fragments follow STYLE.md; intent commented where non-obvious
- [ ] Committed on `device/stealth` with explicit paths (no `git add -A`)
- [ ] `receipts.edn` and unrelated working-tree changes left untouched
