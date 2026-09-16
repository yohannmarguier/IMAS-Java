package imasjava;

/**
 * The single place that decides, for one field, whether a refusal is a
 * {@code tolerated refusal} the generated traversal may absorb and carry
 * on past, or a failure the caller must abort on.
 *
 * <p>The decision rests entirely on the {@code carried status} of the
 * {@link ALException} the traversal caught: a code inside the
 * <strong>refusal band</strong> {@code -1000..-1099} (both boundaries
 * included) is tolerated, everything else — code {@code 0}, one of
 * IMAS-Core's own negative codes, or any code outside the band — is not.
 *
 * <p>A tolerated refusal is recorded as a {@link SkippedPath} on the
 * calling thread's {@link RefusalCollector} and reported with exactly one
 * line on standard output, spelled to match IMAS-Cpp's
 * {@code REFUSED READ:} / {@code REFUSED WRITE:} / {@code REFUSED DELETE:}.
 */
public final class ToleranceChokepoint {
  /** The refusal band's boundary closest to zero. */
  public static final int REFUSAL_BAND_MAX = -1000;

  /** The refusal band's boundary furthest from zero. */
  public static final int REFUSAL_BAND_MIN = -1099;

  private ToleranceChokepoint() {
  }

  /**
   * Decides whether {@code failure} is a tolerated refusal for the field
   * at {@code path}.
   *
   * <p>When tolerated, records a {@link SkippedPath} on the calling
   * thread's {@link RefusalCollector} and prints the diagnostic line
   * before returning.
   *
   * @param failure   the exception a tolerant site caught
   * @param operation the root operation the traversal was performing
   * @param path      the field path as the traversal knows it, relative
   *                  to the enclosing context
   * @return {@code true} when the caller may carry on past the refusal,
   *         {@code false} when the caller must abort
   */
  public static boolean tolerate(ALException failure, SkippedPath.Operation operation, String path) {
    int code = failure.getCode();
    if (code < REFUSAL_BAND_MIN || code > REFUSAL_BAND_MAX) {
      return false;
    }

    RefusalCollector.current().record(new SkippedPath(operation, path, failure.getRawMessage(), code));
    System.out.println(operation.getRefusalPrefix() + path);
    return true;
  }
}
