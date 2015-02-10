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
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.query.BusinessObjectQuery;
import org.eclipse.stardust.engine.api.query.BusinessObjects;
import org.eclipse.stardust.engine.api.runtime.BusinessObject;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.runtime.scheduling.ScheduledDocument;
import org.eclipse.stardust.engine.core.runtime.scheduling.SchedulingUtils;

import com.google.gson.JsonObject;

public class ScheduledCalendar extends ScheduledDocument
{
   private List<JsonObject> events;
   private Map<String, Object> data;

   public ScheduledCalendar(JsonObject documentJson, QName owner, String documentName, List<JsonObject> events)
   {
      super(documentJson, owner, documentName);
      this.events = events;
   }

   public void execute()
   {
      for (JsonObject event : events)
      {
         JsonObject details = SchedulingUtils.getAsJsonObject(event, "eventDetails");
         if (details != null)
         {
            String processId = SchedulingUtils.getAsString(details, "processDefinitionId");
            if (!StringUtils.isEmpty(processId))
            {
               Map<String, Object> businessObjectData = getBusinessObjectData();
               getWorkflowService().startProcess(processId, businessObjectData, false);
            }
         }
      }
   }

   protected Map<String, Object> getBusinessObjectData()
   {
      if (data == null)
      {
         data = CollectionUtils.newMap();
         JsonObject businessObjectJson = SchedulingUtils.getAsJsonObject(getDocumentJson(), "businessObjectInstance");
         if (businessObjectJson != null)
         {
            String businessObjectId = SchedulingUtils.getAsString(businessObjectJson, "businessObjectId");
            String modelId = SchedulingUtils.getAsString(businessObjectJson, "modelId");
            String primaryKey = SchedulingUtils.getAsString(businessObjectJson, "primaryKey");

            BusinessObjectQuery query = BusinessObjectQuery.findWithPrimaryKey(
                  new QName(modelId, businessObjectId).toString(), primaryKey);
            query.setPolicy(new BusinessObjectQuery.Policy(BusinessObjectQuery.Option.WITH_VALUES));
            BusinessObjects result = getQueryService().getAllBusinessObjects(query);
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
      return data;
   }

   protected WorkflowService getWorkflowService()
   {
      return getServiceFactory(getUser()).getWorkflowService();
   }

   protected QueryService getQueryService()
   {
      return getServiceFactory(getUser()).getQueryService();
   }
}