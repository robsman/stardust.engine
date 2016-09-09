/*******************************************************************************
 * Copyright (c) 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Florin.Herinean (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.test.calendarconditions;

import java.io.StringWriter;

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;

public class JsonUtil
{
   public static String toPrettyString(JsonElement json)
   {
      Gson gson = new Gson();
      StringWriter writer = new StringWriter();
      JsonWriter jsonWriter = new JsonWriter(writer);
      jsonWriter.setIndent("   ");
      jsonWriter.setLenient(true);
      gson.toJson(json, jsonWriter);
      return writer.toString();
   }

   public static JsonObject json(Property... properties)
   {
      JsonObject jsonObject = new JsonObject();
      if (properties != null)
      {
         for (Property property : properties)
         {
            if (property.value instanceof Boolean)
            {
               jsonObject.addProperty(property.name, (Boolean) property.value);
            }
            else if (property.value instanceof Character)
            {
               jsonObject.addProperty(property.name, (Character) property.value);
            }
            else if (property.value instanceof Number)
            {
               jsonObject.addProperty(property.name, (Number) property.value);
            }
            else if (property.value instanceof String)
            {
               jsonObject.addProperty(property.name, (String) property.value);
            }
            else if (property.value instanceof JsonElement)
            {
               jsonObject.add(property.name, (JsonElement) property.value);
            }
         }
      }
      return jsonObject;
   }

   public static JsonArray array(Object... values)
   {
      JsonArray jsonObject = new JsonArray();
      if (values != null)
      {
         for (Object value : values)
         {
            if (value instanceof Boolean)
            {
               jsonObject.add(new JsonPrimitive((Boolean) value));
            }
            else if (value instanceof Character)
            {
               jsonObject.add(new JsonPrimitive((Character) value));
            }
            else if (value instanceof Number)
            {
               jsonObject.add(new JsonPrimitive((Number) value));
            }
            else if (value instanceof String)
            {
               jsonObject.add(new JsonPrimitive((String) value));
            }
            else if (value instanceof JsonElement)
            {
               jsonObject.add((JsonElement) value);
            }
         }
      }
      return jsonObject;
   }

   public static Property property(String name, Object value)
   {
      return new Property(name, value);
   }

   public static class Property
   {
      String name;
      Object value;

      public Property(String name, Object value)
      {
         super();
         this.name = name;
         this.value = value;
      }
   }
}