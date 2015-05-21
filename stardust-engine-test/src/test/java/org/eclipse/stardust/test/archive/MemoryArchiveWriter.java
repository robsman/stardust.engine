package org.eclipse.stardust.test.archive;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import org.apache.activemq.util.ByteArrayInputStream;
import org.apache.commons.io.IOUtils;
import org.springframework.util.StringUtils;

import org.eclipse.stardust.engine.core.persistence.archive.*;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

public class MemoryArchiveWriter implements IArchiveWriter
{
   protected HashMap<String, HashMap<String, HashMap<Long, byte[]>>> repo;

   protected HashMap<String, HashMap<String, byte[]>> repoData;

   protected HashMap<String, HashMap<String, String>> dateModel;
   
   protected HashMap<String, HashMap<String, HashMap<String, String>>> dateXpdl;

   protected HashMap<String, HashMap<String, String>> dateIndex;

   protected HashMap<String, HashMap<String, Date>> dateArchiveKey;
   
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
      dateXpdl = new HashMap<String, HashMap<String, HashMap<String, String>>>();
      dateIndex = new HashMap<String, HashMap<String, String>>();
      dateArchiveKey = new HashMap<String, HashMap<String, Date>>();
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
            dateXpdl.put(SecurityProperties.getPartition().getId(),
                  new HashMap<String, HashMap<String, String>>());
            dateIndex.put(SecurityProperties.getPartition().getId(),
                  new HashMap<String, String>());
            dateArchiveKey.put(SecurityProperties.getPartition().getId(),
                  new HashMap<String, Date>());
         }
         HashMap<String, Date> keyDateMap = dateArchiveKey.get(SecurityProperties.getPartition().getId());
         String key = "key" + keyCounter++;
         keyDateMap.put(key, indexDate);
         partitionRepo.put(key, new HashMap<Long, byte[]>());
         return key;
      }
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
   public boolean addXpdl(Serializable key, String uuid, String xpdl)
   {
      synchronized (key)
      {
         HashMap<String, HashMap<String, String>> keyXpdls = dateXpdl.get(SecurityProperties.getPartition().getId());
         HashMap<String, String> xpdls = keyXpdls.get(key);
         if (xpdls == null)
         {
            xpdls = new HashMap<String, String>();
            keyXpdls.put((String)key, xpdls);
         }
         xpdls.put(uuid, xpdl);
      }
      return true;
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

   public void clear()
   {
      repo.clear();
      repoData.clear();
      dateModel.clear();
      dateIndex.clear();
      dateArchiveKey.clear();
      keyCounter = 0;
   }

}
