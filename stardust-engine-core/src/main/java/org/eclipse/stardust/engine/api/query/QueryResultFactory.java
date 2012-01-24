/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.query;

import java.util.List;

import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.LogEntry;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserGroup;


/**
 * @author rsauer
 * @version $Revision$
 */
public abstract class QueryResultFactory
{
   public static final LogEntries createLogEntryQueryResult(LogEntryQuery query,
         RawQueryResult<LogEntry> result)
   {
      return new LogEntries(query, result);
   }

   public static final Users createUserQueryResult(UserQuery query, RawQueryResult<User> result)
   {
      return new Users(query, result);
   }

   public static final UserGroups createUserGroupQueryResult(UserGroupQuery query, RawQueryResult<UserGroup> result)
   {
      return new UserGroups(query, result);
   }
   
   public static final ActivityInstances createActivityInstancesQueryResult(ActivityInstanceQuery query, RawQueryResult<ActivityInstance> result)
   {
      return new ActivityInstances(query, result);
   }
   
   public static final ProcessInstances createProcessInstancesQueryResult(ProcessInstanceQuery query, RawQueryResult<ProcessInstance> result)
   {
      return new ProcessInstances(query, result);
   }
   
   public static final UserWorklist createUserWorklistQueryResult(User owner, WorklistQuery query, SubsetPolicy subset, List<ActivityInstance> items,
         boolean moreAvailable, List<ParticipantWorklist> subDetails, Long totalCount)
   {
      return new UserWorklist(owner, query, subset, items, moreAvailable, subDetails, totalCount);
   }
   
   public static final ParticipantWorklist createParticipantWorklistQueryResult(String ownerId, WorklistQuery query, SubsetPolicy subset,
         List<ActivityInstance> items, boolean moreAvailable, Long totalCount)
   {
      return new ParticipantWorklist(ownerId, query, subset, items, moreAvailable, totalCount);
   }
}
