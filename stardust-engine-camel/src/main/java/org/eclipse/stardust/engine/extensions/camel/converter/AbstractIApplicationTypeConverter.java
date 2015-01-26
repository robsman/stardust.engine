package org.eclipse.stardust.engine.extensions.camel.converter;

import java.io.InputStream;
import java.util.*;

import org.apache.camel.Exchange;
import org.apache.camel.TypeConverter;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;

public abstract class AbstractIApplicationTypeConverter extends AbstractBpmTypeConverter
      implements IApplicationTypeConverter
{

   protected AbstractIApplicationTypeConverter(Exchange exchange)
   {
      super(exchange);
   }

   public Object findDataValue(DataMapping mapping, Map<String, Object> extendedAttributes)
   {

      Object dataValue = null;

      if (inBody(mapping, extendedAttributes))
      {
         dataValue = this.exchange.getIn().getBody();
      }
      else
      {
         dataValue = this.exchange.getIn().getHeader(
               mapping.getApplicationAccessPoint().getId());
      }

      if (dataValue instanceof InputStream)
      {
         TypeConverter converter = this.exchange.getContext().getTypeConverter();
         dataValue = converter.convertTo(String.class, dataValue);
      }

      return dataValue;

   }

   public void replaceDataValue(DataMapping mapping, Object dataValue,
         Map<String, Object> extendedAttributes)
   {
      if (inBody(mapping, extendedAttributes))
      {
         exchange.getOut().setBody(dataValue);
      }
      else
      {
         exchange.getOut().setHeader(mapping.getApplicationAccessPoint().getId(),
               dataValue);
      }
   }

   private boolean inBody(DataMapping mapping, Map<String, Object> extendedAttributes)
   {
      String bodyAccessPoint = null;

      boolean multipleAccessPoints = false;

      if (extendedAttributes.get(CamelConstants.SUPPORT_MULTIPLE_ACCESS_POINTS) != null)
      {
         multipleAccessPoints = (Boolean) extendedAttributes
               .get(CamelConstants.SUPPORT_MULTIPLE_ACCESS_POINTS);
      }

      if (multipleAccessPoints)
      {

         if (Direction.IN.equals(mapping.getDirection()))
         {
            if (extendedAttributes.get(CamelConstants.CAT_BODY_IN_ACCESS_POINT) != null)
               bodyAccessPoint = (String) extendedAttributes
                     .get(CamelConstants.CAT_BODY_IN_ACCESS_POINT);
         }
         else
         {
            if (extendedAttributes.get(CamelConstants.CAT_BODY_OUT_ACCESS_POINT) != null)
               bodyAccessPoint = (String) extendedAttributes
                     .get(CamelConstants.CAT_BODY_OUT_ACCESS_POINT);
         }

         if (bodyAccessPoint != null && !"".equals(bodyAccessPoint))
         {
            return bodyAccessPoint.equals(mapping.getApplicationAccessPoint().getId());
         }
         else
         {
            return false;
         }

      }

      return true;
   }

}
