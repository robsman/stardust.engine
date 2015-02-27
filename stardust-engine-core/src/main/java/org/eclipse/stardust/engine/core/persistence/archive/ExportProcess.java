package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

public class ExportProcess implements Serializable
{
   private static final long serialVersionUID = 1L;

   @Expose
   private long oid;

   @Expose
   private String uuid;

   public ExportProcess()
   {}

   public ExportProcess(long oid, String uuid)
   {
      super();
      this.oid = oid;
      this.uuid = uuid;
   }

   public long getOid()
   {
      return oid;
   }

   public String getUuid()
   {
      return uuid;
   }

   public void setOid(long oid)
   {
      this.oid = oid;
   }

   public void setUuid(String uuid)
   {
      this.uuid = uuid;
   }

   @Override
   public String toString() {
       String rv = String.valueOf(oid) + ":" + uuid;
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
}
