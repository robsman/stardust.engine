package org.eclipse.stardust.test.archive;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import org.apache.activemq.util.ByteArrayInputStream;
import org.apache.commons.io.IOUtils;

import org.eclipse.stardust.engine.core.persistence.archive.*;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

public class MemoryArchiveManager implements IArchiveManager
{

   private static HashMap<String, HashMap<Date, HashMap<Long, byte[]>>> repo;

   private static HashMap<String, HashMap<Date, byte[]>> repoData;

   private static HashMap<String, HashMap<Date, byte[]>> dateModel;

   private static HashMap<String, HashMap<Date, String>> dateIndex;
   
   private String archiveManagerId;

   public MemoryArchiveManager()
   {
      if (repo == null)
      {
         synchronized (MemoryArchiveManager.class)
         {
            if (repo == null)
            {
               repo = new HashMap<String, HashMap<Date, HashMap<Long, byte[]>>>();
               repoData = new HashMap<String, HashMap<Date, byte[]>>();
               dateModel = new HashMap<String, HashMap<Date, byte[]>>();
               dateIndex = new HashMap<String, HashMap<Date, String>>();
               archiveManagerId = ArchiveManagerFactory.getCurrentId();
            }
         }
      }
   }

   @Override
   public Serializable open(Date indexDate)
   {
      synchronized (indexDate)
      {
         HashMap<Date, HashMap<Long, byte[]>> partitionRepo = repo.get(SecurityProperties
               .getPartition().getId());
         if (partitionRepo == null)
         {
            partitionRepo = new HashMap<Date, HashMap<Long, byte[]>>();
            repo.put(SecurityProperties.getPartition().getId(), partitionRepo);
            repoData.put(SecurityProperties.getPartition().getId(),
                  new HashMap<Date, byte[]>());
            dateModel.put(SecurityProperties.getPartition().getId(),
                  new HashMap<Date, byte[]>());
            dateIndex.put(SecurityProperties.getPartition().getId(),
                  new HashMap<Date, String>());
         }
         if (partitionRepo.get(indexDate) == null)
         {
            partitionRepo.put(indexDate, new HashMap<Long, byte[]>());
         }
      }
      return indexDate;
   }
   
   @Override
   public String getArchiveManagerId()
   {
      return archiveManagerId;
   }

   @Override
   public boolean add(Serializable key, byte[] results)
   {
      synchronized (key)
      {
         repoData.get(SecurityProperties.getPartition().getId()).put((Date) key, results);
      }
      return true;
   }

   @Override
   public boolean addModel(Serializable key, byte[] results)
   {
      synchronized (key)
      {
         dateModel.get(SecurityProperties.getPartition().getId())
               .put((Date) key, results);
      }
      return true;

   }

   @Override
   public boolean close(Serializable key, Date indexDate, ExportResult exportResult)
   {
      synchronized (key)
      {
         List<Long> processInstanceOids = exportResult.getProcessInstanceOids(indexDate);
         HashMap<Date, HashMap<Long, byte[]>> partitionRepo = repo.get(SecurityProperties
               .getPartition().getId());
         HashMap<Date, byte[]> partitionRepoData = repoData.get(SecurityProperties
               .getPartition().getId());

         HashMap<Long, byte[]> hashMap = partitionRepo.get((Date) key);
         byte[] data = partitionRepoData.get((Date) key);
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
   public ArrayList<IArchive> findArchives(List<Long> processInstanceOids)
   {
      ArrayList<IArchive> archives = new ArrayList<IArchive>();
      ArrayList<IArchive> unfilteredArchives = findArchives();
      Set<Long> searchItems = new HashSet<Long>();
      searchItems.addAll(processInstanceOids);
      Set<Long> found = new HashSet<Long>();
      for (IArchive archive : unfilteredArchives)
      {
         for (Long processInstanceOid : searchItems)
         {
            if (archive.getExportIndex().contains(processInstanceOid))
            {
               if (!archives.contains(archive))
               {
                  archives.add(archive);
               }
               found.add(processInstanceOid);
            }
         }
         searchItems.removeAll(found);
         found.clear();
         if (searchItems.isEmpty())
         {
            break;
         }
      }
      return archives;
   }

   @Override
   public ArrayList<IArchive> findArchives(Date fromDate, Date toDate)
   {
      ArrayList<IArchive> archives = new ArrayList<IArchive>();
      HashMap<Date, HashMap<Long, byte[]>> partitionRepo = repo.get(SecurityProperties
            .getPartition().getId());
      if (partitionRepo != null)
      {
         HashMap<Date, byte[]> partitionDateModel = dateModel.get(SecurityProperties
               .getPartition().getId());
         HashMap<Date, String> partitionDateIndex = dateIndex.get(SecurityProperties
               .getPartition().getId());
         for (Date date : partitionRepo.keySet())
         {
            if ((fromDate.compareTo(date) < 1) && (toDate.compareTo(date) > -1))
            {
               archives.add(new MemoryArchive(date, partitionRepo.get(date), partitionDateModel
                     .get(date), partitionDateIndex.get(date)));
            }
         }
      }
      return archives;
   }

   @Override
   public ArrayList<IArchive> findArchives()
   {
      ArrayList<IArchive> archives = new ArrayList<IArchive>();
      HashMap<Date, HashMap<Long, byte[]>> partitionRepo = repo.get(SecurityProperties
            .getPartition().getId());
      if (partitionRepo != null)
      {
         HashMap<Date, byte[]> partitionDateModel = dateModel.get(SecurityProperties
               .getPartition().getId());
         HashMap<Date, String> partitionDateIndex = dateIndex.get(SecurityProperties
               .getPartition().getId());
         for (Date date : partitionRepo.keySet())
         {
            archives.add(new MemoryArchive(date, partitionRepo.get(date), partitionDateModel
                  .get(date), partitionDateIndex.get(date)));
         }
      }
      return archives;
   }

   @Override
   public boolean addIndex(Serializable key, String indexData)
   {
      synchronized (key)
      {
         dateIndex.get(SecurityProperties.getPartition().getId()).put((Date) key,
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
   }

}
