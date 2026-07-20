# ADR 0001: Kotoba is the AFBF catalog source authority

- Status: Accepted
- Date: 2026-07-21

## Context

The former CLJC catalog exposed unbounded host maps and sequences. Its second
citation deliberately has no established date, and governance and advocacy
belong to different entries, so a mechanical uniform-record conversion would
silently corrupt the evidence.

## Decision

`src/association_facts.kotoba` is the sole production source. Both citations
retain every present scalar field and their distinct topics. The missing
established date for `afbf.what-we-do` remains typed option-none. A fixed field
vocabulary and bounded count/index ABI replace host traversal.

Unknown associations, fields, topics, negative indexes, and out-of-range
indexes return zero or option-none. The source declares no effects. DataScript
EDN remains a derived provider artifact.

CI executes reference semantics, restricted JavaScript, and instantiated typed
WebAssembly, and rejects production `.clj`, `.cljc`, and `.cljs` sources.

## Consequences

- Deliberate source absence is preserved rather than filled or guessed.
- Topic filtering remains ordered and bounded.
- Clojure and the JVM are compiler/test hosts only.
