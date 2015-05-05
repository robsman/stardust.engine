package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;

import org.apache.commons.collections.CollectionUtils;

import com.google.gson.Gson;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessElementExporter;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessElementPurger;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessElementsVisitor;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceProperty;
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

   private static final int[] EXPORT_STATES = new int[] {
         ProcessInstanceState.COMPLETED, ProcessInstanceState.ABORTED};
   private static final int SQL_IN_CHUNK_SIZE = 1000;

   private final Collection<Long> processInstanceOids;

   private List<Integer> modelOids;

   private Date fromDate;

   private Date toDate;

   private ExportMetaData exportMetaData;

   private ExportResult exportResult;

   private final Operation operation;

   private final boolean dumpData;

   private final HashMap<String, Object> descriptors;
   
   private final ObjectMessage message;
   
   private ExportProcessesCommand(Operation operation, ExportMetaData exportMetaData,
         List<Integer> modelOids, Collection<Long> processInstanceOids, Date fromDate,
         Date toDate, HashMap<String, Object> descriptors, ExportResult exportResult,
         boolean dumpData, ObjectMessage message)
   {
      this.operation = operation;
      this.exportMetaData = exportMetaData;
      this.processInstanceOids = processInstanceOids;
      this.modelOids = modelOids;
      this.fromDate = fromDate;
      this.toDate = toDate;
      this.exportResult = exportResult;
      this.descriptors = descriptors;
      this.dumpData = dumpData;
      this.message = message;
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
         Collection<Long> processInstanceOids, HashMap<String, Object> descriptors,
         boolean dumpData)
   {
      this(operation, null, modelOids, processInstanceOids, null, null, descriptors,
            null, dumpData, null);
   }

   /**
    * Use this constructor to export all processInstances
    */
   public ExportProcessesCommand(Operation operation,
         HashMap<String, Object> descriptors, boolean dumpData)
   {

      this(operation, null, null, null, null, null, descriptors, null, dumpData, null);
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
   public ExportProcessesCommand(Operation operation, Date fromDate, Date toDate,
         HashMap<String, Object> descriptors, boolean dumpData)
   {

      this(operation, null, null, null, fromDate, toDate, descriptors, null, dumpData, null);
   }

   /**
    * Use this constructor to export all processInstances or models for given
    * exportMetaData. Set dumpOnly to true if they should not be marked as exported by
    * receiving a unique id
    */
   public ExportProcessesCommand(Operation operation, ExportMetaData exportMetaData,
         boolean dumpData)
   {

      this(operation, exportMetaData, null, null, null, null, null, null, dumpData, null);
   }

   /**
    * Use this constructor to archive or purge all processInstances for given
    * exportResults
    */
   public ExportProcessesCommand(Operation operation, ExportResult exportResult,
         boolean dumpData)
   {

      this(operation, null, null, null, null, null, null, exportResult, dumpData, null);
   }
    
   /**
   * Use this constructor to auto archive a DEFERRED process instance
   */
   public ExportProcessesCommand(ObjectMessage message)
   {
      this(Operation.ARCHIVE_MESSAGES, null, null, new ArrayList<Long>(), null, null, null,
            null, false, message);
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
            queryAndExport(session);
            result = exportResult;
            break;
         case QUERY:
            query(session);
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
            result = archive(session);
            break;
         case ARCHIVE_MESSAGES:
            result = archiveMessages(session);
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

   private boolean archiveMessages(Session session)
   {
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("Received message to export");
      }
      
      Serializable object;
      List<ExportResult> exportResults;
      try
      {
         object = message.getObject();
         if (object instanceof List)
         {
            exportResults = (List) object;
         }
         else
         {
            throw new IllegalArgumentException("Invalid object received to archive.");
         }
      }
      catch (JMSException e)
      {
         LOGGER.error("Failed to retrieve archive object from message", e);
         return false;
      }
      
      if (!exportResults.isEmpty())
      {
         List<ExportResult> groupedResults = ExportImportSupport.groupByExportModel(exportResults);
         for (ExportResult result : groupedResults)
         {
            exportResult = result;
            archive(session);
         }
      }
//      if (1 ==1 )
//      {
//         throw new RuntimeException();
//      }
      return true;
   }

   private int purge(Session session)
   {
      int result;
      if (exportResult != null)
      {
         if (CollectionUtils.isNotEmpty(exportResult.getPurgeProcessIds()))
         {
            if (LOGGER.isDebugEnabled())
            {
               LOGGER.debug("Purging " + exportResult.getPurgeProcessIds().size()
                     + " processInstances");
            }
            ProcessElementPurger purger = new ProcessElementPurger();
            ;
            ProcessElementsVisitor processVisitor = new ProcessElementsVisitor(purger);
            // purge processInstances
            result = processVisitor.visitProcessInstances(
                  exportResult.getPurgeProcessIds(), session);
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

   private Boolean archive(Session session)
   {
      IArchiveManager archiveManager = ArchiveManagerFactory.getCurrent();
      boolean success = true;
      if (exportResult != null)
      {
         dateloop: for (Date date : exportResult.getDates())
         {

            ExportIndex exportIndex = exportResult.getExportIndex(date);
            if (!exportIndex.isDump())
            {
               markProcessesAsExported(session, exportIndex);
            }
            
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
               Gson gson = ExportImportSupport.getGson();
               if (success)
               {
                  success = archiveManager.addModel(key, gson.toJson(exportResult.getExportModel()));
                  if (!success)
                  {
                     break dateloop;
                  }
               }
               if (success)
               {
                  success = archiveManager.addIndex(key, gson
                        .toJson(exportIndex));
                  if (!success)
                  {
                     break dateloop;
                  }
               }
               if (success)
               {
                  success = archiveManager.close(key, date, exportResult);
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

   private void markProcessesAsExported(Session session, ExportIndex exportIndex)
   {
      for (Long oid : exportIndex.getProcessInstanceOids())
      {
         Persistent persistent = session.findByOID(ProcessInstanceBean.class, oid);
         // instance can be null if archiving of queue is done after process already deleted
         // this will not harm anything the export id will be in archive anyways
         if (persistent != null)
         {
            ProcessInstanceBean instance = (ProcessInstanceBean) persistent;
            instance.createProperty(
                  ProcessElementExporter.EXPORT_PROCESS_ID, exportIndex.getUuid(oid));
         }
      }
   }
   
   private void exportBatch(Session session)
   {
      List<Long> allIds = exportMetaData.getAllProcessesForExport(dumpData);
      if (CollectionUtils.isNotEmpty(allIds))
      {
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("Exporting " + allIds.size() + " processInstances");
         }
         if (exportResult == null)
         {
            exportResult = new ExportResult(dumpData);
         }
         ProcessElementExporter exporter = new ProcessElementExporter(exportResult,
               !dumpData);
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
         if (exportResult == null)
         {
            exportResult = new ExportResult(dumpData);
         }
      }
      exportResult.getPurgeProcessIds().clear();
      if (allIds != null)
      {
         exportResult.getPurgeProcessIds().addAll(allIds);
      }
      exportResult.getPurgeProcessIds().addAll(exportMetaData.getBackedUpProcesses());
   }

   private void exportModels(Session session)
   {
      ExportModel exportModel;
      if (CollectionUtils.isNotEmpty(exportMetaData.getModelOids()))
      {
         exportModel = ExportImportSupport.exportModels(exportMetaData.getModelOids());
      }
      else
      {
         exportModel = ExportImportSupport.exportModels();
      }
      if (exportResult == null)
      {
         exportResult = new ExportResult(dumpData);
      }
      exportResult.setExportModel(exportModel);
   }

   private void query(Session session)
   {
      validateDates();

      exportMetaData = new ExportMetaData();
      if (CollectionUtils.isEmpty(modelOids))
      {
         if (modelOids == null)
         {
            modelOids = new ArrayList<Integer>();
         }
         modelOids.addAll(ExportImportSupport.getActiveModelOids());
      }
      findExportInstances(session);
   }

   private void queryAndExport(Session session)
   {
      query(session);
      if (exportMetaData.hasExportOids())
      {
         exportModels(session);
      }
      else
      {
         exportResult = new ExportResult(dumpData);
      }
      if (exportResult.hasExportModel())
      {
         exportBatch(session);
      }
   }

   private void processQueryResults(Session session, QueryDescriptor query)
   {
      ResultSet rs = session.executeQuery(query);
      try
      {
         while (rs.next())
         {
            Long oid = rs.getBigDecimal(ProcessInstanceBean.FIELD__OID).longValue();
            Integer modelOid = rs.getBigDecimal(ProcessInstanceBean.FIELD__MODEL)
                  .intValue();
            Long rootOid = rs.getBigDecimal(
                  ProcessInstanceBean.FIELD__ROOT_PROCESS_INSTANCE).longValue();
            Date startDate = new Date(rs.getBigDecimal(
                  ProcessInstanceBean.FIELD__START_TIME).longValue());
            String uuid = rs.getString(ProcessInstanceProperty.FIELD__STRING_VALUE);
            boolean exported = ExportImportSupport.getUUID(oid, startDate).equals(uuid);
            boolean isInFilter = false;
            if (descriptors != null && descriptors.size() > 0)
            {
               IProcessInstance processInstance = ProcessInstanceBean.findByOID(oid);
               IProcessDefinition processDefinition = processInstance.getProcessDefinition();
               Map<String, Object> pathValues = ExportImportSupport
                     .getDescriptors(processInstance, processDefinition, descriptors.keySet());
               for (String id : pathValues.keySet())
               {
                  if (descriptors.get(id).equals(pathValues.get(id)))
                  {
                     // we do or logic, as soon as one descriptor matches we have a match
                     isInFilter = true;
                     break;
                  }
               }
            }
            else
            {
               isInFilter = true;
            }
            if (isInFilter)
            {
               if (exported)
               {
                  exportMetaData.addProcess(oid, startDate, rootOid, modelOid, uuid);
               }
               else
               {
                  exportMetaData.addProcess(oid, startDate, rootOid, modelOid, null);
               }
            }
         }
      }
      catch (SQLException e)
      {
         throw new IllegalStateException("Can't find process instance to export", e);
      }
      finally
      {
         QueryUtils.closeResultSet(rs);
      }
   }

   private QueryDescriptor getBaseQuery()
   {
      QueryDescriptor query = QueryDescriptor.from(ProcessInstanceBean.class).select(
            new Column[] {
                  ProcessInstanceBean.FR__OID, ProcessInstanceBean.FR__MODEL,
                  ProcessInstanceBean.FR__ROOT_PROCESS_INSTANCE,
                  ProcessInstanceBean.FR__START_TIME, ProcessInstanceProperty.FR__STRING_VALUE});

      query.leftOuterJoin(ProcessInstanceProperty.class, "pip")
            .on(ProcessInstanceBean.FR__OID, ProcessInstanceProperty.FIELD__OBJECT_OID)
            .andOnConstant(ProcessInstanceProperty.FR__NAME,
                  "'" + ProcessElementExporter.EXPORT_PROCESS_ID + "'");

      return query;
   }

   private void findExportInstances(Session session)
   {
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("Received " + processInstanceOids.size() + " oids to export");
         LOGGER.debug("Received " + modelOids.size() + " modelIds to export");
      }

      QueryDescriptor query = getBaseQuery();

      ComparisonTerm processStateRestriction = Predicates.inList(
            ProcessInstanceBean.FR__STATE, EXPORT_STATES);
      ComparisonTerm modelRestriction = Predicates.inList(ProcessInstanceBean.FR__MODEL,
            modelOids);
      ComparisonTerm processDefinitionRestriction = Predicates.greaterThan(
            ProcessInstanceBean.FR__PROCESS_DEFINITION, 0);

      // TODO Improve by adding restriction on uuid ProcessInstanceProperty.FR__STRING_VALUE:
      // (not like currentArchiveId_processId_starttime in long) or is null
      // currently dont know how to add such complex like condition
      AndTerm whereTerm = Predicates.andTerm(processStateRestriction, modelRestriction,
            processDefinitionRestriction);

      if (CollectionUtils.isNotEmpty(processInstanceOids))
      {
         List<List<Long>> splitList = org.eclipse.stardust.common.CollectionUtils.split(processInstanceOids, SQL_IN_CHUNK_SIZE);
         MultiPartPredicateTerm rootOidTerm = new OrTerm();
         for (List<Long> list : splitList)
         {
            rootOidTerm.add(Predicates.inList(ProcessInstanceBean.FR__ROOT_PROCESS_INSTANCE, list));
         }
         whereTerm.add(rootOidTerm);
      }
      if (fromDate != null && toDate != null)
      {
         AndTerm dateRestriction = Predicates.andTerm(Predicates.greaterOrEqual(
               ProcessInstanceBean.FR__START_TIME, this.fromDate.getTime()), Predicates
               .lessOrEqual(ProcessInstanceBean.FR__START_TIME,
                     this.toDate.getTime()));
         whereTerm.add(dateRestriction);
      }
      query.where(whereTerm);
      // order by start time since root processes must be first
      query.orderBy(ProcessInstanceBean.FR__START_TIME, ProcessInstanceBean.FR__OID);
      processQueryResults(session, query);
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
       * Takes an export result and deletes the processes in it from the database
       */
      PURGE,
      /**
       * Takes an export result and persists it to an archive
       */
      ARCHIVE,
      /**
       * Takes messages that has been read from a JMS queue and archives them 
       */
      ARCHIVE_MESSAGES
      ;
   };

   public static class ExportMetaData implements Serializable
   {
      private static final long serialVersionUID = 1L;

      private final HashMap<Long, ArrayList<Long>> rootToSubProcesses;

      private final Map<Date, List<Long>> dateToRootPIOids;

      private final Map<Date, List<Integer>> dateToModelOids;
      
      private final Map<Long, String> oidsToUuids;

      public ExportMetaData()
      {
         this.dateToModelOids = new HashMap<Date, List<Integer>>();
         this.rootToSubProcesses = new HashMap<Long, ArrayList<Long>>();
         this.dateToRootPIOids = new HashMap<Date, List<Long>>();
         this.oidsToUuids = new HashMap<Long, String>();
      }

      public ExportMetaData(Map<Date, List<Integer>> dateToModelOids,
            HashMap<Long, ArrayList<Long>> rootToSubProcesses,
            Map<Date, List<Long>> dateToRootProcessInstanceOids, 
            Map<Long, String> oidsToUuids)
      {
         this.dateToModelOids = dateToModelOids;
         this.rootToSubProcesses = rootToSubProcesses;
         this.dateToRootPIOids = dateToRootProcessInstanceOids;
         this.oidsToUuids = oidsToUuids;
      }

      /**
       * Returns map of processInstanceOids with keyset being rootProcessInstanceOids and
       * values are corresponding subprocess's processInstanceOids
       * 
       * @return
       */
      public HashMap<Long, ArrayList<Long>> getRootToSubProcesses()
      {
         return rootToSubProcesses;
      }

      /**
       * Guaranteed to be a list of valid modelOids
       * 
       * @return
       */
      public List<Integer> getModelOids(Date date)
      {
         if (date == null)
         {
            throw new IllegalArgumentException("Invalid date provided");
         }
         return dateToModelOids.get(date);
      }

      public Set<Integer> getModelOids()
      {
         HashSet<Integer> result = new HashSet<Integer>();
         for (Date date : dateToModelOids.keySet())
         {
            result.addAll(dateToModelOids.get(date));
         }
         return result;
      }

      public Map<Long, String> getOidsToUuids()
      {
         return oidsToUuids;
      }

      public boolean hasExportOids()
      {
         return rootToSubProcesses != null
               && CollectionUtils.isNotEmpty(rootToSubProcesses.keySet());
      }

      /**
       * @param dumpData
       * @return Combined list of processInstanceOids for root processes and subprocesses
       */
      public List<Long> getAllProcessesForExport(boolean dumpData)
      {
         List<Long> allIds = new ArrayList<Long>();
         if (rootToSubProcesses != null)
         {
            for (Long key : rootToSubProcesses.keySet())
            {
               if (oidsToUuids.get(key) == null || dumpData)
               {
                  allIds.add(key);
                  for (Long subProcess : rootToSubProcesses.get(key))
                  {
                     allIds.add(subProcess);
                  }
               }
            }
         }
         return allIds;
      }

      public List<Long> getBackedUpProcesses()
      {
         List<Long> allIds = new ArrayList<Long>();
         if (rootToSubProcesses != null)
         {
            for (Long key : rootToSubProcesses.keySet())
            {
               if (oidsToUuids.get(key) != null)
               {
                  allIds.add(key);
                  for (Long subProcess : rootToSubProcesses.get(key))
                  {
                     allIds.add(subProcess);
                  }
               }
            }
         }
         return allIds;
      }

      public Set<Date> getIndexDates()
      {
         return dateToRootPIOids.keySet();
      }

      public List<Long> getRootProcessesForDate(Date date)
      {
         if (date == null)
         {
            throw new IllegalArgumentException("Invalid date provided");
         }
         Date indexDateTime = ExportImportSupport.getIndexDateTime(date);
         return dateToRootPIOids.get(indexDateTime);
      }

      private void addProcess(Long oid, Date startTime, Long rootProcessOid,
            Integer modelOid, String uuid)
      {
         Date indexDateTime = ExportImportSupport.getIndexDateTime(startTime);
         if (uuid != null)
         {
            oidsToUuids.put(oid, uuid);
         }
         if (rootProcessOid.equals(oid))
         {
            ArrayList<Long> subProcesses = rootToSubProcesses.get(oid);
            if (subProcesses == null)
            {
               rootToSubProcesses.put(oid, new ArrayList<Long>());
            }
            List<Long> piOids = dateToRootPIOids.get(indexDateTime);
            if (piOids == null)
            {
               piOids = new ArrayList<Long>();
               dateToRootPIOids.put(indexDateTime, piOids);
            }
            piOids.add(oid);
         }
         else
         {
            Long rootProcess = null;
            for (Long exportProcess : rootToSubProcesses.keySet())
            {
               if (exportProcess.equals(rootProcessOid))
               {
                  rootProcess = exportProcess;
                  break;
               }
            }
            if (rootProcess == null)
            {
               throw new IllegalStateException("Root processes not yet added");
            }
            ArrayList<Long> siblingList = rootToSubProcesses.get(rootProcess);
            if (siblingList == null)
            {
               siblingList = new ArrayList<Long>();
               rootToSubProcesses.put(rootProcess, siblingList);
            }
            if (!siblingList.contains(oid))
            {
               siblingList.add(oid);
            }
         }
         List<Integer> modelOids = dateToModelOids.get(indexDateTime);
         if (modelOids == null)
         {
            modelOids = new ArrayList<Integer>();
            dateToModelOids.put(indexDateTime, modelOids);
         }
         modelOids.add(modelOid);
      }

   }

}
