import imasjava.*;
import imasjava.wrapper.LowLevel;
import imasjava.wrapper.Wrapper;

class TestALExceptionCarriedStatus {
    public static void main(String args[]) {
        // Force the native binding to load before calling Wrapper directly, since
        // Wrapper itself does not trigger imas's static loadLibrary block.
        imas.get_al_version();

        System.out.println("### Testing that a status raised through the JNI binding carries its real code and raw message");
        boolean sawFailure = false;
        try {
            Wrapper.alBeginDataEntryAction("not-a-valid-uri", LowLevel.OPEN_PULSE);
            System.out.println("Issue, expected a failure opening an invalid URI.");
            System.exit(1);
        } catch (ALException e) {
            sawFailure = true;
            if (e.getCode() == 0) {
                System.out.println("Issue: expected a non-zero code carried from the Access Layer.");
                System.exit(1);
            }
            if (e.getRawMessage() == null || e.getRawMessage().isEmpty()) {
                System.out.println("Issue: expected a non-empty raw message carried from the Access Layer.");
                System.exit(1);
            }
            String expectedMessage = "ERROR[" + e.getCode() + "]\n" + e.getRawMessage() + "\n";
            if (!expectedMessage.equals(e.getMessage())) {
                System.out.println("Issue: the formatted message no longer matches the carried code and raw message.");
                System.exit(1);
            }
        }
        if (!sawFailure) {
            System.out.println("Issue: no failure was observed opening an invalid URI.");
            System.exit(1);
        }
        System.out.println("Carried code and raw message observed on a failure raised through the Access Layer.");
        System.out.println("");

        System.out.println("### Testing that a status raised from Java itself carries code 0");
        imas.core_profiles ids = new imas.core_profiles();
        ids.ids_properties.homogeneous_time = LowLevel.IDS_TIME_MODE_HOMOGENEOUS;
        try {
            ids.validate();
            System.out.println("Issue, expected a failure validating an inconsistent IDS.");
            System.exit(1);
        } catch (ALException e) {
            if (e.getCode() != 0) {
                System.out.println("Issue: expected code 0 for a failure raised from Java itself.");
                System.exit(1);
            }
            if (!e.getMessage().equals(e.getRawMessage())) {
                System.out.println("Issue: expected the raw message to match the formatted message when code is 0.");
                System.exit(1);
            }
        }
        System.out.println("Code 0 observed on a failure raised from Java itself.");
    }
}
