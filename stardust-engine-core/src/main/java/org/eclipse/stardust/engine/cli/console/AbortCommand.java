package org.eclipse.stardust.engine.cli.console;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.utils.console.ConsoleCommand;
import org.eclipse.stardust.common.utils.console.Options;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes;
import org.eclipse.stardust.engine.api.ejb2.ServiceFactoryLocator;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.AbortScope;

public class AbortCommand extends ConsoleCommand
{

   private static final Options argTypes = new Options();

   private static final String PROCESS_INSTANCE = "processInstance";

   private static final String ACTIVITY_INSTANCE = "activityInstance";

   private static final String ABORT_SCOPE = "abortScope";

   private static final String ABORT_SCOPE_ROOT_STRING = "root";

   private static final String ABORT_SCOPE_SUB_STRING = "sub";

   private static final String INFO = "info";

   static
   {
      argTypes.register("-processInstance", "-pi", PROCESS_INSTANCE,
            "Abort process instance with the given OID.", true);

      argTypes.register("-activityInstance", "-ai", ACTIVITY_INSTANCE,
            "Abort activity instance with the given OID", true);

      argTypes.register("-scope", "-sc", ABORT_SCOPE,
            "Defines the abort scope ('root'/'sub')", true);

      argTypes.register("-info", "-i", INFO, "Add a note to the aborted instance", true);

   }

   @Override
   public Options getOptions()
   {
      return argTypes;
   }

   @Override
   public int run(Map options)
   {
      ServiceFactory serviceFactory = ServiceFactoryLocator.get(globalOptions);

      print(options.get(INFO).toString());

      AbortScope scope = AbortScope.RootHierarchy;

      if (options.containsKey(ABORT_SCOPE))
      {
         if (options.get(ABORT_SCOPE)
               .toString()
               .equalsIgnoreCase(ABORT_SCOPE_ROOT_STRING))
         {
            scope = AbortScope.RootHierarchy;
         }
         else if (options.get(ABORT_SCOPE)
               .toString()
               .equalsIgnoreCase(ABORT_SCOPE_SUB_STRING))
         {
            scope = AbortScope.SubHierarchy;
         }
      }
      else
      {
         print("Abort scope must be provided!");
         return -1;
      }

      try
      {
         if (options.containsKey(PROCESS_INSTANCE))
         {
            List<Long> oids = splitOids((String) options.get(PROCESS_INSTANCE));

            for (Long piOID : oids)
            {

               // Add comment to PI
               if (options.containsKey(INFO))
               {
                  ProcessInstanceAttributes attrib = serviceFactory.getWorkflowService()
                        .getProcessInstance(piOID)
                        .getAttributes();

                  attrib.addNote((String) options.get(INFO));
                  serviceFactory.getWorkflowService()
                        .setProcessInstanceAttributes(attrib);
               }
               serviceFactory.getWorkflowService().abortProcessInstance(piOID, scope);
               print("Process Instance with OID " + piOID + " has been aborted");
            }
         }
         else if (options.containsKey(ACTIVITY_INSTANCE))
         {
            List<Long> oids = splitOids((String) options.get(ACTIVITY_INSTANCE));

            for (Long aiOID : oids)
            {

               // Add comment to PI
               if (options.containsKey(INFO))
               {
                  ProcessInstanceAttributes attrib = serviceFactory.getWorkflowService()
                        .getProcessInstance(
                              serviceFactory.getWorkflowService()
                                    .getActivityInstance(aiOID)
                                    .getOID())
                        .getAttributes();
                  
                  attrib.addNote((String) options.get(INFO));
                  serviceFactory.getWorkflowService()
                        .setProcessInstanceAttributes(attrib);                  
               }
               serviceFactory.getWorkflowService().abortActivityInstance(aiOID, scope);
               print("Activitiy Instance with OID " + aiOID + " has been aborted");
            }
         }
         else
         {
            print("Instances OIDs must be provided!");
            return -1;
         }
      }
      finally
      {
         serviceFactory.close();
      }
      return 0;
   }

   @Override
   public String getSummary()
   {
      return "Aborts the process/activity instances with the given OIDs.";
   }

   /*
    * write comma separated list of oids to list
    */
   private List<Long> splitOids(String oidString)
   {
      List<Long> oidList = CollectionUtils.newArrayList();
      List<String> elements = Arrays.asList(oidString.split(","));

      for (String element : elements)
      {
         oidList.add(Long.parseLong(element));
      }

      return oidList;
   }

}
