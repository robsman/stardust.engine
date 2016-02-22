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

   /**
    * Will create a list from the provided list. when the AccessPoint ID contains ":", the
    * list field name is set to the mapping id. otherwise the list is registered with the
    * same id as the AccessPoint ID when stardust:sqlScriptingOverlay::outputType is set
    * to SelectOne: the elemeent will be used instead of the full list.
    */
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
                  && extendedAttributes
                        .containsKey("stardust:sqlScriptingOverlay::outputType")
                  && ((String) extendedAttributes
                        .get("stardust:sqlScriptingOverlay::outputType"))
                              .equals("SelectOne"))
            {
               this.replaceDataValue(mapping, ((List) value).get(0), extendedAttributes);
            }
         }
         dataTypeMap = putEntry(mapping, value);
         this.replaceDataValue(mapping, dataTypeMap, extendedAttributes);
      }
   }

   private Map<String, Object> putEntry(DataMapping mapping, Object value)
   {
      Map<String, Object> dataTypeMap = new HashMap<String, Object>();
      if (mapping.getApplicationAccessPoint().getId().contains(":"))
      {
         dataTypeMap.put(mapping.getId(), value);
      }
      else
      {
         dataTypeMap.put(mapping.getApplicationAccessPoint().getId(), value);
      }
      return dataTypeMap;
   }

   @Override
   public void marshal(DataMapping mapping, Map<String, Object> extendedAttributes)
   {
      Object value = this.findDataValue(mapping, extendedAttributes);
      if (value instanceof Map< ? , ? >)
      {
         Object listObject = ((Map) value)
               .get(mapping.getApplicationAccessPoint().getId());
         if (listObject instanceof List< ? >)
         {
            this.replaceDataValue(mapping, listObject, extendedAttributes);
         }
      }

   }

}
