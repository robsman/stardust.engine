package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.Serializable;
import java.util.List;

/**
 * Implement this interface for reading an archive itself
 * @author jsaayman
 *
 */
public interface IArchive
{

   /**
    * Returns the key for this archive. Using the key we can identify where the 
    * Model And Index data for this archive is located
    * @return
    */
   public Serializable getArchiveKey();
   
   /**
    * This must return null if the archive only has model data
    * @return data for specific processInstanceOid, null if it is not in archive
    */
   public byte[] getData(List<Long> processes);
  
   /**
    * This must return null if all model and export data is all in one byte array.
    * In such a case it must all be returned by getData()
    * @return
    */
   public ExportModel getExportModel();

   /**
    * Returns ExportIndex for this archive
    * @return
    */
   public ExportIndex getExportIndex();
   
   /**
    * 
    * @return
    */
   public String getArchiveManagerId();

   public String getDumpLocation();
   
   public byte[] getDocumentContent(String documentName);
   
   public ImportDocument getDocumentProperties(String documentName);

}
