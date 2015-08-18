package org.eclipse.stardust.engine.extensions.camel.trigger;

import org.eclipse.stardust.engine.api.model.IData;

public class AccessPointProperties
{

   private String paramId;
   
   private String accessPointType;

   private String accessPointPath;

   private String accessPointLocation;

   private IData data;

   private String dataPath;

   private String xsdName;

   public String getAccessPointType()
   {
      return accessPointType;
   }

   public void setAccessPointType(String accessPointType)
   {
      this.accessPointType = accessPointType;
   }

   public String getAccessPointLocation()
   {
      return accessPointLocation;
   }

   public void setAccessPointLocation(String accessPointLocation)
   {
      this.accessPointLocation = accessPointLocation;
   }

   public String getAccessPointPath()
   {
      return accessPointPath;
   }

   public void setAccessPointPath(String accessPointPath)
   {
      this.accessPointPath = accessPointPath;
   }

   public IData getData()
   {
      return data;
   }

   public void setData(IData data)
   {
      this.data = data;
   }

   public String getDataPath()
   {
      return dataPath;
   }

   public void setDataPath(String dataPath)
   {
      this.dataPath = dataPath;
   }

   public String getXsdName()
   {
      return xsdName;
   }

   public void setXsdName(String xsdName)
   {
      this.xsdName = xsdName;
   }

   public String getParamId()
   {
      return paramId;
   }

   public void setParamId(String paramId)
   {
      this.paramId = paramId;
   }

   @Override
   public String toString()
   {
      return "AccessPointProperties [accessPointType=" + accessPointType
            + ", accessPointPath=" + accessPointPath + ", accessPointLocation="
            + accessPointLocation + ", data=" + data + ", dataPath=" + dataPath
            + ", xsdName=" + xsdName + "]";
   }
   
   
}
