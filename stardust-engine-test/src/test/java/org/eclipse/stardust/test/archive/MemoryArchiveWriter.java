package org.eclipse.stardust.test.archive;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.activemq.util.ByteArrayInputStream;
import org.apache.commons.io.IOUtils;
import org.springframework.util.StringUtils;

import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.core.persistence.archive.ArchiveManagerFactory;
import org.eclipse.stardust.engine.core.persistence.archive.ExportIndex;
import org.eclipse.stardust.engine.core.persistence.archive.ExportResult;
import org.eclipse.stardust.engine.core.persistence.archive.IArchiveWriter;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

public class MemoryArchiveWriter implements IArchiveWriter
{
   private static final String EXT_JSON = ".json";
   
   private static final String FILENAME_DOCUMENT_META_SUFFIX = "_meta";
   
   protected HashMap<String, HashMap<String, HashMap<Long, byte[]>>> repo;

   protected HashMap<String, HashMap<String, byte[]>> repoData;

   protected HashMap<String, HashMap<String, String>> dateModel;
   
   protected HashMap<String, HashMap<String, String>> modelXpdl;

   protected HashMap<String, HashMap<String, String>> dateIndex;

   protected HashMap<String, HashMap<String, Date>> dateArchiveKey;
   
   protected HashMap<String,HashMap<Long, HashMap<String, byte[]>>> docRepo;
   
   private String archiveManagerId;

   private String dateFormat;
   
   private boolean auto;

   private boolean autoDocs;
   
   private static int keyCounter = 0;
   
   public MemoryArchiveWriter(Map<String, String> preferences)
   {
      String id = preferences.get(ArchiveManagerFactory.CARNOT_ARCHIVE_WRITER_MANAGER_ID);

      if (StringUtils.isEmpty(id))
      {
         throw new IllegalArgumentException(
               ArchiveManagerFactory.CARNOT_ARCHIVE_WRITER_MANAGER_ID
                     + " must be provided for MemoryArchiveManger archive type");
      }
      String dateFormat = preferences.get(ArchiveManagerFactory.CARNOT_ARCHIVE_WRITER_DATE_FORMAT);
      boolean auto = "true".equals(preferences.get(ArchiveManagerFactory.CARNOT_ARCHIVE_WRITER_AUTO_ARCHIVE));
      boolean autoDocs = "true".equals(preferences.get(ArchiveManagerFactory.CARNOT_ARCHIVE_WRITER_AUTO_ARCHIVE_DOCUMENTS));
      repo = new HashMap<String, HashMap<String, HashMap<Long, byte[]>>>();
      repoData = new HashMap<String, HashMap<String, byte[]>>();
      dateModel = new HashMap<String, HashMap<String, String>>();
      modelXpdl = new HashMap<String, HashMap<String, String>>();
      dateIndex = new HashMap<String, HashMap<String, String>>();
      dateArchiveKey = new HashMap<String, HashMap<String, Date>>();
      docRepo = new HashMap<String, HashMap<Long,HashMap<String,byte[]>>>();
      
      this.archiveManagerId = id;
      this.dateFormat = dateFormat;
      this.auto = auto;
      this.autoDocs = autoDocs;
   }
   
   @Override
   public String getArchiveManagerId()
   {
      return archiveManagerId;
   }
   
   @Override
   public String getDateFormat()
   {
      return dateFormat;
   }

   @Override
   public boolean isAutoArchive()
   {
      return auto;
   }

   @Override
   public Serializable open(Date indexDate, ExportIndex exportIndex)
   {
      synchronized (indexDate)
      {
         HashMap<String, HashMap<Long, byte[]>> partitionRepo = createPartitionRepo();
         HashMap<String, Date> keyDateMap = dateArchiveKey.get(SecurityProperties.getPartition().getId());
         String key = "key" + keyCounter++;
         keyDateMap.put(key, indexDate);
         partitionRepo.put(key, new HashMap<Long, byte[]>());
         return key;
      }
   }

   private HashMap<String, HashMap<Long, byte[]>> createPartitionRepo()
   {
      HashMap<String, HashMap<Long, byte[]>> partitionRepo = repo.get(SecurityProperties
            .getPartition().getId());
      if (partitionRepo == null)
      {
         partitionRepo = new HashMap<String, HashMap<Long, byte[]>>();
         repo.put(SecurityProperties.getPartition().getId(), partitionRepo);
         repoData.put(SecurityProperties.getPartition().getId(),
               new HashMap<String, byte[]>());
         dateModel.put(SecurityProperties.getPartition().getId(),
               new HashMap<String, String>());
         modelXpdl.put(SecurityProperties.getPartition().getId(),
               new HashMap<String, String>());
         dateIndex.put(SecurityProperties.getPartition().getId(),
               new HashMap<String, String>());
         docRepo.put(SecurityProperties.getPartition().getId(), new HashMap<Long, HashMap<String,byte[]>>());
         dateArchiveKey.put(SecurityProperties.getPartition().getId(),
               new HashMap<String, Date>());
      }
      return partitionRepo;
   }
   

   @Override
   public boolean add(Serializable key, byte[] results)
   {
      synchronized (key)
      {
         repoData.get(SecurityProperties.getPartition().getId()).put((String) key, results);
      }
      return true;
   }


   @Override
   public boolean addModelXpdl(Serializable dumpLocation, String uuid, String xpdl)
   {
      synchronized (uuid)
      {
         createPartitionRepo();
         HashMap<String, String> modelXpdls = modelXpdl.get(SecurityProperties.getPartition().getId());
         if (!modelXpdls.containsKey(uuid))
         {
            modelXpdls.put(uuid, xpdl);
         }
      }
      return true;
   }
   
   public boolean isModelExported(Serializable dumpLocation, String uuid)
   {
      createPartitionRepo();
      HashMap<String, String> modelXpdls = modelXpdl.get(SecurityProperties.getPartition().getId());
      return !modelXpdls.containsKey(uuid);
   }
   
   @Override
   public boolean addModel(Serializable key, String model)
   {
      synchronized (key)
      {
         dateModel.get(SecurityProperties.getPartition().getId())
               .put((String) key, model);
      }
      return true;

   }

   @Override
   public boolean close(Serializable key, Date indexDate, ExportResult exportResult)
   {
      synchronized (key)
      {
         List<Long> processInstanceOids = exportResult.getProcessInstanceOids(indexDate);
         HashMap<String, HashMap<Long, byte[]>> partitionRepo = repo.get(SecurityProperties
               .getPartition().getId());
         HashMap<String, byte[]> partitionRepoData = repoData.get(SecurityProperties
               .getPartition().getId());

         HashMap<Long, byte[]> hashMap = partitionRepo.get((String) key);
         byte[] data = partitionRepoData.get((String) key);
         BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(data));

         try
         {
            for (int i = 0; i < processInstanceOids.size(); i++)
            {
               byte[] process = new byte[exportResult.getProcessLengths(indexDate).get(i)];
               in.read(process);

               hashMap.put(processInstanceOids.get(i), process);
            }
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
         finally
         {
            IOUtils.closeQuietly(in);
         }
      }
      return true;
   }
   
   @Override
   public boolean addIndex(Serializable key, String indexData)
   {
      synchronized (key)
      {
         dateIndex.get(SecurityProperties.getPartition().getId()).put((String) key,
               indexData);
      }
      return true;
   }
   

   @Override
   public boolean addDocument(Serializable key, long piOid, Document doc, byte[] content,
         String metaData)
   {
      boolean success;
      synchronized (key)
      {
         if (key != null && content != null)
         {
            HashMap<Long, HashMap<String, byte[]>> partitionDocs = docRepo.get(SecurityProperties.getPartition().getId());
            HashMap<String, byte[]> processDocs = partitionDocs.get(piOid);
            if (processDocs == null)
            {
               processDocs = new HashMap<String, byte[]>();
               partitionDocs.put(piOid, processDocs);
            }
            
            int lastIndex = doc.getName().lastIndexOf('.');
            String ext = doc.getName().substring(lastIndex);
            String docName = doc.getName().substring(0, lastIndex);
            
            String name = docName + "_" + doc.getRevisionName() + ext;
            String jsonNname = docName + "_" + doc.getRevisionName() + FILENAME_DOCUMENT_META_SUFFIX + EXT_JSON;
            processDocs.put(name, content);
            processDocs.put(jsonNname, metaData.getBytes());
            success = true;
         }
         else
         {
            success = false;
         }
      }
      return success;
   }

   public void clear()
   {
      repo.clear();
      repoData.clear();
      dateModel.clear();
      dateIndex.clear();
      docRepo.clear();
      dateArchiveKey.clear();
      modelXpdl.clear();
      keyCounter = 0;
   }
}
