import imasjava.ALException;
import imasjava.RefusalCollector;
import imasjava.SkippedPath;
import imasjava.ToleranceChokepoint;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

/**
 * Drives {@link ToleranceChokepoint} directly with hand-made exceptions.
 * Needs no shim, no pulse, no backend and no native call: it never touches
 * {@code imas} or the JNI binding, only the hand-written decision class.
 */
class TestToleranceChokepoint {

    private static int failures = 0;

    public static void main(String[] args) {
        testBandBoundariesAreTolerated();
        testJustOutsideBandAborts();
        testZeroCodeAborts();
        testExceptionWithNoCodeAborts();
        testCoreOwnCodesAbort();
        testEveryOperationTag();
        testCollectorIsPerThread();

        if (failures > 0) {
            System.out.println("Issue: " + failures + " check(s) failed.");
            System.exit(1);
        }
        System.out.println("Tolerance chokepoint truth table verified.");
    }

    // Truth-table cases
    // ------------------------------------------------------------------

    private static void testBandBoundariesAreTolerated() {
        assertTolerated("band boundary closest to zero (-1000)", ToleranceChokepoint.REFUSAL_BAND_MAX, "band/max-boundary");
        assertTolerated("band boundary furthest from zero (-1099)", ToleranceChokepoint.REFUSAL_BAND_MIN, "band/min-boundary");
    }

    private static void testJustOutsideBandAborts() {
        assertAborts("one above the band's near boundary (-999)", ToleranceChokepoint.REFUSAL_BAND_MAX + 1);
        assertAborts("one below the band's far boundary (-1100)", ToleranceChokepoint.REFUSAL_BAND_MIN - 1);
    }

    private static void testZeroCodeAborts() {
        assertAborts("code 0", 0);
    }

    private static void testExceptionWithNoCodeAborts() {
        int before = recordedCount();
        ALException noCode = new ALException("raised from Java");
        Decision decision = decide(noCode, SkippedPath.Operation.READ, "no-code/path");
        check("an exception carrying no code should abort", !decision.tolerated);
        check("an exception carrying no code should record nothing", recordedCount() == before);
        check("an exception carrying no code should print nothing", decision.stdout.isEmpty());
    }

    private static void testCoreOwnCodesAbort() {
        assertAborts("IMAS-Core code -1", -1);
        assertAborts("IMAS-Core code -2", -2);
        assertAborts("IMAS-Core code -3", -3);
        assertAborts("IMAS-Core code -4", -4);
    }

    private static void testEveryOperationTag() {
        assertOperationTagged(SkippedPath.Operation.READ, "REFUSED READ: ", "leaf/read");
        assertOperationTagged(SkippedPath.Operation.WRITE, "REFUSED WRITE: ", "leaf/write");
        assertOperationTagged(SkippedPath.Operation.DELETE, "REFUSED DELETE: ", "leaf/delete");
    }

    private static void testCollectorIsPerThread() {
        Recorder first = new Recorder("thread/first");
        Recorder second = new Recorder("thread/second");
        first.start();
        second.start();
        try {
            first.join();
            second.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            check("the two recorder threads should finish without interruption", false);
            return;
        }
        check("the first thread's collector should hold only its own skipped path", first.sawOwnPathOnly);
        check("the second thread's collector should hold only its own skipped path", second.sawOwnPathOnly);
    }

    // Helpers
    // ------------------------------------------------------------------

    private static void assertTolerated(String label, int code, String path) {
        int before = recordedCount();
        Decision decision = decide(withCode(code), SkippedPath.Operation.READ, path);
        check(label + " should be tolerated", decision.tolerated);
        check(label + " should record exactly one skipped path", recordedCount() == before + 1);
    }

    private static void assertAborts(String label, int code) {
        int before = recordedCount();
        Decision decision = decide(withCode(code), SkippedPath.Operation.READ, "abort/path");
        check(label + " should not be tolerated", !decision.tolerated);
        check(label + " should record nothing", recordedCount() == before);
        check(label + " should print nothing", decision.stdout.isEmpty());
    }

    private static void assertOperationTagged(SkippedPath.Operation operation, String expectedLabel, String path) {
        ALException failure = withCode(ToleranceChokepoint.REFUSAL_BAND_MAX);
        Decision decision = decide(failure, operation, path);
        String expectedLine = expectedLabel + path + System.lineSeparator();
        check("a tolerated " + operation + " should be reported as \"" + expectedLabel.trim() + "\"",
                decision.stdout.equals(expectedLine));

        List<SkippedPath> recorded = RefusalCollector.current().getSkippedPaths();
        SkippedPath last = recorded.get(recorded.size() - 1);
        check("the recorded skipped path should carry the " + operation + " operation tag",
                last.getOperation() == operation);
        check("the recorded skipped path should carry the field path", last.getPath().equals(path));
        check("the recorded skipped path should carry the refusal message",
                last.getMessage().equals(failure.getRawMessage()));
        check("the recorded skipped path should carry the status code", last.getCode() == failure.getCode());
    }

    private static int recordedCount() {
        return RefusalCollector.current().getSkippedPaths().size();
    }

    private static ALException withCode(int code) {
        return new ALException("formatted", code, "raw refusal for code " + code);
    }

    private static void check(String label, boolean condition) {
        if (!condition) {
            System.out.println("Issue: " + label);
            failures++;
        }
    }

    private static final class Decision {
        final boolean tolerated;
        final String stdout;

        Decision(boolean tolerated, String stdout) {
            this.tolerated = tolerated;
            this.stdout = stdout;
        }
    }

    private static Decision decide(ALException failure, SkippedPath.Operation operation, String path) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        boolean tolerated;
        try {
            System.setOut(new PrintStream(buffer));
            tolerated = ToleranceChokepoint.tolerate(failure, operation, path);
        } finally {
            System.setOut(original);
        }
        return new Decision(tolerated, buffer.toString());
    }

    private static final class Recorder extends Thread {
        private final String path;
        private boolean sawOwnPathOnly;

        Recorder(String path) {
            this.path = path;
        }

        @Override
        public void run() {
            ToleranceChokepoint.tolerate(withCode(ToleranceChokepoint.REFUSAL_BAND_MAX), SkippedPath.Operation.READ, path);
            List<SkippedPath> recorded = RefusalCollector.current().getSkippedPaths();
            boolean sawOwn = false;
            boolean sawOther = false;
            for (SkippedPath skipped : recorded) {
                if (skipped.getPath().equals(path)) {
                    sawOwn = true;
                } else if (skipped.getPath().startsWith("thread/")) {
                    sawOther = true;
                }
            }
            sawOwnPathOnly = sawOwn && !sawOther;
        }
    }
}
