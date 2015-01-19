package org.eclipse.stardust.engine.extensions.camel.core;

import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;
import org.eclipse.stardust.engine.extensions.camel.Util;

public class ConsumerRouteContext extends ApplicationRouteContext
{
   public ConsumerRouteContext(IApplication application, String partitionId, String camelContextId)
   {
      this.application = application;
      this.partitionId = partitionId;
      this.camelContextId = camelContextId;
   }
   public String getDescription()
   {
      return Util.getDescription(getPartitionId(), getModelId(), getId());
   }
   public String getUserProvidedRouteConfiguration()
   {

      return Util.getConsumerRouteConfiguration(application);
   }
   @Override
   public String getRouteId()
   {
      return Util.getRouteId(partitionId, getModelId(), null, getId(), false);
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
