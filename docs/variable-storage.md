# Variable storage and serialization

```text
variable mutation (server thread)
  -> Variables.serializeChange / Classes.serialize
  -> SerializedVariable(name, Value(ClassInfo code name, bytes))
  -> global saveQueue / dispatcher
  -> backend queue
  -> file or database
```

## Serialization boundary

`Classes.serialize` selects the most specific registered `ClassInfo`, applies its
`serializeAs` conversion if configured, and requires a serializer. It writes through
`Variables.yggdrasil`, omitting the stream/type prefix reconstructed by
`Classes.deserialize`. Backends transport this representation without knowing
individual Bukkit types. Java `Serializable` does not determine support.

Serialization and deserialization run on the server thread because serializers may
access Bukkit state. Queues carry serialized bytes, not live objects. Null mutations
mean deletion; unsupported non-null mutations are skipped and reported once per name
until a successful change. Skipping an incremental write preserves its previous
stored value, but a later CSV snapshot rewrite can omit an unsupported memory value.

Skript lists are individual named leaves (`list::1`, `list::2`). Yggdrasil recursively
handles its supported collections, arrays and serializer fields; this does not
register arbitrary Java collections as top-level Skript types.

Named colors and RGB values have separate concrete registrations because enums and
objects use different wire tags. RGB values save ARGB and derive their dye color on
load. These serializers work across backends. Parse-time warnings consult
`Classes.mayBeSerializable`, allowing declared interfaces with registered serializable
implementations. Runtime serialization still validates each value. Yggdrasil resolves
registered superclasses and permits legacy read aliases with canonical write IDs.

## Selection and loading

Optional MySQL is disabled by default. When enabled, it replaces normal `databases`
routing only after successful initialization. Invalid settings, schema or startup
connection/write failures fall back to configured storage without publishing partial
MySQL data. The stores are not automatically synchronized after fallback.

Startup loads rows ordered by name, overlays the recovery journal (including
deletions), checks write access, and publishes decoded values on the server thread.
Unreadable rows are reported and retained while other rows load. Startup SQL follows
the existing loading lifecycle; steady-state SQL belongs to the backend worker.

## Writing, shutdown and recovery

`PooledMySQLStorage` owns a connection pool and one batch worker. Producers append to
its concurrent incoming queue; the worker coalesces mutations by name. The pool
reuses one physical JDBC connection with validated logical handles, discarding broken
connections. Statements and handles close with their scopes; the worker closes the
pool on exit. The base storage writer is retained by the common lifecycle, but
optional MySQL overrides enqueueing to use its batch worker.

Before a prepared-statement transaction, the worker writes an atomic, checksummed
`mysql-pending.bin` journal. Full-name checks protect against hash collisions. After
commit it clears the journal before clearing pending memory. Failed commits or
acknowledgements retain the batch for retry; replayed updates/deletions are idempotent.
The journal's target identity prevents replay into a different database/table.

Shutdown drains and joins the global dispatcher before closing backends. MySQL drains
its queue, attempts a final batch, preserves pending changes in the journal if the
database is unavailable, and joins its worker. Disk failure is reported as critical.
A sudden crash can lose changes not yet journaled: writes are asynchronous, not
synchronously durable.

## Compatibility

The table layout is `name, type, hash, value`. Startup upgrades old `name_hash` and
`small_value` schemas while preserving their data; this requires ALTER permission.
The serialization refactor itself needs no schema change or automatic row rewrite.

New writes use standard Skript data. `LegacyMySQLValueReader` only validates the
previous `mysql:yggdrasil:1` and `SKMYSQL2` frames, delegating decoding to shared
Yggdrasil serializers. Color registration accepts the old RGB ID and payload.

Custom inventory persistence is removed. Old inventory rows and missing-addon values
remain stored but cannot load without their serializers. Old collection envelopes
can still load through Yggdrasil; assigning an unregistered top-level collection
remains unsupported. No backend-specific replacement serializers are provided.
