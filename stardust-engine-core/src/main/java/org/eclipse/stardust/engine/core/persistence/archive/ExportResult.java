package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.collections.CollectionUtils;

import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceUtils;
import org.eclipse.stardust.engine.core.persistence.jms.ByteArrayBlobBuilder;
import org.eclipse.stardust.engine.core.persistence.jms.ProcessBlobWriter;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataValueBean;

/**
 * 
 * @author jsaayman
 *
 */
public class ExportResult implements Serializable
{
   private static final long serialVersionUID = 1L;

   private final HashMap<Date, byte[]> resultsByDate;

   private transient final HashMap<Date, Map<Long, Map<Class, List<Persistent>>>> dateToPersistents = new HashMap<Date, Map<Long, Map<Class, List<Persistent>>>>();

   private transient final Map<Long, Date> piOidsToDate = new HashMap<Long, Date>();

   private final Map<Date, ExportIndex> exportIndexByDate;

   private final Map<Date, List<Long>> processInstanceOidsByDate;

   private final Map<Date, List<Integer>> processLengthsByDate;

   private boolean open = true;

   private ExportModel exportModel;

   private final Set<Long> purgeProcessIds;

   private boolean isDump;

   public ExportResult(ExportModel exportModel, HashMap<Date, byte[]> resultsByDate,
         HashMap<Date, ExportIndex> exportIndexByDate,
         Map<Date, List<Long>> processInstanceOidsByDate,
         Map<Date, List<Integer>> processLengthsByDate, boolean isDump,
         Set<Long> purgeProcessIds)
   {
      this.exportModel = exportModel;
      this.resultsByDate = resultsByDate;
      this.exportIndexByDate = exportIndexByDate;
      this.processInstanceOidsByDate = processInstanceOidsByDate;
      this.processLengthsByDate = processLengthsByDate;
      this.purgeProcessIds = purgeProcessIds;
      this.open = false;
      this.isDump = isDump;
   }

   public ExportResult(boolean isDump)
   {
      this.resultsByDate = new HashMap<Date, byte[]>();
      this.exportIndexByDate = new HashMap<Date, ExportIndex>();
      this.processInstanceOidsByDate = new HashMap<Date, List<Long>>();
      this.processLengthsByDate = new HashMap<Date, List<Integer>>();
      this.purgeProcessIds = new HashSet<Long>();
      this.isDump = isDump;
   }

   private static void addExportProcess(ExportIndex exportIndex, IProcessInstance rootProcess,
         IProcessInstance subProcess)
   {
      
      List<Long> subProcesses = exportIndex.getRootProcessToSubProcesses().get(rootProcess.getOID());
      if (subProcesses == null)
      {
         subProcesses = new ArrayList<Long>();
         exportIndex.getRootProcessToSubProcesses().put(rootProcess.getOID(), subProcesses);
         addProcessDetail(exportIndex, rootProcess);
      }
      if (subProcess != null)
      {
         subProcesses.add(subProcess.getOID());
         addProcessDetail(exportIndex, subProcess);
      }
   }
   
   private static void addProcessDetail(ExportIndex exportIndex, IProcessInstance processInstance)
   {
      String uuid = ExportImportSupport.getUUID(processInstance);
      Map<String, String> descriptors = ExportImportSupport.getFormattedDescriptors(
            processInstance, null);
      String start = ExportImportSupport.formatDate(processInstance.getStartTime(), null);
      String end = ExportImportSupport.formatDate(processInstance.getTerminationTime(), null);
      
      exportIndex.setUuid(processInstance.getOID(), uuid);
      for (String field : descriptors.keySet())
      {
         exportIndex.addField(processInstance.getOID(), field, descriptors.get(field));
      }
      exportIndex.addField(processInstance.getOID(), ExportIndex.FIELD_START_DATE, start);
      exportIndex.addField(processInstance.getOID(), ExportIndex.FIELD_END_DATE, end);
   }

   private void addResult(IProcessInstance process)
   {
      if (open)
      {
         Date indexDate;
         IProcessInstance rootProcess;
         IProcessInstance subProcess;
         
         if (process.getOID() == process.getRootProcessInstanceOID())
         {
            subProcess = null;
            rootProcess = process;
            indexDate = ExportImportSupport.getIndexDateTime(process.getStartTime());
         }
         else
         {
            subProcess = process;
            rootProcess = process.getRootProcessInstance();
            indexDate = ExportImportSupport.getIndexDateTime(process
                  .getRootProcessInstance().getStartTime());
         }

         ExportIndex exportIndex = exportIndexByDate.get(indexDate);
         if (exportIndex == null)
         {
            exportIndex = new ExportIndex(ArchiveManagerFactory.getCurrentId(),
                  ArchiveManagerFactory.getDateFormat(), isDump);
            exportIndexByDate.put(indexDate, exportIndex);
         }
         
         addExportProcess(exportIndex, rootProcess, subProcess);
        
         if (indexDate == null)
         {
            throw new IllegalStateException("ProcessInstanceOid " + process.getOID()
                  + " - no start date for that process determined.");
         }
         piOidsToDate.put(process.getOID(), indexDate);
         addResult(process, process.getOID());
      }
      else
      {
         throw new IllegalStateException("ExportResult is closed.");
      }
   }

   private void addResult(Persistent persistent, long processInstanceOid)
   {
      if (open)
      {
         Date indexDate;
         if (piOidsToDate.containsKey(processInstanceOid))
         {
            indexDate = piOidsToDate.get(processInstanceOid);
         }
         else
         {
            throw new IllegalStateException(
                  "Persistent is linked to processInstanceOid "
                        + processInstanceOid
                        + " but that process is not in this batch. Possible incorrect processInstanceOid. Persistent Type: "
                        + persistent.getClass());
         }
         if (indexDate == null)
         {
            throw new IllegalStateException("Persistent is linked to processInstanceOid "
                  + processInstanceOid
                  + " but no start date for that process determined.");
         }
         Map<Long, Map<Class, List<Persistent>>> processPersistentByTypeMap = dateToPersistents
               .get(indexDate);
         if (processPersistentByTypeMap == null)
         {
            processPersistentByTypeMap = new HashMap<Long, Map<Class, List<Persistent>>>();
            dateToPersistents.put(indexDate, processPersistentByTypeMap);
         }
         Map<Class, List<Persistent>> persistentByTypeMap = processPersistentByTypeMap
               .get(processInstanceOid);
         if (persistentByTypeMap == null)
         {
            persistentByTypeMap = new HashMap<Class, List<Persistent>>();
            processPersistentByTypeMap.put(processInstanceOid, persistentByTypeMap);
         }
         List<Persistent> persistents = persistentByTypeMap.get(persistent.getClass());
         if (persistents == null)
         {
            persistents = new ArrayList<Persistent>();
            persistentByTypeMap.put(persistent.getClass(), persistents);
         }
         persistents.add(persistent);
      }
      else
      {
         throw new IllegalStateException("ExportResult is closed.");
      }
   }

   public void addResult(Session session, Persistent persistent)
   {
      long processInstanceOid = -1;
      if (persistent instanceof ProcessInstanceBean)
      {
         ProcessInstanceBean processInstance = (ProcessInstanceBean) persistent;
         addResult(processInstance);
      }
      else if (!(persistent instanceof ProcessInstanceScopeBean))
      {
         if (persistent instanceof IProcessInstanceAware)
         {
            processInstanceOid = ((IProcessInstanceAware) persistent)
                  .getProcessInstance().getOID();
         }
         else if (persistent instanceof IActivityInstanceAware)
         {
            processInstanceOid = ((IActivityInstanceAware) persistent)
                  .getActivityInstance().getProcessInstance().getOID();
         }
         else if (persistent instanceof LargeStringHolder)
         {
            LargeStringHolder str = ((LargeStringHolder) persistent);
            Long structureDataOid = str.getObjectID();
            if (StructuredDataValueBean.TABLE_NAME.equals(str.getDataType()))
            {
               StructuredDataValueBean dataBean = (StructuredDataValueBean) session
                     .findByOID(StructuredDataValueBean.class, structureDataOid);
               processInstanceOid = dataBean.getProcessInstance().getOID();
            }
            else if (DataValueBean.TABLE_NAME.equals(str.getDataType()))
            {
               DataValueBean dataBean = (DataValueBean) session.findByOID(
                     DataValueBean.class, structureDataOid);
               processInstanceOid = dataBean.getProcessInstance().getOID();
            }
            else
            {
               throw new IllegalStateException(
                     "Can't determine related process instance. LargeStringHolder type is :"
                           + str.getDataType());
            }
         }
         else
         {
            throw new IllegalStateException(
                  "Can't determine related process instance. Not a clob, IProcessInstanceAware or IActivityInstanceAware:"
                        + persistent.getClass().getName());
         }

         if (processInstanceOid == -1)
         {
            throw new IllegalStateException("Can't determine related process instance."
                  + persistent.getClass().getName());
         }
         addResult(persistent, processInstanceOid);
      }
   }
      
   public void close(Set<Persistent> persistents, Session session)
   {
      if (open)
      {
         persistents = TransientProcessInstanceUtils.processPersistents(session, null, persistents);
         List<Persistent> subs = new ArrayList<Persistent>();
         List<Persistent> other = new ArrayList<Persistent>();

         Set<Integer> modelOids = new HashSet<Integer>();
         for (Persistent persistent : persistents)
         {
            if (persistent instanceof ProcessInstanceBean)
            {
               ProcessInstanceBean process = (ProcessInstanceBean) persistent;
               purgeProcessIds.add(process.getOID());
               if (process.getRootProcessInstanceOID() == process.getOID())
               {
                  addResult(session, process);
                  modelOids.add(process.getProcessDefinition().getModel().getModelOID());
               }
               else
               {
                  subs.add(process);
               }
            }
            else
            {
               other.add(persistent);
            }
            
         }
         exportModel = ExportImportSupport.exportModels(modelOids);
         for (Persistent persistent : subs)
         {
            addResult(session, persistent);
         }
         for (Persistent persistent : other)
         {
            addResult(session, persistent);
         }
         close();
      }
      else
      {
         throw new IllegalStateException("ExportResult is not open.");
      }
      open = false;
   }

   public void close()
   {
      if (open)
      {
         for (Date indexDate : dateToPersistents.keySet())
         {

            Map<Long, Map<Class, List<Persistent>>> processPersistentByTypeMap = dateToPersistents
                  .get(indexDate);
            byte[] result = new byte[] {};
            for (Long processInstanceOid : processPersistentByTypeMap.keySet())
            {
               ByteArrayBlobBuilder blobBuilder = new ByteArrayBlobBuilder();
               blobBuilder.init(null);
               Map<Class, List<Persistent>> persistentsByClass = processPersistentByTypeMap
                     .get(processInstanceOid);
               for (Class type : persistentsByClass.keySet())
               {
                  TypeDescriptor td = TypeDescriptor.get(type);
                  ProcessBlobWriter.writeInstances(blobBuilder, td,
                        persistentsByClass.get(type));
               }
               blobBuilder.persistAndClose();
               byte[] processData = blobBuilder.getBlob();

               // they are created already when adding, now just add oids and lengths
               List<Long> processInstanceOids = processInstanceOidsByDate.get(indexDate);
               List<Integer> processLengths = processLengthsByDate.get(indexDate);
               if (processInstanceOids == null)
               {
                  processInstanceOids = new ArrayList<Long>();
                  processLengths = new ArrayList<Integer>();
                  processInstanceOidsByDate.put(indexDate, processInstanceOids);
                  processLengthsByDate.put(indexDate, processLengths);
               }
               
               processInstanceOids.add(processInstanceOid);
               processLengths.add(processData.length);
               result = ExportImportSupport.addAll(result, processData);
            }
            resultsByDate.put(indexDate, result);
         }
         open = false;
      }
   }

   public Set<Date> getDates()
   {
      return resultsByDate.keySet();
   }

   public byte[] getResults(Date startDate)
   {
      if (open)
      {
         throw new IllegalStateException("ExportResult is open. Close it first.");
      }
      Date indexDate = ExportImportSupport.getIndexDateTime(startDate);
      byte[] results = resultsByDate.get(indexDate);
      return results;
   }

   public ExportIndex getExportIndex(Date startDate)
   {
      if (open)
      {
         throw new IllegalStateException("ExportResult is open. Close it first.");
      }
      Date indexDate = ExportImportSupport.getIndexDateTime(startDate);
      return exportIndexByDate.get(indexDate);
   }

   public boolean hasExportModel()
   {
      return exportModel != null;
   }

   public boolean hasExportData()
   {
      return CollectionUtils.isNotEmpty(resultsByDate.keySet());
   }

   public void setExportModel(ExportModel exportModel)
   {
      this.exportModel = exportModel;
   }

   public ExportModel getExportModel()
   {
      return exportModel;
   }

   public Set<Long> getPurgeProcessIds()
   {
      return purgeProcessIds;
   }
   
   public List<Long> getProcessInstanceOids(Date startDate)
   {
      if (open)
      {
         throw new IllegalStateException("ExportResult is open. Close it first.");
      }
      Date indexDate = ExportImportSupport.getIndexDateTime(startDate);
      return processInstanceOidsByDate.get(indexDate);
   }

   public List<Integer> getProcessLengths(Date startDate)
   {
      if (open)
      {
         throw new IllegalStateException("ExportResult is open. Close it first.");
      }
      Date indexDate = ExportImportSupport.getIndexDateTime(startDate);
      return processLengthsByDate.get(indexDate);
   }
}