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
package org.eclipse.stardust.engine.cli.console;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.utils.console.ConsoleCommand;
import org.eclipse.stardust.common.utils.console.Options;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.AuditTrailHealthReport;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class DashboardCommand extends ConsoleCommand
{
   private static final String SHOW_OIDS = "showOids";

   private static final Options argTypes = new Options();

   private static final String OVERVIEW = "overview";
   private static final String RECOVERY = "recovery";

   private static final String XML = "xml";

   static
   {
      argTypes.register("-" + OVERVIEW, "-o", OVERVIEW,
            "Evaluates some overall indicators (default option).", false);
      argTypes.register("-" + RECOVERY, "-r", RECOVERY,
            "Evaluates process recovery related indicators.", false);
      argTypes.register("-" + SHOW_OIDS, "-so", SHOW_OIDS,
            "Shows a list of process instances oids of -recovery option result.", false);
   }

   public Options getOptions()
   {
      return argTypes;
   }

   public int run(Map options)
   {
      ServiceFactory serviceFactory = ServiceFactoryLocator.get(globalOptions);
      
      QueryService qs = serviceFactory.getQueryService();
      
      if (options.containsKey(OVERVIEW)
            || !(options.containsKey(OVERVIEW) || options.containsKey(RECOVERY)))
      {
         print("");
         print("Overview");
         print("");
         
         print(MessageFormat.format("Processes (total):\t{0}", new Object[] {new Long(
               qs.getProcessInstancesCount(ProcessInstanceQuery.findAll()))}));
         
         print(MessageFormat.format("Processes (alive):\t{0}", new Object[] {new Long(
               qs.getProcessInstancesCount(ProcessInstanceQuery.findAlive()))}));
         
         print(MessageFormat.format("Processes (completed):\t{0}", new Object[] {new Long(
               qs.getProcessInstancesCount(ProcessInstanceQuery.findCompleted()))}));
         
         print(MessageFormat.format("Processes (aborted):\t{0}", new Object[] {new Long(
               qs.getProcessInstancesCount(ProcessInstanceQuery.findInState(ProcessInstanceState.Aborted)))}));
         
         print("");
         
         print(MessageFormat.format("Activities (total):\t{0}", new Object[] {new Long(
               qs.getActivityInstancesCount(ActivityInstanceQuery.findAll()))}));
         
         print(MessageFormat.format("Activities (alive):\t{0}", new Object[] {new Long(
               qs.getActivityInstancesCount(ActivityInstanceQuery.findAlive()))}));
         
         print(MessageFormat.format("Activities (completed):\t{0}", new Object[] {new Long(
               qs.getActivityInstancesCount(ActivityInstanceQuery.findCompleted()))}));
         
         print(MessageFormat.format("Activities (aborted):\t{0}", new Object[] {new Long(
               qs.getActivityInstancesCount(ActivityInstanceQuery.findInState(ActivityInstanceState.Aborted)))}));
         
         print("");
         
         print(MessageFormat.format("Users (total):\t\t{0}", new Object[] {new Long(
               qs.getUsersCount(UserQuery.findAll()))}));
         
         print(MessageFormat.format("Users (active):\t\t{0}", new Object[] {new Long(
               qs.getUsersCount(UserQuery.findActive()))}));
         
         print("");
      }
      
      if (options.containsKey(RECOVERY))
      {
         print("");
         print("Process Recovery Indicators");
         print("");
         
         print(MessageFormat.format("Interrupted process instances:\t{0}", new Object[] {new Long(
               qs.getProcessInstancesCount(ProcessInstanceQuery.findInterrupted()))}));
         
         print(MessageFormat.format("Interrupted activity instances:\t{0}", new Object[] {new Long(
               qs.getActivityInstancesCount(ActivityInstanceQuery.findInState(ActivityInstanceState.Interrupted)))}));
         
         print("");
         
         boolean countOnly = !options.containsKey(SHOW_OIDS);
         AuditTrailHealthReport report = serviceFactory.getAdministrationService().getAuditTrailHealthReport(countOnly);
         if(countOnly) 
         {
            print("Number of process instances likely to have ..");
            print(MessageFormat.format(" .. pending process completion:\t{0}", new Object[] {new Long(
                  report.getNumberOfProcessInstancesLackingCompletion())}));
            print(MessageFormat.format(" .. pending process abortion:\t{0}", new Object[] {new Long(
                  report.getNumberOfProcessInstancesLackingAbortion())}));
            print(MessageFormat.format(" .. pending activity abortion:\t{0}", new Object[] {new Long(
                  report.getNumberOfActivityInstancesLackingAbortion())}));
            print(MessageFormat.format(" .. crashed activity instances:\t{0}", new Object[] {new Long(
                  report.getNumberOfProcessInstancesHavingCrashedActivities())}));
            print(MessageFormat.format(" .. crashed activity threads:\t{0}", new Object[] {new Long(
                  report.getNumberOfProcessInstancesHavingCrashedThreads())}));
         }
         else 
         {
            print("List of process instances oids likely to have ..");
            print(MessageFormat.format(" .. pending process completion:\t{0}", 
                  getOids(report.getProcessInstancesLackingCompletion())));
            print(MessageFormat.format(" .. pending process abortion:\t{0}", getOids(
                  report.getProcessInstancesLackingAbortion())));
            print(MessageFormat.format(" .. pending activity abortion:\t{0}", getOids(
                  report.getActivityInstancesLackingAbortion())));
            print(MessageFormat.format(" .. crashed activity instances:\t{0}", getOids(
                  report.getProcessInstancesHavingCrashedActivities())));
            print(MessageFormat.format(" .. crashed activity threads:\t{0}", getOids(
                  report.getProcessInstancesHavingCrashedThreads())));
         }
         
         print("");
      }

      return 0;
   }

   private String getOids(Set<Long> oidSet)
   {
      StringBuilder oids = new StringBuilder();
      for (Iterator iterator = oidSet.iterator(); iterator.hasNext();)
      {
         Long oid = (Long) iterator.next();
         oids.append(oid);
         if (iterator.hasNext())
         {
            oids.append(", ");
         }
      }
      return oidSet.isEmpty() ? "none" : oids.toString();
   }

   public String getSummary()
   {
      return "Retrieves health and performance indicators from a runtime\n"
            + "environment.";
   }
}
