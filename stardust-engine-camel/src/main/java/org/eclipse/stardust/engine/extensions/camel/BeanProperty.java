package org.eclipse.stardust.engine.extensions.camel;
/**
 * 
 * @deprecated
 *
 */
public class BeanProperty
{
   private String propertyName;
   private String propertyValue;
   private boolean isMap;

   public BeanProperty(String propertyName, String propertyValue, boolean isMap)
   {
      super();
      this.propertyName = propertyName;
      this.propertyValue = propertyValue;
      this.isMap = isMap;
   }

   public BeanProperty(String propertyName, String propertyValue)
   {
      super();
      this.propertyName = propertyName;
      this.propertyValue = propertyValue;
      this.isMap = false;
   }

   public String getPropertyName()
   {
      return propertyName;
   }

   public void setPropertyName(String propertyName)
   {
      this.propertyName = propertyName;
   }

   public String getPropertyValue()
   {
      return propertyValue;
   }

   public void setPropertyValue(String propertyValue)
   {
      this.propertyValue = propertyValue;
   }

   public boolean isMap()
   {
      return isMap;
   }

   public void setMap(boolean isMap)
   {
      this.isMap = isMap;
   }

}