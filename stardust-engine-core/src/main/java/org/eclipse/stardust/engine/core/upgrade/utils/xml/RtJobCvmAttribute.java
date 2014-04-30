package org.eclipse.stardust.engine.core.upgrade.utils.xml;

import javax.xml.bind.annotation.XmlAttribute;

public class RtJobCvmAttribute
{
   @XmlAttribute
   private String name;

   @XmlAttribute
   private String type;

   @XmlAttribute
   private String value;

   public String getName()
   {
      return name;
   }

   public String getType()
   {
      return type;
   }

   public String getValue()
   {
      return value;
   }
}
