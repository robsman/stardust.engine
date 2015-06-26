package org.eclipse.stardust.engine.core.persistence.archive;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * Implement this interface to allow searching for archives in a specific type of archiving system
 * @author jsaayman
 *
 */
public interface IArchiveReader
{
   public void init(Map<String, String> preferences);
   
   /**
    * Finds archive that matches filter criteria. AND logic must be used between criteria
    * @param filter
    * @return
    */
   public ArrayList<IArchive> findArchives(ArchiveFilter filter);
   
   public ArrayList<IArchive> findArchives(ArrayList<IArchive> unfilteredArchives, Collection<Long> processInstanceOids, Map<String, Object> descriptors);

   public ArrayList<IArchive> findArchives(ArrayList<IArchive> unfilteredArchives, Date fromDate, Date toDate, Map<String, Object> descriptors);

   public ArrayList<IArchive> findArchives(ArrayList<IArchive> unfilteredArchives, Map<String, Object> descriptors);
   
   public ArrayList<IArchive> findArchives(ArrayList<IArchive> unfilteredArchives, String key, Collection<? extends Object> values, boolean onlyMatchOnRoots);

}
