package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public interface IArchiveManager
{
   /**
    * Opens archive for writing results. Typically this is to reserve a unique place in the archive repository for results with specified index date.
    * Returns a handle to the place in the repository where the data must be written to
    * @param partition
    * @param indexDate
    * @return Returns a handle to the place in the repository where the data must be written to
    */
   public Serializable open(Date indexDate);

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
   public boolean addModel(Serializable key, byte[] results);
   
   /**
    * Closes repository location identified by key, and performs any final operations needed
    * on it
    * @param key
    * @param results
    * @return success indicator
    */
   public boolean close(Serializable key, List<Long> processIds, List<Integer> processLengths);

   public ArrayList<IArchive> findArchives(List<Long> processInstanceOids);

   public ArrayList<IArchive> findArchives(Date fromDate, Date toDate);

   public ArrayList<IArchive> findArchives();

   public boolean addIndex(Serializable key, String indexData);

}
