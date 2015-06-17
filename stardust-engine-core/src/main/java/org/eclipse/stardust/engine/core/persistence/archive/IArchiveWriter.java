package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.Serializable;
import java.util.Date;

public interface IArchiveWriter
{

   public static final String FILENAME_DOCUMENT_META_SUFFIX = "_meta";
   
   public static final String EXT_JSON = ".json";
   
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

   public boolean addIndex(Serializable key, String indexData);

   public String getArchiveManagerId();

   public boolean addModelXpdl(Serializable dumpLocation, String uuid, String xpdl);

   public String getDateFormat();

   public boolean isAutoArchive();

   public boolean addDocuments(Serializable key, byte[] data);
   
   public boolean isModelExported(Serializable dumpLocation, String uuid);
   
   public DocumentOption getDocumentOption();

   public boolean isKeyDescriptorsOnly();
}
