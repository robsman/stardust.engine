package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.Serializable;
import java.util.Map;

import com.google.gson.Gson;

public class ExportProcess implements Serializable
{
   private static final long serialVersionUID = 1L;

   private long oid;
   
   private String startDate;
   
   private String endDate;

   private String uuid;
   
   Map<String, String> descriptors;

   public ExportProcess()
   {}

   public ExportProcess(long oid, String startDate, String endDate, String uuid, Map<String, String> descriptors)
   {
      super();
      this.oid = oid;
      this.uuid = uuid;
      this.descriptors = descriptors;
      this.startDate = startDate;
      this.endDate = endDate;
   }
   
   public long getOid()
   {
      return oid;
   }

   public String getUuid()
   {
      return uuid;
   }

   @Override
   public String toString() {
      Gson gson = ExportImportSupport.getGson();
      String descr = gson.toJson(descriptors);
      
      String rv = String.valueOf(oid) + ExportProcessSerializer.DELIMETER + 
            startDate + ExportProcessSerializer.DELIMETER + 
            endDate + ExportProcessSerializer.DELIMETER + 
            uuid + ExportProcessSerializer.DELIMETER + 
            descr.toString();
      return rv;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result
         + Long.valueOf(oid).hashCode();
      result = prime * result
         + ((uuid == null) ? 0 : uuid.hashCode());
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
      final ExportProcess other = (ExportProcess) obj;
      if (oid != other.getOid()) {
         return false;
      }
      if (uuid == null) {
         if (other.getUuid() != null) 
            return false;
      } else if (!uuid.equals(other.getUuid())) {
         return false;
      }
      return true;
   }
   
   public String getStartDate()
   {
      return startDate;
   }

   public String getEndDate()
   {
      return endDate;
   }

   public Map<String, String> getDescriptors()
   {
      return descriptors;
   }
   
}
