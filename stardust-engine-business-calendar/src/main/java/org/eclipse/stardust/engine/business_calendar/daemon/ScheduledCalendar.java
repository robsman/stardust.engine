/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Florin.Herinean (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.engine.business_calendar.daemon;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.query.BusinessObjectQuery;
import org.eclipse.stardust.engine.api.query.BusinessObjects;
import org.eclipse.stardust.engine.api.runtime.BusinessObject;
import org.eclipse.stardust.engine.core.runtime.scheduling.ScheduledDocument;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ScheduledCalendar extends ScheduledDocument
{
   public ScheduledCalendar(JsonObject documentJson, QName owner, String documentName)
   {
      super(documentJson, owner, documentName);
   }

   public void execute()
   {
      JsonElement json = getDocumentJson().get("processData");
      if (json != null && json.isJsonObject())
      {
         Map<String, Object> data = CollectionUtils.newMap();

         JsonObject processJson = json.getAsJsonObject();
         String processId = processJson.get("processDefinitionFullId").getAsString();

         JsonArray businessObjectsArray = processJson.getAsJsonArray("businessObjectInstances");
         if (businessObjectsArray != null)
         {
            for (int n = 0; n < businessObjectsArray.size(); ++n)
            {
               JsonObject businessObjectJson = businessObjectsArray.get(n).getAsJsonObject();

               String businessObjectId = businessObjectJson.get("businessObjectId").getAsString();
               String modelId = businessObjectJson.get("modelId").getAsString();
               String primaryKey = businessObjectJson.get("primaryKey").getAsString();

               BusinessObjectQuery query = BusinessObjectQuery.findWithPrimaryKey(
                     new QName(modelId, businessObjectId).toString(), primaryKey);
               query.setPolicy(new BusinessObjectQuery.Policy(BusinessObjectQuery.Option.WITH_VALUES));
               BusinessObjects result = getServiceFactory(getUser()).getQueryService().getAllBusinessObjects(query);
               if (!result.isEmpty())
               {
                  BusinessObject bo = result.get(0);
                  List<BusinessObject.Value> values = bo.getValues();
                  if (values != null && !values.isEmpty())
                  {
                     BusinessObject.Value value = values.get(0);
                     if (value.getValue() != null)
                     {
                        data.put(businessObjectId, value.getValue());
                     }
                  }
               }
            }
         }

         getServiceFactory(getUser()).getWorkflowService().startProcess(processId, data, false);
      }
   }
}