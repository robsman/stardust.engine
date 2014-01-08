/*
 * $Id$
 * (C) 2000 - 2013 CARNOT AG
 */
package org.eclipse.stardust.engine.extensions.camel.converter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.mozilla.javascript.NativeObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class JsonTypeConverter extends AbstractBpmTypeConverter
{
   public static String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
   public static String LONG_DATA_FORMAT = "LONG";
   
   private String dateFormat = ISO_DATE_FORMAT;
	
   public JsonTypeConverter(Exchange exchange)
   {
      super(exchange);
      this.dateFormat = ISO_DATE_FORMAT;
   }
   
   public JsonTypeConverter(Exchange exchange, String dateFormat)
   {
      super(exchange);
      this.dateFormat = dateFormat;
   }

   @Override
   public void unmarshal(DataMapping dataMapping, Map<String, Object> extendedAttributes)
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

               JsonParser parser = new JsonParser();
               JsonElement element = parser.parse(json);

               // TODO: is that the correct OID?
               long modelOid = new Long(dataMapping.getModelOID());
               SDTConverter converter = new SDTConverter(dataMapping, modelOid);

               Map<String, Object> complexType = new HashMap<String, Object>();

               if (element.isJsonObject()) // is root already
               {

                  JsonObject jObj = element.getAsJsonObject();
                  processJsonObject(jObj, complexType, "", converter.getxPathMap());

               }
               else if (element.isJsonArray())
               {

                  List<Object> list = new ArrayList<Object>();
                  processJsonArray(element.getAsJsonArray(), list, accessPointId, converter.getxPathMap());
                  complexType.put(accessPointId, list);

               }

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
            	gsonBuilder.registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
            		
					@Override
					public JsonElement serialize(Date date, Type type,
							JsonSerializationContext context) {
						return date != null ? 
								new JsonPrimitive("/Date(" + date.getTime() + ")/") : null;
					}
				});
            }
            else
            {
            	gsonBuilder.setDateFormat(this.dateFormat).create();
            }

            Gson gson = gsonBuilder.create();
            String json = null;

            if (dataMap instanceof NativeObject)
            {
               json = gson.toJson(ScriptValueConverter.unwrapValue(dataMap));
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
