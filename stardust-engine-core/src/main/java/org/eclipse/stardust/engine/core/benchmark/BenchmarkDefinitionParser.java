/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Roland.Stamm (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.benchmark;

import java.io.Serializable;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.eclipse.stardust.vfs.impl.utils.CollectionUtils;

public class BenchmarkDefinitionParser
{


   // Benchmark Definition First Level Elements
   private static final String JSON_DEFAULTS = "defaults";
   
   private static final String JSON_CATEGORIES = "categories";
   
   // Benchmark Definition Category Elements
   private static final String CATEGORY_KEY_COLOR = "color";
   
   private static final String CATEGORY_KEY_NAME = "name";
   
   private static final String CATEGORY_INDEX = "index";
   
   private static final String CATEGORY_KEY_ID = "id";

   // Benchmark Definition Global Elements
   private static final String ACTIVITIES_GLOBAL_KEY = "activities";
   
   private static final String PROCESS_DEFINITIONS_GLOBAL_KEY = "processDefinitions";
   
   private static final String CATEGORY_CONDITIONS = "categoryConditions";
   
   // Benchmark Condition Elements
   private static final String CONDITION_TYOE_KEY_FREEFORM_EXPRESSION = "freeformExpression";
   
   private static final String CONDITION_TYPE_KEY_FREEFORM = "freeform";
   
   private static final String CONDITION_TYPE = "type";
   
   private static final String CONDITION_CATEGORY_ID = "categoryId";

   public static void parse(BenchmarkDefinition benchmarkDefinition, byte[] contentBytes)
   {
      JsonObject json = getDocumentJson(contentBytes);
      if (json != null)
      {

         Map<String, Integer> categoryIndexMap = CollectionUtils.newMap();

         // Parse for categories
         JsonArray jsonCategories = json.getAsJsonArray(JSON_CATEGORIES);

         for (JsonElement jsonElement : jsonCategories)
         {
            JsonObject categoryObject = jsonElement.getAsJsonObject();

            int index = categoryObject.get(CATEGORY_INDEX).getAsInt();

            String idValue = categoryObject.get(CATEGORY_KEY_ID).getAsString();
            String nameValue = categoryObject.get(CATEGORY_KEY_NAME).getAsString();
            String colorValue = categoryObject.get(CATEGORY_KEY_COLOR).getAsString();

            Map<String, Serializable> propertyMap = CollectionUtils.newMap();
            propertyMap.put(CATEGORY_KEY_NAME, nameValue);
            propertyMap.put(CATEGORY_KEY_COLOR, colorValue);
            propertyMap.put(CATEGORY_KEY_ID, idValue);

            benchmarkDefinition.properties.put(index, propertyMap);

            categoryIndexMap.put(idValue, index);

         }

         JsonObject jsonDefaults = json.getAsJsonObject(JSON_DEFAULTS);

         // Parse for global process conditions
         JsonObject pdDefaults = jsonDefaults.getAsJsonObject(PROCESS_DEFINITIONS_GLOBAL_KEY);

         JsonArray pdCategoryConditions = pdDefaults.getAsJsonArray(CATEGORY_CONDITIONS);

         for (JsonElement jsonElement : pdCategoryConditions)
         {
            JsonObject conditionObject = jsonElement.getAsJsonObject();

            String categoryId = conditionObject.get(CONDITION_CATEGORY_ID).getAsString();

            if (categoryIndexMap.containsKey(categoryId))
            {
               if (conditionObject.get(CONDITION_TYPE).getAsString().equals(CONDITION_TYPE_KEY_FREEFORM))
               {
                  benchmarkDefinition.globalActivityConditions.put(
                        categoryIndexMap.get(categoryId), new FreeFormCondition(
                              conditionObject.get(CONDITION_TYOE_KEY_FREEFORM_EXPRESSION).getAsString()));
               }
               else
               {
                  benchmarkDefinition.globalProcessConditions.put(
                        categoryIndexMap.get(categoryId), new NoBenchmarkCondition());
               }
            }
         }
         System.out.println(benchmarkDefinition.globalProcessConditions);

         // Parse for global activity conditions
         JsonObject aDefaults = jsonDefaults.getAsJsonObject(ACTIVITIES_GLOBAL_KEY);

         JsonArray aCategoryConditions = aDefaults.getAsJsonArray(CATEGORY_CONDITIONS);

         for (JsonElement jsonElement : aCategoryConditions)
         {
            JsonObject conditionObject = jsonElement.getAsJsonObject();

            String categoryId = conditionObject.get(CONDITION_CATEGORY_ID).getAsString();

            if (categoryIndexMap.containsKey(categoryId))
            {
               if (conditionObject.get(CONDITION_TYPE).getAsString().equals(CONDITION_TYPE_KEY_FREEFORM))
               {
                  benchmarkDefinition.globalActivityConditions.put(
                        categoryIndexMap.get(categoryId), new FreeFormCondition(
                              conditionObject.get(CONDITION_TYOE_KEY_FREEFORM_EXPRESSION).getAsString()));
               }
               else
               {
                  benchmarkDefinition.globalActivityConditions.put(
                        categoryIndexMap.get(categoryId), new NoBenchmarkCondition());
               }
            }
         }
         System.out.println(benchmarkDefinition.globalActivityConditions);

      }
   }

   protected static JsonObject getDocumentJson(byte[] contentBytes)
   {
      String content;
      try
      {
         content = new String(contentBytes, "UTF-8");
      }
      catch (Exception e)
      {
         content = new String(contentBytes);
      }

      JsonObject jsonObject;
      try
      {
         JsonParser jsonParser = new JsonParser();
         jsonObject = jsonParser.parse(content).getAsJsonObject();
      }
      catch (Exception e)
      {
         jsonObject = null;
      }

      return jsonObject;
   }

}
