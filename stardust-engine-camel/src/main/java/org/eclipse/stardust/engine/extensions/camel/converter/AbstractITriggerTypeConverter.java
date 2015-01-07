package org.eclipse.stardust.engine.extensions.camel.converter;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.ACCESS_POINT_HEADERS;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.ACCESS_POINT_MESSAGE;

import org.apache.camel.Exchange;

import org.eclipse.stardust.engine.extensions.camel.trigger.AccessPointProperties;

public abstract class AbstractITriggerTypeConverter extends AbstractBpmTypeConverter implements ITriggerTypeConverter
{

   protected AbstractITriggerTypeConverter(Exchange exchange)
   {
      super(exchange);
   }

   public void replaceDataValue(AccessPointProperties accessPoint, Object value)
   {
      if (accessPoint.getAccessPointLocation().equals(ACCESS_POINT_HEADERS))
      {
         String headerName = null;
         if (accessPoint.getAccessPointPath() != null)
         {

            int startIndex = accessPoint.getAccessPointPath().indexOf("get");
            int endIndex = accessPoint.getAccessPointPath().lastIndexOf("()");
            headerName = accessPoint.getAccessPointPath().substring(startIndex + 3,
                  endIndex);
         }

         exchange.getOut().setHeader(headerName, value);
      }
      else if (accessPoint.getAccessPointLocation().equals(ACCESS_POINT_MESSAGE))
      {
         exchange.getOut().setBody(value);
      }

   }
   /**
    * According to the trigger configuration, the method will return the value of the
    * provided accessPoint configuration if the location is message, the value is
    * retrieved from the body of the exchange, else it will be retrieved from the header
    * 
    * @param accessPoint
    * @return
    */
   public Object findDataValue(AccessPointProperties accessPoint)
   {
      if (accessPoint.getAccessPointLocation().equals(ACCESS_POINT_HEADERS))
      {
         String headerName = null;
         if (accessPoint.getAccessPointPath() != null)
         {

            int startIndex = accessPoint.getAccessPointPath().indexOf("get");
            int endIndex = accessPoint.getAccessPointPath().lastIndexOf("()");
            headerName = accessPoint.getAccessPointPath().substring(startIndex + 3,
                  endIndex);
         }
         return exchange.getIn().getHeader(headerName);
      }
      else if (accessPoint.getAccessPointLocation().equals(ACCESS_POINT_MESSAGE))
      {
         return exchange.getIn().getBody();
      }
      return null;
   }
}
