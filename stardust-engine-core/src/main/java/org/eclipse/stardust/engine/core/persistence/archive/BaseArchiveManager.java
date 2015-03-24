package org.eclipse.stardust.engine.core.persistence.archive;

import java.util.*;

public abstract class BaseArchiveManager implements IArchiveManager
{

   protected abstract ArrayList<IArchive> findAllArchives();

   @Override
   public ArrayList<IArchive> findArchives(List<Long> processInstanceOids, Map<String, String> descriptors)
   {
      ArrayList<IArchive> archives = new ArrayList<IArchive>();
      ArrayList<IArchive> unfilteredArchives = findAllArchives();
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
         //we did not find a match based on processInstanceOid so search by descriptors
         if (!archives.contains(archive) && descriptors != null)
         {
            if (archive.getExportIndex().contains(descriptors))
            {
               archives.add(archive);
            }
         }
         searchItems.removeAll(found);
         found.clear();
         if (searchItems.isEmpty() && (descriptors == null || descriptors.isEmpty()))
         {
            break;
         }
      }
      return archives;
   }

   @Override
   public ArrayList<IArchive> findArchives(Map<String, String> descriptors)
   {
      ArrayList<IArchive> archives = new ArrayList<IArchive>();
      ArrayList<IArchive> unfilteredArchives = findAllArchives();
      for (IArchive archive : unfilteredArchives)
      {
         if (archive.getExportIndex().contains(descriptors))
         {
            if (!archives.contains(archive))
            {
               archives.add(archive);
            }
         }
      }
      return archives;
   }

}