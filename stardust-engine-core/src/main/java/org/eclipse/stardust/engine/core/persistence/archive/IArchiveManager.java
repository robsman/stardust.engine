package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public interface IArchiveManager
{
   /**
    * Opens archive for writing results. Typically this is to reserve a unique place in the archive repository for results with specified index date.
    * Returns a handle to the place in the repository where the data must be written to
    * @param partition
    * @param indexDate
    * @return Returns a handle to the place in the repository where the data must be written to
    */
   public Serializable open(Date indexDate, ExportIndex exportIndex);

   /**
    * Adds results to the repository location identified by key
    * @param key
    * @param results
    * @return success indicator
    */
   public boolean add(Serializable key, byte[] results);
   
   /**
    * Adds model to the repository location identified by key
    * @param key
    * @param results
    * @return success indicator
    */
   public boolean addModel(Serializable key, String model);
   
   /**
    * Closes repository location identified by key, and performs any final operations needed
    * on it
    * @param key
    * @param indexDate
    * @param exportResult
    * @return success indicator
    */
   public boolean close(Serializable key, Date indexDate, ExportResult exportResult);

   public ArrayList<IArchive> findArchives(ArrayList<IArchive> unfilteredArchives, Collection<Long> processInstanceOids, Map<String, Object> descriptors);

   public ArrayList<IArchive> findArchives(ArrayList<IArchive> unfilteredArchives, Date fromDate, Date toDate, Map<String, Object> descriptors);

   public ArrayList<IArchive> findArchives(ArrayList<IArchive> unfilteredArchives, Map<String, Object> descriptors);
   
   public ArrayList<IArchive> findArchives(ArrayList<IArchive> unfilteredArchives, String key, Collection<? extends Object> values, boolean onlyMatchOnRoots);

   public boolean addIndex(Serializable key, String indexData);

   public String getArchiveManagerId();

   public ArrayList<IArchive> findArchives(ArchiveFilter filter);

   public boolean addXpdl(Serializable key, String uuid, String xpdl);
}
