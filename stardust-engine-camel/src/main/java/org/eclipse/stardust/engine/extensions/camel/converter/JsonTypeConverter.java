/*
 * $Id$
 * (C) 2000 - 2013 CARNOT AG
 */
package org.eclipse.stardust.engine.extensions.camel.converter;

import java.util.ArrayList;
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

public class JsonTypeConverter extends AbstractBpmTypeConverter
{
   public JsonTypeConverter(Exchange exchange)
   {
      super(exchange);
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
               SDTConverter converter = new SDTConverter(typeDeclarationId, modelOid);

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
            gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").create();

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
