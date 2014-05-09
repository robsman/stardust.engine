package org.eclipse.stardust.engine.extensions.camel.core;

import static org.eclipse.stardust.engine.extensions.camel.Util.isProducerApplication;

import org.apache.commons.lang.StringUtils;

import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;
import org.eclipse.stardust.engine.extensions.camel.Util;

public class ProducerRouteContext extends ApplicationRouteContext
{
   public ProducerRouteContext(IApplication application, String partitionId,
         String camelContextId)
   {
      this.application = application;
      this.partitionId = partitionId;
      this.camelContextId = camelContextId;
   }

   public String getUserProvidedRouteConfiguration()
   {

      return Util.getProducerRouteConfiguration(application);
   }

   public String getRouteId()
   {
      return Util.getRouteId(partitionId, getModelId(), null,
            getId(), true);
   }
   
   public boolean addApplicationAttributesToHeaders()
   {
      String providedRoute = getUserProvidedRouteConfiguration();
      if (isProducerApplication(application) && !StringUtils.isEmpty(providedRoute))
      {
         if (providedRoute.contains("://service/"))
            return true;
      }

      Object value = application
            .getAttribute("carnot:engine:camel::includeAttributesAsHeaders");

      if (value != null && value instanceof Boolean)
         return (Boolean) value;

      if (value != null)
         return Boolean.parseBoolean((String) value);
      return false;
   }

   public boolean addProcessContextHeaders()
   {
      Object value = application
            .getAttribute("carnot:engine:camel::processContextHeaders");
      if (value != null && value instanceof Boolean)
         return (Boolean) value;

      if (value != null)
         return Boolean.parseBoolean((String) value);
      return false;
   }

   public boolean getProducerBpmTypeConverter()
   {
      Object value = application.getAttribute(CamelConstants.PRODUCER_BPM_TYPE_CONVERTER);
      if (value == null)
         return false;
      if (value instanceof Boolean)
         return (Boolean) value;
      return Boolean.getBoolean((String) value);
   }

   public String getProducerOutboundConversion()
   {
      return (String) application
            .getAttribute(CamelConstants.PRODUCER_OUTBOUND_CONVERSION);
   }

   public String getProducerInboundConversion()
   {
      return (String) application
            .getAttribute(CamelConstants.PRODUCER_INBOUND_CONVERSION);
   }
}
