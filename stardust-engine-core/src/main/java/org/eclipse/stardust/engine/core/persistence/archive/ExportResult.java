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

   private final HashMap<Date, Map<Class, List<Persistent>>> dateToPersistents = new HashMap<Date, Map<Class, List<Persistent>>>();

   private final Map<Long, Date> piOidsToDate = new HashMap<Long, Date>();

   private boolean open = true;

   private byte[] modelData;

   public ExportResult()
   {
      this.resultsByDate = new HashMap<Date, byte[]>();
   }

   public void addResult(ProcessInstanceBean process)
   {
      Date indexDate = ExportImportSupport.getIndexDateTime(process.getStartTime());
      piOidsToDate.put(process.getOID(), indexDate);
      addResult(process, process.getOID());
   }

   public void addResult(Persistent persistent, long processInstanceOid)
   {
      Date indexDate = piOidsToDate.get(processInstanceOid);
      Map<Class, List<Persistent>> persistentByTypeMap = dateToPersistents.get(indexDate);
      if (persistentByTypeMap == null)
      {
         persistentByTypeMap = new HashMap<Class, List<Persistent>>();
         dateToPersistents.put(indexDate, persistentByTypeMap);
      }
      List<Persistent> persistents = persistentByTypeMap.get(persistent.getClass());
      if (persistents == null)
      {
         persistents = new ArrayList<Persistent>();
         persistentByTypeMap.put(persistent.getClass(), persistents);
      }
      persistents.add(persistent);
   }

   public void close()
   {
      if (open)
      {
         for (Date indexDate : dateToPersistents.keySet())
         {
            ByteArrayBlobBuilder blobBuilder = new ByteArrayBlobBuilder();
            blobBuilder.init(null);
            Map<Class, List<Persistent>> persistentsByClass = dateToPersistents.get(indexDate);
            for (Class type : persistentsByClass.keySet())
            {
               TypeDescriptor td = TypeDescriptor.get(type);
               ProcessBlobWriter.writeInstances(blobBuilder, td, persistentsByClass.get(type));
            }
            blobBuilder.persistAndClose();
            resultsByDate.put(indexDate, blobBuilder.getBlob());
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

}