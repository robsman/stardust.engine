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

import org.eclipse.stardust.engine.core.persistence.archive.*;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

public class MemoryArchiveWriter implements IArchiveWriter
{
   protected HashMap<String, HashMap<String, HashMap<Long, byte[]>>> repo;

   protected HashMap<String, HashMap<String, HashMap<String, byte[]>>> repoDoc;

   private HashMap<String, HashMap<String, byte[]>> allData;

   private HashMap<String, HashMap<String, byte[]>> allDocs;

   protected HashMap<String, HashMap<String, String>> dateModel;
   
   protected HashMap<String, HashMap<String, String>> modelXpdl;

   protected HashMap<String, HashMap<String, String>> dateIndex;

   protected HashMap<String, HashMap<String, Date>> dateArchiveKey;
   
   
   private String archiveManagerId;

   private String dateFormat;
   
   private boolean auto;

   private final DocumentOption documentOption;
   
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
      this.documentOption = DocumentOption.valueOf(preferences.get(ArchiveManagerFactory.CARNOT_ARCHIVE_WRITER_AUTO_ARCHIVE_DOCUMENTS));
      repo = new HashMap<String, HashMap<String, HashMap<Long, byte[]>>>();
      repoDoc = new HashMap<String, HashMap<String, HashMap<String, byte[]>>>();
      allDocs = new HashMap<String, HashMap<String, byte[]>>();
      allData = new HashMap<String, HashMap<String, byte[]>>();
      dateModel = new HashMap<String, HashMap<String, String>>();
      modelXpdl = new HashMap<String, HashMap<String, String>>();
      dateIndex = new HashMap<String, HashMap<String, String>>();
      dateArchiveKey = new HashMap<String, HashMap<String, Date>>();

      this.archiveManagerId = id;
      this.dateFormat = dateFormat;
      this.auto = auto;
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
   public DocumentOption getDocumentOption()
   {
      return documentOption;
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
         repoDoc.get(SecurityProperties.getPartition().getId()).put(key, new HashMap<String, byte[]>());
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
         repoDoc.put(SecurityProperties.getPartition().getId(), new HashMap<String, HashMap<String, byte[]>>());
         allData.put(SecurityProperties.getPartition().getId(),
               new HashMap<String, byte[]>());
         allDocs.put(SecurityProperties.getPartition().getId(),
               new HashMap<String, byte[]>());
         dateModel.put(SecurityProperties.getPartition().getId(),
               new HashMap<String, String>());
         modelXpdl.put(SecurityProperties.getPartition().getId(),
               new HashMap<String, String>());
         dateIndex.put(SecurityProperties.getPartition().getId(),
               new HashMap<String, String>());
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
         allData.get(SecurityProperties.getPartition().getId()).put((String) key, results);
      }
      return true;
   }
   
   @Override
   public boolean addDocuments(Serializable key, byte[] results)
   {
      synchronized (key)
      {
         allDocs.get(SecurityProperties.getPartition().getId()).put((String) key, results);
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
         HashMap<String, byte[]> processes = allData.get(SecurityProperties
               .getPartition().getId());
         HashMap<String, HashMap<String, byte[]>> partitionRepoDoc = repoDoc.get(SecurityProperties
               .getPartition().getId());
         HashMap<String, byte[]> documents = allDocs.get(SecurityProperties
               .getPartition().getId());
         
         HashMap<Long, byte[]> processesForDate = partitionRepo.get((String) key);
         byte[] data = processes.get((String) key);
         BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(data));

         try
         {
            for (int i = 0; i < processInstanceOids.size(); i++)
            {
               byte[] process = new byte[exportResult.getProcessLengths(indexDate).get(i)];
               in.read(process);

               processesForDate.put(processInstanceOids.get(i), process);
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

         HashMap<String, byte[]> docsForDate = partitionRepoDoc.get((String) key);
         byte[] docs = documents.get((String) key);
         if (docs != null)
         {
            in = new BufferedInputStream(new ByteArrayInputStream(docs));
            List<Integer> documentLengths = exportResult.getDocumentLengths(indexDate);
            List<String> documentNames = exportResult.getDocumentNames(indexDate);
            try
            {
               for (int i = 0; i < documentNames.size(); i++)
               {
                  byte[] doc = new byte[documentLengths.get(i)];
                  in.read(doc);
   
                  docsForDate.put(documentNames.get(i), doc);
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

   public void clear()
   {
      repo.clear();
      repoDoc.clear();
      allData.clear();
      allDocs.clear();
      dateModel.clear();
      dateIndex.clear();
      dateArchiveKey.clear();
      modelXpdl.clear();
      keyCounter = 0;
   }
}
