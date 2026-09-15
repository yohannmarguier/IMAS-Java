# IMAS-Java

The Java High Level Interface of the IMAS Access Layer. Most of it is generated
at build time by `IDSDef2Java.xsl`, which transforms the IMAS Data Dictionary
into Java classes, so the stylesheet — not the generated sources — is where this
context's language is realised. The rest is the hand-written interface in
`src/imasjava/` and the JNI binding in `wrapper/`.

Shim-side vocabulary (shim, seam, occurrence, DD-version stamp, stored DD
version, loss log, rule, fidelity verdict) is owned by
`IMAS-Multiversion-DD-Loader/CONTEXT.md` and is used here unchanged. Terms this
repository shares with the other HLIs keep the definitions in
`IMAS-Cpp/CONTEXT.md` and `IMAS-Fortran/CONTEXT.md`; the terms below are the
ones this repository owns, and the last three exist only because Java reports
failure through an exception rather than through a status.

## Language

**Refusal band**:
The status codes `-1000..-1099`, reserved for a shim that declines to serve a
path. Disjoint from IMAS-Core's own `-1..-4`.
_Avoid_: error range, MVDD codes

**Tolerated refusal**:
A refusal the generated traversal absorbs at a single field and carries on past,
instead of abandoning the operation. Only a tolerant site may absorb one.
_Avoid_: ignored error, swallowed refusal, soft failure

**Skipped path**:
One field the traversal left unset because of a tolerated refusal, recorded with
the path, the status code and the refusal message.
_Avoid_: missing field, failed path, skipped node

**Partial read**:
The outcome of a read that completed after at least one tolerated refusal. The
IDS is usable and no exception is raised; the record is what says the read was
incomplete.
_Avoid_: failed read, incomplete read, degraded read

**Partial put**:
The write-side counterpart of a partial read. Covers both a refused write and a
refused delete, which reach the caller through the same outcome.
_Avoid_: failed put, partial write

**Root operation**:
One `get`, `getSlice`, `put`, `putSlice` or `delete` called on an IDS object. It
is the unit a record describes: each root operation starts with an empty record
and leaves its own behind on the IDS it ran against.
_Avoid_: call, transaction, traversal — the traversal is the walk, the root
operation is the thing a user asked for.

**Tolerant site**:
A place in the generated traversal permitted to absorb a refusal: a leaf read, a
leaf write, a delete, and the open of an array of structures. Which sites are
tolerant is a structural property of the stylesheet, not a run-time test.
_Avoid_: safe site, leaf — the array-of-structures open is not a leaf.

**Intolerant site**:
A place that must abandon the operation on any failure, refusal included:
the time-mode read that precedes every get, the occurrence open, the data-entry
seams, the end of an action, and the readback-plugin bind and unbind. The
refusal band also carries a malformed DD-version stamp and a version-latch
conflict, and tolerating one of those would sail past an IDS that was never
opened.
_Avoid_: fatal site, critical site

**Carried status**:
The status code and the shim's own unformatted message, conveyed on the
exception the binding raises. Java's generated code never sees an `al_status_t`,
so without this a refusal is indistinguishable from any other failure and its
code survives only inside a formatted string.
_Avoid_: error code, status — "status" alone reads as the return value the other
HLIs have and Java does not.

**Collector**:
The record open for the duration of one root operation, which tolerant sites
write skipped paths into. It belongs to the operation in flight, not to any IDS;
at the end of the root operation the IDS takes its own copy.
_Avoid_: log, buffer, global record — "loss log" already names a structure of
the shim's, and the collector is neither global nor shared between operations.

**Operation record**:
The copy of the collector an IDS keeps after a root operation completes,
together with the partial outcome. It is what a caller queries, and it describes
that IDS's last root operation and no other.
_Avoid_: history, journal — nothing accumulates across operations.
