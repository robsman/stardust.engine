/*
 * $Id$
 * (C) 2000 - 2013 CARNOT AG
 */
package org.eclipse.stardust.engine.extensions.camel.converter;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SCRIPTING_LANGUAGE_EA_KEY;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.PYTHON;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.apache.camel.Exchange;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.core.struct.ClientXPathMap;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.extensions.camel.trigger.AccessPointProperties;
import org.mozilla.javascript.IdScriptableObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class JsonTypeConverter
{
   public static String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
   public static String LONG_DATA_FORMAT = "LONG";

   static class ApplicationTypeConverter extends AbstractIApplicationTypeConverter
   {

      private String dateFormat = ISO_DATE_FORMAT;

      public ApplicationTypeConverter(Exchange exchange)
      {
         super(exchange);
         this.dateFormat = ISO_DATE_FORMAT;
      }

      public ApplicationTypeConverter(Exchange exchange, String dateFormat)
      {
         super(exchange);
         this.dateFormat = dateFormat;
      }

      @Override
      public void unmarshal(DataMapping dataMapping,
            Map<String, Object> extendedAttributes)
      {
         if (isStuctured(dataMapping) || isPrimitive(dataMapping))
         {
            String typeDeclarationId = this.getTypeDeclarationId(dataMapping);

            String accessPointId = dataMapping.getApplicationAccessPoint().getId();

            if (typeDeclarationId != null)
            {
               String json = (String) findDataValue(dataMapping, extendedAttributes);

               if (json != null)
               {
                  long modelOid = new Long(dataMapping.getModelOID());
                  SDTConverter converter = new SDTConverter(dataMapping, modelOid);
                  Map<String, Object> complexType = parseJson(converter, json,
                        accessPointId);
                  replaceDataValue(dataMapping, complexType, extendedAttributes);
               }

            }
            else
            {
               // primitive
               Object response = findDataValue(dataMapping, extendedAttributes);

               if (response != null)
               {
                  replaceDataValue(dataMapping, response, extendedAttributes);
               }

            }
         }
      }

      @Override
      public void marshal(DataMapping dataMapping, Map<String, Object> extendedAttributes)
      {
         if (isStuctured(dataMapping))
         {
            Object dataMap = findDataValue(dataMapping, extendedAttributes);

            if (dataMap != null)
            {
               GsonBuilder gsonBuilder = new GsonBuilder();
               if (LONG_DATA_FORMAT.equals(this.dateFormat))
               {
                  gsonBuilder.registerTypeAdapter(Date.class, new JsonSerializer<Date>()
                  {

                     @Override
                     public JsonElement serialize(Date date, Type type,
                           JsonSerializationContext context)
                     {
                        return date != null ? new JsonPrimitive("/Date(" + date.getTime()
                              + ")/") : null;
                     }
                  });
               }
               else
               {
                  gsonBuilder.setDateFormat(this.dateFormat).create();
               }
               
               if(extendedAttributes.get(SCRIPTING_LANGUAGE_EA_KEY)!=null && StringUtils.isNotEmpty((String)extendedAttributes.get(SCRIPTING_LANGUAGE_EA_KEY)) && 
            		   ((String)extendedAttributes.get(SCRIPTING_LANGUAGE_EA_KEY)).equals(PYTHON)
            		   ){
            	   gsonBuilder.registerTypeAdapter(Boolean.class, new JsonSerializer<Boolean>()
                           {

                              @Override
                              public JsonElement serialize(Boolean val, Type type,
                                    JsonSerializationContext context)
                              {
                                  return val ? new JsonPrimitive("True") : new JsonPrimitive("False");
                              }
                           });
               }
               Gson gson=gsonBuilder.create();
               
               String json = null;
               long modelOid = new Long(dataMapping.getModelOID());
               SDTConverter converter = new SDTConverter(dataMapping, modelOid);
               Set<TypedXPath> allXPaths=converter.getxPathMap().getAllXPaths();
               IXPathMap xPathMap = new ClientXPathMap(allXPaths);
               TypedXPath rootXPath=    xPathMap.getRootXPath();
               if (dataMap instanceof IdScriptableObject)
               {
//                  long modelOid = new Long(dataMapping.getModelOID());
//                  SDTConverter converter = new SDTConverter(dataMapping, modelOid);
//                  Set<TypedXPath> allXPaths=converter.getxPathMap().getAllXPaths();
//                  IXPathMap xPathMap = new ClientXPathMap(allXPaths);
//                  TypedXPath rootXPath=    xPathMap.getRootXPath();
                  json = gson.toJson(ScriptValueConverter.unwrapValue(dataMap,rootXPath));
               }else if((!rootXPath.getEnumerationValues().isEmpty())||dataMap instanceof String){
                  json=(String) dataMap;
               }
               else
               {
            	   json = gson.toJson(dataMap);
               }
               replaceDataValue(dataMapping, json, extendedAttributes);
            }
         }
      }
   }

   static class TriggerTypeConverter extends AbstractITriggerTypeConverter
   {

      public TriggerTypeConverter(Exchange exchange)
      {
         super(exchange);
      }

      @Override
      public void unmarshal(IModel iModel, AccessPointProperties accessPoint)
      {
         if (accessPoint.getAccessPointType().equalsIgnoreCase("struct"))
         {
            Object value = findDataValue(accessPoint);
            String jsonInput = exchange.getContext().getTypeConverter()
                  .convertTo(String.class, value);
            if (jsonInput != null)
            {
               SDTConverter converter = new SDTConverter(iModel, accessPoint.getData()
                     .getId());
               Map<String, Object> complexType = parseJson(converter, jsonInput,
                     accessPoint.getParamId());
               replaceDataValue(accessPoint, complexType);
            }
         }
         else if (accessPoint.getAccessPointType().equalsIgnoreCase("primitive"))
         {
            Object response = findDataValue(accessPoint);

            if (response != null)
            {
               replaceDataValue(accessPoint, response);
            }
         }
      }
   }
}
