# GIT_MODULE_INDEX — i3 config (yoga)

## Parent

| path | repo/remote | branch | role | one-line |
|---|---|---|---|---|
| `~` (home superproject) | home dotfiles superproject (not nested here) | main-temp | parent | Houses `~/.config/i3` as a workspace member; i3-config is checked out on `device/yoga` |

## Children

| path | repo/remote | branch | role | one-line |
|---|---|---|---|---|
| *(none)* | — | — | — | Leaf repo. `devops/` and `sandbox/` are not nested git repos — they do not exist in the current working tree (checked: `ls devops/.git sandbox/.git` → nothing). Referenced by the i3-devops skill but absent; restore as in-tree directories or submodules if re-added. |

## Notes

- Remote: `git@github.com:riatzukiza/i3-config.git`
- Local branches: `device/yoga` (current), `main`, `yoga`; remotes: `origin/main`, `origin/yoga`.
- One fork-tax tag: `Π/2026-07-10/170122-e5a811b`.
- Untracked state intentionally left out of commits: `receipts.edn`
  (receipt-river ledger), deleted `docs/notes/*` files (process history).
