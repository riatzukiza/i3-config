# GIT_MODULE_INDEX — i3 config (stealth)

## Parent

| path | repo/remote | branch | role | one-line |
|---|---|---|---|---|
| `~` (stealth home superproject) | home dotfiles superproject (not nested here) | device/stealth | parent | Houses `~/.config/i3` as a workspace member; i3-config is checked out on `device/stealth` |

## Children

| path | repo/remote | branch | role | one-line |
|---|---|---|---|---|
| *(none)* | — | — | — | Leaf repo. Verified via `find ~/.config/i3 -name .git`: only the repo root is a git repo; `devops/` and `sandbox/` are plain in-tree directories (tracked), not nested repos or submodules. |

## Notes

- Remote: `git@github.com:riatzukiza/i3-config.git`
- Current branch: `device/stealth` (stay on it).
- Fork-tax tags in history include `Π/2026-07-10/170122-e5a811b` and
  `Π/device/stealth/c73a301`.
- `receipts.edn` and `.ημ/Π_LAST.md` are **tracked** on this branch
  (unlike the yoga sibling where the ledger is untracked) — stage them
  only with their own deliberate change (append-only for the ledger).
- `devops/` and `sandbox/` are present and tracked on stealth — they are
  NOT "referenced but absent" here as they are on the yoga sibling tree.
