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
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
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
   private static final Logger trace = LogManager.getLogger(ScheduledCalendar.class);

   private static final Object NO_BUSINESS_OBJECT = new Object();

   private List<JsonObject> events;
   private Map<String, Object> data;
   private String documentPath;

   public ScheduledCalendar(JsonObject documentJson, QName owner, String documentName, String documentPath, List<JsonObject> events)
   {
      super(documentJson, owner, documentName);
      this.documentPath = documentPath;
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
               if (businessObjectData != null)
               {
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
                           Object value = businessObjectValue.get(fkField);
                           if (value instanceof List<?>)
                           {
                              @SuppressWarnings("unchecked")
                              List<?> fks = (List<Object>) value;
                              for (Object fk : fks)
                              {
                                 Map<String, Object> data = CollectionUtils.newMap();
                                 value = getBusinessObjectValue(other, fk);
                                 if (value != NO_BUSINESS_OBJECT)
                                 {
                                    data.put(dataName, value);
                                    startProcess(processId, data);
                                 }
                              }
                           }
                           else
                           {
                              trace.warn("'" + documentPath + "': relationship '" + fkField + "' is not a list.");
                           }
                        }
                     }
                  }
                  else
                  {
                     startProcess(processId, businessObjectData);
                  }
               }
            }
         }
      }
   }

   protected void startProcess(String processId, Map<String, Object> businessObjectData)
   {
      try
      {
         getWorkflowService().startProcess(processId, businessObjectData, false);
      }
      catch (Exception ex)
      {
         trace.warn("'" + documentPath + "': could not start scheduled process '" + processId + "'.");
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
            String primaryKey = SchedulingUtils.getAsString(businessObjectJson, "primaryKey");
            if (!StringUtils.isEmpty(businessObjectId) && !StringUtils.isEmpty(primaryKey))
            {
               Object value = getBusinessObjectValue(businessObjectId, primaryKey);
               if (value == NO_BUSINESS_OBJECT)
               {
                  return null;
               }
               if (value != null)
               {
                  data.put(businessObjectId, value);
               }
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
      if (result.isEmpty())
      {
         trace.warn("'" + documentPath + "': no value found for business object '" + businessObject + "' with primary key '" + primaryKey + "'.");
         return NO_BUSINESS_OBJECT;
      }
      BusinessObject bo = result.get(0);
      List<BusinessObject.Value> values = bo.getValues();
      if (values != null && !values.isEmpty())
      {
         BusinessObject.Value value = values.get(0);
         return value.getValue();
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