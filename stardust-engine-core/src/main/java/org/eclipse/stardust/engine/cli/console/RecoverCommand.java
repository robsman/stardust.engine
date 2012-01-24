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
import java.util.*;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.utils.console.ConsoleCommand;
import org.eclipse.stardust.common.utils.console.IllegalUsageException;
import org.eclipse.stardust.common.utils.console.Options;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class RecoverCommand extends ConsoleCommand
{
   private static final Options argTypes = new Options();

   private static final String PROCESSES = "processes";
   private static final String ALL = "all";
   private static final String BATCH_SIZE = "batchSize";
   private static final String MAX = "max";
   private static final String STARTED_AFTER = "startedAfter";
   private static final String STARTED_BEFORE = "startedBefore";
   private static final String QUICK = "quick";

   static
   {
      argTypes.register("-" + ALL, "-a", ALL,
            "Recovers all process instances of the audit trail.", false);
      argTypes.register("-" + QUICK, "-q", QUICK,
            "Performs a quick recovery of the audit trail, which effectively just\n"
            + "considers INTERRUPTED and ABORTING process instances.", false);
      argTypes.register("-" + PROCESSES, "-p", PROCESSES,
            "Recovers the specified process instances (comma separated list of OIDs).", true);
      argTypes.register("-" + MAX, "-m", MAX,
            "Recovers at most the given number of process instances.\n"
            + "Might be used for both full and quick recovery.", true);
      argTypes.register("-" + BATCH_SIZE, "-b", BATCH_SIZE,
            "Performs recovery of process instances in controlled batches. If this\n"
            + "option is missing, all qualifying process instances are recovered in\n"
            + "one big transaction.\n"
            + "Might be used for both full and quick recovery.", true);
      argTypes.register("-" + STARTED_AFTER, Options.NO_SHORTNAME, STARTED_AFTER,
            "Recovers only process instances started after the given date.\n"
            + "Might be used for both full and quick recovery.\n"
            + "The specified date must conforms to ISO date patterns\n"
            + "(i.e. \"2005-10-01\",\"2005-10-01 08:30\" or \"2006-01-01T08:30:00:000\").", true);
      argTypes.register("-" + STARTED_BEFORE, Options.NO_SHORTNAME, STARTED_BEFORE,
            "Recovers only process instances started before the given date.\n"
            + "Might be used for both full and quick recovery.\n"
            + "The specified date must conforms to ISO date patterns\n"
            + "(i.e. \"2005-12-31\", \"2005-12-31 23:59\" or \"2005-12-31T23:59:59:999\").", true);
      argTypes.addExclusionRule(new String[] {ALL, PROCESSES, QUICK}, true);
      argTypes.addExclusionRule(new String[] {PROCESSES, MAX}, false);
      argTypes.addExclusionRule(new String[] {PROCESSES, BATCH_SIZE}, false);
      argTypes.addExclusionRule(new String[] {PROCESSES, STARTED_BEFORE}, false);
      argTypes.addExclusionRule(new String[] {PROCESSES, STARTED_AFTER}, false);
   }

   public Options getOptions()
   {
      return argTypes;
   }

   public int run(Map options)
   {
      if (options.containsKey(ALL) || options.containsKey(QUICK))
      {
         if (!force() && !confirm(
               "You are going to recover the Infinity Runtime Environment. Continue?: "))
         {
            return -1;
         }

         ServiceFactory serviceFactory = ServiceFactoryLocator.get(globalOptions);

         try
         {
            if (options.containsKey(QUICK) || options.containsKey(MAX)
                  || options.containsKey(BATCH_SIZE)
                  || options.containsKey(STARTED_BEFORE)
                  || options.containsKey(STARTED_AFTER))
            {
               // query for OIDs of qualifying process instances
               
               ProcessInstanceQuery query;
               if (options.containsKey(QUICK))
               {
                  query = ProcessInstanceQuery
                        .findInState(new ProcessInstanceState[] {
                              ProcessInstanceState.Interrupted,
                              ProcessInstanceState.Aborting });
               }
               else
               {
                  query = ProcessInstanceQuery.findInState(new ProcessInstanceState[] {
                        ProcessInstanceState.Active, ProcessInstanceState.Interrupted,
                        ProcessInstanceState.Aborting });
               }
               
               if (options.containsKey(MAX))
               {
                  query.setPolicy(new SubsetPolicy(Options.getLongValue(options, MAX)
                        .intValue()));
               }

               if (options.containsKey(STARTED_AFTER))
               {
                  Date startedAfter = Options.getDateValue(options, STARTED_AFTER);
                  if (null != startedAfter)
                  {
                     query.where(ProcessInstanceQuery.START_TIME.greaterOrEqual(startedAfter.getTime()));
                  }
                  else
                  {
                     print(MessageFormat.format(
                           "Unsupported value ''{1}'' for option -{0}.", new Object[] {
                                 STARTED_AFTER, options.get(STARTED_AFTER)}));
                     return -1;
                  }
               }
               if (options.containsKey(STARTED_BEFORE))
               {
                  Date startedBefore = Options.getDateValue(options, STARTED_BEFORE);
                  if (null != startedBefore)
                  {
                     query.where(ProcessInstanceQuery.START_TIME.lessOrEqual(startedBefore.getTime()));
                  }
                  else
                  {
                     print(MessageFormat.format(
                           "Unsupported value ''{1}'' for option -{0}.", new Object[] {
                                 STARTED_BEFORE, options.get(STARTED_BEFORE)}));
                     return -1;
                  }
               }
               
               ProcessInstances pis = serviceFactory.getQueryService()
                     .getAllProcessInstances(query);
               
               print(MessageFormat.format(
                     "Performing {0} recovery of {1} process instance(s).", new Object[] {
                           options.containsKey(QUICK) ? "a quick" : "a",
                           new Integer(pis.size())}));
               
               // perform recovery using batches of retrieved OIDs

               int batchSize = (int) pis.getSize();
               if (options.containsKey(BATCH_SIZE))
               {
                  batchSize = Options.getLongValue(options, BATCH_SIZE).intValue();
               }
               
               AdministrationService adminSrvc = serviceFactory.getAdministrationService();
               try
               {
                  List piOids = new ArrayList(batchSize);
                  for (Iterator i = pis.iterator(); i.hasNext();)
                  {
                     ProcessInstance pi = (ProcessInstance) i.next();

                     piOids.add(new Long(pi.getOID()));

                     if ((piOids.size() >= batchSize) || !i.hasNext())
                     {
                        // do recovery
                        if (batchSize < pis.getSize())
                        {
                           print(MessageFormat.format(
                                 "Recovering a batch of {0} process instance(s).",
                                 new Object[] {new Integer(piOids.size())}));
                        }
                        adminSrvc.recoverProcessInstances(piOids);
                        piOids.clear();
                     }
                  }
               }
               finally
               {
                  serviceFactory.release(adminSrvc);
               }               
            }
            else
            {
               serviceFactory.getAdministrationService().recoverRuntimeEnvironment();
            }
         }
         finally
         {
            serviceFactory.close();
         }

         print("Runtime environment recovered.");
         return 0;
      }
      else
      {
         List processes = Collections.EMPTY_LIST;
         try
         {
            processes = Options.getLongValues(options, PROCESSES);
         }
         catch (PublicException pe)
         {
            throw new IllegalUsageException(pe.getMessage());
         }
         if ( !force()
               && !confirm("You are going to recover processes from the Runtime Environment. Continue?: "))
         {
            return -1;
         }

         List errors = CollectionUtils.newList();
         ServiceFactory serviceFactory = ServiceFactoryLocator.get(globalOptions);
         try
         {
            for (Iterator i = processes.iterator(); i.hasNext();)
            {
               Number oid = (Number) i.next();
               try
               {
                  serviceFactory.getAdministrationService().recoverProcessInstance(
                        oid.longValue());
               }
               catch (ObjectNotFoundException e)
               {
                  errors.add(e.getMessage());
               }
            }
         }
         finally
         {
            serviceFactory.close();
         }

         if (errors.isEmpty())
         {
            print("Processes recovered.");
            return 0;
         }
         else
         {
            print("Not all processes could be recovered: ");
            for (Iterator i = errors.iterator(); i.hasNext();)
            {
               print("  " + i.next());
            }
            return -1;
         }
      }
   }

   public String getSummary()
   {
      return "Recovers process instances.";
   }
}
