package org.eclipse.stardust.engine.core.query.statistics.evaluation;


import org.eclipse.stardust.engine.core.query.statistics.api.CriticalityStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.CriticalityStatisticsQuery;

/**
 * 
 * @author thomas.wolfram
 *
 */
public class CriticalityStatisticsResult extends CriticalityStatistics
{

   private static final long serialVersionUID = -514346654886122348L;

   public CriticalityStatisticsResult(CriticalityStatisticsQuery query)
   {
      super(query);
   }
   
   public void addInstances(String processId, String activityId, double criticality, long aiOid)
   {
      ProcessEntry processEntry = (ProcessEntry) processEntries.get(processId);
      if (null == processEntry)
      {
         processEntry = new ProcessEntry(processId);
         processEntries.put(processId, processEntry);
      }

      ActivityEntry activityEntry = processEntry.getForActivity(activityId);
      if (null == activityEntry)
      {
         activityEntry = new ActivityEntry(processId, activityId);
         processEntry.activityEntries.put(activityId, activityEntry);
      }      
      
      activityEntry.registerInstance(aiOid);
   }   

}
