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
               JsonObject relationship = SchedulingUtils.getAsJsonObject(details, "relationship");
               if (relationship != null && !businessObjectData.isEmpty())
               {
                  String other = SchedulingUtils.getAsString(relationship, "otherBusinessObject");
                  if (!StringUtils.isEmpty(other))
                  {
                     String fkField = SchedulingUtils.getAsString(relationship, "otherForeignKeyField");
                     if (!StringUtils.isEmpty(fkField))
                     {
                        String dataName = QName.valueOf(other).getLocalPart();
                        @SuppressWarnings("unchecked")
                        Map<String, Object> businessObjectValue = (Map<String, Object>) businessObjectData.values().iterator().next();
                        @SuppressWarnings("unchecked")
                        List<Object> fks = (List<Object>) businessObjectValue.get(fkField);
                        for (Object fk : fks)
                        {
                           Map<String, Object> data = CollectionUtils.newMap();
                           Object value = getBusinessObjectValue(other, fk);
                           if (value != null)
                           {
                              data.put(dataName, value);
                              getWorkflowService().startProcess(processId, data, false);
                           }
                        }
                     }
                  }
               }
               else
               {
                  getWorkflowService().startProcess(processId, businessObjectData, false);
               }
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

            String boFullId = new QName(modelId, businessObjectId).toString();
            Object value = getBusinessObjectValue(boFullId, primaryKey);
            if (value != null)
            {
               data.put(businessObjectId, value);
            }
         }
      }
      return data;
   }

   protected Object getBusinessObjectValue(String businessObject, Object primaryKey)
   {
      BusinessObjectQuery query = BusinessObjectQuery.findWithPrimaryKey(businessObject, primaryKey);
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
               return value.getValue();
            }
         }
      }
      return null;
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