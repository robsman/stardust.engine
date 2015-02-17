package org.eclipse.stardust.engine.core.persistence.archive;

public interface IArchive
{

   public String getName();
   
   /**
    * This must return null if the archive only has model data
    * @return
    */
   public byte[] getData();

   /**
    * This must return null if all model and export data is all in one byte array.
    * In such a case it must all be returned by getData()
    * @return
    */
   public byte[] getModelData();

}
