package org.eclipse.stardust.engine.api.dto;

import java.io.Serializable;

/**
 * 
 * @author Thomas.Wolfram
 *
 */
public interface HistoricalData extends Serializable
{
   /**
    * Gets the value of the historical data entry
    * 
    * @return
    */
   Serializable getHistoricalDataValue();
   
   /**
    * Gets the OID of the process data
    * 
    * @return
    */
   long getDataType();
   
   /**
    * Gets the timestamp of when the data was modified
    * 
    * @return
    */
   long getDataModificationTimestamp();
   
   /**
    * Gets the OID of the user who modified the data
    * 
    * @return
    */
   long getModifyingUserOID();
   
   /**
    * Gets the OID of the ActivityInstance in which the data was modified.
    * Returns 0 if modification did not occur in the context of an activity (i.e. data path)
    * 
    * @return
    */
   long getModifyingActivityInstanceOID();
   
}
