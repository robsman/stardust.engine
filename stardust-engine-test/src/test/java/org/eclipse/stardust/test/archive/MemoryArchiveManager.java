package org.eclipse.stardust.test.archive;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.activemq.util.ByteArrayInputStream;
import org.apache.commons.io.IOUtils;
import org.springframework.util.StringUtils;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.core.persistence.archive.*;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

public class MemoryArchiveManager extends BaseArchiveManager
{
   private static volatile ConcurrentHashMap<String,MemoryArchiveManager> MANAGERS = CollectionUtils.newConcurrentHashMap();
   
   private static HashMap<String, HashMap<String, HashMap<Long, byte[]>>> repo;

   private static HashMap<String, HashMap<String, byte[]>> repoData;

   private static HashMap<String, HashMap<String, String>> dateModel;

   private static HashMap<String, HashMap<String, String>> dateIndex;

   private static HashMap<String, HashMap<String, Date>> dateArchiveKey;
   
   private String archiveManagerId;
   
   private static int keyCounter = 0;

   public static MemoryArchiveManager getInstance(Map<String, String> preferences)
   {
      String id = ArchiveManagerFactory.getPreferenceValue(preferences,
            ArchiveManagerFactory.CARNOT_ARCHIVE_MANAGER_ID, null);

      if (StringUtils.isEmpty(id))
      {
         throw new IllegalArgumentException(
               ArchiveManagerFactory.CARNOT_ARCHIVE_MANAGER_ID
                     + " must be provided for MemoryArchiveManger archive type");
      }
      if (!MANAGERS.containsKey(id))
      {
         synchronized (MemoryArchiveManager.class)
         {
            if (!MANAGERS.containsKey(id))
            {
               MemoryArchiveManager manager = new MemoryArchiveManager(id);
               MANAGERS.put(id, manager);
            }
         }
      }
      return MANAGERS.get(id);
   }
   
   private MemoryArchiveManager(String archiveManagerId)
   {
      if (repo == null)
      {
         synchronized (MemoryArchiveManager.class)
         {
            if (repo == null)
            {
               repo = new HashMap<String, HashMap<String, HashMap<Long, byte[]>>>();
               repoData = new HashMap<String, HashMap<String, byte[]>>();
               dateModel = new HashMap<String, HashMap<String, String>>();
               dateIndex = new HashMap<String, HashMap<String, String>>();
               dateArchiveKey = new HashMap<String, HashMap<String, Date>>();
               archiveManagerId = ArchiveManagerFactory.getCurrentId();
            }
         }
      }
   }
   
   @Override
   public String getArchiveManagerId()
   {
      return archiveManagerId;
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
   public ArrayList<IArchive> findArchives(ArrayList<IArchive> unfilteredArchives,
         Date fromDate, Date toDate, Map<String, Object> descriptors)
   {

      if (unfilteredArchives == null)
      {
         unfilteredArchives = findAllArchives();
      }
      ArrayList<IArchive> archives = new ArrayList<IArchive>();
      for (IArchive archive : unfilteredArchives)
      {
         Date date = dateArchiveKey.get(SecurityProperties.getPartition().getId()).get(archive.getArchiveKey());
         if ((fromDate.compareTo(date) < 1) && (toDate.compareTo(date) > -1))
         {
            archives.add(archive);
         }
         //we did not find a match based on processInstanceOid so search by descriptors
         if (!archives.contains(archive) && descriptors != null)
         {
            if (archive.getExportIndex().contains(descriptors))
            {
               archives.add(archive);
            }
         }
      }
      return archives;
   }

   @Override
   protected ArrayList<IArchive> findAllArchives()
   {
      ArrayList<IArchive> archives = new ArrayList<IArchive>();
      HashMap<String, HashMap<Long, byte[]>> partitionRepo = repo.get(SecurityProperties
            .getPartition().getId());
      if (partitionRepo != null)
      {
         HashMap<String, String> partitionDateModel = dateModel.get(SecurityProperties
               .getPartition().getId());
         HashMap<String, String> partitionDateIndex = dateIndex.get(SecurityProperties
               .getPartition().getId());
         HashMap<String, Date> keyDate = dateArchiveKey.get(SecurityProperties
               .getPartition().getId());
         for (String key : partitionRepo.keySet())
         {
            archives.add(new MemoryArchive(key, keyDate.get(key), partitionRepo.get(key), partitionDateModel
                  .get(key), partitionDateIndex.get(key)));
         }
      }
      return archives;
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
