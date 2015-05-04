/*
 * $Id$
 * (C) 2000 - 2013 CARNOT AG
 */
package org.eclipse.stardust.engine.extensions.camel.converter;

import java.util.Map;
import java.util.Set;

import org.apache.camel.Exchange;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.core.struct.ClientXPathMap;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.TypedXPath;

public class JavaScriptTypeConverter extends JsonTypeConverter.ApplicationTypeConverter
{
   public JavaScriptTypeConverter(Exchange exchange, String dateFormat)
   {
      super(exchange, dateFormat);
   }

   @Override
   public void unmarshal(DataMapping dataMapping, Map<String, Object> extendedAttributes)
   {
      Object javascriptResponse = findDataValue(dataMapping, extendedAttributes);
      if (javascriptResponse != null && (isStuctured(dataMapping)))
      {
         long modelOid = new Long(dataMapping.getModelOID());
         SDTConverter converter = new SDTConverter(dataMapping, modelOid);
         Set<TypedXPath> allXPaths = converter.getxPathMap().getAllXPaths();
         IXPathMap xPathMap = new ClientXPathMap(allXPaths);
         TypedXPath rootXPath = xPathMap.getRootXPath();
         javascriptResponse = ScriptValueConverter.unwrapValue(javascriptResponse,
               rootXPath);
      }
      replaceDataValue(dataMapping, javascriptResponse, extendedAttributes);
   }

   @Override
   public void marshal(DataMapping dataMapping, Map<String, Object> extendedAttributes)
   {
      super.marshal(dataMapping, extendedAttributes);
   }

}
