package org.eclipse.stardust.test.archive;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;

import org.eclipse.stardust.engine.core.persistence.archive.ExportImportSupport;
import org.eclipse.stardust.engine.core.persistence.archive.ExportIndex;
import org.eclipse.stardust.engine.core.persistence.archive.IArchive;

public class MemoryArchive implements IArchive
{
   private byte[] modelData;
   
   private ExportIndex exportIndex;
   
   private HashMap<Long, byte[]> dataByProcess;
   
   private Date key;

   public MemoryArchive(Date key, HashMap<Long, byte[]> dataByProcess, byte[] modelData, String indexString)
   {
      this.dataByProcess = dataByProcess;
      this.modelData = modelData;
      Gson gson = ExportImportSupport.getGson();
      exportIndex = gson.fromJson(new String(indexString), ExportIndex.class);
      this.key = key;
   }

   @Override
   public Serializable getArchiveKey()
   {
      return key;
   }

   public byte[] getModelData()
   {
      return modelData;
   }

   public void setModelData(byte[] modelData)
   {
      this.modelData = modelData;
   }

   @Override
   public byte[] getData(List<Long> processInstanceOids)
   {
      byte[] result = new byte[]{};
            
      if (dataByProcess != null)
      {
         for (Long processInstanceOid : processInstanceOids)
         {
            if (getExportIndex().contains(processInstanceOid))
            {
               byte[] data = dataByProcess.get(processInstanceOid);
               result = ExportImportSupport.addAll(result, data);
            }
         }
         if (result.length == 0)
         {
            result = null;
         }
         
      }
      return result;
   }

   @Override
   public ExportIndex getExportIndex()
   {
      return exportIndex;
   }

   @Override
   public String getArchiveManagerId()
   {
      return getExportIndex().getArchiveManagerId();
   }
}