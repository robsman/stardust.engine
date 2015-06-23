package org.eclipse.stardust.engine.core.persistence.archive;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public interface IArchiveReader
{
   public void init(Map<String, String> preferences);
   
   public ArrayList<IArchive> findArchives(ArchiveFilter filter);
   
   public ArrayList<IArchive> findArchives(ArrayList<IArchive> unfilteredArchives, Collection<Long> processInstanceOids, Map<String, Object> descriptors);

   public ArrayList<IArchive> findArchives(ArrayList<IArchive> unfilteredArchives, Date fromDate, Date toDate, Map<String, Object> descriptors);

   public ArrayList<IArchive> findArchives(ArrayList<IArchive> unfilteredArchives, Map<String, Object> descriptors);
   
   public ArrayList<IArchive> findArchives(ArrayList<IArchive> unfilteredArchives, String key, Collection<? extends Object> values, boolean onlyMatchOnRoots);

}
