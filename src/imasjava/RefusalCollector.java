package imasjava;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * The record open for the duration of one root operation, which tolerant
 * sites write {@link SkippedPath skipped paths} into.
 *
 * <p>The collector is per-thread, so two root operations running
 * concurrently on different threads never see each other's skipped paths.
 * {@link #current()} returns the calling thread's collector.
 *
 * <p>A root operation calls {@link #open()} for its duration and
 * {@link #close()} in a {@code finally}, then takes its own copy of
 * {@link #getSkippedPaths()} onto the IDS it ran against. A root operation
 * that calls another internally (such as {@code put}'s own preparatory
 * {@code delete}) opens a nested collector; on {@link #close()} its skipped
 * paths are folded into the enclosing one, so the outer root operation's
 * copy still reflects everything absorbed during its own duration.
 *
 * <p>Outside of any open root operation, {@link #current()} falls back to
 * an accumulating per-thread collector, so a tolerant site invoked with no
 * root operation in progress still has somewhere to record.
 */
public final class RefusalCollector {
  private static final ThreadLocal<RefusalCollector> FALLBACK = new ThreadLocal<RefusalCollector>() {
    @Override
    protected RefusalCollector initialValue() {
      return new RefusalCollector();
    }
  };

  private static final ThreadLocal<Deque<RefusalCollector>> STACK = new ThreadLocal<Deque<RefusalCollector>>() {
    @Override
    protected Deque<RefusalCollector> initialValue() {
      return new ArrayDeque<RefusalCollector>();
    }
  };

  private final List<SkippedPath> skippedPaths = new ArrayList<SkippedPath>();

  private RefusalCollector() {
  }

  /**
   * Opens a fresh collector for the duration of one root operation. Must
   * be paired with a {@link #close()} in a {@code finally}.
   *
   * @return the newly opened collector
   */
  public static RefusalCollector open() {
    RefusalCollector collector = new RefusalCollector();
    STACK.get().push(collector);
    return collector;
  }

  /**
   * Closes the collector most recently opened on the calling thread,
   * folding its skipped paths into the collector it was nested in, if
   * any.
   */
  public static void close() {
    Deque<RefusalCollector> stack = STACK.get();
    RefusalCollector finished = stack.pop();
    RefusalCollector parent = stack.peek();
    if (parent != null) {
      parent.skippedPaths.addAll(finished.skippedPaths);
    }
  }

  /**
   * @return the calling thread's currently open collector, or a
   *         per-thread fallback when no root operation is in progress
   */
  public static RefusalCollector current() {
    Deque<RefusalCollector> stack = STACK.get();
    RefusalCollector top = stack.peek();
    return top != null ? top : FALLBACK.get();
  }

  /**
   * Records a skipped path absorbed by a tolerant site.
   *
   * @param skippedPath the skipped path to record
   */
  public void record(SkippedPath skippedPath) {
    skippedPaths.add(skippedPath);
  }

  /**
   * @return the skipped paths recorded so far in this collector, in the
   *         order they were recorded
   */
  public List<SkippedPath> getSkippedPaths() {
    return Collections.unmodifiableList(new ArrayList<SkippedPath>(skippedPaths));
  }
}
