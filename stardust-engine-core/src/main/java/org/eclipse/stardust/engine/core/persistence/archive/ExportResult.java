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

public class ExportResult implements Serializable
{
   private static final long serialVersionUID = 1L;

   private final HashMap<Date, byte[]> resultsByDate;

   private transient final HashMap<Date, Map<Long, Map<Class, List<Persistent>>>> dateToPersistents = new HashMap<Date, Map<Long, Map<Class, List<Persistent>>>>();

   private transient final Map<Long, Date> piOidsToDate = new HashMap<Long, Date>();
   
   private transient final Map<Long, List<Long>> rootProcessToSubProcesses = new HashMap<Long, List<Long>>();

   private final Map<Date, ExportIndex> exportIndexByDate;
   

   private Set<Long> processInstanceOids;

   private boolean open = true;

   private byte[] modelData;

   public ExportResult(byte[] modelData, HashMap<Date, byte[]> resultsByDate,
         HashMap<Date, ExportIndex> exportIndexByDate)
   {
      this.modelData = modelData;
      this.resultsByDate = resultsByDate;
      this.exportIndexByDate = exportIndexByDate;
      processInstanceOids = new HashSet<Long>();
      for (Date date : exportIndexByDate.keySet())
      {
         processInstanceOids.addAll(exportIndexByDate.get(date).getProcessInstanceOids());
      }
      this.open = false;
   }

   public ExportResult()
   {
      this.resultsByDate = new HashMap<Date, byte[]>();
      this.exportIndexByDate = new HashMap<Date, ExportIndex>();
   }

   public void addResult(ProcessInstanceBean process)
   {
      if (open)
      {
         Date indexDate;
         IProcessInstance rootProcess;
         Long subId = null;
         if (process.getOID() == process.getRootProcessInstanceOID())
         {
            rootProcess = process.getProcessInstance();
         }
         else
         {
            rootProcess = process.getRootProcessInstance();
            subId = process.getOID();
         }
         indexDate = ExportImportSupport.getIndexDateTime(rootProcess.getStartTime());
         List<Long> subProcesses = rootProcessToSubProcesses.get(rootProcess.getOID());
         if (subProcesses == null)
         {
            subProcesses = new ArrayList<Long>();
            rootProcessToSubProcesses.put(rootProcess.getOID(), subProcesses);
         }
         if (subId != null)
         {
            subProcesses.add(subId);
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
               ExportIndex exportIndex = exportIndexByDate.get(indexDate);
               if (exportIndex == null)
               {
                  exportIndex = new ExportIndex();
                  exportIndexByDate.put(indexDate, exportIndex);
               }
               List<Long> subprocesses = rootProcessToSubProcesses.get(processInstanceOid);
               if (subprocesses != null)
               {
                  exportIndex.getRootProcessToSubProcesses().put(processInstanceOid, subprocesses);
               }
               exportIndex.getProcessInstanceOids().add(processInstanceOid);
               exportIndex.getProcessLengths().add(processData.length);
               result = ExportImportSupport.addAll(result, processData);
            }
            resultsByDate.put(indexDate, result);
            processInstanceOids = new HashSet<Long>();
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

   public Map<Long, List<Long>> getRootProcessToSubProcesses()
   {
      if (open)
      {
         throw new IllegalStateException("ExportResult is open. Close it first.");
      }
      return rootProcessToSubProcesses;
   }

   public Set<Long> getAllProcessIds()
   {
      if (open)
      {
         throw new IllegalStateException("ExportResult is open. Close it first.");
      }
      return processInstanceOids;
   }

}