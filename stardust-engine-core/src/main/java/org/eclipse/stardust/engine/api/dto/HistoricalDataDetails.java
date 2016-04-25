package org.eclipse.stardust.engine.api.dto;

import java.io.Serializable;

import org.eclipse.stardust.engine.core.runtime.beans.DataValueHistoryBean;

/**
 * 
 * @author Thomas.Wolfram
 *
 */
public class HistoricalDataDetails implements HistoricalData
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private Serializable dataValue;

   private DataDetails data;

   private long modificationTimestamp;

   private long modificatingUser;

   private long modificatingActivityInstance;

   public HistoricalDataDetails(DataValueHistoryBean valueHistoryBean)
   {
      this.dataValue = valueHistoryBean.getSerializedValue();

      this.data = new DataDetails(valueHistoryBean.getData());

      this.modificatingActivityInstance = valueHistoryBean.getModificatingActivityInstance();
      this.modificatingUser = valueHistoryBean.getModificatingUser();
      this.modificationTimestamp = valueHistoryBean.getModificationTimestamp();

   }

   @Override
   public Serializable getHistoricalDataValue()
   {
      return dataValue;
   }

   @Override
   public long getDataModificationTimestamp()
   {
      return modificationTimestamp;
   }

   @Override
   public long getModifyingUserOID()
   {
      return modificatingUser;
   }

   @Override
   public long getModifyingActivityInstanceOID()
   {
      return modificatingActivityInstance;
   }

   @Override
   public DataDetails getData()
   {
      return data;
   }

}
