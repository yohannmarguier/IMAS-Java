import imasjava.*;
import imasjava.wrapper.LowLevel;

import java.util.List;

/**
 * Exercises the operation record's lifecycle through the public accessors
 * a user already has, against a real backend and with no refusal in sight:
 * the record is empty before any root operation, empty and clean after an
 * ordinary get and after an ordinary put, it does not accumulate across
 * calls, and its reported count agrees with its entries.
 */
class TestOperationRecordLifecycle {

    private static int failures = 0;

    public static void main(String[] args) {
        try {
            imas.core_profiles fresh = new imas.core_profiles();
            checkEmptyAndClean("before any root operation", fresh);

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
        List<SkippedPath> skippedPaths = ids.getSkippedPaths();
        check(label + ": skipped paths should be empty", skippedPaths.isEmpty());
        check(label + ": skipped path count should be zero", ids.getSkippedPathCount() == 0);
        check(label + ": count should agree with the number of entries",
                ids.getSkippedPathCount() == skippedPaths.size());
        check(label + ": outcome should be clean", ids.getOutcome() == Ids.CLEAN);
        check(label + ": operation should not be reported as partial", !ids.isPartial());
    }

    private static void check(String label, boolean condition) {
        if (!condition) {
            System.out.println("Issue: " + label);
            failures++;
        }
    }
}
