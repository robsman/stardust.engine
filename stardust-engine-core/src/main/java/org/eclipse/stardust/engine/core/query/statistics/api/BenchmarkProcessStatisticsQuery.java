/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.query.statistics.api;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionFilter;
import org.eclipse.stardust.engine.core.spi.query.CustomProcessInstanceQuery;


/**
 * @author roland.stamm
 * @version $Revision$
 */
public class BenchmarkProcessStatisticsQuery extends CustomProcessInstanceQuery
{
   static final long serialVersionUID = -4101345299290353466L;

   public static final String ID = BenchmarkProcessStatisticsQuery.class.getName();

   public static BenchmarkProcessStatisticsQuery forAllProcesses()
   {
      return new BenchmarkProcessStatisticsQuery();
   }

   public static BenchmarkProcessStatisticsQuery forProcesses(
         Set<ProcessDefinition> processes)
   {
      BenchmarkProcessStatisticsQuery query = new BenchmarkProcessStatisticsQuery();

      FilterOrTerm processFilter = query.getFilter().addOrTerm();

      for (Iterator<ProcessDefinition> i = processes.iterator(); i.hasNext();)
      {
         ProcessDefinition process = i.next();

         processFilter.add(PROCESS_DEFINITION_OID.isEqual(process.getRuntimeElementOID()));
      }

      return query;
   }

   public static BenchmarkProcessStatisticsQuery forProcessIds(Set<String> processes)
   {
      BenchmarkProcessStatisticsQuery query = new BenchmarkProcessStatisticsQuery();

      FilterOrTerm processFilter = query.getFilter().addOrTerm();

      for (Iterator<String> i = processes.iterator(); i.hasNext();)
      {
         String processId = (String) i.next();

         processFilter.add(new ProcessDefinitionFilter(processId, false));
      }

      return query;
   }

   protected BenchmarkProcessStatisticsQuery()
   {
      super(ID);
   }

}
