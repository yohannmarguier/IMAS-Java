import imasjava.*;
import imasjava.wrapper.LowLevel;

/**
 * Exercises the leaf-read try/catch that {@code GET_SINGLE} now emits around
 * every scalar and array data read, on the clean path: every leaf, including
 * ones nested inside an array of structures, round-trips through get() and
 * getSlice() unchanged, and the operation record stays empty.
 *
 * <p>Driving the catch's tolerated branch needs a refusal-band status, which
 * only a linked multiversion shim can produce; without it every read here
 * takes the try block's normal path, so this is a regression test for that
 * path, not a test of {@link ToleranceChokepoint} itself (see
 * {@code TestToleranceChokepoint}).
 */
class TestLeafReadTolerance {

    private static int failures = 0;

    public static void main(String[] args) {
        try {
            String currentDir = System.getProperty("user.dir");
            String uri = "imas:mdsplus?path=" + currentDir + "/test_db_TestLeafReadTolerance";
            int idx = imas.open(uri, LowLevel.FORCE_CREATE_PULSE);

            double[] time = {1.0, 2.0, 3.0};

            imas.core_profiles ids = new imas.core_profiles();
            ids.setPulseCtx(idx);
            ids.ids_properties.homogeneous_time = 1;
            ids.ids_properties.comment = "TestLeafReadTolerance";
            ids.time = new Vect1DDouble(time);

            ids.profiles_1d = new imas.core_profiles.profiles_1dClass[time.length];
            for (int i = 0; i < time.length; i++) {
                ids.profiles_1d[i] = new imas.core_profiles.profiles_1dClass();
                ids.profiles_1d[i].time = time[i];
                ids.profiles_1d[i].ion = new imas.core_profiles.profiles_1dClass.ionClass[1];
                ids.profiles_1d[i].ion[0] = new imas.core_profiles.profiles_1dClass.ionClass();
                ids.profiles_1d[i].ion[0].z_ion = 10.0 + i;
            }

            ids.put(0);

            imas.core_profiles fetched = new imas.core_profiles();
            fetched.setPulseCtx(idx);
            fetched.get(0);
            checkCleanGet("after get(0)", fetched, time);

            imas.core_profiles sliced = new imas.core_profiles();
            sliced.setPulseCtx(idx);
            sliced.getSlice(0, time[1], LowLevel.CLOSEST_INTERP);
            check("getSlice: profiles_1d ion leaf survives the wrapped read",
                    sliced.profiles_1d != null && sliced.profiles_1d.length > 0
                            && sliced.profiles_1d[0].ion[0].z_ion == 10.0);
            check("getSlice: operation record stays clean on the untaken catch branch",
                    !sliced.isPartial() && sliced.getSkippedPathCount() == 0);

            imas.close(idx);
        } catch (Exception exc) {
            System.out.println("Issue: unexpected exception " + exc);
            failures++;
        }

        if (failures > 0) {
            System.out.println("Issue: " + failures + " check(s) failed.");
            System.exit(1);
        }
        System.out.println("Leaf-read tolerance clean-path regression verified.");
    }

    private static void checkCleanGet(String label, imas.core_profiles ids, double[] time) {
        check(label + ": ids_properties.comment leaf survives the wrapped read",
                "TestLeafReadTolerance".equals(ids.ids_properties.comment));
        check(label + ": profiles_1d array-of-structures leaf survives the wrapped read",
                ids.profiles_1d != null && ids.profiles_1d.length == time.length);
        for (int i = 0; i < time.length; i++) {
            check(label + ": profiles_1d[" + i + "].ion[0].z_ion leaf survives the wrapped read",
                    ids.profiles_1d[i].ion[0].z_ion == 10.0 + i);
        }
        check(label + ": operation record stays clean on the untaken catch branch",
                !ids.isPartial() && ids.getSkippedPathCount() == 0);
    }

    private static void check(String label, boolean condition) {
        if (!condition) {
            System.out.println("Issue: " + label);
            failures++;
        }
    }
}
