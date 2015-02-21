package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.collections.CollectionUtils;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
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
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;

/**
 * This class allows a request to archive processes instances. The processes will be
 * exported to a byte[] and will optionally be deleted from the database. If a process has
 * subprocesses the subprocesses will be exported and purged as well.
 * 
 * Processes can be exported:<br/>
 * <li/>completely (per partition) <li/>by root process instance OID <li/>by Model OIDs
 * <li/>
 * by business identifier (unique primitive key descriptor) <li/>by from/to filter (start
 * time to termination time) Dates are inclusive <br/>
 * If no valid processInstanceOids are provided or derived from criteria then null will be
 * returned. <br/>
 * If a fromDate is provided, but no toDate then toDate defaults to now. If a toDate is
 * provided, but no fromDate then fromDate defaults to 1 January 1970. If a null fromDate
 * and toDate is provided then all processes will be exported. <br/>
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

   private final Collection<Long> processInstanceOids;

   private List<Integer> modelOids;

   private Date fromDate;

   private Date toDate;

   private ExportMetaData exportMetaData;

   private ExportResult exportResult;

   private final Operation operation;

   private ExportProcessesCommand(Operation operation, ExportMetaData exportMetaData,
         List<Integer> modelOids, Collection<Long> processInstanceOids, Date fromDate,
         Date toDate, ExportResult exportResult)
   {
      this.operation = operation;
      this.exportMetaData = exportMetaData;
      this.processInstanceOids = processInstanceOids;
      this.modelOids = modelOids;
      this.fromDate = fromDate;
      this.toDate = toDate;
      this.exportResult = exportResult;
   }

   /**
    * If processInstanceOIDs and ModelOIDs are provided we perform AND logic between the
    * processInstanceOIDs and ModelOIDs provided
    * 
    * @param modelOids
    *           Oids of models to export
    * @param processInstanceOids
    *           Oids of process instances to export
    */
   public ExportProcessesCommand(Operation operation, List<Integer> modelOids,
         Collection<Long> processInstanceOids)
   {
      this(operation, null, modelOids, processInstanceOids, null, null, null);
   }

   /**
    * Use this constructor to export all processInstances
    */
   public ExportProcessesCommand(Operation operation)
   {

      this(operation, null, null, null, null, null, null);
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
   public ExportProcessesCommand(Operation operation, Date fromDate, Date toDate)
   {

      this(operation, null, null, null, fromDate, toDate, null);
   }

   /**
    * Use this constructor to export all processInstances or models for given
    * exportMetaData
    */
   public ExportProcessesCommand(Operation operation, ExportMetaData exportMetaData)
   {

      this(operation, exportMetaData, null, null, null, null, null);
   }

   /**
    * Use this constructor to archive or purge all processInstances for given
    * exportResults
    */
   public ExportProcessesCommand(Operation operation, ExportResult exportResult)
   {

      this(operation, null, null, null, null, null, exportResult);
   }

   @Override
   public Serializable execute(ServiceFactory sf)
   {
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("START Export Operation: " + this.operation.name());
      }
      final Session session = (Session) SessionFactory
            .getSession(SessionFactory.AUDIT_TRAIL);
      Serializable result;
      switch (operation)
      {
         case QUERY_AND_EXPORT:
            queryAndExport(session, sf);
            result = exportResult;
            break;
         case QUERY:
            query(session, sf);
            result = exportMetaData;
            break;
         case EXPORT_MODEL:
            exportModels(session);
            result = exportResult;
            break;
         case EXPORT_BATCH:
            exportBatch(session);
            result = exportResult;
            break;
         case PURGE:
            result = purge(session);
            break;
         case ARCHIVE:
            result = archive();
            break;
         default:
            throw new IllegalArgumentException("No valid operation provided");
      }
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("END Export Operation: " + this.operation.name());
      }
      return result;
   }

   private int purge(Session session)
   {
      int result;
      if (exportResult != null)
      {
         if (CollectionUtils.isNotEmpty(exportResult.getAllProcessIds()))
         {
            if (LOGGER.isDebugEnabled())
            {
               LOGGER.debug("Purging " + exportResult.getAllProcessIds().size() + " processInstances");
            }
            if (exportResult == null)
            {
               exportResult = new ExportResult();
            }
            ProcessElementPurger purger = new ProcessElementPurger();;
            ProcessElementsVisitor processVisitor = new ProcessElementsVisitor(purger);
            // purge processInstances
            result = processVisitor.visitProcessInstances(exportResult.getAllProcessIds(), session);
            if (LOGGER.isDebugEnabled())
            {
               LOGGER.debug("Exporting complete.");
            }
         }
         else
         {
            result = 0;
            if (LOGGER.isDebugEnabled())
            {
               LOGGER.debug("No processInstanceOids provided for purge");
            }
         }
      }
      else
      {
         result = 0;
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("No ExportResults provided for purge");
         }
      }
      return result;
   }

   private Boolean archive()
   {
      IArchiveManager archiveManager = ArchiveManagerFactory.getCurrent();
      boolean success = true;
      if (exportResult != null)
      {
         dateloop: for (Date date : exportResult.getDates())
         {
            Serializable key = archiveManager.open(date);
            if (key == null)
            {
               success = false;
               break;
            }
            else
            {
               byte[] data = exportResult.getResults(date);
               success = archiveManager.add(key, data);
               if (!success)
               {
                  break dateloop;
               }
               if (success)
               {
                  success = archiveManager.addModel(key, exportResult.getModelData());
                  if (!success)
                  {
                     break dateloop;
                  }
               }
               if (success)
               {
                  success = archiveManager.addIndex(key, exportResult.getIndex(date));
                  if (!success)
                  {
                     break dateloop;
                  }
               }
               if (success)
               {
                  success = archiveManager.close(key, exportResult.getProcessIds(date), exportResult.getProcessLengths(date));
               }
            }
            if (!success)
            {
               break;
            }
         }
      }
      return success;
   }

   private void exportBatch(Session session)
   {
      List<Long> allIds = exportMetaData.getAllProcessInstanceOids();
      if (CollectionUtils.isNotEmpty(allIds))
      {
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("Exporting " + allIds.size() + " processInstances");
         }
         if (exportResult == null)
         {
            exportResult = new ExportResult();
         }
         ProcessElementExporter exporter = new ProcessElementExporter(exportResult, false);
         ProcessElementsVisitor processVisitor = new ProcessElementsVisitor(exporter);
         // export processInstances
         processVisitor.visitProcessInstances(allIds, session);
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("Exporting complete.");
         }
      }
      else
      {
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("No processInstanceOids provided for export");
         }
      }

   }

   private void exportModels(Session session)
   {
      byte[] model;
      if (processInstanceOids != null || modelOids != null)
      {
         model = ExportImportSupport.exportModels(exportMetaData.getModelOids());
      }
      else
      {
         model = ExportImportSupport.exportModels();
      }
      if (exportResult == null)
      {
         exportResult = new ExportResult();
      }
      exportResult.setModelData(model);
   }

   private void query(Session session, ServiceFactory sf)
   {
      validateDates();

      QueryService queryService = sf.getQueryService();
      exportMetaData = new ExportMetaData();
      boolean hasModelOidsParameter = CollectionUtils.isNotEmpty(modelOids);
      if (!hasModelOidsParameter)
      {
         if (modelOids == null)
         {
            modelOids = new ArrayList<Integer>();
         }
         modelOids.addAll(ExportImportSupport.getActiveModelOids());
      }

      if (CollectionUtils.isNotEmpty(processInstanceOids) || hasModelOidsParameter)
      {
         findExportInstancesByOids(queryService);
      }
      else if (fromDate != null && toDate != null)
      {
         findExportInstancesByDate(queryService);
      }
      else
      {
         findExportInstancesAll(queryService);
      }
   }

   private void queryAndExport(Session session, ServiceFactory sf)
   {
      query(session, sf);
      if (exportMetaData.hasExportOids())
      {
         exportModels(session);
      }
      else
      {
         exportResult = new ExportResult();
      }
      if (exportResult.hasModelData())
      {
         exportBatch(session);
      }
   }

   private void findExportInstancesByDate(QueryService queryService)
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
            if (process != null && modelOids.contains(process.getModelOID()))
            {
               exportMetaData.addProcess(process);
            }
         }
      }
   }

   private void findExportInstancesAll(QueryService queryService)
   {
      ProcessInstanceQuery query = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ProcessInstances processes = queryService.getAllProcessInstances(query);
      if (processes != null)
      {
         for (ProcessInstance process : processes)
         {
            if (process != null && modelOids.contains(process.getModelOID()))
            {
               exportMetaData.addProcess(process);
            }
         }
      }
   }

   private void findExportInstancesByOids(QueryService queryService)
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
            if (oid != null)
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
            if (EXPORT_STATES.contains(process.getState())
                  && modelOids.contains(process.getModelOID()))
            {
               exportMetaData.addProcess(process);
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
         this.fromDate = ExportImportSupport.getStartOfDay(fromDate);
         this.toDate = ExportImportSupport.getEndOfDay(toDate);
      }
   }

   /**
    * @author jsaayman
    */
   public static enum Operation
   {
      /**
       * Find processes to export, export model and all processes found.
       */
      QUERY_AND_EXPORT,
      /**
       * Find processes to export, do not export anything.
       */
      QUERY,
      /**
       * Export model(s) only. Provide modelOids to specify which models to export. If no
       * modelOids are provided, the latest model will be exported.
       */
      EXPORT_MODEL,
      /**
       * Export batch of supplied unique processInstanceOids. This should include
       * subprocess processInstanceOids. Use query operations to find these
       * processInstanceOids.
       */
      EXPORT_BATCH,
      /**
       * 
       */
      PURGE,
      /**
       * 
       */
      ARCHIVE;
   };

   public static class ExportMetaData implements Serializable
   {
      private static final long serialVersionUID = 1L;

      private final HashMap<Long, ArrayList<Long>> uniqueOids;

      private final List<Integer> modelOids;

      public ExportMetaData()
      {
         this.modelOids = new ArrayList<Integer>();
         this.uniqueOids = new HashMap<Long, ArrayList<Long>>();
      }

      public ExportMetaData(List<Integer> modelOids,
            HashMap<Long, ArrayList<Long>> uniqueOids)
      {
         this.modelOids = modelOids;
         this.uniqueOids = uniqueOids;
      }

      /**
       * Returns map of processInstanceOids with keyset being rootProcessInstanceOids and
       * values are corresponding subprocess's processInstanceOids
       * 
       * @return
       */
      public HashMap<Long, ArrayList<Long>> getMappedProcessInstances()
      {
         return uniqueOids;
      }

      /**
       * Guaranteed to be a list of valid modelOids
       * 
       * @return
       */
      public List<Integer> getModelOids()
      {
         return modelOids;
      }

      public boolean hasExportOids()
      {
         return uniqueOids != null && CollectionUtils.isNotEmpty(uniqueOids.keySet());
      }

      /**
       * 
       * @return list of root processInstanceOids
       */
      public Collection<Long> getRootProcessInstanceOids()
      {
         Set<Long> allIds;
         if (uniqueOids != null)
         {
            allIds = uniqueOids.keySet();
         }
         else
         {
            allIds = new HashSet<Long>();
         }
         return allIds;
      }

      /**
       * @return Combined list of processInstanceOids for root processes and subprocesses
       */
      public List<Long> getAllProcessInstanceOids()
      {
         List<Long> allIds = new ArrayList<Long>();
         if (uniqueOids != null)
         {
            for (Long key : uniqueOids.keySet())
            {
               allIds.add(key);
               allIds.addAll(uniqueOids.get(key));
            }
         }
         return allIds;
      }

      public void addProcess(ProcessInstance process)
      {
         if (process.getRootProcessInstanceOID() == process.getOID())
         {
            if (!uniqueOids.keySet().contains(process.getOID()))
            {
               uniqueOids.put(process.getOID(), new ArrayList<Long>());
            }
         }
         else
         {
            ArrayList<Long> siblingList = uniqueOids.get(process
                  .getRootProcessInstanceOID());
            if (siblingList == null)
            {
               siblingList = new ArrayList<Long>();
               uniqueOids.put(process.getRootProcessInstanceOID(), siblingList);
            }
            if (!siblingList.contains(process.getOID()))
            {
               siblingList.add(process.getOID());
            }
         }
         if (!modelOids.contains(process.getModelOID()))
         {
            modelOids.add(process.getModelOID());
         }
      }

   }

}
