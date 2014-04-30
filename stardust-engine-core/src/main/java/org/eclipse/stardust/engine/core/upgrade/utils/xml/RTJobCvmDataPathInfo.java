package org.eclipse.stardust.engine.core.upgrade.utils.xml;

import javax.xml.bind.annotation.XmlAttribute;

public class RTJobCvmDataPathInfo
{
   @XmlAttribute
   private String data;

   @XmlAttribute
   private String dataPath;

   @XmlAttribute
   private boolean descriptor;

   @XmlAttribute
   private String direction;

   @XmlAttribute
   private String id;

   @XmlAttribute
   private String name;

   public String getData()
   {
      return data;
   }

   public String getDataPath()
   {
      return dataPath;
   }

   public boolean isDescriptor()
   {
      return descriptor;
   }

   public String getDirection()
   {
      return direction;
   }

   public String getId()
   {
      return id;
   }

   public String getName()
   {
      return name;
   }
}
