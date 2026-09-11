# PROCESS — i3 config (yoga)

Mini-charter, modeled on the epiphany process. Full charter:
[epiphany PROCESS.md](../../spaces/foresight/epiphany/PROCESS.md)
(path: `~/spaces/foresight/epiphany/PROCESS.md`).

1. **Observe** — reproduce against the live session; silent failures are the
   norm (binding commands parse at key-press time).
2. **Model** — the config is data: the include tree assembles fragments into
   one rule set. Reason in terms of fragments, criteria, and modes.
3. **Verify before live** — static check first (`i3 -C`), lint/sandbox when
   the kit is restored, only then `i3-msg reload`.
4. **Record** — non-trivial work leaves a receipt (`receipts.edn`, untracked)
   and a commit on `device/yoga`. Deleted process notes get a receipt note.
5. **Disabled ≠ deleted** — superseded mechanisms are commented out with a
   header explaining why (see `rofi-go.conf`).
