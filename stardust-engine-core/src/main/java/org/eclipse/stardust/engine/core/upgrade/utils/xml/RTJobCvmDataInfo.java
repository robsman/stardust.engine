package org.eclipse.stardust.engine.core.upgrade.utils.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.pojo.data.Type;

public class RTJobCvmDataInfo
{
   @XmlAttribute
   private String id;

   @XmlAttribute
   private String name;

   @XmlAttribute
   private boolean predefined;

   @XmlElement(name = "attribute", namespace = "http://www.carnot.ag/workflowmodel/3.1")
   private List<RtJobCvmAttribute> attributes = new ArrayList<RtJobCvmAttribute>();

   public String getId()
   {
      return id;
   }

   public String getName()
   {
      return name;
   }

   public boolean isPredefined()
   {
      return predefined;
   }

   public List<RtJobCvmAttribute> getAttributes()
   {
      return attributes;
   }

   public boolean isStringType()
   {
      for(RtJobCvmAttribute attribute: attributes)
      {
         if(PredefinedConstants.TYPE_ATT.equals(attribute.getName())
               && Type.String.toString().equals(attribute.getValue()))
         {
            return true;
         }
      }

      return false;
   }

   public String getType()
   {
      for(RtJobCvmAttribute attribute: attributes)
      {
         if(PredefinedConstants.TYPE_ATT.equals(attribute.getName()))
         {
            return attribute.getValue();
         }
      }

      return null;
   }
}
