# PROCESS — i3 config (stealth)

Mini-charter, modeled on the epiphany process. Full charter:
[epiphany PROCESS.md](../../spaces/foresight/epiphany/PROCESS.md)
(path: `~/spaces/foresight/epiphany/PROCESS.md` — relative path verified
to resolve from `~/.config/i3` on stealth).

1. **Observe** — reproduce against the live session; silent failures are
   the norm (binding commands parse at key-press time, not at reload).
2. **Model** — the config is data: the include tree assembles fragments
   into one rule set. Reason in terms of fragments, criteria, and modes;
   the devops kit reads that tree as an EDN IR.
3. **Verify before live** — static check first (`i3 -C`), then the real
   gate (`bb lint` + `bb test`), sandbox probes for risky keybindings,
   and only then `i3-msg reload`.
4. **Record** — non-trivial work leaves a receipt appended to
   `receipts.edn` (append-only; tracked but staged only with its own
   change) and a commit on `device/stealth`. Deleted process notes get a
   receipt note.
5. **Disabled ≠ deleted** — superseded bindings/fragments are commented
   out with a header explaining why and what replaced them.
