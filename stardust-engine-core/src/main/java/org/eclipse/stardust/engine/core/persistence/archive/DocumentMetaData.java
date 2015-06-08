package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class DocumentMetaData implements Serializable
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private List<String> revisions;
   
   private String dataId;

   private Map vfsResource;

   public DocumentMetaData()
   {}

   public List<String> getRevisions()
   {
      return revisions;
   }

   public void setRevisions(List<String> revisions)
   {
      this.revisions = revisions;
   }

   public Map getVfsResource()
   {
      return vfsResource;
   }

   public void setVfsResource(Map vfsResource)
   {
      this.vfsResource = vfsResource;
   }

   public String getDataId()
   {
      return dataId;
   }

   public void setDataId(String dataId)
   {
      this.dataId = dataId;
   }

}
