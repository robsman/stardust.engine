package org.eclipse.stardust.test.archive;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;

import org.eclipse.stardust.engine.core.persistence.archive.*;

public class MemoryArchive implements IArchive
{
   private ExportIndex exportIndex;
   private ExportModel exportModel;
   
   private HashMap<Long, byte[]> dataByProcess;
   private HashMap<String, byte[]> documentData;
   
   private String key;
   private Date date;

   public MemoryArchive(String key, Date date, HashMap<Long, byte[]> dataByProcess, String modelData, String indexString, HashMap<String, byte[]> documentData)
   {
      this.dataByProcess = dataByProcess;
      Gson gson = ExportImportSupport.getGson();
      exportIndex = gson.fromJson(new String(indexString), ExportIndex.class);
      exportModel = gson.fromJson(new String(modelData), ExportModel.class);
      this.documentData = documentData;
      this.key = key;
      this.date = date;
   }

   @Override
   public Serializable getArchiveKey()
   {
      return key;
   }
   
   public Date getDate()
   {
      return date;
   }

   public ExportModel getExportModel()
   {
      return exportModel;
   }

   public void setModelData(ExportModel exportModel)
   {
      this.exportModel = exportModel;
   }

   public HashMap<Long, byte[]> getDataByProcess()
   {
      return dataByProcess;
   }
   
   @Override
   public byte[] getDocumentContent(String documentName)
   {
      byte[] result = documentData.get(documentName);
      return result;
   }

   @Override
   public DocumentMetaData getDocumentProperties(String documentName)
   {
      String metaName = ExportImportSupport.getDocumentMetaDataName(documentName);
      byte[] raw = documentData.get(metaName);
      DocumentMetaData result;
      if (raw != null)
      {
         result = ExportImportSupport.getGson().fromJson(new String(raw), DocumentMetaData.class);
      }
      else
      {
         result = null;
      }
      return result;
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
   
   @Override
   public String getDumpLocation()
   {
      return getExportIndex().getDumpLocation();
   }
}