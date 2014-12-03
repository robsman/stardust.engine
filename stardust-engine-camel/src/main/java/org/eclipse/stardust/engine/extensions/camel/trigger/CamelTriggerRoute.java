package org.eclipse.stardust.engine.extensions.camel.trigger;

import static org.eclipse.stardust.engine.extensions.camel.core.RouteDefinitionBuilder.createRouteDefintionForCamelTrigger;
import static org.eclipse.stardust.engine.extensions.camel.Util.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.camel.CamelContext;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.IAccessPoint;
import org.eclipse.stardust.engine.api.model.ITrigger;
import org.eclipse.stardust.engine.extensions.camel.core.CamelTriggerRouteContext;
import org.eclipse.stardust.engine.extensions.camel.trigger.exceptions.UndefinedEndpointException;

public class CamelTriggerRoute
{
   private ITrigger trigger;
   private StringBuilder routeDefinition;

   public CamelTriggerRoute(CamelContext camelContext, ITrigger trigger, String partition) throws IOException,
         ClassNotFoundException, InstantiationException, IllegalAccessException,
         UndefinedEndpointException
   {
      this.trigger = trigger;
      if (!StringUtils.isEmpty(getProvidedRouteConfiguration(this.trigger)))
      {
         CamelTriggerRouteContext routeContext = new CamelTriggerRouteContext(
               this.trigger, partition, camelContext.getName());
         StringBuilder route = createRouteDefintionForCamelTrigger(routeContext);
         routeDefinition = route;
      }

   }

   public StringBuilder getRouteDefinition()
   {
      return routeDefinition;
   }

   @SuppressWarnings({"rawtypes", "unchecked"})
   public Map getData()
   {

      Map data = new HashMap();
      Iterator _iterator = trigger.getAllAccessPoints();
      while (_iterator.hasNext())
      {
         IAccessPoint ap = (IAccessPoint) _iterator.next();
         if (null != ap)
         {
            data.put(ap.getId(), "");
         }
      }

      return data;
   }
}
