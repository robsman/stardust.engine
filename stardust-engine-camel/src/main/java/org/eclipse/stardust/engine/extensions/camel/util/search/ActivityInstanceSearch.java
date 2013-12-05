package org.eclipse.stardust.engine.extensions.camel.util.search;

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.ProcessInstanceFilter;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.QueryService;

public final class ActivityInstanceSearch
{

   /**
    * Returns all ActivityInstances in state Suspended or Hibernated for the given process instance.
    * 
    * @param qService
    * @param processInstanceOid
    * @return Suspended or Hibernated ActivityInstances relative to the processInstance
    */
   public static ActivityInstances findWaitingForProcessInstance(QueryService qService, long processInstanceOid)
   {
      ActivityInstanceQuery query = ActivityInstanceQuery.findInState(new ActivityInstanceState[] {
            ActivityInstanceState.Suspended, ActivityInstanceState.Hibernated});
      query.where(new ProcessInstanceFilter(processInstanceOid));
      return qService.getAllActivityInstances(query);
   }
}
