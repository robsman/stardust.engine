package org.eclipse.stardust.engine.extensions.camel;

import org.eclipse.stardust.engine.api.model.IApplication;

public class ConsumerRouteContext extends RouteContext
{
   public ConsumerRouteContext(IApplication application, String partitionId, String camelContextId)
   {
      this.application = application;
      this.partitionId = partitionId;
      this.camelContextId = camelContextId;
   }

   public String getUserProvidedRouteConfiguration()
   {

      return Util.getConsumerRouteConfiguration(application);
   }
   @Override
   public String getRouteId()
   {
      return Util.getRouteId(partitionId, application.getModel().getId(), null, application.getId(), false);
   }

   public String getModelId(){
      return application.getModel().getId();
   }

   public String getConsumerInboundConversion()
   {
      return (String) application.getAttribute(CamelConstants.CONSUMER_INBOUND_CONVERSION);
   }

   public Boolean getConsumerBpmTypeConverter()
   {
      Object value=application.getAttribute(CamelConstants.CONSUMER_BPM_TYPE_CONVERTER);
      if(value == null)
         return false;
      else
         if(value instanceof Boolean)
            return (Boolean) value;
         else
            return Boolean.parseBoolean((String)value);
   }
}
