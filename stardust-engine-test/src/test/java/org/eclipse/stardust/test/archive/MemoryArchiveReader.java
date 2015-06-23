package org.eclipse.stardust.test.archive;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.StringUtils;

import org.eclipse.stardust.engine.core.persistence.archive.ArchiveManagerFactory;
import org.eclipse.stardust.engine.core.persistence.archive.BaseArchiveReader;
import org.eclipse.stardust.engine.core.persistence.archive.IArchive;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

public class MemoryArchiveReader extends BaseArchiveReader
{

   private MemoryArchiveWriter writer;
   
   public MemoryArchiveReader()
   {}
   
   public void init(Map<String, String> preferences)
   {
      String id = preferences.get(ArchiveManagerFactory.CARNOT_ARCHIVE_READER_MANAGER_ID);

      if (StringUtils.isEmpty(id))
      {
         throw new IllegalArgumentException(
               ArchiveManagerFactory.CARNOT_ARCHIVE_READER_MANAGER_ID
                     + " must be provided for MemoryArchiveReader archive type");
      }
      this.writer = (MemoryArchiveWriter)ArchiveManagerFactory.getArchiveWriter(id);
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
         Date date = writer.dateArchiveKey.get(SecurityProperties.getPartition().getId()).get(archive.getArchiveKey());
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
      HashMap<String, HashMap<Long, byte[]>> partitionRepo = writer.repo.get(SecurityProperties
            .getPartition().getId());
      if (partitionRepo != null)
      {
         HashMap<String, HashMap<String, byte[]>> partitionDocRepo = writer.repoDoc.get(SecurityProperties
               .getPartition().getId());
         
         HashMap<String, String> partitionDateModel = writer.dateModel.get(SecurityProperties
               .getPartition().getId());
         HashMap<String, String> partitionDateIndex = writer.dateIndex.get(SecurityProperties
               .getPartition().getId());
         HashMap<String, Date> keyDate = writer.dateArchiveKey.get(SecurityProperties
               .getPartition().getId());
         for (String key : partitionRepo.keySet())
         {
            archives.add(new MemoryArchive(key, keyDate.get(key), partitionRepo.get(key), partitionDateModel
                  .get(key), partitionDateIndex.get(key), partitionDocRepo.get(key)));
         }
      }
      return archives;
   }
   
}
