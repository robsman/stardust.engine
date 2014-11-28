package org.eclipse.stardust.engine.core.query.statistics;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Pair;

/**
 * <p>
 * The class holding the processing time query results as a {@link Set} of {@link ProcessingTime}s.
 * </p>
 *
 * @author Nicolas.Werlein
 */
public class ProcessingTimes implements Serializable
{
   private static final long serialVersionUID = -4259445814904017303L;

   private final Set<ProcessingTime> processingTimes = CollectionUtils.newHashSet();

   /* package-private */ void addProcessingTime(final long oid, final long processingTime)
   {
      processingTimes.add(new ProcessingTime(oid, processingTime));
   }

   /**
    * @return the processing times
    */
   public Set<ProcessingTime> processingTimes()
   {
      return Collections.unmodifiableSet(processingTimes);
   }

   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return processingTimes.toString();
   }

   /**
    * <p>
    * The class holding the processing time for a particular activity/process instance.
    * </p>
    *
    * @author Nicolas.Werlein
    */
   public static final class ProcessingTime extends Pair<Long, Long>
   {
      private static final long serialVersionUID = 8628678559744677858L;

      /* package-private */ ProcessingTime(final long oid, final long processingTime)
      {
         super(Long.valueOf(oid), Long.valueOf(processingTime));
      }

      /**
       * @return the activity/process instance's OID
       */
      public long oid()
      {
         return getFirst();
      }

      /**
       * @return the processing time in ms
       */
      public long processingTime()
      {
         return getSecond();
      }
   }
}
