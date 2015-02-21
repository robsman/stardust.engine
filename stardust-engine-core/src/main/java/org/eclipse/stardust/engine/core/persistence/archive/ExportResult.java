package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.collections.CollectionUtils;

import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;
import org.eclipse.stardust.engine.core.persistence.jms.ByteArrayBlobBuilder;
import org.eclipse.stardust.engine.core.persistence.jms.ProcessBlobWriter;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;

public class ExportResult implements Serializable
{
   private static final long serialVersionUID = 1L;

   private final HashMap<Date, byte[]> resultsByDate;

   private transient final HashMap<Date, Map<Long, Map<Class, List<Persistent>>>> dateToPersistents = new HashMap<Date, Map<Long, Map<Class, List<Persistent>>>>();

   private transient final Map<Long, Date> piOidsToDate = new HashMap<Long, Date>();
   
   private List<Long> processInstanceOids;

   private boolean open = true;

   private byte[] modelData;

   private HashMap<Date, List<Long>> processIds = new HashMap<Date, List<Long>>();
   private HashMap<Date, List<Integer>> processLengths = new HashMap<Date, List<Integer>>();

   public ExportResult(byte[] modelData, HashMap<Date, byte[]> resultsByDate, HashMap<Date, List<Long>> processIds, HashMap<Date, List<Integer>> processLengths)
   {
      this.modelData = modelData;
      this.resultsByDate = resultsByDate;
      this.processIds = processIds;
      this.processLengths = processLengths;
      processInstanceOids = new ArrayList<Long>();
      for (Date date : processIds.keySet())
      {
         processInstanceOids.addAll(processIds.get(date));
      }
      this.open = false;
   }

   public ExportResult()
   {
      this.resultsByDate = new HashMap<Date, byte[]>();
   }

   public void addResult(ProcessInstanceBean process)
   {
      if (open)
      {
         Date indexDate;
         if (process.getOID() == process.getRootProcessInstanceOID())
         {
            indexDate = ExportImportSupport.getIndexDateTime(process.getStartTime());
         }
         else
         {
            indexDate = ExportImportSupport.getIndexDateTime(process
                  .getRootProcessInstance().getStartTime());
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
            throw new IllegalStateException("Persistent is linked to processInstanceOid " + processInstanceOid
                  + " but that process is not in this batch. Possible incorrect processInstanceOid. Persistent Type: " + persistent.getClass());
         }
         if (indexDate == null)
         {
            throw new IllegalStateException("Persistent is linked to processInstanceOid " + processInstanceOid
                  + " but no start date for that process determined.");
         }
         Map<Long, Map<Class, List<Persistent>>> processPersistentByTypeMap = dateToPersistents
               .get(indexDate);
         if (processPersistentByTypeMap == null)
         {
            processPersistentByTypeMap = new HashMap<Long, Map<Class, List<Persistent>>>();
            dateToPersistents.put(indexDate, processPersistentByTypeMap);
         }
         Map<Class, List<Persistent>> persistentByTypeMap = processPersistentByTypeMap.get(processInstanceOid);
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
            byte[] result = new byte[]{};
            for (Long processInstanceOid : processPersistentByTypeMap.keySet())
            {
               ByteArrayBlobBuilder blobBuilder = new ByteArrayBlobBuilder();
               blobBuilder.init(null);
               Map<Class, List<Persistent>> persistentsByClass = processPersistentByTypeMap.get(processInstanceOid);
               for (Class type : persistentsByClass.keySet())
               {
                  TypeDescriptor td = TypeDescriptor.get(type);
                  ProcessBlobWriter.writeInstances(blobBuilder, td,
                        persistentsByClass.get(type));
               }
               blobBuilder.persistAndClose();
               byte[] processData = blobBuilder.getBlob();
               List<Long> processIdsForDate = processIds.get(indexDate);
               List<Integer> processLengthsForDate = processLengths.get(indexDate);
               if (processIdsForDate == null)
               {
                  processIdsForDate = new ArrayList<Long>();
                  processLengthsForDate = new ArrayList<Integer>();
                  processIds.put(indexDate, processIdsForDate);
                  processLengths.put(indexDate, processLengthsForDate);
               }
               processIdsForDate.add(processInstanceOid);
               processLengthsForDate.add(processData.length);
               result = ExportImportSupport.addAll(result, processData);
            }
            resultsByDate.put(indexDate, result);
            processInstanceOids = new ArrayList<Long>();
            processInstanceOids.addAll(piOidsToDate.keySet());
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
   
   public List<Integer> getProcessLengths(Date startDate)
   {
      if (open)
      {
         throw new IllegalStateException("ExportResult is open. Close it first.");
      }
      Date indexDate = ExportImportSupport.getIndexDateTime(startDate);
      return processLengths.get(indexDate);
   }

   public List<Long> getProcessIds(Date startDate)
   {
      if (open)
      {
         throw new IllegalStateException("ExportResult is open. Close it first.");
      }
      Date indexDate = ExportImportSupport.getIndexDateTime(startDate);
      return processIds.get(indexDate);
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

   public List<Long> getAllProcessIds()
   {
      if (open)
      {
         throw new IllegalStateException("ExportResult is open. Close it first.");
      }
      return processInstanceOids;
   }

   public String getIndex(Date date)
   {
      return getProcessIds(date).toString();
   }

}