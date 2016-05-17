package org.eclipse.stardust.engine.extensions.json;

import java.io.Reader;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.*;

import org.apache.commons.lang.BooleanUtils;

import com.google.gson.*;

public class GsonHandler
{

   private final Gson gson = new GsonBuilder()
         .registerTypeAdapter(Map.class, new MapSerializeHandler())// .setPrettyPrinting()
         .registerTypeAdapter(Object.class, new ObjectDeserializer()).create();

   public Gson gson()
   {
      return gson;
   }

   public String toJson(final Object o)
   {
      return gson.toJson(o);
   }

   public <T> T fromJson(String json, Class<T> classOfT)
   {
      return gson.fromJson(json, classOfT);
   }

   public <T> T fromJson(Reader json, Class<T> classOfT)
   {
      return gson.fromJson(json, classOfT);
   }

   private static class ObjectDeserializer implements JsonDeserializer<Object>
   {
      public Object deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context)
      {
         if (json.isJsonNull())
            return null;
         else if (json.isJsonPrimitive())
            return handlePrimitive(json.getAsJsonPrimitive());
         else if (json.isJsonArray())
            return handleArray(json.getAsJsonArray(), context);
         else
            return handleObject(json.getAsJsonObject(), context);
      }

      private Object handlePrimitive(JsonPrimitive json)
      {
         if (json.isBoolean())
            return json.getAsBoolean();
         else if (json.isString())
         {
            if (BooleanUtils.toBooleanObject(json.getAsString()) != null)
               return BooleanUtils.toBoolean(json.getAsString());
            return json.getAsString();
         }
         else
         {
            BigDecimal bigDec = json.getAsBigDecimal();
            try
            {
               bigDec.toBigIntegerExact();
               try
               {
                  return bigDec.intValueExact();
               }
               catch (ArithmeticException e)
               {
               }
               return bigDec.longValue();
            }
            catch (ArithmeticException e)
            {
            }
            return bigDec.doubleValue();
         }
      }

      private Object handleArray(JsonArray json, JsonDeserializationContext context)
      {
         List<Object> array = new ArrayList<Object>();
         for (int i = 0; i < json.size(); i++)
            array.add(context.deserialize(json.get(i), Object.class));
         return array;
      }

      private Object handleObject(JsonObject json, JsonDeserializationContext context)
      {
         Map<String, Object> map = new HashMap<String, Object>();
         for (Map.Entry<String, JsonElement> entry : json.entrySet())
            map.put(entry.getKey(), context.deserialize(entry.getValue(), Object.class));
         return map;
      }
   }

   class MapSerializeHandler implements JsonSerializer<Map>
   {
      public JsonElement serialize(Map src, Type typeOfSrc,
            JsonSerializationContext context)
      {
         JsonObject map = new JsonObject();
         Type childGenericType = null;
         for (Map.Entry entry : (Set<Map.Entry>) src.entrySet())
         {
            Object value = entry.getValue();
            JsonElement valueElement;
            if (value == null)
            {
               valueElement = createJsonNull();
            }
            else
            {
               Type childType = determineChildType(childGenericType, value);
               valueElement = context.serialize(value, childType);
            }
            map.add(String.valueOf(entry.getKey()), valueElement);
         }
         return map;
      }
   }

   private Type determineChildType(Type childGenericType, Object value)
   {
      if (childGenericType == null)
         return value.getClass();
      else
      {
         return childGenericType;
      }
   }

   static JsonNull createJsonNull()
   {
      return new JsonNull();
   }
}
