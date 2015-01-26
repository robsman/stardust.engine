/*
 * $Id$
 * (C) 2000 - 2013 CARNOT AG
 */
package org.eclipse.stardust.engine.extensions.camel.converter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.eclipse.stardust.engine.api.model.DataMapping;

public class ListTypeConverter extends AbstractIApplicationTypeConverter
{

   public ListTypeConverter(Exchange exchange)
   {
      super(exchange);
   }

   @Override
   public void unmarshal(DataMapping mapping, Map<String, Object> extendedAttributes)
   {
      Object value = this.findDataValue(mapping, extendedAttributes);
      if (value instanceof List< ? >)
      {
         Map<String, Object> dataTypeMap = new HashMap<String, Object>();
         if (((List) value).size() == 1 && ((List) value).get(0) instanceof Map)
         {
            if (extendedAttributes != null
                  && extendedAttributes.containsKey("stardust:sqlScriptingOverlay::outputType")
                  && ((String) extendedAttributes.get("stardust:sqlScriptingOverlay::outputType")).equals("SelectOne"))
            {
               this.replaceDataValue(mapping, ((List) value).get(0), extendedAttributes);
            }
            else
            {
               dataTypeMap.put(mapping.getApplicationAccessPoint().getId(), value);
               this.replaceDataValue(mapping, dataTypeMap, extendedAttributes);
            }
         }
         else
         {
            dataTypeMap.put(mapping.getApplicationAccessPoint().getId(), value);
            this.replaceDataValue(mapping, dataTypeMap, extendedAttributes);
         }
      }

   }

   @Override
   public void marshal(DataMapping mapping, Map<String, Object> extendedAttributes)
   {
      Object value = this.findDataValue(mapping, extendedAttributes);
      if (value instanceof Map< ? , ? >)
      {
         Object listObject = ((Map) value).get(mapping.getApplicationAccessPoint().getId());
         if (listObject instanceof List< ? >)
         {
            this.replaceDataValue(mapping, listObject, extendedAttributes);
         }
      }

   }

}
