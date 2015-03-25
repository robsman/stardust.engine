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
}
