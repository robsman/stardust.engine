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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class BenchmarkDefinitionParser
{

   public static void parse(BenchmarkDefinition benchmarkDefinition, byte[] contentBytes)
   {
      JsonObject json = getDocumentJson(contentBytes);
      if (json != null)
      {

         JsonElement jsonColumns = json.get("columns");
         JsonElement jsonConditions = json.get("conditions");
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
