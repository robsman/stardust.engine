package org.eclipse.stardust.engine.extensions.camel.component;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultEndpointConfiguration;

public class DataEndpointConfiguration extends DefaultEndpointConfiguration
{
   private Map<String,Object> params;

   public DataEndpointConfiguration(CamelContext camelContext)
   {
      super(camelContext);
   }

   public DataEndpointConfiguration(CamelContext camelContext, String uri)
   {
      super(camelContext, uri);
   }

   private String subCommand;

   public String getSubCommand()
   {
      return subCommand;
   }

   public void setSubCommand(String subCommand)
   {
      this.subCommand = subCommand;
   }

   public Map<String, Object> getParams()
   {
      return params;
   }
   public void setParams(Map<String, Object> params)
   {
      this.params = params;
   }

   @Override
   public String toUriString(UriFormat format)
   {
      return super.toString();
   }
}
