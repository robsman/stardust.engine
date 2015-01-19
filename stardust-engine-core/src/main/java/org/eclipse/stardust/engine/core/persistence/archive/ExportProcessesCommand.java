package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessElementExporter;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessElementPurger;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessElementsVisitor;
import org.eclipse.stardust.engine.core.runtime.command.ServiceCommand;

/**
 * This class allows a request to archive processes instances. The processes will be
 * exported to a byte[] and will be deleted from the database. If a process has
 * subprocesses the subprocesses will be exported and purged as well. If no valid
 * processInstanceOids are provided then null will be returned.
 * 
 * @author jsaayman
 * @version $Revision: $
 */
public class ExportProcessesCommand implements ServiceCommand
{
   private static final long serialVersionUID = 1L;

   private final List<Long> processInstanceOids;

   /**
    * @param processInstanceOids
    *           Oids of process instances to export
    */
   public ExportProcessesCommand(List<Long> processInstanceOids)
   {
      super();
      this.processInstanceOids = processInstanceOids;
   }

   @Override
   public Serializable execute(ServiceFactory sf)
   {

      byte[] result;
      if (CollectionUtils.isNotEmpty(processInstanceOids))
      {
         final Session session = (Session) SessionFactory
               .getSession(SessionFactory.AUDIT_TRAIL);

         QueryService queryService = sf.getQueryService();

         List<Long> uniqueOids = new ArrayList<Long>();
         for (Long oid : processInstanceOids)
         {
            if (oid != null && !uniqueOids.contains(oid))
            {
               // check that the oid is valid. if it is valid add it to export, further if
               // it has any subprocesses add them to the export as well
               ProcessInstanceQuery query = new ProcessInstanceQuery();
               query.where(ProcessInstanceQuery.ROOT_PROCESS_INSTANCE_OID.isEqual(oid));
               ProcessInstances processes = queryService.getAllProcessInstances(query);
               if (processes != null)
               {
                  for (ProcessInstance subProcess : processes)
                  {
                     if (!uniqueOids.contains(subProcess.getOID()))
                     {
                        uniqueOids.add(subProcess.getOID());
                     }
                  }
               }
            }
         }

         if (CollectionUtils.isNotEmpty(uniqueOids))
         {
            ProcessElementExporter exporter = new ProcessElementExporter();
            ProcessElementsVisitor processVisitor = new ProcessElementsVisitor(exporter);
            // export processInstances
            processVisitor.visitProcessInstances(uniqueOids, session);

            ProcessElementPurger purger = new ProcessElementPurger();
            processVisitor = new ProcessElementsVisitor(purger);
            // purge processInstances
            processVisitor.visitProcessInstances(uniqueOids, session);

            result = exporter.getBlob();
         }
         else
         {
            result = null;
         }
      }
      else
      {
         result = null;
      }
      return result;
   }
}
