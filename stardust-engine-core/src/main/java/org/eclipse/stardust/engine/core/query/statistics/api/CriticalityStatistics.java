package org.eclipse.stardust.engine.core.query.statistics.api;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.core.spi.query.CustomActivityInstanceQueryResult;

/**
 * 
 * @author thomas.wolfram
 * 
 */
public abstract class CriticalityStatistics extends CustomActivityInstanceQueryResult
{
   protected Map<String, ProcessEntry> processEntries;

   private static final long serialVersionUID = -2048179479880011373L;

   protected CriticalityStatistics(CriticalityStatisticsQuery query)
   {
      super(query);

      this.processEntries = CollectionUtils.newMap();

   }

   /**
    * Retrieves the criticality statistics for a particular activity
    * 
    * @param processId
    *           full qualified process defintion id
    * @param activityId
    *           full qualified activity id
    * @return {@link IActivityEntry}
    */
   public IActivityEntry getStatisiticsForActivity(String processId, String activityId)
   {
      ProcessEntry processEntry = (ProcessEntry) processEntries.get(processId);

      return (null != processEntry) ? processEntry.getForActivity(activityId) : null;
   }

   /**
    * Retrieves the criticality statistics for a particular process definition
    * 
    * @param processId
    *           full qualified process defintion id
    * @return {@link IProcessEntry}
    */
   public IProcessEntry getStatisitcsForProcess(String processId)
   {
      ProcessEntry processEntry = (ProcessEntry) processEntries.get(processId);

      return (null != processEntry) ? processEntry : null;
   }

   public interface IProcessEntry
   {
      /**
       * Returns the process ID for the process entry
       * 
       * @return {@link String}
       */
      String getProcessId();

      /**
       * Returns the cumulated number of all activity instances within the process having
       * the priority range given in the query
       * 
       * @return {@link Long}
       */
      long getCumulatedInstances();
   }

   public interface IActivityEntry
   {

      /**
       * Returns the corresponding process id for this activity entry
       * 
       * @return {@link String}
       */
      String getProcessId();

      /**
       * Returns the ID of the activity for this activity entry
       * 
       * @return {@link String}
       */
      String getActivityId();

      /**
       * Returns the cumulated number activity instances of this particular activity
       * having the priority range given in the query
       * 
       * @return {@link Long}
       */
      long getInstancesCount();

      /**
       * Returns a set of all activity instances OIDs for this particular activity having
       * the priority range given in the query
       * 
       * @return {@link SortedSet}
       */
      SortedSet<Long> getInstances();
   }

   /**
    * Class to hold statistics information for a particular process
    * 
    * @author thomas.wolfram
    * 
    */
   protected static class ProcessEntry implements Serializable, IProcessEntry
   {

      private static final long serialVersionUID = 5921907918366529618L;

      public final String processId;

      public final Map<String, ActivityEntry> activityEntries = CollectionUtils.newMap();

      public ProcessEntry(String processId)
      {
         this.processId = processId;
      }

      public ActivityEntry getForActivity(String activityId)
      {
         return (ActivityEntry) activityEntries.get(activityId);
      }

      public long getCumulatedInstances()
      {
         long instancesCounter = 0l;
         Iterator<String> keys = activityEntries.keySet().iterator();
         while (keys.hasNext())
         {
            ActivityEntry entry = activityEntries.get(keys.next());
            instancesCounter += entry.getInstancesCount();
         }
         return instancesCounter;
      }

      public String getProcessId()
      {
         return processId;
      }
   }

   /**
    * Class to hold statistics information for a particular activity
    * 
    * @author thomas.wolfram
    * 
    */
   protected static class ActivityEntry implements Serializable, IActivityEntry
   {
      private SortedSet<Long> instances;

      private static final long serialVersionUID = 6776569277456335430L;

      public final String processId;

      public final String activityId;

      public ActivityEntry(String processId, String activityId)
      {
         this.processId = processId;
         this.activityId = activityId;
      }

      public String getProcessId()
      {
         return processId;
      }

      public String getActivityId()
      {
         return activityId;
      }

      public long getInstancesCount()
      {
         if (null != instances)
         {
            return instances.size();
         }
         return 0;
      }

      public void registerInstance(long instanceOid)
      {
         if (instances == null)
         {
            instances = new TreeSet<Long>();
         }
         instances.add(instanceOid);
      }

      public SortedSet<Long> getInstances()
      {
         return instances;
      }

   }

}
