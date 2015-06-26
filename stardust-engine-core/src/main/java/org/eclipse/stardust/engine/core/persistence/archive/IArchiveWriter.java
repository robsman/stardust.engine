package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public interface IArchiveWriter
{

   public static final String FILENAME_DOCUMENT_META_SUFFIX = "_meta";
   
   public static final String EXT_JSON = ".json";

   public void init(Map<String, String> preferences);
   
   /**
    * Opens archive for writing results. Typically this is to reserve a unique place in the archive repository for results with specified index date.
    * Returns a handle to the place in the repository where the data must be written to
    * @param indexDate
    * @param exportIndex
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
    * @param model
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

   /**
    * Add index json to archive
    * @param key
    * @param indexData
    * @return
    */
   public boolean addIndex(Serializable key, String indexData);

   /**
    * Add Model xpdl to Archive. 
    * This method does not require the open and close operations to be called separately, it encompasses this.
    * This is because a model is only written to the archive the first time a process is ever exported for the model.  
    * @param dumpLocation
    * @param uuid
    * @param xpdl
    * @return
    */
   public boolean addModelXpdl(Serializable dumpLocation, String uuid, String xpdl);

   public boolean addDocuments(Serializable key, byte[] data);
   
   public String getArchiveManagerId();
   
   /**
    * @return Dateformat for archive exportindex
    */
   public String getDateFormat();

   /**
    * @return true if archive on complete is enabled
    */
   public boolean isAutoArchive();
   
   /**
    * Returns true if the model with the uuid has been written to archive
    * @param dumpLocation
    * @param uuid
    * @return
    */
   public boolean isModelExported(Serializable dumpLocation, String uuid);
   
   /**
    * Return DocumentOption configured via preferences
    * @return
    */
   public DocumentOption getDocumentOption();

   /**
    *  Return true if only key descriptors must be exported, as configured via preferences
    * @return
    */
   public boolean isKeyDescriptorsOnly();
}
