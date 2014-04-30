package org.eclipse.stardust.engine.core.upgrade.utils.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.stardust.engine.core.model.parser.info.ModelInfo;

@XmlRootElement(name = "model", namespace = "http://www.carnot.ag/workflowmodel/3.1")
public class RTJobCvmModelInfo extends ModelInfo
{
   @XmlElement(name = "processDefinition", namespace = "http://www.carnot.ag/workflowmodel/3.1")
   private List<RTJobCvmProcessDefinitionInfo> processDefintions = new ArrayList<RTJobCvmProcessDefinitionInfo>();

   @XmlElement(name = "data", namespace = "http://www.carnot.ag/workflowmodel/3.1")
   private List<RTJobCvmDataInfo> data = new ArrayList<RTJobCvmDataInfo>();

   @XmlAttribute(name = "id")
   public void setId(String id)
   {
      this.id = id;
   }

   public List<RTJobCvmProcessDefinitionInfo> getProcessDefintions()
   {
      return processDefintions;
   }

   public List<RTJobCvmDataPathInfo> findDataPathById(String id)
   {
      List<RTJobCvmDataPathInfo> cvmDataPathInfos = new ArrayList<RTJobCvmDataPathInfo>();
      for(RTJobCvmProcessDefinitionInfo cvmPd: processDefintions)
      {
         for(RTJobCvmDataPathInfo cvmDataPathInfo : cvmPd.getDataPathInfos())
         {
            if(cvmDataPathInfo.getId().equals(id))
            {
               cvmDataPathInfos.add(cvmDataPathInfo);
            }
         }
      }

      return cvmDataPathInfos;
   }

   public RTJobCvmDataInfo findDataById(String id)
   {
      for(RTJobCvmDataInfo di: data)
      {
         if(di.getId().equals(id))
         {
            return di;
         }
      }

      return null;
   }
}
