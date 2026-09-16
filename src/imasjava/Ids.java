package imasjava;

import java.util.Collections;
import java.util.List;

/**
 * Base class of every generated IDS, holding the operation record and the
 * partial outcome of its last root operation.
 *
 * <p>A root operation ({@code get}, {@code getSlice}, {@code put},
 * {@code putSlice} or {@code delete}) still returns {@code void} and still
 * reports failure by throwing {@link ALException}. A completed root
 * operation that absorbed at least one tolerated refusal is reported here
 * instead: {@link #getSkippedPaths()} names every path that was skipped and
 * {@link #getOutcome()} says whether the operation was a {@link #PARTIAL_READ}
 * or a {@link #PARTIAL_PUT}. A completed root operation that skipped nothing
 * leaves both {@link #getSkippedPaths()} empty and {@link #getOutcome()}
 * {@link #CLEAN}.
 *
 * <p>The record describes only the last root operation run against this
 * IDS: it starts empty before any operation, does not accumulate across
 * calls, and is replaced in full by the next one.
 */
public class Ids
{
   /** Outcome of a root operation that completed with nothing skipped. */
   public static final int CLEAN = 0;

   /** Outcome of a {@code get} or {@code getSlice} that skipped at least one path. */
   public static final int PARTIAL_READ = 1;

   /** Outcome of a {@code put}, {@code putSlice} or {@code delete} that skipped at least one path. */
   public static final int PARTIAL_PUT = 2;

   private List<SkippedPath> skippedPaths = Collections.emptyList();
   private int               outcome      = CLEAN;

   /**
    * Opens a collector for the duration of a root operation about to run
    * against this IDS. Must be paired with {@link #endRootOperation(int)}
    * in a {@code finally}.
    */
   protected final void beginRootOperation()
   {
      RefusalCollector.open();
   }

   /**
    * Closes the collector opened by {@link #beginRootOperation()} and takes
    * this IDS's own copy of what it recorded, replacing whatever this IDS
    * held from an earlier root operation.
    *
    * <p>A root operation that did not complete reports failure by throwing,
    * so it leaves no partial outcome behind: its record is replaced with an
    * empty one and its outcome with {@link #CLEAN}. Anything a tolerant
    * site absorbed before the operation failed is discarded with it,
    * keeping "an {@link ALException} is failure, a non-empty record is
    * partial" true of every operation.
    *
    * @param partialOutcome the outcome to report when the operation
    *                       completed and skipped at least one path:
    *                       {@link #PARTIAL_READ} for a read,
    *                       {@link #PARTIAL_PUT} for a write or delete
    * @param completed      {@code true} when the root operation ran to the
    *                       end, {@code false} when it is unwinding
    */
   protected final void endRootOperation(int partialOutcome, boolean completed)
   {
      List<SkippedPath> recorded = RefusalCollector.current().getSkippedPaths();
      RefusalCollector.close();
      this.skippedPaths = completed ? recorded : Collections.<SkippedPath>emptyList();
      this.outcome = (completed && !recorded.isEmpty()) ? partialOutcome : CLEAN;
   }

   /**
    * Restores a previously captured operation record, without going
    * through the collector. Used by {@code serialize}/{@code deserialize},
    * which run a root operation internally against an in-memory pulse and
    * must not let it overwrite the record of the root operation the caller
    * actually asked for.
    *
    * @param skippedPaths the skipped paths to restore
    * @param outcome      the outcome to restore
    */
   protected final void restoreOperationRecord(List<SkippedPath> skippedPaths, int outcome)
   {
      this.skippedPaths = skippedPaths;
      this.outcome = outcome;
   }

   /**
    * @return the paths skipped by this IDS's last root operation, in the
    *         order they were skipped; empty if that operation completed
    *         cleanly
    */
   public final List<SkippedPath> getSkippedPaths()
   {
      return skippedPaths;
   }

   /**
    * @return the number of paths skipped by this IDS's last root
    *         operation; always equal to {@code getSkippedPaths().size()}
    */
   public final int getSkippedPathCount()
   {
      return skippedPaths.size();
   }

   /**
    * @return {@link #CLEAN}, {@link #PARTIAL_READ} or {@link #PARTIAL_PUT},
    *         describing this IDS's last root operation
    */
   public final int getOutcome()
   {
      return outcome;
   }

   /**
    * @return {@code true} if this IDS's last root operation skipped at
    *         least one path
    */
   public final boolean isPartial()
   {
      return outcome != CLEAN;
   }
}
