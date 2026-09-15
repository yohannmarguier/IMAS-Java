import imasjava.*;
import imasjava.wrapper.LowLevel;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 * Exercises the operation record's lifecycle through the public accessors
 * a user already has, against a real backend: the record is empty before
 * any root operation, empty and clean after an ordinary get and after an
 * ordinary put, it does not accumulate across calls, its reported count
 * agrees with its entries, and a record left by a partial read survives a
 * subsequent serialize/deserialize round trip rather than being clobbered
 * by their internal put/get against an in-memory pulse.
 */
class TestOperationRecordLifecycle {

    private static int failures = 0;

    public static void main(String[] args) {
        try {
            imas.core_profiles fresh = new imas.core_profiles();
            checkRecord("before any root operation", fresh, Collections.<SkippedPath>emptyList(), Ids.CLEAN);

            String currentDir = System.getProperty("user.dir");
            String uri = "imas:mdsplus?path=" + currentDir + "/test_db_TestOperationRecordLifecycle";
            int idx = imas.open(uri, LowLevel.FORCE_CREATE_PULSE);

            imas.core_profiles ids = new imas.core_profiles();
            ids.setPulseCtx(idx);
            ids.ids_properties.homogeneous_time = 1;
            ids.ids_properties.comment = "TestOperationRecordLifecycle";

            ids.put(0);
            checkEmptyAndClean("after an ordinary put", ids);

            ids.put(0);
            checkEmptyAndClean("after a second ordinary put (no accumulation)", ids);

            ids.get(0);
            checkEmptyAndClean("after an ordinary get", ids);

            ids.get(0);
            checkEmptyAndClean("after a second ordinary get (no accumulation)", ids);

            // The traversal does not yet absorb any tolerated refusal on its own, so a
            // partial-read record is forced directly onto the IDS, standing in for the
            // record a future tolerant read will leave, to prove serialize/deserialize
            // do not clobber it with the clean outcome of their own internal put/get.
            List<SkippedPath> partialRead = Collections.singletonList(
                    new SkippedPath(SkippedPath.Operation.READ, "some/path", "tolerated for test",
                            ToleranceChokepoint.REFUSAL_BAND_MAX));
            forceOperationRecord(ids, partialRead, Ids.PARTIAL_READ);
            checkRecord("after forcing a partial-read record", ids, partialRead, Ids.PARTIAL_READ);

            byte[] data = ids.serialize();
            checkRecord("after serialize (a record from a partial read must survive)",
                    ids, partialRead, Ids.PARTIAL_READ);

            ids.deserialize(data);
            checkRecord("after deserialize (a record from a partial read must survive)",
                    ids, partialRead, Ids.PARTIAL_READ);

            imas.close(idx);
        } catch (Exception exc) {
            System.out.println("Issue: unexpected exception " + exc);
            failures++;
        }

        if (failures > 0) {
            System.out.println("Issue: " + failures + " check(s) failed.");
            System.exit(1);
        }
        System.out.println("Operation record lifecycle verified.");
    }

    private static void checkEmptyAndClean(String label, Ids ids) {
        checkRecord(label, ids, Collections.<SkippedPath>emptyList(), Ids.CLEAN);
    }

    private static void checkRecord(String label, Ids ids, List<SkippedPath> expectedSkippedPaths,
            int expectedOutcome) {
        List<SkippedPath> skippedPaths = ids.getSkippedPaths();
        check(label + ": skipped paths should match", expectedSkippedPaths.equals(skippedPaths));
        check(label + ": skipped path count should match",
                ids.getSkippedPathCount() == expectedSkippedPaths.size());
        check(label + ": count should agree with the number of entries",
                ids.getSkippedPathCount() == skippedPaths.size());
        check(label + ": outcome should match", ids.getOutcome() == expectedOutcome);
        check(label + ": partial flag should match", ids.isPartial() == (expectedOutcome != Ids.CLEAN));
    }

    private static void check(String label, boolean condition) {
        if (!condition) {
            System.out.println("Issue: " + label);
            failures++;
        }
    }

    // Reflection stands in for the tolerant read call site's own hook onto the
    // record, deliberately not part of the public API a caller ever exercises.
    private static void forceOperationRecord(Ids ids, List<SkippedPath> skippedPaths, int outcome) {
        try {
            Method restoreOperationRecord = Ids.class.getDeclaredMethod("restoreOperationRecord",
                    List.class, int.class);
            restoreOperationRecord.setAccessible(true);
            restoreOperationRecord.invoke(ids, skippedPaths, outcome);
        } catch (ReflectiveOperationException exc) {
            throw new RuntimeException(exc);
        }
    }
}
