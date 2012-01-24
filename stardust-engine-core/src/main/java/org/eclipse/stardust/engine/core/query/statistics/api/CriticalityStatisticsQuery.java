package org.eclipse.stardust.engine.core.query.statistics.api;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionFilter;
import org.eclipse.stardust.engine.core.spi.query.CustomActivityInstanceQuery;

/**
 * 
 * @author thomas.wolfram
 * 
 */
public class CriticalityStatisticsQuery extends CustomActivityInstanceQuery
{

   private static final long serialVersionUID = 3262660097100514709L;

   public static final String ID = CriticalityStatisticsQuery.class.getName();

   public static CriticalityStatisticsQuery forProcesses(ProcessDefinition process)
   {
      return forProcesses(Collections.singleton(process));
   }

   public static CriticalityStatisticsQuery forProcesses(Set<ProcessDefinition> processes)
   {
      Set<String> processIds = CollectionUtils.newSet();
      for (Iterator<ProcessDefinition> i = processes.iterator(); i.hasNext();)
      {
         ProcessDefinition process = i.next();
         processIds.add(process.getQualifiedId());
      }
      return forProcessIds(processIds);
   }

   public static CriticalityStatisticsQuery forProcessIds(String processId)
   {
      return forProcessIds(Collections.singleton(processId));
   }

   public static CriticalityStatisticsQuery forProcessIds(Set<String> processIds)
   {
      CriticalityStatisticsQuery query = new CriticalityStatisticsQuery();

      FilterOrTerm processFilter = query.getFilter().addOrTerm();

      for (Iterator<String> i = processIds.iterator(); i.hasNext();)
      {
         String processId = i.next();

         processFilter.add(new ProcessDefinitionFilter(processId, false));
      }

      return query;
   }

   protected CriticalityStatisticsQuery()
   {
      super(ID);
   }

}
