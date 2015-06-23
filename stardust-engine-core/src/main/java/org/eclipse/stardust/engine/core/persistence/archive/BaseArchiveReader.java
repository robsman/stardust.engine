package org.eclipse.stardust.engine.core.persistence.archive;

import java.util.*;

public abstract class BaseArchiveReader implements IArchiveReader
{

   protected abstract ArrayList<IArchive> findAllArchives();

   @Override
   public ArrayList<IArchive> findArchives(ArchiveFilter filter)
   {
      ArrayList<IArchive> archives = findAllArchives();
      if (filter.getFromDate() != null && filter.getToDate() != null)
      {
         archives = findArchives(archives, filter.getFromDate(), filter.getToDate(), filter.getDescriptors());
      }
      if (filter.getProcessInstanceOids() != null)
      {
         archives = findArchives(archives, filter.getProcessInstanceOids(), filter.getDescriptors());
      }
      if (filter.getDescriptors() != null)
      {
         archives = findArchives(archives, filter.getDescriptors());
      }
      if (filter.getModelIds() != null)
      {
         archives = findArchives(archives, ExportIndex.FIELD_MODEL_ID, filter.getModelIds(), true);
      }
      if (filter.getProcessDefinitionIds() != null)
      {
         archives = findArchives(archives, ExportIndex.FIELD_PROCESS_DEFINITION_ID, filter.getProcessDefinitionIds(), true);
      }
      return archives;
   }

   @Override
   public ArrayList<IArchive> findArchives(ArrayList<IArchive> unfilteredArchives, String key, Collection<? extends Object> values, boolean onlyMatchOnRoots)
   {
      if (unfilteredArchives == null)
      {
         unfilteredArchives = findAllArchives();
      }
      ArrayList<IArchive> archives = new ArrayList<IArchive>();
      for (IArchive archive : unfilteredArchives)
      {
         if (archive.getExportIndex().contains(key, values, onlyMatchOnRoots))
         {
            if (!archives.contains(archive))
            {
               archives.add(archive);
            }
         }
      }
      return archives;
   }
   
   @Override
   public ArrayList<IArchive> findArchives(ArrayList<IArchive> unfilteredArchives, 
         Collection<Long> processInstanceOids, Map<String, Object> descriptors)
   {
      if (unfilteredArchives == null)
      {
         unfilteredArchives = findAllArchives();
      }
      ArrayList<IArchive> archives = new ArrayList<IArchive>();
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
   public ArrayList<IArchive> findArchives(ArrayList<IArchive> unfilteredArchives,
         Map<String, Object> descriptors)
   {
      if (unfilteredArchives == null)
      {
         unfilteredArchives = findAllArchives();
      }
      ArrayList<IArchive> archives = new ArrayList<IArchive>();
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