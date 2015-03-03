package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.collections.CollectionUtils;

import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;
import org.eclipse.stardust.engine.core.persistence.jms.ByteArrayBlobBuilder;
import org.eclipse.stardust.engine.core.persistence.jms.ProcessBlobWriter;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;

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
   
   private byte[] modelData;
   
   private final Set<Long> purgeProcessIds;
   
   private boolean isDump;

   public ExportResult(byte[] modelData, HashMap<Date, byte[]> resultsByDate,
         HashMap<Date, ExportIndex> exportIndexByDate, Map<Date, 
         List<Long>> processInstanceOidsByDate, Map<Date, List<Integer>> processLengthsByDate,
         boolean isDump, Set<Long> purgeProcessIds)
   {
      this.modelData = modelData;
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


   private static ExportProcess createExportProcess(IProcessInstance processInstance)
   {
      String uuid = ExportImportSupport.getUUID(processInstance);
      ExportProcess result = new ExportProcess(processInstance.getOID(), uuid);
      return result;
   }
   
   public void addResult(ProcessInstanceBean process)
   {
      if (open)
      {
         Date indexDate;
         ExportProcess rootProcess;
         ExportProcess subProcess = null;
         if (process.getOID() == process.getRootProcessInstanceOID())
         {
            rootProcess = createExportProcess(process.getProcessInstance());
            indexDate = ExportImportSupport.getIndexDateTime(process.getStartTime());
         }
         else
         {
            indexDate = ExportImportSupport.getIndexDateTime(process.getRootProcessInstance().getStartTime());
            rootProcess = createExportProcess(process.getRootProcessInstance());
            subProcess = createExportProcess(process.getProcessInstance());
         }

         ExportIndex exportIndex = exportIndexByDate.get(indexDate);
         if (exportIndex == null)
         {
            exportIndex = new ExportIndex(ArchiveManagerFactory.getCurrentId(), isDump);
            exportIndexByDate.put(indexDate, exportIndex);
         }
         List<ExportProcess> subProcesses = exportIndex.getRootProcessToSubProcesses().get(rootProcess);
         if (subProcesses == null)
         {
            subProcesses = new ArrayList<ExportProcess>();
            exportIndex.getRootProcessToSubProcesses().put(rootProcess, subProcesses);
         }
         if (subProcess != null)
         {
            subProcesses.add(subProcess);
         }
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

   public void addResult(Persistent persistent, long processInstanceOid)
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
               
               //they are created already when adding, now just add oids and lengths
               List<Long> processInstanceOids = processInstanceOidsByDate.get(indexDate);
               List<Integer> processLengths= processLengthsByDate.get(indexDate);
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

   public boolean hasModelData()
   {
      return modelData != null;
   }

   public boolean hasExportData()
   {
      return CollectionUtils.isNotEmpty(resultsByDate.keySet());
   }

   public void setModelData(byte[] modelData)
   {
      this.modelData = modelData;
   }

   public byte[] getModelData()
   {
      return modelData;
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