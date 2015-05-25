package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.Serializable;
import java.util.Map;

import com.google.gson.annotations.Expose;

public class ExportModel implements Serializable
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   @Expose
   private String partition;
   @Expose
   private Map<Integer, String> modelOIdToUuid;
   @Expose
   private Map<String, Long> fqIdToRtOid;
   
   public ExportModel()
   {
   }
   public ExportModel(Map<String, Long> fqIdToRtOid, 
         Map<Integer, String> modelOIdToUuid, String partition)
   {
      super();
      this.fqIdToRtOid = fqIdToRtOid;
      this.modelOIdToUuid = modelOIdToUuid;
      this.partition = partition;
   }
   
   public Map<String, Long> getFqIdToRtOid()
   {
      return fqIdToRtOid;
   }
   
   public Map<Integer, String> getModelOidToUuid()
   {
      return modelOIdToUuid;
   }

   public String getPartition()
   {
      return partition;
   }
   
   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + partition.hashCode();
      if (modelOIdToUuid != null)
      {
         for (Integer key : modelOIdToUuid.keySet())
         {
            result = prime * result + key.hashCode() + modelOIdToUuid.get(key).hashCode();
         }
      }
      if (fqIdToRtOid != null)
      {
         for (String key : fqIdToRtOid.keySet())
         {
            result = prime * result + key.hashCode() + fqIdToRtOid.get(key).hashCode();
         }
      }
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      final ExportModel other = (ExportModel) obj;
      if (partition == null)
      {
         if (other.getPartition() != null)
         {
            return false;
         }
      }
      else if (!partition.equals(other.getPartition()))
      {
         return false;
      }
      if (fqIdToRtOid == null)
      {
         if (other.getFqIdToRtOid() != null)
         {
            return false;
         }
      }
      else
      {
         if (other.getFqIdToRtOid() == null)
         {
            return false;
         }
         if (!fqIdToRtOid.keySet().equals(other.getFqIdToRtOid().keySet()))
         {
            return false;
         }
         for (String key : fqIdToRtOid.keySet())
         {
            Long otherId = other.getFqIdToRtOid().get(key);
            if (!fqIdToRtOid.get(key).equals(otherId))
            {
               return false;
            }
         }
      }

      if (modelOIdToUuid == null)
      {
         if (other.getModelOidToUuid() != null)
         {
            return false;
         }
      }
      else
      {
         if (other.getModelOidToUuid() == null)
         {
            return false;
         }
         if (!modelOIdToUuid.keySet().equals(other.getModelOidToUuid().keySet()))
         {
            return false;
         }
         for (Integer key : modelOIdToUuid.keySet())
         {
            String otherId = other.getModelOidToUuid().get(key);
            if (!modelOIdToUuid.get(key).equals(otherId))
            {
               return false;
            }
         }
      }
      return true;
   }
}
