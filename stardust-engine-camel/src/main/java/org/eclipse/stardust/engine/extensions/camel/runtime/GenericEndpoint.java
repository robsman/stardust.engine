package org.eclipse.stardust.engine.extensions.camel.runtime;

public class GenericEndpoint extends Endpoint
{

   private String component;

   public String getComponent()
   {
      return component;
   }

   public void setComponent(String component)
   {
      this.component = component;
   }

   public void parse()
   {}

   public String getMessageId()
   {
      return "GenericMessage";
   }

   public String getMessageName()
   {
      return "GenericMessage";
   }

}
