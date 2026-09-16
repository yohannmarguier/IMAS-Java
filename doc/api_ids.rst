IDS API
=======

.. highlight:: java

..
    Subroutines are generated, so choosing to document manual instead.

.. java:type:: public class ids_type

    .. java:method:: public static ids_type get(int expIdx, String idsFullName)

        Read the contents of the an IDS into memory.

        This method fetches the IDS in its entirety, with all time slices it may
        contain. See :java:ref:`getSlice` for reading a specific time slice.

        Empty fields within the IDS in the Data Entry are returned with the
        default values indicated in :ref:`Default values`.

        :param int expIdx: Data entry context created with
            :java:ref:`imas.open`, :java:ref:`imas.openEnv` or
            :java:ref:`imas.createEnv`
        :param String idsFullName: name of the ids with optional occurrence
            number, e.g. ``"core_profiles"`` (for occurrence 0),
            ``"core_profiles/1"`` (for occurrence 1)
        :return: The loaded IDS
        :example: .. literalinclude:: code_samples/dbentry_get

    .. java:method:: public static ids_type getSample(int expIdx, String idsFullName, double tmin, double tmax, double[] dtime, int interpolMode)

        Read the contents of an IDS over a specific time range into memory.

        This method fetches an IDS with all time slices in a time range between tmin and tmax. 
        
        1. In case of no interpolation in the time range, interpolMode must be set to 0 and dtime = {}.

           This mode returns an IDS object with all constant/static data filled. The dynamic data is retrieved for the provided time range [tmin, tmax].

        2. The method can interpolate time slices in the time range, if interpolMode is not set to 0 and dtime = {step} (double array of size equals 1) where 'step' is the constant time between two slices. 

            This mode will generate an IDS with a homogeneous time vector ``[tmin, tmin + dtime, tmin + 2*dtime, ...`` up to ``tmax``. The chosen interpolation method will have no effect on the time vector, but may have an impact on the
            other dynamic values. The returned IDS always has ``ids_properties.homogeneous_time = 1``.

        3. Interpolation of dynamic data on an explicit time base. This method is selected when dtime and interpolMode are provided. dtime must be a double[] of size larger than 1.

            This mode will generate an IDS with a homogeneous time vector equal to ``dtime``. ``tmin`` and ``tmax`` are ignored in this mode.
            The chosen interpolation method will have no effect on the time vector, but may have an impact on the other dynamic values. 
            The returned IDS always has ``ids_properties.homogeneous_time = 1``.

        Empty fields within the IDS in the Data Entry are returned with the
        default values indicated in :ref:`Default values`.

        :param int expIdx: Data entry context created with
            :java:ref:`imas.open`, :java:ref:`imas.openEnv` or
            :java:ref:`imas.createEnv`
        :param String idsFullName: name of the ids with optional occurrence
            number, e.g. ``"core_profiles"`` (for occurrence 0),
            ``"core_profiles/1"`` (for occurrence 1)
        :param double tmin: Lower bound of the requested time range
        :param double tmax:  Upper bound of the requested time range, must be larger than or
                equal to :param:`tmin`
        :param double[] dtime: Interval to use when interpolating, must be a std::vector<double>
                containing an explicit time base to interpolate.
        :param interpolMode: Interpolation method to use. Available options:
            - :const: CLOSEST_INTERP
            - :const: PREVIOUS_INTERP
            - :const: LINEAR_INTERP

            :returns: The loaded IDS.
        :example: .. literalinclude:: code_samples/dbentry_getSample

    .. java:method:: public boolean isDefined()
        
        Checks whether IDS was initialized or not.
        
        This method returns true in case `ids_properties` field inside `IDS` is initialized and `homogenous_time` is set to either `0`, or `1`, or `2`

        :return: `true` in case `IDS` is already initialized, `false` otherwise
        :example: .. literalinclude:: code_samples/ids_is_defined

    .. java:method:: public static ids_type getSlice(int pulseCtx, String idsFullName, double time, int interpolMode)

        Read a single time slice from an IDS in this Database Entry.

        This method fetches the IDS object with all constant/static data filled.
        The dynamic data is interpolated on the requested time slice. This means
        that the size of the time dimension in the returned data is 1.

        :param int pulseCtx: Data entry context created with
            :java:ref:`imas.open`, :java:ref:`imas.openEnv` or
            :java:ref:`imas.createEnv`
        :param String idsFullName: name of the ids with optional occurrence
            number, e.g. ``"core_profiles"`` (for occurrence 0),
            ``"core_profiles/1"`` (for occurrence 1)
        :param double time: Requested time slice
        :param int interpolMode: Interpolation method to use, see :ref:`Load a
                single \`time slice\` of an IDS`
        :return: The loaded IDS
        :example: .. literalinclude:: code_samples/dbentry_getslice

    .. java:method:: public static void put(int pulseCtx, String idsFullName, ids_type ids)

        Write the contents of an IDS to the Database Entry.

        The IDS is written entirely, with all time slices it may contain.

        The IDS object can have none or many empty fields, empty fields are
        ignored and remain empty in the data entry. Some fields are required to
        be filled before calling this method, see :ref:`Mandatory and
        recommended IDS attributes`.

        .. caution::
            The put method deletes any previously existing data within the
            target IDS occurrence in the Database Entry.

        :param int pulseCtx: Data entry context created with
            :java:ref:`imas.open`, :java:ref:`imas.openEnv` or
            :java:ref:`imas.createEnv`
        :param String idsFullName: name of the ids with optional occurrence
            number, e.g. ``"core_profiles"`` (for occurrence 0),
            ``"core_profiles/1"`` (for occurrence 1)
        :param ids_type ids: IDS object to put
        :example: .. literalinclude:: code_samples/dbentry_put

    .. java:method:: public static void putSlice(int pulseCtx, String idsFullName, ids_type ids)

        Append a time slice of the provided IDS to the Database Entry.

        Time slices must be appended in strictly increasing time order, since
        the Access Layer is not reordering time arrays. Doing otherwise will
        result in non-monotonic time arrays, which will create confusion and
        make subsequent :java:ref:`getSlice` commands to fail.

        Although being put progressively time slice by time slice, the final IDS
        must be compliant with the data dictionary. A typical error when
        constructing IDS variables time slice by time slice is to change the
        size of the IDS fields during the time loop, which is not allowed but
        for the children of an array of structure which has time as its
        coordinate.

        The :java:ref:`putSlice` command is appending data, so does not modify
        previously existing data within the target IDS occurrence in the Data
        Entry.

        It is possible possible to append several time slices to a node of the
        IDS in one :java:ref:`putSlice` call, however the user must ensure that
        the size of the time dimension of the node remains consistent with the
        size of its timebase.

        :param int pulseCtx: Data entry context created with
            :java:ref:`imas.open`, :java:ref:`imas.openEnv` or
            :java:ref:`imas.createEnv`
        :param String idsFullName: name of the ids with optional occurrence
            number, e.g. ``"core_profiles"`` (for occurrence 0),
            ``"core_profiles/1"`` (for occurrence 1)
        :param ids_type ids: IDS object to put
        :example: .. literalinclude:: code_samples/dbentry_put_slice

    .. java:method:: public byte[] serialize()
    .. java:method:: public byte[] serialize(int protocol)

        Serialize the contents of this IDS into binary data.

        There are currently two different serialization protocols. The ASCII protocol
        serializes the data though the ASCII backend. This is a simpler human readable
        protocol, but it's also less efficient than the (newer) Flexbuffers protocol.
        The latter is the default and should be preferred.

        The ID of the used serializer protocol is kept in the header of the serialized
        buffer, such that specifying the protocol is not necessary when deserializing.

        :param int protocol: Which serialization protocol to use. Available
            options are: 

            - :java:ref:`ASCII_SERIALIZER_PROTOCOL`
            - :java:ref:`FLEXBUFFERS_SERIALIZER_PROTOCOL`
            - :java:ref:`DEFAULT_SERIALIZER_PROTOCOL`
        :return: Binary representation of this IDS.
        :example:
            .. code-block:: java

                imas.ids_pf_active ids = new imas.ids_pf_active();
                // populate the IDS
                // ...
                byte[] data = ids.serialize();

                // move the binary data around, for example to another process using
                // memory communication, then deserialize
                imas.ids_pf_active ids2 = new imas.ids_pf_active();
                ids2.deserialize(data);

    .. java:method:: public void deserialize(byte[] data)

        Deserialize the provided binary data into an IDS.

        :param byte[] data: data representing a serialized IDS.
        :example: See :java:ref:`serialize`.

    .. java:method:: public void validate()

        Validate the IDS coordinate consistency. The method should always be tested for exception/errors while it is being executed. A ValidationException can be raised if a coordinate inconsistency is found. Nothing occurs if the data are valids.

        :example: .. literalinclude:: code_samples/ids_validate


    Partial operations
    -------------------

    A root operation (:java:ref:`get`, :java:ref:`getSlice`,
    :java:ref:`put`, :java:ref:`putSlice` or ``delete``) still returns
    ``void`` and still throws :java:ref:`ALException` on failure. The
    static convenience wrappers keep returning the loaded IDS, whose
    record is then queried exactly as below. Against
    a multiversion Data Dictionary shim, such an operation can also complete
    normally after quietly leaving one or more fields unset, because a field
    could not be converted between the stored and the requested Data
    Dictionary versions. This is a **partial** operation: it is neither a
    failure nor a fully clean result, and the three outcomes are told apart
    as follows:

    - An :java:ref:`ALException` is thrown: the operation failed.
    - The operation returns and :java:ref:`getSkippedPathCount` is ``0``: the
      operation completed cleanly.
    - The operation returns and :java:ref:`getSkippedPathCount` is greater
      than ``0``: the operation completed but is partial. :java:ref:`getOutcome`
      reports :java:ref:`PARTIAL_READ` or :java:ref:`PARTIAL_PUT`, and
      :java:ref:`getSkippedPaths` names every field that was left unset.

    The record describes only the IDS's last root operation: it starts empty,
    does not accumulate across calls, and is replaced in full by the next root
    operation. A :java:ref:`serialize`/:java:ref:`deserialize` pair runs a
    root operation internally against an in-memory pulse, and preserves the
    record of the root operation the caller actually asked for rather than
    that internal one.

    Without a multiversion Data Dictionary shim linked, no field can be
    refused, so every operation is either an exception or clean.

    .. caution::

        A refused write or delete is not rolled back. The generated write
        traversal has no rollback: a refusal partway through a ``put`` or
        ``putSlice`` leaves on disk whatever had already been written before
        the refusal, and a refused delete leaves its target in place. This is
        an accepted limitation of the multiversion shim, not an oversight.

    .. java:field:: public static final int CLEAN = 0

        Outcome of a root operation that completed with nothing skipped.

    .. java:field:: public static final int PARTIAL_READ = 1

        Outcome of a :java:ref:`get` or :java:ref:`getSlice` that skipped at
        least one path.

    .. java:field:: public static final int PARTIAL_PUT = 2

        Outcome of a :java:ref:`put`, :java:ref:`putSlice` or ``delete`` that
        skipped at least one path.

    .. java:method:: public final java.util.List<SkippedPath> getSkippedPaths()

        :return: the paths skipped by this IDS's last root operation, in the
            order they were skipped; empty if that operation completed
            cleanly

    .. java:method:: public final int getSkippedPathCount()

        :return: the number of paths skipped by this IDS's last root
            operation; always equal to ``getSkippedPaths().size()``

    .. java:method:: public final int getOutcome()

        :return: :java:ref:`CLEAN`, :java:ref:`PARTIAL_READ` or
            :java:ref:`PARTIAL_PUT`, describing this IDS's last root
            operation

    .. java:method:: public final boolean isPartial()

        :return: ``true`` if this IDS's last root operation skipped at least
            one path


SkippedPath
-----------

.. java:package:: imasjava

.. java:type:: public final class SkippedPath

    One field a tolerant site left unset because the multiversion Data
    Dictionary shim refused it. Recorded with the kind of root operation
    that was running, the field path as the traversal knows it, the
    refusal message, and the status code.

    .. java:type:: public enum Operation

        The kind of root operation being performed when a refusal was
        absorbed: ``READ``, ``WRITE`` or ``DELETE``.

    .. java:method:: public Operation getOperation()

        :return: the kind of root operation being performed

    .. java:method:: public String getPath()

        :return: the field path as the traversal knows it, relative to the
            enclosing context, not the full Data Dictionary path

    .. java:method:: public String getMessage()

        :return: the refusal message reported by the shim; the full Data
            Dictionary path is available inside this message

    .. java:method:: public int getCode()

        :return: the status code, inside the refusal band


ALException
-----------

.. java:type:: public class ALException extends Exception

    Signals a failure reported through the Java High-Level Interface. When
    the failure originates in the Access Layer C ABI, the exception carries
    the status code and the underlying message exactly as the Access Layer
    reported them, unformatted.

    .. java:method:: public int getCode()

        :return: the Access Layer status code carried by this exception, or
            ``0`` if the exception was not raised from the C ABI (several
            are raised from Java itself). Such an exception is never eligible
            for tolerance.

    .. java:method:: public String getRawMessage()

        :return: the underlying message, unformatted. When :java:ref:`getCode`
            is ``0``, this is the same text as ``getMessage()``.

    .. note::

        ``getMessage()`` keeps returning the formatted text it has always
        returned, so existing code that catches :java:ref:`ALException` and
        reads ``getMessage()`` is unaffected by the carried status.
