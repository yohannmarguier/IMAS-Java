.. todo::

    Verify that the code samples run on SDCC (once a pre-release version is
    available as a module).

.. highlight:: java

.. include:: ./doc_common/load_store_ids.rst


Partial reads and writes against a multiversion Data Dictionary
-----------------------------------------------------------------

.. note::

    This is unrelated to fetching a subset of an IDS's paths in a single
    call (sometimes called "partial get" in other Access Layer interfaces),
    which the Java interface does not support.

When reading or writing through a multiversion Data Dictionary shim, a
:java:ref:`get`, :java:ref:`getSlice`, :java:ref:`put` or :java:ref:`putSlice`
can complete normally and still be **partial**: one or more fields could not
be converted between the stored and the requested Data Dictionary versions,
so the shim refused them, and the traversal left those fields unset rather
than abandon the rest of the IDS.

A completed root operation is therefore not necessarily a clean one. Three
outcomes exist and are told apart as follows:

1. The call throws :java:ref:`ALException`: the operation failed.
2. The call returns and the IDS reports no skipped paths
   (:java:ref:`getSkippedPathCount` is ``0``): the operation completed
   cleanly.
3. The call returns and the IDS reports at least one skipped path: the
   operation completed but is partial.

Every partial IDS can be queried after the call: :java:ref:`isPartial` and
:java:ref:`getOutcome` report whether the last root operation was clean, a
:java:ref:`PARTIAL_READ` or a :java:ref:`PARTIAL_PUT`, and
:java:ref:`getSkippedPaths` names every field that was left unset, together
with the kind of operation being performed, the shim's own refusal message
(which carries the full Data Dictionary path), and the status code. One line
naming the operation and the path is also printed to standard output for
every field that is skipped, so a partial result is never silent even when
nothing is queried.

.. caution::

    A refused write is not rolled back: whatever had already been written to
    the Database Entry before the refusal is left in place. This is an
    accepted limitation of the multiversion shim, not an oversight.

Without a multiversion Data Dictionary shim linked, no field can be refused,
so every root operation is either an exception or a clean result, exactly as
before.

.. seealso::

    API documentation: :ref:`Partial operations`, :java:ref:`SkippedPath`,
    :java:ref:`ALException`.


..
    Below follow all replacement texts to fill in the language specific parts
    of the load_store_ids common documentation

.. |lang| replace:: Java

.. |dbentry| replace:: an integer index
.. |dbentry_open| replace:: :java:ref:`imas.open`, :java:ref:`imas.openEnv` (legacy)
.. |dbentry_get| replace:: :java:ref:`get`
.. |dbentry_getslice| replace:: :java:ref:`getSlice`
.. |CLOSEST_INTERP| replace:: :java:ref:`CLOSEST_INTERP`
.. |PREVIOUS_INTERP| replace:: :java:ref:`PREVIOUS_INTERP`
.. |LINEAR_INTERP| replace:: :java:ref:`LINEAR_INTERP`

.. |dbentry_create| replace:: :java:ref:`imas.open`, :java:ref:`imas.createEnv` (legacy)
.. |dbentry_put| replace:: :java:ref:`put`
.. |dbentry_put_slice| replace:: :java:ref:`putSlice`

.. |list_all_occurrences| replace:: :java:ref:`listAllOccurrences`

