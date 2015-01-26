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
 * @author rsauer
 * @version $Revision$
 */
public class ActivityStatisticsQuery extends CustomActivityInstanceQuery
{
   static final long serialVersionUID = 8790093503685888481L;
   
   public static final String ID = ActivityStatisticsQuery.class.getName();

   public static ActivityStatisticsQuery forAllProcesses()
   {
      return new ActivityStatisticsQuery();
   }

   public static ActivityStatisticsQuery forProcesses(
         ProcessDefinition process)
   {
      return forProcesses(Collections.singleton(process));
   }
   
   public static ActivityStatisticsQuery forProcesses(
         Set<ProcessDefinition> processes)
   {
      Set<String> processIds = CollectionUtils.newSet();
      for (Iterator<ProcessDefinition> i = processes.iterator(); i.hasNext();)
      {
         ProcessDefinition process = i.next();
         processIds.add(process.getQualifiedId());
      }

      return forProcessIds(processIds);
   }

   public static ActivityStatisticsQuery forProcessId(String processId)
   {
      return forProcessIds(Collections.singleton(processId));
   }
   
   public static ActivityStatisticsQuery forProcessIds(Set<String> processIds)
   {
      ActivityStatisticsQuery query = new ActivityStatisticsQuery();

      FilterOrTerm processFilter = query.getFilter().addOrTerm();

      for (Iterator<String> i = processIds.iterator(); i.hasNext();)
      {
         String processId = i.next();

         processFilter.add(new ProcessDefinitionFilter(processId, false));
      }

      return query;
   }

   protected ActivityStatisticsQuery()
   {
      super(ID);
   }
}