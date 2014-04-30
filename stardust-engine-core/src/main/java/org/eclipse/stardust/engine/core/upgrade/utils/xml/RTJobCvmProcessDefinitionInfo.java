package org.eclipse.stardust.engine.core.upgrade.utils.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class RTJobCvmProcessDefinitionInfo
{
   @XmlElement(name = "dataPath", namespace = "http://www.carnot.ag/workflowmodel/3.1")
   private List<RTJobCvmDataPathInfo> dataPathInfos = new ArrayList<RTJobCvmDataPathInfo>();

   @XmlAttribute
   private String id;

   public List<RTJobCvmDataPathInfo> getDataPathInfos()
   {
      return dataPathInfos;
   }

   public String getId()
   {
      return id;
   }
}
