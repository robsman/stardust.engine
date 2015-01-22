package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
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
 * subprocesses the subprocesses will be exported and purged as well.
 * 
 * Processes can be exported:<br/>
 * <li/>completely (per partition) <li/>by root process instance OID <li/>by business
 * identifier (unique primitive key descriptor) <li/>by from/to filter (start time to termination time) <br/>
 * If no valid processInstanceOids are provided or derived from criteria then null will be
 * returned.
 * 
 * If a fromDate is provided, but no toDate then toDate defaults to now. If a toDate is
 * provided, but no fromDate then fromDate defaults to 1 January 1970. If a null fromDate
 * and toDate is provided then all processes will be exported.
 * 
 * @author jsaayman
 * @version $Revision: $
 */
public class ExportProcessesCommand implements ServiceCommand
{
   private static final long serialVersionUID = 1L;

   private static final Logger LOGGER = LogManager
         .getLogger(ExportProcessesCommand.class);

   private static final List<ProcessInstanceState> EXPORT_STATES = Arrays.asList(
         ProcessInstanceState.Completed, ProcessInstanceState.Aborted);

   private final List<Long> processInstanceOids;

   private final Date fromDate;

   private final Date toDate;

   /**
    * @param processInstanceOids
    *           Oids of process instances to export
    */
   public ExportProcessesCommand(List<Long> processInstanceOids)
   {
      super();
      this.processInstanceOids = processInstanceOids;
      this.fromDate = null;
      this.toDate = null;
   }

   /**
    * Use this constructor to export all processInstances
    */
   public ExportProcessesCommand()
   {
      super();
      this.processInstanceOids = null;
      this.fromDate = null;
      this.toDate = null;
   }

   /**
    * If a fromDate is provided, but no toDate then toDate defaults to now. If a toDate is
    * provided, but no fromDate then fromDate defaults to 1 January 1970. If a null fromDate
    * and toDate is provided then all processes will be exported.
    * @param fromDate includes processes with a start time greator or equal to fromDate
    * @param toDate includes processes with a termination time less or equal than toDate
    */
   public ExportProcessesCommand(Date fromDate, Date toDate)
   {
      super();
      this.processInstanceOids = null;
      if (fromDate != null || toDate != null)
      {
         // TODO : TimeZone?
         if (fromDate == null)
         {
            fromDate = new Date(0);
         }
         if (toDate == null)
         {
            toDate = new Date();
         }
      }
      this.fromDate = fromDate;
      this.toDate = toDate;
   }

   @Override
   public Serializable execute(ServiceFactory sf)
   {
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("START Export");
      }
      byte[] result;
      List<Long> uniqueOids = new ArrayList<Long>();
      QueryService queryService = sf.getQueryService();
   
      final Session session = (Session) SessionFactory
            .getSession(SessionFactory.AUDIT_TRAIL);
      if (CollectionUtils.isNotEmpty(processInstanceOids))
      {
         findExportInstancesByOids(uniqueOids, queryService);
      }
      else if (fromDate != null && toDate != null)
      {
         findExportInstancesByDate(uniqueOids, queryService);
      }
      else
      {
         findExportInstancesAll(uniqueOids, queryService);
      }
      result = exportAndPurge(session, uniqueOids);
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("END Export");
      }
      return result;
   }

   private void findExportInstancesByDate(List<Long> uniqueOids, QueryService queryService)
   {
      ProcessInstanceQuery query = new ProcessInstanceQuery();
      FilterAndTerm andTerm = query.getFilter().addAndTerm();
      andTerm.and(ProcessInstanceQuery.START_TIME.greaterOrEqual(this.fromDate.getTime()));
      andTerm.and(ProcessInstanceQuery.TERMINATION_TIME.lessOrEqual(this.toDate.getTime()));
      ProcessInstances processes = queryService.getAllProcessInstances(query);
      if (processes != null)
      {
         for (ProcessInstance process : processes)
         {
            if (process != null && !uniqueOids.contains(process.getOID()))
            {
               uniqueOids.add(process.getOID());
            }
         }
      }
   }

   private void findExportInstancesAll(List<Long> uniqueOids, QueryService queryService)
   {
      ProcessInstanceQuery query = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ProcessInstances processes = queryService.getAllProcessInstances(query);
      if (processes != null)
      {
         for (ProcessInstance process : processes)
         {
            if (process != null && !uniqueOids.contains(process.getOID()))
            {
               uniqueOids.add(process.getOID());
            }
         }
      }
   }

   private void findExportInstancesByOids(List<Long> uniqueOids, QueryService queryService)
   {
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("Received " + processInstanceOids.size() + " oids to export");
      }

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
               for (ProcessInstance process : processes)
               {
                  if (!uniqueOids.contains(process.getOID()))
                  {
                     if (EXPORT_STATES.contains(process.getState()))
                     {
                        uniqueOids.add(process.getOID());
                        if (LOGGER.isDebugEnabled())
                        {
                           if (process.getOID() != oid)
                           {
                              LOGGER.debug("Adding process with oid " + process.getOID()
                                    + " to export");
                           }
                        }
                     }
                     else
                     {
                        if (LOGGER.isInfoEnabled())
                        {
                           LOGGER.info("Process with oid " + process.getOID()
                                 + " can't be exported as it is not in one in state: "
                                 + EXPORT_STATES);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private byte[] exportAndPurge(final Session session, List<Long> uniqueOids)
   {
      byte[] result;
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("Exporting " + uniqueOids.size() + " processInstances");
      }
      if (CollectionUtils.isNotEmpty(uniqueOids))
      {
         ProcessElementExporter exporter = new ProcessElementExporter();
         ProcessElementsVisitor processVisitor = new ProcessElementsVisitor(exporter);
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("Exporting...");
         }
         // export processInstances
         processVisitor.visitProcessInstances(uniqueOids, session);
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("Exporting complete. Starting Purging...");
         }
         ProcessElementPurger purger = new ProcessElementPurger();
         processVisitor = new ProcessElementsVisitor(purger);
         // purge processInstances
         processVisitor.visitProcessInstances(uniqueOids, session);
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("Purging Complete.");
         }
         result = exporter.getBlob();
      }
      else
      {
         result = null;
      }
      return result;
   }
}
