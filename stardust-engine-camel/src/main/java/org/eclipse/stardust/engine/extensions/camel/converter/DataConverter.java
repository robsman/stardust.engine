package org.eclipse.stardust.engine.extensions.camel.converter;

public interface DataConverter
{
   String getFromEndpoint();

   /**
    * @param endpoint
    */
   void setFromEndpoint(String endpoint);

   /**
    * @return the target type
    */
   String getTargetType();

   /**
    * @param clazz
    */
   void setTargetType(String clazz);
}
