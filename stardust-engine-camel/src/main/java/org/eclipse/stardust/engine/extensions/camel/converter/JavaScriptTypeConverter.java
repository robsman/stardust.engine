/*
 * $Id$
 * (C) 2000 - 2013 CARNOT AG
 */
package org.eclipse.stardust.engine.extensions.camel.converter;

import java.util.Map;

import org.apache.camel.Exchange;
import org.eclipse.stardust.engine.api.model.DataMapping;

public class JavaScriptTypeConverter extends JsonTypeConverter.ApplicationTypeConverter
{
   public JavaScriptTypeConverter(Exchange exchange, String dateFormat)
   {
      super(exchange, dateFormat);
   }

   @Override
   public void unmarshal(DataMapping dataMapping, Map<String, Object> extendedAttributes)
   {
      if (isStuctured(dataMapping))
      {
         super.marshal(dataMapping, extendedAttributes);
         super.unmarshal(dataMapping, extendedAttributes);
      }
   }

   @Override
   public void marshal(DataMapping dataMapping, Map<String, Object> extendedAttributes)
   {
      super.marshal(dataMapping, extendedAttributes);
   }

}
