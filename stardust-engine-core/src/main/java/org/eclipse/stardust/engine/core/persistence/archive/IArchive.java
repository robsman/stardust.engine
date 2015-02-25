package org.eclipse.stardust.engine.core.persistence.archive;

import java.util.List;


public interface IArchive
{

   public String getName();
   
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
   public byte[] getModelData();

   /**
    * Returns ExportIndex for this archive
    * @return
    */
   public ExportIndex getExportIndex();

}
