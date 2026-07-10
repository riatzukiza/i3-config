# Π Handoff — i3-config

- **branch:** `yoga`
- **parent head:** `94a51e6`
- **snapshot time:** `2026-07-10T17:01:22Z`
- **tag format:** `Π/2026-07-10/170122-<short-head>`

## Owned changes

### Modified
- `conf.d/helm-spotlight.conf` — switch `for_window` matcher from `instance` to `title`; add `agent-shell-project` frame rule; add `$mod+Ctrl+Shift+p` binding for project-to-agent-shell.
- `conf.d/wm/pi-desktop-ops.conf` — comment out `$mod+Ctrl+p` rofi i3sessions binding to avoid conflict with helm-spotlight.
- `config` — start `barrier.service` on login; move picom toggle to `$mod+Ctrl+Shift+o`.

### Untracked (now staged)
- `docs/notes/` — four existing notes files.
- `.ημ/PRINCIPLE.edn` — existing principle artifact.
- `.ημ/Π_STATE.sexp`, `.ημ/Π_LAST.md`, `.ημ/Π_MANIFEST.sha256` — this handoff set.

## Concurrent / unowned dirt

None. All observed working-tree changes are owned by this handoff.

## Verification

```
$ i3 -C -c /home/err/.config/i3/config
exit: 0
```

i3 config syntax validated before commit.

## Push status

To be filled after push.
