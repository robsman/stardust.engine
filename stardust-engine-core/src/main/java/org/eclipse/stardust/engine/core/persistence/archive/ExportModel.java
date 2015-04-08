package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.Serializable;
import java.util.Map;

public class ExportModel implements Serializable
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   private String partition;
   private Map<String, Long> modelIdToOid;
   private Map<String, Long> fqIdToRtOid;
   
   public ExportModel()
   {
   }
   public ExportModel(Map<String, Long> fqIdToRtOid, Map<String, Long> modelIdToOid,
         String partition)
   {
      super();
      this.fqIdToRtOid = fqIdToRtOid;
      this.modelIdToOid = modelIdToOid;
      this.partition = partition;
   }
   public Map<String, Long> getFqIdToRtOid()
   {
      return fqIdToRtOid;
   }
   public Map<String, Long> getModelIdToOid()
   {
      return modelIdToOid;
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
      if (modelIdToOid != null)
      {
         for (String key : modelIdToOid.keySet())
         {
            result = prime * result + key.hashCode() + modelIdToOid.get(key).hashCode();
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
      if (modelIdToOid == null)
      {
         if (other.getModelIdToOid() != null)
         {
            return false;
         }
      }
      else
      {
         if (other.getModelIdToOid() == null)
         {
            return false;
         }
         if (!modelIdToOid.keySet().equals(other.getModelIdToOid().keySet()))
         {
            return false;
         }
         for (String key : modelIdToOid.keySet())
         {
            Long otherId = other.getModelIdToOid().get(key);
            if (!modelIdToOid.get(key).equals(otherId))
            {
               return false;
            }
         }
      }
      return true;
   }
}
