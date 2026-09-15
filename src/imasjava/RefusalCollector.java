package imasjava;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The record open for the duration of one root operation, which tolerant
 * sites write {@link SkippedPath skipped paths} into.
 *
 * <p>The collector is per-thread, so two root operations running
 * concurrently on different threads never see each other's skipped paths.
 * {@link #current()} returns the calling thread's collector, creating it
 * on first use.
 *
 * <p>Opening a fresh collector at the start of a root operation and
 * handing its contents to the IDS at the end is not this class's
 * responsibility; today it accumulates for the lifetime of the thread.
 */
public final class RefusalCollector
{
   private static final ThreadLocal<RefusalCollector> PER_THREAD = new ThreadLocal<RefusalCollector>()
   {
      @Override
      protected RefusalCollector initialValue()
      {
         return new RefusalCollector();
      }
   };

   private final List<SkippedPath> skippedPaths = new ArrayList<SkippedPath>();

   private RefusalCollector()
   {
   }

   /**
    * @return the calling thread's collector
    */
   public static RefusalCollector current()
   {
      return PER_THREAD.get();
   }

   /**
    * Records a skipped path absorbed by a tolerant site.
    *
    * @param skippedPath the skipped path to record
    */
   public void record(SkippedPath skippedPath)
   {
      skippedPaths.add(skippedPath);
   }

   /**
    * @return the skipped paths recorded so far on the calling thread, in
    *         the order they were recorded
    */
   public List<SkippedPath> getSkippedPaths()
   {
      return Collections.unmodifiableList(new ArrayList<SkippedPath>(skippedPaths));
   }
}
