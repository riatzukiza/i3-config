# Π Handoff — 2026-08-12

## Branch
`device/stealth`

## What changed
- **config**: removed redundant xss-lock/nm-applet/emacs autostart lines; added barrier toggle binding; quoted rofi commands (comma-separated modi lists need whole-command quotes); added web-search binding ($mod+slash)
- **conf.d/wm/pi-desktop-ops.conf**: added explicit modi path for i3sessions rofi mode
- **New files**: `.gitignore`, `AGENTS.md`, `devops/` (bb lint/parser/test kit), `sandbox/` (Docker i3 4.23 test harness), `docs/inbox/`, `receipts.edn`

## Verification
- `bb lint`: 0 errors, 1 pre-existing warning (caffeine-toggle.sh path missing — not introduced by this change)
- `bb test`: not run this session (no code changes to parser/lint rules)

## Concurrent dirt
None detected. All changes are owned.

## Tag
`Π/device/stealth/c73a301`

## Commit
`c73a301` on `device/stealth` — pushed to origin.
