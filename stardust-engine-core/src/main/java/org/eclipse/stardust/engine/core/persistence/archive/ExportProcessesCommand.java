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

/**
 * This class allows a request to archive or dump processes instances. The processes will be
 * exported to a byte[] and will be deleted from the database if the operation is not a dump operation. If a process has
 * subprocesses the subprocesses will be exported and purged as well.
 * 
 * Processes can be exported:<br/>
 * <li/>completely (per partition) <li/>by root process instance OID <li/>by Model OIDs
 * <li/>
 * by business identifier (unique primitive key descriptor) <li/>by from/to filter (start
 * time to termination time) Dates are inclusive <br/>
 * If no valid processInstanceOids are provided or derived from criteria then no processes will be included. <br/>
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

   
   private ExportMetaData exportMetaData;

   private ExportResult exportResult;

   private final Operation operation;

   private final String dumpLocation;
   
   private ArchiveFilter filter;

   private final ObjectMessage message;
   
   private ExportProcessesCommand(Operation operation, ArchiveFilter filter, ExportMetaData exportMetaData,
         ExportResult exportResult,
         String dumpLocation, ObjectMessage message)
   {
      this.operation = operation;
      this.exportMetaData = exportMetaData;
      this.filter = filter;
      this.exportResult = exportResult;
      this.dumpLocation = dumpLocation;
      this.message = message;
   }

   /**
    * 
    * @param filter criteria
    */
   public ExportProcessesCommand(Operation operation, ArchiveFilter filter,
         String dumpLocation)
   {
      this(operation, filter, null, null, dumpLocation, null);
   }
   
   /**
    * Use this constructor to export all processInstances or models for given
    * exportMetaData. Set dumpOnly to true if they should not be marked as exported by
    * receiving a unique id
    */
   public ExportProcessesCommand(Operation operation, ExportMetaData exportMetaData,
         String dumpLocation)
   {

      this(operation, null, exportMetaData, null, dumpLocation, null);
   }

   /**
    * Use this constructor to archive or purge all processInstances for given
    * exportResults
    */
   public ExportProcessesCommand(Operation operation, ExportResult exportResult,
         String dumpLocation)
   {

      this(operation, null, null, exportResult, dumpLocation, null);
   }
    
   /**
   * Use this constructor when auto archive is enabled auto archive
   */
   public ExportProcessesCommand(ObjectMessage message)
   {
      this(Operation.ARCHIVE_MESSAGES, null, null, null, null, message);
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
      IArchiveManager archiveManager = ArchiveManagerFactory.getArchiveManagerFactory(null);
      boolean success = true;
      if (exportResult != null)
      {
         dateloop: for (Date date : exportResult.getDates())
         {

            ExportIndex exportIndex = exportResult.getExportIndex(date);
                        
            Serializable key = archiveManager.open(date, exportIndex);
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
         if (dumpLocation == null)
         {
            purge(session);
         }
      }
      return success;
   }
   
   private void exportBatch(Session session)
   {
      List<Long> allIds = exportMetaData.getAllProcessesForExport(dumpLocation != null);
      if (CollectionUtils.isNotEmpty(allIds))
      {
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("Exporting " + allIds.size() + " processInstances");
         }
         if (exportResult == null)
         {
            exportResult = new ExportResult(dumpLocation);
         }
         ProcessElementExporter exporter = new ProcessElementExporter(exportResult,
               dumpLocation == null);
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
            exportResult = new ExportResult(dumpLocation);
         }
         exportResult.close();
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
         exportResult = new ExportResult(dumpLocation);
      }
      exportResult.setExportModel(exportModel);
   }

   private void query(Session session)
   {
      filter.validateDates();

      exportMetaData = new ExportMetaData();
      if (CollectionUtils.isNotEmpty(filter.getModelIds()))
      {
         List<Integer> modelOids = ExportImportSupport.findModelOids(filter.getModelIds());
         filter.getModelOids().clear();
         filter.getModelOids().addAll(modelOids);
      }
      else if (CollectionUtils.isEmpty(filter.getModelOids()))
      {
         filter.getModelOids().addAll(ExportImportSupport.getActiveModelOids());
      }
      if (CollectionUtils.isNotEmpty(filter.getModelOids()))
      {
         if (CollectionUtils.isNotEmpty(filter.getProcessDefinitionIds()))
         {
            List<Integer> processDefinitionOids = ExportImportSupport.findProcessDefinitionOids(filter.getProcessDefinitionIds());
            List<Long> oids = findRootProcesses(session, processDefinitionOids);
            // if no process instances are found we add and invalid processInstance, 
            // since our filtering uses an AND conjunction no results should be returned
            if (CollectionUtils.isEmpty(oids))
            {
               oids = Arrays.asList(-1L);
            }
            if (filter.getProcessInstanceOids() == null)
            {
               filter.setProcessInstanceOids(oids);
            }
            else
            {
               filter.getProcessInstanceOids().addAll(oids);
            }
         }
         List<Long> rootsNotAddedYet = findExportInstances(session);
         // in case a subprocess matched and could not be added, add it's process tree now
         if (!rootsNotAddedYet.isEmpty())
         {
            filter = new ArchiveFilter(null, null, rootsNotAddedYet, filter.getModelOids(), null, null, null);
            findExportInstances(session);
         }
      }
   }

   private List<Long> findRootProcesses(Session session, List<Integer> processDefinitionOids)
   {
      List<Long> result = new ArrayList<Long>();

      if (CollectionUtils.isNotEmpty(processDefinitionOids))
      {
         QueryDescriptor query = QueryDescriptor.from(ProcessInstanceBean.class).select(
               new Column[] {ProcessInstanceBean.FR__OID});
         AndTerm processDefRestriction = Predicates.andTerm(Predicates.inList(ProcessInstanceBean.FR__PROCESS_DEFINITION,
               processDefinitionOids), Predicates.isEqual(
                     ProcessInstanceBean.FR__ROOT_PROCESS_INSTANCE, ProcessInstanceBean.FR__OID));
         query.where(processDefRestriction);
         
         ResultSet rs = session.executeQuery(query);
         try
         {
            while (rs.next())
            {
               Long oid = rs.getBigDecimal(ProcessInstanceBean.FIELD__OID).longValue();
               result.add(oid);
            }
         }
         catch (SQLException e)
         {
            throw new IllegalStateException("Can't find process instances for processdefinition", e);
         }
         finally
         {
            QueryUtils.closeResultSet(rs);
         }
      }
      return result;
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
         exportResult = new ExportResult(dumpLocation);
      }
      if (exportResult.hasExportModel())
      {
         exportBatch(session);
      }
   }

   /**
    * returns any root processes that could not be added first time around and has to be queried for again,
    * e.g when descriptor of a subprocess matched filter criteria
    * @param session
    * @return
    */
   private List<Long> processQueryResults(Session session, QueryDescriptor query)
   {
      ResultSet rs = session.executeQuery(query);
      // store root processInstanceOids that matched by descriptor, so we can add it's subs
      List<Long> descriptorMatchedRootOids = new ArrayList<Long>();
      // store root processInstanceOids where the subProcess matched by descriptor so we can add root and siblings
      List<Long> descriptorMatchedSubRootOids = new ArrayList<Long>();
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
            if (filter.getDescriptors() != null && filter.getDescriptors().size() > 0)
            {
               if (descriptorMatchedRootOids.contains(rootOid))
               {
                  isInFilter = true;
               }
               else
               {
                  IProcessInstance processInstance = ProcessInstanceBean.findByOID(oid);
                  IProcessDefinition processDefinition = processInstance
                        .getProcessDefinition();
                  Map<String, Object> pathValues = ExportImportSupport
                        .getDescriptors(processInstance, processDefinition, filter
                              .getDescriptors().keySet());
                  for (String id : pathValues.keySet())
                  {
                     if (filter.getDescriptors().get(id).equals(pathValues.get(id)))
                     {
                        // we do or logic, as soon as one descriptor matches we have a match
                        isInFilter = true;
                        break;
                     }
                  }
               }
              
               if (isInFilter)
               {
                  // the root process matched, remember this so we can automatically add it's subs
                  if (oid.equals(rootOid))
                  {
                     descriptorMatchedRootOids.add(oid);
                  }
                  else
                  {
                     // if the match is by subProcess, we can't add subprocess now since root is not added.
                     // we will need to iterate of rs again to add these
                     isInFilter = false;
                     descriptorMatchedSubRootOids.add(rootOid);
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
      return descriptorMatchedSubRootOids;
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

   /**
    * returns any root processes that could not be added first time around and has to be queried for again,
    * e.g when descriptor of a subprocess matched filter criteria
    * @param session
    * @return
    */
   private List<Long> findExportInstances(Session session)
   {
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("Received " + filter.getProcessInstanceOids().size() + " oids to export");
         LOGGER.debug("Received " + filter.getModelOids().size() + " modelIds to export");
      }

      QueryDescriptor query = getBaseQuery();

      ComparisonTerm processStateRestriction = Predicates.inList(
            ProcessInstanceBean.FR__STATE, EXPORT_STATES);
      
      ComparisonTerm modelRestriction = Predicates.inList(ProcessInstanceBean.FR__MODEL,
            filter.getModelOids());
      ComparisonTerm processDefinitionRestriction = Predicates.greaterThan(
            ProcessInstanceBean.FR__PROCESS_DEFINITION, 0);

      AndTerm whereTerm;
      if (dumpLocation != null)
      {
         // TODO Improve by adding restriction on uuid ProcessInstanceProperty.FR__STRING_VALUE:
         // (not like currentArchiveId_processId_starttime in long) or is null
         // currently dont know how to add such complex like condition
         whereTerm = Predicates.andTerm(modelRestriction,
            processDefinitionRestriction);
      }
      else
      {
         whereTerm = Predicates.andTerm(processStateRestriction, modelRestriction,
            processDefinitionRestriction);
      }

      if (CollectionUtils.isNotEmpty(filter.getProcessInstanceOids()))
      {
         List<List<Long>> splitList = org.eclipse.stardust.common.CollectionUtils.split(filter.getProcessInstanceOids(), SQL_IN_CHUNK_SIZE);
         MultiPartPredicateTerm rootOidTerm = new OrTerm();
         for (List<Long> list : splitList)
         {
            rootOidTerm.add(Predicates.inList(ProcessInstanceBean.FR__ROOT_PROCESS_INSTANCE, list));
         }
         whereTerm.add(rootOidTerm);
      }
      if (filter.getFromDate() != null && filter.getToDate() != null)
      {
         AndTerm dateRestriction;
         if (dumpLocation != null)
         {
            dateRestriction = Predicates.andTerm(Predicates.greaterOrEqual(
               ProcessInstanceBean.FR__START_TIME, this.filter.getFromDate().getTime()), Predicates
               .lessOrEqual(ProcessInstanceBean.FR__START_TIME,
                     this.filter.getToDate().getTime()));
         }
         else
         {
            dateRestriction = Predicates.andTerm(Predicates.greaterOrEqual(
                  ProcessInstanceBean.FR__TERMINATION_TIME, this.filter.getFromDate().getTime()), Predicates
                  .lessOrEqual(ProcessInstanceBean.FR__TERMINATION_TIME,
                        this.filter.getToDate().getTime()));
         }
         whereTerm.add(dateRestriction);
      }
      query.where(whereTerm);
      // order by start time since root processes must be first
      query.orderBy(ProcessInstanceBean.FR__START_TIME, ProcessInstanceBean.FR__OID);
      return processQueryResults(session, query);
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
       * Takes an export result and persists it to an archive, deletes processes it if the archive is not a dump
       */
      ARCHIVE,
      /**
       * Takes messages that has been read from a JMS queue and archives them, then deletes them 
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
       * @param dumpLocation
       * @return Combined list of processInstanceOids for root processes and subprocesses
       */
      public List<Long> getAllProcessesForExport(boolean isDump)
      {
         List<Long> allIds = new ArrayList<Long>();
         if (rootToSubProcesses != null)
         {
            for (Long key : rootToSubProcesses.keySet())
            {
               if (oidsToUuids.get(key) == null || isDump)
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
