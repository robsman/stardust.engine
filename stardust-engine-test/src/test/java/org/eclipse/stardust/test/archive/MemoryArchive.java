package org.eclipse.stardust.test.archive;

import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.eclipse.stardust.engine.core.persistence.archive.ExportImportSupport;
import org.eclipse.stardust.engine.core.persistence.archive.ExportIndex;
import org.eclipse.stardust.engine.core.persistence.archive.IArchive;

public class MemoryArchive implements IArchive
{
   private byte[] modelData;
   
   private ExportIndex exportIndex;
   
   private HashMap<Long, byte[]> dataByProcess;

   public MemoryArchive(HashMap<Long, byte[]> dataByProcess, byte[] modelData, String indexString)
   {
      this.dataByProcess = dataByProcess;
      this.modelData = modelData;
      GsonBuilder gsonBuilder = new GsonBuilder();
      gsonBuilder.excludeFieldsWithoutExposeAnnotation();
      Gson gson = gsonBuilder.create();
      exportIndex = gson.fromJson(new String(indexString), ExportIndex.class);
   }

   @Override
   public String getName()
   {
      return "MemoryArchive";
   }

   public byte[] getData()
   {
      byte[] results = new byte[]{};
      for (Long oid : exportIndex.getProcessInstanceOids())
      {
         results = ExportImportSupport.addAll(results, dataByProcess.get(oid));
      }
      return results;
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
            if (getExportIndex().getProcessInstanceOids().contains(processInstanceOid))
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
   
}