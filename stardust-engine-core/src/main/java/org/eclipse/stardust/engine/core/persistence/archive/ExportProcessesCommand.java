package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.*;
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
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;

/**
 * This class allows a request to archive processes instances. The processes will be
 * exported to a byte[] and will be deleted from the database. If a process has
 * subprocesses the subprocesses will be exported and purged as well.
 * 
 * Processes can be exported:<br/>
 * <li/>completely (per partition) <li/>by root process instance OID <li/>by Model OIDs <li/>
 * by business identifier (unique primitive key descriptor) <li/>by from/to filter (start
 * time to termination time) <br/>
 * If no valid processInstanceOids are provided or derived from criteria then null will be
 * returned.
 * <br/>
 * If a fromDate is provided, but no toDate then toDate defaults to now. If a toDate is
 * provided, but no fromDate then fromDate defaults to 1 January 1970. If a null fromDate
 * and toDate is provided then all processes will be exported.
 *  <br/>
 * If processInstanceOIDs and ModelOIDs are provided we perform AND logic between the
 * processInstanceOIDs and ModelOIDs provided
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

   private final List<Integer> modelOids;

   private Date fromDate;

   private Date toDate;

   private final boolean purge;

   /**
    * If processInstanceOIDs and ModelOIDs are provided we perform AND logic between the
    * processInstanceOIDs and ModelOIDs provided
    * @param modelOids Oids of models to export
    * @param processInstanceOids
    *           Oids of process instances to export
    */
   public ExportProcessesCommand(List<Integer> modelOids, List<Long> processInstanceOids,
         boolean purge)
   {
      super();
      this.purge = purge;
      this.processInstanceOids = processInstanceOids;
      this.modelOids = modelOids;
      this.fromDate = null;
      this.toDate = null;
   }

   /**
    * Use this constructor to export all processInstances
    */
   public ExportProcessesCommand(boolean purge)
   {
      super();
      this.purge = purge;
      this.modelOids = null;
      this.processInstanceOids = null;
      this.fromDate = null;
      this.toDate = null;
   }

   /**
    * If a fromDate is provided, but no toDate then toDate defaults to now. If a toDate is
    * provided, but no fromDate then fromDate defaults to 1 January 1970. If a null
    * fromDate and toDate is provided then all processes will be exported.
    * 
    * @param fromDate
    *           includes processes with a start time greator or equal to fromDate
    * @param toDate
    *           includes processes with a termination time less or equal than toDate
    */
   public ExportProcessesCommand(Date fromDate, Date toDate, boolean purge)
   {
      super();
      this.purge = purge;
      this.modelOids = null;
      this.processInstanceOids = null;
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
      validateDates();
      byte[] result;
      List<Long> uniqueOids = new ArrayList<Long>();
      QueryService queryService = sf.getQueryService();

      final Session session = (Session) SessionFactory
            .getSession(SessionFactory.AUDIT_TRAIL);
      if (CollectionUtils.isNotEmpty(processInstanceOids)
            || CollectionUtils.isNotEmpty(modelOids))
      {
         findExportInstancesByOids(modelOids, uniqueOids, queryService);
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
      andTerm
            .and(ProcessInstanceQuery.START_TIME.greaterOrEqual(this.fromDate.getTime()));
      andTerm
            .and(ProcessInstanceQuery.TERMINATION_TIME.lessOrEqual(this.toDate.getTime()));
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

   private void findExportInstancesByOids(List<Integer> modelOids, List<Long> uniqueOids,
         QueryService queryService)
   {
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("Received " + processInstanceOids.size() + " oids to export");
         LOGGER.debug("Received " + modelOids.size() + " modelIds to export");
      }
      ProcessInstanceQuery query = new ProcessInstanceQuery();
      if (CollectionUtils.isNotEmpty(processInstanceOids))
      {
         FilterOrTerm orTerm;
         orTerm = query.getFilter().addOrTerm();
         for (Long oid : processInstanceOids)
         {
            if (oid != null && !uniqueOids.contains(oid))
            {
               // check that the oid is valid. if it is valid add it to export, further if
               // it has any subprocesses add them to the export as well
               orTerm.or(ProcessInstanceQuery.ROOT_PROCESS_INSTANCE_OID.isEqual(oid));
            }
         }
      }

      ProcessInstances processes = queryService.getAllProcessInstances(query);
      if (processes != null)
      {
         for (ProcessInstance process : processes)
         {
            if (!uniqueOids.contains(process.getOID()))
            {
               if (EXPORT_STATES.contains(process.getState())
                     && (modelOids == null || modelOids.isEmpty() || modelOids
                           .contains(process.getModelOID())))
               {
                  uniqueOids.add(process.getOID());
                  if (LOGGER.isDebugEnabled())
                  {
                     if (process.getOID() != process.getRootProcessInstanceOID())
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

   private byte[] exportAndPurge(final Session session, List<Long> uniqueOids)
   {
      byte[] result;
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("Exporting " + uniqueOids.size() + " processInstances");
      }
      if (CollectionUtils.isNotEmpty(uniqueOids))
      {
         byte[] model = ExportImportSupport.exportModel();
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
         if (purge)
         {
            if (LOGGER.isDebugEnabled())
            {
               LOGGER.debug("Starting Purging...");
            }
            ProcessElementPurger purger = new ProcessElementPurger();
            processVisitor = new ProcessElementsVisitor(purger);
            // purge processInstances
            processVisitor.visitProcessInstances(uniqueOids, session);
            if (LOGGER.isDebugEnabled())
            {
               LOGGER.debug("Purging Complete.");
            }
         }
         else
         {
            if (LOGGER.isDebugEnabled())
            {
               LOGGER.debug("Purge not required.");
            }
         }
         byte[] export = exporter.getBlob();
         result = new byte[model.length + export.length];
         System.arraycopy(model, 0, result, 0, model.length);
         System.arraycopy(export, 0, result, model.length, export.length);
      }
      else
      {
         result = null;
      }
      return result;
   }

   private void validateDates()
   {
      if (fromDate != null || toDate != null)
      {
         if (fromDate == null)
         {
            this.fromDate = new Date(0);
         }
         if (toDate == null)
         {
            this.toDate = TimestampProviderUtils.getTimeStamp();
         }
         if (toDate.before(fromDate))
         {
            throw new IllegalArgumentException(
                  "Export from date can not be before export to date");
         }
      }
   }
}
